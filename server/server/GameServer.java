package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
	private int portNumber;
	private ServerSocket serverSocket;
	private ServerLogger serverLogger;
	private ServerMngement mngeGames;
	
	/*
	 * This class creates main GameServer based on the given sockets and waits for new connections from the game clients
	 * 
	 */
	
	public GameServer(int portNumber) {
		this.portNumber = portNumber;
		serverLogger = new ServerLogger();
		try {
			serverSocket = new ServerSocket(portNumber);
			serverLogger.piszLog("Created new ServerSocket for GameServer: " + serverSocket);
			mngeGames = new ServerMngement(serverLogger);
			startGameServer();
		} catch (IOException e) {
			serverLogger.piszLog("Socket is currently busy, please restart with different socket number: " + e.getMessage());
			//e.printStackTrace();
		}
		
	}
	
	public void startGameServer() {
		while(true) {
			try {
				serverLogger.piszLog("Waiting for new connection to game server");
				Socket clientSocket = serverSocket.accept();
				ClientConnectionThread clientThread = new ClientConnectionThread(clientSocket, serverLogger, mngeGames);
				serverLogger.piszLog("Connection from new player");
			} catch (IOException e) {
				serverLogger.piszLog("Interrupted game server: " + e.getMessage());
			}
		}
	}
}
