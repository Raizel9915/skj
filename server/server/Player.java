package server;

import java.net.SocketAddress;

public class Player {
	
	private static int lowestFreeNumber = 1;
	
	private int playerID;
	private SocketAddress socketAddress;
	private PlayerStatus playerStatus;
	private ServerLogger serverLogger;
	private char gameIcon = '-';
	
	/*
	 * Class that handles the player object, contains function for setting the status, keeps the client socket (ip and port number)
	 * 
	 */
	
	
	public Player(SocketAddress socketAddress, ServerLogger serverLogger) {
		this.socketAddress = socketAddress;
		playerID = lowestFreeNumber;
		playerStatus = PlayerStatus.LoggedIn;
		this.serverLogger = serverLogger;
		lowestFreeNumber++;
		serverLogger.piszLog("New player was created" + this);
	}
	
	public void setStatus(PlayerStatus status) {
		playerStatus = status;
	}
	
	public void setIcon(char icon) {
		gameIcon = icon;
	}
	
	public char getIcon() {
		return gameIcon;
	}
	
	public int getPlayerID() {
		return playerID;
	}
	
	public PlayerStatus getPlayerStatus() {
		return playerStatus;
	}
	
	public String toString() {
		return "Player: " + playerID + " Status: " + playerStatus + " Address: " + socketAddress;
	}
	
}