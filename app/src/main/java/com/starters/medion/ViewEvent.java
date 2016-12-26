package com.starters.medion;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.widgets.Dialog;
import com.starters.medion.contract.EventsContract;
import com.starters.medion.dbtasks.InsertTask;
import com.starters.medion.model.Delid;
import com.starters.medion.model.Eid;
import com.starters.medion.model.Event;
import com.starters.medion.dbhelper.EventsDbhelper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Ashish on 12/8/2016.
 */
public class ViewEvent extends AppCompatActivity {

    private TextView eventid;
    private TextView eventnameview;
    private TextView eventdate;
    private TextView eventadmin;
    public HashMap<String,String> myMap;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS = 2;
    private TextView eventime;
    private TextView eventmembers;
    private TextView location;
    private Event event;
    private EventsDbhelper mdbhelper;
    private Eid eid;
    private ImageButton finalizeEvent;
    public ButtonRectangle saveButton;
    public ImageButton membersButton;
    private ImageButton currentMembers;
    private ImageButton cancelEvent;
    private ImageButton requestPlaces;
    private String members;
    private String [] newMemberList;
    public ListView contact_list = null;
    public ArrayList<String> contactsarray = new ArrayList<String>();
    public ArrayList<String> contactsarray2 = new ArrayList<String>();
    private String eventId;
    private String mem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_event_layout);
        eventnameview = (TextView)findViewById(R.id.eventname);
        eventid = (TextView)findViewById(R.id.eventid);
        eventdate = (TextView)findViewById(R.id.eventdate);
        eventime = (TextView)findViewById(R.id.eventtime);
//        eventmembers = (TextView)findViewById(R.id.eventmembers);
        eventadmin = (TextView)findViewById(R.id.eventadmin);
        location = (TextView)findViewById(R.id.location);
        currentMembers = (ImageButton) findViewById(R.id.edit_viewevent_currentMembers);


        Bundle b = getIntent().getExtras();
        if(b != null){
            eventId = b.getString("id");
            String eventName = b.getString("eventname");
            String eventAdmin=b.getString("admin");
            String date = b.getString("date");
            String time = b.getString("time");
            String latlongs = b.getString("latlongs");
            String members = b.getString("members");
            System.out.println("IDID:" + eventId);
            System.out.println("Name:" + eventName);
            System.out.println("admin:" + eventAdmin);
            System.out.println("date:" + date);
            System.out.println("time:" + time);
            System.out.println("members:" + members);

//            eventid.setText(eventId);
//            eventnameview.setText(eventName);
//            eventdate.setText(date);
//            eventime.setText(time);
//            eventmembers.setText(members);
//            eventadmin.setText(eventAdmin);
//            location.setText(latlongs);

        }

            mdbhelper = new EventsDbhelper(this);
        Cursor rs = mdbhelper.getData(eventId);
        try{

        if(rs!=null) {

            rs.moveToFirst();
            String nam = rs.getString(rs.getColumnIndex(EventsContract.EventsEntry.COLUMN_NAME_EVENTNAME));
            String dat = rs.getString(rs.getColumnIndex(EventsContract.EventsEntry.COLUMN_NAME_DATE));
            String tim = rs.getString(rs.getColumnIndex(EventsContract.EventsEntry.COLUMN_NAME_TIME));
            mem = rs.getString(rs.getColumnIndex(EventsContract.EventsEntry.COLUMN_NAME_MEMBERS));
            String pla = rs.getString(rs.getColumnIndex(EventsContract.EventsEntry.COLUMN_NAME_LOCATION));
            String adm = rs.getString(rs.getColumnIndex(EventsContract.EventsEntry.COLUMN_NAME_ADMIN));

            eventid.setText(eventId);
            eventnameview.setText(nam);
            eventdate.setText(dat);
            eventime.setText(tim);
//            eventmembers.setText(mem);
            eventadmin.setText(adm);
            try {
                String[] x = pla.split(",");
                location.setText(x[0] + "," + x[1]);
            }
            catch(Exception e)
            {
                System.out.println("before connecting location is null");
            }
            }
        }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        finally{

            try {
                if( rs != null && !rs.isClosed())
                    rs.close();
                    mdbhelper.close();
            } catch(Exception ex) {}



        }

        finalizeEvent = (ImageButton) findViewById(R.id.edit_viewevent_finalizeevent);
        saveButton = (ButtonRectangle) findViewById(R.id.edit_viewevent_save);
        membersButton = (ImageButton) findViewById(R.id.edit_viewevent_addMembers);
        cancelEvent = (ImageButton) findViewById(R.id.edit_viewevent_cancelevent);
        requestPlaces = (ImageButton)findViewById(R.id.edit_viewevent_places);

        newMemberList = mem.split(",");

        requestPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Integer temp = Integer.parseInt(eventid.getText().toString());
                eid = new Eid();
                eid.setId(temp);
                new HttpAsyncTask().execute("https://whispering-everglades-62915.herokuapp.com/api/calcMedian");

            }
        });

        membersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v==membersButton)
                {
                    checkContactPermission();
                    showDialogListView(v);
                }
            }
        });

        currentMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewEvent.this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(mem)
                        .setTitle("Current Members");

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        cancelEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder cancelbuilder = new android.app.AlertDialog.Builder(ViewEvent.this);
                cancelbuilder.setTitle("Cancel Invitation?");
                cancelbuilder.setMessage("Are you sure you can't make it? last chance.!");
                cancelbuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DelAsyncTask().execute("https://whispering-everglades-62915.herokuapp.com/api/delEvent",eventId);
                    }
                });
                cancelbuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
                android.app.AlertDialog dialog = cancelbuilder.create();
                dialog.show();
            }
        });
//        saveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                System.out.println(eventnameview.getText().toString());
//                System.out.println(eventdate.getText().toString()+"vvv"+eventime.getText().toString());
//
//
//                ArrayList<String> mem = new ArrayList<String>();
//                for(int i=0; i<contactsarray.size(); i++) {
//                    mem.add(i, contactsarray.get(i));
//                }
////                mem.add(0,"123");
////                mem.add(0,"8129551395");
//                members = TextUtils.join(",", mem);
//                //To get it back to ArrayList,
//                //List<String> myList = new ArrayList<String>(Arrays.asList(members.split(",")));
////                new EditAdmin.HttpAsyncTask().execute(eventname.getText().toString(),home.getDate(),home.getTime(),members,"http://149.161.150.243:8080/api/notifyMembers");
//                new HttpAsyncTask().execute(eventnameview.getText().toString(),eventdate.getText().toString(),eventime.getText().toString(),members,"https://whispering-everglades-62915.herokuapp.com/api/notifyMembers");
//
//            }
//        });

    }

    public void checkContactPermission()
    {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED)
        {


            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        else
        {
            populateContactList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("populating contacts list");
                populateContactList();
            }
            else
            {
                Toast.makeText(this,"until you give permissions, this app cannot function properly",Toast.LENGTH_LONG);
            }
            return;
        }


    }


    public void populateContactList()
    {
        myMap = new HashMap<String,String>();


        ContentResolver resolver = this.getContentResolver();
        Cursor cursor =resolver.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        try {

            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?", new String[]{id}, null);
                while (phoneCursor.moveToNext()) {
                    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    phoneNumber = phoneNumber.replaceAll("\\s+", "");
                    phoneNumber = phoneNumber.replaceAll("[^a-zA-Z0-9]", "");
                    int count = 0;
                    for (int i = 0; i < newMemberList.length; i++) {
                        if (newMemberList[i].equals(phoneNumber)) {
                            count++;
                            System.out.println("insi");
                            break;
                        }
                    }
                    if (count == 0) {
                        myMap.put(name, phoneNumber);
                        contactsarray2.add(name + "/" + phoneNumber);
                    }
                    break;

                }
                phoneCursor.close();
            }
            cursor.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally{
            try {
                if( cursor != null && !cursor.isClosed())
                    cursor.close();
            } catch(Exception ex) {}

        }
    }

    public static String POST(String stringURL, Eid eid){
        String result="";
        try {
            Log.d("POST","reached!");
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
            JSONObject eventIDJson = new JSONObject();
            eventIDJson.accumulate("id", eid.getId());

            // 4. convert JSONObject to JSON to String and send json content
            out.write(eventIDJson.toString());
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                result = inputLine;
            in.close();

        }catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }

//    public static String POST(String stringURL, Event event) {
//        String result = "";
//        try {
//
//            // 1. create URL
//            URL url = new URL(stringURL);
//
//            // 2. create connection to given URL
//            URLConnection connection = url.openConnection();
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//            connection.setUseCaches(false);
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setConnectTimeout(5000);
//            connection.setReadTimeout(5000);
//            connection.connect();
//            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
//
//            // 3. build jsonObject
//            JSONObject eventJson = new JSONObject();
//            eventJson.accumulate("eventName", event.getEventName());
//            eventJson.accumulate("eventDate", event.getEventDate());
//            eventJson.accumulate("eventTime", event.getEventTime());
//            eventJson.accumulate("memberList", event.getMemberList());
//
//            // 4. convert JSONObject to JSON to String and send json content
//            out.write(eventJson.toString());
//            out.flush();
//            out.close();
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(
//                    connection.getInputStream()));
//            String inputLine;
//            while ((inputLine = in.readLine()) != null)
//                result = inputLine;
//            System.out.println(result);
//            in.close();
////            }
//            System.out.println("\nMedion notify REST Service Invoked Successfully..");
////            in.close();
//        } catch (Exception e) {
//            Log.d("InputStream", e.getLocalizedMessage());
//        }
//
//        return result;
//    }

    public static String POST(String stringURL, Delid eid){
        String result="";
        try {
            Log.d("POST","reached!");
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
            JSONObject eventIDJson = new JSONObject();
            eventIDJson.accumulate("id", eid.getId());

            // 4. convert JSONObject to JSON to String and send json content
            out.write(eventIDJson.toString());
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                result = inputLine;
            in.close();

        }catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }


    public void showDialogListView(View view)
    {
        contact_list = new ListView(ViewEvent.this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ViewEvent.this,R.layout.contact_list,R.id.contacts,contactsarray2);
        contact_list.setAdapter(adapter);
        contact_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewGroup contactvg = (ViewGroup)view;
                TextView contactstxt = (TextView)contactvg.findViewById(R.id.contacts);
                String s = contactstxt.getText().toString();
                String[] phone =s.split("/");
                phone[1]=phone[1].replaceAll("\\s+","");
                phone[1]=phone[1].replaceAll("[^a-zA-Z0-9]","");
                System.out.println("contact is:"+phone[1]);
                contactsarray.add(phone[1]);
                Toast.makeText(ViewEvent.this, contactstxt.getText().toString(),Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(ViewEvent.this);
        builder.setCancelable(true);
        builder.setPositiveButton("OK",null);
        builder.setView(contact_list);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        private String res;
        @Override
        protected String doInBackground(String... args) {
            int count = args.length;
            if(count < 3){
                res= POST(args[1], eid);
                return res;
            }
//            else {
//                event = new Event();
//                event.setEventId(eid);
//                event.setEventName(args[0]);
//                event.setEventDate(args[1]);
//                event.setEventTime(args[2]);
//                event.setMemberList(args[3]);
//
//                eventId= POST(args[4], event);
//                return eventId;
//            }
            return null;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            System.out.println("inside postexecture"+res);
            if(res.isEmpty())
            {
                Toast.makeText(getApplicationContext(),"Please retry pick places!",Toast.LENGTH_LONG).show();
                return;
            }
            String [] parts = res.split(",");
            EventsDbhelper eventsDbhelper = new EventsDbhelper(getApplicationContext());
            location.setText(parts[0]+","+parts[1]);
            if(eventsDbhelper.updateContact(eventid.getText().toString(),eventnameview.getText().toString(),eventdate.getText().toString(),eventime.getText().toString(),mem,res,eventadmin.getText().toString()))
            {
                Toast.makeText(ViewEvent.this,"Location Updated!",Toast.LENGTH_LONG).show();
            }
            Intent mesintent=new Intent(ViewEvent.this,PlacesMap.class);
            mesintent.putExtra("latlong",parts[0]+"/"+parts[1]+"/"+parts[2]);
            startActivity(mesintent);

//            InsertTask insertTask = new InsertTask(getContext());
//            insertTask.execute("",eventId,eventname.getText().toString(),home.getDate(),home.getTime(),members,"ADMIN",null);
//            Toast.makeText(getActivity().getApplicationContext(), "Event Created!", Toast.LENGTH_LONG).show();
        }
    }



    private class DelAsyncTask extends AsyncTask<String, Void, String> {

        private String res;
        @Override
        protected String doInBackground(String... args) {
            Delid delid = new Delid();
            delid.setId(args[1]);
                res= POST(args[0], delid);
                return res;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            System.out.println("inside postexectue: "+res);
            Toast.makeText(ViewEvent.this,res,Toast.LENGTH_LONG).show();
            EventsDbhelper eventsDbhelper = new EventsDbhelper(getApplicationContext());
            SQLiteDatabase db = eventsDbhelper.getWritableDatabase();
            db.delete(EventsContract.EventsEntry.TABLE_NAME,EventsContract.EventsEntry.COLUMN_NAME_EVENTID+"=?",new String[]{eventId});
            db.close();
            eventsDbhelper.close();
            Intent mesintent=new Intent(ViewEvent.this,Home.class);
            startActivity(mesintent);

        }
    }

}
