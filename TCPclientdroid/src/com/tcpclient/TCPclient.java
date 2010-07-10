package com.tcpclient;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

public class TCPclient extends ListActivity {

	private String tag = "Main Activity ";
	private TextView selection;
	private String[] complist = mockComps();

	public static final int ADD_ID = Menu.FIRST + 1;
	public static final int EXIT_ID = Menu.FIRST + 2;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
		setListAdapter(new IconicAdapter());
		selection = (TextView) findViewById(R.id.selection);
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		Log.d(tag, complist[position]);
	}

	class IconicAdapter extends ArrayAdapter {
		Activity context;

		IconicAdapter() {
			super(TCPclient.this, R.layout.row, complist);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = context.getLayoutInflater();
			View row = inflater.inflate(R.layout.row, null);
			TextView label = (TextView) row.findViewById(R.id.label);

			String item = complist[position];

			try {
				JSONObject object = (JSONObject) new JSONTokener(item)
						.nextValue();
				label.setText(object.getString("name"));
				ImageView icon = (ImageView) row.findViewById(R.id.icon);
				if (object.getString("name").equals("ponline")) {
					icon.setImageResource(R.drawable.delete);
				} else if (object.getString("name").equals("offline")) {
					icon.setImageResource(R.drawable.ok);
				}
			} catch (JSONException e) {
				Log.e(tag, e.getMessage());
			}

			return (row);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		this.finish();
	}

	private String[] mockComps() {
		String[] mock_comp_list = {
				"{'name' : 'will', 'id':'2', 'status':'ponline'}",
				"{'name' : 'arthur', 'id':'1', 'status':'offline'}" };
		return mock_comp_list;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADD_ID, 0, "Add");
		menu.add(0, EXIT_ID, 0, "Exit");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_ID:

			return true;
		case EXIT_ID:
			exitApp();
			return true;
		}
		return false;
	}

	private void exitApp() {
		onStop();
	}

	private void makeAlert(String msg) {
		new AlertDialog.Builder(this)
				.setTitle("Exception")
				.setMessage(msg)
				.setNeutralButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dlg, int sumthin) {
								// Do nothing, it will close itself...hopefully
							}
						}).show();
	}

	private void makeToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}