package com.jain.shreyash.smanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.util.Arrays;

public class FragmentAttendance extends Fragment {

    GoogleAccountCredential mCredential;
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

        accountName="sattvikmess@gmail.com";
        mCredential.setSelectedAccountName(accountName);
        //if it is DashboardFragment it should have R.layout.fragment_dashboard
        return view;
    }
}
