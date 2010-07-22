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
	private int serverID = -1;
	String pKey, mac, hostname, name = null;
	String status = "online";
	InetAddress serverIP = null;
	

	public Server(InetAddress serverIP) {
		this.serverID = serverID;
		this.serverIP = serverIP;
		this.setHostName();
		this.setName("");
	}

	public Server(int serverID, InetAddress serverIP) {		
		this.serverID = serverID;
		this.serverIP = serverIP;
		this.setHostName();
		this.setName(""); 
	}

	public Server(int serverID, InetAddress serverIP, String pKey,
			String hostname) {
		this.serverIP = serverIP;
		this.pKey = pKey;
		this.hostname = hostname;
		this.setHostName();
		this.setName("");
	}

	public boolean isPaired() {
		if (status.equals("ponline"))
			return true;
		return false;
	}
//"{'name' : 'will', 'id':'2', 'status':'ponline'}",
	public JSONObject getInfo() throws JSONException {
		JSONObject object = new JSONObject();
		if (serverID != -1)
			object.put("id", Integer.toString(serverID));
		if (pKey != null)
			object.put("pKey", pKey);
		if (mac != null)
			object.put("mac", mac);
		if (hostname != null)
			object.put("hostname", hostname);
		if (name != null)
			object.put("name", name);
		if (status != null)
			object.put("status", status);
		
		return object;
	}

	public void setServerID(int serverID) {
		this.serverID = serverID;
	}

	public int getServerID() {
		return this.serverID;
	}

	public void setMAC(String string) {
		this.mac = string; 		
	}
	
	public String getMAC() {
		return this.mac; 		
	}
	public void setPKey(String string) {
		this.pKey = string; 			
	}
	
	public String getPKey() {
		return this.pKey; 			
	}

	public void setHostName() {		
		if (serverIP != null)
			this.hostname = serverIP.getHostName();
	}
	
	public void setName(String name) {
		this.name = name;
		if (name.equals("")) {
			this.name = serverIP.getHostName();
		} else {
			this.name = name;
		}
	}
	
	public void setStatus(String string) {
		this.status = string;
		
	}

	public InetAddress getServerIP() {
		return serverIP;
	}

}
