package client;
/* File: FishStickClient.java
 * Description: This class perform as a Networking client that uses simple protocol to send and receive transfer objects.
 * 		FishStick Client creates Fishstick object based on user input, then wrap the Fishstick 
 * 		object and a String command as a Message object and sends it to Fishstick Server.
 * Author: Stanley Pieda, based on course example by Todd Kelley
 * Modified Date:Jan 2018
 * Modified By: Phuong Pham, Shamarokh Arjumand
 * Modified Date: March, 2018
 * Modifications:
 *   Modified to use Message objects and the simple protocal (add/command_worked/command_failed/disconnect) to communicate with Fishstick Server
*/
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.UUID;

import datatransfer.FishStick;
import datatransfer.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class Message used to wrap fishsticks object and command for data transfer between fishsticks sever and client.
 * @author Phuong Pham, Shamarokh Arjumand
 * @version 1.0.0 
 */
public class FishStickClient {
	
	private Socket connection; //private Socket connection to fishsticks Server.
	private ObjectOutputStream output; //private ObjectOutputStream object represents output stream.
	private ObjectInputStream input; //private ObjectInputStream object represents output stream.
	private Message message = null; //private Message object for data transfer between Cabbage Server and Client.
	private String serverName = "localhost"; //private String holds server name.
	private int portNum = 8081; //private int holds server port number.
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); //BufferedReader for reading user input.
	private String userChoice; //private String holds user choice (y for continue adding new fishstick, n for stop adding fishstick).

    /**
	 * main method, Fishsticks Client entry point.
	 */
	public static void main(String[] args) {
		switch (args.length){
		case 2:
			(new FishStickClient(args[1],Integer.parseInt(args[2]))).runClient();
			break;
		case 1:
			(new FishStickClient("localhost",Integer.parseInt(args[1]))).runClient();
			break;
		case 0:
		default:
			(new FishStickClient("localhost", 8081)).runClient();
			break;
		}

	}
	/**
	 * Constructor with parameters to initialize variable members
	 */
	public FishStickClient(String serverName){
		this.serverName = serverName;
	}
	/**
	 * Constructor with parameters to initialize variable members
	 */
	public FishStickClient(String serverName, int portNum){
		this.serverName = serverName;
		this.portNum = portNum;
	}
	/**
	 * sets up connection with Fishsticks Server, reads user input from console then wraps up Message object and sends Message object to Fishsticks Server
	 */
	public void runClient(){
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM d yyyy hh:mm a");
		System.out.println("FishsticksClient by:Phuong Pham, Shamarokh Arjumand run on " + dateTime.format(format));

		String myHostName = null;
		try {
			InetAddress myHost = Inet4Address.getLocalHost();
			myHostName = myHost.getHostName();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		try {
			connection = new Socket(InetAddress.getByName(serverName), portNum);
			output = new ObjectOutputStream (connection.getOutputStream());
			input = new ObjectInputStream( connection.getInputStream());               

			int recordNumber;
			String commandFromServer= "";
			message = new Message();
			
			do {
				FishStick newFishStick = new FishStick();
				System.out.println("Please enter data for new FishStick! => ");
				System.out.print("Please enter record number: ");
				recordNumber= Integer.parseInt(br.readLine());
				newFishStick.setRecordNumber(recordNumber); 
				
				System.out.print("Please enter Omega: ");
				newFishStick.setOmega(br.readLine().trim());
				
				System.out.print("Please enter Lambda: ");
				newFishStick.setLambda(br.readLine().trim());
				
				//generate a UUID for the FishStick
		        UUID uuid = UUID.randomUUID();
		        newFishStick.setUUID(uuid.toString());
				
				message.setFishStick(newFishStick);
				message.setCommand("add");

				output.writeObject(message);
				output.flush();
				message = (Message) input.readObject();
				commandFromServer=message.getCommand();
				if (commandFromServer.equalsIgnoreCase("command_worked"))
				{
					System.out.println("Command: " + message.getCommand() + 
								" Returned FishStick: " + message.getFishStick().toString());

				}
				else if(commandFromServer.equalsIgnoreCase("command_failed"))
				{
					System.out.println("Server failed to perform requested operation");

					message.setFishStick(null);
					message.setCommand("disconnect");
					output.writeObject(message);
					output.flush();
					message = (Message) input.readObject();
					if (!message.getCommand().equalsIgnoreCase("disconnect")){
						System.out.println("System Error, there should be disconnect command returned from server");
					}					
					System.out.println("Shutting down connection to server");
					break;
				}
				
				System.out.println("Do you want to add another FishStick? (y or n) : ");
				this.userChoice = br.readLine().trim();
				if (!userChoice.equalsIgnoreCase("y"))
				{
					message.setFishStick(null);
					message.setCommand("disconnect");

					output.writeObject(message);
					output.flush();
					message = (Message) input.readObject();
					if (message.getCommand().equalsIgnoreCase("disconnect")){
						System.out.println("Shutting down connection to server");
					}else{
						System.out.println("System error, there should be disconnect command returned from server");
					}
				}

			} while (userChoice.equalsIgnoreCase("y"));
			
		} catch (IOException exception) {
			System.out.println(exception.getMessage());
			exception.printStackTrace();
		} catch (ClassNotFoundException exception) {
			System.out.println(exception.getMessage());
			exception.printStackTrace();
		} 
		finally{
			try{if(input != null){input.close();}}catch(IOException ex){
				System.out.println(ex.getMessage());}
			try{if(output != null){output.flush(); output.close();}}catch(IOException ex){
				System.out.println(ex.getMessage());}
			try{if(connection != null){connection.close();}}catch(IOException ex){
				System.out.println(ex.getMessage());}
		}
	}

}
