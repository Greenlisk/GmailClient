package com.example.green.gmailclient;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.green.gmailclient.adapters.ThreadAdapter;
import com.example.green.gmailclient.database.MessageEntry;
import com.example.green.gmailclient.database.ThreadEntry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.Profile;
import com.google.api.services.gmail.model.Thread;

import java.io.IOException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.realm.Realm;
import io.realm.Sort;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ListView.OnItemClickListener,
                    EasyPermissions.PermissionCallbacks{
    private final static String LOG_TAG = "MainActivity";
    private GoogleAccountCredential mCredential;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { GmailScopes.GMAIL_READONLY};
    private ThreadAdapter threadAdapter;
    private Realm realm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        realm = Realm.getDefaultInstance();
        ListView threadsList = (ListView)findViewById(R.id.threads_list);
        threadAdapter = new ThreadAdapter(this, realm.where(ThreadEntry.class).findAll());
        threadsList.setAdapter(threadAdapter);
        threadsList.setOnItemClickListener(this);

        getResultsFromApi();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(this, ActivityChat.class);
        intent.putExtra("id", threadAdapter.getItem(i).getId());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_account) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {

                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }


    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }


    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {

        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }


    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }


    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Gmail API Android Quickstart")
                    .build();

        }

        /**
         * Background task to call Gmail API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {

            try {
                Profile profile = mService.users().getProfile("me").execute();
                getSharedPreferences(GmailClient.prefsName, MODE_PRIVATE).edit().putString("email", profile.getEmailAddress()).apply();
                return getDataFromApi();
            } catch (Exception e) {
                Log.v(LOG_TAG, e.toString());
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of Gmail labels attached to the specified account.
         * @return List of Strings labels.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get the labels in the user's account.
            Realm realm = Realm.getDefaultInstance();
            String user = "me";
            Log.v(LOG_TAG, "Getting data");
            ListThreadsResponse threads = mService.users().threads().list(user).execute();
            int count = 0;
            for(Thread thread : threads.getThreads()){
                Log.v(LOG_TAG, "Getting thread");
                ThreadEntry threadEntry = new ThreadEntry(thread);

                List<Message> messages = mService.users().threads().get(user,thread.getId()).execute().getMessages();
                Set<String> fromSet = null;
                long lastDate = 0;
                for(Message message : messages){
                    message = mService.users().messages().get(user, message.getId()).execute();
                    lastDate = 0;
                    MessageEntry messageEntry = new MessageEntry();
                    messageEntry.setInternalDate(message.getInternalDate());
                    if(message.getInternalDate() > lastDate){
                        lastDate = message.getInternalDate();
                    }
                    messageEntry.setId(message.getId());
                    messageEntry.setThreadId(message.getThreadId());
                    messageEntry.setBody(message.getSnippet());
                    messageEntry.setInternalDate(message.getInternalDate());
                    messageEntry.setBody(message.getRaw());
                    //Log.v(LOG_TAG, "Body: \n" );
                    MessagePart part  = message.getPayload();
                    byte[] decoded = Base64.decodeBase64(message.getPayload().getParts().get(0).getBody().getData());
                    if(decoded != null) {
                        String body = new String(decoded);
                        if(body.length() > 20){
                            body = body.substring(0, 20);
                        }
                        messageEntry.setBody(body);
                    }

                    Log.v(LOG_TAG, "PArt: \n" + message.getId() );
                    List<MessagePartHeader> headers = part.getHeaders();
                    for(MessagePartHeader header : headers){
                        fromSet = new TreeSet<>();
                        if(header.getName().equals("From")){
                            messageEntry.setFrom(header.getValue());
                            fromSet.add(header.getValue());
                        } else if(header.getName().equals("Subject") && threadEntry.getSubject() == null){
                            threadEntry.setSubject(header.getValue());
                        }
                        //Log.v(LOG_TAG, header.getName());
                    }
                    Log.v(LOG_TAG, " \n--------------\n");
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(messageEntry);
                    realm.commitTransaction();
                }
                if(fromSet != null) {
                    threadEntry.setFrom(Arrays.toString(fromSet.toArray()));
                }
                threadEntry.setLastDate(lastDate);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(threadEntry);
                realm.commitTransaction();
                ++count;
                if(count == 10){
                    break;
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(List<String> output) {
            threadAdapter.updateData(realm.where(ThreadEntry.class).findAllSorted("lastDate", Sort.DESCENDING));
            if (output == null || output.size() == 0) {

            }
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {

                }
            } else {
            }
        }


    }

}
