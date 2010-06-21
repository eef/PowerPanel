package com.tcpclient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

public class TCPclient extends ListActivity {
	
	String res;
	TextView selection;
	HashMap<String, String> storedComps;
	String[] comps = {"amber", "arthur"};
	
	public static final int ADD_ID = Menu.FIRST + 1;
	public static final int EXIT_ID = Menu.FIRST + 2;

	@Override
	public void onCreate(Bundle icicle) {
		
		WifiManager wifi = (WifiManager)getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		final Comms comm = new Comms(dhcp);
		
		super.onCreate(icicle);
		setContentView(R.layout.main);
		
		storedComps = storedComps();
		
		comps = buildCompList(storedComps);
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.row, R.id.label, comps));
		
		selection=(TextView)findViewById(R.id.selection);
		
		selection.setText(comm.broadcaststr.toString());
		
	}
	
	public void onListItemClick(ListView parent, View v, int position, long id) {
		
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
	
	private String[] buildCompList(HashMap<String, String> sc) {
		 Iterator it = mp.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        System.out.println(pairs.getKey() + " = " + pairs.getValue());
		    }
	}
	
	private Map<String, String> storedComps() {
		// Mocking the database at the moment
		Map<String, String> compInfos = new HashMap<String, String>();
		compInfos.put("Amber", "192.168.0.105");
		compInfos.put("Arthur", "192.168.0.103");
		return compInfos;
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