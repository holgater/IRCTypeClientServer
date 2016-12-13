import java.io.ObjectOutputStream;
import java.util.LinkedList;

public class User {

	//class variables
	private String username;
	private ObjectOutputStream outStream;
	
	//constructor w/ username and outStream
	public User(String usernameIn, ObjectOutputStream outStreamIn) {
		username = usernameIn;
		outStream = outStreamIn;
	}

	//getter: username
	public String getUsername() {
		return username;
	}

	//getter: outStream
	public ObjectOutputStream getOustream() {
		return outStream;
	}

}
