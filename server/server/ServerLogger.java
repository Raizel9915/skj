package server;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerLogger {
	
	private final String nazwaPliku = "Logi.txt";
	private SimpleDateFormat formatDaty = new SimpleDateFormat("YYYY-MM-dd HH-mm-ss-SS");
	private PrintWriter printWriter;
	
	/*
	 * This class exists only to keep logs on the server actions and to check for any kind of errors
	 * It connects to other classes and helps print out all the errors and actions on the command line
	 * screen as well as saves them into log file "Logi.txt"
	 */
	
	//constructor
	public ServerLogger() {
		File logiFile = new File(nazwaPliku);
		try {
			printWriter = new PrintWriter(logiFile);
		} catch (FileNotFoundException e) {
			System.err.println("Server: No log file created");
		}
	}
	
	public synchronized void piszLog(String komunikat) {
		
		Date forLogs = new Date();
		String formatData = formatDaty.format(forLogs);
		String log = "Serwer: " +  formatData + " " + komunikat;
		System.out.println(log);
		System.out.flush();
		
		if(printWriter != null) {
			printWriter.println(log);
			printWriter.flush();
			
		}
		
	}
}
