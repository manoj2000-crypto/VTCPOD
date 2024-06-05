package com.vtc3pl.app4.vtcpod;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class MainActivity extends AppCompatActivity {

    String res = "";
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnE = findViewById(R.id.btnE);
        Spinner dropdown = findViewById(R.id.spinnerDepo);
        ImageView imageView1 = findViewById(R.id.imageView1);
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vtc3pl.com"));
                startActivity(browserIntent);
            }
        });

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        EditText txtUsername = findViewById(R.id.txtUsername);
        EditText txtPassword = findViewById(R.id.txtPassword);
        CheckBox chkR = findViewById(R.id.chkR);

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            txtUsername.setText(loginPreferences.getString("username", ""));
            txtPassword.setText(loginPreferences.getString("password", ""));
            dropdown.setSelection(loginPreferences.getInt("Depo", 0));
            chkR.setChecked(true);
        }

        //the array adapter to load data into list
        String[] Depo = {"Please Select Depo",
                "PNA-PUNE",
                "NSK-NASHIK",
                "AKL-AKOLA",
                "AUR-AURANGABAD",
                "SHV-SHIVARI",
                "KOP-KOLHAPUR",
                "SGN-SANGAMNER",
                "NAG-NAGPUR",
                "BRS-BARSHI",
                "JBL-JABALPUR",
                "PDR-PANDHARPUR",
                "ISL-ISLAMPUR",
                "SOL-SOLAPUR",
                "SGL-SANGLI",
                "URL-URULIDEVACHI",
                "ANK-ANKLESHWAR",
                "ASL-ASLALI",
                "BEL-BELLARY",
                "BNH-BANGLORE",
                "BRD-BARODA",
                "HYD-HYDERABAD",
                "IND-INDORE",
                "NAG-NAGPUR",
                "JNPT-NAVA-SHIVA",
                "TRI-TRICHY",
                "OZAR-OZAR",
                "JLN-JALNA",
                "STN-SATPUR NASHIK",
                "NAN-NANDED",
                "PBN-PARBHANI",
                "AKJ-AKLUJ",
                "BIJ-BAIJAPUR",
                "KLG-KHALGHAT DHAR",
                "WGL-WAGHOLI",
                "LCK-LUCKNOW",
                "JAI-JAIPUR",
                "PCV-PANCHAVATI NASHIK",
                "GZB-GHAZIABAD UP",
                "BWD-BHIWANDI THANE",
                "GNT-GUNTUR",
                "BBRD-BARAMATI",
                "HDD-HANDEWADI",
                "NRG-NARAYANGAON",
                "JJR-JEJURI",
                "BBRM-BARAMATI",
                "SNR-SINNER",
                "GWH-GUWAHATI",
                "HSR-HISSAR",
                "PTN-PATNA",
                "JSP-JAMSHEDPUR",
                "LDH-LUDHIANA",
        };
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, Depo) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the First item from Spinner
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the disable item text color
                    tv.setTextColor(Color.RED);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        //attaching adapter to listview
        dropdown.setAdapter(arrayAdapter);

        dropdown.setSelection(loginPreferences.getInt("Depo", 0));

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Spinner dropdown = findViewById(R.id.spinnerDepo);
                Intent myIntent = new Intent(MainActivity.this, Main2Activity.class);
                myIntent.putExtra("Depo", dropdown.getSelectedItem().toString());
                startActivity(myIntent);*/

                EditText txtUsername = findViewById(R.id.txtUsername);
                EditText txtPassword = findViewById(R.id.txtPassword);
                String type = "login";
                Spinner spinnerDepo = findViewById(R.id.spinnerDepo);
                if (spinnerDepo.getSelectedItemPosition() == 0) {
                    Toast.makeText(getApplicationContext(), "Please Select Depo", Toast.LENGTH_SHORT).show();
                    return;
                }
                BackgroundWorker backgroundWorker = new BackgroundWorker();
                backgroundWorker.execute(type, txtUsername.getText().toString(), txtPassword.getText().toString());
            }
        });

        btnE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, Main3Activity.class);
                startActivity(myIntent);
            }
        });
    }

    public void changeScreen() {
        EditText txtUsername = findViewById(R.id.txtUsername);
        EditText txtPassword = findViewById(R.id.txtPassword);
        CheckBox chkR = findViewById(R.id.chkR);
        Spinner dropdown = findViewById(R.id.spinnerDepo);
        //TextView textView1= findViewById(R.id.textView2);
        if (res.matches("Success")) {
            if (chkR.isChecked()) {
                loginPrefsEditor.putBoolean("saveLogin", true);
                loginPrefsEditor.putString("username", txtUsername.getText().toString());
                loginPrefsEditor.putString("password", txtPassword.getText().toString());
                loginPrefsEditor.putInt("Depo", dropdown.getSelectedItemPosition());
                loginPrefsEditor.commit();
            } else {
                loginPrefsEditor.clear();
                loginPrefsEditor.commit();
            }
            Intent myIntent = new Intent(this, Main4Activity.class);
            myIntent.putExtra("Depo", dropdown.getSelectedItem().toString());
            startActivity(myIntent);
        } else {
            Log.d("TAG", "Value of res in else part: " + res);
            Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
        }
    }

    public class BackgroundWorker extends AsyncTask<String, Void, String> {
/*
        Context context;
        AlertDialog alertDialog;

        BackgroundWorker(Context ctx) {
            context = ctx;
        }
*/

        @Override
        protected String doInBackground(String... params) {
            String type = params[0];
            //String login_url = "http://subcranial-minuses.000webhostapp.com/logintest.php";
            String login_url = "https://vtc3pl.com/applogin.php";
            if (type.equals("login")) {
                try {
                    String user_name = params[1];
                    String password = params[2];
                    URL url = new URL(login_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("user_name", "UTF-8") + "=" + URLEncoder.encode(user_name, "UTF-8") + "&"
                            + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
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
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
/*
            alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Login Status");
*/
        }

        @Override
        protected void onPostExecute(String result) {
            res = result;
            changeScreen();
        /*    alertDialog.setMessage(result);
            alertDialog.show();*/
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

}