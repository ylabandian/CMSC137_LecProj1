import java.net.*;
import java.util.*;
import java.io.*;

public class Server implements Runnable{
	byte[] buffer = new byte[1024];
	DatagramSocket socket;
	int ws = 4; //WINDOW SIZE
	String toPrint = "";
	Thread t = new Thread(this);
	String oldsyn = "";
	 // pd=0;
	int[] sent = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	public static void main(String[] args){
		new Server();
	}

	public Server(){
		try{
			socket = new DatagramSocket(8080);
			System.out.println("The server has started!");
			// this.address = InetAddress.getByName(server);
			this.t.start();
		}catch(Exception e){
			System.out.println("Error in creating server.");
		}
	}

	public void send(String msg){
		try{
			buffer = msg.getBytes();
			InetAddress address = InetAddress.getByName("127.0.0.1");
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 9090);
			socket.send(packet);
		}catch(Exception e){
			System.out.println("Error in sending packet.");
		}
	}

	public String packetDrop(String[] values){
		Random rand = new Random();
		int syn = rand.nextInt(4) + 0;
		int[] flag = {1, 1, 1, 1};
		
		int x, min=0, check=0;
		String newSyn = "";
		String drp = "";
		String[] print;
		boolean plural = true;

		switch(syn){
			case 0:
				newSyn = Integer.toString(Integer.parseInt(oldsyn) + 4);
				drp = "--";
				break;
			case 1:		// 25%	-- one packet will be dropped
				x = rand.nextInt(3) + 0;
				flag[x] = 0;
				break;

			case 2:		// 50%	-- two packets dropped (this should be randomized)
				flag[2] = 0;
				flag[3] = 0;
				
				break;
			case 3:		// 75%	-- three packets dropped
				flag[0] = 0;
				flag[1] = 0;
				flag[2] = 0;
				plural = false;
				
				break;
		}


		for(int i=0; i<ws; i++){
			if(flag[i] == 1){
				if(sent[Integer.parseInt(values[i])] == 0 || min == 1){
					sent[Integer.parseInt(values[i])] = 1;
				}
			}else{
				if(min==0){
					newSyn = values[i];
					min = 1;
				}
				drp += values[i] + " "; 
			}
		}

		if(plural){
			System.out.println("Packets dropped: " + drp);
		}else{
			System.out.println("Packet dropped: " + drp);
		}

		System.out.print("Received: ");
		for(int i=0; i<10; i++){
			if(sent[i] == 1) System.out.print(i+" ");
		}
		System.out.print("\n");

		for(int i=0; i<10; i++){
			if(sent[i] == 1) check++;
		}
		if(check == 10){		//if all of the sent flags are 1
			return "9y";
		}

		return newSyn;
	}

	public void run(){
		try{
			while(true){
				byte[] buffer = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

				socket.receive(packet);

				String data = new String(packet.getData());

				if(data.contains("SYN")){
					System.out.println("Client sent SYN.");					//client is requesting connection
					System.out.println("Server sending SYN-ACK...");	//server acknowledges SYN
					Thread.sleep(2000);
					send("SYNACK");
				}else if(data.contains("ACK")){
					System.out.println("Client sent ACK.");					//client is requesting connection
					System.out.println("\nClient is now connected to the server.");
					send("ACK/0");
					System.out.println("SYN: 0");	
				}else if(data.contains("BYE")){
					System.out.println("Client has disconnected.");
					socket.close();
					break;
				}else if(data.length() > 3){
					String[] values = data.split(" ");
					String received = "";

					String syn = packetDrop(values);
					oldsyn = syn;
					System.out.println("\nSYN: "+syn.charAt(0));

					send("ACK/"+syn);
				}
			}
		}catch(Exception e){}
	}
}
