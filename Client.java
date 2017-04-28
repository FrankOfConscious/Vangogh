package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.*;

import org.apache.log4j.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Server.Server;

@SuppressWarnings("unused")
class Client {
	private static String ip;
	private static int port;
	private static boolean debug = false;
	
	private static final Logger log = Logger.getLogger(Logger.class);

	public static void main(String[] args) {
		log.info("Client has started.");

		// Parse CMD options
		Options options = new Options();
		AddOptions(options);

		// accept args from CMD
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println("It's not an internal or external command.");
			//e.printStackTrace();
			System.exit(0);
		}
		if (cmd.hasOption("port") && cmd.hasOption("ip")) {
			port = Integer.parseInt(cmd.getOptionValue("port"));
			ip = cmd.getOptionValue("ip");
		} else {
			log.warn("Please provide IP and PORT options");
			System.exit(0);
		}
		if(cmd.hasOption("debug")) {
			debug = true;
		}
		
		

		// connect to a server socket
		try (Socket socket = new Socket(ip, port)) {

			// Get I/O streams for connection
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
//			output.writeUTF("I am client");
//			output.flush();
			JSONObject raw=new JSONObject();

			if (cmd.hasOption("fetch")) {
//				JSONFetch(cmd, output, input);
				raw.put("command", "FETCH");
			} else {
				if (cmd.hasOption("publish")) {
//					JSONPublish(cmd, output);
					raw.put("command", "PUBLISH");
				} else if (cmd.hasOption("remove")) {
//					JSONRemove(cmd, output);
					raw.put("command", "REMOVE");
				} else if (cmd.hasOption("share")) {
//					JSONShare(cmd, output);
					raw.put("command", "SHARE");
				} else if (cmd.hasOption("exchange")) {
//					JSONExchange(cmd, output);
					raw.put("command", "EXCHANGE");
				} else if (cmd.hasOption("query")) {
//					JSONQuery(cmd, output);
					raw.put("command", "QUERY");
				}
			if(cmd.hasOption("channel")) raw.put("channel",cmd.getOptionValue("channel") );
			if(cmd.hasOption("description")) raw.put("description",cmd.getOptionValue("description") );
			if(cmd.hasOption("name")) raw.put("name",cmd.getOptionValue("name") );
			if(cmd.hasOption("owner")) raw.put("owner",cmd.getOptionValue("owner") );
			if(cmd.hasOption("secret")) raw.put("secret",cmd.getOptionValue("secret") );
			if(cmd.hasOption("servers")) raw.put("servers",cmd.getOptionValue("servers") );
			if(cmd.hasOption("tags")) raw.put("tags",cmd.getOptionValue("tags") );
			if(cmd.hasOption("uri")) raw.put("uri",cmd.getOptionValue("uri") );
			if(cmd.hasOption("relay")) raw.put("relay",cmd.getOptionValue("relay") );

			try{
				output.writeUTF(raw.toJSONString());
				output.flush();
			}catch(IOException e){
				e.printStackTrace();
				System.exit(0);
				
			}
			try {
				while(true){
					if(input.available()>0){
						String message = input.readUTF();
						if(message.equals("{\"endOfTransmit\":true}")) break;
						log.info("RECEIVED: " + message);

					}
				}
				input.close();
				output.close();
			} catch (IOException e) {
					log.warn("Server seems to have closed connection.");
					System.exit(0);
				}
			}

		} catch (Exception e) {
			log.warn("Server seems to have closed connection.");
			System.exit(0);
			//e.printStackTrace();
			
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings({ "unchecked" })
	private static void JSONFetch(CommandLine command, DataOutputStream output, DataInputStream input) {
		String name = "";
		if (command.hasOption("name")) {
			name = command.getOptionValue("name");
		}
		String tags="";
		if(command.hasOption("tags")){
			tags=command.getOptionValue("tags");
		}
		String des = "";
		if (command.hasOption("description")) {
			des = command.getOptionValue("description");
		}
		String uri = "";
		if (command.hasOption("uri")) {
			uri = command.getOptionValue("uri");
		}
		String channel = "";
		if (command.hasOption("channel")) {
			channel = command.getOptionValue("channel");
		}
		String owner = "";
		if (command.hasOption("owner")) {
			owner = command.getOptionValue("owner");
		}
		JSONObject resourceTemplate = new JSONObject();
		JSONObject commandObj = new JSONObject();
		resourceTemplate.put("name", name);
		resourceTemplate.put("description", des);
		resourceTemplate.put("uri", uri);
		resourceTemplate.put("channel", channel);
		resourceTemplate.put("owner", owner);
		resourceTemplate.put("ezserver", null);
		commandObj.put("command", "FETCH");
		commandObj.put("resourceTemplate", resourceTemplate);

		try {
			output.writeUTF(commandObj.toJSONString());
			output.flush();
			log.info("fetching from " + ip + ":" + port);
			if(debug) log.debug("SENT: " + commandObj.toJSONString());
			
			JSONParser parser = new JSONParser();
			while (true) {
				if (input.available() > 0) {
					String result = input.readUTF();// get input stream from server
					//System.out.println(result);
					
					if(debug) log.info("RECEIVED: " + result);
					
					if(result.contains("response")) continue;
					
					if(result.contains("name")) continue;
					
					if(result.contains("resultSize")) break;
					
					JSONObject cmd = new JSONObject();
					try {
						cmd = (JSONObject) parser.parse(result);
						// Create a RandomAccessFile to read and write the
						// output file.
						String uriStr = (String)cmd.get("uri");
						String fileName = uriStr.substring( uriStr.lastIndexOf('/')+1, uriStr.length() );
						RandomAccessFile downloadingFile = new RandomAccessFile(fileName, "rw");

						// Find out how much size is remaining to get from the
						// server.
						
						long fileSizeRemaining = (Long) cmd.get("resourceSize");

						int chunkSize = setChunkSize(fileSizeRemaining);

						// Represents the receiving buffer
						byte[] receiveBuffer = new byte[chunkSize];

						// Variable used to read if there are remaining size
						// left to read.
						int num;

						while ((num = input.read(receiveBuffer)) > 0) {
							// Write the received bytes into the
							// RandomAccessFile
							downloadingFile.write(Arrays.copyOf(receiveBuffer, num));

							// Reduce the file size left to read..
							fileSizeRemaining -= num;

							// Set the chunkSize again
							chunkSize = setChunkSize(fileSizeRemaining);
							receiveBuffer = new byte[chunkSize];

							// If you're done then break
							if (fileSizeRemaining == 0) {
								break;
							}
						}
						downloadingFile.close();
					} catch (org.json.simple.parser.ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int setChunkSize(long fileSizeRemaining) {
		// Determine the chunkSize
		int chunkSize = 1024 * 1024;

		// If the file size remaining is less than the chunk size
		// then set the chunk size to be equal to the file size.
		if (fileSizeRemaining < chunkSize) {
			chunkSize = (int) fileSizeRemaining;
		}

		return chunkSize;
	}


	public static void AddOptions(Options options) {
		options.addOption("port", true, "Server port");
		options.addOption("ip", true, "Server IP address");
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
		options.addOption("relay", true, "query relay");

	}

}