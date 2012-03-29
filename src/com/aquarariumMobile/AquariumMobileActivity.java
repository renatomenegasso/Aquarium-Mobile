package com.aquarariumMobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aquarariumMobile.services.ArduinoServices;
import com.aquarariumMobile.services.ArduinoServices.DeviceStatus;

public class AquariumMobileActivity extends Activity {
	
	private ToggleButton btnLight;
    private ToggleButton btnPump;
    private ToggleButton btnHeater;
    private Button btnAlimentator;
    private Button btnRefresh;
    private TextView txtStatus;
    private TextView txtTemperature;
    SharedPreferences preferences;
    
    private NotificationManager mNotificationManager = null;
    private boolean checkAutoChanged = false;
    
	private static int interval = 10 * 1000;
    private static ArduinoServices arduino = new ArduinoServices();
    
    private RefreshHandler updater = new RefreshHandler();  
    
    class RefreshHandler extends Handler {  
      @Override  
      public void handleMessage(Message msg) {  
    	  AquariumMobileActivity.this.updateUI();
      }  
    
      public void sleep(long delayMillis) {  
        this.removeMessages(0);  
        sendMessageDelayed(obtainMessage(0), delayMillis);  
      }  
    };  
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        setServiceIp(preferences.getString(Preferences.IP_PREFERENCE, "127.0.0.1"));
        setInterval(Integer.parseInt(preferences.getString(Preferences.TIME_PREFERENCE, "30")));
        
        fillScreenElements();
        addScreenElementsEvents();
        updateUI(true);
        
        updater.sleep(interval);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       super.onCreateOptionsMenu(menu);
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.menu, menu);
       return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
       case R.id.settings:
          startActivity(new Intent(this, Preferences.class));
          return true;
       }
       return false;
    }
    
    public static void setServiceIp(String url){
    	arduino.setServiceUrl("http://" +url + "/?");
    }
    
    public static void setInterval(int seconds){
    	interval = seconds * 1000;
    }
    
	private void fillScreenElements(){
		this.txtStatus = (TextView)super.findViewById(R.id.lbl_service_status);
    	this.btnLight = (ToggleButton)super.findViewById(R.id.btnLight);
    	this.btnPump = (ToggleButton)super.findViewById(R.id.btnPump);
    	this.btnHeater = (ToggleButton)super.findViewById(R.id.btnHeater);
    	this.txtTemperature = (TextView)super.findViewById(R.id.lbl_current_temperature);
    	this.btnAlimentator = (Button)super.findViewById(R.id.btnTriggerAlimentator);
    	this.btnRefresh = (Button)super.findViewById(R.id.btnRefresh);
    	
    }
	
	public void updateUI(){
		updateUI(false);
	}
	
	public void updateUI(boolean isForced){
		boolean isOnlineBefore = arduino.isServiceOnline();
		
		float currentTemperature = arduino.recuperarTemperatura();
    	if(currentTemperature > 0)
    		txtTemperature.setText(currentTemperature + "ºC");
    	else
    		txtTemperature.setText("");
    	if(!arduino.isServiceOnline()){
    		txtStatus.setText(getString(R.string.msg_aquarium_online));
    		txtStatus.setTextColor(Color.rgb(0,255,0));
    		
    		mNotificationManager.cancel(R.string.msg_aquarium_offline);
    		
    		if(!isForced && !isOnlineBefore){
    			showNotification(R.string.msg_aquarium_online, R.string.msg_aquarium_online, R.string.msg_aquarium_online);
    		}
    		
    		handleCurrentDevicesStatus(isForced);
    	} else {
    		txtStatus.setText(getString(R.string.msg_aquarium_offline));
    		txtStatus.setTextColor(Color.rgb(255,0,0));
    		
    		mNotificationManager.cancel(R.string.msg_aquarium_online);
    		
    		if(!isForced && isOnlineBefore){
    			showNotification(R.string.msg_aquarium_offline, R.string.msg_aquarium_offline, R.string.msg_aquarium_offline);
    		}
    	}
    	
    	if(!isForced){
    		updater.sleep(interval);
    	}
	}
	
	private void handleCurrentDevicesStatus(boolean isForced){
		checkAutoChanged = true;
    	
		boolean oldPumpStatus = btnPump.isChecked();
		boolean oldLightStatus = btnLight.isChecked();
		boolean oldHeaterStatus = btnLight.isChecked();
		
    	btnPump.setChecked(DeviceStatus.ON.equals(arduino.statusBomba()));
    	btnLight.setChecked(DeviceStatus.ON.equals(arduino.statusLuz()));
    	btnHeater.setChecked(DeviceStatus.ON.equals(arduino.statusAquecedor()));
    	
    	checkAutoChanged = false;
    	
    	if(isForced){
    		return;
    	}
    	
    	if(oldLightStatus != btnLight.isChecked()){
    		int titleId = R.string.light;
    		int contentId = (oldLightStatus == false) ? R.string.light_on : R.string.light_off;
    		
    		showNotification(titleId, contentId, titleId);
    	}
    	
    	if(oldPumpStatus != btnPump.isChecked()){
    		int titleId = R.string.pump;
    		int contentId = (oldPumpStatus == false) ? R.string.pump_on : R.string.pump_off;
    		
    		showNotification(titleId, contentId, titleId);
    	}
    	
    	if(oldHeaterStatus != btnHeater.isChecked()){
    		int titleId = R.string.heater;
    		int contentId = (oldHeaterStatus == false) ? R.string.heater_on : R.string.heater_off;
    		
    		showNotification(titleId, contentId, titleId);
    	}
	}
	
	private void addScreenElementsEvents() {
		this.btnLight.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(checkAutoChanged)
					return;
				
				Boolean operationSuccess = false;
				if(isChecked){
					operationSuccess = arduino.ascenderLuz();
				} else {
					operationSuccess = arduino.apagarLuz();
				}
				
				if(!operationSuccess){
					btnLight.setChecked(!isChecked);
					showDialog(getString(R.string.light), getString(R.string.msg_communication_error));
				} else {
					showToast(R.string.operation_success);
				}
			}	
		});
		
		this.btnPump.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(checkAutoChanged)
					return;
				
				showNotification(R.string.msg_aquarium_offline, R.string.msg_aquarium_offline, R.string.msg_aquarium_offline);
	    		
			}	
		});
		
		this.btnHeater.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(checkAutoChanged)
					return;
				
				Boolean operationSuccess = false;
				if(isChecked){
					operationSuccess = arduino.ligarAquecedor();
				} else {
					operationSuccess = arduino.desligarAquecedor();
				}
				
				if(!operationSuccess){
					btnHeater.setChecked(!isChecked);
					showDialog(getString(R.string.heater), getString(R.string.msg_communication_error));
				} else {
					showToast(R.string.operation_success);
				}
			}	
		});

		this.btnAlimentator.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!arduino.dispararAlimentador()){
					showDialog(getString(R.string.alimentator), getString(R.string.msg_communication_error));
				} else {
					showToast(R.string.alimentator_trigger_ok);
				}
			}
		});
		
		this.btnRefresh.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				updateUI(true);
				showToast(R.string.data_updated);
			}
		});
	}
	
	private void showToast(int stringId){
		Toast.makeText(AquariumMobileActivity.this, getString(stringId), Toast.LENGTH_LONG).show();
	}
	
	private void showDialog(String title, String content){
		AlertDialog.Builder dialogo = new AlertDialog.Builder(AquariumMobileActivity.this); 
		dialogo.setTitle(title); 
		dialogo.setMessage(content); 
		dialogo.setNeutralButton("OK", null);
		
		dialogo.show();
	}
	
	private void showNotification(int titleId, int contentId, int notificationId){
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();         // notification time
		Context context = getApplicationContext();      // application Context

		Intent notificationIntent = new Intent(this, AquariumMobileActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(AquariumMobileActivity.this, 0, notificationIntent, 0);

		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(icon, getString(titleId), when);
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.setLatestEventInfo(context, getString(titleId), getString(contentId), contentIntent);
		mNotificationManager.notify(notificationId, notification);
	}
}