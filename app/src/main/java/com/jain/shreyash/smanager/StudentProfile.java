package com.jain.shreyash.smanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


public class StudentProfile extends AppCompatActivity {

    TextView student_name,student_email,student_register_no;
    ArrayList<Boolean> student_bk = new ArrayList<>();
    ArrayList<Boolean> student_ln = new ArrayList<>();
    ArrayList<Boolean> student_dn = new ArrayList<>();
    ArrayList<String> month_dates = new ArrayList<>();
    ArrayList<String> month_day_name = new ArrayList<>();
    String register_no;
    int month_start_col,today_col;
    int student_row;
    String name,email;
    ListView listView;
    int daysInMonth;
    GoogleAccountCredential mCredential;
    private static final String PREF_ACCOUNT_NAME = "accountName";


    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };
    int repot_int_month,this_month;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        student_name=findViewById(R.id.student_name);
        student_email=findViewById(R.id.student_email);
        student_register_no=findViewById(R.id.student_res_no);

        listView=findViewById(R.id.all_diets_info_list);


        Intent intent = getIntent();
        register_no = intent.getStringExtra("Register");
        student_register_no.setText(register_no);
        student_row=7+(Integer.valueOf(register_no)-1)*3;
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        // SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat df_month = new SimpleDateFormat("MM");
        DateFormat df_day = new SimpleDateFormat("dd");
        DateFormat df_year = new SimpleDateFormat("yyyy");

// Get the date today using Calendar object.

// Using DateFormat format method we can create a string
// representation of a date with the defined format.
        String reportDate = df_day.format(c);
        String reportMonth = df_month.format(c);
        Log.i("Here : ",reportMonth);
        String reportYear = df_year.format(c);
        int repot_int_day=Integer.valueOf(reportDate);
        repot_int_month=Integer.valueOf(reportMonth);
        this_month=repot_int_month;
        month_start_col=8+(this_month-1)*31;

        today_col=7+(this_month-1)*31+repot_int_day;
        Calendar mycal = new GregorianCalendar(Integer.valueOf(reportYear), (repot_int_month-1), repot_int_day);

// Get the number of days in that month
        daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
        mCredential = GoogleAccountCredential.usingOAuth2(
                StudentProfile.this.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        // String accountName = getResources().getString(R.string.pref_account_name);
        // = getActivity().getPreferences(Context.MODE_PRIVATE)
        // .getString(PREF_ACCOUNT_NAME, null);
        String accountName="";

        accountName="sattvikmess@gmail.com";
        mCredential.setSelectedAccountName(accountName);

        new ProfileRequestTask(mCredential).execute();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public class ProfileRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        public ProgressDialog loginDialog = new ProgressDialog( StudentProfile.this);
        public Boolean flag;

       ProfileRequestTask(GoogleAccountCredential credential) {
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


            String name_range="bhaiya_sheet!"+"B"+student_row;
            ValueRange sname=mService.spreadsheets().values().get(spreadsheetId,name_range).execute();
            name=sname.getValues().toString();
            String email_range="bhaiya_sheet!"+"A"+student_row;
            ValueRange semail=mService.spreadsheets().values().get(spreadsheetId,email_range).execute();
            email=semail.getValues().toString();
            email=email.substring(2,email.length()-2);
            name=name.substring(2,name.length()-2);
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
           /*/ for (int j=0;j<3;j++) {
                for (int i = 0; i <= today_col - month_start_col; i++) {
                    StringBuilder columnName = new StringBuilder();
                    int columnNumber = month_start_col + i;

                    while (columnNumber > 0) {
                        // Find remainder
                        int rem = columnNumber % 26;

                        // If remainder is 0, then a
                        // 'Z' must be there in output
                        if (rem == 0) {
                            columnName.append("Z");
                            columnNumber = (columnNumber / 26) - 1;
                        } else // If remainder is non-zero
                        {
                            columnName.append((char) ((rem - 1) + 'A'));
                            columnNumber = columnNumber / 26;
                        }
                    }
                    String getcolumn = String.valueOf(columnName.reverse());

                    String cancel_range = "bhaiya_sheet!" + getcolumn + student_row+j;
                    Log.i("COL : ", cancel_range);

                    if (!uncheck_dn.contains(new Integer(i))) {
                        cancel_range = "bhaiya_sheet!" + getcolumn + (student_row + 2);
                        data.add(new ValueRange()
                                .setRange(cancel_range)
                                .setValues(Arrays.asList(
                                        Arrays.asList(0))));
                    }


                }
            }*/
            DateFormat format2=new SimpleDateFormat("EEE");

           String student_row1=student_row+":"+(student_row+2);
           Log.i("St row : ",student_row1);

            String range = "bhaiya_sheet!"+student_row1;

            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {

                int getrow=0;
                for (List row : values) {
                    getrow=getrow+1;
                    for (int i =  month_start_col; i <= today_col ; i++) {

                        if (getrow==1){
                            Log.i("ms: ","i");

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(cal.getTime());
                            cal.add(Calendar.DATE, (i-today_col));
                            Date dateBefore30Days = cal.getTime();
                            String reportDate = df.format(dateBefore30Days);
                            String finalDay=format2.format(dateBefore30Days);
                            month_day_name.add(finalDay);
                            month_dates.add(reportDate);
                            if (row.get(i-1).toString().equals("0")){
                                student_bk.add(false);
                            }
                            else student_bk.add(true);


                        }
                        else if (getrow==2){
                            if (row.get(i-1).toString().equals("0")){
                                student_ln.add(false);
                            }
                            else student_ln.add(true);


                        }
                        else {
                            if (row.get(i-1).toString().equals("0")){
                                student_dn.add(false);
                            }
                            else student_dn.add(true);

                        }

                    }



                }
            }




            BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                    .setValueInputOption("RAW")
                    .setData(data);

            BatchUpdateValuesResponse batchResult = this.mService.spreadsheets().values()
                    .batchUpdate(spreadsheetId, batchBody)
                    .execute();



            return results;
        }



        @Override
        protected void onPreExecute() {
            Log.i("DATA:","1");
            loginDialog.setMessage("Getting profile Info");
            loginDialog.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            int num_dates=month_dates.size();
            params.height = (168)*num_dates;
            String[] dates_stringArray = month_dates.toArray(new String[0]);
            String[] days_stringArray = month_day_name.toArray(new String[0]);

            Boolean[] bk_list = student_bk.toArray(new Boolean[0]);
            Boolean[] ln_list = student_ln.toArray(new Boolean[0]);
            Boolean[] dn_list = student_dn.toArray(new Boolean[0]);
            OnlineCustomListAdapter whatever = new OnlineCustomListAdapter(StudentProfile.this,dates_stringArray,days_stringArray,bk_list,ln_list,dn_list);
            listView.setLayoutParams(params);
            listView.requestLayout();
            listView.setAdapter(whatever);
            student_email.setText(email);
            student_name.setText(name);





            loginDialog.dismiss();




            if (output == null || output.size() == 0) {
                Log.i("DATA:","2");
            } else {
                output.add(0, "Data retrieved using the Google Sheets API:");
                Log.i("DATA:","3");
            }
        }


    }
}
