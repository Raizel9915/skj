package player;

public class MainClient {
	
	//small main function for checking number and correctness of parameters, if correct it creates object of GameClient class
	
	public static void main(String[] args) {
		if(args.length == 2) {
			int targetPort = Integer.parseInt(args[1]);
			String address = args[0];
			GameClient gameClient = new GameClient(address, targetPort);
			
		} else {
			System.out.println("Client wrong parameters!");
		}
	}

}
