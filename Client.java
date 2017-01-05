//In some cases, not all data can be send correctly, because of packet dropping...

import java.net.*;
import java.util.*;
import java.io.*;

public class Client implements Runnable{
	Thread t = new Thread(this);
	DatagramSocket socket;
	InetAddress address;

	boolean connected = false;
	boolean all = false;
	byte[] buffer = new byte[1024];
	String toSend = "";
	int ws = 4; //WINDOW SIZE
	int ctr = 0;
	int[] message = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}; // the client message is a list from 0 to 9

	public static void main(String[] args){
		new Client();
	}

	public Client(){
		try{
			System.out.println("Connecting to server...");
			socket = new DatagramSocket(9090);
			t.start();
		}catch(Exception e){
			System.out.println("Error in creating instance.");
			e.printStackTrace();
		}
	}

	public void send(String msg){
		try{
			buffer = msg.getBytes();
			InetAddress address = InetAddress.getByName("127.0.0.1");
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 8080);
			
			socket.send(packet);
		}catch(Exception e){
			System.out.println("Error in sending packet.");
		}
	}

	public void disconnect(){
		try{
			System.out.println("All data has been sent. Disconnecting from server...");
			Thread.sleep(5000);
			send("BYE");
			System.out.println("Connection to the server has been ended.");
		}catch(Exception e){
			System.out.println("Error in sending packet.");
		}
	}

	public void run(){
		try{
			while(true){
				byte[] incomingData = new byte[256];
				DatagramPacket packet = new DatagramPacket(incomingData, incomingData.length);

				if(!connected){	
					System.out.println("\nSending SYN bit...");
					Thread.sleep(2000);
					send("SYN");
					connected = true;
				}else{
					socket.receive(packet);

					String data = new String(incomingData);

					if(data.contains("SYNACK")){
						System.out.println("\nServer sent SYN-ACK.");			// server has acknowledged SYN
						System.out.println("Sending ACK bit...");			
						Thread.sleep(2000);
						System.out.println("\nClient is now connected to the server.");
						send("ACK");
					}else{
						if(data.contains("ACK/")){
							int synNum = data.charAt(4) - '0';
							
							System.out.println("SYN:"+ synNum);

							toSend = "";


							if(Character.toString(data.charAt(5)).equals("y")){
								disconnect();
								break;
							}
							else{
								if(synNum+ws > 10){ //if out of bounds
									for(int i=synNum; i<10; i++){
										toSend += message[i] + " ";
									}
									synNum = 10 - synNum;
									for(int i=0; i<synNum; i++){
										toSend += "0 ";				//THE ZEROES ARE JUST DUMMIES :(
									}
								}else{
									for(int i=synNum; i<synNum+ws; i++){
										toSend += message[i] + " ";
									}
								}
								
								System.out.println("Sending "+toSend+"...\n");
								Thread.sleep(2000);
								send(toSend);
							}
						}
					}
				}			
			}
		}catch(Exception e){}
	}
}
