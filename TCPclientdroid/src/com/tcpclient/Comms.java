package com.tcpclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.NetworkInterface;
import java.util.Enumeration;

import android.net.DhcpInfo;

public class Comms {
	String broadcaststr;
	public Comms(DhcpInfo dhcp) {
		
		try {	    	  
	    	   
	    	   
			    if (dhcp == null) {
			      //Log.d(TAG, "Could not get dhcp info");
			    }

			    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
			    byte[] quads = new byte[4];
			    for (int k = 0; k < 4; k++)
			      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
			    
			    broadcaststr = InetAddress.getByAddress(quads).toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

	}

	@SuppressWarnings("unused")
	private String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
		}
		return null;
	}

	

	public String doSend(String sentence) throws Exception {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress[] = InetAddress.getAllByName("192.168.0.255");
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress[0], 2501);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			String modifiedSentence = new String(receivePacket.getData());
			clientSocket.close();
			return modifiedSentence;
		} catch (UnknownHostException e) {
			return "Unknown host: " + e.getMessage();
		} catch (IOException e) {
			return "IO Exception: " + e.getMessage();
		}
	}

}
