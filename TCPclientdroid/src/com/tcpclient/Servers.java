package com.tcpclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class Servers {
	
	private int PORT = 2501;
	private InetAddress broadcastIP;
	private List<Server> serverList = new ArrayList<Server>();
	private int nextID;
	
	public Servers(WifiManager wifi) {

		try {
			DhcpInfo dhcp = wifi.getDhcpInfo();
			if (dhcp == null) {
				Log.d("shit", "Could not get dhcp info");
			}
			int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
			byte[] quads = new byte[4];
			for (int k = 0; k < 4; k++)
				quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
			broadcastIP = InetAddress.getByAddress(quads);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addToServerList(InetAddress newServerIP){
		
		Server bogla = new Server(nextID, newServerIP);
		nextID += 1;		
		serverList.add(bogla);
	}
	
	public String getServerInfo(InetAddress[] serverIPs){	
		
		
		
	}
	
	public void discover() throws Exception {	
		
		if (broadcastIP != null) {
			Log.e("discovery", "shit the bed..");
		}

		try {
			DatagramSocket clientSocket = new DatagramSocket();
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			sendData = "hello".getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, broadcastIP, 2501);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);

			clientSocket.setSoTimeout(5000); // sets how long reicive() blocks
												// for once there are no new
												// packates
			long t = System.currentTimeMillis();
			long end = t + 3000;
			while (System.currentTimeMillis() < end) {
				clientSocket.receive(receivePacket);
				
				//iplist.add(receivePacket.getAddress());
				
				addToServerList(receivePacket.getAddress());
				
				Log.e("discovery", (receivePacket.getAddress().toString()));
				Thread.sleep(500);
				// modifiedSentence = modifiedSentence +
				// receivePacket.getAddress().toString();
				//Log.e("discovery1", String.valueOf(iplist.size()));
			}
			clientSocket.close();

		} catch (UnknownHostException e) {
			Log.e("recieved:", "UnknownHostException:" + e.toString());

		} catch (IOException e) {
			Log.e("recieved:", "IOException:" + e.toString());
		}
	}
	
	public boolean pair() {		
		return false;		
	}
	
	public boolean pair(Server server) {
		try {
			String reply = doSend("pair", server.serverIP);
			JSONObject object = (JSONObject) new JSONTokener(reply).nextValue();
			if (object.getString("paired").equals("yes")){
				//TODO: create setters/getters?
				server.mac = object.getString("mac");
				server.pKey = object.getString("pkey");
				//call 'sync' method
				return true;
			}
			return false;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (UnknownHostException ue) {
		 	return false;
		} catch (Exception euz) {
			return false;
		}	
	}
	
	public String doSend(String command, InetAddress ip) throws Exception {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = ip;
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			sendData = command.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, PORT);
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

}
