package com.tcpclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import java.io.*;
import java.net.*;

public class TCPclient extends Activity{
	
	EditText inputString;
	EditText hostname;
	TextView status;
	Button sendButton;
	Button cancelButton;
	
    @Override
    public void onCreate(Bundle icicle){
    	
        super.onCreate(icicle);        
        setContentView(R.layout.main);
        
        inputString = (EditText)findViewById(R.id.inp);
        hostname = (EditText)findViewById(R.id.hostname);
        status = (TextView)findViewById(R.id.status);
        sendButton = (Button)findViewById(R.id.send);
        cancelButton = (Button)findViewById(R.id.cancel_sh);
        
        cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {				
				try {
					doSend("cancel", hostname.getText().toString());
				} catch (Exception e) {
					status.setText(e.getCause().toString());
				}
			}
		});
        
        sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {
					doSend(inputString.getText().toString(), hostname.getText().toString());
				} catch (Exception e) {
					status.setText(e.getCause().toString());
				}
			}
		});
        
    }
    
    public void doSend(String sentence, String hostname) throws Exception {
        try {
        	System.out.println("Got into method");
        	System.out.println("Got into method");
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("192.168.0.255");
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];
            status.setText("about Sent data");
            sendData = sentence.getBytes();
            status.setText("Sent data");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 2501);
            clientSocket.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String modifiedSentence = new String(receivePacket.getData());
            status.setText("FROM SERVER:" + modifiedSentence);
            clientSocket.close();
        } catch (UnknownHostException e) {
			status.setText("Unknown host: " + e.getMessage());
		} catch (IOException e) {
			status.setText("IO Exception: " + e.getMessage());
		}
        
//		try {	
//			status.setText("try loop");
//			clientSocket = new Socket(hostname, 2501);
//			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
//	        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//	        //sentence = inFromUser.readLine();
//	        outToServer.writeBytes(sentence);
//	        modifiedSentence = inFromServer.readLine();
//	        status.setText(modifiedSentence);
//	        clientSocket.close();
//		} catch (UnknownHostException e) {
//			status.setText("Unknown host: " + e.getMessage());
//		} catch (IOException e) {
//			status.setText("IO Exception: " + e.getMessage());
//		}
//		
    }
}