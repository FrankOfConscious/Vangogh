package Resource;

class Resource {
	private String owner="";
	private String channel="";
	private String uri="";
	private String name="";
	private String description="";
	private String[] tags=null;
	private String EZserver;
	
	public Resource(String name, String description, String[] tags, String uri, String channel, String owner, String EZserver){
		this.name=name;
		this.description=description;
		int num=tags.length;
		this.tags=new String[num];
		this.tags=tags;
		this.uri=uri;
		this.channel=channel;
		this.owner=owner;
		this.EZserver=EZserver;	
	}
	public void update(Resource obj, )
	
	

}
