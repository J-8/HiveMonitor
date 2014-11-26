package org.janb.hivemonitor;

import android.app.Activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.widget.DrawerLayout;
import org.janb.hivemonitor.R;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks{

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
	FragmentManager fm;
	FragmentTransaction ft;
	  MyBroadcastReceiver receiver;
	  IntentFilter intentFilter;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    	   intentFilter = new IntentFilter();
		   intentFilter.addAction("org.janb.hivemonitor");
		   receiver = new MyBroadcastReceiver();
		   
		   isFirstStart();
    }

    private Boolean isFirstStart() {
       SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
       Boolean isFirstStart = mPrefs.getBoolean("firstStart", true);
       if (mPrefs.getBoolean("firstStart", true)){
    	   Intent set = new Intent(this, WelcomeActivity.class);
    	   startActivity(set);
       }
	return isFirstStart;
		
	}

	@Override
    public void onNavigationDrawerItemSelected(int position) {  	
        fm = getFragmentManager();
        ft = fm.beginTransaction();
    	switch (position) {
            case 0:
                mTitle = getString(R.string.title_section1);
                getActionBar().setTitle(mTitle);
                ft.replace(R.id.container, new FragmentOverview(""), "FRAGMENT_OVERVIEW");
            	ft.commit();
                break;
            case 1:
            	mTitle = getString(R.string.title_section2);
            	getActionBar().setTitle(mTitle);
            	ft.replace(R.id.container, new FragmentHistory("temp"), "FRAGMENT_HISTORY_TEMP");
            	ft.commit(); 
                break;
            case 2:
            	mTitle = getString(R.string.title_section3);
            	getActionBar().setTitle(mTitle);
            	ft.replace(R.id.container, new FragmentHistory("weight"), "FRAGMENT_HISTORY_WEIGHT");
            	ft.commit(); 
                break;    
        }
    }

    public void onSectionAttached(int number) {
        fm = getFragmentManager();
        ft = fm.beginTransaction();
    	switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                ft.replace(R.id.container, new FragmentOverview(""), "FRAGMENT_OVERVIEW");
            	ft.commit();
                break;
            case 2:
            	mTitle = getString(R.string.title_section2);
            	ft.replace(R.id.container, new FragmentHistory("temp"), "FRAGMENT_HISTORY_TEMP");
            	ft.commit(); 
                break;
            case 3:
            	mTitle = getString(R.string.title_section3);
            	ft.replace(R.id.container, new FragmentHistory("weight"), "FRAGMENT_HISTORY_WEIGHT");
            	ft.commit(); 
                break;
            case 4:
                Intent set = new Intent(this, SettingsActivity.class);
                startActivity(set);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    public void onResume(){
		   super.onResume();
		   registerReceiver(receiver, intentFilter);
		   fm = getFragmentManager();
		   ft = fm.beginTransaction();
           mTitle = getString(R.string.title_section1);
		   if (isFirstStart()){
			   ft.replace(R.id.container, new FragmentOverview(""), "FRAGMENT_OVERVIEW");   
		   } else {
			   ft.replace(R.id.container, new FragmentOverview("refresh"), "FRAGMENT_OVERVIEW");
		   }      
	       ft.commit();	   
	   }
    
    public void onPause(){
    	super.onPause();
    	setVisibility(false);
    	unregisterReceiver(receiver);
    }


	private class MyBroadcastReceiver extends BroadcastReceiver{
 	   
 	   public void onReceive(Context context, Intent intent) {
 		  fm = getFragmentManager();
		   ft = fm.beginTransaction();
		   mTitle = getString(R.string.title_section1);
	       ft.replace(R.id.container, new FragmentOverview("refresh"), "FRAGMENT_OVERVIEW");
	       ft.commit();	   
 	   } 	   
    }

	private void setVisibility(Boolean state){
	       SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	       Editor editor = mPrefs.edit();
	       editor.putBoolean("visible", state);
	       editor.commit();
	   }
	
}
