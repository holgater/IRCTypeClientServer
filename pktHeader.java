import java.io.Serializable;

public class pktHeader implements Serializable {
	
	private int opcode;
	
	public pktHeader(int i) {
		opcode = i;
	}
	
	public int getOpcode() {
		return opcode;
	}
	
}
