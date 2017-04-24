package Server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Math {
	public int add(int x, int y){
		return (x + y);
	}
	
	public int subtract(int x, int y){
		return (x - y);
	}
	
	public int multiply(int x, int y){
		return (x * y);
		
	}
	
	public static JSONObject parseCommand(JSONObject command, DataOutputStream output) throws IOException {
		JSONObject result = new JSONObject();
		if (!command.containsKey("resource") ||
				!((HashMap)command.get("resource")).containsKey("name") ||
				!((HashMap)command.get("resource")).containsKey("tags") ||
				!((HashMap)command.get("resource")).containsKey("description") ||
				!((HashMap)command.get("resource")).containsKey("uri")||
				!((HashMap)command.get("resource")).containsKey("channel")||
				!((HashMap)command.get("resource")).containsKey("owner")||
				!((HashMap)command.get("resource")).containsKey("ezserver")) {
		}
		//this solves generic response
		if (command.containsKey("command")) {
			switch((String) command.get("command")) {
			//each case handles more explicit situation
			case "PUBLISH":
				publishJSON(command);
				break;
			case "REMOVE":
				removeJSON(command);
				break;
			case "SHARE":
				shareJSON(command);
				break;
			case "QUERY":
				break;
			case "FETCH":
				break;
			case "EXCHANGE":
				break;
			default:
				//return invalid command
				result.put("response", "error");
				result.put("errorMessage", "invalid command");
				output.writeUTF(result.toJSONString());
				break;
			}
		} else {
			//return missing or incorrect type
			result.put("response", "error");
			result.put("errorMessage", "missing or incorrect type for command");
			output.writeUTF(result.toJSONString());
		}
		return result;
	}
	
	/*private static JSONObject publishJSON(JSONObject command) {
		JSONObject result = new JSONObject();
		if ()
		if (!command.containsKey("resource") ||
				!((JSONObject) command.get("resource")).containsKey("names") || 
				!((JSONObject) command.get("resource")).containsKey("tags")) {
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
			return result;
		}
		if (command.get("owner").equals("*")) {
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			return result;
		}
		//other rules are not defined, should be added later
		return command;	
	}*/
	
	private static JSONArray publishJSON(JSONObject command) {
		JSONObject result = new JSONObject();
		JSONArray array=new JSONArray();
		if(!command.containsKey("resource")||
				!((HashMap) command.get("resource")).containsKey("owner")||
				!((HashMap) command.get("resource")).containsKey("channel")||
				!((HashMap) command.get("resource")).containsKey("uri")){
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
			array.add(result);
			return array;
		} else if(((HashMap) command.get("resource")).get("owner").equals("*")){
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			array.add(result);
			return array;
		} else if(!((String)((HashMap) command.get("resource")).get("uri")).substring(0, 4).equals("http")) {
			//this if clause check if the file scheme is "file"
			result.put("response", "error");
			result.put("errorMessage", "cannot publish resource");
			array.add(result);
			return array;
		} else {
			for (int i = 0; i < Server.resourceList.size(); i++) {
				//this if clause check if there is resource with same channel and uri but different owner
				if (Server.resourceList.get(i).ifduplicated(command)) {
					result.put("response", "error");
					result.put("errorMessage", "cannot publish resource");
					array.add(result);
					return array;
				}
				//this checks if there is resource with same channel, uri and owner, replace the obj
				if (Server.resourceList.get(i).ifOverwrites(command)) {
					Server.resourceList.get(i).overwrites(command);
					result.put("response", "success");
					array.add(result);
					return array;
				}	
			}
			Server.resourceList.add(new KeyTuple(new Resource(command)));
			result.put("response", "success");
			return array;
		}
	}
	
	private static JSONArray shareJSON(JSONObject command) {
		JSONObject result = new JSONObject();
		JSONArray array=new JSONArray();
		if(!command.containsKey("resource")||
				!command.containsKey("secret") ||
				!((HashMap) command.get("resource")).containsKey("owner")||
				!((HashMap) command.get("resource")).containsKey("channel")||
				!((HashMap) command.get("resource")).containsKey("uri")){
			result.put("response", "error");
			result.put("errorMessage", "missing resource and\\/or secret");
			array.add(result);
			return array;
		} else if(((HashMap) command.get("resource")).get("owner").equals("*")){
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			array.add(result);
			return array;
		} else {
			//this checks whether the secret is correct
			boolean eligible = false;
			for (int i = 0; i < Server.secretList.size(); i++) {
				if (Server.secretList.get(i) == command.get("secret")) {
					eligible = true;
				}
			}
			if (!eligible) {
				result.put("response", "error");
				result.put("errorMessage", "incorrect secret");
				array.add(result);
				return array;
			}
		}
		//this if clause check if the file scheme is "file"
		if(!((String)((HashMap) command.get("resource")).get("uri")).substring(0, 4).equals("file") ||
				((String)((HashMap) command.get("resource")).get("uri")).charAt(0) == '/') {
			result.put("response", "error");
			result.put("errorMessage", "cannot publish resource");
			array.add(result);
			return array;
		} else {
			for (int i = 0; i < Server.resourceList.size(); i++) {
				//this if clause check if there is resource with same channel and uri but different owner
				if (Server.resourceList.get(i).ifduplicated(command)) {
					result.put("response", "error");
					result.put("errorMessage", "cannot share resource");
					array.add(result);
					return array;
				}
				//this checks if there is resource with same channel, uri and owner, replace the obj
				if (Server.resourceList.get(i).ifOverwrites(command)) {
					Server.resourceList.get(i).overwrites(command);
					result.put("response", "success");
					array.add(result);
					return array;
				}	
			}
			Server.resourceList.add(new KeyTuple(new Resource(command)));
			result.put("response", "success");
			return array;
		}
	}
	
	
	private static JSONObject removeJSON(JSONObject command){
		JSONObject result= new JSONObject();
		if(!command.containsKey("resource")||
				!((HashMap) command.get("resource")).containsKey("owner")||
				!((HashMap) command.get("resource")).containsKey("channel")||
				!((HashMap) command.get("resource")).containsKey("uri")){
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
			return result;
		}else if(((HashMap) command.get("resource")).get("owner").equals("*")){
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			return result;
		}else {
			boolean removed=false;
			for(int i=0;i<Server.resourceList.size();i++){
				if(Server.resourceList.get(i).ifOverwrites(command)){
					Server.resourceList.remove(i);
					result.put("response", "success");
					removed=true;
					break;
				}	
			}
			if(!removed){
				result.put("response", "error");
				result.put("errorMessage", "cannot remove resource");	
			}
			return result;
		}
	}
	
	private static JSONArray fetchJSON(JSONObject command, DataOutputStream output) {
		JSONArray result = new JSONArray();
		JSONObject obj = new JSONObject();
		if (!command.containsKey("resourceTemplate")) {
			obj.put("response", "error");
			obj.put("errorMessage", "missing resourceTemplate");
			result.add(obj);
			return result;
		}	
		String channel = (String) ((HashMap) command.get("resourceTemplate")).get("channel");
		String uri = (String) ((HashMap) command.get("resourceTemplate")).get("uri");
		for (int i = 0; i < Server.resourceList.size(); i++) {
			
			if (Server.resourceList.get(i).getChannel().equals(channel) &&
					Server.resourceList.get(i).getUri().equals(uri)) {
				//if the command matches a KeyTuple storeed in the server, the obj in that KeyTuple will be returned
				File f = new File(Server.resourceList.get(i).getUri());
				if (f.exists()) {
					JSONObject obj1 = new JSONObject();
					JSONObject obj2 = Server.resourceList.get(i).toJSON();
					JSONObject obj3 = new JSONObject();
					
					obj1.put("response", "success");
					obj2.put("resourceSize", f.length());
					obj3.put("resultSize", 1);
					result.add(obj1);
					result.add(obj2);
					result.add(obj3);
					try{
						RandomAccessFile byteFile = new RandomAccessFile(f, "r");
						byte[] sendingBuffer = new byte[1024*1024];
						int num;
						while((num = byteFile.read(sendingBuffer))>0) {
							output.write(Arrays.copyOf(sendingBuffer, num));
						}
						byteFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return result;
				}
				
			}
		}
		
		obj.put("response", "error");
		obj.put("errorMessage", "invalid resourceTemplate");
		result.add(obj);
		return result;
		
	}

}
