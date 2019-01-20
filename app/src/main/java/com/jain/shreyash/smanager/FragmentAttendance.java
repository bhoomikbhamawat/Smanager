package com.jain.shreyash.smanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.util.Arrays;

public class FragmentAttendance extends Fragment {

    GoogleAccountCredential mCredential;

    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance, null);

        mCredential = GoogleAccountCredential.usingOAuth2(
                view.getContext().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String accountName = getResources().getString(R.string.pref_account_name);
        mCredential.setSelectedAccountName(accountName);
        MakeRequest mk = new MakeRequest(mCredential);
        String data = mk.getData(view.getContext(),"Sheet1!A1:A2");
        if(data == null)
            data = "null";
        Log.i("DATA:",data);
        return view;
    }
}
