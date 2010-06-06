package com.tcpclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;


public class TCPclient extends Activity{
	
	EditText inputString;
	EditText hostname;
	TextView status;
	Button sendButton;
	Button cancelButton;
	String res;
	
    @Override
    public void onCreate(Bundle icicle){
    	
        super.onCreate(icicle);        
        setContentView(R.layout.main);
        
        inputString = (EditText)findViewById(R.id.inp);
        status = (TextView)findViewById(R.id.status);
        sendButton = (Button)findViewById(R.id.send);
        cancelButton = (Button)findViewById(R.id.cancel_sh);
        final Comms comm = new Comms();
        
        cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {				
				try {
					res = comm.doSend("cancel");
					status.setText(res);
				} catch (Exception e) {
					status.setText(e.getCause().toString());
				}
			}
		});
        
        sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {
					res = comm.doSend(inputString.getText().toString());
					status.setText(res);
				} catch (Exception e) {
					status.setText(e.getCause().toString());
				}
			}
		});
        
    }
    
//    public void doSend(String sentence) throws Exception {
//        try {
//            DatagramSocket clientSocket = new DatagramSocket();
//            InetAddress IPAddress[] = InetAddress.getAllByName("192.168.0.255");
//            byte[] sendData = new byte[1024];
//            byte[] receiveData = new byte[1024];
//            status.setText("about Sent data");
//            sendData = sentence.getBytes();
//            status.setText("Sent data");
//            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress[0], 2501);
//            clientSocket.send(sendPacket);
//            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//            clientSocket.receive(receivePacket);
//            String modifiedSentence = new String(receivePacket.getData());
//            status.setText(modifiedSentence);
//            clientSocket.close();
//        } catch (UnknownHostException e) {
//			status.setText("Unknown host: " + e.getMessage());
//		} catch (IOException e) {
//			status.setText("IO Exception: " + e.getMessage());
//		}
//    }
}