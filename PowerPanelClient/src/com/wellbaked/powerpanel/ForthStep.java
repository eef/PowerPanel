package com.wellbaked.powerpanel;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ForthStep extends Activity {
	
	SharedPreferences settings;
	public static final String PREFS_NAME = "ppprefs";

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stepfour);
        TextView textView = (TextView) findViewById(R.id.support_link);
        Linkify.addLinks(textView, Linkify.ALL);
        Button next = (Button) findViewById(R.id.four_next);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	settings = getSharedPreferences(PREFS_NAME, 0);
        	    SharedPreferences.Editor editor = settings.edit();
    	    	editor.putString("instruct", "yes");
    	        editor.commit();
            	Intent myIntent = new Intent(view.getContext(), PowerPanel.class);
            	startActivityForResult(myIntent, 0);
            }

        });
    }
}