package Server;

import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

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
	
	public static JSONObject parseCommand(JSONObject command, DataOutputStream outpur) {
		JSONObject result = new JSONObject();
		
		//this solves generic response
		if (command.containsKey("command")) {
			switch((String) command.get("command")) {
			//each case handles more explicit situation
			case "PUBLISH":
				publishJSON(command);
				break;
			case "REMOVE":removeJSON(command);
				break;
			case "SHARE":
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
				break;
			}
		} else {
			//return missing or incorrect type
			result.put("response", "error");
			result.put("errorMessage", "missing or incorrect type for command");
		}
		return result;
	}
	
	private static JSONObject publishJSON(JSONObject command) {
		JSONObject result = new JSONObject();
		if(!command.containsKey("resource")||
				!((HashMap) command.get("resource")).containsKey("owner")||
				!((HashMap) command.get("resource")).containsKey("channel")||
				!((HashMap) command.get("resource")).containsKey("uri")){
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
			return result;
		} else if(((HashMap) command.get("resource")).get("owner").equals("*")){
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			return result;
		} else if(!((String)((HashMap) command.get("resource")).get("uri")).substring(0, 4).equals("http")) {
			//this if clause check if the file scheme is "file"
			result.put("response", "error");
			result.put("errorMessage", "cannot publish resource");
			return result;
		} else {
			for (int i = 0; i < Server.resourceList.size(); i++) {
				//this if clause check if there is resource with same channel and uri but different owner
				if (Server.resourceList.get(i).ifduplicated(command)) {
					result.put("response", "error");
					result.put("errorMessage", "cannot publish resource");
					return result;
				}
				//this checks if there is resource with same channel, uri and owner, replace the obj
				if (Server.resourceList.get(i).ifOverwrites(command)) {
					Server.resourceList.get(i).overwrites(command);
					result.put("response", "success");
					return result;
				}	
			}
			Server.resourceList.add(new KeyTuple(new Resource(command)));
			result.put("response", "success");
		}
		return result;
	}
	
	private static JSONObject shareJSON(JSONObject command) {
		JSONObject result = new JSONObject();
		if(!command.containsKey("resource")||
				!command.containsKey("secret") ||
				!((HashMap) command.get("resource")).containsKey("owner")||
				!((HashMap) command.get("resource")).containsKey("channel")||
				!((HashMap) command.get("resource")).containsKey("uri")){
			result.put("response", "error");
			result.put("errorMessage", "missing resource and\\/or secret");
			return result;
		} else if(((HashMap) command.get("resource")).get("owner").equals("*")){
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			return result;
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
				return result;
			}
		}
		//this if clause check if the file scheme is "file"
		if(!((String)((HashMap) command.get("resource")).get("uri")).substring(0, 4).equals("file") ||
				((String)((HashMap) command.get("resource")).get("uri")).charAt(0) == '/') {
			result.put("response", "error");
			result.put("errorMessage", "cannot publish resource");
			return result;
		} else {
			for (int i = 0; i < Server.resourceList.size(); i++) {
				//this if clause check if there is resource with same channel and uri but different owner
				if (Server.resourceList.get(i).ifduplicated(command)) {
					result.put("response", "error");
					result.put("errorMessage", "cannot share resource");
					return result;
				}
				//this checks if there is resource with same channel, uri and owner, replace the obj
				if (Server.resourceList.get(i).ifOverwrites(command)) {
					Server.resourceList.get(i).overwrites(command);
					result.put("response", "success");
					return result;
				}	
			}
			Server.resourceList.add(new KeyTuple(new Resource(command)));
			result.put("response", "success");
		}
		return result;
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
		private static boolean queryMatch(KeyTuple Tuple, JSONObject command) {
		// TODO Auto-generated method stub
		boolean[] rules=new boolean[7];
		rules[0]=Tuple.getChannel().equals(((HashMap) command.get("resource")).get("channel"));
		if(!rules[0]) return false;
		rules[1]=Tuple.getOwner().equals("")||Tuple.getOwner().equals(((HashMap) command.get("resource")).get("owner"));
		if(!rules[1]) return false;
		rules[2]=true;
		for(int j=0;j<((String[]) ((HashMap) command.get("resource")).get("tags")).length;j++){
			if(!Arrays.asList(Tuple.getObj().getTags()).contains(((String[])((HashMap) command.get("resource")).get("tags"))[j])){
				rules[2]=false;
				return false;
			}
		}
		if(!((HashMap) command.get("resource")).containsKey("uri")) rules[3]=true;
		else if(((HashMap) command.get("resource")).get("uri").equals("")) rules[3]=true;
		else {
			if(((HashMap) command.get("resource")).get("uri").equals(Tuple.getUri())) rules[3]=true;
			else return false;
		}
		if(!((HashMap) command.get("resource")).containsKey("name")) rules[4]=true;
		else if(((HashMap) command.get("resource")).get("name").equals("")) rules[4]=true;
		else {
			if(   Tuple.getObj().get("name").contains(  (String) ((HashMap) command.get("resource")).get("name")     )  ) rules[4]=true;
			else rules[4]= false;
		}
		if(!((HashMap) command.get("resource")).containsKey("description")) rules[5]=true;
		else if(((HashMap) command.get("resource")).get("description").equals("")) rules[5]=true;
		else {
			if(   Tuple.getObj().get("description").contains(  (String) ((HashMap) command.get("resource")).get("description")     )  ) rules[4]=true;
			else rules[5]= false;
		}
		
		if(((HashMap) command.get("resource")).containsKey("name")&&
				((HashMap) command.get("resource")).containsKey("description")) rules[6]=true;
		else rules[6]=false;
		
		if(rules[4]==true ||rules[5]==true||rules[6]==true) return true;
		else return false;
	}
	}
