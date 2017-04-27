package Server;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
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
	
	static JSONArray parseCommand(JSONObject command,DataOutputStream output) {
		JSONArray result = new JSONArray();
		
		//this solves generic response
		if (command.containsKey("command")) {
			switch((String) command.get("command")) {
			//each case handles more explicit situation
			case "PUBLISH":
				result=publishJSON(command);
				System.out.println("Current ResourceList size:"+result.size());///////
				break;
			case "REMOVE":result=removeJSON(command);
				System.out.println("Current ResourceList size:"+result.size());///////
				break;
			case "SHARE":result=shareJSON(command);
				break;
			case "QUERY":result=queryJSON(command);
				break;
			case "FETCH":result=fetchJSON(command,output);
				break;
			case "EXCHANGE":result=exchangeJSON(command);
				System.out.println("Current server List:"+Server.serverRecords.toString());
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
		} else if(((String)((HashMap) command.get("resource")).get("uri")).equals("")) {
			//this if clause check if the file scheme is "file"
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
			array.add(result);
			return array;
		} else {
//			System.out.println("file or http::"+(String)((HashMap) command.get("resource")).get("uri"));//////
			if(   ((String)((HashMap) command.get("resource")).get("uri")).length()<7
					||  (((String)((HashMap) command.get("resource")).get("uri")).length()>=7 
					&& !((String)((HashMap) command.get("resource")).get("uri")).substring(0, 7).equals("file://"))
					){
//			System.out.println("i am in if");
				
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
						result.put("response", "success(overwrites)");/////
						array.add(result);
						return array;
				}	
//					System.out.println("in for");
			}
			
			Server.resourceList.add(new KeyTuple(new Resource(command)));
			System.out.println("resourceList:"+Server.resourceList.size());
			for(int k=0;k<Server.resourceList.size();k++){
				System.out.println(Server.resourceList.get(k).getUri());
			}
			result.put("response", "success");
			array.add(result);
			return array;
			
			}else{
				result.put("response", "error");
				result.put("errorMessage", "cannot publish resource");
				array.add(result);
				return array;
				
			}
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
			
			if (Server.secret.equals(command.get("secret"))) 
					eligible = true;
				
			
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
			array.add(result);
			return array;
		}
	}
	
	private static JSONArray queryJSON(JSONObject command){	
		int resultSize=0;
		ArrayList<KeyTuple> tempList=new ArrayList<KeyTuple>();
		if(command.containsKey("resourceTemplate")){
			if(((HashMap) command.get("resourceTemplate")).get("owner").equals("*")){
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
//			for(int i=0;i<Server.resourceList.size();i++){
//				if(queryMatch(Server.resourceList.get(i),command))
//					tempList.add(Server.resourceList.get(i));
//			}
			JSONArray result=new JSONArray();
			JSONObject obj=new JSONObject();
			obj.put("response", "success");
			result.add(obj);
			for(int i=0;i<tempList.size();i++){           /////////////////////
				result.add(( new Resource(tempList.get(i).getObj())).toJSON());
				
			}
			obj=new JSONObject();
			obj.put("resultsize", tempList.size());
			result.add(obj);
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

	private static JSONArray removeJSON(JSONObject command){
		JSONObject result= new JSONObject();
		JSONArray array=new JSONArray();
		if(
				((HashMap) command.get("resource")).get("owner")==null||
				((HashMap) command.get("resource")).get("channel")==null||
				((HashMap) command.get("resource")).get("uri")==null){
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
					System.out.println("resourceList:"+Server.resourceList.size());
					for(int k=0;k<Server.resourceList.size();k++){
						System.out.println(Server.resourceList.get(k).getUri());
					}
					result.put("response", "success");
					removed=true;
					break;
				}
				
			}
			if(!removed){
				result.put("response", "error");
				result.put("errorMessage", "cannot remove resource(not exsit)");	
			}
			array.add(result);
			return array;
			
		}
			
		}
	private static JSONArray exchangeJSON(JSONObject command){
		if((!command.containsKey("serverList"))||(command.get("serverList")==null)){
			JSONObject obj=new JSONObject();
			obj.put("response", "error");
			obj.put("errorMessage", "missing or invalid server list");
			JSONArray result=new JSONArray();
			result.add(obj);
			return result;
		}else {
	
			String serverRecord=checker((String) command.get("serverList"));
			String [] RecordArray =serverRecord.split(",");
// 			for(int i=0;i<RecordArray.length;i++){
// 				if(!ishostPort(RecordArray[i])){
// 					JSONObject obj=new JSONObject();
// 					obj.put("response", "error");
// 					obj.put("errorMessage", "missing resourceTemplate");
// 					JSONArray result=new JSONArray();
// 					result.add(obj);
// 					return result;
// 				}
// 			}
			for(int i=0;i<RecordArray.length;i++){
				String [] hostPort=RecordArray[i].split(":");
				String hostName=hostPort[0];
				String port=hostPort[1];
				if(//!(isIpv4(hostName)||
				     !isPort(port)){
			
					JSONObject obj=new JSONObject();
					obj.put("response", "error");
					obj.put("errorMessage", "missing resourceTemplate");
					JSONArray result=new JSONArray();
					result.add(obj);
					return result;
				}
				boolean exist=false;
				for(int j=0;j<Server.serverRecords.size();j++){
					if(Server.serverRecords.get(j).equals(RecordArray[i])){
						exist=true;
						break;
					}
				}
				if(!exist)
				Server.serverRecords.add(RecordArray[i]);
		
				
			}
		}
		JSONObject obj=new JSONObject();
		obj.put("response", "success");
		JSONArray result=new JSONArray();
		result.add(obj);	
		return result;
		
	}
	private static String checker(String input){
		String b=input.replaceAll("\\s", "");
		 b=b.replace("\0", "");
		return b;	
	}
	 private static boolean isIpv4(String ipAddress) {
	        String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
	                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
	                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
	                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
	        Pattern pattern = Pattern.compile(ip);
	        Matcher matcher = pattern.matcher(ipAddress);
	        return matcher.matches();
	    }
	  static boolean ishostPort(String HP) {
	        String hostPort = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
	                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
	                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
	                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)"+":"+"\\d{1,5}$";
	        Pattern pattern = Pattern.compile(hostPort);
	        Matcher matcher = pattern.matcher(HP);
	        return matcher.matches();
	    }
	 private static boolean isPort(String port) {
		 int portNumber =Integer.parseInt(port);
	        if(portNumber<0||portNumber>65535){
	        	return false;
	        }
	        return true;
	    }
	
	
	private static boolean queryMatch(KeyTuple Tuple, JSONObject command) {
		// TODO Auto-generated method stub
		boolean[] rules=new boolean[11];
		rules[0]=Tuple.getChannel().equals(((HashMap) command.get("resourceTemplate")).get("channel"));
		if(!rules[0]) return false;
		rules[1]= (((HashMap) command.get("resourceTemplate")).get("owner")).equals("") ||Tuple.getOwner().equals("")||Tuple.getOwner().equals(((HashMap) command.get("resourceTemplate")).get("owner"));
		if(!rules[1]) return false;
		rules[2]=true;
		if( !((HashMap) command.get("resourceTemplate")).get("tags").equals("")){
			if(Tuple.getObj().getTags()==null) {
				rules[2]=false;
				return false;
			}
			else{
				String[] tempTags=( (String) ((HashMap) command.get("resourceTemplate")).get("tags")).split(",");
		
				for(int j=0;j<tempTags.length;j++){
					if(!Arrays.asList(Tuple.getObj().getTags()).contains(tempTags[j])){
						rules[2]=false;
						return false;
					}
				}
			}
		}
		if(!((HashMap) command.get("resourceTemplate")).containsKey("uri")) rules[3]=true;
		else if(((HashMap) command.get("resourceTemplate")).get("uri").equals("")) rules[3]=true;
		else {
			if(((HashMap) command.get("resourceTemplate")).get("uri").equals(Tuple.getUri())) rules[3]=true;
			else return false;
		}
		rules[8]=false;
		if(!((HashMap) command.get("resourceTemplate")).containsKey("name")) rules[7]=true;
		else if(((HashMap) command.get("resourceTemplate")).get("name").equals("")) rules[8]=true;
		else {
			if(   Tuple.getObj().get("name").contains(  (String) ((HashMap) command.get("resourceTemplate")).get("name")     )  ) {rules[4]=true; return true;}
			else rules[4]= false;
		}
		rules[10]=false;
		if(!((HashMap) command.get("resourceTemplate")).containsKey("description")) rules[9]=true;
		else if(((HashMap) command.get("resourceTemplate")).get("description").equals("")) rules[10]=true;
		else {
			if(   Tuple.getObj().get("description").contains(  (String) ((HashMap) command.get("resourceTemplate")).get("description")     )  ){rules[5]=true; return true;}
			else rules[5]= false;
		}
		
		if((rules[7]&&rules[9])||(rules[8]&&rules[10])) {rules[6]=true;return true;}
		else rules[6]=false;
		
		if(rules[4]==true ||rules[5]==true||rules[6]==true) return true;
		else return false;
	}
	}
