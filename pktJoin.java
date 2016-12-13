
public class pktJoin extends pktGeneric {
	
	//class variables
	String roomName;
	
	//constructor w/ header
	public pktJoin(pktHeader headerIn) {
		super(headerIn);
	}

	//constructor w/ room
	public pktJoin(String roomIn) {
		super(new pktHeader(0x10000005));
		roomName = roomIn;
	}

	//getter: room name
	public String getRoomName() {
		return roomName;
	}
}
