package org.janb.hivemonitor;

import java.util.ArrayList;

import org.janb.hivemonitor.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
 
public class FragmentHistory extends ListFragment implements ResultsListener {
  
	AsyncTask<String, Integer, String> mytask;
	
private String sensor;
private ProgressDialog dialog;
FragmentManager fm;
FragmentTransaction ft;
	
	public FragmentHistory(String sensor){
	    this.sensor=sensor;
	}
	
 @Override
 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
  View view = inflater.inflate(R.layout.fragment_history, new LinearLayout(getActivity().getApplicationContext()), false);
	FragmentManager fm = getFragmentManager();
	FragmentTransaction ft = fm.beginTransaction();
  dialog = new ProgressDialog(getActivity());
	dialog.setCancelable(false);
	dialog.setCanceledOnTouchOutside(false);
	dialog.setMessage(getString(R.string.DialogLoading));
	if (sensor.equals("temp")) { getActivity().setTitle(R.string.title_section2); }
	if (sensor.equals("weight")) { getActivity().setTitle(R.string.title_section3); }
  setHasOptionsMenu(true);
   	if (sensor != null){ 		
   		startSync();
   	}
  return view;
 }

 @Override
 public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
     super.onCreateOptionsMenu(menu, inflater);
     menu.clear();
     inflater.inflate(R.menu.history, menu);
 }
 
 public void onResultsSucceeded(String result) {
	 dialog.dismiss();
 	ArrayList<SensorData> list = parseJSON(result);
 // 1. pass context and data to the custom adapter
    MyAdapter adapter = new MyAdapter(getActivity().getApplicationContext(), list);

    //2. setListAdapter
    setListAdapter(adapter);
 	}

	public ArrayList<SensorData> parseJSON(String jsondata) {
		ArrayList<SensorData> items = new ArrayList<SensorData>();
		String value = null;
		String timestamp = null;
		JSONArray array = null;
		SensorData sensordata = null;
		try {
			array = new JSONArray(jsondata);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (array != null){
			for (int i = 0; i < array.length(); i++) {
				JSONObject row = null;
				try {
					row = array.getJSONObject(i);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				try {
					value = row.getString("value");
					timestamp = row.getString("timestamp");
					 sensordata = new SensorData(value, timestamp);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				items.add(sensordata);
			}
		} else {
	    	ft.replace(R.id.container, new FragmentConnectError(sensor), "FRAGMENT_CONNECT_ERROR");
        	ft.commit(); 
			Log.i("BeeMonitor", "No data received from server");
		}
		return items;
	
	}

	
	public void onStop() {
		super.onStop();
		//mytask.cancel(true);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (item.getItemId() == R.id.menu_load_history) {
        	startSync();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private void startSync() {
		dialog.show();
		   AsyncTask<String, Integer, String> task = new MTMAsyncTask(getActivity());
	   	((MTMAsyncTask) task).setOnResultsListener(this);
	   	mytask = task.execute("getjson_values.php", sensor);
		
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
}
