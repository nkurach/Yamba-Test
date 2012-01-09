package com.agentsolid.nk.yamba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TimelineData {
	
	private static final String TAG = TimelineData.class.getSimpleName();
	private static final String TIMELINE_TABLE = "timeline";
	public static final String ID = "_id";
	public static final String USER = "user";
	public static final String MESSAGE = "message";
	public static final String CREATED_AT = "created_at";
	public static final String GET_ALL_ORDER = CREATED_AT + " DESC";
	private static final String[] LATEST_CREATED_AT_PROJECTION = {"max("+ CREATED_AT + ")"};
	
	private static final class DBHelper extends SQLiteOpenHelper{
		
		private static final String DATABASE = "timeline.db";
		private static final int VERSION = 1;
		
		
		public DBHelper(Context context) {
			super(context, DATABASE, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "Creating Database");
			db.execSQL("CREATE TABLE " + TIMELINE_TABLE + " (" + ID
					+ " int primary key, " + CREATED_AT + " int, " + MESSAGE
					+ " text, " + USER + " text)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "Upgrading Database " + DATABASE + " from  " + oldVersion + " to " + newVersion);
			db.execSQL("DROP TABLE IF EXISTS " + TIMELINE_TABLE);
			this.onCreate(db);
		}
		
	}
	
	private DBHelper dbHelper;
	
	public TimelineData(Context context){
		this.dbHelper = new DBHelper(context);
	}
	
	public void add(ContentValues values){
		Log.d(TAG, "Inserting " + values);
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		try{
			db.insertWithOnConflict(TIMELINE_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		}finally{
			db.close();
		}
	}
	
	public long getLatestCreatedAt(){
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try{
			Cursor c = db.query(TIMELINE_TABLE, LATEST_CREATED_AT_PROJECTION, null, null, null, null, null);
			try{
				return c.moveToFirst() ? c.getLong(0) : Long.MIN_VALUE;
			}finally{
				c.close();
			}
		}finally{
			db.close();
		}
	}
	
	public Cursor getAll(){
		Log.d(TAG, "Getting All");
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TIMELINE_TABLE, null, null, null, null, null, GET_ALL_ORDER);
	}
	
	public void close(){
		this.dbHelper.close();
	}
	
}
