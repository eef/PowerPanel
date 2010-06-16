package com.tcpclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

public class TCPclient extends Activity {

	EditText inputString;
	EditText hostname;
	TextView status;
	Button sendButton;
	Button cancelButton;
	String res;
	public static final int ADD_ID = Menu.FIRST + 1;
	public static final int EXIT_ID = Menu.FIRST + 2;

//	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle icicle) {
		
		WifiManager wifi = (WifiManager)getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();

		super.onCreate(icicle);
		setContentView(R.layout.main);

		sendButton = (Button) findViewById(R.id.send);
		cancelButton = (Button) findViewById(R.id.cancel_sh);
		final Comms comm = new Comms(dhcp);
		makeAlert(comm.broadcaststr);

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

	@Override
	protected void onStop() {
		super.onStop();
		this.finish();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADD_ID, 0, "Add");
		menu.add(0, EXIT_ID, 0, "Exit");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_ID:
			addComputer();
			return true;
		case EXIT_ID:
			exitApp();
			return true;
		}
		return false;
	}

	private void makeToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private void exitApp() {
		onStop();
	}

	private void addComputer() {
		makeToast("Would add PC");
	}

	private void makeAlert(String msg) {
		new AlertDialog.Builder(this).setTitle("Exception").setMessage(msg)
				.setNeutralButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dlg, int sumthin) {
								// Do nothing, it will close itself...hopefully
							}
						}).show();
	}
}