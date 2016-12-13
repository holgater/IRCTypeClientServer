import java.io.Serializable;
import java.util.LinkedList;

public class Room implements Serializable {

	//class variables
	private String name;
	private LinkedList<String> users;
	
	//constructor w/ name
	public Room(String nameIn) {
		name = nameIn;
		users = new LinkedList<String>();
	}

	//getter: name
	public Object getName() {
		return name;
	}

	//getter: users
	public LinkedList<String> getUsers() {
		return users;
	}
	
	//add a user to the room
	public void addUser(User userIn) {
		users.add(userIn.getUsername());
	}

	//remove a user from the room
	public void removeUser(String userIn) {
		users.remove(userIn);
	}

	
	
}
