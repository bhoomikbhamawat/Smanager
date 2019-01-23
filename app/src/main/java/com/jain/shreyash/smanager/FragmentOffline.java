package com.jain.shreyash.smanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
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

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.Arrays;


public class FragmentOffline extends Fragment implements AdapterView.OnItemSelectedListener,View.OnClickListener {
    private  String field;
    Button btnDatePicker,send_request;
    ArrayList<Integer> offline_coloumn_list = new ArrayList<Integer>();


    ListView listView;
    EditText student_reg_no;
    int student_row;
    static int confirmation=0;
    GoogleAccountCredential mCredential;
    ArrayList<Integer> uncheck_bk = new ArrayList<Integer>();
    ArrayList<Integer> uncheck_ln = new ArrayList<Integer>();
   ArrayList<Integer> uncheck_dn = new ArrayList<Integer>();

    private static final String PREF_ACCOUNT_NAME = "accountName";


    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //just change the fragment_dashboard
        //with the fragment you want to inflate
        View view = inflater.inflate(R.layout.fragment_offline, container, false);
        listView = (ListView) view.findViewById(R.id.listview_on_offline);
        student_reg_no= view.findViewById(R.id.reg_no_on_offline);

        //like if the class is HomeFragment it should have R.layout.home_fragment
        //if it is DashboardFragment it should have R.layout.fragment_dashboard



        send_request=view.findViewById(R.id.send_offline_cancel_request);

        btnDatePicker=(Button)view.findViewById(R.id.btn_date);

        btnDatePicker.setOnClickListener(this);
        mCredential = GoogleAccountCredential.usingOAuth2(
                view.getContext().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        // String accountName = getResources().getString(R.string.pref_account_name);
        // = getActivity().getPreferences(Context.MODE_PRIVATE)
               // .getString(PREF_ACCOUNT_NAME, null);
        String accountName="";

        accountName="sattvikmess@gmail.com";
        mCredential.setSelectedAccountName(accountName);
        //MakeRequest mk = new MakeRequest(mCredential);
        //String data = mk.getData(view.getContext(),"Sheet1!A1:A2");
        // if(data == null)
        // data = "null";
        // Log.i("DATA:","");
       //


        send_request.setOnClickListener(new View.OnClickListener() {


            int confirm=0;
            @Override
            public void onClick(View view) {
                if(student_reg_no.getText().toString()==""){
                    Toast.makeText(getActivity(),"Enter Register no. first", Toast.LENGTH_SHORT).show();
                }
                else if (offline_coloumn_list.size()==0){
                        Toast.makeText(getActivity(),"Select dates to cancel\nरद्द करने के लिए तिथियों का चयन करें", Toast.LENGTH_SHORT).show();
                }
               else {
                    ListAdapter adapter = listView.getAdapter();
                    uncheck_bk =((OfflineCustomListAdapter) adapter).unchecked_bk;
                    uncheck_ln =((OfflineCustomListAdapter) adapter).unchecked_ln;
                    uncheck_dn =((OfflineCustomListAdapter) adapter).unchecked_dn;
                    student_row = Integer.parseInt(student_reg_no.getText().toString());

                    student_row=7+(student_row-1)*3;
                    Log.i("BK: ",uncheck_bk.toString());
                    Log.i("LN: ",uncheck_ln.toString());
                    Log.i("DN: ",uncheck_dn.toString());
                    confirmation=0;
                    createAlertDialog();


                }
            }
        });


        return view;
    }

    public class MakeCancelRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        public ProgressDialog loginDialog = new ProgressDialog( getContext());
        public Boolean flag;

        MakeCancelRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         *
         */
        private List<String> getDataFromApi() throws IOException {
            String spreadsheetId = "1THhdUIIopAzMwh4IxTVoHP2WLtsS_EFgKg5ZeMekgQY";

            List<String> results = new ArrayList<String>();
            List<ValueRange> data = new ArrayList<>();
            /*data.add(new ValueRange()
                    .setRange("D1")
                    .setValues(Arrays.asList(
                            Arrays.asList("January Total"))));
            data.add(new ValueRange()
                    .setRange("D4")
                    .setValues(Arrays.asList(
                            Arrays.asList("February Total"))));*/


            int num_dates=offline_coloumn_list.size();
            for(int i=0;i<num_dates;i++){
                StringBuilder columnName = new StringBuilder();
                int columnNumber= offline_coloumn_list.get(i);

                while (columnNumber > 0)
                {
                    // Find remainder
                    int rem = columnNumber % 26;

                    // If remainder is 0, then a
                    // 'Z' must be there in output
                    if (rem == 0)
                    {
                        columnName.append("Z");
                        columnNumber = (columnNumber / 26) - 1;
                    }
                    else // If remainder is non-zero
                    {
                        columnName.append((char)((rem - 1) + 'A'));
                        columnNumber = columnNumber / 26;
                    }
                }
                String getcolumn= String.valueOf(columnName.reverse());

                String cancel_range="bhaiya_sheet!"+getcolumn+student_row;
                Log.i("COL : ",cancel_range);
                if(!uncheck_bk.contains(new Integer(i))){
                data.add(new ValueRange()
                        .setRange(cancel_range)
                        .setValues(Arrays.asList(
                                Arrays.asList(0))));}
                if(!uncheck_ln.contains(new Integer(i))){
                    cancel_range="bhaiya_sheet!"+getcolumn+(student_row+1);
                    data.add(new ValueRange()
                            .setRange(cancel_range)
                            .setValues(Arrays.asList(
                                    Arrays.asList(0))));}
                if(!uncheck_dn.contains(new Integer(i))){
                    cancel_range="bhaiya_sheet!"+getcolumn+(student_row+2);
                    data.add(new ValueRange()
                            .setRange(cancel_range)
                            .setValues(Arrays.asList(
                                    Arrays.asList(0))));}



            }
            BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                    .setValueInputOption("RAW")
                    .setData(data);

            BatchUpdateValuesResponse batchResult = this.mService.spreadsheets().values()
                    .batchUpdate(spreadsheetId, batchBody)
                    .execute();


           /* Object a1 = new Object();
            a1 = "1";
            Object a2 = new Object();
            a2 = "0";


            ValueRange body = new ValueRange()
                    .setValues(Arrays.asList(
                            Arrays.asList(a1)

                    ));
            this.mService.spreadsheets().values().update(spreadsheetId, "board_sheet!A1", body)
                    .setValueInputOption("RAW")
                    .execute();*/




            return results;
        }



        @Override
        protected void onPreExecute() {
            Log.i("DATA:","1");
            loginDialog.setMessage("Cancelling Meals");
            loginDialog.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();
            loginDialog.dismiss();
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            alertDialog.setTitle("DONE !");
            alertDialog.setMessage("Diets are now cancelled\nआपका भोजन रद्द कर दिया गया है");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ViewGroup.LayoutParams params = listView.getLayoutParams();
                            params.height = 0;
                            listView.setAdapter(null);
                            listView.setLayoutParams(params);
                            listView.requestLayout();
                            student_reg_no.getText().clear();
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            if (output == null || output.size() == 0) {
                Log.i("DATA:","2");
            } else {
                output.add(0, "Data retrieved using the Google Sheets API:");
                Log.i("DATA:","3");
            }
        }


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


        if (student_reg_no.getText().toString().isEmpty() || Integer.valueOf(student_reg_no.getText().toString())>221 ){
            Toast.makeText(getActivity(), "First enter Student Register No\nपहले छात्र रजिस्टर नंबर दर्ज करें", Toast.LENGTH_SHORT).show();
        }
        else {
            DatePickerBuilder builder = new DatePickerBuilder(getContext(), listener)

                    .pickerType(CalendarView.MANY_DAYS_PICKER);

            DatePicker datePicker = builder.build();
            datePicker.show();


        }
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
                offline_coloumn_list.add(7+this_day_of_month+(this_month-1)*31);


            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = (165)*num_dates;
            OfflineCustomListAdapter whatever = new OfflineCustomListAdapter(getActivity(),off_dates,off_day, make_ck_set,make_ck_set,make_ck_set);
            listView.setLayoutParams(params);
            listView.requestLayout();
            listView.setAdapter(whatever);


        }

    };

    public void createAlertDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("Confirm ?");

        // Setting Dialog Message
        alertDialog.setMessage("Check the details before cancelling\nStudent Register Number : "+student_reg_no.getText().toString());

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_check_box_black_24dp);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed YES button. Write Logic Here
                confirmation=1;
                new MakeCancelRequestTask(mCredential).execute();
                //Toast.makeText(getContext(), "You clicked on YES",Toast.LENGTH_SHORT).show();
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed No button. Write Logic Here
                confirmation=0;
                Toast.makeText(getContext(), "You clicked on NO", Toast.LENGTH_SHORT).show();
            }
        });




        // Showing Alert Message
        alertDialog.show();

    }
}
