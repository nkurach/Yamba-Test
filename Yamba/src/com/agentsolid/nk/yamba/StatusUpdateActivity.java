package com.agentsolid.nk.yamba;


import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StatusUpdateActivity extends Activity implements OnClickListener,  TextWatcher {
    private static final int MAX_COUNT = 140;
    private static final String TAG = StatusUpdateActivity.class.getSimpleName();
    
	private EditText editText;
	private Button button;
	private TextView counter;
	private ColorStateList counterColor;
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_update);
        
        this.editText = (EditText) super.findViewById(R.id.status_update_edit_text);
        this.button = (Button) super.findViewById(R.id.status_update_button);
        this.counter = (TextView) super.findViewById(R.id.update_status_counter);
        
        this.counter.setText(String.valueOf(MAX_COUNT));
        this.counterColor = this.counter.getTextColors();
        //this.counter.setTextColor(getResources().getColor(R.color.counter_green));
        
        this.button.setOnClickListener(this);
        // this.editText.setOnKeyListener(this);
        this.editText.addTextChangedListener(this);

    }
    
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.getMenuInflater().inflate(R.menu.status_update, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_prefs:
			Log.d(TAG, "Prefs menu selected");
			super.startActivity(new Intent(this, PrefsActivity.class));
			return true;
		case R.id.menu_item_toggle_service:
			Intent i = new Intent(this, UpdaterService.class);
			if(this.getApp().isServiceStarted()){
				Log.d(TAG, "Stopping service");
				super.stopService(i);
			}else{
				Log.d(TAG, "Starting service");
				super.startService(i);
			}
			return true;
		case R.id.menu_item_timeline:
			Log.d(TAG, "Timeline menu selected");
			super.startActivity(new Intent(this, TimelineActivity.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		MenuItem item = menu.findItem(R.id.menu_item_toggle_service);
		if (this.getApp().isServiceStarted()) {
			item.setTitle(R.string.menu_item_stop_service_title);
			item.setIcon(R.drawable.menu_stop_service);
		} else {
			item.setTitle(R.string.menu_item_start_service_title);
			item.setIcon(R.drawable.menu_start_service);
		}
		return super.onMenuOpened(featureId, menu);
	}



	private YambaApplication getApp(){
		return (YambaApplication) super.getApplication();
	}

	public void onClick(View v) {
		if(v==this.button){
			StatusUpdateActivity.this.button.setEnabled(false);
			Toast.makeText(this, R.string.status_update_pending_message, Toast.LENGTH_SHORT).show();
			String status = this.editText.getText().toString();
			new AsyncTask<String, Void, Long>(){

				@Override
				protected Long doInBackground(String... params) {
					try{
						StatusUpdateActivity.this.button.setEnabled(true);
						long t = System.currentTimeMillis();
						StatusUpdateActivity.this.getApp().getTwitter().setStatus(params[0]);
						t = System.currentTimeMillis() - t;
						return t;
					}catch(Exception e){
						Log.e(TAG, "Failed to post update");
						return null;
					}
				}
				
				@Override
				protected void onPostExecute(Long result) {
					
					if (result == null) {
						Toast.makeText(StatusUpdateActivity.this,
								R.string.status_update_failed_message,
								Toast.LENGTH_SHORT).show();
					} else {
						StatusUpdateActivity.this.editText.setText("");
						String successMessage = StatusUpdateActivity.this
								.getString(
										R.string.status_update_success_message,
										result / 1000.00);
						Toast.makeText(StatusUpdateActivity.this,
								successMessage, Toast.LENGTH_SHORT).show();
						Log.d(TAG, "Posted status update in " + result + " ms");
					}
				}	
			}.execute(status);
		}
		
	}

	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		int mycount = MAX_COUNT-this.editText.length();
		String mycountstring = String.valueOf(mycount);
		this.counter.setText(mycountstring);
		if(mycount < 0){
			this.counter.setTextColor(Color.RED);
			this.button.setEnabled(false);
		}else{
			this.counter.setTextColor(this.counterColor);
			this.button.setEnabled(mycount < MAX_COUNT);
		}
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}

	

}