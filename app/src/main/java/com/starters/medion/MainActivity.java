package com.starters.medion;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NotificationCompat;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.starters.medion.constants.config;

import com.starters.medion.contract.EventsContract;
import com.starters.medion.dbhelper.EventsDbhelper;
import com.starters.medion.model.GeoCoordinates;
import com.starters.medion.model.UserEvent;
import com.starters.medion.service.TrackGPS;
import com.starters.medion.dbtasks.InsertTask;
//import com.starters.medion.utils.Maps;
import com.starters.medion.utils.NotificationUtils;

import com.gc.materialdesign.views.ButtonRectangle;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements EditAdmin.MainActivityListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static String fcmToken = null;
    private static GeoCoordinates geoCoordinates;
    private TrackGPS trackGPS;
    private UserEvent userEvent;
    private EditText userName;
    private EditText password;
    private String username;
    private String pass;
    public String res;
    private String eventId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addLoginClickListener();
        addSignupClickListener();
        displayFirebaseRegId();
        userName = (EditText) findViewById(R.id.ConnectStage_Username);
        password = (EditText) findViewById(R.id.ConnectStage_Password);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("intentKey"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("key");
            Log.e("mm", message);
            int notifyID=1;
            NotificationManager notify = (NotificationManager) getSystemService(context.NOTIFICATION_SERVICE);
            String[] parts = message.split(",");

            if(parts[0].equals("EventCreated")) {
                System.out.println("ENTERED EVENT CREATED");

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.appimage)
                                .setContentTitle("Medion")
                                .setContentText("New Event "+parts[2]+" created");
                notify.notify(notifyID,mBuilder.build());
                Toast.makeText(getApplicationContext(), "You have been Added to an Event", Toast.LENGTH_LONG).show();
                eventId = parts[1];
                InsertTask insert = new InsertTask(getApplicationContext());
                insert.execute("",parts[1],parts[2],parts[3],parts[4],parts[5],"MEMBER","");
                eventId = parts[1];

//              geoCoordinates = new GeoCoordinates();
                trackGPS = new TrackGPS(MainActivity.this);
                if (trackGPS.canGetLocation()) {
//                            geoCoordinates.setLatitude(trackGPS.getLatitude());
//                            geoCoordinates.setLongitude(trackGPS.getLongitude());
                    System.out.println("LOCATION"+trackGPS.getLongitude());
//                    new MainActivity.HttpAsyncTask().execute(parts[1], fcmToken, String.valueOf(trackGPS.getLatitude()), String.valueOf(trackGPS.getLongitude()),"http://149.161.150.243:8080/api/addUserEvent");
                    new MainActivity.HttpAsyncTask().execute(parts[1], fcmToken, String.valueOf(trackGPS.getLatitude()), String.valueOf(trackGPS.getLongitude()), "https://whispering-everglades-62915.herokuapp.com/api/addUserEvent");
                }
            }else if(parts[0].equals("MedionCalculated")){

                System.out.println("inside medion..!");
                String latitude = parts[1];
                String longitude = parts[2];
                String evid = parts[3];
                EventsDbhelper eventsDbhelper = new EventsDbhelper(getApplicationContext());
                SQLiteDatabase db = eventsDbhelper.getWritableDatabase();
                Cursor data = db.rawQuery("Update " + EventsContract.EventsEntry.TABLE_NAME+" set "+EventsContract.EventsEntry.COLUMN_NAME_LOCATION+"="+parts[1]+","+parts[2]+" where "+EventsContract.EventsEntry.COLUMN_NAME_EVENTID+"="+evid, null);
                Intent mesintent=new Intent(MainActivity.this,PlacesMap.class);
                mesintent.putExtra("latlong",latitude+"/"+longitude);
                startActivity(mesintent);

            }else if(parts[0].equals("FinalPlace")){
                String latitude = parts[1];
                String longitude = parts[2];
                Intent msgfinal = new Intent(MainActivity.this,Home.class);
                msgfinal.putExtra("ll",latitude+"/"+longitude);
                startActivity(msgfinal);
            }

        }
    };

    public void addLoginClickListener()
    {
        Button login = (Button) findViewById(R.id.Connectstage_login);
        login.setFocusable(true);
        login.setFocusableInTouchMode(true);
        login.requestFocus();
        login.setOnClickListener(new OnClickListener() {
                                     public void onClick(View v) {
                                         if(userName.getText().toString().isEmpty() || password.getText().toString().isEmpty())
                                         {
                                             Toast.makeText(getApplicationContext(),"Invalid username or password sequences",Toast.LENGTH_LONG).show();
                                             return;
                                         }
                                         else {
                                             username = userName.getText().toString();
                                             pass = password.getText().toString();
                                             new LoginAsyncTask().execute(username, pass);
//                                         Intent intent = new Intent(getApplicationContext(),Home.class);
//                                         startActivity(intent);
                                         }
                                     }
                                 }

        );
    }

    public void addSignupClickListener()
    {
        Button signup = (Button) findViewById(R.id.ConnectStage_SignUp);
        signup.setFocusable(true);
        signup.setFocusableInTouchMode(true);
        signup.requestFocus();
        signup.setOnClickListener(new OnClickListener(){


            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(intent);
            }

        });
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);
        fcmToken = regId;
        Log.e(TAG, "Firebase reg id: " + regId);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    public static String POST(String stringURL, UserEvent userEvent) {
        InputStream inputStream = null;
        String result = "";
        try {

            Log.d("InputStream", "Before Connecting");
            // 1. create URL
            URL url = new URL(stringURL);

            // 2. create connection to given URL
            URLConnection connection = url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());

            // 3. build jsonObject
            JSONObject userEventJson = new JSONObject();
            userEventJson.accumulate("eventId", userEvent.getEventId());
            userEventJson.accumulate("userFcmToken", userEvent.getUserFcmToken());
            userEventJson.accumulate("acceptance", true);
            userEventJson.accumulate("latitude", userEvent.getLatitude());
            userEventJson.accumulate("longitude", userEvent.getLongitude());

            // 4. convert JSONObject to JSON to String and send json content
            out.write(userEventJson.toString());
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while (in.readLine() != null) {
                System.out.println(in);
            }
            System.out.println("\nMedion REST Service Invoked Successfully..");
            in.close();
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }



    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            userEvent = new UserEvent();
            userEvent.setEventId(Integer.parseInt(args[0]));
            userEvent.setUserFcmToken(args[1]);
            userEvent.setLatitude(args[2]);
            userEvent.setLongitude(args[3]);

            return POST(args[4],userEvent);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "You have signed up!", Toast.LENGTH_LONG).show();
        }
    }

    private class LoginAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
//            Toast.makeText(MainActivity.this,"Successfully sent login details to server",Toast.LENGTH_LONG).show();
            if(res.equals("Valid User"))
            {
                Toast.makeText(MainActivity.this,"Successfully logged in",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(),Home.class);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(MainActivity.this,"Invalid Login Details",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {

                res = GET("https://whispering-everglades-62915.herokuapp.com/api/login?name="+params[0]+"&pass="+params[1]);

            return res ;
        }


    }
    public static String GET(String stringURL) {
        InputStream inputStream = null;
        String result = "";
        try {

            Log.d("InputStream", "Before Connecting");
            // 1. create URL
            URL url = new URL(stringURL);

            // 2. create connection to given URL
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
//            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            System.out.println(connection.getResponseMessage());
//            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
//
//            // 3. build jsonObject
//           JSONObject json = new JSONObject();
//            json.put("username",myuser);
//            json.put("password",mypass);
//
//            // 4. convert JSONObject to JSON to String and send json content
//            out.write(json.toString());
//            out.flush();
//            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                result = inputLine;
            in.close();
//            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//
//            while (in.readLine() != null) {
//                System.out.println(in);
//            }
            System.out.println("\nsuccessfully sent login details.");
//            in.close();
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }
}