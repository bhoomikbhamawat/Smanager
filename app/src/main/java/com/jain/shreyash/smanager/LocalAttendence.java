package com.jain.shreyash.smanager;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.Locale;


public class LocalAttendence extends AppCompatActivity {


    Integer student_reg_no;
    ArrayList<Integer> diet_data_local= new ArrayList<>();
    ArrayList<String> name_data_local= new ArrayList<>();
    GoogleAccountCredential mCredential;
    private Date date;
    private Date dateCompareOne;
    TextToSpeech t1;
    private Date dateCompareTwo;
    int meal_type; //0 for breakfast,1 for lunch,2 for dinner
    private Date dateCompareThree;
    private Date dateCompareFour;
    String CurrentUser,CurrentString;
    String getcolumn;

    private String compareStringOne = "6:00";
    private String compareStringTwo = "11:00";
    private String compareStringThree = "16:00";
    private String compareStringFour = "23:00";
    EditText reg_no;
    public static final String inputFormat = "HH:mm";

    private static final String PREF_ACCOUNT_NAME = "accountName";
    SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat, Locale.US);



    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_attendence);

        mCredential = GoogleAccountCredential.usingOAuth2(
                this.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        // String accountName = getResources().getString(R.string.pref_account_name);
        // = getActivity().getPreferences(Context.MODE_PRIVATE)
        // .getString(PREF_ACCOUNT_NAME, null);
        String accountName="";
        accountName="sattvikmess@gmail.com";
        mCredential.setSelectedAccountName(accountName);
        diet_data_local=FragmentAttendance.diet_data;

        DateFormat dateFormat = new SimpleDateFormat("dd/MM");
        Date date_s = new Date();

        name_data_local=FragmentAttendance.name_data;
        Log.i("local array ",diet_data_local.toString());

        reg_no=findViewById(R.id.local_edit_text);
        Intent iin= getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
            getcolumn=(String) b.get("Today Col");

        }

        Button ok_btn=findViewById(R.id.button_ok);
        TextView name=findViewById(R.id.set_text_on_local_page);
        TextView status_att=findViewById(R.id.set_text2_on_local_page);
        Calendar now = Calendar.getInstance();

        int hour = now.get(Calendar.HOUR);
        int minute = now.get(Calendar.MINUTE);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String str = sdf.format(new Date());

        date = parseDate(str);
        Log.d("Time now  ",date.toString());
        dateCompareOne = parseDate(compareStringOne);
        Log.d("Time now  ",dateCompareOne.toString());
        dateCompareTwo = parseDate(compareStringTwo);
        Log.d("Time now  ",dateCompareTwo.toString());
        dateCompareThree=parseDate(compareStringThree);
        Log.d("Time now  ",dateCompareThree.toString());
        dateCompareFour=parseDate(compareStringFour);
        TextView meal_of_day=findViewById(R.id.meal_type);

        if ( dateCompareOne.before( date ) && dateCompareTwo.after(date)) {
            meal_type=0;
            meal_of_day.setText("Breakfast on "+ dateFormat.format(date_s));

        }
        else if ( dateCompareTwo.before( date ) && dateCompareThree.after(date)){

            meal_type=1;
            meal_of_day.setText("Lunch on "+dateFormat.format(date_s));
        }else if ( dateCompareThree.before( date ) && dateCompareFour.after(date)){

            meal_type=2;
            meal_of_day.setText("Dinner on "+dateFormat.format(date_s));
        }
        else {
            meal_type=-1;
            meal_of_day.setText("Error");
        }


        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!reg_no.getText().toString().isEmpty()){
                    student_reg_no=Integer.valueOf(reg_no.getText().toString());
                    name.setText("Last user : "+name_data_local.get(student_reg_no-1));
                    CurrentUser=name_data_local.get(student_reg_no-1);

                    if(diet_data_local.get(meal_type+(student_reg_no-1)*3)!=0){
                      //  status_att.setText("You May have your Meal.\nThanks for your cooperation");
                        Log.d("diet index  ",(meal_type+(student_reg_no-1)*3)+"");
                        CurrentString="You May have your Meal.\nThanks for your cooperation";
                        createAlertDialog();
                    }
                    else{
                        new AttendanceRequestTask(mCredential).execute();
                    }

                }
            }
        });





    }
    public void createAlertDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(CurrentUser);

        // Setting Dialog Message
        alertDialog.setMessage("You May have your Meal.\nThanks for your cooperation");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_check_box_black_24dp);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed YES button. Write Logic Here
                reg_no.getText().clear();
                //Toast.makeText(getContext(), "You clicked on YES",Toast.LENGTH_SHORT).show();
            }
        });






        // Showing Alert Message
        alertDialog.show();

    }

    private void myfun() {
    }
    private Date parseDate(String date) {

        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }


    public class AttendanceRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        public ProgressDialog loginDialog = new ProgressDialog( getApplicationContext());
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

           // String name_range="bhaiya_sheet!"+"B"+student_row;
            //ValueRange name=mService.spreadsheets().values().get(spreadsheetId,name_range).execute();
          //  student_name=name.getValues().toString();
            //student_name=student_name.substring(2,student_name.length()-2);
            String student_row= String.valueOf((7+(student_reg_no-1)*3));
            String bk_att=check_atte_range+student_row;
            ValueRange resultes = mService.spreadsheets().values().get(spreadsheetId,bk_att).execute();

            Log.i("Attendance :",resultes.getValues().toString());
            if(meal_type==0)
            {
                String bk_range_att=check_atte_range+student_row;
                ValueRange result = mService.spreadsheets().values().get(spreadsheetId,bk_range_att).execute();


                    data.add(new ValueRange()
                            .setRange(bk_range_att)
                            .setValues(Arrays.asList(
                                    Arrays.asList(1))));


            }
            else if(meal_type==1)
            {
                String ln_range_att=check_atte_range+(student_row+1);
                ValueRange result = mService.spreadsheets().values().get(spreadsheetId, ln_range_att).execute();


                    data.add(new ValueRange()
                            .setRange(ln_range_att)
                            .setValues(Arrays.asList(
                                    Arrays.asList(1))));

            }
            else if (meal_type==2)
            {
                String dn_range_att=check_atte_range+(student_row+2);
                ValueRange result = mService.spreadsheets().values().get(spreadsheetId, dn_range_att).execute();
                if(result.getValues().toString().equals("[[0]]"))
                {

                    data.add(new ValueRange()
                            .setRange(dn_range_att)
                            .setValues(Arrays.asList(
                                    Arrays.asList(1))));
                }
            }



                BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                        .setValueInputOption("RAW")
                        .setData(data);

                BatchUpdateValuesResponse batchResult = this.mService.spreadsheets().values()
                        .batchUpdate(spreadsheetId, batchBody)
                        .execute();

            t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        t1.setLanguage(new Locale("en", "IN"));
                    }
                }
            });
            t1.speak(CurrentUser, TextToSpeech.QUEUE_FLUSH, null);


            return results;
        }



        @Override
        protected void onPreExecute() {
            Log.i("DATA:","1");

        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();



            if (output == null || output.size() == 0) {
                Log.i("DATA:","2");
            } else {
                output.add(0, "Data retrieved using the Google Sheets API:");
                Log.i("DATA:","3");
            }
        }


    }




}
