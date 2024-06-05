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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String res = "";
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;
    private Spinner dropdown;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnE = findViewById(R.id.btnE);
        dropdown = findViewById(R.id.spinnerDepo);
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
        if (saveLogin) {
            txtUsername.setText(loginPreferences.getString("username", ""));
            txtPassword.setText(loginPreferences.getString("password", ""));
//            dropdown.setSelection(loginPreferences.getInt("Depo", 0));
            chkR.setChecked(true);
        }

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        ArrayList<String> depoList = new ArrayList<>();
        depoList.add("Select Depot"); // Adding a default value for the first item

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, depoList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Disable the first item
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? Color.RED : Color.BLACK);
                return view;
            }
        };
        //attaching adapter to listview
        dropdown.setAdapter(arrayAdapter);
//        dropdown.setSelection(loginPreferences.getInt("Depo", 0));
        fetchDepoData(arrayAdapter);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dropdown.getSelectedItemPosition() == 0) {
                    Toast.makeText(getApplicationContext(), "Please Select Depo", Toast.LENGTH_SHORT).show();
                    return;
                }
                BackgroundWorker backgroundWorker = new BackgroundWorker();
                backgroundWorker.execute("login", txtUsername.getText().toString(), txtPassword.getText().toString());
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

    private void fetchDepoData(ArrayAdapter<String> arrayAdapter) {
        Request request = new Request.Builder()
                .url("https://vtc3pl.com/fetch_depotcode_and_names_for_pod_app.php") // Replace with your PHP file URL
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to fetch Depot data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        try {
                            String responseBody = response.body().string();
                            JSONArray jsonArray = new JSONArray(responseBody);
                            ArrayList<String> fetchedDepoList = new ArrayList<>();
                            fetchedDepoList.add("Select Depot"); // Adding a default value for the first item
                            for (int i = 0; i < jsonArray.length(); i++) {
                                fetchedDepoList.add(jsonArray.getString(i));
                            }
                            runOnUiThread(() -> {
                                arrayAdapter.clear();
                                arrayAdapter.addAll(fetchedDepoList);
                                arrayAdapter.notifyDataSetChanged();
                                // Ensure dropdown selection is set after data is loaded
                                int savedDepoPosition = loginPreferences.getInt("Depo", 0);
                                if (savedDepoPosition < fetchedDepoList.size()) {
                                    dropdown.setSelection(savedDepoPosition);
                                }
                            });
                        } catch (JSONException e) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to parse Depot data", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Response body is null", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to fetch Depot data", Toast.LENGTH_SHORT).show());
                }
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