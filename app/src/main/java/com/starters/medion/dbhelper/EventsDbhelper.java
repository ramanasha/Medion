package com.starters.medion.dbhelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.CalendarContract;

import com.starters.medion.contract.EventsContract.EventsEntry;
/**
 * Created by Ashish on 12/1/2016.
 */
public class EventsDbhelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    private Context context;
    public static final String DATABASE_NAME = "Events.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + EventsEntry.TABLE_NAME + " (" +
                    EventsEntry._ID + " INTEGER PRIMARY KEY," +
                    EventsEntry.COLUMN_NAME_EVENTNAME + TEXT_TYPE + COMMA_SEP +
                    EventsEntry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    EventsEntry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    EventsEntry.COLUMN_NAME_MEMBERS + TEXT_TYPE + COMMA_SEP +
                    EventsEntry.COLUMN_NAME_LOCATION + TEXT_TYPE +" )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + EventsEntry.TABLE_NAME;
    public EventsDbhelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.setContext(context);
    }


    public Context getContext(){
        return this.context;

    }
    public void setContext(Context con){
        this.context = con;
    }
    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean addData(String item){
        return false;
    }

    public Cursor getListContents(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor data = db.rawQuery("SELECT * from " + EventsEntry.TABLE_NAME, null);
        return data;
    }


}
