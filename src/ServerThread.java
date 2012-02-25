import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;

public class ServerThread extends Thread
{
	// The Server that spawned us
	private Server server;
	// The Socket connected to our client
	private Socket socket;
	private static final String username = "";
	private static final String password = "";

	// Constructor.
	public ServerThread( Server server, Socket socket) {
		// Save the parameters
		this.server = server;
		this.socket = socket;
		// Start up the thread
		start();
	}

	// This runs in a separate thread when start() is called in the
	// constructor.
	public void run() {
		try {
			// Create a DataInputStream for communication; the client
			// is using a DataOutputStream to write to us
			DataInputStream din = new DataInputStream(socket.getInputStream());
			while (true) {
			String message = din.readUTF();
			System.out.println("Sending message");
			String[] splitted = message.split(";");
			if(splitted.length!=0){
				if(splitted[0].equals("client")){
					//Its an app wanting the link.
					DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
					Date date = Date.valueOf(splitted[1]);
					Time time = Time.valueOf(splitted[2]);
					String location = splitted[3];
					dout.writeUTF(getLink(date, time, location));
				}else if(splitted[0].equals("lecturer")){
					Time startTime = Time.valueOf(splitted[1]);
					Time endTime = Time.valueOf(splitted[2]);
					Date date = Date.valueOf(splitted[3]);
					String location = splitted[4];
					String link = splitted[5];
					
				}
			}
			// ... and have the server send it to all clients
			//server.sendToAll( message );
			}
		} catch( EOFException ie ) {
			// This doesn't need an error message
		} catch( IOException ie ) {
			// This does; tell the world!
			ie.printStackTrace();
		} finally {
			// The connection is closed for one reason or another,
			// so have the server dealing with it
			server.removeConnection(socket);
		}
	}
	
	private void insertIntoDatabase(Time start, Time end, Date date, String location, String link){
		try{
			String d = "', '"; //delimiter
			Connection con = DriverManager.getConnection("jdbc:myDriver:linksDB", username,password);
			Statement stmt = con.createStatement();
		    stmt.executeQuery("INSERT INTO linksTable (date, startTime, endTime, location, link) VALUES "+
			"('" + date + d + start + d + end + d + location + d + link + "')") ;
	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private String getLink(Date date, Time time, String location){
		String link = "The link";
		try{
			Connection con = DriverManager.getConnection("jdbc:myDriver:linksDB", username,password);
			Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery("SELECT link FROM linksTable WHERE date="+date+" AND startTime<"+time+" AND endTime>"+time+" AND location="+location);
			    while (rs.next()) {
			        link = rs.getString("link");
			    }
		}catch(Exception e){
			e.printStackTrace();
		}
		return link;
	}
}