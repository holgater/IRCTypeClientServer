import java.util.LinkedList;

public class pktListR extends pktGeneric {
	
	//object variable
	private boolean listUsers;
	
	//constructor w/ header
	public pktListR(pktHeader headerIn) {
		super(headerIn);
	}

	//constructor w/o header
	public pktListR() {
		super(new pktHeader(0x10000004));
	}
	
	//checks if listUsers is set
	public boolean listUsers() {
		return listUsers;
	}

	//setter: listUsers
	public void setListUsers(boolean listUsersIn) {
		listUsers = listUsersIn;		
	}
}
