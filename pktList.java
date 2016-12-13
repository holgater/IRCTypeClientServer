
public class pktList extends pktGeneric {

	//class variables
	private String room;
	
	//constructor w/o header
	public pktList() {
		super(new pktHeader(0x10000003));
		room = null;
	}
	
	//constructor w/ header
	public pktList(pktHeader headerIn) {
		super(headerIn);
		room = null;
	}
	
	//constructor w/ room
	public pktList(String roomIn) {
		super(new pktHeader(0x10000003));
		room = roomIn;
	}

	//getter: room
	public String getRoom() {
		return room;
	}


}
