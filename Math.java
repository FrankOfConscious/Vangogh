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
	
	public static JSONArray parseCommand(JSONObject command, DataOutputStream outpur) {
		JSONArray result = new JSONArray();
		
		//this solves generic response
		if (command.containsKey("command")) {
			switch((String) command.get("command")) {
			//each case handles more explicit situation
			case "PUBLISH":
				result=publishJSON(command);
				break;
			case "REMOVE":result=removeJSON(command);
				break;
			case "SHARE":result=shareJSON(command);
				break;
			case "QUERY":result=queryJSON(command);
				break;
			case "FETCH":result=fetchJSON(command);
				break;
			case "EXCHANGE":result=exchangeJSON(command);
				break;
			default:
				//return invalid command
				JSONObject obj=new JSONObject();
				obj.put("response", "error");
				obj.put("errorMessage", "invalid command");
				result.add(obj);
				break;
			}
		} else {
			//return missing or incorrect type
			JSONObject obj=new JSONObject();
			obj.put("response", "error");
			obj.put("errorMessage", "missing or incorrect type for command");
			result.add(obj);
		}
		return result;
	}
	
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
		}
		return array;
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
		}
		return array;
	}
	private static JSONArray queryJSON(JSONObject command){	
		int resultSize=0;
		ArrayList<KeyTuple> tempList=new ArrayList<KeyTuple>();
		if(command.containsKey("resource")&&command.containsKey("secret")){
			if(((HashMap) command.get("resource")).get("owner").equals("*")){
				JSONObject obj=new JSONObject();
				obj.put("response", "error");
				obj.put("errorMessage", "invalid resourceTemplate");
				JSONArray result=new JSONArray();
				result.add(obj);
				return result;
			}else{
				for(int i=0;i<Server.resourceList.size();i++){
					if(queryMatch(Server.resourceList.get(i),command)){
						tempList.add(Server.resourceList.get(i));
					}
				}
			}
			
		}else{
			JSONObject obj=new JSONObject();
			obj.put("response", "error");
			obj.put("errorMessage", "missing resourceTemplate");
			JSONArray result=new JSONArray();
			result.add(obj);
			return result;
		}
		if(tempList.size()==0){
			JSONObject obj=new JSONObject();
			JSONObject obj2=new JSONObject();
			obj.put("response", "success");
			obj2.put("resultSize", 0);
			JSONArray result=new JSONArray();
			result.add(obj);
			result.add(obj2);
			return result;
		}else{
			for(int i=0;i<Server.resourceList.size();i++){
				if(queryMatch(Server.resourceList.get(i),command))
					tempList.add(Server.resourceList.get(i));
			}
			JSONArray result=new JSONArray();
			JSONObject obj=new JSONObject();
			obj.put("response", "success");
			result.add(obj);
			for(int i=0;i<tempList.size();i++){
				result.add(tempList.get(i).toJSON());
			}
			obj=new JSONObject();
			obj.put("resultsize", tempList.size());
			result.add(obj);
			return result;
			
		}
		
		
		
	}
	
	private static JSONArray removeJSON(JSONObject command){
		JSONObject result= new JSONObject();
		JSONArray array=new JSONArray();
		if(!command.containsKey("resource")||
				!((HashMap) command.get("resource")).containsKey("owner")||
				!((HashMap) command.get("resource")).containsKey("channel")||
				!((HashMap) command.get("resource")).containsKey("uri")){
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
			array.add(result);
			return array;
		}else if(((HashMap) command.get("resource")).get("owner").equals("*")){
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			array.add(result);
			return array;
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
			array.add(result);
			return array;
			
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
