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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int SHUTDOWN_ID = Menu.FIRST + 3;
	private static final int CANCEL_ID = Menu.FIRST + 5;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
		setListAdapter(new IconicAdapter());
		registerForContextMenu(getListView());
		selection = (TextView) findViewById(R.id.selection);
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		Log.d(tag, complist[position]);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, SHUTDOWN_ID, Menu.NONE, "Shutdown")
				.setAlphabeticShortcut('a');
		menu.add(Menu.NONE, CANCEL_ID, Menu.NONE, "Cancel")
				.setAlphabeticShortcut('b');
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, "Delete")
				.setAlphabeticShortcut('c');
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();

			makeToast("Delete");
			break;
		case SHUTDOWN_ID:
			AdapterView.AdapterContextMenuInfo info1 = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();

			makeToast("Shutdown");
			break;
		case CANCEL_ID:
			AdapterView.AdapterContextMenuInfo info3 = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();

			makeToast("Cancel " + info3.id);
			break;
		}

		return (super.onOptionsItemSelected(item));
	}

	class IconicAdapter extends ArrayAdapter {
		IconicAdapter() {
			super(TCPclient.this, R.layout.row, complist);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.row, null);
			TextView label = (TextView) row.findViewById(R.id.label);

			String item = complist[position];

			try {
				JSONObject object = (JSONObject) new JSONTokener(item)
						.nextValue();
				label.setText(object.getString("name"));
				ImageView icon = (ImageView) row.findViewById(R.id.icon);
				if (object.getString("status").equals("ponline")) {
					icon.setImageResource(R.drawable.delete);
				} else if (object.getString("status").equals("offline")) {
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
				"{'name' : 'arthur', 'id':'1', 'status':'offline'}",
				"{'name' : 'luke', 'id':'3', 'status':'online'}",
				"{'name' : 'yer da', 'id':'4', 'status':'online'}"};
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