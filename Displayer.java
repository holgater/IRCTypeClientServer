import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class Displayer implements Runnable {

	//class variables
	private ObjectInputStream inStream;
	
	//constructor w/ scanner
	public Displayer(ObjectInputStream inStreamIn) {
		inStream = inStreamIn;
	}
	
	@Override
	public void run() {
		while (true) {
			//check if we've been interruped
			if (Thread.currentThread().isInterrupted()) {
			  return;
			}
			
			pktGeneric pktIn;
			//try to get incoming packet
			try {
				pktIn = (pktGeneric) inStream.readObject();
			} catch (ClassNotFoundException e) {
				System.out.println("CLIENT: [ERROR] ClassNotFoundException during runtime");
				//e.printStackTrace();
				return;
			} catch (IOException e) {
				//can't figure out easy way to stop thread
				//System.out.println("CLIENT: [ERROR] IOException during runtime");
				//e.printStackTrace();
				return;
			}
			
			//actually handle the packet
			handlePacket(pktIn);
		}
	} //end run()

	private void handlePacket(pktGeneric pktIn) {
		int opcodeIn = pktIn.getOpcode();
		
		switch(opcodeIn) {
		
			case 0x10000001: //error
				
				handleError(pktIn);
				System.out.print("CLIENT: ");
				break;
				
			case 0x10000004: //list_r				
				//receive pktListR object
				pktListR response = (pktListR) pktIn;
				//check if we're displaying all rooms, or the users of a room
				boolean listUsers = response.listUsers();
				
				if (listUsers) {
					//if displaying users
					//we want to open the packet and list the users in the room given
					LinkedList<Room> room;
					try {
						room = (LinkedList<Room>) inStream.readObject();
					} catch (ClassNotFoundException e) {
						System.out.println("CLIENT: [ERROR] ClassNotFoundException while receiving listR command");
						//e.printStackTrace();
						System.out.print("CLIENT: ");
						return;
					} catch (IOException e) {
						System.out.println("CLIENT: [ERROR] IOException while receiving listR command");
						//e.printStackTrace();
						System.out.print("CLIENT: ");
						return;
					}
					//get the users
					LinkedList<String> users = room.get(0).getUsers();
					//loop through and list users
					System.out.print("CLIENT: USERS = ");
					for (int i = 0; i < users.size(); ++i) {
						System.out.print(" [" + users.get(i) + "]");
					}
					//print newline at the end for correct display
					System.out.print("\n");
					
				} else {
					//otherwise we're displaying all rooms
					//the room list will follow, so get that object
					LinkedList<Room> rooms;
					
					try {
						rooms = (LinkedList<Room>) inStream.readObject();
					} catch (ClassNotFoundException e) {
						//e.printStackTrace();
						System.out.print("CLIENT: ");
						return;
					} catch (IOException e) {
						//e.printStackTrace();
						System.out.print("CLIENT: ");
						return;
					}
					System.out.print("CLIENT: ROOMS =");
					//loops through and print room names
					for (int i = 0; i < rooms.size(); ++i) {
						System.out.print(" [" + rooms.get(i).getName() + "]");
					}
					//print newline at the end for correct display
					System.out.print("\n");
				}
				
				System.out.print("CLIENT: ");
				break;
				
			case 0x10000008: //broadcast
				
				//receive pktBroadcast object
				pktBroadcast broadcast = (pktBroadcast) pktIn;
				//access the sender, the room, and the message
				String room = broadcast.getTarget();
				String sender = broadcast.getSender();
				String message = broadcast.getMessage();
				//display the message
				System.out.println("[" + room + "] " + sender + ": " + message);
				
				System.out.print("CLIENT: ");
				break;
				
			default:
				//unknown opcode received, send error
				System.out.println("CLIENT: Unknown opcode received");
				System.out.print("CLIENT: ");
				break;
		
		}
	} //end handlePacket()

	private void handleError(pktGeneric pktIn) {
		//open the packet and get the error code
		pktError error = (pktError) pktIn;
		int errorCode = error.getErrorCode();
		
		switch (errorCode) {
		
			case 0x10000001: //error unknown
				System.out.println("CLIENT: [ERROR] Unknown");
				break;
			
			case 0x10000002: //illegal opcode
				System.out.println("CLIENT: [ERROR] Sent illegal opcode");
				break;
				
			case 0x10000004: //non-existent room
				System.out.println("CLIENT: [ERROR] Requested user list from non-existant room");
				break;
				
			case 0x10000005: //user not in room
				System.out.println("Client: [ERROR] Attempted to send a message to a room you're not in");
				break;
		}
		
	}

}
