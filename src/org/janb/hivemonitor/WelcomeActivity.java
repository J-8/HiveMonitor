package org.janb.hivemonitor;

import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collections;

import de.duenndns.ssl.MemorizingTrustManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class WelcomeActivity extends Activity implements OnClickListener, ResultsListener {

	private Button serverCheckBTN, certBTN, startBTN;
	private TextView serverTV;
	private ImageView serverCheckIC;
	MemorizingTrustManager mtm;
	private ProgressDialog dialog;
	SharedPreferences mPrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		serverCheckBTN = (Button)findViewById(R.id.serverCheckBTN);
		certBTN = (Button)findViewById(R.id.certManageBTN);
		startBTN = (Button)findViewById(R.id.startBTN);
		serverTV = (TextView)findViewById(R.id.serverTV);
		serverCheckIC = (ImageView)findViewById(R.id.serverCheckIC);
		serverCheckBTN.setOnClickListener(this);
		certBTN.setOnClickListener(this);
		startBTN.setOnClickListener(this);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		serverTV.setHint(mPrefs.getString("server_preference", getString(R.string.serverHint)));
		if (mPrefs.getBoolean("firstStart", true)) { getActionBar().setDisplayHomeAsUpEnabled(false); }
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.serverCheckBTN:
			if (serverTV.getText().length() == 0){
				break;
			}
			dialog = new ProgressDialog(this);
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			dialog.setMessage(getString(R.string.DialogServerCheck));
			dialog.show();
		       AsyncTask<String, Integer, String> task = new MTMAsyncTask(this);
		      	((MTMAsyncTask) task).setOnResultsListener(this);
		      	task.execute("servercheck.php", "");
			break;
		case R.id.certManageBTN:
			certManage(v);
			break;
		case R.id.startBTN:
			Editor editor = mPrefs.edit();
		       editor.putString("server_preference", serverTV.getText().toString());
		       editor.putBoolean("firstStart", false);
		       editor.commit();
			Intent main = new Intent(this, MainActivity.class);
			startActivity(main);
			finish();
			break;
		default:
			//default
		}
		
	}

	@Override
	public void onResultsSucceeded(String result) {
		serverCheckIC.setVisibility(View.VISIBLE);
		if (dialog != null){
			dialog.dismiss();
		}
		if (result.trim().equals("serverok")){
			serverCheckIC.setImageResource(R.drawable.ic_action_accept);
		       startBTN.setVisibility(View.VISIBLE);
		} else {
			serverCheckIC.setImageResource(R.drawable.ic_action_cancel);
		}
	}
	
	public void certManage(View view) {
		mtm = new MemorizingTrustManager(this);
		final ArrayList<String> aliases = Collections.list(mtm.getCertificates());
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, aliases);
		new AlertDialog.Builder(this).setTitle(getString(R.string.certDialog))
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
}
