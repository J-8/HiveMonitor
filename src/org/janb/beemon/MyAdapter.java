package org.janb.beemon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.janb.beemon.FragmentHistory.SensorData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
 
public class MyAdapter extends ArrayAdapter<SensorData> {
 
        private final Context context;
        private final ArrayList<SensorData> itemsArrayList;
 
        public MyAdapter(Context context, ArrayList<SensorData> itemsArrayList) {
 
            super(context, R.layout.rowlayout, itemsArrayList);
 
            this.context = context;
            this.itemsArrayList = itemsArrayList;
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
 
            // 1. Create inflater
            LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
            // 2. Get rowView from inflater
            View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
            // 3. Get the two text view from the rowView
            TextView labelView = (TextView) rowView.findViewById(R.id.label);
            TextView detailView = (TextView) rowView.findViewById(R.id.detail);
 
            // 4. Set the text for textView
            labelView.setText(itemsArrayList.get(position).getValue());
            detailView.setText(StrtoDate(itemsArrayList.get(position).getTimestamp()));    
            
            
            // 5. return rowView
            return rowView;
        }

		private String StrtoDate(String timestamp){
			String datetime = "NO DATE AVAILABLE";
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
			try {
				cal.setTime(sdf.parse(timestamp));
				String minute = Integer.toString(cal.get(Calendar.MINUTE));
				if (cal.get(Calendar.MINUTE) < 10){
					minute = "0" + minute;
				}
				datetime = cal.get(Calendar.DAY_OF_MONTH) + "." + cal.get(Calendar.MONTH) + "." + cal.get(Calendar.YEAR) + " / " + cal.get(Calendar.HOUR) + ":" + minute + " Uhr";
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return datetime;
		}

        
        
}
