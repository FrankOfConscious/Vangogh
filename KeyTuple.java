package Server;

public class KeyTuple<A,B,C,D> {
	/** <p>Title: TwoTuple</p>
	 * <p>Description: 三个元素的元组，用于在一个方法里返回两种类型的值</p>

	 */

	    public final String key1;
	    public final String key2;
	    public final String key3;
	    public Resource obj;
	    
	     
	    KeyTuple(Resource obj) {
	        key1 = obj.get("owner");
	        key2 = obj.get("channel");
	        key3 = obj.get("uri");
	        this.obj = obj;
	    
	}

}
