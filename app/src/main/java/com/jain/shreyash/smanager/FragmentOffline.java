package com.jain.shreyash.smanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FragmentOffline extends Fragment implements AdapterView.OnItemSelectedListener,View.OnClickListener {
    private  String field;
    Button btnDatePicker;
    ArrayList<Integer> offline_coloumn_list = new ArrayList<Integer>();
    private int mYear, mMonth, mDay;
    TextView txtDate;
    ListView listView;
    GoogleAccountCredential mCredential;

    private static final String PREF_ACCOUNT_NAME = "accountName";


    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //just change the fragment_dashboard
        //with the fragment you want to inflate
        View view = inflater.inflate(R.layout.fragment_offline, container, false);
        listView = (ListView) view.findViewById(R.id.listview_on_offline);

        //like if the class is HomeFragment it should have R.layout.home_fragment
        //if it is DashboardFragment it should have R.layout.fragment_dashboard



        btnDatePicker=(Button)view.findViewById(R.id.btn_date);
        btnDatePicker.setOnClickListener(this);




        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        field = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), "Selected: " + field, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {

        DatePickerBuilder builder = new DatePickerBuilder(getContext(),listener )

                .pickerType(CalendarView.MANY_DAYS_PICKER);

        DatePicker datePicker = builder.build();
        datePicker.show();


    }
    private OnSelectDateListener listener = new OnSelectDateListener() {
        @Override
        public void onSelect(List<Calendar> calendars) {
            offline_coloumn_list.clear();

            int num_dates=calendars.size();
            String[] off_dates = new String[num_dates];
            String[] off_day = new String[num_dates];
            Boolean[] make_ck_set =new Boolean[num_dates];
            Toast.makeText(getActivity(), String.valueOf(num_dates), Toast.LENGTH_SHORT).show();
            for(int i = 0; i<num_dates; i++){
                Calendar thiscal= calendars.get(i);
                Date thisdate=thiscal.getTime();
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                DateFormat df_day = new SimpleDateFormat("dd");
                String get_only_day_string = df_day.format(thisdate);

                int this_day_of_month = Integer.valueOf(get_only_day_string);
                DateFormat df_month = new SimpleDateFormat("MM");
                String get_only_month_string=df_month.format(thisdate);
                int this_month = Integer.valueOf(get_only_month_string);
                String reportDate = df.format(thisdate);
                SimpleDateFormat formatter = new SimpleDateFormat("EEE");
                String this_day = formatter.format(thiscal.getTime());
                off_dates[i]=reportDate;
                off_day[i]=this_day;
                make_ck_set[i]=true;
                offline_coloumn_list.add(7+this_day_of_month+(this_month-1));


            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = (165)*num_dates;
            OfflineCustomListAdapter whatever = new OfflineCustomListAdapter(getActivity(),off_dates,off_day, make_ck_set,make_ck_set,make_ck_set);
            listView.setLayoutParams(params);
            listView.requestLayout();
            listView.setAdapter(whatever);


        }
    };
}
