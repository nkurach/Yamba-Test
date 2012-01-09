package com.agentsolid.nk.yamba;

import winterwell.jtwitter.Twitter.Status;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service implements Runnable {
	
	private static final String TAG = UpdaterService.class.getSimpleName();
	private Thread thread;
	private boolean runFlag;
	public static final String NEW_TIMELINE_DATA = UpdaterService.class.getPackage().getName() + ".NEW_TIMELINE_DATA";
	private NotificationManager notificationManager;
	private Notification notification;
	
	@Override
	public IBinder onBind(Intent intent) {
		//ignore
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate'd");
		this.thread = new Thread(this, "UpdaterService-Thread");
		this.notificationManager = (NotificationManager) super.getSystemService(Context.NOTIFICATION_SERVICE);
		this.notification = new Notification(android.R.drawable.stat_notify_chat, "", 0);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		synchronized(this){
			if (!this.runFlag){
				this.runFlag = true;
				this.thread.start();
				this.getApp().setServiceStarted(true);
			}
		}
		Log.d(TAG, "onStart'd");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.runFlag = false;
		this.thread.interrupt();
		this.thread = null;
		this.getApp().setServiceStarted(false);
		Log.d(TAG, "onDestroy'ed");
	}
	
	private YambaApplication getApp(){
		return (YambaApplication) super.getApplication();
	}
	
	private void sendNotification(int newStatusCount){
		if (this.getApp().isReceiveNotifications()) {
			Log.d(TAG, "Sending notification");
			PendingIntent pendingIntent = PendingIntent.getActivity(this, -1,
					new Intent(this, TimelineActivity.class),
					PendingIntent.FLAG_UPDATE_CURRENT);

			this.notification.when = System.currentTimeMillis();
			this.notification.flags |= Notification.FLAG_AUTO_CANCEL;
			this.notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			CharSequence notificationTitle = this
					.getText(R.string.timeline_updated_notification_title);
			CharSequence notificationSummary = this.getString(
					R.string.timeline_updated_notification_summary,
					newStatusCount);
			this.notification.setLatestEventInfo(this, notificationTitle,
					notificationSummary, pendingIntent);
			this.notificationManager.notify(0, this.notification);
		}
	}
	
	public void run() {
		while(this.runFlag){
			try{
				int newStatusCount = this.getApp().fetchTimeline();
				if(newStatusCount > 0){
					Intent i = new Intent(NEW_TIMELINE_DATA);
					i.putExtra("count", newStatusCount);
					Log.d(TAG, "Sending Broadcast of " + newStatusCount + " new updates");
					super.sendBroadcast(i);
					this.sendNotification(newStatusCount);
				}
				
			}catch(Exception e){
				
			}
			// wait
			try{
				Thread.sleep(30000);
			}catch (InterruptedException e){
				break;
			}
		}
		
	}

	

}
