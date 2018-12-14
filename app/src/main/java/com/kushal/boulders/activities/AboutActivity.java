package com.kushal.boulders.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.kushal.boulders.R;
import com.kushal.boulders.utils.storage.ImageStorage;


public class AboutActivity extends AppCompatActivity {

    public static Intent createIntent(Activity activity) {
        return new Intent(activity, AboutActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        //"all the steps will come here "
        TextView textView =(TextView)findViewById(R.id.aboutHyperLink);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='http://www.iamkushal.tumblr.com/android/boulders'> iamkushal.tumblr.com/android/boulders </a>";
        textView.setText(Html.fromHtml(text));


    }

}
