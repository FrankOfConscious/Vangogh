package Server;

import java.util.HashMap;

import org.json.simple.JSONObject;

public class KeyTuple<A,B,C,D> {
	/** <p>Title: TwoTuple</p>
	 * <p>Description: 三个元素的元组，用于在一个方法里返回两种类型的值</p>
	 */

	    private final String key1;
	    private final String key2;
	    private final String key3;
	    private Resource obj;
	    
	     
	    KeyTuple(Resource obj) {
	        key1 = obj.get("owner");
	        key2 = obj.get("channel");
	        key3 = obj.get("uri");
	        this.obj = obj;
	    
	}
	//如果两个tuple的key1，2，3都相同，则用新的obj替换原本keytuple中的obj
	    boolean ifOverwrites(KeyTuple old){
	    	if(this.key1.equals(old.getOwner())
	    			&& this.key2.equals(old.getChannel())
	    			&& this.key3.equals(old.getUri()))
	    		return true;
	    	else return false;
	    	
	    }
	    boolean ifOverwrites(JSONObject command){
	    	if(this.key1.equals(((HashMap) command.get("resource")).get("owner"))
	    			&& this.key2.equals(((HashMap) command.get("resource")).get("channel"))
	    			&& this.key3.equals(((HashMap) command.get("resource")).get("uri")))
	    		return true;
	    	else return false;
	    	
	    }
	    //如果两个tuple channel，uri一样，但owner不同，则报错
	    boolean ifduplicated(KeyTuple old){
	    	if(this.key2.equals(old.getChannel())
	    			&& this.key3.equals(old.getUri()))
	    		return true;
	    	else return false;
	    }
	    boolean ifduplicated(JSONObject command){
	    	if(this.key2.equals(((HashMap) command.get("resource")).get("channel"))
	    			&& this.key3.equals(((HashMap) command.get("resource")).get("uri")))
	    		return true;
	    	else return false;
	    }
	    
	    String getOwner(){
	    	return this.key1;
	    }
	    String getChannel(){
	    	return this.key2;
	    }
	    String getUri(){
	    	return this.key3;
	    }
	    Resource getObj(){
	    	return this.obj;
	    }
	    JSONObject toJSON(){
	    	JSONObject obj=new JSONObject();
	    	obj.put("name", this.getObj().get("name"));
	    	obj.put("tags", this.getObj().getTags());
	    	obj.put("description", this.getObj().get("description"));
	    	obj.put("uri", this.getObj().get("uri"));
	    	obj.put("channel", this.getObj().get("channel"));
	    	obj.put("owner", this.getObj().get("owner"));
	    	obj.put("ezserver", this.getObj().get("ezserver"));
	    	return obj;
	    }
	 
	    void overwrites(JSONObject command) {
	    	this.obj = new Resource(command);
	    }
	
	
	    

}
