package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

class Client {
	private static String ip;
	private static int port;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		System.out.println("Client has started.");
		
		// Parse CMD options
		Options options = new Options();
		AddOptions(options);

		// accept args from commandline
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (cmd.hasOption("PORT") && cmd.hasOption("IP")) {
			port = Integer.parseInt(cmd.getOptionValue("PORT"));
			ip = cmd.getOptionValue("IP");
		} else {
			System.out.println("Please provide IP and PORT options");
			System.exit(0);
		}
		
		//  connect to a server socket
		try (Socket socket = new Socket(ip, port)) {

			// Get I/O streams for connection
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
					
			if (cmd.hasOption("publish")) {
				JSONPublish(cmd,output,input);
			} else if (cmd.hasOption("remove")) {
				JSONRemove(cmd,output,input);
			} else if (cmd.hasOption("share")) {
				JSONShare(cmd,output,input);
			} else if (cmd.hasOption("fetch")) {
				JSONFetch(cmd, output,input);
			} else if (cmd.hasOption("exchange")) {
				 JSONExchange(cmd,output,input);
			} else if (cmd.hasOption("query")) {
				JSONQuery(cmd,output,input);
			}
			
			try {
				String message = input.readUTF();
				System.out.println(message);
			} catch (IOException e) {
				System.out.println("Server seems to have closed connection.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void JSONFetch(CommandLine command, DataOutputStream output, DataInputStream input) {
		// TODO Auto-generated method stub
		
	}

	private static void JSONExchange(CommandLine command, DataOutputStream output, DataInputStream input) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unchecked")
	private static void JSONQuery(CommandLine command, DataOutputStream output, DataInputStream input) {
		String name="";
		if(command.hasOption("name")){
		  name = command.getOptionValue("name");
		}
		String des = "";
		if(command.hasOption("description")){
			des = command.getOptionValue("description");
		}
		String uri= "";
		if(command.hasOption("uri")){
			uri = command.getOptionValue("uri");
		}
		String channel = "";
		if(command.hasOption("channel")){
			channel = command.getOptionValue("channel");
		}
		String owner= "";
		if(command.hasOption("owner")){
			owner = command.getOptionValue("owner");
		}
		JSONObject resourceTemplate= new JSONObject();
		JSONObject commandObj= new JSONObject();	
		resourceTemplate.put("name", name);
		resourceTemplate.put("description", des);
		resourceTemplate.put("uri", uri);
		resourceTemplate.put("channel", channel);
		resourceTemplate.put("owner", owner);
		resourceTemplate.put("ezserver",null);
		commandObj.put("command","QUERY");
		commandObj.put("relay",true);		
		commandObj.put("resourceTemplate",resourceTemplate);
		try {
			output.writeUTF(commandObj.toJSONString());
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
		
	@SuppressWarnings("unchecked")
	private static void JSONPublish(CommandLine command, DataOutputStream output, DataInputStream input) {
		
		String name="";
		if(command.hasOption("name")){
		  name = command.getOptionValue("name");
		}
		String des = "";
		if(command.hasOption("description")){
			des = command.getOptionValue("description");
		}
		String uri= "";
		if(command.hasOption("uri")){
			uri = command.getOptionValue("uri");
		}
		String channel = "";
		if(command.hasOption("channel")){
			channel = command.getOptionValue("channel");
		}
		String owner= "";
		if(command.hasOption("owner")){
			owner = command.getOptionValue("owner");
		}
		JSONObject resource= new JSONObject();
		JSONObject commandObj= new JSONObject();	
		resource.put("name", name);
		resource.put("description", des);
		resource.put("uri", uri);
		resource.put("channel", channel);
		resource.put("owner", owner);
		resource.put("ezserver",null);
		commandObj.put("command","PUBLISH");
		commandObj.put("resource",resource);
		try {
			output.writeUTF(commandObj.toJSONString());
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private static void JSONRemove(CommandLine command,DataOutputStream output,DataInputStream input) {
		String name="";
		if(command.hasOption("name")){
		  name = command.getOptionValue("name");
		}
		String des = "";
		if(command.hasOption("description")){
			des = command.getOptionValue("description");
		}
		String uri= "";
		if(command.hasOption("uri")){
			uri = command.getOptionValue("uri");
		}
		String channel = "";
		if(command.hasOption("channel")){
			channel = command.getOptionValue("channel");
		}
		String owner= "";
		if(command.hasOption("owner")){
			owner = command.getOptionValue("owner");
		}
		JSONObject resource= new JSONObject();
		JSONObject commandObj= new JSONObject();	
		resource.put("name", name);
		resource.put("description", des);
		resource.put("uri", uri);
		resource.put("channel", channel);
		resource.put("owner", owner);
		resource.put("ezserver",null);
		commandObj.put("command","REMOVE");
		commandObj.put("resource",resource);
		try {
			output.writeUTF(commandObj.toJSONString());
			output.flush();
			while(true){
    			if(input.available() > 0){
    				
    	    		String result = input.readUTF();
    	    		System.out.println("Received from server: "+result);
    	    		
    	    		}}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static void JSONShare(CommandLine command, DataOutputStream output, DataInputStream input) {
		String name="";
		if(command.hasOption("name")){
		  name = command.getOptionValue("name");
		}
		String des = "";
		if(command.hasOption("description")){
			des = command.getOptionValue("description");
		}
		String uri= "";
		if(command.hasOption("uri")){
			uri = command.getOptionValue("uri");
		}
		String channel = "";
		if(command.hasOption("channel")){
			channel = command.getOptionValue("channel");
		}
		String owner= "";
		if(command.hasOption("owner")){
			owner = command.getOptionValue("owner");
		}
		JSONObject resource= new JSONObject();
		JSONObject commandObj= new JSONObject();	
		resource.put("name", name);
		resource.put("description", des);
		resource.put("uri", uri);
		resource.put("channel", channel);
		resource.put("owner", owner);
		resource.put("ezserver",null);
		String secrect="";
		if(command.hasOption("secrect")){
			secrect = command.getOptionValue("secrect");	
		}
		commandObj.put("command","SHARE");
		commandObj.put("resource",resource);
		commandObj.put("secrect",secrect);
		
		try {
			output.writeUTF(commandObj.toJSONString());
			output.flush();
			while(true){
    			if(input.available() > 0){
    				
    	    		String result = input.readUTF();
    	    		System.out.println("Received from server: "+result);
    	    		
    	    		}}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void AddOptions(Options options){
		options.addOption("PORT", true, "Server port");
		options.addOption("IP", true, "Server IP address");
		options.addOption("channel", true, "channel");
		options.addOption("debug", false, "print debug information");
		options.addOption("description", true, "resource description");
		options.addOption("exchange", false, "exchange server list with server");
		options.addOption("fetch", false, "fetch resources from server");
		options.addOption("name", true, "resource name");
		options.addOption("owner", true, "owner");
		options.addOption("publish", false, "publish resource on server");
		options.addOption("query", false, "query for resources from server");
		options.addOption("remove", false, "remove resource from server");
		options.addOption("secret", true, "secret");
		options.addOption("servers", true, "server list");
		options.addOption("share", false, "share resource on server");
		options.addOption("tags", true, "resource tags");
		options.addOption("uri", true, "resource URI");
		options.addOption("host", true, "server host");
		
	}
	
}
