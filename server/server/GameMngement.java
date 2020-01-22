package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameMngement {
	List<Player> waitingPlayerList;
	ServerLogger serverLogger;
	Map<Integer, Game> gameTrack;
	
	//constructor that creates the hash map that keeps track of all the ongoing games, the player number points to the game if
	//one exists
	public GameMngement(ServerLogger serverLogger) {
		waitingPlayerList = new ArrayList<Player>();
		this.serverLogger = serverLogger;
		gameTrack = new HashMap<Integer ,Game>();
	}
	
	/*
	 This function attempts to find the opponent for the player who sent command PLAY, after it was called it adds player to the
	 waiting list (players waiting for their match to be found) and if the list size is 2 (assuming function is sychronized -
	 accessed by 1 thread after another the size of it shouldnt go over it) it gets both players, removes them from the list
	 and creates game that gets returned to ClientConnectionThread as well as changes their statuses, if the list size is less
	 than 2 then the player (implemented in else part of the block) is forcefully made wait in while loop until he gets notified
	 by another player that accessed the function while this one was "asleep", at the end of both if and else once 2 players
	 got their match a newly created game is passed to both of the ClientConnectionThreads
	 
	 */
	
	public synchronized Game attemptStartGame(Player player) {
		waitingPlayerList.add(player);
		if(waitingPlayerList.size() == 2) {
			serverLogger.piszLog("2nd Player for game");
			Player g1 = waitingPlayerList.get(0);
			Player g2 = waitingPlayerList.get(1);
			Game newGame = new Game(g1, g2);
			
			waitingPlayerList.remove(1);
			waitingPlayerList.remove(0);
			
			gameTrack.put(g1.getPlayerID(), newGame);
			gameTrack.put(g2.getPlayerID(), newGame);
			serverLogger.piszLog("Created new Game for " + g1.getPlayerID() + " and " + g2.getPlayerID() + " and added game to hashmap");
			g1.setStatus(PlayerStatus.InGame);
			g2.setStatus(PlayerStatus.InGame);
			notifyAll();
			
			return newGame;
			
		} else {
			player.setStatus(PlayerStatus.WaitingForGame);
			while(gameTrack.get(player.getPlayerID()) == null) {
				try {
					wait();
				} catch (InterruptedException e) {
					serverLogger.piszLog("Interrupted Wait Exception");
				}
			}
			serverLogger.piszLog("First player found opponent");
			return gameTrack.get(player.getPlayerID());
		}
		
		
	}
	

}
