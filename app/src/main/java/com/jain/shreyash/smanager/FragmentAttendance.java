package com.jain.shreyash.smanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

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
import java.util.List;

public class FragmentAttendance extends Fragment {

    GoogleAccountCredential mCredential;
    int choise_attendance=-1;
    int this_month,warn_user=-1,add_guest=0,add_extra=0;
    TextToSpeech t1;
    int repot_int_month;
    String student_name;
    int today_column,student_row;
    EditText student_reg_no;
    String getcolumn;
    private static final String PREF_ACCOUNT_NAME = "accountName";


    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //just change the fragment_dashboard
        //with the fragment you want to inflate
        //like if the class is HomeFragment it should have R.layout.home_fragment


        View view = inflater.inflate(R.layout.fragment_attendance, container, false);
        mCredential = GoogleAccountCredential.usingOAuth2(
                view.getContext().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        // String accountName = getResources().getString(R.string.pref_account_name);
        // = getActivity().getPreferences(Context.MODE_PRIVATE)
        // .getString(PREF_ACCOUNT_NAME, null);
        String accountName="";
        student_reg_no=view.findViewById(R.id.reg_no_on_attendance);
        Button chk_att=view.findViewById(R.id.chk_att);
        Button add_guest_btn=view.findViewById(R.id.add_guest);
        Button add_extra_btn=view.findViewById(R.id.add_extra);
        Button get_student_profile=view.findViewById(R.id.full_info);
        get_student_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (student_reg_no.getText().toString().isEmpty()){

                    Toast.makeText(getActivity().getApplicationContext(), "First enter Register Number\nरजिस्टर नंबर दर्ज करें",
                            Toast.LENGTH_LONG).show();

                }
                else {
                Intent i=new Intent(getActivity(),StudentProfile.class);
                i.putExtra("Register",student_reg_no.getText().toString());

                startActivity(i);
            }}
        });

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
        Log.i("Here 2 : ",repot_int_month+"");
        int repot_int_year=Integer.valueOf(reportYear);
        today_column=repot_int_day+7+(repot_int_month-1)*31;
        //String formattedDate = df.format(c);
        chk_att.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              /*  if(student_reg_no.getText().toString().isEmpty()){
                    Toast.makeText(getActivity().getApplicationContext(), "First enter Register Number\nरजिस्टर नंबर दर्ज करें",
                            Toast.LENGTH_LONG).show();
                }
                else if (choise_attendance==-1){
                    Toast.makeText(getActivity().getApplicationContext(), "Please select the meal first\nकृपया भोजन के प्रकार का चयन करें",
                            Toast.LENGTH_LONG).show();

                }
                else {
                    student_row=7+(Integer.valueOf(student_reg_no.getText().toString())-1)*3;


                    new AttendanceRequestTask(mCredential).execute();
                }*/


              Intent i=new Intent(getContext(),LocalAttendence.class);
              startActivity(i);


            }
        });

        add_guest_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(student_reg_no.getText().toString().isEmpty()){
                    Toast.makeText(getActivity().getApplicationContext(), "First enter Register Number\nरजिस्टर नंबर दर्ज करें",
                            Toast.LENGTH_LONG).show();
                }
                else if (choise_attendance==-1||add_guest==0){
                    Toast.makeText(getActivity().getApplicationContext(), "Please select the meal first\nकृपया भोजन के प्रकार का चयन करें",
                            Toast.LENGTH_LONG).show();

                }
                else {
                    student_row=7+(Integer.valueOf(student_reg_no.getText().toString())-1)*3;


                    new GuestRequestTask(mCredential).execute();
                }
            }
        });
        add_extra_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(student_reg_no.getText().toString().isEmpty()){
                    Toast.makeText(getActivity().getApplicationContext(), "First enter Register Number\nरजिस्टर नंबर दर्ज करें",
                            Toast.LENGTH_LONG).show();
                }
                else if (add_extra==0){
                    Toast.makeText(getActivity().getApplicationContext(), "Please select number of extra first\nकृपया भोजन के प्रकार का चयन करें",
                            Toast.LENGTH_LONG).show();

                }
                else {
                    student_row=7+(Integer.valueOf(student_reg_no.getText().toString())-1)*3;


                    new ExtraRequestTask(mCredential).execute();
                }
            }
        });

        accountName="sattvikmess@gmail.com";
        mCredential.setSelectedAccountName(accountName);

        /*RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.RGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.breakfast_radio) {
                    choise_attendance=  1;
                    Toast.makeText(getActivity().getApplicationContext(), "choice: A",
                            Toast.LENGTH_SHORT).show();
                } else if(checkedId == R.id.lunch_radio) {
                    choise_attendance = 2;
                    Toast.makeText(getActivity().getApplicationContext(), "choice: B",
                            Toast.LENGTH_SHORT).show();
                } else if(checkedId == R.id.dinner_radio){
                    choise_attendance = 3;
                    Toast.makeText(getActivity().getApplicationContext(), "choice: C",
                            Toast.LENGTH_SHORT).show();
                }
            }

        });*/

        RadioGroup radioGroupGuest = (RadioGroup) view.findViewById(R.id.RGroupguest);
        radioGroupGuest.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.og_radio) {
                    add_guest=  1;

                } else if(checkedId == R.id.tg_radio) {
                    add_guest = 2;

                } else if(checkedId == R.id.thg_radio){
                    add_guest = 3;

                }
            }

        });

        RadioGroup radioGroupExtra = (RadioGroup) view.findViewById(R.id.RGroupextra);
        radioGroupExtra.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.oe_radio) {
                    add_extra=  1;

                } else if(checkedId == R.id.te_radio) {
                    add_extra = 2;

                } else if(checkedId == R.id.the_radio){
                    add_extra = 3;

                }
            }

        });
        StringBuilder columnName = new StringBuilder();
        int columnNumber= today_column;

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
         getcolumn= String.valueOf(columnName.reverse());

        //if it is DashboardFragment it should have R.layout.fragment_dashboard
        return view;
    }


    public class AttendanceRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        public ProgressDialog loginDialog = new ProgressDialog( getContext());
        public Boolean flag;

        AttendanceRequestTask(GoogleAccountCredential credential) {
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
            String check_atte_range="bhaiya_sheet!"+getcolumn;
            List<ValueRange> data = new ArrayList<>();

            String name_range="bhaiya_sheet!"+"B"+student_row;
            ValueRange name=mService.spreadsheets().values().get(spreadsheetId,name_range).execute();
            student_name=name.getValues().toString();
            student_name=student_name.substring(2,student_name.length()-2);
            String bk_att=check_atte_range+student_row;
            ValueRange resultes = mService.spreadsheets().values().get(spreadsheetId,bk_att).execute();

            Log.i("Attendance :",resultes.getValues().toString());
            if(choise_attendance==1)
            {
                String bk_range_att=check_atte_range+student_row;
                ValueRange result = mService.spreadsheets().values().get(spreadsheetId,bk_range_att).execute();
                if(result.getValues().toString().equals("[[0]]"))
                {
                    warn_user=1;
                    data.add(new ValueRange()
                            .setRange(bk_range_att)
                            .setValues(Arrays.asList(
                                    Arrays.asList(1))));

                }
            }
            else if(choise_attendance==2)
            {
                String ln_range_att=check_atte_range+(student_row+1);
                ValueRange result = mService.spreadsheets().values().get(spreadsheetId, ln_range_att).execute();
                if(result.getValues().toString().equals("[[0]]"))
                {
                    warn_user=1;
                    data.add(new ValueRange()
                            .setRange(ln_range_att)
                            .setValues(Arrays.asList(
                                    Arrays.asList(1))));
                }
            }
            else if (choise_attendance==3)
            {
                String dn_range_att=check_atte_range+(student_row+2);
                ValueRange result = mService.spreadsheets().values().get(spreadsheetId, dn_range_att).execute();
                if(result.getValues().toString().equals("[[0]]"))
                {
                    warn_user=1;
                    data.add(new ValueRange()
                            .setRange(dn_range_att)
                            .setValues(Arrays.asList(
                                    Arrays.asList(1))));
                }
            }

            if(warn_user==1){

                BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                        .setValueInputOption("RAW")
                        .setData(data);

                BatchUpdateValuesResponse batchResult = this.mService.spreadsheets().values()
                        .batchUpdate(spreadsheetId, batchBody)
                        .execute();
            }
            t1=new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        t1.setLanguage(new Locale("en", "IN"));
                    }
                }
            });

            return results;
        }



        @Override
        protected void onPreExecute() {
            Log.i("DATA:","1");
            loginDialog.setMessage("Checking attendance...");
            loginDialog.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();
            String  s1;
            String s2;
            if(warn_user==1){
                s1="Give Warning";
                s2=" You are misusing the online cancellation service ";
            }
            else {

                s1="Let him/her eat";
                s2=" present";
            }
            warn_user=-1;
            loginDialog.dismiss();
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();

            alertDialog.setTitle(s1);
            alertDialog.setMessage(student_name+s2);
            String finalS = s2;
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                           //student_reg_no.getText().clear();
                            t1.speak(student_name+" "+ finalS, TextToSpeech.QUEUE_FLUSH, null);
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
    public class GuestRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        public ProgressDialog loginDialog = new ProgressDialog( getContext());
        public Boolean flag;

        GuestRequestTask(GoogleAccountCredential credential) {
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
            String check_atte_range="bhaiya_sheet!"+getcolumn;
            List<ValueRange> data = new ArrayList<>();

            String name_range="bhaiya_sheet!"+"B"+student_row;
            ValueRange name=mService.spreadsheets().values().get(spreadsheetId,name_range).execute();
            student_name=name.getValues().toString();
            student_name=student_name.substring(2,student_name.length()-2);
            String bk_att=check_atte_range+student_row;
            ValueRange resultes = mService.spreadsheets().values().get(spreadsheetId,bk_att).execute();

            Log.i("Attendance :",resultes.getValues().toString());
            if(choise_attendance==1)
            {
                Log.i("Extra bK :","bk");
                String bk_range_att=check_atte_range+student_row;
                ValueRange result = mService.spreadsheets().values().get(spreadsheetId,bk_range_att).execute();
                String get_now_diet=result.getValues().toString();
                get_now_diet=get_now_diet.substring(2,3);
                Log.i("Extra bK :",get_now_diet);


                    int add_diet=Integer.valueOf(get_now_diet)+add_guest;
                    data.add(new ValueRange()
                            .setRange(bk_range_att)
                            .setValues(Arrays.asList(
                                    Arrays.asList(add_diet))));


            }
            else if(choise_attendance==2)
            {
                String ln_range_att=check_atte_range+(student_row+1);
                ValueRange result = mService.spreadsheets().values().get(spreadsheetId, ln_range_att).execute();
                String get_now_diet=result.getValues().toString();
                get_now_diet=get_now_diet.substring(2,3);
                Log.i("Extra ln :",get_now_diet);


                int add_diet=Integer.valueOf(get_now_diet)+add_guest;


                    data.add(new ValueRange()
                            .setRange(ln_range_att)
                            .setValues(Arrays.asList(
                                    Arrays.asList(add_diet))));

            }
            else if (choise_attendance==3)
            {
                String dn_range_att=check_atte_range+(student_row+2);
                ValueRange result = mService.spreadsheets().values().get(spreadsheetId, dn_range_att).execute();
                String get_now_diet=result.getValues().toString();
                get_now_diet=get_now_diet.substring(2,3);


                int add_diet=Integer.valueOf(get_now_diet)+add_guest;


                    data.add(new ValueRange()
                            .setRange(dn_range_att)
                            .setValues(Arrays.asList(
                                    Arrays.asList(add_diet))));

            }



                BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                        .setValueInputOption("RAW")
                        .setData(data);

                BatchUpdateValuesResponse batchResult = this.mService.spreadsheets().values()
                        .batchUpdate(spreadsheetId, batchBody)
                        .execute();

            t1=new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        t1.setLanguage(new Locale("en", "IN"));
                    }
                }
            });

            return results;
        }



        @Override
        protected void onPreExecute() {
            Log.i("DATA:","1");
            loginDialog.setMessage("Adding Guest");
            loginDialog.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();
            String  s1;
            String s2;



                s1="Guest added";
                s2="Added "+add_guest+" Guest with "+student_name;


            loginDialog.dismiss();
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();

            alertDialog.setTitle(s1);
            alertDialog.setMessage(s2);
            String finalS = s2;
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            //student_reg_no.getText().clear();
                            t1.speak( finalS, TextToSpeech.QUEUE_FLUSH, null);
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
    public class ExtraRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        public ProgressDialog loginDialog = new ProgressDialog( getContext());
        public Boolean flag;

        ExtraRequestTask(GoogleAccountCredential credential) {
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
            String extra_col;
            if (repot_int_month==4){
                extra_col="E";
            }
            else {
                extra_col="D";
            }

            String check_atte_range="bhaiya_sheet!"+extra_col;
            List<ValueRange> data = new ArrayList<>();


            String name_range="bhaiya_sheet!"+"B"+student_row;
            ValueRange name=mService.spreadsheets().values().get(spreadsheetId,name_range).execute();
            student_name=name.getValues().toString();
            student_name=student_name.substring(2,student_name.length()-2);
            String bk_att=check_atte_range+student_row;
            ValueRange resultes = mService.spreadsheets().values().get(spreadsheetId,bk_att).execute();

            Log.i("Attendance :",resultes.getValues().toString());
            if(repot_int_month==1)
            {
                Log.i("Extra bK :","bk");
                String bk_range_att=check_atte_range+student_row;
                ValueRange result = mService.spreadsheets().values().get(spreadsheetId,bk_range_att).execute();
                String get_now_diet=result.getValues().toString();
                get_now_diet=get_now_diet.substring(2,3);
                if (get_now_diet.isEmpty()) get_now_diet=("0");
                Log.i("Extra bK :",get_now_diet);


                int add_diet=Integer.valueOf(get_now_diet)+add_extra;
                data.add(new ValueRange()
                        .setRange(bk_range_att)
                        .setValues(Arrays.asList(
                                Arrays.asList(add_diet))));


            }
            else if(repot_int_month==2)
            {
                String ln_range_att=check_atte_range+(student_row+1);
                ValueRange result = mService.spreadsheets().values().get(spreadsheetId, ln_range_att).execute();
                String get_now_diet=result.getValues().toString();
                get_now_diet=get_now_diet.substring(2,3);
                if (get_now_diet.isEmpty()) get_now_diet=("0");
                Log.i("Extra ln :",get_now_diet);


                int add_diet=Integer.valueOf(get_now_diet)+add_extra;


                data.add(new ValueRange()
                        .setRange(ln_range_att)
                        .setValues(Arrays.asList(
                                Arrays.asList(add_diet))));

            }
            else if (repot_int_month==3 || repot_int_month==4)
            {
                String dn_range_att=check_atte_range+(student_row+2);
                ValueRange result = mService.spreadsheets().values().get(spreadsheetId, dn_range_att).execute();
                String get_now_diet=result.getValues().toString();
                get_now_diet=get_now_diet.substring(2,3);
                if (get_now_diet.isEmpty()) get_now_diet=("0");


                int add_diet=Integer.valueOf(get_now_diet)+add_extra;


                data.add(new ValueRange()
                        .setRange(dn_range_att)
                        .setValues(Arrays.asList(
                                Arrays.asList(add_diet))));

            }



            BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                    .setValueInputOption("RAW")
                    .setData(data);

            BatchUpdateValuesResponse batchResult = this.mService.spreadsheets().values()
                    .batchUpdate(spreadsheetId, batchBody)
                    .execute();

            t1=new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        t1.setLanguage(new Locale("en", "IN"));
                    }
                }
            });

            return results;
        }



        @Override
        protected void onPreExecute() {
            Log.i("DATA:","1");
            loginDialog.setMessage("Adding Extra");
            loginDialog.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();
            String  s1;
            String s2;



            s1="Extra added";
            s2="Added "+add_extra+" Extra with "+student_name;


            loginDialog.dismiss();
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();

            alertDialog.setTitle(s1);
            alertDialog.setMessage(s2);
            String finalS = s2;
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            //student_reg_no.getText().clear();
                            t1.speak( finalS, TextToSpeech.QUEUE_FLUSH, null);
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
}
