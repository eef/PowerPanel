package com.tcpclient;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TCPclient extends ListActivity {

	private String tag = "Main Activity ";
	private List<String> complist = new ArrayList<String>();
	public static final int REFRESH_ID = Menu.FIRST + 1;
	public static final int CLEARDB_ID = Menu.FIRST + 2;

	private static final int PAIR_ID = Menu.FIRST + 3;
	private static final int SHUTDOWN_ID = Menu.FIRST + 4;
	private static final int CANCEL_ID = Menu.FIRST + 5;
	private static final int HIBERNATE_ID = Menu.FIRST + 6;
	private static final int REBOOT_ID = Menu.FIRST + 7;
	private int id = 0;
	Servers serversobject;
	Context thisContext = this;
	private String status;
	public TextView hour_label;
	public TextView mins_label;
	public String shutdownSecs;

	@Override
	public void onCreate(Bundle icicle) {
		Log.d(tag, "created server object");
		super.onCreate(icicle);
		setContentView(R.layout.main);
		new Construct().execute();
	}

	public void listSetup() {
		complist = serversobject.getServerInfo();
		setListAdapter(new IconicAdapter());
		registerForContextMenu(getListView());
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		String item = complist.get(position);
		try {
			JSONObject object = (JSONObject) new JSONTokener(item).nextValue();
			makeToast("Pairing: " + object.getString("name"), false);
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
		menu.add(Menu.NONE, HIBERNATE_ID, Menu.NONE, "Hibernate")
				.setAlphabeticShortcut('b');
		menu.add(Menu.NONE, REBOOT_ID, Menu.NONE, "Reboot")
				.setAlphabeticShortcut('c');
		menu.add(Menu.NONE, CANCEL_ID, Menu.NONE, "Cancel")
				.setAlphabeticShortcut('d');
		menu.add(Menu.NONE, PAIR_ID, Menu.NONE, "Pair").setAlphabeticShortcut(
				'e');
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case PAIR_ID:
			pairReq(info.position);
			break;
		case SHUTDOWN_ID:
			shutdown(info.position);
			break;
		case HIBERNATE_ID:
			hibernate(info.position);
			break;
		case REBOOT_ID:
			reboot(info.position);
			break;
		case CANCEL_ID:
			cancel(info.position);
			break;
		}
		return (super.onOptionsItemSelected(item));
	}
	
	private void clearDatabase() {
		DataHelper database = new DataHelper(this);
		database.deleteAll();
	}

	private void pairReq(int comp) {
		String item = complist.get(comp);
		try {
			JSONObject object = (JSONObject) new JSONTokener(item).nextValue();
			id = object.getInt("id");
		} catch (JSONException e) {
			Log.e(tag, e.getMessage());
		}
		makeToast("Pairing...", false);
		new Pair().execute();
	}
	
	private void shutdown(int idd) {
		id = idd;
		LayoutInflater inflater=LayoutInflater.from(this);
		View addView=inflater.inflate(R.layout.add_edit, null);
		final DialogWrapper wrapper=new DialogWrapper(addView);
		new AlertDialog.Builder(this)
			.setTitle(R.string.add_title)
			.setView(addView)
			.setPositiveButton(R.string.ok,
													new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
															int whichButton) {
					processShutdown(id, wrapper);
				}
			})
			.setNegativeButton(R.string.cancel,
													new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
															int whichButton) {
					// ignore, just dismiss
				}
			})
			.show();
	}
	
	class DialogWrapper {
		View base=null;
		
		DialogWrapper(View base) {
			this.base=base;
			hour_label = (TextView)this.base.findViewById(R.id.hour_label);
			Button add_hour_button =(Button)this.base.findViewById(R.id.add_hour_button);
			add_hour_button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Integer hour_value = Integer.parseInt(hour_label.getText().toString());
					hour_value++;
					hour_label.setText(Integer.toString(hour_value));
				}
			});
			Button minus_hour_button =(Button)this.base.findViewById(R.id.minus_hour_button);
			minus_hour_button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Integer hour_value = Integer.parseInt(hour_label.getText().toString());
					if(hour_value != 0) {
						hour_value--;
						hour_label.setText(Integer.toString(hour_value));
					}
				}
			});
			
			mins_label = (TextView)this.base.findViewById(R.id.min_label);
			Button add_min_button =(Button)this.base.findViewById(R.id.add_min_button);
			add_min_button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Integer min_value = Integer.parseInt(mins_label.getText().toString());
					min_value++;
					mins_label.setText(Integer.toString(min_value));
				}
			});
			Button minus_min_button =(Button)this.base.findViewById(R.id.minus_min_button);
			minus_min_button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Integer min_value = Integer.parseInt(mins_label.getText().toString());
					if(min_value != 0) {
						min_value--;
						mins_label.setText(Integer.toString(min_value));
					}
				}
			});
		}
		
		int getMins() {
			return Integer.parseInt(mins_label.getText().toString());
		}
		
		int getHours() {
			return Integer.parseInt(hour_label.getText().toString());
		}
		
		public String getSeconds() {
			int mins = this.getMins();
			int hours = this.getHours();
			int hoursInMins = 0;
			int totalSeconds = 0;
			if(hours > 0) {
				hoursInMins = hours * 60;
			}
			if(mins > 0 || hoursInMins > 0) {
				totalSeconds = (mins + hoursInMins) * 60;
			}
			return Integer.toString(totalSeconds);
		}
	}

	private void processShutdown(int idd, DialogWrapper wrapper) {
		id = idd;
		shutdownSecs = wrapper.getSeconds();
		makeToast("Shutting down..", false);
		new Shutdown().execute();
	}
	
	private void processReboot(int idd) {
		id = idd;
		makeToast("Rebooting..", false);
		new Reboot().execute();
	}

	private void processHibernate(int idd) {
		id = idd;
		makeToast("Hibernating..", false);
		new Hibernate().execute();
	}

	private void processCancel(int idd) {
		id = idd;
		makeToast("Cancelling shutdown", false);
		new CancelShutdown().execute();
	}
	
	private void reboot(int comp) {
		String item = complist.get(comp);
		try {
			JSONObject object = (JSONObject) new JSONTokener(item).nextValue();
			id = object.getInt("id");
		} catch (JSONException e) {
			Log.e(tag, e.getMessage());
		}
		if (id >= 0) {
			new AlertDialog.Builder(this).setTitle(R.string.reboot)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									processReboot(id);
								}
							}).setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).show();
		}
	}

	private void hibernate(int comp) {
		String item = complist.get(comp);
		try {
			JSONObject object = (JSONObject) new JSONTokener(item).nextValue();
			id = object.getInt("id");
		} catch (JSONException e) {
			Log.e(tag, e.getMessage());
		}
		if (id >= 0) {
			new AlertDialog.Builder(this).setTitle(R.string.hibernate)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									processHibernate(id);
								}
							}).setNegativeButton(R.string.cancel,
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
			new AlertDialog.Builder(this).setTitle(R.string.cancel_shutdown)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									processCancel(id);
								}
							}).setNegativeButton(R.string.cancel,
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
				Log.d("SHOW NAME", object.getString("name"));
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
				makeToast(e.getMessage(), true);
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
		menu.add(0, REFRESH_ID, 0, "Refresh").setIcon(R.drawable.refresh);
		//menu.add(0, CLEARDB_ID, 0, "Clear Database").setIcon(R.drawable.refresh);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case REFRESH_ID:
			makeToast("Refreshing servers...", false);
			new RefreshList().execute();
			return true;
		case CLEARDB_ID:
			clearDatabase();
			makeToast("Clearing database...", false);
			new RefreshList().execute();
			return true;
		}
		return false;
	}

	private void exitApp() {
		onStop();
	}

	private void makeAlert(String msg, String title) {
		new AlertDialog.Builder(this).setTitle(title).setMessage(msg)
				.setNeutralButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dlg, int sumthin) {
								// Do nothing, it will close itself...hopefully
							}
						}).show();
	}

	private void makeToast(String msg, Boolean len) {
		int length;
		if (len) {
			length = Toast.LENGTH_LONG;
		} else {
			length = Toast.LENGTH_SHORT;
		}
		Toast.makeText(this, msg, length).show();
	}

	class Construct extends AsyncTask<Void, String, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			serversobject = new Servers(thisContext);
			return (null);
		}

		protected void onProgressUpdate(Void... unused) {
			Toast.makeText(thisContext, "Still working...", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		protected void onPostExecute(Void unused) {
			listSetup();
		}
	}

	class RefreshList extends AsyncTask<Void, String, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			refreshIPs();
			return (null);
		}

		protected void onProgressUpdate(Void... unused) {
			Toast.makeText(thisContext, "Still working...", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		protected void onPostExecute(Void unused) {
			refreshList();

		}
	}

	class Pair extends AsyncTask<Void, String, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			serversobject.pair(id);
			return (null);
		}

		protected void onProgressUpdate(Void... unused) {
			Toast.makeText(thisContext, "Still working...", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		protected void onPostExecute(Void unused) {
			Toast.makeText(thisContext, "Paired", Toast.LENGTH_SHORT).show();
			refreshIPs();
			refreshList();
		}
	}

	class Shutdown extends AsyncTask<Void, Integer, Void> {

		protected Void doInBackground(Void... unused) {
			status = serversobject.shutdown(id, shutdownSecs);
			return (null);
		}

		protected void onProgressUpdate(Void... unused) {
			Toast.makeText(thisContext, "Still working...", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		protected void onPostExecute(Void unused) {
			refreshList();
			Toast.makeText(thisContext, status,
					Toast.LENGTH_SHORT).show();
		}
	}
	
	class Reboot extends AsyncTask<Void, Integer, Void> {

		protected Void doInBackground(Void... unused) {
			status = serversobject.reboot(id);
			return (null);
		}

		protected void onProgressUpdate(Void... unused) {
			Toast.makeText(thisContext, "Still working...", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		protected void onPostExecute(Void unused) {
			refreshList();
			Toast.makeText(thisContext, status,
					Toast.LENGTH_SHORT).show();
		}
	}

	class Hibernate extends AsyncTask<Void, Integer, Void> {

		protected Void doInBackground(Void... unused) {
			status = serversobject.hibernate(id);
			return (null);
		}

		protected void onProgressUpdate(Void... unused) {
			Toast.makeText(thisContext, "Still working...", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		protected void onPostExecute(Void unused) {
			refreshList();
			Toast.makeText(thisContext, status,
					Toast.LENGTH_SHORT).show();
		}
	}

	class CancelShutdown extends AsyncTask<Void, Integer, Void> {

		protected Void doInBackground(Void... unused) {
			status = serversobject.cancelShutdown(id);
			return (null);
		}

		protected void onProgressUpdate(Void... unused) {
			Toast.makeText(thisContext, "Still working...", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		protected void onPostExecute(Void unused) {
			refreshList();
			Toast.makeText(thisContext, status,
					Toast.LENGTH_SHORT).show();
		}
	}

}