package server;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;

public class ClientConnectionThread extends Thread {
	private Socket clientSocket;
	private InputStream is;
	private OutputStream os;
	private ServerLogger serverLogger;
	private Player player;
	private ServerMngement mngeGames;
	private boolean keepAlive = true;
	private Game currentGame;
	private int order;
	private Player opponent;
	private boolean keepGameAlive = true;;
	
	/*
	constructor for thread handling the client player
	starts input and output streams to communicate with client as well as created "profile"
	for the player and adds him to list of currently logged in players
	*/
	
	public ClientConnectionThread(Socket clientSocket, ServerLogger serverLogger, ServerMngement mngeGames) {
		this.clientSocket = clientSocket;
		this.serverLogger = serverLogger;
		try {
			is = clientSocket.getInputStream();
			os = clientSocket.getOutputStream();
			player = new Player(clientSocket.getRemoteSocketAddress(), serverLogger);   ///creating new player

			this.mngeGames = mngeGames;
			
			mngeGames.getPlayerMnge().addPlayerToList(player);
			currentGame = null;
   
			
			this.start();
		} catch (IOException e) {
			serverLogger.piszLog("Exception while creating connection thread with Client: " + e.getMessage());
		}
	}
	
	/*
	 this function receives command from client, based on the client's command it will either log out the client (terminate 
	the connection), send him the list of currently logged in players or will start searching for an opponent for the player
	
	 */
	
	
	
	public void run() {
		serverLogger.piszLog("Running ClientConnectionThread for: " + clientSocket);
		
		
		byte[] byteCommand = new byte[400];

		while(keepAlive) {

			try {
				int bytes_read = is.read(byteCommand);
				String commandString = new String(byteCommand, 0, bytes_read);
				
				
				if(commandString.equals("LOGOUT")) {
					keepAlive = false;
					mngeGames.getPlayerMnge().removePlayerFromList(player);
				} else if(commandString.equals("PLAY")) {
					startGame();
				} else if(commandString.equals("LIST")) {
					System.out.println("Received LIST command and proceeding to execute function");
					sendList();
					System.out.println("Finished sending list function");
				} else {
					//repeat the loop
				}
				
				
			} catch (IOException e) {
				serverLogger.piszLog("IOException in commmands " + e.getMessage());
				keepAlive = false;
			}

				
		}
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/*
	 this function starts looking for the game for the player who sent command "PLAY", finding the game is handled by the 
	 class called GameMngement (in the function attemptStartGame()) that sends finds the opponent and returns a game to this
	 function, next the function sets the int order and that will be used to identify turns for each player based on another
	 variable - currentOrder
	 */
	
	public void startGame() {
		
		serverLogger.piszLog("Waiting for new game for player" + player);
		
		currentGame = mngeGames.getGameMnge().attemptStartGame(player);
		
		serverLogger.piszLog("New game created for " + player);
		
		byte[] bytes;

		if(player.getPlayerID() == currentGame.getGamePlayer1().getPlayerID()) {
			order = 1;
			opponent = currentGame.getGamePlayer2();
		} else {
			order = 2;
			opponent = currentGame.getGamePlayer1();
		}
		
		String gameStartInfo = "" + opponent.getPlayerID() + ":" + order;
		
		bytes = gameStartInfo.getBytes();
		try {
			os.write(bytes);
			communicateWithClientWhileGame(); ////////////the game has started after sending info
		} catch (IOException e) {
			serverLogger.piszLog("IOException in start game in ClientConnectionThread for player " + player.getPlayerID());
			keepAlive = false;
		}
	
	}
	
	/*
	 This function takes a list of all players and converts it into String that next get converted into an array of bytes,
	 which finally gets sent through output stream to the client
	 */
	
	public void sendList() {

		List<Player> allPlayers = mngeGames.getPlayerMnge().getAllPlayerList();
		
		String string = "";
		
		System.out.println("Creating string to send");
		
		for(int i = 0; i < allPlayers.size(); i++) {
			string = string + allPlayers.get(i).toString() + "\n";
		}

		byte[] bytes = string.getBytes();

		try {
			System.out.println("Sending bytes arr");
			os.write(bytes);
			System.out.println("Finished sending");
			//os.flush();
			
		} catch (IOException e) {
			serverLogger.piszLog("IOException in sending list to client function" + e.getMessage());
		}
		
		
	}
	/*
	This function was called by another function only if current order was matching with the order given to the player
	and will take the mssge from the client (this mssge consists only of 2 ints 1 of them being int for the row, other for column)
	and will create object of the class Move, then the if block will check if the move is correct (if not then it will recursively
	call this function to receive the move again) - this part might need to be improved on the client side, given that the move
	from the client is correct it will call another function from the Game class that will "make" the move aka update the board
	for the server side
	*/
	
	public void receiveMyMove() {
		
		System.out.println("starting receive my move");
		
		
		try {

			byte[] bytes = new byte[200];
			int bytes_read;
			bytes_read = is.read(bytes);
			
			String moveString = new String(bytes, 0, bytes_read);
			
			System.out.println(moveString);
			
			int row = moveString.charAt(0) - 48;
			int column = moveString.charAt(1) - 48;
			
			System.out.print("row: " + row + " column: " + column);
			
			Move move = new Move(row, column, player.getIcon());
			
			if(currentGame.checkIfCorrectMove(move)) {
				currentGame.makeMove(move);
				
			}else {
				System.out.println("Wrong move");
				//receiveMyMove();
				
				
				
			}
			
		} catch (IOException e) {
			
		}
		
		
		
	}
	
	/*
	This function is for sending communicates to the client, each time move is made it sends the updated board result. This
	message always contains the same structure and is decoded by the client based on the element under certain index.
	The mssge structure is as follows:
	index 0 : row of the last move
	index 1: column of the last move
	index 2: char of the character that made the move (O or X)
	index 3: (updated in class Game after making move already) current order
	index 4: int being either 0(the game did not finish) or 1(if the game finished)
	index 5: winner - at the beginning its set to none with char '-', once the game ends class Game sets the winner to O or X

	*/
	public void updateBoardSendForPlayer() {
		Move lastMove = currentGame.getLastMove();
		int row = lastMove.getRow();
		int column = lastMove.getColumn();
		char playerChar = lastMove.getPlayerIcon();
		int currentOrderSend = currentGame.getCurrentOrder();
		int didGameEnd;
		char winner;
		
		if(currentGame.endGame() == true) {
			didGameEnd = 1;
			winner = lastMove.getPlayerIcon();
			
		} else {
			didGameEnd = 0;
			winner = '-';
		}
		
		String komunikat = "" + row + "" + column + playerChar + currentOrderSend + "" + didGameEnd + winner;
		
		byte[] bytes = komunikat.getBytes();
		
		try {
			os.write(bytes);
		} catch (IOException e) {
			serverLogger.piszLog("IOException while sending board update to player");
		}
		
	}
	
	/*
	This function is mainly used to supervise communication with the client side, it uses boolean value and keeps the loop
	alive as long as the game didnt end (set with each iteration - move based on result from class Game
	if the current order matches the assigned player order then it goes into receiveMyMove() function, otherwise the player
	calls function from Game class that makes him wait until other player finished making his move and game was updated,
	after end of the if-else block the function sends communicate to both clients with the function updateBoardSendForPlayer()
	*/
	
	public void communicateWithClientWhileGame() {
		
		serverLogger.piszLog("Starting duel for " + player);
		
		
		while(keepGameAlive) {
			System.out.println("current Order: " + currentGame.getCurrentOrder() + " Order: " + order + " Player id: " + player.getIcon());
			if(currentGame.getCurrentOrder() == order) {
				serverLogger.piszLog("About to go into receiveMyMove");
				receiveMyMove();
				serverLogger.piszLog("receiveMyMove");
				
				
				//notifyAll();
			} else {
				serverLogger.piszLog("about to go for wait");
				currentGame.makePlayerWait();
				serverLogger.piszLog("finished wait");
				
				
			}
			
			boolean isEndGameTrue = currentGame.endGame();
			
			if(isEndGameTrue == true) {
				keepGameAlive = false;
				
				player.setStatus(PlayerStatus.LoggedIn);
	
			}
			
			updateBoardSendForPlayer();
		
		
	}
	
		/*
	public void waitForOtherPlayerMove() {
		
	}
	*/
	

	}
}
