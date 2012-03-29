package com.aquarariumMobile;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;
 
public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener  {
 
	public static final String IP_PREFERENCE = "ip";
	public static final String TIME_PREFERENCE = "tempo";
	
	
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.settings);
   }
   
   @Override
   protected void onResume() {
       super.onResume();
       getPreferenceScreen().getSharedPreferences()
               .registerOnSharedPreferenceChangeListener(this);
   }

   @Override
   protected void onPause() {
       super.onPause();
       getPreferenceScreen().getSharedPreferences()
               .unregisterOnSharedPreferenceChangeListener(this);
   }

   
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	   if (key.equals(IP_PREFERENCE)) {
    	   AquariumMobileActivity.setServiceIp(sharedPreferences.getString(IP_PREFERENCE, ""));
       }
       else if (key.equals(TIME_PREFERENCE)) {
    	   int interval = 0;
    	   String intervalStr = sharedPreferences.getString(TIME_PREFERENCE, "30");
    	   
    	   try{
    		   interval = Integer.parseInt(intervalStr);
    	   } catch(NumberFormatException e){
    		   showToast(R.string.number_error);
    	   }
    	   
    	   AquariumMobileActivity.setInterval(interval);
       }
   }
   
   private void showToast(int stringId){
		Toast.makeText(Preferences.this, getString(stringId), Toast.LENGTH_LONG).show();
	}
}
