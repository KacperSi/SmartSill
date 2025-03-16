package com.example.smartsill;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;

public class ChangePass extends AppCompatActivity {

    private SharedPreferences devices_settings;
    private Storage main_storage;
    List<Device> devices;
    TextView current_passsword;
    TextView new_password_1;
    TextView new_password_2;
    String device_ip;
    TextView ip_textView;
    Device device;
    Button save_button;
    String port;
    BluetoothAdapter bluetoothAdapter;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        Intent this_intent = getIntent();
        this.device_ip = this_intent.getStringExtra("ip");

        current_passsword = findViewById(R.id.current_pass_ET);
        new_password_1 = findViewById(R.id.new_pass_1_ET);
        new_password_2 = findViewById(R.id.new_pass_2_ET);
        ip_textView = findViewById(R.id.ip_textView);
        ip_textView.setText(this.device_ip);
        save_button = findViewById(R.id.save_button);
        this.queue = Volley.newRequestQueue(this);
        this.port = "80";
        initData();

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String p1 = new_password_1.getText().toString();
                    String p2 = new_password_2.getText().toString();
                    if(p1.equals(p2)) {
                        boolean BT_ok = checkBluetoothConnection();
                        if(BT_ok) {
                            send_request("change_pass", Request.Method.POST);
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Bluetooth authentication error!", Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Given passwords are not the same", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void send_request(String endpoint_url, int request_type) throws JSONException {
        String url = "http://" + this.device_ip + ":" + this.port + "/" + endpoint_url;
        JSONObject jsonBody = new JSONObject();
        String pass = this.current_passsword.getText().toString();
        if(request_type == Request.Method.POST)
        {
            jsonBody = get_body_for_request(endpoint_url);
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (request_type, url, jsonBody, new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            response_handling(endpoint_url, request_type, response);
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
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
    private void response_handling(String endpoint, int request_type, JSONObject response) throws JSONException { //nieelastyczne rozwiązanie, tymczasowe(można użyć callback)
            switch(endpoint){
                case "change_pass":
                    if(request_type == Request.Method.POST)
                    {
                        this.device.setPassword(new_password_1.getText().toString());
                        set_devices();
                        Toast.makeText(getApplicationContext(), "New password saved!", Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "Unknown endpoint", Toast.LENGTH_LONG).show();
                    break;
            }
    }
    private void errorHandling(VolleyError error) { //TO DO
        //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
        if (error.networkResponse != null){
            if (error.networkResponse.statusCode == 400) {
                Toast.makeText(getApplicationContext(), "Wrong data. Check is input valid!", Toast.LENGTH_LONG).show();
            }
        }
        else {
            // Toast.makeText(DeviceConfig.this, "Network error!", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "Network error!", Toast.LENGTH_LONG).show();
        }
    }
    private JSONObject get_body_for_request(String endpoint) throws JSONException {
        switch(endpoint){
            case "change_pass":
                JSONObject json_body = new JSONObject();
                try {
                    json_body.put("cred1", "singlepotuser");
                    json_body.put("cred2", this.new_password_1.getText().toString());
                }
                catch (Exception e){
                    return json_body;
                }
                return json_body;
            default:
                return null;
        }
    }
    private boolean checkBluetoothConnection()
    {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            return false;
        }

        // Sprawdź, czy Bluetooth jest włączony
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled() || !this.device.getUUID().equals("singlePot862301") ) {
            // Toast.makeText(getApplicationContext(), "Bluetooth is disabled!", Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "Bluetooth authentication error!!!", Toast.LENGTH_LONG).show();
            return false;
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
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
            if(dev_found) {
                // Bluetooth is enabled, wykonaj dalsze czynności
                return true;
            }
            else{
                return false;
            }
        }
    }
    private void initData() {
        main_storage = new Storage(this,"devices_settings");
        devices_settings = main_storage.getSp();
        this.devices = main_storage.getDevicesList("devices_list");
        for (Device dev : this.devices) {
            if (dev.getIp().equals(this.device_ip)) {
                this.device = dev;
            }
        }
    }

    private void set_devices() {
        List<Device> new_devices = new ArrayList<>();
        for (Device dev : this.devices) {
            if (dev.getIp().equals(this.device_ip)) {
                dev.setPassword(this.device.getPassword());
                new_devices.add(dev);
            }
        }
        main_storage.putList(new_devices, "devices_list");
    }
}