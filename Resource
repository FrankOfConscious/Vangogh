package Server;

import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

class Resource {
	private String owner="";
	private String channel="";
	private String uri="";
	private String name="";
	private String description="";
	private String[] tags=null;
	private String ezserver;
	
	//用来存储server里创建的Resource obj
	private static ArrayList<Resource> resourceList = new ArrayList<Resource>();
	
	public Resource(String name, String description, String[] tags, String uri, String channel, String owner, String ezserver){
		this.name=checker(name);
		this.description=checker(description);
		int num=tags.length;
		this.tags=new String[num];
		this.tags=tags;
		this.uri=checker(uri);
		this.channel=checker(channel);
		this.owner=checker(owner);
		this.ezserver=checker(ezserver);	
	}
	//输入JSONObject，建成的Resource和上面一个方法类似
	public Resource(JSONObject command) {
		this.name = checker((String) command.get("name"));
		this.tags = (String[])((HashMap)command.get("resource")).get("tags");
		this.description = checker((String) command.get("description"));
		this.uri= checker((String) command.get("uri"));
		this.channel = checker((String) command.get("channel"));
		this.owner = checker((String) command.get("owner"));
		this.ezserver = checker((String)command.get("ezserver"));
	}
	//根据command创建Resource，并将其存储在resourceList里
	public static void createResource(JSONObject command) {
		resourceList.add(new Resource(command));
	}
	
	public void update(Resource obj, String username ){
		
		
	}
	
	
	private static String checker(String input){
		String b=input.replaceAll("\\s*", "");
		 b=b.replace("\0", "");
		return b;	
	}
	
        String[] getTags(){
		return this.tags;
	}
	
	
	
	String get(String str){
		switch(str){
			case "owner": return owner;
			case "channel": return channel;
			case "uri":return uri;
			case "name":return name;
			case "description":return description;
			case "EZserver": return ezserver; 
			default: return null;
			
		}
		
	}

}
