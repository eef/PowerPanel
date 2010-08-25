package com.wellbaked.powerpanel;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PowerPanel extends ListActivity {

	private String tag = "Main Activity ";
	private List<String> complist = new ArrayList<String>();
	public static final int REFRESH_ID = Menu.FIRST + 1;
	public static final int CLEARDB_ID = Menu.FIRST + 2;
	
	private int id = 0;
	Servers serversobject;
	Context thisContext = this;
	private String status;
	public TextView hour_label;
	public TextView mins_label;
	public TextView status_label;
	public String shutdownSecs;
	public Integer test = 0;
	Button btn1;
	public static final String PREFS_NAME = "ppprefs";
	SharedPreferences settings;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		settings = getSharedPreferences(PREFS_NAME, 0);
		String first = settings.getString("instruct", null);
		if (first == null) {
			Intent myIntent = new Intent(thisContext, FirstStep.class);
	        startActivityForResult(myIntent, 0);
		} else {
			btn1 = (Button) this.findViewById(R.id.Button01);
			btn1.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					makeToast("Refreshing servers...", false);
					new RefreshList().execute();
				}
			});
			makeToast("Discovering servers...", true);
			new Construct().execute();
		}
	}

	public void listSetup() {
		complist = serversobject.getServerInfo();
		setListAdapter(new IconicAdapter());
		registerForContextMenu(getListView());
	}

	public void onListItemClick(ListView parent, View v, final int position,
			long id) {
		String item = complist.get(position);
		final Server current_server = serversobject.getServer(position);
		final ActionItem qa_pair = new ActionItem();
		final ActionItem qa_shutdown = new ActionItem();
		final ActionItem qa_cancel_shutdown = new ActionItem();
		final ActionItem qa_restart = new ActionItem();
		final ActionItem qa_hibernate = new ActionItem();
		final ActionItem qa_wol = new ActionItem();
		final QuickAction qa = new QuickAction(v);

		if (!current_server.isPaired()) {
			qa_pair.setTitle("Pair");
			qa_pair.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					pairReq(position);
					qa.dismiss();
				}
			});
			qa.addActionItem(qa_pair);
		}

		if (current_server.isPaired()) {
			if (!current_server.getIsShuttingDown()) {
				qa_shutdown.setTitle("Shutdown");
				qa_shutdown.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						shutdown(position);
						current_server.setIsShuttingDown(true);
						qa.dismiss();
					} 
				});
				qa.addActionItem(qa_shutdown);
			}

			if (current_server.getIsShuttingDown()) {
				qa_cancel_shutdown.setTitle("Cancel shutdown");
				qa_cancel_shutdown.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						cancel(position);
						current_server.setIsShuttingDown(false);
						qa.dismiss();
					}
				});
				qa.addActionItem(qa_cancel_shutdown);
			}
			qa_restart.setTitle("Reboot");
			qa_restart.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					reboot(position);
					qa.dismiss();
				}
			});
			qa.addActionItem(qa_restart);

			qa_hibernate.setTitle("Hibernate");
			qa_hibernate.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					hibernate(position);
					qa.dismiss();
				}
			});
			qa.addActionItem(qa_hibernate);
		}

		qa.setAnimStyle(QuickAction.ANIM_AUTO);
		qa.show();
	}

	private void refreshList() {
		complist.clear();
		complist = serversobject.getServerInfo();
		setListAdapter(new IconicAdapter());
		registerForContextMenu(getListView());
	}

	private void wake(int comp) {
		// TODO Auto-generated method stub
		String item = complist.get(comp);

		try {
			JSONObject object = (JSONObject) new JSONTokener(item).nextValue();
			id = object.getInt("id");
		} catch (JSONException e) {
			Log.e(tag, e.getMessage());
		}
		if (id >= 0) {
			new AlertDialog.Builder(this).setTitle(R.string.wake_up)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									processWakeUP(id);
								}
							}).setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).show();
		}

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
		LayoutInflater inflater = LayoutInflater.from(this);
		View addView = inflater.inflate(R.layout.add_edit, null);
		final DialogWrapper wrapper = new DialogWrapper(addView);
		new AlertDialog.Builder(this).setTitle(R.string.add_title).setView(
				addView).setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						processShutdown(id, wrapper);
					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// ignore, just dismiss
					}
				}).show();
	}

	class DialogWrapper {
		View base = null;

		DialogWrapper(View base) {
			this.base = base;
			hour_label = (TextView) this.base.findViewById(R.id.hour_label);
			Button add_hour_button = (Button) this.base
					.findViewById(R.id.add_hour_button);
			add_hour_button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Integer hour_value = Integer.parseInt(hour_label.getText()
							.toString());
					hour_value++;
					hour_label.setText(Integer.toString(hour_value));
				}
			});
			Button minus_hour_button = (Button) this.base
					.findViewById(R.id.minus_hour_button);
			minus_hour_button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Integer hour_value = Integer.parseInt(hour_label.getText()
							.toString());
					if (hour_value != 0) {
						hour_value--;
						hour_label.setText(Integer.toString(hour_value));
					}
				}
			});

			mins_label = (TextView) this.base.findViewById(R.id.min_label);
			Button add_min_button = (Button) this.base
					.findViewById(R.id.add_min_button);
			add_min_button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Integer min_value = Integer.parseInt(mins_label.getText()
							.toString());
					min_value++;
					mins_label.setText(Integer.toString(min_value));
				}
			});
			Button minus_min_button = (Button) this.base
					.findViewById(R.id.minus_min_button);
			minus_min_button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Integer min_value = Integer.parseInt(mins_label.getText()
							.toString());
					if (min_value != 0) {
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
			if (hours > 0) {
				hoursInMins = hours * 60;
			}
			if (mins > 0 || hoursInMins > 0) {
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

	private void processWakeUP(int idd) {
		id = idd;
		makeToast("Sending WOL packet", false);
		new SendWOL().execute();
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
			super(PowerPanel.this, R.layout.row, complist);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.row, null);
			TextView label = (TextView) row.findViewById(R.id.label);
			TextView status_label = (TextView) row
					.findViewById(R.id.status_label);
			String item = complist.get(position);

			try {
				JSONObject object = (JSONObject) new JSONTokener(item)
						.nextValue();
				label.setText(object.getString("name"));
				// ImageView icon = (ImageView) row.findViewById(R.id.icon);
				if (object.getString("status").equals("ponline")) {
					row.setBackgroundResource(R.color.ponline);
					// icon.setImageResource(R.drawable.ponline);
					status_label.setText("Paired");
				} else if (object.getString("status").equals("offline")) {
					// icon.setImageResource(R.drawable.offline);
					row.setBackgroundResource(R.color.offline);
					status_label.setText("Offline");
				} else if (object.getString("status").equals("online")) {
					// icon.setImageResource(R.drawable.online);
					row.setBackgroundResource(R.color.online);
					status_label.setText("Not paired");
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
		menu.add(0, REFRESH_ID, 0, "Refresh");
		menu.add(0, CLEARDB_ID, 0, "Help");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case REFRESH_ID:
			makeToast("Refreshing servers...", false);
			new RefreshList().execute();
			return true;
		case CLEARDB_ID:
			Intent myIntent = new Intent(thisContext, FirstStep.class);
	        startActivityForResult(myIntent, 0);
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
			Toast.makeText(thisContext, status, Toast.LENGTH_SHORT).show();
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
			Toast.makeText(thisContext, status, Toast.LENGTH_SHORT).show();
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
			Toast.makeText(thisContext, status, Toast.LENGTH_SHORT).show();
		}
	}

	class SendWOL extends AsyncTask<Void, Integer, Void> {
		protected Void doInBackground(Void... unused) {
			status = serversobject.wakeUp(id);
			return (null);
		}

		protected void onProgressUpdate(Void... unused) {
			Toast.makeText(thisContext, "Still working...", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		protected void onPostExecute(Void unused) {
			refreshList();
			Toast.makeText(thisContext, status, Toast.LENGTH_SHORT).show();
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
			Toast.makeText(thisContext, status, Toast.LENGTH_SHORT).show();
		}
	}
	private boolean first_time_check() {
	    String first = settings.getString("first", null);
	    SharedPreferences.Editor editor = settings.edit();
	    if((first == null)){
	    	makeAlert("Welcome to PowerPanel.  Please download the desktop application from:\n\nhttp://www.wellbaked.net", "Welcome!");
	    	editor.putString("first", "yes");
	        editor.commit();
	        return false;
	    }
	    else 
	        return true;
	}
}