package com.example.smartsill;

import static java.util.concurrent.TimeUnit.SECONDS;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.Build;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;


public class DeviceConfig extends AppCompatActivity {
    private static final int CHANGE_PASSWORD_REQUEST_CODE = 1;
    private SharedPreferences devices_settings;
    private Storage main_storage;
    List<Device> devices;
    TextView ip_textView;
    String device_name;
    String device_ip;
    String device_password;
    String device_uuid;
    ToggleButton toggle_watering_btn;
    TextView humidity;
    TextView warning;
    TextView network_error;
    TextView bluetooth_error;
    Boolean network_error_flag;
    Boolean bluetooth_previous_state;
    TextView humidity_min;
    TextView humidity_max;
    TextView watering_time;
    TextView watering_max_time;
    Button save_button;
    //HTTPClient http_client;
    String port;
    BluetoothAdapter bluetoothAdapter;
    boolean bluetooth_connection = false;
    private RequestQueue queue;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mInterval = 1000;

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.layout.change_pass, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.change_pass) {
            Intent change_pass_intent = new Intent(this, ChangePass.class);
            change_pass_intent.putExtra("ip", device_ip);
            //startActivity(change_pass_intent);
            //startActivityForResult(change_pass_intent, CHANGE_PASSWORD_REQUEST_CODE);
            startForResult.launch(change_pass_intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final ActivityResultLauncher<Intent> startForResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            get_new_password();
                            // Handle the result if needed
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_config);
        ip_textView = findViewById(R.id.ip_textView);

        Intent this_intent = getIntent();
        this.device_name = this_intent.getStringExtra("name");
        this.device_ip = this_intent.getStringExtra("ip");
        this.device_uuid = this_intent.getStringExtra("uuid");
        this.device_password = this_intent.getStringExtra("password");
        this.port = "80";
        this.queue = Volley.newRequestQueue(this);
        this.network_error_flag = false;

        //http_client = new HTTPClient(this, this.device_ip, this.port);

        ip_textView.setText(this.device_ip);
        getSupportActionBar().setTitle(device_name);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_change_pass);
        toggle_watering_btn = findViewById(R.id.toggle_watering_btn);
        humidity = findViewById(R.id.textView_humidity);
        this.warning = findViewById(R.id.warning_textView);
        this.network_error = findViewById(R.id.network_error_textView);
        this.bluetooth_error = findViewById(R.id.bluetooth_error_textView);
        this.bluetooth_previous_state = false;
        humidity_min = findViewById(R.id.humidity_min_ET);
        humidity_max = findViewById(R.id.humidity_max_ET);
        watering_time = findViewById(R.id.watering_time_ET);
        watering_max_time = findViewById(R.id.watering_max_time_ET);
        save_button = findViewById(R.id.save_button);


        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            this.bluetooth_connection = false;
        }

        // Sprawdź, czy Bluetooth jest włączony
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is disabled!", Toast.LENGTH_LONG).show();
            this.bluetooth_connection = false;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(getApplicationContext(), "Bluetooth permissions for app needed!", Toast.LENGTH_LONG).show();
                this.bluetooth_connection = false;
            }
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // Bluetooth is enabled, wykonaj dalsze czynności
            this.bluetooth_connection = true;
        }

        startRepeatingTask();

        try {
            send_request("set_watering", Request.Method.GET);
            send_request("get_soil_moisture", Request.Method.GET);
            send_request("get_water_level", Request.Method.GET);
            send_request("set_watering_settings", Request.Method.GET);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        toggle_watering_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    send_request("set_watering", Request.Method.POST);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    send_request("set_watering_settings", Request.Method.POST);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                send_request("get_soil_moisture", Request.Method.GET);
                send_request("get_water_level", Request.Method.GET);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    public void send_request(String endpoint_url, int request_type) throws JSONException {
        String url = "http://" + this.device_ip + ":" + this.port + "/" + endpoint_url;
        JSONObject jsonBody = new JSONObject();
        String pass = this.device_password;
        if (request_type == Request.Method.POST) {
            jsonBody = get_body_for_request(endpoint_url);
            //Toast.makeText(getApplicationContext(), "tu", Toast.LENGTH_LONG).show();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (request_type, url, jsonBody, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            checkBluetoothConnection();
                            response_handling(endpoint_url, request_type, response);
                            network_error.setVisibility(View.GONE);
                            checkAreSettingsDownloaded();
                        } catch (JSONException e) {
                            e.printStackTrace(); //TO DO
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorHandling(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Dodaj nagłówek dla podstawowej autentykacji
                String credentials = "singlepotuser:" + pass;
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                return headers;
            }
        };

        int MY_SOCKET_TIMEOUT_MS = 10000; // 10 seconds
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);
    }

    private JSONObject get_body_for_request(String endpoint) throws JSONException {
        switch (endpoint) {
            case "set_watering":
                if (this.toggle_watering_btn.isChecked() == true) {
                    // return new JSONObject("{\"watering\":\"ON\"}");
                    return new JSONObject().put("watering", "ON");
                } else {
                    return new JSONObject("{\"watering\":\"OFF\"}");
                }
            case "set_watering_settings":
                JSONObject json_body = new JSONObject();
                try {
                    json_body.put("moisture_max", Integer.parseInt(this.humidity_max.getText().toString()));
                    json_body.put("moisture_min", Integer.parseInt(this.humidity_min.getText().toString()));
                    json_body.put("watering_time", this.watering_time.getText());
                    json_body.put("watering_max_time", Integer.parseInt(this.watering_max_time.getText().toString()));
                } catch (Exception e) {
                    return json_body;
                }
                return json_body;
            default:
                return null;
        }
    }

    private void response_handling(String endpoint, int request_type, JSONObject response) throws JSONException { //nieelastyczne rozwiązanie, tymczasowe(można użyć callback)
        if (this.bluetooth_connection) {
            switch (endpoint) {
                case "set_watering":
                    if (request_type == Request.Method.GET) {
                        Boolean watering_state = ("ON" == response.getString("watering"));
                        //Toast.makeText(getApplicationContext(), watering_state.toString(), Toast.LENGTH_LONG).show();
                        toggle_watering_btn.setChecked(watering_state);
                    }
                    break;
                case "get_soil_moisture":
                    if (request_type == Request.Method.GET) {
                        int soil_moisture = response.getInt("soil_moisture");
                        humidity.setText(Integer.toString(soil_moisture) + "%");
                        //Toast.makeText(getApplicationContext(), Integer.toString(soil_moisture), Toast.LENGTH_LONG).show();
                    }
                    break;
                case "get_water_level":
                    if (request_type == Request.Method.GET) {
                        int water_level_correct = response.getInt("water_level");
                        if (water_level_correct == 1) {
                            warning.setVisibility(View.GONE);
                        } else {
                            warning.setVisibility(View.VISIBLE);
                        }
                        //Toast.makeText(getApplicationContext(), water_level.toString(), Toast.LENGTH_LONG).show();
                    }
                    break;
                case "set_watering_settings":
                    if (request_type == Request.Method.GET) {
                        humidity_min.setText(response.getString("moisture_min"));
                        humidity_max.setText(response.getString("moisture_max"));
                        watering_time.setText(response.getString("watering_time"));
                        watering_max_time.setText(response.getString("watering_max_time"));
                        //Toast.makeText(getApplicationContext(), water_level.toString(), Toast.LENGTH_LONG).show();
                    }
                    if (request_type == Request.Method.POST) {
                        Toast.makeText(getApplicationContext(), "Watering settings saved!", Toast.LENGTH_LONG).show();
                    }//teoretycznie inne metody trzeba obsłużyć
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "Unknown endpoint", Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            if (request_type == Request.Method.POST) {
                Toast.makeText(getApplicationContext(), "Operation cannot be performed!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void errorHandling(VolleyError error) { //TO DO
        //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
        if (error.networkResponse != null) {
            if (error.networkResponse.statusCode == 400) {
                Toast.makeText(getApplicationContext(), "Wrong data. Check is input valid!", Toast.LENGTH_LONG).show();
            }
        } else {
            // Toast.makeText(DeviceConfig.this, "Network error!", Toast.LENGTH_SHORT).show();
            network_error.setText("Network error!!!");
            network_error.setVisibility(View.VISIBLE);
            this.network_error_flag = true;
        }
    }

    private void checkAreSettingsDownloaded() {
        if (this.network_error_flag) {
            try {
                send_request("set_watering", Request.Method.GET);
                send_request("get_soil_moisture", Request.Method.GET);
                send_request("get_water_level", Request.Method.GET);
                send_request("set_watering_settings", Request.Method.GET);
                this.network_error_flag = false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkBluetoothConnection() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            this.bluetooth_connection = false;
        }

        // Sprawdź, czy Bluetooth jest włączony
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled() || !this.device_uuid.equals("singlePot862301")) {
            // Toast.makeText(getApplicationContext(), "Bluetooth is disabled!", Toast.LENGTH_LONG).show();
            bluetooth_error.setText("Bluetooth authentication error!!!");
            bluetooth_error.setVisibility(View.VISIBLE);
            this.bluetooth_connection = false;
            this.bluetooth_previous_state = false;
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // Bluetooth is enabled, wykonaj dalsze czynności

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                bluetooth_error.setText("Bluetooth authentication error!!!");
                bluetooth_error.setVisibility(View.VISIBLE);
                this.bluetooth_connection = false;
                this.bluetooth_previous_state = false;
            }
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            boolean dev_found = false;

            for (BluetoothDevice device : pairedDevices) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    break;
                }
                String bt_device_name = device.getName();

                if (bt_device_name != null && bt_device_name.equals("singlePot862301")) {
                    dev_found = true;
                }
            }
            if(dev_found){
            bluetooth_error.setVisibility(View.GONE);
            this.bluetooth_connection = true;
            if(!this.bluetooth_previous_state){
                try {
                    send_request("set_watering", Request.Method.GET);
                    send_request("get_soil_moisture", Request.Method.GET);
                    send_request("get_water_level", Request.Method.GET);
                    send_request("set_watering_settings", Request.Method.GET);
                    this.bluetooth_previous_state = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }}
            else{
                bluetooth_error.setText("Bluetooth authentication error!!!");
                bluetooth_error.setVisibility(View.VISIBLE);
                this.bluetooth_connection = false;
                this.bluetooth_previous_state = false;
            }
        }
    }
    private void get_new_password(){
        main_storage = new Storage(this,"devices_settings");
        devices_settings = main_storage.getSp();
        this.devices = main_storage.getDevicesList("devices_list");
        for (Device dev : this.devices) {
            if (dev.getIp().equals(this.device_ip)) {
                this.device_password = dev.getPassword();
                this.device_password = dev.getPassword();
            }
        }
    }
}