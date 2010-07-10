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
	String pKey, mac, hostname = null;
	InetAddress serverIP = null;

	public Server(InetAddress serverIP) {
		this.serverID = serverID;
		this.serverIP = serverIP;
	}

	public Server(int serverID, InetAddress serverIP) {
		this.serverID = serverID;
		this.serverIP = serverIP;
	}

	public Server(int serverID, InetAddress serverIP, String pKey,
			String hostname) {
		this.serverIP = serverIP;
		this.pKey = pKey;
		this.hostname = hostname;
	}

	public boolean isPaired() {
		if (pKey == null)
			return false;
		return true;
	}

	public JSONObject getInfo() throws JSONException {
		JSONObject object = new JSONObject();
		if (pKey == null)
			object.put("pKey", pKey);
		if (mac == null)
			object.put("mac", mac);
		if (hostname == null)
			object.put("hostname", hostname);
		if (serverIP == null)
			object.put("serverIP", serverIP);
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

	public void setPKey(String string) {
		this.pKey = string; 			
	}

}
