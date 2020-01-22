package server;

public class Game {
	private static final int boardSize = 3;
	private static final int countToWin = boardSize;
	private Player G1;
	private Player G2;
	private char[][] ticTactoe = new char[boardSize][boardSize];
	private Player winner;
	private int currentOrder;
	private Move lastMove;
	private boolean didMoveFinish = false;
	
	
	
	/*
	This class describes Game objects that are created for the players (one Game object is being accessed by its 2 respective
	players), it contains the currentOrder that keeps track of which player makes the next move, game board, functions that check if the
	given move is correct or not, make move as well as check if game ended or not or set the winner
	
	
	*/
	
	//constructor for the clas
	
	public Game(Player G1, Player G2) {
		this.G1 = G1;
		this.G2 = G2;
		winner = null;
		G1.setIcon('O');
		G2.setIcon('X');
		currentOrder = 1;
		lastMove = null;
		
		//creating board for the game
		
		for(int i = 0; i < boardSize; i++) {
			for(int j = 0; j < boardSize; j++) {
				ticTactoe[i][j] = ' ';
			}
		}
		
	}
	
	//small function that sets the order diff from the prev one
	
	public synchronized void setCurrentOrder() {
		if(currentOrder == 1) {
			currentOrder = 2;
		} else if(currentOrder == 2) {
			currentOrder = 1;
		}
	}
	
	//List of getters for the private variables of the class - mostly accessed in the ClientConnectionThread
	
	public synchronized Player getWinner() {
		return winner;
	}
	
	public synchronized int getCurrentOrder() {
		return currentOrder;
	}
	
	
	public synchronized Move getLastMove() {
		return lastMove;
	}
	
	public synchronized Player getGamePlayer1() {
		return G1;
	}
	
	public synchronized Player getGamePlayer2() {
		return G2;
	}
	
	//function for checking if the move is correct
	
	public synchronized boolean checkIfCorrectMove(Move move) {
		int row = move.getRow();
		int column = move.getColumn();
		
		if((row > boardSize-1) || (row < 0)) {
			return false;
		}
		if((column > boardSize-1) || (column < 0)) {
			return false;
		}
		
		if(ticTactoe[row][column] == ' ') {
			return true;
		}
		
		return false;
	}
	
	/*
	Once its confirmed that the move is correct this function gets called in ClientConnectionThread and updated the board
	with the given move, calls the function to set currentOrder tot he next player as well as notifies the sleeping - waiting
	client thread the the move was made
	*/
	
	public synchronized void makeMove(Move move) {
		int row = move.getRow();
		int column = move.getColumn();
		char icon = move.getPlayerIcon();
		ticTactoe[row][column] = icon;
		setCurrentOrder();
		setLastMove(move);
		
		
		didMoveFinish = true;
		
		notifyAll();
		
		
	}
	
	//small function for making player who is waiting for other players turn
	
	public synchronized void makePlayerWait() {
		while(!didMoveFinish) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		didMoveFinish = false;
	}
	
	//small setter for last move variable(its used to make the creation of communicate easier)
	
	public synchronized void setLastMove(Move move) {
		lastMove = move;
	}
	
	/*
	 This function checks if the game can be continued or not with 3 loops that check 4 diff cases that end the game
	 loop 1: checks if there r 3 same symbols in the same row - if yes then it sets the winner to 1 of the players
	 loop 2: checks if there r 3 same symbols in the same column - if yes then it sets the winner to 1 of the players
	 loop 3: checks if there r 3 same symbols in the same diagonal - if yes then it sets the winner to 1 of the players
	 loop 4: checks if there is at least 1 empty field (if not and no one won till now), it sets the winner to null
	 */
	
	public synchronized boolean endGame() {
		
		
		boolean emptyFieldExists = false;
		//this for is for setting emptyFieldExists
		for(int row = 0; row < boardSize; row++) {
			for(int column = 0; column < boardSize; column++) {
				if(ticTactoe[row][column] == ' ') {
					emptyFieldExists = true;
				}
			}
		}
		
		
		
		//
		for(int row = 0; row < boardSize; row++) {
			int OInRow = 0;
			int XinRow = 0;
			
			for(int column = 0; column < boardSize; column++) {
				if(ticTactoe[row][column] == ' ') {
					OInRow = 0;
					XinRow = 0;
				}
				if(ticTactoe[row][column] == 'O') {
					OInRow++;
					XinRow = 0;
					if(OInRow == countToWin) {
						
						if(G1.getIcon() == 'O') {
							winner = G1;
						} else {
							winner = G2;
						}
						return true;
					}
					
				}
				
				if(ticTactoe[row][column] == 'X') {
					OInRow = 0;
					XinRow++;
					if(XinRow == countToWin) {
						if(G1.getIcon() == 'X') {
							winner = G1;
						} else {
							winner = G2;
						}
						return true;
					}
					
					}
				
				
				}
				
			}
		
		
		//DO IN COLUMNS
		
		
		
		for(int column = 0; column < boardSize; column++) {
			int XInColumn = 0;
			int OInColumn = 0;
			
			for(int row = 0; row < boardSize; row++) {
				if(ticTactoe[row][column] == ' ') {
					XInColumn = 0;
					OInColumn = 0;
				}
				if(ticTactoe[row][column] == 'O') {
					OInColumn++;
					XInColumn = 0;
					
					if(OInColumn == countToWin) {
						
						if(G1.getIcon() == 'O') {
							winner = G1;
						} else {
							winner = G2;
						}
						return true;
						
					}
				}
				if(ticTactoe[row][column] == 'X') {
					XInColumn++;
					OInColumn = 0;
					if(XInColumn == countToWin) {
						
						if(G1.getIcon() == 'X') {
							winner = G1;
						} else {
							winner = G2;
						}
						return true;
						
					}
				}
				
			}
		}
		
		
		int XInDiagonal = 0;
		int OInDiagonal = 0;
		
		for(int rk = 0; rk < boardSize; rk++) {
			if(ticTactoe[rk][rk] == ' ') {
				XInDiagonal = 0;
				OInDiagonal = 0;
			}
			if(ticTactoe[rk][rk] == 'O') {
				OInDiagonal++;
				XInDiagonal = 0;
				
				if(OInDiagonal == countToWin) {
					
					if(G1.getIcon() == 'O') {
						winner = G1;
					} else {
						winner = G2;
					}
					return true;
				}
				
				
			}
			if(ticTactoe[rk][rk] == 'X') {
				XInDiagonal++;
				OInDiagonal = 0;
				
				if(XInDiagonal == countToWin) {
					
					if(G1.getIcon() == 'X') {
						winner = G1;
					} else {
						winner = G2;
					}
					return true;
				}
				
			}
		
		}
		
		if(emptyFieldExists = false) {
			
			winner = null;
			return true;
		}
		
		return false;
		
		
		//sets the winner
	}
	
	

}
