package org.janb.beemon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
 
public class FragmentConnectError extends Fragment {
 
	private String parent;
	
	public FragmentConnectError(String parent){
	    this.parent=parent;
	}
	
 @Override
 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
  View view = inflater.inflate(R.layout.activity_fragment_connect_error, new LinearLayout(getActivity().getApplicationContext()), false);
  setHasOptionsMenu(true);
  return view;
 }

 @Override
 public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
     super.onCreateOptionsMenu(menu, inflater);
     menu.clear();
     inflater.inflate(R.menu.global, menu);
 }

@Override
public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.menu_load) {
		FragmentManager fm = getFragmentManager();
    	FragmentTransaction ft = fm.beginTransaction();
    	if (parent.equals("overview")){
    		ft.replace(R.id.container, new FragmentOverview("BackFromError"), "FRAGMENT_OVERVIEW");
    	}
    	if (parent.equals("temp")){
    		ft.replace(R.id.container, new FragmentHistory("temp"), "FRAGMENT_HISTORY_TEMP");
    	}
    	if (parent.equals("weight")){
    		ft.replace(R.id.container, new FragmentHistory("weight"), "FRAGMENT_HISTORY_WEIGHT");
    	}
    	ft.commit();
        return true;
    }
    
    return super.onOptionsItemSelected(item);
}
}
