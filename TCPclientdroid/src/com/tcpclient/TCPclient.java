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
	
    @Override
    public void onCreate(Bundle icicle){
    	
        super.onCreate(icicle);        
        setContentView(R.layout.main);
        
        inputString = (EditText)findViewById(R.id.inp);
        hostname = (EditText)findViewById(R.id.hostname);
        status = (TextView)findViewById(R.id.status);
        sendButton = (Button)findViewById(R.id.send);
        
        sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {
					doSend(inputString.getText().toString(), hostname.getText().toString());
				} catch (Exception e) {
					status.setText(e.getMessage());
				}
			}
		});
        
    }
    
    public void doSend(String sentence, String hostname) throws Exception {
    	
        String modifiedSentence;
        BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
        Socket clientSocket;
        
		try {	
			status.setText("try loop");
			clientSocket = new Socket(hostname, 2087);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
	        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	        sentence = inFromUser.readLine();
	        outToServer.writeBytes(sentence + '\n');
	        modifiedSentence = inFromServer.readLine();
	        status.setText(modifiedSentence);
	        clientSocket.close();
		} catch (UnknownHostException e) {
			status.setText("Unknown host: " + e.getMessage());
		} catch (IOException e) {
			status.setText("IO Exception: " + e.getMessage());
		}
		
    }
}