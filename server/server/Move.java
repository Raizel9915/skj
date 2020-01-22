package server;

public class Move {
	private int column;
	private int row;
	private char playerIcon;
	
	/*
	 * Small class describing move that is made every turn by one of the players
	 */
	
	public Move(int row, int column, char playerIcon) {
		this.row = row;
		this.column = column;
		this.playerIcon = playerIcon;
	}
	
	public int getColumn() {
		return column;
	}
	
	public int getRow() {
		return row;
	}
	
	public char getPlayerIcon() {
		return playerIcon;
	}

}
