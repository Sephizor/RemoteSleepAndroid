package com.sephizor.remotesleep;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences preferences;
    private String ip;
    private int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupButtons();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        ip = preferences.getString("ip_address", "192.168.0.0");
        port = Integer.parseInt(preferences.getString("port", "15000"));
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupButtons() {
        final Button sleepButton = (Button) findViewById(R.id.sleepButton);
        final Button shutdownButton = (Button) findViewById(R.id.shutdownButton);
        final Button hibernateButton = (Button) findViewById(R.id.hibernateButton);

        sleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToSocket("sleep");
            }
        });

        shutdownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToSocket("shutdown");
            }
        });

        hibernateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToSocket("hibernate");
            }
        });
    }

    private void writeToSocket(final String command) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket s = new Socket();
                    s.connect(new InetSocketAddress(ip, port), 1000);
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    writer.write(command);
                    writer.close();
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Could not connect to the server", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch(key) {
            case "ip_address":
                ip = sharedPreferences.getString(key, "192.168.0.0");
                break;
            case "port":
                port = Integer.parseInt(sharedPreferences.getString("port", "15000"));
        }
    }
}
