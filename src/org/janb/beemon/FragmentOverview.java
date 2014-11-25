package org.janb.beemon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.duenndns.ssl.MemorizingTrustManager;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
 
public class FragmentOverview extends Fragment implements ResultsListener {
  
	AsyncTask<String, Integer, String> mytask;
	  TextView tempTV, weightTV, temptimeTV, weighttimeTV, templabelTV, weightlabelTV, lastupdateTV, lastupdatelabelTV;
	  ImageView tempIMG, weightIMG;
	  View v;
	  private ProgressDialog dialog;
	  FragmentManager fm;
	  	FragmentTransaction ft;
	  	String command;
	  	
 public FragmentOverview(String command) {
	 this.command = command;
		}

@Override
 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
  View view = inflater.inflate(R.layout.activity_fragment_overview, new LinearLayout(getActivity().getApplicationContext()), false);
  v = view;
	getActivity().setTitle(R.string.title_section1);
	  tempTV = (TextView) v.findViewById(R.id.tempTV);
	  weightTV = (TextView)v.findViewById(R.id.weightTV);
	  temptimeTV = (TextView)v.findViewById(R.id.temptimeTV);
	  weighttimeTV = (TextView)v.findViewById(R.id.weighttimeTV);
	  lastupdateTV = (TextView)v.findViewById(R.id.lastupdateTV);
	  tempIMG = (ImageView)v.findViewById(R.id.tempIMG);
	  weightIMG = (ImageView)v.findViewById(R.id.weightIMG);
	  fm = getActivity().getFragmentManager();
  	ft = fm.beginTransaction();
  setHasOptionsMenu(true);
	dialog = new ProgressDialog(getActivity());
	dialog.setCancelable(false);
	dialog.setCanceledOnTouchOutside(false);
	dialog.setMessage(getString(R.string.DialogLoading));
	if (command.equals("refresh") || command.equals("BackFromError")) { startSync(); } else {
		screenSetup();	
	}
  
  return view;
 }

 private void screenSetup() {
	 SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
	 tempTV.setText(mPrefs.getString("temp", getString(R.string.dummy))+"°C");
	 temptimeTV.setText(mPrefs.getString("temptime", getString(R.string.dummy)));
	 weightTV.setText(mPrefs.getString("weight", getString(R.string.dummy))+" kg");
	 weighttimeTV.setText(mPrefs.getString("weighttime", getString(R.string.dummy)));
}

@Override
 public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
     super.onCreateOptionsMenu(menu, inflater);
     menu.clear();
     inflater.inflate(R.menu.fragment_overview, menu);
 }
 
 public void onResultsSucceeded(String result) {
	 dialog.dismiss();
	 if (result.equals("sslerror")){
		 ft.replace(R.id.container, new FragmentConnectError("overview"), "FRAGMENT_CONNECT_ERROR");
		 ft.commit(); 
	 } else {
		 parseJSON(result);
	 }
}

public void parseJSON(String jsondata) {
	String sensor = null;
	String value = null;
	String timestamp = null;
	JSONArray array = null;
	try {
		array = new JSONArray(jsondata);
	} catch (JSONException e) {
		//e.printStackTrace();
		getLastUpdate();
	}
	if (array != null){
				
		for (int i = 0; i < array.length(); i++) {
			JSONObject row = null;
			try {
				row = array.getJSONObject(i);
			} catch (JSONException e) {
				Log.i("BeeMonitor", "JSON ARRAY EXCEPTION");
				//e.printStackTrace();
			}
			try {
				sensor = row.getString("sensor");
				value = row.getString("value");
				timestamp = row.getString("timestamp");
				saveData(sensor, value, timestamp);
				//screenSetup();
				setValues(sensor, value, timestamp);
			} catch (JSONException e) {
				Log.i("BeeMonitor", "JSON OBJECT EXCEPTION");
				//e.printStackTrace();
			}
		}
		saveLastUpdate();
		getLastUpdate();
	} else {
		setVisibility(false);
		
    	ft.replace(R.id.container, new FragmentConnectError("overview"), "FRAGMENT_CONNECT_ERROR");
    	ft.commit(); 
		Log.i("BeeMonitor - Overview", "Connection to server failed");
	}

}

private void setValues() {
	// TODO Auto-generated method stub
	
}

public void onResume(){
	   super.onResume();
	   setVisibility(true);  	   
   }

public void onPause(){
	   super.onPause();
	   setVisibility(false);
   }

public void onStart() {
	   super.onStart();
	   getLastUpdate();
   }

public void startSync() {
	dialog.show();
	   AsyncTask<String, Integer, String> task = new MTMAsyncTask(getActivity());
   	((MTMAsyncTask) task).setOnResultsListener(this);
   	task.execute("getjson_current.php", "");
}

private void saveLastUpdate(){
	try {
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
       Calendar cal = Calendar.getInstance();
       Editor editor = mPrefs.edit();
       String minute = Integer.toString(cal.get(Calendar.MINUTE));
       if (cal.get(Calendar.MINUTE) < 10){
			minute = "0" + minute;
		}
		String lastupdate = cal.get(Calendar.DAY_OF_MONTH) + "." + cal.get(Calendar.MONTH) + "." + cal.get(Calendar.YEAR) + " / " + cal.get(Calendar.HOUR) + ":" + minute + " Uhr";
		editor.putString("last_update", lastupdate);
		editor.commit();
	} catch (NullPointerException e){
		Log.i("BeeMonitor","saveLastUpdateException");
	}
   
   }

private void setVisibility(Boolean state){
	try {
       SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
       Editor editor = mPrefs.edit();
       editor.putBoolean("visible", state);
       editor.commit();
	} catch (NullPointerException e){
		Log.i("BeeMonitor","setVisibilityException");
	}
   }

private void getLastUpdate(){
	try {
       SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
       TextView lastupdateTV = (TextView)v.findViewById(R.id.lastupdateTV);
       lastupdateTV.setText(mPrefs.getString("last_update", getString(R.string.dummy)));
	} catch (NullPointerException e){
		Log.i("BeeMonitor","getLastUpdateException");
	}
   }

private String StrtoDate(String timestamp){
	String datetime = "NO DATA AVAILABLE";
	Calendar cal = Calendar.getInstance();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
	try {
		cal.setTime(sdf.parse(timestamp));
		String minute = Integer.toString(cal.get(Calendar.MINUTE));
		if (cal.get(Calendar.MINUTE) < 10){
			minute = "0" + minute;
		}
		datetime = cal.get(Calendar.DAY_OF_MONTH) + "." + cal.get(Calendar.MONTH) + "." + cal.get(Calendar.YEAR) + " / " + cal.get(Calendar.HOUR_OF_DAY) + ":" + minute + " Uhr";
	} catch (ParseException e) {
		e.printStackTrace();
	}
	return datetime;
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
        return true;
    }
    if (item.getItemId() == R.id.menu_load_overview) {
		startSync();
        return true;
    }
    return super.onOptionsItemSelected(item);
}

public class SensorData {
		 
	    private String value;
	    private String timestamp;
	 
	    public SensorData(String value, String timestamp) {
	        super();
	        this.value = value;
	        this.timestamp = timestamp;
	    }
	    public String getTimestamp() {
	    	return this.timestamp;
		}
		public String getValue(){
	    	return this.value;
		}
	}

private void saveData(String sensor, String value, String timestamp){
	try {
		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		Editor editor = mPrefs.edit();
		editor.putString(sensor, value);
		editor.putString(sensor+"time", StrtoDate(timestamp));
       editor.commit();
	} catch (NullPointerException e){
		Log.i("BeeMonitor","saveDataException");
	}
   
   }

private void setValues(String sensor, String value, String timestamp) {

	if (sensor.equals("temp")){
		tempTV.setText(value + "°C");
		temptimeTV.setText(StrtoDate(timestamp));
	}
	if (sensor.equals("weight")){
		weightTV.setText(value + " kg");
		weighttimeTV.setText(StrtoDate(timestamp));
	}
}


}
