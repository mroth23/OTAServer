import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
	private ServerSocket ss;
	private Hashtable outputStreams = new Hashtable();
	private int keyNumber;
	
	public Server( int port ) throws IOException {
		listen( port );
	}
	
	private void listen( int port ) throws IOException {
		ss = new ServerSocket( port );
		System.out.println("Listening on "+ ss);
		while(true){
			Socket s = ss.accept();
			System.out.println("Connection from "+s);
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			dout.writeUTF(String.valueOf(keyNumber));
			outputStreams.put(s, dout);
			//Inform the others of the new connection
			new ServerThread(this, s);
		}
	}

	
	Enumeration getOutputStreams(){
		return outputStreams.elements();
	}
	
	void sendToAll(String message){
		//System.out.println("Recieved: "+message);
		synchronized(outputStreams){
			for(Enumeration e = getOutputStreams(); e.hasMoreElements();){
				DataOutputStream dout = (DataOutputStream)e.nextElement();
				try{
					dout.writeUTF(message);
				}catch(IOException ie){
					ie.printStackTrace();
				}
			}
		}
	}
	
	
	void removeConnection(Socket s){
		synchronized(outputStreams){
			outputStreams.remove(s);
			try{
				s.close();
			} catch(IOException ie){
				ie.printStackTrace();
			}
		}
	}
	
	static public void main(String args[]) throws Exception{
		int port = 4444;
		new Server(port);
	}
	
}