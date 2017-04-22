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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("unused")
class Client {
	private static String ip;
	private static int port;

	public static void main(String[] args) {
		System.out.println("Client has started.");

		// Parse CMD options
		Options options = new Options();
		AddOptions(options);

		// accept args from CMD
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

		// connect to a server socket
		try (Socket socket = new Socket(ip, port)) {

			// Get I/O streams for connection
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());

			if (cmd.hasOption("fetch")) {
				JSONFetch(cmd, output, input);
			} else {
				if (cmd.hasOption("publish")) {
					JSONPublish(cmd, output);
				} else if (cmd.hasOption("remove")) {
					JSONRemove(cmd, output);
				} else if (cmd.hasOption("share")) {
					JSONShare(cmd, output);
				} else if (cmd.hasOption("exchange")) {
					JSONExchange(cmd, output);
				} else if (cmd.hasOption("query")) {
					JSONQuery(cmd, output);
				}
				try {
					String message = input.readUTF();
					System.out.println(message);
				} catch (IOException e) {
					System.out.println("Server seems to have closed connection.");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked" })
	private static void JSONFetch(CommandLine command, DataOutputStream output, DataInputStream input) {
		String name = "";
		if (command.hasOption("name")) {
			name = command.getOptionValue("name");
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
			JSONParser parser = new JSONParser();
			while (true) {
				if (input.available() > 0) {
					String result = input.readUTF();// get input stream from
													// server
					JSONObject cmd = new JSONObject();
					try {
						cmd = (JSONObject) parser.parse(result);
						// Create a RandomAccessFile to read and write the
						// output file.
						RandomAccessFile downloadingFile = new RandomAccessFile("file", "rw");

						// Find out how much size is remaining to get from the
						// server.
						long fileSizeRemaining = (Long) cmd.get("resultSize");

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
						System.out.println(result);
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

	@SuppressWarnings("unchecked")
	private static void JSONExchange(CommandLine command, DataOutputStream output) {
		String[] host = new String[2];
		if (command.hasOption("servers")) {
			host = command.getOptionValue("servers").split(",");
		}
		JSONObject commandObj = new JSONObject();
		JSONObject element0 = new JSONObject();
		JSONObject element1 = new JSONObject();
		JSONArray ServerList = new JSONArray();

		element0.put("hostname", host[0]);
		element0.put("port", Integer.parseInt(command.getOptionValue("PORT")));
		element1.put("hostname", host[1]);
		element1.put("port", Integer.parseInt(command.getOptionValue("PORT")));
		ServerList.add(element0);
		ServerList.add(element1);

		commandObj.put("command", "EXCHANGE");
		commandObj.put("serverlist", ServerList);
		try {
			output.writeUTF(commandObj.toJSONString());
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	private static void JSONQuery(CommandLine command, DataOutputStream output) {
		String name = "";
		if (command.hasOption("name")) {
			name = command.getOptionValue("name");
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
		commandObj.put("command", "QUERY");
		commandObj.put("relay", true);
		commandObj.put("resourceTemplate", resourceTemplate);
		try {
			output.writeUTF(commandObj.toJSONString());
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	private static void JSONPublish(CommandLine command, DataOutputStream output) {

		String name = "";
		if (command.hasOption("name")) {
			name = command.getOptionValue("name");
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
		JSONObject resource = new JSONObject();
		JSONObject commandObj = new JSONObject();
		resource.put("name", name);
		resource.put("description", des);
		resource.put("uri", uri);
		resource.put("channel", channel);
		resource.put("owner", owner);
		resource.put("ezserver", null);
		commandObj.put("command", "PUBLISH");
		commandObj.put("resource", resource);
		try {
			output.writeUTF(commandObj.toJSONString());
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	private static void JSONRemove(CommandLine command, DataOutputStream output) {
		String name = "";
		if (command.hasOption("name")) {
			name = command.getOptionValue("name");
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
		JSONObject resource = new JSONObject();
		JSONObject commandObj = new JSONObject();
		resource.put("name", name);
		resource.put("description", des);
		resource.put("uri", uri);
		resource.put("channel", channel);
		resource.put("owner", owner);
		resource.put("ezserver", null);
		commandObj.put("command", "REMOVE");
		commandObj.put("resource", resource);
		try {
			output.writeUTF(commandObj.toJSONString());
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static void JSONShare(CommandLine command, DataOutputStream output) {
		String name = "";
		if (command.hasOption("name")) {
			name = command.getOptionValue("name");
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
		JSONObject resource = new JSONObject();
		JSONObject commandObj = new JSONObject();
		resource.put("name", name);
		resource.put("description", des);
		resource.put("uri", uri);
		resource.put("channel", channel);
		resource.put("owner", owner);
		resource.put("ezserver", null);
		String secrect = "";
		if (command.hasOption("secrect")) {
			secrect = command.getOptionValue("secrect");
		}
		commandObj.put("command", "SHARE");
		commandObj.put("resource", resource);
		commandObj.put("secrect", secrect);

		try {
			output.writeUTF(commandObj.toJSONString());
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void AddOptions(Options options) {
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

	}

}
