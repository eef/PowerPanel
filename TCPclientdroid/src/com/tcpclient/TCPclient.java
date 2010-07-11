package com.tcpclient;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
	private List<String> complist = new ArrayList<String>();
	private List<String> test = new ArrayList<String>();
	public static final int ADD_ID = Menu.FIRST + 1;
	public static final int EXIT_ID = Menu.FIRST + 2;

	private static final int PAIR_ID = Menu.FIRST + 2;
	private static final int SHUTDOWN_ID = Menu.FIRST + 3;
	private static final int CANCEL_ID = Menu.FIRST + 5;
	private int id = 0;
	Servers serversobject = null;

	@Override
	public void onCreate(Bundle icicle) {
		serversobject = new Servers(
				(WifiManager) getSystemService(Context.WIFI_SERVICE));
		Log.d(tag, "created server object");
		try {
			Log.d(tag, "trying dicover");
			serversobject.discover();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onCreate(icicle);
		setContentView(R.layout.main);
		complist = serversobject.getServerInfo();
		setListAdapter(new IconicAdapter());
		registerForContextMenu(getListView());
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		String item = complist.get(position);
		try {
			JSONObject object = (JSONObject) new JSONTokener(item).nextValue();
			makeToast("Pairing: " + object.getString("name"));
			pairReq(position);
		} catch (JSONException e) {
			Log.e(tag, "failed to serialize select item");
		}

	}

	private void refreshList() {
		complist.clear();
		complist = serversobject.getServerInfo();
		setListAdapter(new IconicAdapter());
		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, SHUTDOWN_ID, Menu.NONE, "Shutdown")
				.setAlphabeticShortcut('a');
		menu.add(Menu.NONE, CANCEL_ID, Menu.NONE, "Cancel")
				.setAlphabeticShortcut('b');
		menu.add(Menu.NONE, PAIR_ID, Menu.NONE, "Pair").setAlphabeticShortcut(
				'c');
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case PAIR_ID:
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();
			pairReq(info.position);
			break;
		case SHUTDOWN_ID:
			AdapterView.AdapterContextMenuInfo info1 = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();
			shutdown(info1.position);
			break;
		case CANCEL_ID:
			AdapterView.AdapterContextMenuInfo info3 = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();

			cancel(info3.position);
			break;
		}

		return (super.onOptionsItemSelected(item));
	}

	private void pairReq(int comp) {
		String item = complist.get(comp);
		try {
			JSONObject object = (JSONObject) new JSONTokener(item).nextValue();
			id = object.getInt("id");
		} catch (JSONException e) {
			Log.e(tag, e.getMessage());
		}
		serversobject.pair(id);
		makeToast("Server has been paired");
		refreshIPs();
		refreshList();
	}

	private void processShutdown(int id) {
		serversobject.shutdown(id);
		makeToast("Shutdown ok");
	}

	private void processCancel(int id) {
		serversobject.cancelShutdown(id);
		makeToast("Cancel ok");
	}

	private void shutdown(int comp) {
		String item = complist.get(comp);
		try {
			JSONObject object = (JSONObject) new JSONTokener(item).nextValue();
			id = object.getInt("id");
		} catch (JSONException e) {
			Log.e(tag, e.getMessage());
		}
		if (id >= 0) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.shutdown)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									processShutdown(id);
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).show();
		}
	}

	private void refreshIPs() {
		complist.clear();
		try {
			Log.d(tag, "trying dicover");
			serversobject.discover();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void cancel(int comp) {
		String item = complist.get(comp);
		try {
			JSONObject object = (JSONObject) new JSONTokener(item).nextValue();
			id = object.getInt("id");
		} catch (JSONException e) {
			Log.e(tag, e.getMessage());
		}
		if (id >= 0) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.cancel_shutdown)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									processCancel(id);
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).show();
		}
	}

	class IconicAdapter extends ArrayAdapter {
		IconicAdapter() {
			super(TCPclient.this, R.layout.row, complist);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.row, null);
			TextView label = (TextView) row.findViewById(R.id.label);
			String item = complist.get(position);

			try {
				JSONObject object = (JSONObject) new JSONTokener(item)
						.nextValue();
				label.setText(object.getString("name"));
				ImageView icon = (ImageView) row.findViewById(R.id.icon);
				if (object.getString("status").equals("ponline")) {
					icon.setImageResource(R.drawable.ponline);
				} else if (object.getString("status").equals("offline")) {
					icon.setImageResource(R.drawable.offline);
				} else if (object.getString("status").equals("online")) {
					icon.setImageResource(R.drawable.online);
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

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADD_ID, 0, "Refresh");
		menu.add(0, EXIT_ID, 0, "Exit");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_ID:
			refreshIPs();
			refreshList();
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