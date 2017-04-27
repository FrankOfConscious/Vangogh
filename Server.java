package Server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ServerSocketFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

	public class Server {
		
	
		// Declare the port number
		static int port;
		//static String hostname;
		// Identifies the user number connected
		static String advertisedHostName;
		private static int connectionIntervalLimit;
		private static int exchangeInterval;
		private static int counter = 0;
		private static boolean debug;
		public static  ArrayList< KeyTuple> resourceList=new ArrayList<KeyTuple>();
		static String secret = null;
		static ArrayList<String> serverRecords=new ArrayList<String>();


		
		@SuppressWarnings("deprecation")
		public static void main(String[] args) throws ParseException, org.apache.commons.cli.ParseException {
			
			
//			setServer(String[] args);
			
			
			//////////////////////////////////////////////////////////////
			// Parse CMD options
			//
			Options options = new Options();
			AddOptions(options);

			// accept args from CMD
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = null;

			cmd = parser.parse(options, args);
			if (cmd.hasOption("port")&& Math.isPort(cmd.getOptionValue("port")) ) {
				
				port = Integer.parseInt(cmd.getOptionValue("port"));
				
	//			advertisedHostName = cmd.getOptionValue("advertisedname");
			} else {
				System.out.println("Please provide invalid port options");
				System.exit(0);
			}
			if(cmd.hasOption("connectionintervallimit")){
				try{
					Server.connectionIntervalLimit=Integer.parseInt(cmd.getOptionValue("connectionintervallimit"));
				}catch(Exception e){
					e.printStackTrace();
				}
			}else Server.connectionIntervalLimit=1;
			if(cmd.hasOption("exchangeinterval")){
				try{
					Server.exchangeInterval=Integer.parseInt(cmd.getOptionValue("exchangeinterval"));
				}catch(Exception e){
					e.printStackTrace();
				}
			}else Server.exchangeInterval=600;
			if(cmd.hasOption("secret")){
				try{
					Server.secret=cmd.getOptionValue("secret");
				}catch(Exception e){
					e.printStackTrace();
				}
			}else {
				Random rand = new Random();
				Server.secret=getRandomString(rand.nextInt(10)+20);
			}
			if(cmd.hasOption("advertisedhostname")){
				try{
					Server.advertisedHostName=cmd.getOptionValue("advertiedhostname");
				}catch(Exception e){
					e.printStackTrace();
				}
			}else {
				InetAddress gethost;
				try {
					gethost = InetAddress.getLocalHost();
					Server.advertisedHostName=gethost.getHostName();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			if(cmd.hasOption("debug")) Server.debug=true;
			else debug=false;
//			System.out.println(Server.advertisedHostName);
//			System.out.println(Server.connectionIntervalLimit);
//			System.out.println(Server.exchangeInterval);
//			System.out.println(Server.port);
//			System.out.println(Server.secret);
//			System.out.println(Server.hostname);

			
			//////////////////////////////////////////////////////////////
			ServerSocketFactory factory = ServerSocketFactory.getDefault();
			try(ServerSocket server = factory.createServerSocket(port)){
				System.out.println("Waiting for client connection..");
				
				//***********************
				Thread t2 = new Thread(() -> {
					try {
						exchangeServerRec();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				t2.start();
				//**********************
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
		
		

		private static void serveClient(Socket client) {
			try(Socket clientSocket = client){
				
				// The JSON Parser
				JSONParser parser = new JSONParser();
				// Input stream
				DataInputStream input = new DataInputStream(clientSocket.
						getInputStream());
				// Output Stream
			    DataOutputStream output = new DataOutputStream(clientSocket.
			    		getOutputStream());
//			    System.out.println("CLIENT: "+input.readUTF());
//			    output.writeUTF("Server: Hi Client "+counter+" !!!");
			    
			    // Receive more data..
			    while(true){
			    	if(input.available() > 0){
			    		// Attempt to convert read data to JSON
			    		JSONObject command = (JSONObject) parser.parse(input.readUTF());
			    		System.out.println("COMMAND RECEIVED: "+command.toJSONString());//////
			    		JSONArray result = Math.parseCommand(command, output);
			    		for(int i=0;i<result.size();i++){
				    		
				    		output.writeUTF(((JSONObject)result.get(i)).toJSONString());
				    		output.flush();
				    		System.out.println("What server did:"+result.get(i).toString());/////
			    		}
//			    		output.flush();
//			    		output.close();
//			    		input.close();
			    		break;

			    	}
			    }
			    output.close();
	    		input.close();
			    
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
		}

		private static Integer parseCommand(JSONObject command, DataOutputStream output) {
			
			int result = 0;
			
			if(command.get("command_name").equals("Math")){
				Math math = new Math();
				Integer firstInt = Integer.parseInt(command.get("first_integer").toString());
				Integer secondInt = Integer.parseInt(command.get("second_integer").toString());
				
				switch((String) command.get("method_name")){
					case "add":
						result = math.add(firstInt,secondInt);
						break;
					case "multiply":
						result = math.multiply(firstInt,secondInt);
						break;
					case "subtract":
						result = math.subtract(firstInt,secondInt);
						break;
					default:
						// Really bad design!!
						try {
							throw new Exception();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
			}
			
			// This section deals with the file handler
			else if(command.get("command_name").equals("GET_FILE")){
				String fileName = (String) command.get("file_name");
				// Check if file exists
				File f = new File("server_files/"+fileName);
				if(f.exists()){
					
					// Send this back to client so that they know what the file is.
					JSONObject trigger = new JSONObject();
					trigger.put("command_name", "SENDING_FILE");
					trigger.put("file_name","sauron.jpg");
					trigger.put("file_size",f.length());
					try {
						// Send trigger to client
						output.writeUTF(trigger.toJSONString());
						
						// Start sending file
						RandomAccessFile byteFile = new RandomAccessFile(f,"r");
						byte[] sendingBuffer = new byte[1024*1024];
						int num;
						// While there are still bytes to send..
						while((num = byteFile.read(sendingBuffer)) > 0){
							System.out.println(num);
							output.write(Arrays.copyOf(sendingBuffer, num));
						}
						byteFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{
					// Throw an error here..
				}
			}
			// TODO Auto-generated method stub
			return result;
		}
		
		////////to update
		public static void AddOptions(Options options) {
			options.addOption("debug", false, "Print debut information");
			options.addOption("secret", true, "Server secret");
			options.addOption("port", true, "server port, an integer");
			options.addOption("exchangeinterval", true, "exchange interval in seconds");
			options.addOption("connectionintervallimit", true, "connection interval limit in seconds");
			options.addOption("advertisehostname", true, "advertised hostname");
			

		}
//		private static void setServer(String[] args) {
//			// TODO Auto-generated method stub
//			Options options = new Options();
//			AddOptions(options);
//
//			// accept args from CMD
//			CommandLineParser parser = new DefaultParser();
//			CommandLine cmd = null;
//
//			cmd = parser.parse(options, args);
//			if (cmd.hasOption("port") ) {
//				port = Integer.parseInt(cmd.getOptionValue("PORT"));
//	//			advertisedHostName = cmd.getOptionValue("advertisedname");
//			} else {
//				System.out.println("Please provide  PORT options");
//				System.exit(0);
//			}
//			if(cmd.hasOption("connectionintervallimit")){
//				try{
//					Server.connectionIntervalLimit=Integer.parseInt(cmd.getOptionValue("connectionintervallimit"));
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//			}else Server.connectionIntervalLimit=1;
//			if(cmd.hasOption("exchangeinterval")){
//				try{
//					Server.connectionIntervalLimit=Integer.parseInt(cmd.getOptionValue("exchangeinterval"));
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//			}else Server.exchangeInterval=600;
//			if(cmd.hasOption("secret")){
//				try{
//					Server.secret=cmd.getOptionValue("secret");
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//			}else {
//				Random rand = new Random();
//				Server.secret=getRandomString(rand.nextInt(10)+20);
//			}
//			if(cmd.hasOption("advertisedhostname")){
//				try{
//					Server.advertisedHostName=cmd.getOptionValue("advertiedhostname");
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//			}else {
//				InetAddress gethost;
//				try {
//					gethost = InetAddress.getLocalHost();
//					Server.advertisedHostName=gethost.getHostName();
//				} catch (UnknownHostException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} 
//			}
//		}
		private static String getRandomString(int length){
		     String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		     Random random=new Random();
		     StringBuffer sb=new StringBuffer();
		     for(int i=0;i<length;i++){
		       int number=random.nextInt(62);
		       sb.append(str.charAt(number));
		     }
		     return sb.toString();
		 }
		
		//********************
		private static void exchangeServerRec() throws UnknownHostException, IOException {
			
			
			// Creates a socket for another server, the socket that will send msg to

			Timer timer = new Timer();
			TimerTask task = new TimerTask() {

				public void run() {
					// TODO Auto-generated method stub
					if (serverRecords.size() == 0) {
						System.out.println("No record to share");
					} else {
						System.out.println("Ready to exchange my records");
						int selectedIndex = (new Random()).nextInt(serverRecords.size());
						System.out.println("After random!");
						String host_ip = serverRecords.get(selectedIndex);
						String[] host_ip_arr = host_ip.split(":");
						String host_name = host_ip_arr[0];
						int ip_add = Integer.parseInt(host_ip_arr[1]);
						JSONObject exchangeCommand = new JSONObject();
						String records = "";
						for (int i = 0; i<serverRecords.size(); i++) {
							if (i == serverRecords.size() - 1) {
								records += serverRecords.get(i);
							} else {
								records += serverRecords.get(i) + ",";
							}
						}
						exchangeCommand.put("command", "EXCHANGE");
						exchangeCommand.put("serverList", records);
						
						try(Socket randomServer = new Socket(host_name, ip_add)){
							DataInputStream input = new DataInputStream(randomServer.getInputStream());
							DataOutputStream output = new DataOutputStream(randomServer.getOutputStream());
							System.out.println("Ready to share my server records: " + records);
							output.writeUTF(exchangeCommand.toJSONString());
							output.flush();
							System.out.println("Command sent");
							
							// Time limit for execution
							long start = System.currentTimeMillis();
							long end = start + 5 * 1000;
							boolean isReachable = false;
							while(System.currentTimeMillis() < end) {
								if (input.available() > 0) {
									isReachable = true;
									String result = input.readUTF();
									System.out.println("Response from other server:" + result);
								}
							}
							if (!isReachable) {
								serverRecords.remove(selectedIndex);
								System.out.println("Removed unreachable server-" + serverRecords.get(selectedIndex));
							}
							
						} catch (IOException e) {
							//e.printStackTrace();
							System.out.println("Record invalid!" + serverRecords.size());
							serverRecords.remove(selectedIndex);
						}
					}
				}
				
			};
			timer.schedule(task, 0, exchangeInterval * 1000);
		}

	}
