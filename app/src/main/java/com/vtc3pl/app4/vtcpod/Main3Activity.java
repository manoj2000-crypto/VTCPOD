package com.vtc3pl.app4.vtcpod;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Main3Activity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;
    Location mylocation;
    Double latitude, longitude;
    private GoogleApiClient googleApiClient;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        TextView txtloc = findViewById(R.id.txtloc);
        mylocation = location;
        if (mylocation != null) {
            txtloc.append("\n Latitude: " + mylocation.getLatitude() + " Longitude : " + mylocation.getLongitude());
            latitude = mylocation.getLatitude();
            longitude = mylocation.getLongitude();
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    private void getMyLocation() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(Main3Activity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(3000);
                    locationRequest.setFastestInterval(3000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    //builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(googleApiClient, locationRequest, this);
                    PendingResult<LocationSettingsResult> result =
                            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    int permissionLocation = ContextCompat
                                            .checkSelfPermission(Main3Activity.this,
                                                    Manifest.permission.ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                        mylocation = LocationServices.FusedLocationApi
                                                .getLastLocation(googleApiClient);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        status.startResolutionForResult(Main3Activity.this,
                                                REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    break;
                            }
                        }
                    });
                }
            }
        }
    }

    private void checkPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Button buttonE = findViewById(R.id.buttonE);
        checkPermissions();
        setUpGClient();

        buttonE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomDialogClass cdd = new CustomDialogClass(Main3Activity.this);
                cdd.show();
                                           /*DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                               @Override
                                               public void onClick(DialogInterface dialog, int which) {
                                                   switch (which){
                                                       case DialogInterface.BUTTON_POSITIVE:
                                                           Toast.makeText(Main3Activity.this,"Yes Button Clicked.",Toast.LENGTH_SHORT).show();
                                                           //Yes button clicked
                                                           break;

                                                       case DialogInterface.BUTTON_NEGATIVE:
                                                           Toast.makeText(Main3Activity.this,"No Button Clicked.",Toast.LENGTH_SHORT).show();
                                                           //No button clicked
                                                           break;
                                                   }
                                               }
                                           };

                                           AlertDialog.Builder builder = new AlertDialog.Builder(Main3Activity.this);
                                           builder.setMessage("Are you sure?")
                                                   .setPositiveButton("Yes", dialogClickListener)
                                                   .setNegativeButton("No", dialogClickListener)
                                                   .show();*/
            }
        });
    }

    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                //.enableAutoManage(MainActivity.class, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void sendSMS(String phoneNumbers, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        String[] phonenos = phoneNumbers.split(";");

        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();

        for (String phoneno : phonenos) {
            sms.sendTextMessage(phoneno, null, message, sentPI, deliveredPI);
        }
    }

    public class CustomDialogClass extends Dialog implements
            android.view.View.OnClickListener {

        public Activity c;
        public Dialog d;
        public Button yes, no;

        public CustomDialogClass(Activity a) {
            super(a);
            // TODO Auto-generated constructor stub
            this.c = a;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_dialog);
            yes = findViewById(R.id.btn_yes);
            no = findViewById(R.id.btn_no);
            yes.setOnClickListener(this);
            no.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_yes:
                    if (mylocation != null) {
                        GetPhoneNos getPhoneNo = new GetPhoneNos();
                        getPhoneNo.execute();
                    } else
                        Toast.makeText(getApplicationContext(), "Please wait, getting your Current Location...", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_no:
                    Toast.makeText(Main3Activity.this, "No Button Clicked.", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            dismiss();
        }
    }

    class GetPhoneNos extends AsyncTask<String, Void, String> {
        //this method will be called before execution
        //you can display a progress bar or something
        //so that user can understand that he should wait
        //as network operation may take some time
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //this method will be called after execution
        //so here we are displaying a toast with the json string
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            try {
                TextView txtsms = findViewById(R.id.txtsms);
                String Phonenos = s.substring(0, s.indexOf(","));
                String txt = s.substring(s.indexOf(",") + 1);
                txt = txt.substring(0, txt.length() - 1);

                //SmsManager smsManager = SmsManager.getDefault();
                StringBuffer smsBody = new StringBuffer();
                smsBody.append(txt);
                smsBody.append(" Location: 'http://maps.google.com?q=");
                smsBody.append(mylocation.getLatitude());
                smsBody.append(",");
                smsBody.append(mylocation.getLongitude());
                smsBody.append("'");
                txtsms.setText("SMS message sent to : " + Phonenos + "\n" + "Message : \n" + smsBody.toString());
                sendSMS(Phonenos, smsBody.toString());
                //smsManager.sendTextMessage(Phonenos, null, smsBody.toString(), null, null);
                //Toast.makeText(Main3Activity.this, "SMS Successfully Sent.", Toast.LENGTH_LONG).show();
                //Toast.makeText(Main3Activity.this, Phonenos + "\n" + smsBody.toString(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //in this method we are fetching the json string
        @Override
        protected String doInBackground(String... params) {
            //String drsno = params[0];
            //String weburl = "http://subcranial-minuses.000webhostapp.com/vtc3pl/getlrno.php";
            String weburl = "https://vtc3pl.com/getphonenos.php";
            try {
                URL url = new URL(weburl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("code", "UTF-8") + "=" + URLEncoder.encode("473918", "UTF-8"); //+ "&"
                //+ URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(drsno, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result = "";
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}