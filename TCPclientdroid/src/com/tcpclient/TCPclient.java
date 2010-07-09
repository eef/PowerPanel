package com.tcpclient;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TCPclient extends ListActivity {
	private static final int ADD_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 3;
	private static final int CLOSE_ID = Menu.FIRST + 4;
	private static final String tag = null;
	private SQLiteDatabase db = null;
	private Cursor constantsCursor = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(tag, "==========1============");
		Log.d(tag, "==========2============");
		super.onCreate(savedInstanceState);
		WifiManager wifi = (WifiManager)getSystemService(WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		final Comms comm = new Comms(dhcp);
		try {
			comm.discover();
		} catch (Exception e) {
			Log.d("discover", "Discover: " + e.getMessage());
		}
		setContentView(R.layout.main);
		db = (new DatabaseHelper(this)).getWritableDatabase();
		constantsCursor = db.rawQuery("SELECT _ID, title, value "
				+ "FROM constants ORDER BY title", null);
		Log.d(tag, "==========3============");
		ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.row,
				constantsCursor, new String[] { "title", "value" }, new int[] {
						R.id.title, R.id.value });
		Log.d(tag, "==========4============");

		setListAdapter(adapter);
		registerForContextMenu(getListView());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		constantsCursor.close();
		db.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, ADD_ID, Menu.NONE, "Add").setIcon(R.drawable.add)
				.setAlphabeticShortcut('a');
		menu.add(Menu.NONE, CLOSE_ID, Menu.NONE, "Close").setIcon(
				R.drawable.eject).setAlphabeticShortcut('c');

		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_ID:
			add();
			return (true);

		case CLOSE_ID:
			finish();
			return (true);
		}

		return (super.onOptionsItemSelected(item));
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, "Delete").setIcon(
				R.drawable.delete).setAlphabeticShortcut('d');
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();

			delete(info.id);
			return (true);
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

	private void processAdd(DialogWrapper wrapper) {
		ContentValues values = new ContentValues(2);

		values.put("title", wrapper.getTitle());
		values.put("value", wrapper.getValue());

		db.insert("constants", "title", values);
		constantsCursor.requery();
	}

	private void processDelete(long rowId) {
		String[] args = { String.valueOf(rowId) };

		db.delete("constants", "_ID=?", args);
		constantsCursor.requery();
	}

	class DialogWrapper {
		EditText titleField = null;
		EditText valueField = null;
		View base = null;

		DialogWrapper(View base) {
			this.base = base;
			valueField = (EditText) base.findViewById(R.id.value);
		}

		String getTitle() {
			return (getTitleField().getText().toString());
		}

		float getValue() {
			return (new Float(getValueField().getText().toString())
					.floatValue());
		}

		private EditText getTitleField() {
			if (titleField == null) {
				titleField = (EditText) base.findViewById(R.id.title);
			}

			return (titleField);
		}

		private EditText getValueField() {
			if (valueField == null) {
				valueField = (EditText) base.findViewById(R.id.value);
			}

			return (valueField);
		}
	}
}