package player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class GameClient {
	private int targetPort;
	private String targetAddress;
	private InputStream is;
	private OutputStream os;
	private Socket mainSocket;
	private boolean keepAlive = true;
	private int opponentID;
	private int order;
	private int currentOrder = 1;
	private boolean keepGameAlive = true;
	
	/*
	 * This class handles all the communication with server while the client is connected to it both outside and while game lasts
	 * the input and output stream are created for the client socket (given target port/address through parameter)
	 * 
	 */
	
	public GameClient(String targetAddress, int targetPort) {
		this.targetAddress = targetAddress;
		this.targetPort = targetPort;
		try {
			mainSocket = new Socket(targetAddress, targetPort);
			is = mainSocket.getInputStream();
			os = mainSocket.getOutputStream();
			startClient();
		} catch (UnknownHostException e) {
			System.out.print("No host: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}
		
		
	}
	
	/*This function creates scanner that will read commands from the player and then passes read command
	 * 
	 */
	
	public void startClient() {
		System.out.println("Main menu \n Type one of the commands: PLAY, LOGOUT, LIST");
		Scanner scanner = new Scanner(System.in);
		
		while(keepAlive) {
			String line = scanner.nextLine();
			recognizeAndRun(line);
			scanner.close();
			
		}
		try {
			mainSocket.close();
			System.out.println("Client socket closed: logged out");
		} catch (IOException e) {
			System.out.println("Client proccess close interrputed" + e.getMessage()) ;
		}

	}
	
	/*
	 * Recognizes the command given by the client and calls specific functions to handle them as well as sends them to the server,
	 * if command is wrongs waits to receive new one
	 */
	
	public void recognizeAndRun(String command) {
		
		byte[] bytes;
		
		if(command.equals(PlayerCommands.Logout)) {
			
			bytes = command.getBytes();
			try {
				os.write(bytes);
				os.flush();
			} catch (IOException e) {
				System.out.println("IOException in recognizeAndRun - logout" + e.getMessage());
			}
			keepAlive = false;
		} else if(command.equals(PlayerCommands.Play)) {
			bytes = command.getBytes();
			try {
				os.write(bytes);
				os.flush();
				startPlayGame();
			} catch (IOException e) {
				System.out.println("IOException in recognizeAndRun - play" + e.getMessage());
			}
			
		} else if(command.equals(PlayerCommands.List)) { //////////////////////////////////////////////////FINISH
			bytes = command.getBytes();
			try {
				os.write(bytes);
				os.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			getListFromServer();
		}
		
		else {
			System.out.print("Wrong command");
			startClient();
		}
	}
	
	/*
	 * This function gets called only after the opponent was found by the server, it receives from server information about
	 * the game that was created - order, symbol and current order are set-reconized based on the mssge received
	 */
	
	public void startPlayGame() {
		
		byte[] received = new byte[200];
		
		try {
			int bytes_read = is.read(received);
			String newGameInfo = new String(received, 0, bytes_read);
			
			int separateIndex = newGameInfo.indexOf(':');
			String opponentString = newGameInfo.substring(0, separateIndex);
			String orderString = newGameInfo.substring(separateIndex + 1);
			
			opponentID = Integer.parseInt(opponentString);
			order = Integer.parseInt(orderString);
			
			String moveInfo;
			
			if(order == 1) {
				moveInfo = "You start first and play as O";
			} else {
				moveInfo = "Your opponent starts first and you play X";
			}
			
			System.out.println("Opponent found \n + Opponent ID: " + opponentID + "\n" + moveInfo);
		
			communicateWithServerWhileGame(); //function handing the communication with server while the game lasts
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	/*
	 * Small function for reading move from the keybaord and sending it to server
	 */
	
	public void readMoveFromPlayer() {
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Give row");
		int row = scan.nextInt();
		System.out.println("Give column");
		int column = scan.nextInt();
		
		String move = "" + row + "" + column;
		
		byte[] bytes = move.getBytes();
		
		
		
		
		try {
			os.write(bytes);
			os.flush();
		} catch (IOException e) {
			System.out.println("IO Exception in readMoveFromPlayer");
		}
		
	}
	
	/*
	 * Main function overseeing the game, the while loop iterates as long as the game doesn't end (if it ends the value of
	 * keepGameAlive will be set to false and hence it will terminate the function, at the beginning of each iteration the 
	 * loop checks the currentOrder if it matches the order int assigned to the player at the beginning of the game
	 * and if it does it calls function to read and send the move from the player to the server
	 * regardless if the if condition was satisified and the block of the code was entered or not, before the loop terminated
	 * the function to read the communicate from the server will be called (because server always sends same mssge to both clients)
	 * 
	*/
	public void communicateWithServerWhileGame() {
	
		while(keepGameAlive) {
			
			if(currentOrder == order) {	
				readMoveFromPlayer();
			}
			
			byte[] bytes = new byte[400];
			
			try {
				int bytes_read = is.read(bytes);
				String fromServer = new String(bytes, 0, bytes_read);
				
				decodeFromServer(fromServer);
				
				
				
				
			} catch (IOException e) {
				System.out.println("IO Exception in communicateWithServerWhileGame");
				
			}
			
			
			
		}	
		
		
	}
	
	/*
	 * This function decodes the universal mssge set from the server and based on it prints different communicates on the client's
	 * screen (the mssge pattern was given in the comments on the server side), it also recognizes and prints the winner if the
	 * game was finished
	 */
	
	public void decodeFromServer(String fromServer) {
		int row = fromServer.charAt(0)-48;
		int column = fromServer.charAt(1)-48;
		char symbol = fromServer.charAt(2);
		int currOrder = fromServer.charAt(3)-48;
		int isGameFinished = fromServer.charAt(4)-48;
		char winner = fromServer.charAt(5);
		
		currentOrder = currOrder;
		
		if(currentOrder == order) {
			//to poprzedni ruch byl przeciwnika
			System.out.println("Opponent move: \n row: " + row + "\n column: " + column);
			
			if(isGameFinished == 1) {
				
				if(winner == '-') {
					System.out.println("Game ended with draw");
					startClient(); //brings back to the "command line" and awaits next command
				} else {
					System.out.println("Game ended \n");
					System.out.println("You loose");
					startClient(); //brings back to the "command line" and awaits next command
				}
				
				

			}
	
		} else if(currentOrder != order) {
			System.out.println("Board updated with your move");
			System.out.println("Row: " + row);
			System.out.println("Column: " + column);
			
			if(isGameFinished == 1) {
				
				if(winner == '-') {
					System.out.println("Game ended with draw");
					startClient(); //brings back to the "command line" and awaits next command
				} else {
					System.out.println("Game Ended");
					System.out.println("You win");
					startClient(); //brings back to the "command line" and awaits next command
				}
				
				
			}
			
		}
		
		
		
		
	}
	
	//small function for reading the list given from server and printing it out on the client's screen
	
	
	public void getListFromServer() {
		
		
		byte[] received = new byte[200000];
		try {
			int bytes_read = is.read(received);
			String playerList = new String(received, 0, bytes_read);
			System.out.println(playerList);
			startClient();
		} catch (IOException e) {
			System.out.println("IOException in getListFromServer()" + e.getMessage());
		}
		
	}
	
	
	

}
