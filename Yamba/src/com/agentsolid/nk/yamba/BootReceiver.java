package com.agentsolid.nk.yamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	
	private static final String TAG = BootReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Receiving boot notification");
		if(((YambaApplication) context.getApplicationContext()).isStartOnBootRequested()){
			context.startService(new Intent(context, UpdaterService.class));
		}
	}

}
