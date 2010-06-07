package com.tcpclient;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;


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
        
        sendButton = (Button)findViewById(R.id.send);
        cancelButton = (Button)findViewById(R.id.cancel_sh);
        final Comms comm = new Comms();
        
        try {
			List ifaces = comm.discover();
			if(ifaces.isEmpty()) {
				makeAlert("No interfaces found");
			} else {
				makeToast(ifaces.toString());
			}
		} catch (Exception dis_ex) {
			makeAlert(dis_ex.getCause().toString());
		}
        
        cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {				
				try {
					res = comm.doSend("cancel");
					makeToast(res.trim());
				} catch (Exception e) {
					makeAlert(e.getCause().toString());
				}
			}
		});
        
        sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {
					res = comm.doSend("shutdown");
					makeToast(res.trim());
				} catch (Exception e) {
					makeAlert(e.getCause().toString());
				}
			}
		});
        
    }
    
    private void makeToast(String msg) {
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
    private void makeAlert(String msg) {
    	new AlertDialog.Builder(this).setTitle("Exception").setMessage(msg).setNeutralButton("Close", new DialogInterface.OnClickListener(){
    		public void onClick(DialogInterface dlg, int sumthin) {
    			// Do nothing, it will close itself...hopefully
    		}
    	}).show();
    }
}