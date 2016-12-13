import java.io.Serializable;

public class pktGeneric implements Serializable {
	
	private pktHeader header;
	
	public pktGeneric(pktHeader headerIn) {
		header = headerIn;
	}

	public pktHeader getHeader() {
		return header;
	}

	public int getOpcode() {
		return header.getOpcode();
	}
}
