import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Scanner;

public class Client {
	
	public static void main(String argv[]) throws Exception {
		
		//User fields
		String username;
		LinkedList<Room> rooms = new LinkedList<Room>();
		
		//set up for local user input/output 
		Scanner reader = new Scanner(System.in);
		
		//set up connection
		System.out.println("CLIENT: Connecting...");
		//setup socket and input/output streams
		Socket clientSocket = new Socket("localhost", 9001);
		ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
		ObjectInputStream inStream = new ObjectInputStream(clientSocket.getInputStream());
		System.out.println("CLIENT: Connection established");
		System.out.println("CLIENT: Verifying connection...");
		
		//we want to make sure the client is using a unique name
		boolean uniqueUsername = false;
		pktListR list;
		
		while (!uniqueUsername) {
			//get username
			System.out.print("CLIENT: Enter username: ");
			username = reader.nextLine();
			//send hello message and receive handshake
			System.out.println("CLIENT: Hello sent...");
			sendHello(username, outStream);
			pktGeneric response;
			//should be pktListR object on success, pktError on failure
			response = (pktGeneric) inStream.readObject();
			//check to see if response was handshake or error
			int opcode = response.getOpcode();
			if (opcode == 0x10000004) {
				//success
				list = (pktListR) response;
				rooms = (LinkedList<Room>) inStream.readObject();
				uniqueUsername = true;
			} else if (opcode == 0x10000001) {
				//error
				pktError error = (pktError) response;
				if (error.getErrorCode() == 0x10000003) {
					//user exists error
					System.out.println("CLIENT: [ERROR] A user with that username already exists");
				} else {
					//unknown
					System.out.println("CLIENT: [ERROR] unkown");
				}
			} else {
				//something else entirely
				//TODO
			}
		}
		//if out of the loop, must have received handshake
		System.out.println("CLIENT: Handshake received");
		System.out.println("CLIENT: Connection verified");
		
		//setup the display from inSttream
		Displayer display = new Displayer(inStream);
		//run displayer in separate thread so that incoming messages don't block use
		Thread thread = new Thread(display);
		thread.start();
		
		//Print help and received list of rooms
		System.out.println("CLIENT: For help type 'help'");
		System.out.print("CLIENT: ROOMS =");
		//loops through and print room names
		for (int i = 0; i < rooms.size(); ++i) {
			System.out.print(" [" + rooms.get(i).getName() + "]");
		}
		//print newline at the end for correct display
		System.out.print("\n");
		//display this the first time, then it's displayed at the end of each call
		System.out.print("CLIENT: ");
		//client loop
		while (true) {
			
			//get user input
			String line = reader.nextLine();
			
			//parse input
			if (line.equals("help")) {
				//if 'help'
				printHelp();
				System.out.print("CLIENT: ");
			} else 
			if (line.equals("quit")){
				//if 'quit'
				System.out.println("CLIENT: closing connection...");
				//create quit packet and send
				pktQuit quit = new pktQuit();
				outStream.writeObject(quit);
				//end displayer thread
				thread = null;
				//close connection, reader, socket
				inStream.close();
				outStream.close();
				reader.close();
				clientSocket.close();
				System.out.println("CLIENT: connection closed");
				return;
			} else {
				//otherwise...
				//package properly and send to output stream
				sendPkt(line, outStream);
							
			}
			
		} //end while
	} //end main

	//print the help command output
	private static void printHelp() {
		System.out.println("CLIENT: Type one of the following commands to get the listed result.\n"
				+ "\tlist [room] - list all available rooms. If a [room] was provided, list all users in that room instead.\n"
				+ "\tjoin [room1] [room2] ... [room n] - join all listed rooms. If a room doesn't exist, it'll be created and the user will join it.\n"
				+ "\tleave [room] - leave room [room]. If [room] doesn't exist nothing will happen.\n"
				+ "\t(msg (or) message) room:[room1] [message1] room:[room2] [message2] ... room:[room n] [message n]\n\t\t- send [message] to [room]. If [room] doesn't exist, nothing will happen\n"
				+ "\tquit - close the program");
	}

	//handle the command and output the appropriate packet
	private static void sendPkt(String line, ObjectOutputStream outStream) {
		//split on first whitespace to get command word
		String[] tokens = line.split("\\s+", 2);
		
		if (tokens[0].equals("list")) {
			//make sure there weren't too many arguments provided
			if (tokens.length > 2) {
				System.out.println("CLIENT: [ERROR] Invalid list - too many arguments provided.\n"
						+ "\tlist [room] - list all available rooms. If a [room] was provided, list all users in that room instead.\n");
				System.out.print("CLIENT: ");
				return;
			}
			//check if a room was provided (if so we're listing the users in that room)
			String listRoom = null;
			if (tokens.length == 2) {
				listRoom = tokens[1];
			}
			//create and send the list packet
			pktList list = new pktList(listRoom);
			try {
				outStream.writeObject(list);
				outStream.flush();
				outStream.reset();
			} catch (IOException e) {
				System.out.println("CLIENT: [ERROR] IOException while sendng list command");
				//e.printStackTrace();
			}
			
		} else if (tokens[0].equals("join")) {
			//make sure the command is valid
			if (tokens.length != 2) {
				System.out.println("CLIENT: [ERROR] Invalid join - incorrect number of arguments.\n"
						+ "\tjoin [room1] [room2] ... [room n] - join all listed rooms. If a room doesn't exist, it'll be created and the user will join it.\n");
				System.out.print("CLIENT: ");
				return;
			}
			//split to see if there's multiple rooms
			String[] rooms = tokens[1].split("\\s+");
			//loop and send a join for each room
			for (int i = 0; i < rooms.length; ++i) {
				//get the name and create a join packet
				String room = rooms[i].trim();
				pktJoin join = new pktJoin(room);
				//send the packet
				try {
					outStream.writeObject(join);
					outStream.flush();
					outStream.reset();
				} catch (IOException e) {
					System.out.println("CLIENT: [ERROR] IOException while sendng join command");
					System.out.print("CLIENT: ");
					//e.printStackTrace();
					return;
				}

			}
			//display prompt
			System.out.print("CLIENT: ");
			return;
			
		} else if (tokens[0].equals("leave")) {
			//make sure the command is valid
			if (tokens.length != 2) {
				System.out.println("CLIENT: [ERROR] Invalid leave - incorrect number of arguments.\n"
						+ "\tleave [room] - leave room [room]. If [room] doesn't exist nothing will happen.\n");
				System.out.print("CLIENT: ");
				return;
			}
			//split again to get [room]
			String room = tokens[1];
			//create and send the packet
			pktLeave leave = new pktLeave(room);
			//send the packet
			try {
				outStream.writeObject(leave);
				outStream.flush();
				outStream.reset();
			} catch (IOException e) {
				System.out.println("CLIENT: [ERROR] IOException while sendng leave command");
				System.out.print("CLIENT: ");
				//e.printStackTrace();
				return;
			}
			System.out.print("CLIENT: ");
			return; 
			
		} else if (tokens[0].equals("msg") || tokens[0].equals("message")) {
			
			//Where we'll store the current room and message
			String room = "";
			String messageOut = "";
			
			//we need to split the rest of the input, and start checking for "room:"
			String[] msgTokens = tokens[1].split("\\s+");
			
			//make sure the command is valid, we want at least 2 tokens
			if (msgTokens.length < 2) {
				System.out.println("CLIENT: Invalid message - too few arguments provided.\n"
						+ "\t(msg (or) message) room:[room1] [message1] room:[room2] [message2] ... room:[room n] [message n]\n\t\t- send [message] to [room]. If [room] doesn't exist, nothing will happen\n");
				System.out.print("CLIENT: ");
				return;
			}
			//check if it doesn't start with "room:"
			if (!msgTokens[0].matches("room:[a-zA-Z0-9]*")) {
				System.out.println("CLIENT: Invalid message - second argument does not match \"room:[room]\".\n"
						+ "\t(msg (or) message) room:[room1] [message1] room:[room2] [message2] ... room:[room n] [message n]\n\t\t- send [message] to [room]. If [room] doesn't exist, nothing will happen\n");
				System.out.print("CLIENT: ");
				return;
			}
			
			for (int i = 0; i < msgTokens.length; ++i) {
				
				//if the current word matches "room:[room]", we've reached a new message
				// so send the stored message packet and start a new one
				if (msgTokens[i].matches("room:[a-zA-Z0-9]*")) {
					
					//check if there's a stored room/message (we don't need to send if this is the first message)
					if(!messageOut.equals("")) {
						//create the message packet and send it
						pktMessage msg = new pktMessage(room, messageOut);
						try {
							outStream.writeObject(msg);
							outStream.flush();
							outStream.reset();
						} catch (IOException e) {
							System.out.println("CLIENT: [ERROR] IOException while sendng message command");
							//e.printStackTrace();
						}
						//reset the message field to start capturing a new one
						messageOut = "";
					} else {
						//if this is the first message, check that a message was provided
						if (msgTokens.length < 2) {
							System.out.println("CLIENT: Invalid message - room provided without message.\n"
									+ "\t(msg (or) message) room:[room1] [message1] room:[room2] [message2] ... room:[room n] [message n]\n\t\t- send [message] to [room]. If [room] doesn't exist, nothing will happen\n");
							System.out.print("CLIENT: ");
							return;
						}
					}
					
					//remove the "room:" from the beginning of the current token, and set as room
					room = msgTokens[i].replace("room:", "");
					
				} else {
					//if we haven't found a "room:[room]" token, we're still adding to the message
					messageOut += " " + msgTokens[i];
				}
				
			} //end for
			
			//now send the last message
			pktMessage msg = new pktMessage(room, messageOut);
			try {
				outStream.writeObject(msg);
				outStream.flush();
				outStream.reset();
			} catch (IOException e) {
				System.out.println("CLIENT: [ERROR] IOException while sendng message command");
				//e.printStackTrace();
			}

		} else {
			//user entered unknown command
			System.out.println("CLIENT: Unknown command. For help type 'help'");
		}
		
		return;
	} //end send packet

	//send the hello packet to initiate communication
	private static void sendHello(String usernameIn, ObjectOutputStream outStreamIn) {
		//create the packet
		pktHeader header = new pktHeader(0x10000002);
		pktHello hello = new pktHello(header, usernameIn);
		//send the packet
		try {
			outStreamIn.writeObject(hello);
		} catch (IOException e) {
			System.out.println("CLIENT: [ERROR] IOException while sendng hello command");
			//e.printStackTrace();
		}
	}
	
}
