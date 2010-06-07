package com.tcpclient;

import java.util.List;

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
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle icicle){
    	
        super.onCreate(icicle);        
        setContentView(R.layout.main);
        
        inputString = (EditText)findViewById(R.id.inp);
        status = (TextView)findViewById(R.id.status);
        sendButton = (Button)findViewById(R.id.send);
        cancelButton = (Button)findViewById(R.id.cancel_sh);
        final Comms comm = new Comms();
        
        try {
			List ifaces = comm.discover();
			if(ifaces.isEmpty()) {
				status.setText("No ips detected");
			} else {
				status.setText(ifaces.toString());
			}
		} catch (Exception e1) {
			status.setText(e1.getCause().toString());
		}
        
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
}