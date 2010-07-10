package com.tcpclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class Server {
	int serverID = -1;
	String pkey,mac,servername = null;
	InetAddress serverIP = null;
	
	public Server(InetAddress serverIP) {
		this.serverIP = serverIP;
		String pkey,mac,servername = null;
		
	}

	public Server(InetAddress serverIP, String pkey, String servername){		
		this.serverIP = serverIP;
		this.pkey = pkey;			
		}
	

	public String doSend(String command) {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			Log.d("broadcast", "1");
			// InetAddress IPAddress =
			// InetAddress.getByName("broadcastIP.toString());
			Log.d("broadcast", "2");
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			sendData = command.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, serverIP, 2501);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			String modifiedSentence = new String(receivePacket.getData());
			clientSocket.close();
			return modifiedSentence.trim();
		} catch (UnknownHostException e) {
			return "Unknown host: " + e.getMessage();
		} catch (IOException e) {
			return "IO Exception: " + e.getMessage();
		}
	}

	public String pair() {
		try {
			String reply = doSend("pair");
			JSONObject object = (JSONObject) new JSONTokener(reply).nextValue();
			this.mac = object.getString("mac");
			this.pkey = object.getString("pkey");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return pkey;
	}

	public boolean authenticate(InetAddress ip, String pkey) {
		/*if ((ip.getHostAddress().length() > 0) && (pkey.length() > 0)) {
			authenticated = true;
		}
		return authenticated;*/
		return false;
	}
}
