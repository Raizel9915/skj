package server;

import java.util.ArrayList;
import java.util.List;

public class PlayerMngement {
	private List<Player> list;
	
	//class containing list of the players and operations on it

	
	public PlayerMngement() {
		this.list = new ArrayList<Player>();
		
	}
	
	public synchronized void addPlayerToList(Player player) {
		list.add(player);
	}
	
	public synchronized void removePlayerFromList(Player player) {
		list.remove(player);
	}
	
	public synchronized List<Player> getAllPlayerList() {
		return list;
	}

	public synchronized List<Player> getPlayersWithStatus(PlayerStatus playerStatus) {
		List<Player> playersStatusList = new ArrayList<Player>();
		for(Player p: list) {
			if(p.getPlayerStatus() == playerStatus) {
				playersStatusList.add(p);
			}
		}
		return playersStatusList;
	}
	
	public String toString() {
		return "Players list: \n" + list;
	}

}
