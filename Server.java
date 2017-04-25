package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.net.ServerSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Server {

	public static ArrayList<KeyTuple> resourceList = new ArrayList<KeyTuple>();
	
	public static ArrayList<Integer> secretList = new ArrayList<Integer>();

	// Declare the port number
	private static int port = 3000;
	
	// Identifies the user number connected
	private static int counter = 0;
	
	public static void main(String[] args) {
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		try(ServerSocket server = factory.createServerSocket(port)){
			System.out.println("Waiting for client connection..");
			
			// Wait for connections.
			while(true){
				Socket client = server.accept();
				counter++;
				System.out.println("Client "+counter+": Applying for connection!");
				
				
				// Start a new thread for a connection
				Thread t = new Thread(() -> serveClient(client));
				t.start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	private static void serveClient(Socket client){
		try(Socket clientSocket = client){
			
			// The JSON Parser
			JSONParser parser = new JSONParser();
			// Input stream
			DataInputStream input = new DataInputStream(clientSocket.
					getInputStream());
			// Output Stream
		    DataOutputStream output = new DataOutputStream(clientSocket.
		    		getOutputStream());
		    System.out.println("CLIENT: "+input.readUTF());
		    output.writeUTF("Server: Hi Client "+counter+" !!!");
		    
		    // Receive more data..
		    while(true){
		    	if(input.available() > 0){
		    		// Attempt to convert read data to JSON
		    		JSONObject command = (JSONObject) parser.parse(input.readUTF());
		    		System.out.println("COMMAND RECEIVED: "+command.toJSONString());
		    		
		    		//we should start writing own method here
		    		parseCommand(command,output);
		    	}
		    }
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
	
	public static void parseCommand(JSONObject command, DataOutputStream output) throws IOException {
		JSONObject result = new JSONObject();
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
	}
	
	private static void publishJSON(JSONObject command, DataOutputStream output) throws IOException {
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
			//!((String)((HashMap) command.get("resource")).get("uri")).substring(0, 4).equals("http"
		} else if(command.get("uri").equals("") || (((String) command.get("uri")).length() > 4 && ((String) command.get("uri")).substring(0, 4).equals("file"))) {
			//this if clause check if the file scheme is "file"
			result.put("response", "error");
			result.put("errorMessage", "cannot publish resource");
			output.writeUTF(result.toJSONString());
			return;
		} else {
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
		} else if (((String) command.get("uri")).length() <= 4 || (((String) command.get("uri")).length() > 4 && !((String) command.get("uri")).substring(0, 4).equals("file"))) {
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
