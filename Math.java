package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



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
	private static final Logger log = Logger.getLogger(Logger.class);
	static JSONArray parseCommand(JSONObject raw,DataOutputStream output)  {
		JSONArray result = new JSONArray();
		JSONObject command=new JSONObject();
		
		//this solves generic response
		if (raw.containsKey("command")) {
			switch((String) raw.get("command")) {
			//each case handles more explicit situation
			case "PUBLISH":
				command=preProcess(raw);
				result=publishJSON(command);
				System.out.println("Current ResourceList size:"+result.size());///////
				break;
			case "REMOVE":
				command=preProcess(raw);
				result=removeJSON(command);
				System.out.println("Current ResourceList size:"+result.size());///////
				break;
			case "SHARE":
				command=preProcess(raw);
				result=shareJSON(command);
				break;
			case "QUERY":
				command=preProcess(raw);
				result=queryJSON(command);
				break;
			case "FETCH":
				command=preProcess(raw);
				result=fetchJSON(command,output);
				break;
			case "EXCHANGE":
				command=preProcess(raw);
				result=exchangeJSON(command);
				System.out.println("Current server List:"+Server.serverRecords.toString());////////////
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
		JSONObject endOfTag=new JSONObject();
		endOfTag.put("endOfTransmit",true);
		result.add(endOfTag);
		return result;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	private static JSONObject preProcess(JSONObject command) {
		// TODO Auto-generated method stub
		if(command.containsKey("command")){
			switch((String)command.get("command")){
			case("PUBLISH"):{//////////////////////////////////////////////////////////////////////////
				String name = "";
				if (command.containsKey("name")){
					name = (String) command.get("name");
				}
				String tags="";
				if(command.containsKey("tags")){
					tags=(String) command.get("tags");
				}
				String des = "";
				if (command.containsKey("description")) {
					des = (String) command.get("description");
				}
				String uri = "";
				if (command.containsKey("uri")) {
					uri = (String) command.get("uri");
				}
				String channel = "";
				if (command.containsKey("channel")) {
					channel = (String) command.get("channel");
				}
				String owner = "";
				if (command.containsKey("owner")) {
					owner = (String) command.get("owner");
					if(owner.equals(".classpath")) owner="*";
				}
				JSONObject resource = new JSONObject();
				JSONObject commandObj = new JSONObject();
				resource.put("name", name);
				resource.put("tags", tags);
				resource.put("description", des);
				resource.put("uri", uri);
				resource.put("channel", channel);
				resource.put("owner", owner);
				resource.put("ezserver", null);
				commandObj.put("command", "PUBLISH");
				commandObj.put("resource", resource);
				return commandObj;
				
			}
			case("REMOVE"):{
				
				String uri = null;
				if (command.containsKey("uri")) {
					uri = (String) command.get("uri");
				}
				String channel = null;
				if (command.containsKey("channel")) {
					channel = (String) command.get("channel");
				}
				String owner = null;
				if (command.containsKey("owner")) {
					owner = (String) command.get("owner");
					if(owner.equals(".classpath")) owner="*";
				}
				JSONObject resource = new JSONObject();
				JSONObject commandObj = new JSONObject();
				resource.put("uri", uri);
				resource.put("channel", channel);
				resource.put("owner", owner);
				resource.put("ezserver", null);
				commandObj.put("command", "REMOVE");
				commandObj.put("resource", resource);
				return commandObj;
				
			}
			case("SHARE"):{
				String name = "";
				if (command.containsKey("name")){
					name = (String) command.get("name");
				}
				String tags="";
				if(command.containsKey("tags")){
					tags=(String) command.get("tags");
				}
				String des = "";
				if (command.containsKey("description")) {
					des = (String) command.get("description");
				}
				String uri = null;
				if (command.containsKey("uri")) {
					uri = (String) command.get("uri");
				}
				String channel = "";
				if (command.containsKey("channel")) {
					channel = (String) command.get("channel");
				}
				String owner = "";
				if (command.containsKey("owner")) {
					owner = (String) command.get("owner");
					if(owner.equals(".classpath")) owner="*";
				}
				String secret = null;
				if (command.containsKey("secret")) {
					owner = (String) command.get("secret");
				}
				JSONObject resource = new JSONObject();
				JSONObject commandObj = new JSONObject();
				resource.put("name", name);
				resource.put("secret", secret);
				resource.put("tags", tags);
				resource.put("description", des);
				resource.put("uri", uri);
				resource.put("channel", channel);
				resource.put("owner", owner);
				resource.put("ezserver", null);
				commandObj.put("command", "SHARE");
				commandObj.put("resource", resource);
				return commandObj;
				
				
			}
			case("QUERY"):{
				String name = "";
				if (command.containsKey("name")){
					name = (String) command.get("name");
				}
				String tags="";
				if(command.containsKey("tags")){
					tags=(String) command.get("tags");
				}
				String des = "";
				if (command.containsKey("description")) {
					des = (String) command.get("description");
				}
				String uri = "";
				if (command.containsKey("uri")) {
					uri = (String) command.get("uri");
				}
				String channel = "";
				if (command.containsKey("channel")) {
					channel = (String) command.get("channel");
				}
				String owner = "";
				if (command.containsKey("owner")) {
					owner = (String) command.get("owner");
					if(owner.equals(".classpath")) owner="*";
				}
				boolean relay = false;
				if (command.containsKey("relay")) {
					relay =command.get("relay").equals("true")?true:false;
				}
				JSONObject resourceTemplate = new JSONObject();
				JSONObject commandObj = new JSONObject();
				resourceTemplate.put("tags", tags);
				resourceTemplate.put("name", name);
				resourceTemplate.put("description", des);
				resourceTemplate.put("uri", uri);
				resourceTemplate.put("channel", channel);
				resourceTemplate.put("owner", owner);
				resourceTemplate.put("ezserver", null);
				commandObj.put("command", "QUERY");
				commandObj.put("relay", relay);
				commandObj.put("resourceTemplate", resourceTemplate);
				return commandObj;
				
			}
			//////////////////////////////////////////////////////////////////////jkljkljkljkl
			case("FETCH"):{

				String uri = null;
				if (command.containsKey("uri")) {
					uri = (String) command.get("uri");
				}
				String channel = "";
				if (command.containsKey("channel")) {
					channel = (String) command.get("channel");
				}
				JSONObject resourceTemplate = new JSONObject();
				JSONObject commandObj = new JSONObject();
				resourceTemplate.put("uri", uri);
				resourceTemplate.put("channel", channel);
				resourceTemplate.put("ezserver", null);
				commandObj.put("command", "FETCH");
				commandObj.put("resourceTemplate", resourceTemplate);
				return commandObj;
				//////////////////////////////////////////////////////////////////////////////////////////
			}
			case("EXCHANGE"):{
				String serverList ;
				if (command.containsKey("servers")) {
					serverList= (String) command.get("servers");
				}
				else serverList=null;
				JSONObject commandObj = new JSONObject();


				commandObj.put("command", "EXCHANGE");
				commandObj.put("serverList", serverList);
				
			}
			
				
			}
		}
		
		return null;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	private static JSONArray publishJSON(JSONObject command) {
	
		JSONObject result = new JSONObject();
		JSONArray array=new JSONArray();
		if(!command.containsKey("resource")||
				!((HashMap) command.get("resource")).containsKey("uri")){
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
			array.add(result);
			debug(array);
			return array;
		} else if(((HashMap) command.get("resource")).get("owner").equals("*")){
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			array.add(result);
			debug(array);
			
			return array;
		} else if(((String)((HashMap) command.get("resource")).get("uri")).equals("")) {
			//this if clause check if the file scheme is "file"
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
			array.add(result);
			debug(array);
			return array;
		} else {
//			System.out.println("file or http::"+(String)((HashMap) command.get("resource")).get("uri"));//////
			if(   isURI((String)((HashMap) command.get("resource")).get("uri"))){
//			System.out.println("i am in if");
				for (int i = 0; i < Server.resourceList.size(); i++) {
				//this if clause check if there is resource with same channel and uri but different owner
					if (Server.resourceList.get(i).ifduplicated(command)) {
						result.put("response", "error");
						result.put("errorMessage", "cannot publish resource1");
						array.add(result);
						debug(array);
						return array;
					}
				//this checks if there is resource with same channel, uri and owner, replace the obj
					if (Server.resourceList.get(i).ifOverwrites(command)) {
						Server.resourceList.get(i).overwrites(command);
						result.put("response", "success(overwrites)");/////
						array.add(result);
						debug(array);
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
			debug(array);
			return array;
			
			}else{
				result.put("response", "error");
				result.put("errorMessage", "cannot publish resource2");
				array.add(result);
				return array;
				
			}
		}
	}
	
private static JSONArray shareJSON(JSONObject command) {
		JSONObject result = new JSONObject();
		JSONArray array=new JSONArray();
		if(!command.containsKey("resource")||
				!command.containsKey("secret") ||((HashMap) command.get("resource")).get("secret")==null||
				!((HashMap) command.get("resource")).containsKey("uri")||
				((HashMap) command.get("resource")).get("uri")==null
				){
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
		String temp=((String)((HashMap) command.get("resource")).get("uri"));
		if( (temp==null || temp.equals("") ||temp.length()<=7 || !temp.substring(0,7).equals("file://") )){
			result.put("response", "error");
			result.put("errorMessage", "cannot share resource");
			array.add(result);
			return array;
		} else if (!(new File(temp.substring(7))).exists()){
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
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
		////////////////////////////////////////////////////////////////////////////
		JSONArray relayList=new JSONArray();
		boolean relaysuccess=false;
		if(command.containsKey("relay")&&((boolean)command.get("relay"))==true){
			JSONObject relaycommand=new JSONObject(command);
			relaycommand.replace("name", "");
			relaycommand.replace("description", "");
			relaycommand.replace("relay", false);
			JSONParser parser = new JSONParser();
			for(int i=0;i<Server.serverRecords.size();i++){
				String relayhost=Server.serverRecords.get(i).split(":")[0];
				int relayport=Integer.parseInt(Server.serverRecords.get(i).split(":")[1]);
				try (Socket socket2 = new Socket(relayhost, relayport)) {

					// Get I/O streams for connection
					DataInputStream input2 = new DataInputStream(socket2.getInputStream());
					DataOutputStream output2 = new DataOutputStream(socket2.getOutputStream());
//					output2.writeUTF("I am client");
//					output2.flush();
					try {
						output2.writeUTF(relaycommand.toJSONString());
						output2.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}


					try {
						while(true){
							if(input2.available()>0){
								String message = input2.readUTF();
								if(message.equals("{\"endOfTransmit\":true}")) break;
								System.out.println(message);/////////////////
								JSONObject relaytemp=(JSONObject)parser.parse(message);
								if(relaytemp.containsKey("response")&&relaytemp.get("response").equals("success"))
									relaysuccess=true;
								if(relaytemp.containsKey("uri")){
									relayList.add(relaytemp);
								}
//								System.out.println(message);
//								int a=Server.resourceList.size();///
//								for(int i =0;i<a;i++){/////
//									System.out.println(Server.resourceList.get(i).toString());////
//								}////
							}
						}
							input2.close();
							output2.close();
							//String message = input.readUTF();
							//JSONArray results=message./////////////////////
							//System.out.println(message);
						} catch (IOException e) {
							System.out.println("Server seems to have closed connection.");
						}
					}

				 catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		if(command.containsKey("resourceTemplate")){
			if(((HashMap) command.get("resourceTemplate")).get("owner").equals("*")){
				JSONObject obj=new JSONObject();
				obj.put("response", "error");
				obj.put("errorMessage", "invalid resourceTemplate");
				JSONArray result=new JSONArray();
				result.add(obj);
				if (!relaysuccess) {
					debug(result);
					return result;
				}
				else{
					JSONArray relayresult=new JSONArray();
					JSONObject obj1=new JSONObject();
					JSONObject obj2=new JSONObject();
					obj1.put("response", "success");
					obj2.put("resultSize", relayList.size());
					relayresult.add(obj1);
					for(int j=0;j<relayList.size();j++){
						relayresult.add(relayList.get(j));
					}
					relayresult.add(obj2);
					debug(relayresult);
					return relayresult;
					
				}
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
			if (!relaysuccess) {
				debug(result);
				return result;
			}
			else{
				JSONArray relayresult=new JSONArray();
				JSONObject obj1=new JSONObject();
				JSONObject obj2=new JSONObject();
				obj1.put("response", "success");
				obj2.put("resultSize", relayList.size());
				relayresult.add(obj1);
				for(int j=0;j<relayList.size();j++){
					relayresult.add(relayList.get(j));
				}
				relayresult.add(obj2);
				debug(relayresult);
				return relayresult;
				
			}
		}
		if(tempList.size()==0){
			JSONObject obj=new JSONObject();
			JSONObject obj2=new JSONObject();
			obj.put("response", "success");
			obj2.put("resultSize", 0);
			JSONArray result=new JSONArray();
			result.add(obj);
			result.add(obj2);
			if (!relaysuccess) {
				debug(result);
				return result;
			}
			else{
				JSONArray relayresult=new JSONArray();
				JSONObject obj1=new JSONObject();				
				obj1.put("resultSize", relayList.size());
				relayresult.add(obj);
				for(int j=0;j<relayList.size();j++){
					relayresult.add(relayList.get(j));
				}
				relayresult.add(obj1);
				debug(relayresult);
				return relayresult;
				
			}
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
			if(relaysuccess){
				for(int j=0;j<relayList.size();j++){
					result.add(relayList.get(j));
				}
			}
				
				
			obj=new JSONObject();
			obj.put("resultsize", tempList.size()+relayList.size());
			result.add(obj);
			debug(result);
			return result;
			
		}
		
		
		
	}
	
	private static JSONArray fetchJSON(JSONObject command, DataOutputStream output){
		JSONArray result = new JSONArray();
		JSONObject obj = new JSONObject();
		if (!command.containsKey("resourceTemplate")||
				((HashMap) command.get("resourceTemplate")).containsKey("uri")||
				(String) ((HashMap) command.get("resourceTemplate")).get("uri")==null
				) {
			obj.put("response", "error");
			obj.put("errorMessage", "missing resourceTemplate");
			result.add(obj);
			debug(result);
			return result;
		}	
		String channel = (String) ((HashMap) command.get("resourceTemplate")).get("channel");
		String uri = (String) ((HashMap) command.get("resourceTemplate")).get("uri");
		for (int i = 0; i < Server.resourceList.size(); i++) {
			
			if (Server.resourceList.get(i).getChannel().equals(channel) &&
					Server.resourceList.get(i).getUri().equals(uri)) {
				//if the command matches a KeyTuple storeed in the server, the obj in that KeyTuple will be returned
				URI uriIns = null;
				try {
					uriIns = new URI(Server.resourceList.get(i).getUri());
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println(uriIns.getPath());
				File f = new File(uriIns.getPath());
				if (f.exists()) {
					JSONObject obj0 = new JSONObject();
					JSONObject obj1 = new JSONObject();
					JSONObject obj2 = Server.resourceList.get(i).toJSON();
					JSONObject obj3 = new JSONObject();
					
					obj0.put("response", "success");
				
					try {
						output.writeUTF(obj0.toJSONString());
				
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					obj2.put("resourceSize", f.length());
					try {
						output.writeUTF(obj2.toJSONString());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
//					obj3.put("resultSize", 1);
//
//					result.add(obj3);
					try{
						JSONObject fileSize = new JSONObject();
						fileSize.put("resourceSize", f.length());
						fileSize.put("uri", uriIns.toString());
						output.writeUTF(fileSize.toJSONString());
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
					obj3.put("resultSize", 1);

					result.add(obj3);
					debug(result);
					return result;
				}
				
			}
		}
		
		obj.put("response", "error");
		obj.put("errorMessage", "invalid resourceTemplate");
		result.add(obj);
		debug(result);
		return result;
		}

	private static JSONArray removeJSON(JSONObject command){
		JSONObject result= new JSONObject();
		JSONArray array=new JSONArray();
		if(
				(((HashMap) command.get("resource")).get("uri")==null||((HashMap) command.get("resource")).get("uri").equals(""))){
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
			array.add(result);
			debug(array);
			return array;
		}else if(((HashMap) command.get("resource")).get("owner").equals("*")){
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			array.add(result);
			debug(array);
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
			debug(array);
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
			debug(result);
			return result;
		}else {
	
			String serverRecord=checker((String) command.get("serverList"));
			String [] RecordArray =serverRecord.split(",");
//			for(int i=0;i<RecordArray.length;i++){
//				if(!ishostPort(RecordArray[i])){
//					JSONObject obj=new JSONObject();
//					obj.put("response", "error");
//					obj.put("errorMessage", "missing resourceTemplate");
//					JSONArray result=new JSONArray();
//					result.add(obj);
//					return result;
//				}
//			}
			for(int i=0;i<RecordArray.length;i++){
				String [] hostPort=RecordArray[i].split(":");
				String hostName=hostPort[0];
				String port=hostPort[1];
				if(!isPort(port)){
			
					JSONObject obj=new JSONObject();
					obj.put("response", "error");
					obj.put("errorMessage", "missing resourceTemplate");
					JSONArray result=new JSONArray();
					result.add(obj);
					debug(result);
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
		debug(result);
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
	static boolean isPort(String port) {
		 int portNumber =Integer.parseInt(port);
	        if(portNumber<0||portNumber>65535){
	        	return false;
	        }
	        return true;
	    }
	private static boolean isURI(String str){
		if(str==null ||str.equals("")) return false;
		else{
			int length=str.length();
			if(length<4) return false;
			if(length<5){if(str.substring(0,4).equals("ftp:")||str.substring(0,4).equals("jar:")) return true; else return false;}
			if(length<6){if(str.substring(0,4).equals("ftp:") || str.substring(0,5).equals("http:")||str.substring(0,4).equals("jar:")) return true; else return false;}
			if(length>=6){if(str.substring(0,4).equals("ftp:") || str.substring(0,5).equals("http:")||str.substring(0,6).equals("https:")||str.substring(0,4).equals("jar:")) return true; else return false;}
		}
		return false;
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
	private static void debug(JSONArray array) {
		if(Server.debug){
			if(array.contains("error")){
				log.error("SENT:"+array.toJSONString());
			}else{
				log.debug("SENT:"+array.toJSONString());
			}
		
		}
	}
//	public static void SetLogger(){
//
//
//
//		log.setLevel(Level.FINE);
//
//
//		log.getHandlers()[0].setLevel(Level.FINE);
//
//
//	}
	
	}