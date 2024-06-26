package com.vtc3pl.app4.vtcpod;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static androidx.core.content.FileProvider.getUriForFile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Main2Activity extends AppCompatActivity {

    public static final int RequestPermissionCode = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ProgressDialog dialog = null;
    int serverResponseCode = 0;
    List<Integer> pos = new ArrayList<Integer>();
    List<Integer> posv = new ArrayList<Integer>();
    List<Integer> posb = new ArrayList<Integer>();
    String upLoadServerUri = "https://vtc3pl.com/upload.php";
    private String pictureImagePath = "", drsno = "";
    private Spinner spinnerDepo;
    private OkHttpClient client;
    //String upLoadServerUri = "http://subcranial-minuses.000webhostapp.com/upload.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Button btn1 = findViewById(R.id.btn1);
        Button btn2 = findViewById(R.id.btn2);
        Button btn3 = findViewById(R.id.btn3);
        Button btnaUpload = findViewById(R.id.btnaUpload);
        //String Depo = getIntent().getStringExtra("Depo");
        //TextView txtDepo = findViewById(R.id.txtDepo);
        Spinner spinnerYear = findViewById(R.id.spinnerYear);
        spinnerDepo = findViewById(R.id.Depo);
        Spinner spinnerVehicleHO = findViewById(R.id.VehicleHO);
        Spinner spinnerMonth = findViewById(R.id.Month);

        //txtDepo.setText("DS/PNA"); //+ Depo.substring(0,4).trim()
        EnableRuntimePermission();
        String[] VehicleHO = {"H", "O"};

        String[] Month = {"April - A", "May - B", "June - C", "July - D", "August - E", "September - F", "October - G", "November - H", "December - I", "January - J", "February - K", "March - L"};

        String[] Year = {"24-25", "25-26", "26-27", "27-28", "28-29", "29-30", "30-31", "31-32"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Main2Activity.this, android.R.layout.simple_spinner_dropdown_item, Year);
        ArrayAdapter<String> arrayAdapterVehicleHO = new ArrayAdapter<>(Main2Activity.this, android.R.layout.simple_spinner_dropdown_item, VehicleHO);
        ArrayAdapter<String> arrayAdapterMonth = new ArrayAdapter<>(Main2Activity.this, android.R.layout.simple_spinner_dropdown_item, Month);
        //attaching adapter to listview
        spinnerYear.setAdapter(arrayAdapter);
        spinnerYear.setSelection(2);

        spinnerVehicleHO.setAdapter(arrayAdapterVehicleHO);
        spinnerVehicleHO.setSelection(0);

        spinnerMonth.setAdapter(arrayAdapterMonth);

        // Get the current month
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        Log.e("CurrentMonth", String.valueOf(Calendar.MONTH));

// Map the Calendar month to your custom Month array index
        int monthIndex;
        switch (currentMonth) {
            case Calendar.JANUARY:
                monthIndex = 9;
                break;
            case Calendar.FEBRUARY:
                monthIndex = 10;
                break;
            case Calendar.MARCH:
                monthIndex = 11;
                break;
            case Calendar.APRIL:
                monthIndex = 0;
                break;
            case Calendar.MAY:
                monthIndex = 1;
                break;
            case Calendar.JUNE:
                monthIndex = 2;
                break;
            case Calendar.JULY:
                monthIndex = 3;
                break;
            case Calendar.AUGUST:
                monthIndex = 4;
                break;
            case Calendar.SEPTEMBER:
                monthIndex = 5;
                break;
            case Calendar.OCTOBER:
                monthIndex = 6;
                break;
            case Calendar.NOVEMBER:
                monthIndex = 7;
                break;
            case Calendar.DECEMBER:
                monthIndex = 8;
                break;
            default:
                monthIndex = 0; // Default to the first month if somehow the current month isn't recognized
        }

// Set the spinner selection to the current month
        spinnerMonth.setSelection(monthIndex);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        fetchDepoData();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openCamera();
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner spinner1 = findViewById(R.id.spinner1);
                if (spinner1.getSelectedItem() == null) {
                    Toast.makeText(getApplicationContext(), "Please Select LR No.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (posv.contains(spinner1.getSelectedItemPosition())) {
                    Toast.makeText(getApplicationContext(), "LRNo image is already verified. Please select other LRNo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pictureImagePath == "") {
                    Toast.makeText(getApplicationContext(), "please take a photo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog = ProgressDialog.show(Main2Activity.this, "", "Uploading file...", true);
                BackgroundWorker backgroundWorker = new BackgroundWorker(Main2Activity.this);
                backgroundWorker.execute();
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TextView txtDepo = findViewById(R.id.txtDepo);
                Spinner spinnerDepo = findViewById(R.id.Depo);
                Spinner spinnerYear = findViewById(R.id.spinnerYear);
                Spinner spinnerVehicleHO = findViewById(R.id.VehicleHO);
                Spinner spinnerMonth = findViewById(R.id.Month);
                EditText editText1 = findViewById(R.id.editText1);

                String depoStr = spinnerDepo.getSelectedItem().toString();
                String yearStr = spinnerYear.getSelectedItem().toString();
                String vehicleHOStr = spinnerVehicleHO.getSelectedItem().toString();
                String monthStr = spinnerMonth.getSelectedItem().toString();
                String lastTwoCharsOfYear = yearStr.substring(yearStr.length() - 2);

                String str = vehicleHOStr + monthStr.charAt(monthStr.length() - 1) + depoStr + lastTwoCharsOfYear + editText1.getText().toString();
                if (str.matches("")) {
                    Toast.makeText(getApplicationContext(), "please enter DRS NO.", Toast.LENGTH_LONG).show();
                    return;
                }
                drsno = "D" + str;
                Toast.makeText(getApplicationContext(), "DRS No: " + drsno, Toast.LENGTH_LONG).show();
                GetJSON getJSON = new GetJSON();
                getJSON.execute(drsno);
            }
        });
        btnaUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Main2Activity.this, Main4Activity.class);
                startActivity(myIntent);
            }
        });
    }

    private void fetchDepoData() {
        Request request = new Request.Builder()
                .url("https://vtc3pl.com/fetch_depotcode_and_names_for_pod_app.php") // Replace with your PHP file URL
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(Main2Activity.this, "Failed to fetch Depot data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        try {
                            String responseBody = response.body().string();
                            JSONArray jsonArray = new JSONArray(responseBody);
                            ArrayList<String> depoList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String fullString = jsonArray.getString(i);
                                String trimmedPart = fullString.split("-")[0].trim(); // Extract and trim the part before "-"
                                depoList.add(trimmedPart);
                            }
                            runOnUiThread(() -> {
                                ArrayAdapter<String> arrayAdapterDepo = new ArrayAdapter<>(Main2Activity.this, android.R.layout.simple_spinner_dropdown_item, depoList);
                                Log.e("Values", arrayAdapterDepo.toString());
                                spinnerDepo.setAdapter(arrayAdapterDepo);
                            });
                        } catch (JSONException e) {
                            runOnUiThread(() -> Toast.makeText(Main2Activity.this, "Failed to parse Depot data", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(Main2Activity.this, "Response body is null", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(Main2Activity.this, "Failed to fetch Depot data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            File imagePath = new File(getFilesDir(), "images");
            File imgFile = new File(imagePath, "temp.jpg");
            if (imgFile.exists()) {
                pictureImagePath = imgFile.getAbsolutePath();
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                Bitmap newBitmap = scaleDown(myBitmap, 1280, true);
                ImageView imageView1 = findViewById(R.id.imageView1);
                imageView1.setVisibility(View.VISIBLE);
                imageView1.setImageBitmap(newBitmap);
                try {
                    FileOutputStream fos = new FileOutputStream(imgFile);
                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ScrollView mScrollView = findViewById(R.id.mScrollView);
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }
    }

    public void uploadFile() {
        EditText editText1 = findViewById(R.id.editText1);

        File sourceFile = new File(pictureImagePath);
        String fileName = editText1.getText().toString() + ".jpg";
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        if (!sourceFile.isFile()) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            Log.e("uploadFile", "Source File not exist :" + pictureImagePath);
            //textView1.append("Source File not exist :" + fileName);
        } else {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);
                //conn.setRequestProperty("image_name", editText1.getText().toString());

                dos = new DataOutputStream(conn.getOutputStream());


                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.flush();

                // Upload POST Data
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"image_name\"" + lineEnd);
                dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(fileName.substring(0, fileName.length() - 4));
                dos.writeBytes(lineEnd);

                dos.flush();
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                dos.flush();
                dos.close();
                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

                //close the streams //
                fileInputStream.close();
            } catch (MalformedURLException ex) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                ex.printStackTrace();
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                e.printStackTrace();
            }
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            Log.i("program", "program end.");
        } // End else block
    }

    private void openCamera() {
        //Get reference to the file
        File imagePath = new File(getFilesDir(), "images");
        if (!imagePath.exists())
            imagePath.mkdir();
        File file = new File(imagePath, "temp.jpg");

        if (file != null) {
            final Uri outputFileUri = getUriForFile(getApplicationContext(), "com.vtc3pl.app4.vtcpod.fileprovider", file);
            //Uri outputFileUri= Uri.fromFile(file);
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                List<ResolveInfo> resolvedIntentActivities = getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                    String packageName = resolvedIntentInfo.activityInfo.packageName;
                    grantUriPermission(packageName, outputFileUri,
                            FLAG_GRANT_WRITE_URI_PERMISSION | FLAG_GRANT_READ_URI_PERMISSION);
                    cameraIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                    cameraIntent.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(cameraIntent, 1);
            }
        }
    }

    public Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        if (realImage.getWidth() < realImage.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            realImage = Bitmap.createBitmap(realImage, 0, 0, realImage.getWidth(), realImage.getHeight(), matrix, true);
        }
        float ratio = Math.min((float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        if (ratio > 1)
            ratio = 1;
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, filter);
        return newBitmap;
    }

    public void EnableRuntimePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(Main2Activity.this,
                Manifest.permission.CAMERA)) {
        } else {
            ActivityCompat.requestPermissions(Main2Activity.this, new String[]{
                    Manifest.permission.CAMERA}, RequestPermissionCode);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void loadIntoSpinner(String json) throws JSONException {
        //creating a json array from the json string
        int nextpos = 0;
        JSONArray jsonArray = new JSONArray(json);

        if (jsonArray.length() == 0) {
            Toast.makeText(getApplicationContext(), "DRS No. not found.", Toast.LENGTH_SHORT).show();
            drsno = "";
            return;
        }

        Spinner dropdown = findViewById(R.id.spinner1);
        dropdown.setAdapter(null);
        pos.clear();
        posv.clear();
        //creating a string array for listview
        String[] LRNO = new String[jsonArray.length()];

        //looping through all the elements in json array
        for (int i = 0; i < jsonArray.length(); i++) {

            //getting json object from the json array
            JSONObject obj = jsonArray.getJSONObject(i);

            //getting the name from the json object and putting it inside string array
            LRNO[i] = obj.getString("lrno");
            if (obj.getInt("uploaded") == 1)
                pos.add(i);
            if (obj.getInt("uploaded") == 0 && nextpos == 0)
                nextpos = i;
            if (obj.getInt("verified") == 1)
                posv.add(i);
            if (obj.getInt("verified") == 2)
                posb.add(i);
        }

        //the array adapter to load data into list
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Main2Activity.this, android.R.layout.simple_spinner_dropdown_item, LRNO) {
            @Override
            public boolean isEnabled(int position) {
                if (posv.contains(position)) {
                    // Disable the second item from Spinner
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
                if (posv.contains(position)) {
                    // Set the disable item(Verified LR) text color
                    tv.setTextColor(Color.GREEN);
                } else if (posb.contains(position)) {
                    // Set the bad LR item text color
                    tv.setTextColor(Color.RED);
                } else if (pos.contains(position)) {
                    // Set the uploaded item text color
                    tv.setTextColor(Color.BLUE);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        //attaching adapter to listview
        dropdown.setAdapter(arrayAdapter);
        dropdown.setSelection(nextpos);
        Toast.makeText(getApplicationContext(), "LR No. Loaded in the List.", Toast.LENGTH_SHORT).show();
    }

    public class BackgroundWorker extends AsyncTask<String, Void, String> {
        Context context;
        AlertDialog.Builder alertDialog;

        BackgroundWorker(Context ctx) {
            context = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            //String result="Photo Not Uploaded. Try again.";
            String result = "";
            //EditText editText1 = findViewById(R.id.editText1);
            Spinner spinner = (Spinner) findViewById(R.id.spinner1);
            File sourceFile = new File(pictureImagePath);
            String fileName = spinner.getSelectedItem().toString() + ".jpg";
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;

            if (!sourceFile.isFile()) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.e("uploadFile", "Source File not exist :" + pictureImagePath);
                //textView1.append("Source File not exist :" + fileName);
            } else {
                try {
                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(upLoadServerUri);
                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.flush();

                    // Upload POST Data
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"image_name\"" + lineEnd);
                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(fileName.substring(0, fileName.length() - 4));
                    dos.writeBytes(lineEnd);
                    dos.flush();

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"DRSNO\"" + lineEnd);
                    dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(drsno);
                    dos.writeBytes(lineEnd);
                    dos.flush();

                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    dos.flush();
                    dos.close();
                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);

                    InputStream inputStream = conn.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        result += line;
                    }
                    bufferedReader.close();
                    inputStream.close();
                    conn.disconnect();
                    //close the streams //
                    fileInputStream.close();
                } catch (MalformedURLException ex) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    ex.printStackTrace();
                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                } catch (Exception e) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    e.printStackTrace();
                }
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.i("program", "program end.");

            } // End else block
            return result;
        }

        @Override
        protected void onPreExecute() {
            alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Upload Status");
            alertDialog.setPositiveButton("OK", null);
        }

        @Override
        protected void onPostExecute(String result) {
            Spinner spinner1 = findViewById(R.id.spinner1);
            ImageView imageView1 = findViewById(R.id.imageView1);
            alertDialog.setMessage(result);
            alertDialog.show();
            if (result.matches("Successfully Uploaded Photo.")) {
                pictureImagePath = "";
                int nextpos = 0;
                try {
                    List<String> sitems = new ArrayList<String>();
                    if (!pos.contains(spinner1.getSelectedItemPosition())) {
                        pos.add(spinner1.getSelectedItemPosition());
                    }
                    SpinnerAdapter Sadapter = spinner1.getAdapter();
                    for (int i = 0; i < Sadapter.getCount(); i++) {
                        sitems.add(Sadapter.getItem(i).toString());
                        if (!pos.contains(i) && nextpos == 0)
                            nextpos = i;
                    }
                    //sitems.remove(spinner1.getSelectedItem());
                    String[] LRNO = new String[sitems.size()];
                    sitems.toArray(LRNO);
                    //the array adapter to load data into list
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Main2Activity.this, android.R.layout.simple_spinner_dropdown_item, LRNO) {
                        @Override
                        public boolean isEnabled(int position) {
                            if (posv.contains(position)) {
                                // Disable the item from Spinner
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
                            if (posv.contains(position)) {
                                // Set the disable item(Verified LR) text color
                                tv.setTextColor(Color.GREEN);
                            } else if (posb.contains(position)) {
                                // Set the bad LR item text color
                                tv.setTextColor(Color.RED);
                            } else if (pos.contains(position)) {
                                // Set the uploaded item text color
                                tv.setTextColor(Color.BLUE);
                            } else {
                                tv.setTextColor(Color.BLACK);
                            }
                            return view;
                        }
                    };
                    //attaching adapter to listview
                    spinner1.setAdapter(arrayAdapter);
                    spinner1.setSelection(nextpos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //imageView1.setImageResource(R.drawable.ic_launcher_background);
                imageView1.setImageDrawable(null);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    class GetJSON extends AsyncTask<String, Void, String> {
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
                loadIntoSpinner(s);
                //Toast.makeText(getApplicationContext(),"LR No. Loaded in the List.", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //in this method we are fetching the json string
        @Override
        protected String doInBackground(String... params) {
            String drsno = params[0];
            //String weburl = "http://subcranial-minuses.000webhostapp.com/vtc3pl/getlrno.php";
            String weburl = "https://vtc3pl.com/getlrno.php";
            try {
                URL url = new URL(weburl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("drsno", "UTF-8") + "=" + URLEncoder.encode(drsno, "UTF-8"); //+ "&"
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