package org.janb.beemon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.duenndns.ssl.MemorizingTrustManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

public class PrefsFragment extends PreferenceFragment implements OnPreferenceClickListener, ResultsListener{
	String SERVER_URL;
	public final String EXTRA_MESSAGE = "message";
    public final String PROPERTY_REG_ID = "registration_id";
    private final String PROPERTY_APP_VERSION = "appVersion";
    private final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "461656059238";
    static final String TAG = "GCM";
    String regid;
    
    GoogleCloudMessaging gcm;
	SharedPreferences mPreferences;
	SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;
	CheckBoxPreference notifyPref;
	EditTextPreference contactPref, serverpref;
	Button certManageBTN;
	Context context = getActivity();
	Activity act = getActivity();
	public String msg;
	private ProgressDialog dialog;
	Boolean originalstate;
	MemorizingTrustManager mtm;
	private CharSequence syncmsg;
	
	@Override
public void onCreate(Bundle savedInstanceState) {
		mtm = new MemorizingTrustManager(getActivity().getApplicationContext());
	super.onCreate(savedInstanceState);
	act = getActivity();
	ScreenSetup();
	}

public void onResume(Bundle savedInstanceState) {
	super.onResume();
	act = getActivity();
}

private void ScreenSetup() {

		 addPreferencesFromResource(R.xml.preferences);
		 mPreferences = getPreferenceScreen().getSharedPreferences();
		 notifyPref = (CheckBoxPreference) getPreferenceScreen().findPreference(
		         "notification_preference");
		contactPref = (EditTextPreference) getPreferenceScreen().findPreference("contact_preference");
		serverpref = (EditTextPreference) getPreferenceScreen().findPreference("server_preference");
		Preference button = (Preference)findPreference("certManageBTN");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		                @Override
		                public boolean onPreferenceClick(Preference arg0) { 
		                	onManage(getView());
		                    return true;
		                }
		            });
		
		serverpref.setSummary(serverpref.getText());
		contactPref.setSummary(contactPref.getText());
		
		syncmsg = getString(R.string.DialogLoading);
		
		if (contactPref.getText() == null) { contactPref.setText(""); contactPref.setSummary(getString(R.string.nameEnter)); }
		if (contactPref.getText().isEmpty()) { contactPref.setSummary(getString(R.string.nameEnter)); }
		if (serverpref.getText() == null) { serverpref.setText("raspberrypi.home"); serverpref.setSummary(getString(R.string.serverEnter)); }
		if (serverpref.getText().isEmpty()) { serverpref.setText("raspberrypi.home"); serverpref.setSummary(getString(R.string.serverEnter)); }
		
		
		notifyPref.setChecked(isRegistered());
		notifyPref.setOnPreferenceClickListener(this);
		contactPref.setEnabled(!isRegistered());
		
		 mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		     

			public void onSharedPreferenceChanged(SharedPreferences prefs,
		             String key) {
		         if (key.equals("contact_preference")) {
		        	 if (!contactPref.getText().isEmpty()){
	        			 contactPref.setSummary(contactPref.getText());
	        		 } else{
	        			 contactPref.setSummary(getString(R.string.nameEnter));
	        		 }
		         }
		         if (key.equals("server_preference")) {
		        	 if (!serverpref.getText().isEmpty()){
		        		 serverpref.setSummary(serverpref.getText());
	        		 } else{
	        			 serverpref.setSummary(getString(R.string.serverEnter));
	        			 serverpref.setText("raspberrypi.home");
	        		 }
       if (key.equals("server_preference")){
		        			startSync();
		        		}

		         }
		     }
		
		 };
		 mPreferences.registerOnSharedPreferenceChangeListener(mPrefListener);
		 checkPlayServices();
}

private SharedPreferences getGCMPreferences() {
	return PreferenceManager.getDefaultSharedPreferences(act);
}



private int getAppVersion() {
    try {
        PackageInfo packageInfo = act.getPackageManager()
                .getPackageInfo(act.getPackageName(), 0);
        return packageInfo.versionCode;
    } catch (NameNotFoundException e) {
        // should never happen
        throw new RuntimeException("Could not get package name: " + e);
    }
}





private boolean checkPlayServices() {
    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(act);
    if (resultCode != ConnectionResult.SUCCESS) {
        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
            GooglePlayServicesUtil.getErrorDialog(resultCode, act, PLAY_SERVICES_RESOLUTION_REQUEST).show();
        } else {
            Log.i("PLAY SERVICES", "This device is not supported.");
        }
        return false;
    } else {
    	return true;
    }


}


private void storeRegistrationId(Context context, String regId) {
    final SharedPreferences prefs = getGCMPreferences();
    int appVersion = getAppVersion();
    appVersion = 2;
    Log.i(TAG, "Saving regId on app version " + appVersion);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(PROPERTY_REG_ID, regId);
    editor.putInt(PROPERTY_APP_VERSION, appVersion);
    editor.commit();
}

public Boolean isRegistered() {
	
    SharedPreferences prefs = getGCMPreferences();
    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
    if (registrationId.isEmpty()) {
        return false;
    } else {
    	return true;
    }
}


public String getRegistrationId() {
    SharedPreferences prefs = getGCMPreferences();
    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
    if (registrationId.isEmpty()) {
        Log.i(TAG, "Registration not found.");
        return "";
    }
    
    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
    int currentVersion = getAppVersion();
    if (registeredVersion != currentVersion) {
        Log.i(TAG, "App version changed.");
        return "";
    }
    return registrationId;
}





class RegisterService extends AsyncTask<String,String,String>{
		@Override
		protected String doInBackground(String ... params) {
		    SharedPreferences prefs = getGCMPreferences();
		    SERVER_URL = prefs.getString("server_preference", "raspberrypi.home");
			
			if (gcm == null) {
				gcm = GoogleCloudMessaging.getInstance(act);
			}
			try {
				regid = gcm.register(SENDER_ID);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			msg = MTMRegDereg(params[0], params[1]);
            return msg;
        }
		
		@Override
	    protected void onPreExecute() {
	        dialog.show();
	    }
		
		@Override
        protected void onPostExecute(String msg) {
			if (!msg.equals("unregistersuccess")){
				if (dialog.isShowing()) {
	            dialog.dismiss();
				}
			}

			GCMregdereg(msg);
			RegisterQueryDialog(msg);
        }
		
		private void GCMregdereg(String msg) {
			if (msg.equals("registersuccess")){
				storeRegistrationId(act, regid);
			}
		
			if (msg.equals("unregistersuccess")){
				new RegisterService().execute("", "gcmunregister");
			}
			
		}

		private void RegisterQueryDialog(String msg) {
			if (msg.equals("registersuccess")){
	       		//Toast.makeText(act, getString(R.string.registerSuccess), Toast.LENGTH_SHORT).show();
				serverpref.setIcon(R.drawable.ic_action_accept);
	       		notifyPref.setChecked(true);
	       		contactPref.setEnabled(false);
	       	} else if (msg.equals("registererror")){
	       		//Toast.makeText(act, getString(R.string.registerFailed), Toast.LENGTH_SHORT).show();
	       		serverpref.setIcon(R.drawable.ic_action_cancel);
	       		notifyPref.setChecked(false);
	       		contactPref.setEnabled(true);
			} else if (msg.equals("unregistersuccess")){
				//Toast.makeText(act, getString(R.string.unregisterSuccess), Toast.LENGTH_SHORT).show();
				serverpref.setIcon(R.drawable.ic_action_accept);
				notifyPref.setChecked(false);
	       		contactPref.setEnabled(true);
			} else if (msg.equals("unregistererror")){
				//Toast.makeText(act, getString(R.string.unregisterFailed), Toast.LENGTH_LONG).show();
				serverpref.setIcon(R.drawable.ic_action_cancel);
				notifyPref.setChecked(true);
				contactPref.setEnabled(false);
			} else if (msg.equals("gcmunregister")){
				removeRegIdValue();
			} else if (msg.equals("nodata")){
				Toast.makeText(act, getString(R.string.ConnectError), Toast.LENGTH_SHORT).show();
				notifyPref.setChecked(originalstate);
			} else {
				Toast.makeText(act, getString(R.string.serverError), Toast.LENGTH_SHORT).show();
				Log.i("BeeMonitor", "Unexpected response: " + msg);
				notifyPref.setChecked(originalstate);
			}
			
		}

		private void removeRegIdValue() {
			final SharedPreferences prefs = getGCMPreferences();
    		prefs.edit().remove(PROPERTY_REG_ID).commit();
			
		}

		protected void onProgressUpdate(Integer... progress){
		}


}

public boolean isAlpha(String name) {
    return name.matches("[a-zA-Z]+");
}

@Override
public boolean onPreferenceClick(Preference preference) {
	String key= preference.getKey();
	if (key.equals("notification_preference")){
		originalstate = !notifyPref.isChecked();
		notificationChange();
	}
	return false;
	
}

private void notificationChange() {
	Boolean missing = false;

	if (contactPref.getText().isEmpty() || !isAlpha(contactPref.getText())){
		Toast.makeText(act, getString(R.string.nameError), Toast.LENGTH_SHORT).show();
		
		contactPref.setIcon(R.drawable.ic_action_warning);
	   	 notifyPref.setChecked(false);
	   	 missing = true;
	}
	
	if (!missing){
		contactPref.setIcon(R.drawable.ic_action_person);
		dialog = new ProgressDialog(act);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		
		if (!isRegistered()){
			dialog.setMessage(getString(R.string.DialogRegister));
			new RegisterService().execute(contactPref.getText(), "register");
		} else {
			dialog.setMessage(getString(R.string.DialogUnregister));
			new RegisterService().execute("","unregister");
		}	
	}


}


public void onResultsSucceeded(String result) {
if (result.equals("nodata") || result.equals("sslerror")){
	serverpref.setIcon(R.drawable.ic_action_cancel);
} else {
	serverpref.setIcon(R.drawable.ic_action_accept);
}
}

public void startSync() {
       AsyncTask<String, Integer, String> task = new MTMAsyncTask(act);
   	((MTMAsyncTask) task).setOnResultsListener(this);
   	task.execute("getjson_current.php", "");
}

/** React on the "Manage Certificates" button press. */
	public void onManage(View view) {
		final ArrayList<String> aliases = Collections.list(mtm.getCertificates());
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(act, android.R.layout.select_dialog_item, aliases);
		new AlertDialog.Builder(act).setTitle(getString(R.string.certDialog))
				.setNegativeButton(android.R.string.cancel, null)
				.setAdapter(adapter, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								String alias = aliases.get(which);
								mtm.deleteCertificate(alias);
							} catch (KeyStoreException e) {
								e.printStackTrace();
							}
						}
					})
				.create().show();
	}

private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
{
    StringBuilder result = new StringBuilder();
    boolean first = true;

    for (NameValuePair pair : params)
    {
        if (first)
            first = false;
        else
            result.append("&");

        result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
        result.append("=");
        result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
    }

    return result.toString();
}

private String MTMRegDereg(String name, String command) {
			if (command.equals("gcmunregister")){
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(act);
				}
				try {
					gcm.unregister();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return "gcmunregister";
			} else {
				String url = "";
				if (command.equals("register")){
					url = "https://" + SERVER_URL + "/beemonitor/c2dm/register.php";
				}
				if (command.equals("unregister")){
					url = "https://" + SERVER_URL + "/beemonitor/c2dm/unregister.php";
				}
			
			String result = "nodata";
			
			try {
				// set location of the keystore
				MemorizingTrustManager.setKeyStoreFile("private", "sslkeys.bks");
				// register MemorizingTrustManager for HTTPS
				SSLContext sc = SSLContext.getInstance("TLS");
				mtm = new MemorizingTrustManager(act);
				sc.init(null, new X509TrustManager[] { mtm },
				new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier(
				mtm.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()));
				// disable redirects to reduce possible confusion
				HttpsURLConnection.setFollowRedirects(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
			    URL u = new URL(url);
				HttpsURLConnection conn = (HttpsURLConnection) u.openConnection();
				conn.setReadTimeout(10000);
				conn.setConnectTimeout(15000);
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("regId", regid));
				params.add(new BasicNameValuePair("name", name));

				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
				        new OutputStreamWriter(os, "UTF-8"));
				writer.write(getQuery(params));
				writer.flush();
				writer.close();
				os.close();

				conn.connect();
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            StringBuilder sb = new StringBuilder();
	            String line;
	            while ((line = br.readLine()) != null) {
	                //sb.append(line+"\n");
	                sb.append(line);
	            }
	            br.close();
	            result = sb.toString();
				//result = conn.getContent().toString();
				Log.i("Response", conn.getContent().toString());
				conn.disconnect();
			} 	catch (SSLHandshakeException e) {
					Log.i("BeeMonitor", "SSL Handshake failed");
					result = "sslerror";
				} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
	return result;
			}

}
}