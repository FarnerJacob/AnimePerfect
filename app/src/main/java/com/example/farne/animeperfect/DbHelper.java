package com.example.farne.animeperfect;


import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ViewHistory.db";
    public static final String HISTORY_TABLE_NAME = "viewHistory";
    public static final String HISTORY_COLUMN_ID = "id";
    public static final String HISTORY_COLUMN_TITLE = "title";
    public static final String HISTORY_COLUMN_EPISODE = "episode";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table viewHistory " +
                        "(id int, title text , episode int)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS viewHistory");
        onCreate(db);
    }

    public boolean insertRecentlyWatched (int id, String title, int episode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("title", title);
        contentValues.put("episode", episode);
        db.insert("viewHistory", null, contentValues);
        return true;
    }

    public ArrayList<String> getRecentlyWatched() {
        ArrayList<String> array = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from viewHistory group by title order by id desc limit 5", null );
        res.moveToFirst();

        int index = 0;
        while(res.isAfterLast() == false){
            array.add(res.getString(res.getColumnIndex(HISTORY_COLUMN_TITLE)));
            res.moveToNext();
        }
        return array;
    }

    public int getNextID(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, HISTORY_TABLE_NAME);
        return numRows+1;
    }

    /*public boolean updateContact (Integer id, String name, String phone, String email, String street,String place) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("street", street);
        contentValues.put("place", place);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }*/

    /*public Integer deleteContact (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }*/

    public ArrayList<String> getAllWatched() {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from viewHistory group by title order by id desc", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(HISTORY_COLUMN_TITLE)));
            res.moveToNext();
        }
        return array_list;
    }


    //Returns an array of the episodes watched. An index equal to 1 is considered watched.
    public int[] getEpisodesWatched(String title, int begin, int end) {
        int[] array = new int[end];

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select episode from viewHistory where title = '"+title+"' group by episode", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array[Integer.parseInt(res.getString(res.getColumnIndex(HISTORY_COLUMN_EPISODE)))-1] = 1;
            res.moveToNext();
        }
        return array;
    }
}