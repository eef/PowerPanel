package com.tcpclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class Servers {
	private String tag = "Servers.java class";

	private int PORT = 2501;
	boolean have = false;
	int x = 0;
	private InetAddress broadcastIP;
	private List<Server> serverList = new ArrayList<Server>();
	private List<String> serverInfo = new ArrayList<String>();
	private int nextID;
	private Iterator<Integer> serverID = null;
	private Server server = null;
	private List<Server> oldServerList = new ArrayList<Server>();
	private String status = new String();

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

	public void addToServerList(InetAddress newServerIP) {
		boolean server = getServer(newServerIP.toString());
		Server bogla = new Server(nextID, newServerIP);
		nextID += 1;
		serverList.add(bogla);
		if(!server) {
			Log.d(tag, "arg2");
			oldServerList.add(bogla);
		} else {
			Log.d(tag, "arg");
		}
	}
	
	public void checkForOffline() {
		Log.d(tag, "Starting checkoffline");
		List<String> serverInfo = this.serverInfo;
		Log.d(tag, "checkoffline serverList size: " + Integer.toString(this.serverList.size()));
		Log.d(tag, "checkoffline oldServerList size: " + Integer.toString(this.oldServerList.size()));
		Iterator<String> serverInfoIt = serverInfo.iterator();
		while(serverInfoIt.hasNext()) {
			Log.d(tag, "Starting checkoffline iterator");
			String current_si = serverInfoIt.next().toString();
			String serverIP = null;
			int serverID = 0;
			try {
				JSONObject object = (JSONObject) new JSONTokener(current_si)
				.nextValue();
				serverIP = object.getString("hostname");
				serverID = object.getInt("id");
				Log.d(tag, "checkoffline " + serverIP);
				boolean offline = getServer(serverIP);
				if (!offline) {
					Server server = getServer(serverID);
					server.setStatus("offline");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(tag, "++++++++++++++++++++++++++++++");
			Log.d(tag, current_si);
		}
	}
	
	private boolean getServer(String serverIP) {
		Log.d(tag, "onrefresh serverList size: " + Integer.toString(this.serverList.size()));
		Log.d(tag, "onrefresh oldServerList size: " + Integer.toString(this.oldServerList.size()));
		have = false;
		Iterator<Server> server = oldServerList.iterator();
		while (server.hasNext()) {
			Server current_server = server.next();
			Log.d(tag, "serverIP: " + serverIP.replace("/", "") + ":" + current_server.getServerIP().getHostAddress());
			if (current_server.getServerIP().getHostAddress().equals(serverIP.replace("/", ""))) {
				Log.d(tag, "online " + current_server.getServerIP().getHostAddress());
				have = true;
			}
		}
		if(have == false) {
			Log.d(tag, "offline");
		}
		return have;
	}

	public void setServername(int serverID, String servername) {
		getServer(serverID).setName(servername);

	}

	public List<String> getServerInfo() {
		Log.d(tag, "starting getServerInfo()");
		Log.d(tag, "set int");
		Iterator<Server> server = oldServerList.iterator();
		Log.d(tag, "created iterator");
		while (server.hasNext()) {
			Server current_server = server.next();
			Log.d(tag, "getServerInfo serverList size: " + Integer.toString(this.serverList.size()));
			Log.d(tag, "getServerInfo oldServerList size: " + Integer.toString(this.oldServerList.size()));
			Log.d(tag, "while getServerInfo()");
			try {
				String current_server_status = checkStatus(current_server);
				Log.d(tag, "Returned status: " + current_server_status);
				current_server.setStatus(current_server_status);
				serverInfo.add(current_server.getInfo().toString());
				x++;
			} catch (JSONException e) {
				Log.d(tag, e.getMessage());
			}
		}
		Log.d(tag, "serverinfo serverList size: " + Integer.toString(this.serverList.size()));
		Log.d(tag, "serverinfo oldServerList size: " + Integer.toString(this.oldServerList.size()));
		return serverInfo;
	}
	
	private String checkStatus(Server current_server) {
		Log.d(tag, "checkStatus serverList size: " + Integer.toString(this.serverList.size()));
		Log.d(tag, "checkStatus oldServerList size: " + Integer.toString(this.oldServerList.size()));
		Iterator<Server> servers = serverList.iterator();
		if(serverList.size() > 0) {
			while(servers.hasNext()) {
				Server current_server_list = servers.next();
				Log.d(tag, "current_server checkstatus if " + current_server.hostname);
				Log.d(tag, "Hostname checkstatus if " + current_server_list.hostname);
				if(current_server.hostname.equals(current_server_list.hostname)) {
					Log.d(tag, "current_server.getServerID() " + Integer.toString(current_server.getServerID()));
					Log.d(tag, "current_server_list.getServerID() " + Integer.toString(current_server_list.getServerID()));
					current_server_list.setServerID(current_server.getServerID());
					Log.d(tag, "current_server.getServerID() after set " + Integer.toString(current_server.getServerID()));
					Log.d(tag, "current_server_list.getServerID() after set " + Integer.toString(current_server_list.getServerID()));
					if(current_server.equals(current_server_list)) {
						Log.d(tag, "current_server.equals(current_server_list)");
					}
					Log.d(tag, "Hostnames match it is online");
					if(current_server.isPaired()) {
						Log.d(tag, "Device is paired with " + current_server.hostname);
						status = "ponline";
					} else {
						Log.d(tag, "Server ["+current_server.hostname+"] is online but not paired");
						status = "online";
					}
				}
			}
		} else {
			Log.d(tag, "Server ["+current_server.hostname+"] is offline");
			status = "offline";
		}
		return status;
	}
	

	public void discover() throws Exception {
		serverList.clear();
		Log.d(tag, "starting discover()");
		InetAddress broadcastIP = InetAddress.getByName("192.168.0.255");
		Log.d(tag, "created inetaddy with broadcast");
		if (broadcastIP == null) {
			Log.e("discovery", "shit the bed..");
		}

		try {
			Log.d(tag, "trying discovery");
			DatagramSocket clientSocket = new DatagramSocket();
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			sendData = "hello".getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, broadcastIP, 2501);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);

			clientSocket.setSoTimeout(1000); // sets how long reicive() blocks
			// for once there are no new
			// packates
			long t = System.currentTimeMillis();
			long end = t + 3000;
			while (System.currentTimeMillis() < end) {
				clientSocket.receive(receivePacket);

				// iplist.add(receivePacket.getAddress());

				addToServerList(receivePacket.getAddress());

				Log.e("discovery", (receivePacket.getAddress().toString()));
				Thread.sleep(500);
				// modifiedSentence = modifiedSentence +
				// receivePacket.getAddress().toString();
				// Log.e("discovery1", String.valueOf(iplist.size()));
			}
			clientSocket.close();

		} catch (UnknownHostException e) {
			Log.e("recieved:", "UnknownHostException:" + e.toString());

		} catch (IOException e) {
			Log.e("recieved:", "IOException:" + e.toString());
		}
	}

	public boolean pair(int serverID) {
		// TODO: make success return proper info
		server = getServer(serverID);
		Log.d(tag, "id: " + Integer.toString(serverID));
		Log.d(tag, "2");
		boolean paired = false;
		try {
			Log.d(tag, "teeee");
			Log.d(tag, "iterate");
			Log.d(tag, "iterate" + server.serverIP);
			if (server.isPaired()) {
				Log.d(tag, "paired");
				paired = true;
			} else {
				String reply = doSend("pair", server);
				JSONObject object = (JSONObject) new JSONTokener(reply)
						.nextValue();
				if (object.getString("pairaccepted").equals("yes")) {
					// TODO: create setters/getters?
					server.setMAC(object.getString("mac"));
					server.setPKey(object.getString("pkey"));
					server.setStatus("ponline");
					// call 'sync' method
					paired = true;
				}
			}
			Log.d(tag, server.getInfo().get("pKey").toString());
			return paired;

		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (Exception euz) {
			return false;
		}
	}

	public boolean shutdown(int serverID) {
		doSend("shutdown", getServer(serverID));
		return false;
	}

	public boolean cancelShutdown(int serverID) {
		doSend("cancel", getServer(serverID));
		return false;
	}
	
	private Server getServer(int serverID) {
		Log.d(tag, "serverinfo serverList size: " + Integer.toString(this.serverList.size()));
		Log.d(tag, "serverinfo oldServerList size: " + Integer.toString(this.oldServerList.size()));
		Iterator<Server> servers = oldServerList.iterator();
		if(servers.hasNext()) {
			Log.d(tag, "getServer has next");
		}
		while (servers.hasNext()) {
			Server current_server = servers.next();
			Log.d(tag, "Current server getServer() hostname " + current_server.hostname);
			Log.d(tag, "Current server getServer() serverID " + current_server.getServerID());
			if (current_server.getServerID() == serverID) {
				return current_server;
			}
		}
		return null;
	}

	public String doSend(String command, Server server) {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = server.getServerIP();
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
