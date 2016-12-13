
public class pktMessage extends pktGeneric {
	
	private String target;
	private String message;
	
	//constructor w/ header
	public pktMessage(pktHeader headerIn) {
		super(headerIn);
	}
	
	//constructor w/ room and message
	public pktMessage(String roomIn, String msgIn) {
		super(new pktHeader(0x10000007));
		target = roomIn;
		message = msgIn;
	}

	public String getMessage() {
		return message;
	}

	public String getTarget() {
		return target;
	}
}
