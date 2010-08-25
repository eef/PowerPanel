package com.wellbaked.powerpanel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FirstStep extends Activity {

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firststep);

        Button next = (Button) findViewById(R.id.first_next);
        TextView textView = (TextView) findViewById(R.id.support_link);
        Linkify.addLinks(textView, Linkify.ALL);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	Intent myIntent = new Intent(view.getContext(), SecondStep.class);
                startActivity(myIntent);
            }

        });
    }
}