package com.cfbrownweb.whatgenre;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class GenreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String genre = getIntent().getStringExtra("genre");
        TextView prediction = (TextView) findViewById(R.id.predicted_genre);
        if (genre != null){
            prediction.setText(genre);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
