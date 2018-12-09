package com.kushal.boulders.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NotificationBar extends Activity {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Creating a new RelativeLayout
            RelativeLayout relativeLayout = new RelativeLayout(this);

            // Defining the RelativeLayout layout parameters.
            // In this case I want to fill its parent
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.FILL_PARENT,
                    RelativeLayout.LayoutParams.FILL_PARENT);

            // Creating a new TextView
            TextView tv = new TextView(this);
            tv.setText("Test");

            // Defining the layout parameters of the TextView
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);

            // Setting the parameters on the TextView
            tv.setLayoutParams(lp);

            // Adding the TextView to the RelativeLayout as a child
            relativeLayout.addView(tv);

            // Setting the RelativeLayout as our content view
            setContentView(relativeLayout, rlp);
        }

}
