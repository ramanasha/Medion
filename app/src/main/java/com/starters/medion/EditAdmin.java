package com.starters.medion;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;

import com.gc.materialdesign.views.Button;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.starters.medion.dbtasks.InsertTask;
import com.starters.medion.model.Eid;
import com.starters.medion.model.Event;
import com.starters.medion.service.TrackGPS;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.Inflater;


import static android.app.Activity.RESULT_OK;

public class EditAdmin extends Fragment {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS = 2;
    ArrayAdapter<String> adapter;
    private ListView contact_list = null;
    private HashMap<String,String> myMap;
    private ArrayList<String> contactsarray = new ArrayList<>();
    private ArrayList<String> contactsarray2 = new ArrayList<>();
    private ImageButton imageButton;
    private int RESULT_LOAD_IMG =1;
    private String decodableImage;
    private int waiter = 0;
    private ProgressBarCircularIndeterminate progBar;
    private String tempDate;
    private String tempTime;
    private TrackGPS trackGPS;
    private EditText eventname;
    private ImageButton membersButton;
    private ButtonRectangle saveButton;
    private Event event;
    private Home home;
    private ButtonRectangle finalizeEvent;
    private Eid eid;
    private LinearLayout layoutToAdd;
    private String eventId;
    private String members;
    private String userphonenum=null;
    private ImageButton datepicker;
    private View view;
    private ImageButton timepicker;
    private android.widget.Button next;
    private EditText eventName;


    //interfaces to communicate with the activities
    public interface HomeListener
    {
        public String getDate();
        public String getTime();
    }


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(getView()!=null) {
            ViewGroup parent = (ViewGroup) getView().getParent();
            if (parent != null) {
                parent.removeView(getView());
            }
        }
        try {
            view = inflater.inflate(R.layout.edit_admin, container, false);
        } catch (InflateException e) {
            Toast.makeText(getActivity(),R.string.layoutexception,Toast.LENGTH_LONG).show();

        }


        datepicker = (ImageButton) view.findViewById(R.id.edit_admin_select_date);
        timepicker= (ImageButton) view.findViewById(R.id.edit_admin_select_time);
        eventname = (EditText)view.findViewById(R.id.edit_admin_event_name);
        imageButton = (ImageButton) view.findViewById(R.id.edit_imagebutton);
        saveButton = (ButtonRectangle) view.findViewById(R.id.edit_admin_save);
        membersButton = (ImageButton) view.findViewById(R.id.edit_admin_addMembers);
        progBar =(ProgressBarCircularIndeterminate) view.findViewById(R.id.progressBarCircularIndeterminate);




        //event listener code for membersButton
        membersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v==membersButton)
                {
                    Toast.makeText(getActivity(),"Please wait! accessing your contacts!",Toast.LENGTH_SHORT).show();
                    progBar.setVisibility(View.VISIBLE);
                    //check whether the user has provisioned the contacts access permission
                    checkContactPermission();
                    //display the list of contacts.
                    showDialogListView(v);
                }
            }
        });



        //Groups image handling code
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v==imageButton)
                {
                    //opens gallery of the user to set a group photo
                    Intent gallery_opener = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivity(gallery_opener);
                }
            }
        });

        //opens the date picker dialog on clicking selectdate button
        datepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v==datepicker){
                    Picker pickerDialogs= new Picker();
                    pickerDialogs.show(getFragmentManager(),"date_picker");

                }
            }
        });
        //opens the time picker dialog on clicking select time button
        timepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v==timepicker){
                    TimePicker timepickerdialog = new TimePicker();
                    timepickerdialog.show(getFragmentManager(),"time_picker");
                }

            }
        });


        //handling the event of clicking save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<String> mem = new ArrayList<>();
                for(int i=0; i<contactsarray.size(); i++) {
                    mem.add(i, contactsarray.get(i));
                }

                members = TextUtils.join(",", mem);

                //getting the current location of the admin
                trackGPS = new TrackGPS(getContext(),getActivity());
                if (trackGPS.canGetLocation()) {

                    System.out.println("LOCATION"+trackGPS.getLongitude());
                }
                try {
                    FileInputStream f = getActivity().openFileInput("login_details_file");
                    BufferedReader br = new BufferedReader( new InputStreamReader(f));
                    String line;
                    //acesssing login_details_file to get the user phone number
                    while((line = br.readLine())!=null)
                    {
                        userphonenum = line;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(getActivity(),"Location retrieved! Please wait!",Toast.LENGTH_LONG).show();
                //set progress bar to visible until the server responds.
                progBar.setVisibility(View.VISIBLE);
                //starting asynctask to send event details along with user's lat long and phone number
                new EditAdmin.HttpAsyncTask().execute(eventname.getText().toString(),home.getDate(),home.getTime(),members,"https://whispering-everglades-62915.herokuapp.com/api/notifyMembers",Double.toString(trackGPS.getLatitude())+","+Double.toString(trackGPS.getLongitude())+","+userphonenum);

            }
        });

        return view;
    }

    //setting date to class variables when communicated to Home.class
    public void setTempDate(String x)
    {

        tempDate = x;

    }


    //setting time to class variables when communicated to Home.class
    public void setTempTime(String y)
    {
        tempTime =y;
    }


    //Attaching home activity to this fragment class
    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        home = (Home) getActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if(requestCode==RESULT_LOAD_IMG && resultCode== RESULT_OK && null!=data)
            {
                try{
                Uri selectedImage = data.getData();
                String[] path = { MediaStore.Images.Media.DATA};
                Cursor imageTraverse = getActivity().getContentResolver().query(selectedImage,path,null,null,null);
                    assert imageTraverse != null;
                    imageTraverse.moveToFirst();
                int column = imageTraverse.getColumnIndex(path[0]);
                decodableImage= imageTraverse.getString(column);
                imageButton.setImageBitmap(BitmapFactory.decodeFile(decodableImage));
                imageButton.invalidate();
                    imageTraverse.close();
                }
                catch(Exception e)
                {
                    Toast.makeText(getActivity(),R.string.dbexception1,Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                Toast.makeText(getActivity(),"You have picked the wrong image",Toast.LENGTH_LONG).show();
            }

        }
        catch(Exception e)
        {
            Toast.makeText(getActivity(),"unknown Error retreiving image",Toast.LENGTH_LONG).show();
        }
    }

    //checking contact permission to verify whether read_contacts is enabled otherwise ask user.
    private void checkContactPermission()
    {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED)
        {


            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        else
        {
            //populating contacts_list
            populateContactList();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("populating contacts list");
                populateContactList();

            }
            else
            {
                Toast.makeText(this.getActivity(),"until you give permissions, this app cannot function properly",Toast.LENGTH_LONG).show();
            }
            return;
        }


    }

    //show a dialog with contact_list items being displayed.
    private void showDialogListView(View view)
    {
        contact_list = new ListView(getActivity());
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.contact_list, R.id.contacts, contactsarray2);
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
                Toast.makeText(getActivity(), contactstxt.getText().toString(),Toast.LENGTH_LONG).show();
            }
        });
        //Alert builder to display the list with ok button.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setPositiveButton("OK",null);
        builder.setView(contact_list);
        AlertDialog dialog = builder.create();
        progBar.setVisibility(View.INVISIBLE);
        dialog.show();


    }


    //populating items in contactsarray2
    private void populateContactList()
    {
        myMap = new HashMap<>();
        ContentResolver resolver = getActivity().getContentResolver();

            Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        Cursor phoneCursor;
        try {

            assert cursor != null;
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?", new String[]{id}, null);
                assert phoneCursor != null;
                while (phoneCursor.moveToNext()) {
                    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    myMap.put(name, phoneNumber);
                    contactsarray2.add(name + "/" + phoneNumber);
                    break;
                }
                phoneCursor.close();
            }
            cursor.close();
        }
        catch(Exception e)
        {
            Toast.makeText(getActivity(),R.string.dbexception1,Toast.LENGTH_LONG).show();
        }
    }


    private static String POST(String stringURL, Eid eid){
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

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while (in.readLine() != null) {
                System.out.println(in);
            }
            System.out.println("\nMedion notify REST Service Invoked Successfully..");
            in.close();

        }catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return null;
    }

    //post request to server to send the newly created event details
    private static String POST(String stringURL, Event event) {
        String result = "";
        try {

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
            JSONObject eventJson = new JSONObject();
            eventJson.accumulate("eventName", event.getEventName());
            eventJson.accumulate("eventDate", event.getEventDate());
            eventJson.accumulate("eventTime", event.getEventTime());
            eventJson.accumulate("memberList", event.getMemberList());

            // 4. convert JSONObject to JSON to String and send json content
            out.write(eventJson.toString());
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                result = inputLine;
                System.out.println("event id returned from server: "+result);
            in.close();
//            }
            System.out.println("\nMedion notify REST Service Invoked Successfully..");
//            in.close();
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }


    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            int count = args.length;
            if(count < 3){
                return POST(args[1], eid);


            }else {
                event = new Event();
                event.setEventName(args[0]);
                event.setEventDate(args[1]);
                event.setEventTime(args[2]+"/"+args[5]);
                event.setMemberList(args[3]);

                eventId= POST(args[4], event);
                return eventId;
            }

        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            System.out.println("inside postexecture"+result);
            if(!result.isEmpty()) {
                eventId = result;
                progBar.setVisibility(View.INVISIBLE);
                InsertTask insertTask = new InsertTask(getContext());
                //opening sqlite db to sotre the event details in the events table.
                insertTask.execute("", eventId, eventname.getText().toString(), home.getDate(), home.getTime(), members, "ADMIN", null);
                Toast.makeText(getActivity().getApplicationContext(), "Event Created!", Toast.LENGTH_LONG).show();
                //redirecting control to the Home class
                Intent intent = new Intent(getActivity(), Home.class);
                startActivity(intent);
            }
            else {
                Toast.makeText(getActivity(),"Network problem please press SAVE again.!",Toast.LENGTH_LONG).show();
            }

            }

    }


}
