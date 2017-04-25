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
				publishJSON(command, output);
				break;
			case "REMOVE":
				removeJSON(command, output);
				break;
			case "SHARE":
				shareJSON(command, output);
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
		public static void publishJSON(JSONObject command, DataOutputStream output) throws IOException {
		JSONObject result = new JSONObject();
		if(!command.containsKey("resource")||
				!((HashMap) command.get("resource")).containsKey("owner")||
				!((HashMap) command.get("resource")).containsKey("channel")||
				!((HashMap) command.get("resource")).containsKey("uri")){
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
			output.writeUTF(result.toJSONString());
			return;
		} else if(((HashMap) command.get("resource")).get("owner").equals("*")){
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			output.writeUTF(result.toJSONString());
			return;
		} else if (((String)((HashMap) command.get("resource")).get("uri")).substring(0, 4).equals("http")) {
			
		} else {
			result.put("response", "error");
			result.put("errorMessage", "cannot publish resource");
			output.writeUTF(result.toJSONString());
			return;
		}
		
		for (int i = 0; i < Server.resourceList.size(); i++) {
			//this if clause check if there is resource with same channel and uri but different owner
			if (Server.resourceList.get(i).ifduplicated(command)) {
				result.put("response", "error");
				result.put("errorMessage", "cannot publish resource");
				output.writeUTF(result.toJSONString());
				return;
			}
			//this checks if there is resource with same channel, uri and owner, replace the obj
			else if (Server.resourceList.get(i).ifOverwrites(command)) {
				Server.resourceList.get(i).overwrites(command);
				result.put("response", "success");
				output.writeUTF(result.toJSONString());
				return;
			}
		}
		Server.resourceList.add(new KeyTuple(new Resource(command)));
		result.put("response", "success");
		return;
	}
	
	private static void shareJSON(JSONObject command, DataOutputStream output) throws IOException {
		JSONObject result = new JSONObject();
		JSONArray array=new JSONArray();
		if(!command.containsKey("resource")||
				!command.containsKey("secret") ||
				!((HashMap) command.get("resource")).containsKey("owner")||
				!((HashMap) command.get("resource")).containsKey("channel")||
				!((HashMap) command.get("resource")).containsKey("uri")){
			result.put("response", "error");
			result.put("errorMessage", "missing resource and\\/or secret");
			output.writeUTF(result.toJSONString());
			return;
		} else if(((HashMap) command.get("resource")).get("owner").equals("*")){
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			output.writeUTF(result.toJSONString());
			return;
		} else {
			//this checks whether the secret is correct
			boolean eligible = false;
			for (int i = 0; i < Server.secretList.size(); i++) {
				if (Server.secretList.get(i) == command.get("secret")) {
					eligible = true;
					break;
				}
			}
			if (!eligible) {
				result.put("response", "error");
				result.put("errorMessage", "incorrect secret");
				output.writeUTF(result.toJSONString());
				return;
			}
		}
		//this if clause check if the file scheme is "file"
		if(!((String)((HashMap) command.get("resource")).get("uri")).substring(0, 4).equals("file") ||
				((String)((HashMap) command.get("resource")).get("uri")).charAt(0) == '/') {
			result.put("response", "error");
			result.put("errorMessage", "cannot publish resource");
			output.writeUTF(result.toJSONString());
			return;
		} else {
			for (int i = 0; i < Server.resourceList.size(); i++) {
				//this if clause check if there is resource with same channel and uri but different owner
				if (Server.resourceList.get(i).ifduplicated(command)) {
					result.put("response", "error");
					result.put("errorMessage", "cannot share resource");
					output.writeUTF(result.toJSONString());
					return;
				}
				//this checks if there is resource with same channel, uri and owner, replace the obj
				if (Server.resourceList.get(i).ifOverwrites(command)) {
					Server.resourceList.get(i).overwrites(command);
					result.put("response", "success");
					output.writeUTF(result.toJSONString());
					return;
				}	
			}
			Server.resourceList.add(new KeyTuple(new Resource(command)));
			result.put("response", "success");
			output.writeUTF(result.toJSONString());
			return;
		}
	}
	
	
	private static void removeJSON(JSONObject command, DataOutputStream output) throws IOException{
		JSONObject result= new JSONObject();
		if(!command.containsKey("resource")||
				!((HashMap) command.get("resource")).containsKey("owner")||
				!((HashMap) command.get("resource")).containsKey("channel")||
				!((HashMap) command.get("resource")).containsKey("uri")){
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
			output.writeUTF(result.toJSONString());
			return;
		}else if(((HashMap) command.get("resource")).get("owner").equals("*")){
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			output.writeUTF(result.toJSONString());
			return;
		}else {
			boolean removed=false;
			for(int i=0;i<Server.resourceList.size();i++){
				if(Server.resourceList.get(i).ifOverwrites(command)){
					Server.resourceList.remove(i);
					result.put("response", "success");
					output.writeUTF(result.toJSONString());
					return;
				}	
			}
			
			if(!removed){
				result.put("response", "error");
				result.put("errorMessage", "cannot remove resource");
				output.writeUTF(result.toJSONString());
				return;
			}
		}
	}
	
	private static void fetchJSON(JSONObject command, DataOutputStream output) throws IOException {
		JSONObject obj = new JSONObject();
		if (!command.containsKey("resourceTemplate")) {
			obj.put("response", "error");
			obj.put("errorMessage", "missing resourceTemplate");
			output.writeUTF(obj.toJSONString());
			return;

		}
		
		String channel = (String) ((HashMap) command.get("resourceTemplate")).get("channel");
		String uri = (String) ((HashMap) command.get("resourceTemplate")).get("uri");
		for (int i = 0; i < Server.resourceList.size(); i++) {
			
			if (Server.resourceList.get(i).getChannel().equals(channel) &&
					Server.resourceList.get(i).getUri().equals(uri)) {
				//if the command matches a KeyTuple storeed in the server, the obj in that KeyTuple will be returned
				File f = new File(Server.resourceList.get(i).getUri());
				if (f.exists()) {
					obj.put("response", "success");
					obj.put("resourceSize", f.length());
					obj.put("resultSize", 1);
					output.writeUTF(obj.toJSONString());
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
					return;
				}	
			}
		}
		
		obj.put("response", "error");
		obj.put("errorMessage", "invalid resourceTemplate");
		output.writeUTF(obj.toJSONString());
		return;
	}

}
