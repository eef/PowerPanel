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

import android.content.Context;
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
	private List<Server> displayList = new ArrayList<Server>();
	private String status = new String();
	public int ipCount;
	public boolean isSaved;
	public String savedName;
	public int compsOnline = 0;
	public int compsOffline = 0;
	public int compsPaired = 0;
	DataHelper database;

	public Servers(Context context) {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		database = new DataHelper(context);
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
		try {
			Log.d(tag, "trying dicover");
			discover();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addToServerList(InetAddress newServerIP, String pKey) {
		boolean server = getServer(newServerIP.toString());
		Server bogla = new Server(nextID, newServerIP);
		Log.d("Addtoserverlist", pKey);
		String name = database.isSaved(pKey);
		if (name.length() > 0) {
			isSaved = true;
			savedName = name;
			Log.d("isSaved", "Yes");
		} else {
			isSaved = false;
			Log.d("isSaved", "No");
		}
		nextID += 1;
		if(isSaved) {
			Log.d("setting status to ponline", "Yes");
			bogla.setStatus("ponline");
			bogla.setName(savedName);
			bogla.setPKey(pKey);
			serverList.add(bogla);
		} else {
			Log.d("setting status to online", "Yes");
			bogla.setStatus("online");
			bogla.setPKey(pKey);
			serverList.add(bogla);
		}
		if (!server) {
			Log.d("not in server list", "no");
			displayList.add(bogla);
		}
	}

	public String wakeUp(int serverID){
		Log.d("####3 server ID:",Integer.toString(serverID));
		final int WOLPORT = 9;    
		Server wolserver = getServer(serverID);
		        //String ipStr = wolserver.getServerIP().toString();
		        String macStr = wolserver.getMAC();
		        
		        try {
		        	
		            byte[] macBytes = getMacBytes(macStr);
		            byte[] bytes = new byte[6 + 16 * macBytes.length];
		            for (int i = 0; i < 6; i++) {
		                bytes[i] = (byte) 0xff;
		            }
		            for (int i = 6; i < bytes.length; i += macBytes.length) {
		                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
		            }
		            Log.d("####PORT",Integer.toString(WOLPORT));
		            InetAddress address = wolserver.getServerIP();
		            Log.d("####IP",wolserver.getServerIP().toString());
		            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, WOLPORT);
		            DatagramSocket socket = new DatagramSocket();
		            socket.send(packet);
		            socket.close();
		            return "success";		            
		        }
		        catch (Exception e) {
		        	Log.d("####","failed");
		        	return "failed";
		        }
	}

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
	
	private boolean getServer(String serverIP) {
		have = false;
		Iterator<Server> server = displayList.iterator();
		while (server.hasNext()) {
			Server current_server = server.next();
			if (current_server.getServerIP().getHostAddress().equals(
					serverIP.replace("/", ""))) {
				have = true;
			}
		}
		return have;
	}

	public void setServername(int serverID, String servername) {
		getServer(serverID).setName(servername);

	}

	public List<String> getServerInfo() {
		Iterator<Server> server = displayList.iterator();
		ipCount = serverList.size();
		while (server.hasNext()) {
			Server current_server = server.next();
			try {
				String current_server_status = checkStatus(current_server);
				current_server.setStatus(current_server_status);
				serverInfo.add(current_server.getInfo().toString());
				x++;
			} catch (JSONException e) {
				Log.d(tag, e.getMessage());
			}
		}
		return serverInfo;
	}

	private String checkStatus(Server current_server) {
		status = "offline";
		Iterator<Server> servers = serverList.iterator();
		if (serverList.size() > 0) {
			while (servers.hasNext()) {
				Server current_server_list = servers.next();
				if (current_server.hostname
						.equals(current_server_list.hostname)) {
					status = "online";
					Log.d(tag, "Device [" + current_server.hostname
							+ "] is online");
					current_server_list.setServerID(current_server
							.getServerID());
					String name = database.isSaved(current_server.pKey);
					if (name.length() > 0) {
						status = "ponline";
					} else {
						status = "online";
					}

				}
			}
		} else {
			Log.d(tag, "Device [" + current_server.hostname + "] is offline");
			status = "offline";
		}
		return status;
	}

	public void discover() throws Exception {
		serverList.clear();
		InetAddress dest = InetAddress.getByName(broadcastIP.getHostAddress());
		//InetAddress dest = InetAddress.getByName("192.168.0.255");
		if (broadcastIP == null) {
			Log.e(tag, "shit the bed...no broadcast");
		}

		try {
			DatagramSocket clientSocket = new DatagramSocket();
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			sendData = "hello:0".getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, dest, 2501);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);

			clientSocket.setSoTimeout(1000); // sets how long reicive() blocks
			long t = System.currentTimeMillis();
			long end = t + 3000;
			while (System.currentTimeMillis() < end) {
				clientSocket.receive(receivePacket);
				String modifiedSentence = new String(receivePacket.getData());
				Log.e("Discover packet", modifiedSentence.trim());
				addToServerList(receivePacket.getAddress(), modifiedSentence.trim());
				Log.e(tag, "Discovered server: "
						+ (receivePacket.getAddress().toString()));
				Thread.sleep(500);
			}
			clientSocket.close();

		} catch (UnknownHostException e) {
			Log.e("recieved:", "UnknownHostException:" + e.toString());

		} catch (IOException e) {
			Log.e("recieved:", "IOException:" + e.toString());
		}
	}

	public boolean pair(int serverID) {
		server = getServer(serverID);
		Log.d(tag, "id: " + Integer.toString(serverID));
		Log.d(tag, "2");
		boolean paired = false;
		try {
			if (server.isPaired()) {
				Log.d(tag, "paired");
				paired = true;
			} else {
				String reply = doSend("pair:0", server);
				JSONObject object = (JSONObject) new JSONTokener(reply)
						.nextValue();
				if (object.getString("pairaccepted").equals("yes")) {
					// TODO: create setters/getters?
					server.setMAC(object.getString("mac"));
					server.setPKey(object.getString("pkey"));
					server.setName(object.getString("name"));
					server.setStatus("ponline");
					database.insert(object.getString("name"), object.getString("pkey"), "none", object.getString("mac"));
					// call 'sync' method
					paired = true;
				}
			}
			return paired;

		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (Exception euz) {
			return false;
		}
	}

	public String shutdown(int serverID, String seconds) {
		return doSend("shutdown:" + seconds, getServer(serverID));
	}
	
	public String hibernate(int serverID) {
		return doSend("hibernate:0", getServer(serverID));
	}
	
	public String reboot(int serverID) {
		return doSend("reboot:0", getServer(serverID));
	}
	
	public String getCompsOnline() {
		return Integer.toString(compsOnline);
	}

	public String cancelShutdown(int serverID) {
		return doSend("cancel:0", getServer(serverID));
	}

	private Server getServer(int serverID) {
		Iterator<Server> servers = displayList.iterator();
		if (servers.hasNext()) {
			Log.d(tag, "getServer has next");
		}
		while (servers.hasNext()) {
			Server current_server = servers.next();
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
			clientSocket.setSoTimeout(10000);
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
