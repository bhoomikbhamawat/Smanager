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
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentOnline extends Fragment {

    GoogleAccountCredential mCredential;
    ArrayList<String> student_details = new ArrayList<String>();
    ArrayList<String> cancel_date = new ArrayList<String>();
    ArrayList<Integer> row_list= new ArrayList<>();
    ArrayList<Integer> date_column= new ArrayList<>();

    ArrayList<Integer> student_reg_no= new ArrayList<>();
    ArrayList<Boolean> ck_list_bk = new ArrayList<Boolean>();
    ArrayList<Boolean> ck_list_ln = new ArrayList<Boolean>();
    ArrayList<Boolean> ck_list_dn = new ArrayList<Boolean>();
    Button btngetreq;
    int position=-1;



    ListView listView;
    private static final String PREF_ACCOUNT_NAME = "accountName";


    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online, container, false);
        listView = (ListView) view.findViewById(R.id.listview_on_online);
        mCredential = GoogleAccountCredential.usingOAuth2(
                view.getContext().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        // String accountName = getResources().getString(R.string.pref_account_name);
        // = getActivity().getPreferences(Context.MODE_PRIVATE)
        // .getString(PREF_ACCOUNT_NAME, null);
        String accountName="";
        btngetreq=view.findViewById(R.id.cancel_info_button);
        btngetreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MakeOnlineCancelRequestTask(mCredential).execute();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                position=i;
                Toast.makeText(getContext(), "Selected: " + i, Toast.LENGTH_SHORT).show();
                createAlertDialog();

            }
        });

        accountName="sattvikmess@gmail.com";
        mCredential.setSelectedAccountName(accountName);
        //if it is DashboardFragment it should have R.layout.fragment_dashboard
        return view;
    }

    public class MakeOnlineCancelRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        public ProgressDialog loginDialog = new ProgressDialog( getContext());
        public Boolean flag;

        MakeOnlineCancelRequestTask(GoogleAccountCredential credential) {
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

            String range = "cancel_sheet!C5:M";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {

                int getrow=0;
                for (List row : values) {
                    getrow=getrow+1;
                    if (row.get(0).toString().isEmpty()|| getrow>1500|| (row.get(9)+"").equals("#N/A") ){
                        Log.d("details ","khatam");

                        break;

                    }
                    Log.d("details",row.get(7)+" : "+ row.get(0)+" : "+(row.get(6)).toString());
                    if(Integer.valueOf((row.get(6)).toString())==-1){
                        student_reg_no.add(Integer.valueOf(row.get(7)+""));
                        student_details.add(row.get(7)+" : "+ row.get(0));
                        cancel_date.add(row.get(10)+"");
                        Log.i("Ye date column",row.get(9)+"");
                        date_column.add(Integer.valueOf(row.get(9)+""));
                        row_list.add(getrow);
                        if (row.get(2).toString().equals("1"))
                        ck_list_bk.add(true);
                        else ck_list_bk.add(false);
                        if (row.get(3).toString().equals("1"))
                            ck_list_ln.add(true);
                        else ck_list_ln.add(false);
                        if (row.get(4).toString().equals("1"))
                            ck_list_dn.add(true);
                        else ck_list_dn.add(false);


                    }
                }
            }
            return results;
        }



        @Override
        protected void onPreExecute() {
            Log.i("DATA:","1");
            loginDialog.setMessage("Getting requests");
            loginDialog.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            int num_dates=student_details.size();
            params.height = (210)*num_dates;
            String[] student_stringArray = student_details.toArray(new String[0]);
            String[] cancel_date_stringArray = cancel_date.toArray(new String[0]);
            Boolean[] bk_list = ck_list_bk.toArray(new Boolean[0]);
            Boolean[] ln_list = ck_list_ln.toArray(new Boolean[0]);
            Boolean[] dn_list = ck_list_dn.toArray(new Boolean[0]);
            OnlineCustomListAdapter whatever = new OnlineCustomListAdapter(getActivity(),student_stringArray,cancel_date_stringArray,bk_list,ln_list,dn_list);
            listView.setLayoutParams(params);
            listView.requestLayout();
            listView.setAdapter(whatever);
            loginDialog.dismiss();
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            alertDialog.setTitle("In this list");
            alertDialog.setMessage("Click on Request to Accept");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                           // student_reg_no.getText().clear();
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



    public class ChangeOnlineCancelRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        public ProgressDialog loginDialog = new ProgressDialog( getContext());
        public Boolean flag;

        ChangeOnlineCancelRequestTask(GoogleAccountCredential credential) {
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

            String gettherow="cancel_sheet!"+"I"+(row_list.get(position)+4)+"";
            //String range = "cancel_sheet!C5:M";
            List<String> results = new ArrayList<String>();
            StringBuilder columnName = new StringBuilder();
            int columnNumber= date_column.get(position);

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
            String getcolumn= "bhaiya_sheet!"+String.valueOf(columnName.reverse());
            int bhaiya_sheet_row=7+(student_reg_no.get(position)-1)*3;
            Log.i("Range :",gettherow);
            List<ValueRange> data = new ArrayList<>();
            data.add(new ValueRange()
                    .setRange(gettherow)
                    .setValues(Arrays.asList(
                            Arrays.asList(1))));
            if(ck_list_bk.get(position)){
                String bk_range=getcolumn+bhaiya_sheet_row;
                data.add(new ValueRange()
                        .setRange(bk_range)
                        .setValues(Arrays.asList(
                                Arrays.asList(0))));
            }
            if(ck_list_ln.get(position)){
                String bk_range=getcolumn+(bhaiya_sheet_row+1);
                data.add(new ValueRange()
                        .setRange(bk_range)
                        .setValues(Arrays.asList(
                                Arrays.asList(0))));
            }
            if(ck_list_dn.get(position)){
                String bk_range=getcolumn+(bhaiya_sheet_row+2);;
                data.add(new ValueRange()
                        .setRange(bk_range)
                        .setValues(Arrays.asList(
                                Arrays.asList(0))));
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
            loginDialog.setMessage("Cancelling Meals");
            loginDialog.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();
            student_details.remove(position);
            cancel_date.remove(position);
            ck_list_bk.remove(position);
            ck_list_ln.remove(position);
            ck_list_dn.remove(position);
            row_list.remove(position);
            date_column.remove(position);
            student_reg_no.remove(position);

            position=-1;
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            int num_dates=student_details.size();
            params.height = (165)*num_dates;
            String[] student_stringArray = student_details.toArray(new String[0]);
            String[] cancel_date_stringArray = cancel_date.toArray(new String[0]);
            Boolean[] bk_list = ck_list_bk.toArray(new Boolean[0]);
            Boolean[] ln_list = ck_list_ln.toArray(new Boolean[0]);
            Boolean[] dn_list = ck_list_dn.toArray(new Boolean[0]);
            OnlineCustomListAdapter whatever = new OnlineCustomListAdapter(getActivity(),student_stringArray,cancel_date_stringArray,bk_list,ln_list,dn_list);
            listView.setLayoutParams(params);
            listView.requestLayout();
            listView.setAdapter(whatever);
            loginDialog.dismiss();
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            alertDialog.setTitle("DONE !");
            alertDialog.setMessage("Request Accepted");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // student_reg_no.getText().clear();
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

    public void createAlertDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("Accept Request ?");

        // Setting Dialog Message
        alertDialog.setMessage("Change acceptance status to 1 ");

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.ic_check_box_black_24dp);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed YES button. Write Logic Here

                new ChangeOnlineCancelRequestTask(mCredential).execute();
                //Toast.makeText(getContext(), "You clicked on YES",Toast.LENGTH_SHORT).show();
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed No button. Write Logic Here

                Toast.makeText(getContext(), "You clicked on NO", Toast.LENGTH_SHORT).show();
            }
        });




        // Showing Alert Message
        alertDialog.show();

    }


}
