package datatransfer;

import java.io.Serializable;

/**
 *This class Message used to wrap fishsticks object and command for data transfer
 * between fishsticks sever and client.
 * @author Phuong Pham, Shamarokh Arjumand
 */
public class Message implements Serializable{

/** Explicit serialVersionUID to avoid generating one automatically */
private static final long serialVersionUID = 1L;
/**
 * private String holds the command of simple protocol.
 * command values:
 * insert (client sends to server)
 * success (server sends to client)
 * Failure (server sends to client)
 * disconnect (client and server send to each other)
 */
private String command;
/**
 * private FishStick object holds the values of fishstick.
 */
private FishStick fishsticks;
/**
 * Default constructor initializes variable members with default values
 */
public Message() {

this.command = null;//initialize the command to null
this.fishsticks = null;//initialize the fishstick object or table
}
/**
 * Constructor with parameters to initialize variable members
 */
public Message(String command, FishStick fishsticks) {

this.command = command;
this.fishsticks = fishsticks;
}
/**
 * Getter for FishStick variable.
 * @return fishsticks the reference of the FishStick
 */
public FishStick getFishStick() {
return fishsticks;
}
/**
 * Setter for fishsticks variable.
 * @param fishsticks a FishStick object
 */
public void setFishStick(FishStick fishsticks) {
this.fishsticks = fishsticks;
}
/**
 * Setter for command variable.
 * @param command value to be set for the command variable
 */
public void setCommand(String command) {
this.command = command;
}
/**
 * Getter for command variable.
 * @return String returns command value
 */
public String getCommand() {
return this.command;
}
	
}
