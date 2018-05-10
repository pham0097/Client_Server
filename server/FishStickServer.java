package server;
/* File: FishStickServer.java
 * Author: Stanley Pieda, based on course materials by Todd Kelley
 * Modified Date:Jan 2018
 * Modified By: Phuong Pham, Shamarokh Arjumand
 * Modified Date: March, 2018
 * Description: Networking server that uses simple protocol to send and receive transfer objects.
 * 				Multi-threaded FishStick Server receives request from Fishstick Client and saves FishStick into database.
 * Modifications:
 *   Modified to use Message objects and the simple protocal (add/command_worked/command_failed/disconnect) to communicate with Fishstick Client
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import datatransfer.FishStick;
import datatransfer.Message;

import dataaccesslayer.FishStickDao;
import dataaccesslayer.FishStickDaoImpl;

/**
 * @author Phuong Pham, Shamarokh Arjumand
 * Date: March, 2018
 */
public class FishStickServer {
	private ServerSocket server; //private ServerSocket object for accepting FishStick Client connection.
	private Socket connection; //private Socket connection to FishStick Client.
	private int portNum = 8081; //private int holds server port number.
	public static ExecutorService threadExecutor = Executors.newCachedThreadPool(); //private ExecutorService objects for managing threads.
	
    /**
	 * main method, FishStick Server entry point.
	 */
	public static void main(String[] args) {
		if(args.length > 0){
			(new FishStickServer(Integer.parseInt(args[0]))).runServer();
		}else{
			(new FishStickServer(8081)).runServer();//run the server now
		}
	}
	/**
	 * Constructor with parameters to initialize variable members
	 */
	public FishStickServer(int portNum) {
		this.portNum = portNum; //set the port number
	}
	
	/**
	 * Initializes a new thread for communicating with a client by using Message object and the simple protocol
	 * @param connection a socket connection to FishStick Client
	 */
	public void talkToClient(final Socket connection, int portNum) {
		threadExecutor.execute( new Runnable () {
			public void run(){	
				ObjectOutputStream output = null;
				ObjectInputStream input = null;
				FishStickDao fishStickDAO = new FishStickDaoImpl();
				Message message = new Message() ;
				String commandFromUser = "";
				String uuid = "";
				System.out.println("Got a connection of port num: " + portNum);
			    Thread.currentThread().setName("Client Port: " + portNum);
				try {
					SocketAddress remoteAddress = connection.getRemoteSocketAddress();
					String remote = remoteAddress.toString();
					output = new ObjectOutputStream (connection.getOutputStream());
					input = new ObjectInputStream( connection.getInputStream());               
					do {
						message = (Message) input.readObject();
						commandFromUser=message.getCommand();
						if (message.getCommand().contentEquals("add")){
							System.out.println("From:" + remote + 
									" Command: " + commandFromUser +
									" FishStick: " + message.getFishStick().toString());
							//add FishStick into DB and reads the FishStick from DB (with PK)
							uuid=message.getFishStick().getUUID();
							try{
								fishStickDAO.insertFishStick(message.getFishStick());
								message.setFishStick(fishStickDAO.findByUUID(uuid));
								message.setCommand("command_worked");
							}catch (Exception e){
								System.out.println(e.getMessage());
								message.setCommand("command_failed");
								message.setFishStick(null);
							}

						}else{//disconnect
							System.out.println("From:" + remote + 
									" Command: " + commandFromUser +
									" FishStick: null");
							System.out.println(remote + " disconnected via request");
							message.setCommand("disconnect");
							message.setFishStick(null);
						}

						output.writeObject(message);
						output.flush();
					} while (commandFromUser.equalsIgnoreCase("add"));//loops when command from user is add
					//} while (message != null);
				} catch (IOException exception) {
		            System.err.println(exception.getMessage());
		            exception.printStackTrace();
		        }catch (ClassNotFoundException exception) {
		            System.out.println(exception.getMessage());
		            exception.printStackTrace();
		        } 
				finally {
				try{if(input != null){input.close();}}catch(IOException ex){
					System.out.println(ex.getMessage());}
				try{if(output != null){output.flush(); output.close();}}catch(IOException ex){
					System.out.println(ex.getMessage());}
				try{if(connection != null){connection.close();}}catch(IOException ex){
					System.out.println(ex.getMessage());}
		        }
			}
		});
	}
    /**
	 * Starts FishStick Server and waits for connections from FishStick Client.
	 */
	public void runServer(){
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM d yyyy hh:mm a");
		System.out.println("FishStickServer by: Phuong Pham, Shamarokh Arjumand run on "+ dateTime.format(format));
		
		try {
			server = new ServerSocket(portNum);
		}catch (IOException e){
			e.printStackTrace();
		}
		System.out.println("Listenting for connections...");
		while(true){
			try{
				connection = server.accept();
				talkToClient(connection, portNum);
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}
}
