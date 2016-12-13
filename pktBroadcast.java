
public class pktBroadcast extends pktGeneric {
	
	//class variables
	private String target;
	private String sendingUser;
	private String message;
	
	//constructor w/ header
	public pktBroadcast(pktHeader headerIn) {
		super(headerIn);
	}
	
	//constructor w/ target, sender, and message
	public pktBroadcast(String targetIn, String sendingUserIn, String messageIn) {
		super(new pktHeader(0x10000008));
		target = targetIn;
		sendingUser = sendingUserIn;
		message = messageIn;
	}
	
	//getter: target
	public String getTarget() {
		return target;
	}
	
	//getter: sender
	public String getSender() {
		return sendingUser;
	}
	
	//getter: message
	public String getMessage() {
		return message;
	}
}
