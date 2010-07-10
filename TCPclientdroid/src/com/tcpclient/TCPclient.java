package com.tcpclient;

import java.net.InetAddress;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TCPclient extends ListActivity {
	private static final int ADD_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;
	private static final int SHUTDOWN_ID = Menu.FIRST + 3;
	private static final int CANCEL_ID = Menu.FIRST + 5;
	private static final int REFRESH_ID = Menu.FIRST + 4;
	private static final String tag = "Main Activity";
	private SQLiteDatabase db = null;
	private Cursor constantsCursor = null;
	public String addresses;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		final Comms comm = new Comms(dhcp);
		try {
			comm.discover();
			Log.d(tag + "1", "1");
			addresses = comm.showServerAddresses();
			Log.d(tag + "2", "2");
		} catch (Exception e) {
			Log.d("Discover: ", e.getMessage());
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		db = (new DatabaseHelper(this)).getWritableDatabase();
		constantsCursor = db.rawQuery("SELECT _ID, name, last_ip "
				+ "FROM computers WHERE name in (" + addresses
				+ ") ORDER BY name", null);
		ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.row,
				constantsCursor, new String[] { "name", "last_ip" }, new int[] {
						R.id.name, R.id.last_ip_dis });

		setListAdapter(adapter);
		registerForContextMenu(getListView());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		constantsCursor.close();
		db.close();
	}

	public void refreshComps() {
		WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		final Comms comm = new Comms(dhcp);
		try {
			comm.discover();
			addresses = comm.showServerAddresses();
		} catch (Exception e) {
			Log.d(tag + " Discover: ", e.getMessage());
		}
		
		db = (new DatabaseHelper(this)).getWritableDatabase();
		constantsCursor = db.rawQuery("SELECT _ID, name, last_ip "
				+ "FROM computers WHERE name in ("+ addresses +") ORDER BY name", null);
		ListAdapter refresh_list = (new SimpleCursorAdapter(this, R.layout.row,
				constantsCursor, new String[] { "name", "last_ip" }, new int[] {
				R.id.name, R.id.last_ip_dis }));
		setListAdapter(refresh_list);
		registerForContextMenu(getListView());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, ADD_ID, Menu.NONE, "Add")
				.setAlphabeticShortcut('a');
		menu.add(Menu.NONE, REFRESH_ID, Menu.NONE, "Refresh")
				.setAlphabeticShortcut('c');

		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_ID:
			add();
			return (true);

		case REFRESH_ID:
			refreshComps();
			return (true);
		}

		return (super.onOptionsItemSelected(item));
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

			delete(info.id);
			break;
		case SHUTDOWN_ID:
			AdapterView.AdapterContextMenuInfo info1 = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();

			shutdown(info1.id);
			break;
		case CANCEL_ID:
			AdapterView.AdapterContextMenuInfo info3 = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();

			cancel(info3.id);
			break;
		}

		return (super.onOptionsItemSelected(item));
	}

	private void add() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View addView = inflater.inflate(R.layout.add_edit, null);
		final DialogWrapper wrapper = new DialogWrapper(addView);

		new AlertDialog.Builder(this).setTitle(R.string.add_title).setView(
				addView).setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						processAdd(wrapper);
					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// ignore, just dismiss
					}
				}).show();
	}

	private void delete(final long rowId) {
		if (rowId > 0) {
			new AlertDialog.Builder(this).setTitle(R.string.delete_title)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									processDelete(rowId);
								}
							}).setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).show();
		}
	}

	private void shutdown(final long rowId) {
		if (rowId > 0) {
			new AlertDialog.Builder(this).setTitle(R.string.shutdown_title)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									processShutdown(rowId);
								}
							}).setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).show();
		}
	}

	private void cancel(final long rowId) {
		if (rowId > 0) {
			new AlertDialog.Builder(this).setTitle(R.string.cancel_title)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									processCancel(rowId);
								}
							}).setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).show();
		}
	}

	private void processAdd(DialogWrapper wrapper) {
		ContentValues values = new ContentValues(2);

		values.put("name", wrapper.getTitle());
		values.put("last_ip", wrapper.getValue());

		db.insert("computers", "name", values);
		constantsCursor.requery();
	}

	private void processDelete(long rowId) {
		String[] args = { String.valueOf(rowId) };

		db.delete("computers", "_ID=?", args);
		constantsCursor.requery();
	}

	private void processShutdown(long rowId) {
		String[] args = { String.valueOf(rowId) };

		constantsCursor = db.rawQuery("SELECT * "
				+ "FROM computers WHERE _ID =?", args);
		if (constantsCursor.moveToFirst())
			Log.d("name", constantsCursor.getString(constantsCursor
					.getColumnIndex("name")));
		Log.d("last_ip", constantsCursor.getString(constantsCursor
				.getColumnIndex("last_ip")));
		try {
			WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
			DhcpInfo dhcp = wifi.getDhcpInfo();
			final Comms comm = new Comms(dhcp);
			comm.doSend("shutdown", constantsCursor.getString(constantsCursor
					.getColumnIndex("last_ip")));
		} catch (Exception e) {
			Log.d("Shutdown send", e.getMessage());
		}
		constantsCursor.close();
	}

	private void processCancel(long rowId) {
		String[] args = { String.valueOf(rowId) };

		constantsCursor = db.rawQuery("SELECT * "
				+ "FROM computers WHERE _ID =?", args);
		if (constantsCursor.moveToFirst())
			Log.d("name", constantsCursor.getString(constantsCursor
					.getColumnIndex("name")));
		Log.d("last_ip", constantsCursor.getString(constantsCursor
				.getColumnIndex("last_ip")));
		try {
			WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
			DhcpInfo dhcp = wifi.getDhcpInfo();
			final Comms comm = new Comms(dhcp);
			comm.doSend("cancel", constantsCursor.getString(constantsCursor
					.getColumnIndex("last_ip")));
		} catch (Exception e) {
			Log.d("Shutdown send", e.getMessage());
		}
		constantsCursor.close();
	}

	class DialogWrapper {
		EditText titleField = null;
		EditText valueField = null;
		View base = null;

		DialogWrapper(View base) {
			this.base = base;
			valueField = (EditText) base.findViewById(R.id.last_ip);
		}

		String getTitle() {
			return (getTitleField().getText().toString());
		}

		String getValue() {
			return (getValueField().getText().toString());
		}

		private EditText getTitleField() {
			if (titleField == null) {
				titleField = (EditText) base.findViewById(R.id.name);
			}

			return (titleField);
		}

		private EditText getValueField() {
			if (valueField == null) {
				valueField = (EditText) base.findViewById(R.id.last_ip);
			}

			return (valueField);
		}
	}
}