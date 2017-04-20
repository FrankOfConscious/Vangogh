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
	}
