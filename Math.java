public class Math {
	public int add(int x, int y){
		return (x + y);
	}
	
	public int subtract(int x, int y){
		return (x - y);
	}
	
	public int multiply(int x, int y){
		return (x * y);
		
	}
	
	public static JSONObject parseCommand(JSONObject command, DataOutputStream outpur) {
		JSONObject result = new JSONObject();
		
		//this solves generic response
		if (command.containsKey("command")) {
			switch((String) command.get("command")) {
			//each case handles more explicit situation
			case "PUBLISH":
				publishJSON(command);
				break;
			case "REMOVE":
				break;
			case "SHARE":
				break;
			case "QUERY":
				break;
			case "FETCH":
				break;
			case "EXCHANGE":
				break;
			default:
				//return invalid command
				result.put("response", "error");
				result.put("errorMessage", "invalid command");
				break;
			}
		} else {
			//return missing or incorrect type
			result.put("response", "error");
			result.put("errorMessage", "missing or incorrect type for command");
		}
		return result;
	}
	
	private static JSONObject publishJSON(JSONObject command) {
		JSONObject result = new JSONObject();
		if (!command.containsKey("resource") ||
				!((JSONObject) command.get("resource")).containsKey("names") || 
				!((JSONObject) command.get("resource")).containsKey("tags")) {
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
			return result;
		}
		if (command.get("owner").equals("*")) {
			result.put("response", "error");
			result.put("errorMessage", "invalid resource");
			return result;
		}
		//other rules are not defined, should be added later
		return command;	
	}
