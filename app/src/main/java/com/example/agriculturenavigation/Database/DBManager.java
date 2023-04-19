package com.example.agriculturenavigation.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBManager extends SQLiteOpenHelper
{
    public static final String DB_NAME = "Navdb";
    public static final String TABLE_FIELDS = "fields";
    public static final String TABLE_PATTERNS = "patterns";
    public static final int DB_VERSION = 1;
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String LOCATION = "location";
    public static final String AREA = "area";

    public static final String PID = "pid";
    public static final String PATTERNAME = "patternname";
    public static final String PATTERN = "pattern"; //Συντεταγμένες pattern
    public static final String BELONGSTO = "belongsto"; //Σε ποιο χωράφι ανήκει

    private ArrayList<String> fieldlist = new ArrayList<>();
    private ArrayList<String> patternList = new ArrayList<>();

    public DBManager(Context context)
    {
        super(context,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query_create_table = "create table "+ TABLE_FIELDS
                + "(id integer primary key autoincrement,name text,location text,area text,pattern text)";
        db.execSQL(query_create_table);
        String query_create_table_patterns = "create table " + TABLE_PATTERNS
                + "(pid integer primary key autoincrement,patternname text,pattern text,belongsto text)";
        db.execSQL(query_create_table_patterns);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_FIELDS);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_PATTERNS);
        onCreate(db);
    }

    public int addField(String name,String location,String area,String pattern)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        updateFieldNameList();
        if(!fieldlist.contains(name))
        {
            values.put(NAME,name);
            values.put(LOCATION,location);
            values.put(AREA,area);
            db.insert(TABLE_FIELDS,null,values);
            db.close();
            updateFieldNameList();
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public int addPattern(String patternname,String pattern,String belongsto)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        updatePatternNameList();
        if(!patternList.contains(patternname))
        {
            values.put(PATTERNAME,patternname);
            values.put(PATTERN,pattern);
            values.put(BELONGSTO,belongsto);
            db.insert(TABLE_PATTERNS,null,values);
            db.close();
            updatePatternNameList();
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public void updateField(String oldname,String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME,name);
        db.update(TABLE_FIELDS,values,"name=?",new String[]{oldname});
        db.close();
        updateFieldNameList();
    }

    public void updatePattern(String oldname,String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PATTERNAME,name);
        db.update(TABLE_PATTERNS,values,"patternname=?",new String[]{oldname});
        db.close();
        updatePatternNameList();
    }

    public void updateFIeldCoordinates(String oldcoord,String coord)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LOCATION,coord);
        db.update(TABLE_FIELDS,values,"location=?",new String[]{oldcoord});
        db.close();
        updateFieldLocationList();
    }

    public void deleteField(String fieldname)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FIELDS,"name=?",new String[]{fieldname});
        db.close();
        updateFieldNameList();
    }

    public void deletePattern(String patternname)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PATTERNS,"patternname=?",new String[]{patternname});
        db.close();
        updatePatternNameList();
    }

    public boolean getNumberOfFields()
    {
        boolean isNull = false;
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db,TABLE_FIELDS);
        db.close();
        if(count == 0)
        {
            isNull = true;
        }
        return isNull;
    }

    @SuppressLint("SuspiciousIndentation")
    public String getFieldCoordinates(String fieldName)
    {
        String fieldCoordinates = null;
        //Δημιουργούμε db για να διαβάζει την db μας
        SQLiteDatabase db = this.getReadableDatabase();

        //Δημιουργόυμε ένα cursor για να διαβάζει δεδομένα απο την db
        Cursor fieldcursor = db.rawQuery("SELECT " + LOCATION + " FROM " + TABLE_FIELDS + " WHERE name=?",new String[]{fieldName},null);

        if(fieldcursor.moveToFirst())
        do {
                fieldCoordinates = fieldcursor.getString(0);
           }while(fieldcursor.moveToNext());
        return fieldCoordinates;
    }

    public ArrayList<FieldModal> readFields(){

        //Δημιουργούμε db για να διαβάζει την db μας
        SQLiteDatabase db = this.getReadableDatabase();

        //Δημιουργόυμε ένα cursor για να διαβάζει δεδομένα απο την db
        Cursor fieldcursor = db.rawQuery("SELECT * FROM " + TABLE_FIELDS,null);

        ArrayList<FieldModal> fieldModalArrayList = new ArrayList<>();

        //Πγαίνουμε τον cursor στην πρώτη θέση
        if(fieldcursor.moveToFirst()){
            do{
                fieldModalArrayList.add(new FieldModal(fieldcursor.getString(1),
                                                       fieldcursor.getString(2),
                                                       fieldcursor.getString(3)));
            }while(fieldcursor.moveToNext());
        }
        fieldcursor.close();
        return fieldModalArrayList;
    }

    public ArrayList<PatternModal> readPatterns(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor patterncursor = db.rawQuery("SELECT * FROM " + TABLE_PATTERNS,null);

        ArrayList<PatternModal> patternModalArrayList = new ArrayList<>();
        //Πγαίνουμε τον cursor στην πρώτη θέση
        if(patterncursor.moveToFirst()){
            do{
                patternModalArrayList.add(new PatternModal(patterncursor.getString(1),
                                                           patterncursor.getString(2),
                                                           patterncursor.getString(3)));
            }while(patterncursor.moveToNext());
        }
        patterncursor.close();
        return patternModalArrayList;
    }

    public ArrayList<PatternModal> readPatternsOfField(String field){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor patterncursor = db.rawQuery("SELECT * FROM " + TABLE_PATTERNS + " WHERE belongsto=?",new String[]{field});

        ArrayList<PatternModal> patternModalArrayList = new ArrayList<>();
        //Πγαίνουμε τον cursor στην πρώτη θέση
        if(patterncursor.moveToFirst()){
            do{
                patternModalArrayList.add(new PatternModal(patterncursor.getString(1),
                        patterncursor.getString(2),
                        patterncursor.getString(3)));
            }while(patterncursor.moveToNext());
        }
        patterncursor.close();
        return patternModalArrayList;
    }

    public void updateFieldNameList()
    {
        fieldlist.removeAll(fieldlist);
        ArrayList<FieldModal> lmlist = readFields();
        int i = lmlist.size();
        for(int j=0;j<i;j++)
        {
            fieldlist.add(lmlist.get(j).getFieldName());
        }
    }

    public void updatePatternNameList()
    {
            patternList.removeAll(patternList);
            ArrayList<PatternModal> ptList = readPatterns();
            int i = ptList.size();
            for(int k=0;k<i;k++)
            {
                patternList.add(ptList.get(k).getPatternname());
            }
    }

    public void updateFieldLocationList()
    {
        fieldlist.removeAll(fieldlist);
        ArrayList<FieldModal> lmlist = readFields();
        int i = lmlist.size();
        for(int j=0;j<i;j++)
        {
            fieldlist.add(lmlist.get(j).getFieldLocation());
        }
    }

    public ArrayList<Double> retrieveFieldLatitude(String fieldLocation)
    {
        ArrayList<Double> latitudelist = new ArrayList<Double>();
        Pattern LAT_LNG = Pattern.compile("([-\\d.]+),([-\\d.]+)");
        Matcher matcher = LAT_LNG.matcher(fieldLocation);
        while(matcher.find())
        {
            double lat = Double.parseDouble(matcher.group(1));
            double lng = Double.parseDouble(matcher.group(2));
            latitudelist.add(lat);
        }


        return latitudelist;
    }
    public ArrayList<Double> retrieveFieldLongitude(String fieldLocation)
    {
        ArrayList<Double> longitudelist = new ArrayList<Double>();
        Pattern LAT_LNG = Pattern.compile("([-\\d.]+),([-\\d.]+)");
        Matcher matcher = LAT_LNG.matcher(fieldLocation);
        while(matcher.find())
        {
            double lat = Double.parseDouble(matcher.group(1));
            double lng = Double.parseDouble(matcher.group(2));
            longitudelist.add(lng);
        }
        return longitudelist;
    }

    //Επιτστρέφει λίστα lat/lng με τα σημεία του χωραφιού που έχει επιλέξει ο χρήστης
    public ArrayList<LatLng> retrievePolygon(String fieldlocation)
    {
        ArrayList<LatLng> lista = new ArrayList<>();
        ArrayList<Double> latlist = retrieveFieldLatitude(fieldlocation);
        ArrayList<Double> lnglist = retrieveFieldLongitude(fieldlocation);
        for(int i =0;i<lnglist.size()-1;i++)
        {
            LatLng point = new LatLng(latlist.get(i),lnglist.get(i));
            lista.add(point);
        }
        return lista;
    }

    //Επιστρέφει λίστα lat/lng με τα poylines(AB Lines)
    public ArrayList<LatLng> retrievePolylines(String fieldPattern)
    {
        ArrayList<LatLng> lista = new ArrayList<>();
        ArrayList<Double> latlist = retrieveFieldLatitude(fieldPattern);
        ArrayList<Double> lnglist = retrieveFieldLongitude(fieldPattern);
        for(int i =0;i<lnglist.size();i++)
        {
            LatLng point = new LatLng(latlist.get(i),lnglist.get(i));
            lista.add(point);
        }
        return lista;
    }
}
