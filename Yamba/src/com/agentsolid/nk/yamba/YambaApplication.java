package com.agentsolid.nk.yamba;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class YambaApplication extends Application implements OnSharedPreferenceChangeListener {
	
	private static final String TAG = YambaApplication.class.getSimpleName();
	private Twitter twitter;
	private SharedPreferences prefs;
	private TimelineData timelineData;
	private boolean serviceStarted;
	private boolean receiveNotifications;
	
	public boolean isServiceStarted() {
		return serviceStarted;
	}

	public void setServiceStarted(boolean serviceStarted) {
		this.serviceStarted = serviceStarted;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Application Created");
		
		 this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
	     this.prefs.registerOnSharedPreferenceChangeListener(this);
	     this.timelineData = new TimelineData(this);
	     this.receiveNotifications = true;
	}
	
	public boolean isReceiveNotifications() {
		return receiveNotifications;
	}

	public void setReceiveNotifications(boolean receiveNotifications) {
		this.receiveNotifications = receiveNotifications;
	}

	public boolean isStartOnBootRequested(){
		return this.prefs.getBoolean("startOnBoot", false);
	}
	
	public TimelineData getTimelineData() {
		return timelineData;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "Application Terminated");
	}
	
	public Twitter getTwitter(){
		if(this.twitter == null){
			this.twitter = new Twitter(prefs.getString("username", null), prefs.getString("password", null));
        	this.twitter.setAPIRootUrl(prefs.getString("url", null));
		}
		return this.twitter;
	}
	
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		this.twitter=null;
	}
	
	public int fetchTimeline(){
		Log.d(TAG, "Fetching Timline");
		long latestCreatedAt = timelineData.getLatestCreatedAt();
		int newStatusCount = 0;
		//pull from twitter
		ContentValues values = new ContentValues();
		for(Status status:this.getTwitter().getFriendsTimeline()){
			long createdAt = status.getCreatedAt().getTime();
			if(createdAt > latestCreatedAt){
				Log.d(TAG, "Got Status: " + status.getUser().getName() + " said " + status.getText());
				values.put(TimelineData.ID, status.getId());
				values.put(TimelineData.CREATED_AT, createdAt);
				values.put(TimelineData.MESSAGE, status.getText());
				values.put(TimelineData.USER, status.getUser().getName());
				timelineData.add(values);
				values.clear();
				newStatusCount++;
			}
		}
		return newStatusCount;
	
	}
	
}
