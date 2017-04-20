package Server;

class Resource {
	private String owner="";
	private String channel="";
	private String uri="";
	private String name="";
	private String description="";
	private String[] tags=null;
	private String EZserver;
	
	public Resource(String name, String description, String[] tags, String uri, String channel, String owner, String EZserver){
		this.name=checker(name);
		this.description=checker(description);
		int num=tags.length;
		this.tags=new String[num];
		this.tags=tags;
		this.uri=checker(uri);
		this.channel=checker(channel);
		this.owner=checker(owner);
		this.EZserver=checker(EZserver);	
	}
	//输入JSONObject，建成的Resource和上面一个方法类似
	public Resource(JSONObject json) {
		this.name = checker((String) json.get("name"));
		this.tags = (String[])((HashMap)json.get("resource")).get("tags");
		this.description = checker((String) json.get("description"));
		this.uri= checker((String) json.get("uri"));
		this.channel = checker((String) json.get("channel"));
		this.owner = checker((String) json.get("owner"));
		this.ezserver = checker((String)json.get("ezserver"));
	}
	
	public void update(Resource obj, String username ){
		
		
	}
	
	
	private static String checker(String input){
		String b=input.replaceAll("\\s*", "");
		 b=b.replace("\0", "");
		return b;	
	}
	
	public static void main(String[] args) {
		System.out.println(checker("   abcd  "));
	
	}
	
	
	
	String get(String str){
		switch(str){
			case "owner": return owner;
			case "channel": return channel;
			case "uri":return uri;
			case "name":return name;
			case "description":return description;
			case "EZserver": return EZserver; 
			default: return null;
			
		}
		
	}
	
	

}
