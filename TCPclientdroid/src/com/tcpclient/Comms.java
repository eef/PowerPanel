package com.tcpclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Comms {
	public static void main(String[] args) {

	}

	@SuppressWarnings("unchecked")
	public List discover() throws Exception {
		List str = new ArrayList();
		for(Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
	        NetworkInterface iface = ifaces.nextElement();
	        for(Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses.hasMoreElements();) {
	            InetAddress address = addresses.nextElement();
            	if(!address.getHostAddress().startsWith("10.")) {
            		if(!address.getHostAddress().startsWith("127.0")) {
            			str.add(address.getHostAddress());	
            		}
            	}
	        }
	    }
		return str;
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
