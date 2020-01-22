package server;

public class ServerMngement {
	private PlayerMngement mngePlayer;
	private GameMngement mngeGame;
	private ServerLogger serverLogger;
	
	//getter class to access PlayerMngement and GameMngement classes by other classes
	
	public ServerMngement(ServerLogger serverLogger) {
		this.serverLogger = serverLogger;
		mngePlayer = new PlayerMngement();
		mngeGame = new GameMngement(serverLogger);
	}
	
	public PlayerMngement getPlayerMnge() {
		return mngePlayer;
	}
	
	public GameMngement getGameMnge() {
		return mngeGame;
	}
	

}
