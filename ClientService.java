import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class ClientService implements Runnable {

	//local variables
	private Socket socket;
	private ClientHandler clientHandler;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	User user;
	boolean quit;
	
	//Constructor 
	public ClientService(Socket socketIn, ClientHandler handlerIn)  {
		socket = socketIn;
		clientHandler = handlerIn;
		quit = false;
	}

	@Override
	public void run() {
		//run the client service
		try {
			try {
				
				//create new input/output streams
				inStream = new ObjectInputStream(socket.getInputStream());
				outStream = new ObjectOutputStream(socket.getOutputStream());
				
				//make sure the user has a unique username
				boolean uniqueUsername = false;
				while (!uniqueUsername) {
					//get hello message from client
					pktHello pktIn;
					//try to get incoming packet and username
					try {
						pktIn = (pktHello) inStream.readObject();
					} catch (ClassNotFoundException e) {
						System.out.println("SERVER: Class Cast Exception");
						return;
					}
					
					//create new user with username and output stream
					String username = pktIn.getUsername();
					if (!clientHandler.userExists(username)) {
						//create user and get out of loop
						user = new User(username, outStream);
						uniqueUsername = true;
					} else {
						//print attempt message and send existing user error packet
						System.out.println("SERVER: User attempted to join with existing username " + username);
						pktError error = new pktError(0x10000003);
						outStream.writeObject(error);
					}
				}
				//package outStream with username		
				//pass off output stream for handler to handle
				clientHandler.addUser(user);
				System.out.println("SERVER: Hello Received from " + user.getUsername());
				
				
				//send the pack then
				pktListR listR = new pktListR();
				outStream.writeObject(listR);
				outStream.flush();
				outStream.reset();
				//get the list of rooms from the client handler and send it to the client
				outStream.writeObject(clientHandler.getRooms());
				outStream.flush();
				outStream.reset();	
				//service the client
				serviceClient();
			} finally {
				//client has disconnected - 
				//remove output stream from handler
				clientHandler.removeUser(user);
				//close the socket
				socket.close();
				System.out.println("SERVER: Client " + user.getUsername() + " has disconnected");
			}
		} catch (IOException e) {
			System.out.println("SERVER: [ERROR] IOException during connection with client " + user.getUsername());
			//e.printStackTrace();
		}
		
	}

	private void serviceClient() throws IOException {
		while (!quit) {
			
			pktGeneric pktIn;
			//try to get incoming packet
			try {
				pktIn = (pktGeneric) inStream.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				return;
			}
			
			//actually handle the packet
			handlePacket(pktIn);
		}
		
	}

	private void handlePacket(pktGeneric pktIn) {
		int opcodeIn = pktIn.getOpcode();
		
		switch(opcodeIn) {
		
			case 0x10000001: //error
				//TODO
				break;
				
			case 0x10000002: //hello
				//print user and command to server console
				System.out.println(user.getUsername() + ": Hello [superfluous] (" + opcodeIn + ")");
				//do nothing - shouldn't be get hellos after first one
				break;
				
			case 0x10000003: //list
				
				//print user and command to server console
				System.out.println(user.getUsername() + ": List(" + opcodeIn + ")");
				//get the packet and room, declare listR response
				pktList list = (pktList) pktIn;
				String listRoom = list.getRoom();
				pktListR listR;
				
				//if a room was provided...
				if (listRoom != null) {
					//create package, turn on listUsers
					listR = new pktListR();
					listR.setListUsers(true);
					//update the clientHandler
					//clientHandler = Server.getClientHandler();
					//get the room and make sure it was found, if not, send an error
					LinkedList<Room> room = clientHandler.getRoom(listRoom);
					if (room == null) {
						//no room returned/room not found
						System.out.println(user.getUsername() + ": [ERROR] User requested users from non-existant room");
						pktError error = new pktError(0x10000004);
						//send the error
						try {
							outStream.writeObject(error);
							outStream.flush();
							outStream.reset();
						} catch (IOException e) {
							System.out.println("SERVER: [ERROR] IOException while sendng error command");
							//e.printStackTrace();
						}
						return;
					}
					//send packet & room
					try {
						outStream.writeObject(listR);
						outStream.flush();
						outStream.reset();
						outStream.writeObject(room);
						outStream.flush();
						outStream.reset();
					} catch (IOException e) {
						System.out.println("SERVER: [ERROR] IOException while sendng listR command");
						//e.printStackTrace();
					}
				} else {
					//otherwise if room wasn't provided
					//send listR packet, then send list of rooms
					listR = new pktListR();
					
					try {
						outStream.writeObject(listR);
						outStream.flush();
						outStream.reset();
						LinkedList<Room> rooms = clientHandler.getRooms();
						outStream.writeObject(rooms);
						outStream.flush();
						outStream.reset();
					} catch (IOException e) {
						System.out.println("SERVER: [ERROR] IOException while sendng listR command");
						//e.printStackTrace();
					}
					
				}
				
				break;
				
			case 0x10000005: //join
				//print system line
				System.out.println(user.getUsername() + ": Join (" + opcodeIn + ")");
				//open packet to get the target room
				pktJoin join = (pktJoin) pktIn;
				String joinRoom = join.getRoomName();
				//send the join command to the client handler
				clientHandler.joinRoom(user, joinRoom);
				break;
				
			case 0x10000006: //leave
				//print system line
				System.out.println(user.getUsername() + ": Leave (" + opcodeIn + ")");
				//open packet to get the target room
				pktLeave leave = (pktLeave) pktIn;
				String leaveRoom = leave.getRoomName();
				//send the leave command to the client handler
				clientHandler.leaveRoom(user, leaveRoom);
				break;
				
			case 0x10000007: //message
				//receive and unpack packet
				pktMessage pktMsg = (pktMessage) pktIn;
				//make sure user is in the room they're sending too
				if (!clientHandler.userInRoom(user.getUsername(), pktMsg.getTarget())) {
					//oops, user not in room, or room didn't exist
					return;
				}
				//print system line
				System.out.println(user.getUsername() + ": Message (" + opcodeIn + ")");
				//create a broadcast packet
				pktBroadcast broadcast = new pktBroadcast(pktMsg.getTarget(), user.getUsername(), pktMsg.getMessage());
				//send to clientHandler to send it out
				clientHandler.broadcastMessage(user, broadcast);
				break;
				
			case 0x10000009: //quit
				//display quit message
				System.out.println("SERVER: User " + user.getUsername() + " has quit");
				//remove user from client handler
				clientHandler.removeUser(user);				
				//close connections
				try {
					socket.close();
					inStream.close();
					outStream.close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				//notify run() we're quitting
				quit = true;
				return;
				
			default:
				//print system line
				System.out.println("SERVER: User " + user.getUsername() + " sent an unknown opcode");
				//create error packet and send
				pktError error = new pktError(0x10000002);
				//send the error
				try {
					outStream.writeObject(error);
					outStream.flush();
					outStream.reset();
				} catch (IOException e) {
					System.out.println("SERVER: [ERROR] IOException while sendng error command");
					//e.printStackTrace();
				}
				break;
				
		} //end handlePacket()
		
	}

}
