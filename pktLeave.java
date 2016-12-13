
public class pktLeave extends pktGeneric {
	
	//class variables
	private String roomName;
	
	//constructor w/ header
	public pktLeave(pktHeader headerIn) {
		super(headerIn);
	}

	//constructor w/ room
	public pktLeave(String roomIn) {
		super(new pktHeader(0x10000006));
		roomName = roomIn;
	}

	//getter: room name
	public String getRoomName() {
		return roomName;
	}
}
