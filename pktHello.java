
public class pktHello extends pktGeneric {
	
	public pktHello(pktHeader headerIn, String usernameIn) {
		super(headerIn);
		username = usernameIn;
	}

	private String username;
	
	public String getUsername() {
		return username;
	}
}
