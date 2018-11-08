package com.sponge.baebot;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    // navigation drawer switch
    SwitchCompat voice_switcher;
    SwitchCompat weather_switcher;
    SwitchCompat alarm_switcher;
    SwitchCompat sleep_switcher;
    SwitchCompat quote_switcher;


    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    // https://developer.android.com/reference/android/provider/CalendarContract.Events
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Events.CALENDAR_ID,                  // 0
            CalendarContract.Events.TITLE,                        // 1
            CalendarContract.Events.DESCRIPTION,                  // 2
            CalendarContract.Events.DTSTART,                      // 3
            CalendarContract.Events.DTEND,                        // 4
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_TITLE_INDEX = 1;
    private static final int PROJECTION_DESCRIPTION_INDEX = 2;
    private static final int PROJECTION_TIMESTART_INDEX = 3;
    private static final int PROJECTION_TIMEEND_INDEX = 4;

    // Initialize client for authorization
    private GoogleSignInClient mGoogleSignInClient;         // Google sign in client
    private FirebaseAuth mAuth;                             // Firebase authorization

    private static final int PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure google login in to access token
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // record firebase and google client instance
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        // checking / request user permission for calendar provider
        requestPermission();

        // call setupNavView to initialized navigation tab
        setupNavView();

        // read calendar data with AsyncQueryHandler
        ArrayList<String> calendarData = readEvent();

        //// Testing display event - hard code
        if(calendarData.size() > 0) {
            TextView event1 = (TextView)findViewById(R.id.event1);
            TextView event2 = (TextView)findViewById(R.id.event2);
            TextView event3 = (TextView)findViewById(R.id.event3);
            TextView event4 = (TextView)findViewById(R.id.event4);

            event1.setText(calendarData.get(0));
            event2.setText(calendarData.get(1));
            event3.setText(calendarData.get(2));
            event4.setText(calendarData.get(3));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /* This is the option menu on the top right, we are not using it for now
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    // Handle navigation drawer switches here.
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.voice_switch) {
            voice_switcher.setChecked(!voice_switcher.isChecked());
            Snackbar.make(item.getActionView(), (voice_switcher.isChecked()) ? "Voice On" : "Voice Off", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }
        if (id == R.id.weather_switch) {
            weather_switcher.setChecked(!weather_switcher.isChecked());
            Snackbar.make(item.getActionView(), (weather_switcher.isChecked()) ? "Weather On" : "Weather Off", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }
        if (id == R.id.alarm_switch) {
            alarm_switcher.setChecked(!alarm_switcher.isChecked());
            Snackbar.make(item.getActionView(), (alarm_switcher.isChecked()) ? "Alarm On" : "Alarm Off", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }
        if (id == R.id.sleep_switch) {
            sleep_switcher.setChecked(!sleep_switcher.isChecked());
            Snackbar.make(item.getActionView(), (sleep_switcher.isChecked()) ? "Sleep Time On" : "Sleep Time Off", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }
        if (id == R.id.quote_switch) {
            quote_switcher.setChecked(!quote_switcher.isChecked());
            Snackbar.make(item.getActionView(), (quote_switcher.isChecked()) ? "Daily Quote On" : "Daily Quote Off", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signOutButton:
                signOut();
                break;
        }
    }

    private void updateUserInfo(FirebaseUser user, NavigationView navView) {
        View headerView = navView.getHeaderView(0);

        TextView userNameText = (TextView)headerView.findViewById(R.id.userName);
        TextView userEmailText = (TextView)headerView.findViewById(R.id.userEmail);
        userNameText.setText(user.getDisplayName());
        userEmailText.setText(user.getEmail());
    }

    // signOut function - sign out from current account and return to sign in page
    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    // switch to sign in activity while complete signout from google
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        switchActivity(SignInActivity.class);
                    }
                });
    }


    // switch Activity utility, switching to new activity given by param
    private void switchActivity(final Class<? extends Activity> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
    }

    // helper function to ensure app has user permission to read/write calendar
    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // checking permission before reading and writing calendar
            if (checkSelfPermission(Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_DENIED) {
                Log.d("permission", "permission denied to read calendar");
                String[] permissions = {Manifest.permission.READ_CALENDAR};
                Log.d("permission", "requesting permission");
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            }

            if (checkSelfPermission(Manifest.permission.WRITE_CALENDAR)
                    == PackageManager.PERMISSION_DENIED) {
                Log.d("permission", "permission denied to write calendar");
                String[] permissions = {Manifest.permission.WRITE_CALENDAR};
                Log.d("permission", "requesting permission");
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            }
        }
    }

    // setUp navigation tab view
    private void setupNavView() {
        // get navigation view component
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //get the menu item from the navigation view
        Menu menu = navigationView.getMenu();

        // update user info on navigation tab
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUserInfo(currentUser, navigationView);

        // set up button on click listener
        View headerView = navigationView.getHeaderView(0);
        headerView.findViewById(R.id.signOutButton).setOnClickListener(this);

        //toolbar for navigation drawer
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        // What is this pieces of code for?
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        //Voice
        MenuItem menuItem_voice = menu.findItem(R.id.voice_switch);
        View actionView_voice = menuItem_voice.getActionView();

        voice_switcher = actionView_voice.findViewById(R.id.switcher_drawer);
        voice_switcher.setChecked(true);
        voice_switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //voice_switcher.setChecked(!voice_switcher.isChecked());
                Snackbar.make(v, (voice_switcher.isChecked()) ? "Voice On" : "Voice Off", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        });

        //Weather
        MenuItem menuItem_weather = menu.findItem(R.id.weather_switch);
        View actionView_weather = menuItem_weather.getActionView();

        weather_switcher = actionView_weather.findViewById(R.id.switcher_drawer);
        weather_switcher.setChecked(true);
        weather_switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, (weather_switcher.isChecked()) ? "Weather On" : "Weather Off", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        });

        //Alarm
        MenuItem menuItem_alarm = menu.findItem(R.id.alarm_switch);
        View actionView_alarm = menuItem_alarm.getActionView();

        alarm_switcher = actionView_alarm.findViewById(R.id.switcher_drawer);
        alarm_switcher.setChecked(true);
        alarm_switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, (alarm_switcher.isChecked()) ? "Alarm On" : "Alarm Off", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        });

        //Sleep
        MenuItem menuItem_sleep = menu.findItem(R.id.sleep_switch);
        View actionView_sleep = menuItem_sleep.getActionView();

        sleep_switcher = actionView_sleep.findViewById(R.id.switcher_drawer);
        sleep_switcher.setChecked(true);
        sleep_switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, (sleep_switcher.isChecked()) ? "Sleep Time On" : "Sleep Time Off", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        });

        //Quote
        MenuItem menuItem_quote = menu.findItem(R.id.quote_switch);
        View actionView_quote = menuItem_quote.getActionView();

        quote_switcher = actionView_quote.findViewById(R.id.switcher_drawer);
        quote_switcher.setChecked(true);
        quote_switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, (quote_switcher.isChecked()) ? "Daily Quote On" : "Daily Quote Off", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        });
    }

    ////// TESTING ONLY - NOT AsyncQueryHandler
    private ArrayList<String> readEvent() {

        // make up a time range for searching event
        // for future "searching" usage
        /*
        long startMillis = 0;
        long endMillis = 0;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2018, 11, 02, 6, 00);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2018, 11, 15, 6, 00);
        endMillis = endTime.getTimeInMillis();
        */

        ContentResolver cr = getContentResolver();

        // use CalendarContract.Instances for read data on calendar (rather than owner info)
        //Uri uri = CalendarContract.Calendars.CONTENT_URI;
        //Uri uri = CalendarContract.Instances.CONTENT_URI;
        Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/events");
        /*
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);
        */

        Cursor cur = null;

        // ---- current design return all calendar event (no filtering)
        // could add the feature in the future - pass in from the parameter
        /*
        String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {"hera@example.com", "com.example",
                "hera@example.com"};
        */

        // set selection and selectionArgs as null
        String selection = null;
        String[] selectionArgs = null;

        //cur = cr.query(CALENDAR_URI, new String[] { "calendar_id", "title", "description",
        //        "dtstart", "dtend", "eventLocation" }, selection, selectionArgs, null);

        cur = cr.query(CALENDAR_URI, EVENT_PROJECTION, selection, selectionArgs, null);
        ArrayList<String> calendarData = new ArrayList<>();

        if(cur.getCount() > 0) {
            Log.d("readEvent", "events found");

            cur.moveToFirst();
            while (cur.moveToNext()) {
                // information of event
                String eventTitle;
                String eventBeginMill;
                String eventBeginDate;

                // Get the field values
                eventTitle = cur.getString(PROJECTION_TITLE_INDEX);
                eventBeginMill = cur.getString(PROJECTION_TIMESTART_INDEX);
                eventBeginDate = milliToDate(eventBeginMill);

                // Building string of current cursor data
                // String currentData = String.format("Calendar ID: %s\nDisplay Name: %s\nAccount Name: %s\nOwner Name: %s", calID, displayName, accountName, ownerName);
                String currentData = String.format("Event Name: %s\nBegin Time: %s", eventTitle, eventBeginDate);
                Log.d("readEvent", currentData);
                calendarData.add(currentData);
            }
        }

        //ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, calendarData);
        //listView.setAdapter(stringArrayAdapter);

        return calendarData;
    }


    // helper function - convert millisecond to readable date
    private String milliToDate(String milliSec) {
        String date;            // date convert from millisecond

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendarTemp = Calendar.getInstance();
        calendarTemp.setTimeInMillis(Long.parseLong(milliSec));
        date = formatter.format(calendarTemp.getTime());

        return date;
    }
}


