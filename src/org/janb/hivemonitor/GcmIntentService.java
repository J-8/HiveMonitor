package org.janb.hivemonitor;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.janb.hivemonitor.R;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GcmIntentService extends IntentService{
	Context context;
	public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    public static final String TAG = "BeeMonitor";
    SharedPreferences prefs;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		String title = intent.getStringExtra("title");
		String msg = intent.getStringExtra("message");
		String code = intent.getStringExtra("code");
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		
		 if (!extras.isEmpty()) {
			 
			 if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
	                sendNotification("ERROR", "Send error: " + extras.toString(), "gcmerror");
	            } else if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_DELETED.equals(messageType)) {
	                sendNotification("ERROR", "Deleted messages on server: " +
	                        extras.toString(), "gcmerror");
	            // If it's a regular GCM message, do some work.
	            } else if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
	            	Log.i(TAG, "Received: " + extras.toString());
	                sendNotification(title, msg, code);
	            }
	        }
		 GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	private void sendNotification(String title, String msg, String code) {
		Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        prefs = getPreferences();
        
        if (code.equals("valueupdate")){
        	if (!prefs.getBoolean("visible", false)){
        		Intent intent = new Intent(this, MainActivity.class);
        		PendingIntent mainactivity = PendingIntent.getActivity(this, 0, intent, 0);
        		NotificationCompat.Builder mBuilder =
        	       	new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setContentIntent(mainactivity);
                    mBuilder.setAutoCancel(true);
                    if(prefs.getBoolean("vibrate_preference", false)){v.vibrate(350);}
                	mNotificationManager.notify(4, mBuilder.build());
        	} else {
        		Intent intent = new Intent();
        		intent.setAction("org.janb.hivemonitor");
        		sendBroadcast(intent);
        	}
        }
        
        if (code.equals("message")){
        	NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setStyle(new NotificationCompat.BigTextStyle()
            .bigText(msg))
            .setContentText(msg);
            mBuilder.setAutoCancel(true);

        	if(prefs.getBoolean("vibrate_preference", false)){v.vibrate(350);}
        	mNotificationManager.notify(5, mBuilder.build());
        }
        
        if (code.equals("warning")){
        	NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.warning_icon)
            .setContentTitle(title)
            .setStyle(new NotificationCompat.BigTextStyle()
            .bigText(msg))	
            .setContentText(msg);
            mBuilder.setAutoCancel(true);

        	if(prefs.getBoolean("vibrate_preference", false)){v.vibrate(350);}
        	mNotificationManager.notify(5, mBuilder.build());
        }
        
        
    }
	
	private SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}
}