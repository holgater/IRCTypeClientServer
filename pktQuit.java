
public class pktQuit extends pktGeneric {

	//constructor
	public pktQuit() {
		super(new pktHeader(0x10000009));
	}
	
	//constructor w/ header
	public pktQuit(pktHeader headerIn) {
		super(headerIn);
	}
}
