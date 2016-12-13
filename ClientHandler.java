import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

public class ClientHandler {

	//class variables
	private LinkedList<User> clients;
	private LinkedList<Room> rooms;
	
	//constructor
	public ClientHandler() {
		clients = new LinkedList<User>();
		rooms = new LinkedList<Room>();
		rooms.add(new Room("general"));
	}
	
	//add a user to the clients list
	public synchronized void addUser(User userIn) {
		clients.add(userIn);
		//add the user to the "general" room
		for (int i = 0; i < rooms.size(); ++i) {
			if (rooms.get(i).getName().equals("general"))
				rooms.get(i).addUser(userIn);
		}
	}

	//remove a user from the clients list
	public synchronized void removeUser(User userIn) {
		clients.remove(userIn);
	}

	//getter: rooms
	public synchronized LinkedList<Room> getRooms() {
		return rooms;		
	}
	
	//return linkedList of just the room with the specified name
	public synchronized LinkedList<Room> getRoom(String roomIn) {
		//the returned list
		LinkedList<Room> room = null;
		
		//find the specified room
		for (int i = 0; i < rooms.size(); ++i) {
			//if found, create new linked list, and add just that room
			if (rooms.get(i).getName().equals(roomIn)) {
				room = new LinkedList<Room>();
				room.add(rooms.get(i));
				break;
			}
		}
		
		return room;
	}
	
	//return the user object with the given username
	public synchronized User getUser(String usernameIn) {
		//go through each client and if the username matches, return it
		User userOut = null;
		for(int i = 0; i < clients.size(); ++i) {
			if (clients.get(i).getUsername().equals(usernameIn))
				userOut = clients.get(i);
		}
		return userOut;
	}

	//check if a user with specified username already exists
	public synchronized boolean userExists(String usernameIn) {
		//attempt to get the user
		User user = getUser(usernameIn);
		if (user != null)
			return true;
		//didn't find them
		return false;
	}

	//if the room exists, add the user to it
	//if not, create the room, then add the user to it
	public synchronized void joinRoom(User userIn, String roomIn) {
		//check if room exists
		for (int i = 0; i < rooms.size(); ++i) {
			//if the name matches, add the user and return
			if (rooms.get(i).getName().equals(roomIn)) {
				rooms.get(i).addUser(userIn);
				return;
			}
		}
		//if a matching room wasn't found, create it, and add the user
		Room room = new Room(roomIn);
		room.addUser(userIn);
		rooms.add(room);
	}
	
	//remove the user from the target room
	public synchronized void leaveRoom(User user, String leaveRoomIn) {
		//check if room exists
		for (int i = 0; i < rooms.size(); ++i) {
			//if the name matches, add the user and return
			if (rooms.get(i).getName().equals(leaveRoomIn)) {
				rooms.get(i).removeUser(user.getUsername());
				return;
			}
		}
	}

	//send the received broadcast to all users in the target room
	public synchronized void broadcastMessage(User userIn, pktBroadcast broadcastIn) {
		String room = broadcastIn.getTarget();
		//get the room
		Room targetRoom = this.getRoom(room).getFirst();
		
		//check if a room wasn't found and send error
		if (targetRoom == null) {
			//print system line
			System.out.println(userIn.getUsername() + ": [Error] Message sent to non-existant room");
			//create packet and get output stream
			pktError error = new pktError(0x10000004);
			ObjectOutputStream out = userIn.getOustream();
			//send packet
			try {
				out.writeObject(error);
				out.flush();
				out.reset();
			} catch (IOException e) {
				System.out.println("SERVER: [ERROR] IOException while sendng broadcast command");
				//e.printStackTrace();
			}
		}
		//get the list of users we're going to broadcast to
		LinkedList<String> users = targetRoom.getUsers();
				
		//loop through and send broadcast
		for(String user: users){
			//variable to hold the user object we want
			User userOut = null;
			//loop through connected clients, and find the one with the matching username
			for (int i = 0; i < clients.size(); ++i) {
				//if a match is found, set that user as the one we want
				if (clients.get(i).getUsername().equals(user)) {
					userOut = clients.get(i);
					break;
				}
			}
			//send the broadcast
			ObjectOutputStream out = userOut.getOustream();
			try {
				out.writeObject(broadcastIn);
			} catch (IOException e) {
				System.out.println("SERVER: [ERROR] IOException while sendng broadcast command");
				//e.printStackTrace();
			}
		}
		
	}

	
	//check if the given user is in the given room
	public boolean userInRoom(String usernameIn, String roomIn) {
		//get the room, unpack it
		LinkedList<Room> room = getRoom(roomIn);
		if (room == null) {
			//print system line
			System.out.println(usernameIn + " [ERROR] User attempted to message room that doesn't exist");
			//package error packet
			pktError error = new pktError(0x10000004);
			//get the outstream of the user
			ObjectOutputStream outStream = getUser(usernameIn).getOustream();
			//send the error
			try {
				outStream.writeObject(error);
				outStream.flush();
				outStream.reset();
			} catch (IOException e) {
				System.out.println("SERVER: [ERROR] IOException while sendng error command");
				//e.printStackTrace();
			}
			return false;
		}
		LinkedList<String> users = room.getFirst().getUsers();
		//cycle through the users and return true if found
		for (int i = 0; i < users.size(); ++i) {
			if (users.get(i).equals(usernameIn))
				return true;
		}
		//print system line
		System.out.println(usernameIn + " [ERROR] User attempted to message room they weren't in");
		//package error packet
		pktError error = new pktError(0x10000005);
		//get the outstream of the user
		ObjectOutputStream outStream = getUser(usernameIn).getOustream();
		//send the error
		try {
			outStream.writeObject(error);
			outStream.flush();
			outStream.reset();
		} catch (IOException e) {
			System.out.println("SERVER: [ERROR] IOException while sendng error command");
			//e.printStackTrace();
		}
		//if not found, return false
		return false;
	}

}
