package com.jain.shreyash.smanager;

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
    private Date dateCompareTwo;
    int meal_type; //0 for breakfast,1 for lunch,2 for dinner
    private Date dateCompareThree;
    private Date dateCompareFour;

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
        name_data_local=FragmentAttendance.name_data;
        Log.i("local array ",diet_data_local.toString());

        reg_no=findViewById(R.id.local_edit_text);

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
            meal_of_day.setText("Breakfast");

        }
        else if ( dateCompareTwo.before( date ) && dateCompareThree.after(date)){

            meal_type=1;
            meal_of_day.setText("Lunch");
        }else if ( dateCompareThree.before( date ) && dateCompareFour.after(date)){

            meal_type=2;
            meal_of_day.setText("Dinner");
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
                    name.setText(name_data_local.get(student_reg_no-1));

                    if(diet_data_local.get(1+meal_type+(student_reg_no-1)*3)==1){
                        status_att.setText("You May have your Meal.\nThanks for your cooperation");
                    }

                }
            }
        });





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
}
