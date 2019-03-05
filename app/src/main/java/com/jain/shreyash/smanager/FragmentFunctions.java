package com.jain.shreyash.smanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class FragmentFunctions extends Fragment {

    GoogleAccountCredential mCredential;
    int choise_attendance = -1;
    int this_month, warn_user = -1;
    TextToSpeech t1;
    String student_name;
    int today_column, student_row;
    EditText student_reg_no;
    String getcolumn;
    private static final String PREF_ACCOUNT_NAME = "accountName";


    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //just change the fragment_dashboard
        //with the fragment you want to inflate
        //like if the class is HomeFragment it should have R.layout.home_fragment


        View view = inflater.inflate(R.layout.fragment_functions, container, false);
        mCredential = GoogleAccountCredential.usingOAuth2(
                view.getContext().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        // String accountName = getResources().getString(R.string.pref_account_name);
        // = getActivity().getPreferences(Context.MODE_PRIVATE)
        // .getString(PREF_ACCOUNT_NAME, null);
        String accountName = "";
        //student_reg_no = view.findViewById(R.id.reg_no_on_attendance);
        //Button chk_att = view.findViewById(R.id.chk_att);

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
        Log.i("Here : ", reportMonth);
        String reportYear = df_year.format(c);

        int repot_int_day = Integer.valueOf(reportDate);
        int repot_int_month = Integer.valueOf(reportMonth);
        this_month = repot_int_month;
        Log.i("Here 2 : ", repot_int_month + "");
        int repot_int_year = Integer.valueOf(reportYear);
        today_column = repot_int_day + 7 + (repot_int_month - 1) * 31;
        //String formattedDate = df.format(c);


        accountName = "sattvikmess@gmail.com";
        mCredential.setSelectedAccountName(accountName);

      //  RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.RGroup);

        StringBuilder columnName = new StringBuilder();
        int columnNumber = today_column;

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
        getcolumn = String.valueOf(columnName.reverse());

        //if it is DashboardFragment it should have R.layout.fragment_dashboard
        return view;
    }
}
