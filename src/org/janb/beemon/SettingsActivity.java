package org.janb.beemon;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  // TODO Auto-generated method stub
  super.onCreate(savedInstanceState);

  getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
  
 }
protected void onResume(){
	super.onResume();
	
	getFragmentManager().beginTransaction().replace(android.R.id.content,
            new PrefsFragment()).commit();
}
}