package com.jain.shreyash.smanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.util.ArrayList;
import java.util.List;

public class MakeRequest {

    private com.google.api.services.sheets.v4.Sheets mService = null;
    ProgressDialog mProgress;

    private static final String spreadsheetId = "1Hy8P3o52wh7i1BpwbgvXMKtMliC8rldgtPQIYQ5ru6g";
    MakeRequest(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Sheets API Android Quickstart")
                .build();
    }

    String getData(Context context, String range) {
        try {
            mProgress = new ProgressDialog(context);
            mProgress.setMessage("Getting Data...");
            List<String> results = new ArrayList<>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            for (List row : values) {
                int i=0;
                while(i<row.size()) {
                    results.add(row.get(i)+" ");
                }
            }
            mProgress.hide();
            return results.toString();

        } catch (UserRecoverableAuthIOException e){
            mProgress.hide();
            ((Activity) context).startActivityForResult(
                    e.getIntent(), MainActivity.REQUEST_AUTHORIZATION
            );
        } catch (Exception e) {
            String msg = "null";
            if(e.getMessage()!=null)
                msg = e.getMessage();
            Log.e("Error occurred:", msg);
            mProgress.hide();
            //todo: Create a pop up for process not complete
        }
        return null;
    }
}