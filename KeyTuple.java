package Server;

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
	    boolean ifOverwrites(KeyTuple old){//比较三个primary key，如果都相同，则返回true否则返回false。都相同需要overwrites操作
	    	if(this.key1.equals(old.getOwner())
	    			&& this.key2.equals(old.getChannel())
	    			&& this.key3.equals(old.getUri()))
	    		return true;
	    	else return false;
	    	
	    }
	    //如果要比较的话，需要给command和resource转换成KeyTuple，但是command里面不含obj啊
	    boolean ifduplicated(KeyTuple old){//比较 channel 和uri 是否都相同，如果都相同则返回true，说明违反了publish的倒数第三条规则
	    	if(this.key2.equals(old.getChannel())
	    			&& this.key3.equals(old.getUri()))
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
	    
	    

}
