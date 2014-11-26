package org.janb.hivemonitor;

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
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.X509TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import de.duenndns.ssl.MemorizingTrustManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

class MTMAsyncTask extends AsyncTask<String, Integer, String>{
	
	private String SERVER_URL;
	private Activity activity;
	MemorizingTrustManager mtm;
	
	public MTMAsyncTask(Activity activity){
	    this.activity = activity;
	}
	   
	ResultsListener listener;
	public void setOnResultsListener(ResultsListener listener) {
		this.listener = listener;
	}
	
	@Override
	protected String doInBackground(String... params) {
		// 	TODO Auto-generated method stub
		String response = MTMgetXML(params[0], params[1]);
		return response;
	}
	@Override
	protected void onPostExecute(String result){
		if (result == null) {
			Log.i("BeeMonitor", "Serverantwort war leer. Fehler?");
		}
		
		listener.onResultsSucceeded(result);

	}


	protected void onProgressUpdate(Integer... progress){
	}

	public String MTMgetXML(String url, String sensor) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
	    SERVER_URL = prefs.getString("server_preference", "raspberrypi.home");
		
		String result = "nodata";
		
		try {
			// set location of the keystore
			MemorizingTrustManager.setKeyStoreFile("private", "sslkeys.bks");
			// register MemorizingTrustManager for HTTPS
			SSLContext sc = SSLContext.getInstance("TLS");
			mtm = new MemorizingTrustManager(activity);
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
		    URL u = new URL("https://" + SERVER_URL + "/hivemonitor/" + url);
			HttpsURLConnection conn = (HttpsURLConnection) u.openConnection();
			conn.setReadTimeout(6000);
			conn.setConnectTimeout(6000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("sensor", sensor));

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
                sb.append(line+"\n");
            }
            br.close();
            result = sb.toString();
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
}
