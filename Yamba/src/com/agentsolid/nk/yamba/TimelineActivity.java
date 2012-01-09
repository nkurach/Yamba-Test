package com.agentsolid.nk.yamba;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class TimelineActivity extends ListActivity {
	
	private static final String TAG = TimelineActivity.class.getSimpleName();
	private static final String[] FROM_COLUMN_NAMES = {TimelineData.USER, TimelineData.CREATED_AT, TimelineData.MESSAGE};
	private static final int[] TO_VIEW_IDS = {R.id.timeline_user, R.id.timeline_created_at, R.id.timeline_message};
	private TimelineReceiver timelineReceiver;
	private IntentFilter timelineFilter;
	
	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.getMenuInflater().inflate(R.menu.timeline, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_refresh:
			Log.d(TAG, "Refresh menu selected");
			refreshTimeline();
			return true;
		case R.id.menu_status_update:
			Log.d(TAG, "status update menu selected");
			super.startActivity(new Intent(this, StatusUpdateActivity.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	private void refreshTimeline(){
		final ProgressDialog dialog = ProgressDialog.show(this, "", this.getText(R.string.timeline_updating_message), true);
		new AsyncTask<Void, Void, Integer>(){
			@Override
			protected Integer doInBackground(Void...params) {
				try{
					return TimelineActivity.this.getApp().fetchTimeline();
					
				}catch(Exception e){
					Log.e(TAG, "Failed to post update");
					return null;
				}
			}
			
			@Override
			protected void onPostExecute(Integer result) {
				dialog.dismiss();
				Toast.makeText(TimelineActivity.this, "Found " + result + " Updates", Toast.LENGTH_SHORT).show();
				if(result > 0){
					TimelineActivity.this.fillData();
				}
				
			}	
		}.execute();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);
		this.timelineReceiver = new TimelineReceiver();
		this.timelineFilter = new IntentFilter(UpdaterService.NEW_TIMELINE_DATA);
	}
	
	private void fillData(){
		Cursor cursor = this.getApp().getTimelineData().getAll();
		super.startManagingCursor(cursor);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.timeline_row, cursor, FROM_COLUMN_NAMES, TO_VIEW_IDS);
		adapter.setViewBinder(TIMELINE_CREATED_AT_VIEW_BINDER);
		super.setListAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		fillData();
		super.registerReceiver(this.timelineReceiver, this.timelineFilter);
		this.getApp().setReceiveNotifications(false);
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		super.unregisterReceiver(this.timelineReceiver);
	}

	private YambaApplication getApp(){
		return (YambaApplication) super.getApplication();
	}
	
	private class TimelineReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent){
			Log.d(TAG, "Timeline updated");
			TimelineActivity.this.fillData();
			/*SimpleCursorAdapter adapter = (SimpleCursorAdapter) TimelineActivity.this
					.getListAdapter();
			adapter.getCursor().requery();
			adapter.notifyDataSetChanged();
			*/
		}
	}
	
	private static final ViewBinder TIMELINE_CREATED_AT_VIEW_BINDER = new ViewBinder(){

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if(view.getId() == R.id.timeline_created_at){
				long createdAt = cursor.getLong(columnIndex);
				CharSequence createdAtString = DateUtils.getRelativeTimeSpanString(view.getContext(), createdAt);
				TextView createdAtView = (TextView) view;
				createdAtView.setText(createdAtString);
				return true;
			}else{
				return false;
			}
			
		}
	};
}
