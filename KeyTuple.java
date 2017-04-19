package Server;

public class KeyTuple<A,B,C,D> {
	/** <p>Title: TwoTuple</p>
	 * <p>Description: ����Ԫ�ص�Ԫ�飬������һ�������ﷵ���������͵�ֵ</p>
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
	    boolean ifOverwrites(KeyTuple old){
	    	if(this.key1.equals(old.getOwner())
	    			&& this.key2.equals(old.getChannel())
	    			&& this.key3.equals(old.getUri()))
	    		return true;
	    	else return false;
	    	
	    }
	    
	    boolean ifduplicated(KeyTuple old){
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
