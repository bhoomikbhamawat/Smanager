package com.jain.shreyash.smanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class FragmentOffline extends Fragment implements AdapterView.OnItemSelectedListener,View.OnClickListener {
    private  String field;
    Button btnDatePicker,send_request,cancel_full_breakfast;
    ArrayList<Integer> offline_coloumn_list = new ArrayList<Integer>();

    DatePicker datePicker_bk;
    String[] off_dates;

    String student_name;

    String student_email;
    ListView listView;
    EditText student_reg_no;
    int this_month_dates,starting_bk_date,today_selected_cancelled_bk;
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



        Calendar max = Calendar.getInstance();
        max.add(Calendar.DAY_OF_MONTH, 15);
        Calendar min = Calendar.getInstance();
        min.add(Calendar.DAY_OF_MONTH, -15);
        DatePickerBuilder builder_bk = new DatePickerBuilder(getContext(), listener_bk)
                .pickerType(CalendarView.ONE_DAY_PICKER)
                ;


        datePicker_bk = builder_bk.build();
        cancel_full_breakfast=view.findViewById(R.id.full_breakfast_cancel);
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

        cancel_full_breakfast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(student_reg_no.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(),"Enter Register no. first", Toast.LENGTH_SHORT).show();
                }


                else {

                    student_row = Integer.parseInt(student_reg_no.getText().toString());

                    student_row=7+(student_row-1)*3;
                    Log.i("BK: ",uncheck_bk.toString());
                    Log.i("LN: ",uncheck_ln.toString());
                    Log.i("DN: ",uncheck_dn.toString());
                    confirmation=0;
                    createAlertDialogbreakfast();


                }
            }
        });

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


            String name_range="bhaiya_sheet!"+"B"+student_row;
            ValueRange sname=mService.spreadsheets().values().get(spreadsheetId,name_range).execute();
            student_name=sname.getValues().toString();
            String email_range="bhaiya_sheet!"+"A"+student_row;
            ValueRange semail=mService.spreadsheets().values().get(spreadsheetId,email_range).execute();
            student_email=semail.getValues().toString();
            student_email=student_email.substring(2,student_email.length()-2);
            student_name=student_name.substring(2,student_name.length()-2);
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
                int meal_sum=0;
                if(!uncheck_bk.contains(new Integer(i))){
                data.add(new ValueRange()
                        .setRange(cancel_range)
                        .setValues(Arrays.asList(
                                Arrays.asList(0))));
                meal_sum+=1;}
                if(!uncheck_ln.contains(new Integer(i))){
                    cancel_range="bhaiya_sheet!"+getcolumn+(student_row+1);
                    data.add(new ValueRange()
                            .setRange(cancel_range)
                            .setValues(Arrays.asList(
                                    Arrays.asList(0))));
                    meal_sum+=3;}
                if(!uncheck_dn.contains(new Integer(i))){
                    cancel_range="bhaiya_sheet!"+getcolumn+(student_row+2);
                    data.add(new ValueRange()
                            .setRange(cancel_range)
                            .setValues(Arrays.asList(
                                    Arrays.asList(0))));
                    meal_sum+=5;}
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

                try {
                    Date date = format.parse(off_dates[i]);
                    uploadCancelDetails(meal_sum,date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }





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
            Calendar maxc = Calendar.getInstance();
            maxc.add(Calendar.DAY_OF_MONTH, 30);
            Calendar minc = Calendar.getInstance();
            minc.add(Calendar.DAY_OF_MONTH, -1);
            DatePickerBuilder builder = new DatePickerBuilder(getContext(), listener)

                    .pickerType(CalendarView.MANY_DAYS_PICKER)
                    .minimumDate(minc) // Minimum available date
                    .maximumDate(maxc) ;

            DatePicker datePicker = builder.build();
            datePicker.show();


        }
    }
    private OnSelectDateListener listener = new OnSelectDateListener() {
        @Override
        public void onSelect(List<Calendar> calendars) {
            offline_coloumn_list.clear();

            int num_dates=calendars.size();
            off_dates = new String[num_dates];
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
    public void createAlertDialogbreakfast() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("Starting Date");

        // Setting Dialog Message
        alertDialog.setMessage("Select a starting date\nप्रारंभिक तिथि चुनें");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_date_range_black_24dp);

        // Setting Positive "Yes" Button
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed YES button. Write Logic Here

                datePicker_bk.show();


                //new MakeCancelRequestTask(mCredential).execute();
                //Toast.makeText(getContext(), "You clicked on YES",Toast.LENGTH_SHORT).show();
            }
        });

        // Showing Alert Message
        alertDialog.show();

    }

    private OnSelectDateListener listener_bk = new OnSelectDateListener() {
        @Override
        public void onSelect(List<Calendar> calendars) {


            Calendar cal=calendars.get(0);
            DateFormat df_month = new SimpleDateFormat("MM");
            DateFormat df_day = new SimpleDateFormat("dd");
            DateFormat df_year = new SimpleDateFormat("yyyy");

// Get the date today using Calendar object.
            Date today = cal.getTime();
// Using DateFormat format method we can create a string
// representation of a date with the defined format.
            String reportDate = df_day.format(today);
            String reportMonth = df_month.format(today);
            Log.i("Here : ",reportMonth);
            String reportYear = df_year.format(today);

            int repot_int_day=Integer.valueOf(reportDate);
            int repot_int_month=Integer.valueOf(reportMonth);
            Log.i("Here 2 : ",repot_int_month+"");
            int repot_int_year=Integer.valueOf(reportYear);
            starting_bk_date=repot_int_day+7+(repot_int_month-1)*31;

            today_selected_cancelled_bk=repot_int_day;

            Log.i("Here : ",starting_bk_date+"");


            int iYear = repot_int_year;
            int iMonth = repot_int_month-1; // 1 (months begin with 0)
            int iDay = repot_int_day;

// Create a calendar object and set year and month
            Calendar mycal = new GregorianCalendar(iYear, iMonth, iDay);

// Get the number of days in that month
            int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
            this_month_dates=daysInMonth;
            new MakeBreakfastCancelRequestTask(mCredential).execute();

        }

    };

    public class MakeBreakfastCancelRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        public ProgressDialog loginDialog = new ProgressDialog( getContext());
        public Boolean flag;

        MakeBreakfastCancelRequestTask(GoogleAccountCredential credential) {
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


            Log.i("Check : ","Hello");

            Log.i("Check : ",this_month_dates+"");
            Log.i("Check : ",starting_bk_date+"");
            for(int i=0;i<=this_month_dates-today_selected_cancelled_bk;i++){
                StringBuilder columnName = new StringBuilder();
                int columnNumber= starting_bk_date+i;

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

                    data.add(new ValueRange()
                            .setRange(cancel_range)
                            .setValues(Arrays.asList(
                                    Arrays.asList(0))));


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
            alertDialog.setMessage("Breakfasts are now cancelled\nआपका भोजन रद्द कर दिया गया है");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

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
    void uploadCancelDetails(int meals, Date rq_date)
    {

        final String Acceptance = "1";
        final String b = (meals == 1 || meals == 4 || meals == 6 || meals ==  9)? "1":"0";
        final String d = (meals == 5 || meals == 6 || meals == 8 || meals ==  9)? "1":"0";
        final String l = (meals == 3 || meals == 4 || meals == 8 || meals ==  9)? "1":"0";
        //Todo take diet from shared preference
        final String diet = "1";

        //sharedPreferences = this.getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE);
        final String name = student_name;

        final DateFormat df = new SimpleDateFormat("yyyy/M/d h:mm:ss a");
        final String request_date = df.format(rq_date);

        final String email = student_email;
        final String email_refined = email.replaceAll("\\W+", "");

       // sharedPreferences = getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE);
        //final SharedPreferences.Editor editor = sharedPreferences.edit();

        //Time from server
        final Calendar calendar = Calendar.getInstance();
        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double offset = snapshot.getValue(Double.class);
                double estimatedServerTimeMs = System.currentTimeMillis() + offset;
                calendar.setTimeInMillis(((long) estimatedServerTimeMs));
                calendar.setTimeInMillis(((long) estimatedServerTimeMs));
                Log.d("inter",""+calendar.getTime());
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });

        //Toast.makeText(this, ""+request_date, Toast.LENGTH_SHORT).show();
        FirebaseDatabase PostReference = FirebaseDatabase.getInstance();
        final DatabaseReference mPostReference = PostReference.getReference("cancel_sheet");

        final String key = mPostReference.child(email_refined).push().getKey();
        mPostReference.child(email_refined).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        CancelDetails cancelDetails;
                        //Creating record to Firebase
                        SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d h:mm:ss a");

                        cancelDetails = new CancelDetails(
                                Acceptance,
                                b,
                                d,
                                format.format(calendar.getTime()),
                                diet,
                                email,
                                l,
                                name,
                                request_date
                        );
                        mPostReference.child(email_refined).child(key).setValue(cancelDetails);
                        String filename = "CancelData";
                        //Getting number of cancel requests
                        int count = 0;


                        //Updating internal storage

                        //Toast.makeText(getContext(), "request sent", Toast.LENGTH_LONG).show();
                      //  Intent i = new Intent(ConfirmCancel.this, Dashboard.class);
                       // i.putExtra("EXTRA", "notopenFragment");
                       // startActivity(i);
                        //finish();

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Toast.makeText(ConfirmCancel.this, "Unable to send request", Toast.LENGTH_LONG).show();
                        Log.w("cancel uplodaed or not", "loadPost:onCancelled", databaseError.toException());
                    }
                });
    }
}
