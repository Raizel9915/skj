package server;

public class MainServer {
	
	/*
	 * Main checking the number and correctness of parameters, if correct it calls function GameServer
	 * 
	 */
	
	public static void main(String[] args) {
		if(args.length == 1) {
			int serverPort = Integer.parseInt(args[0]);
			GameServer server = new GameServer(serverPort);
		} else {
			System.out.println("Wrong parameters!");
		}
	}

}
