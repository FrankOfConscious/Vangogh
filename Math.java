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
