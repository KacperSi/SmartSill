package com.example.smartsill;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements SelectListener{


    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText new_device_name;
    private EditText new_device_uuid;
    private EditText new_wifi_ssid;
    private EditText new_wifi_pass;
    private EditText new_device_pass;
    private Button new_device_save_button, new_device_cancel_button;
    private SharedPreferences devices_settings;
    private Storage main_storage;
    BluetoothAdapter bluetoothAdapter;

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<Device> devices;
    DevicesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initRecyclerView();

    }

    private void initData() {
        main_storage = new Storage(this,"devices_settings");
        devices_settings = main_storage.getSp();
        devices = main_storage.getDevicesList("devices_list");
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.devicesRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DevicesAdapter(devices, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void addDevice(View v) {
        createNewDevice();

        //Toast.makeText(getApplicationContext(), "kliknięte", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemClicked(Device device) {
        Intent device_config_intent = new Intent(this, DeviceConfig.class);
        device_config_intent.putExtra("name", device.getName());
        device_config_intent.putExtra("ip", device.getIp());
        device_config_intent.putExtra("uuid", device.getUUID());
        device_config_intent.putExtra("password", device.getPassword());
        startActivity(device_config_intent);
    }

    public void createNewDevice(){
        dialogBuilder = new AlertDialog.Builder(this);
        final View device_popup_View = getLayoutInflater().inflate(R.layout.device_popup_full, null);
        new_device_name = device_popup_View.findViewById(R.id.humidity_min_ET);
        new_device_uuid = device_popup_View.findViewById(R.id.new_device_uuid);
        new_wifi_ssid = device_popup_View.findViewById(R.id.new_net_ssid);
        new_wifi_pass = device_popup_View.findViewById(R.id.new_net_pass);
        new_device_pass = device_popup_View.findViewById(R.id.new_device_pass);
        new_device_save_button = device_popup_View.findViewById(R.id.new_device_save_button);
        new_device_cancel_button = device_popup_View.findViewById(R.id.new_device_cancel_button);
        dialogBuilder.setView(device_popup_View);
        dialog = dialogBuilder.create();
        dialog.show();

        new_device_save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView addDevText = findViewById(R.id.addDevText);
                addDevText.setVisibility(View.GONE);
                String nameStr = new_device_name.getText().toString();
                String device_uuid = new_device_uuid.getText().toString();
                String wifi_ssid = new_wifi_ssid.getText().toString();
                String wifi_pass = new_wifi_pass.getText().toString();
                String ipStr = "singlepot1";
                String password = new_device_pass.getText().toString();
                //SharedPreferences.Editor editor = main_storage.getEditor();
                //editor.putString("name", nameStr);
                //editor.putString("ip", ipStr);
                //editor.commit();


                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("cred1", wifi_ssid);
                    jsonBody.put("cred2", wifi_pass);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Wykonanie żądania HTTP
                String url = "http://192.168.4.1:80/set_wifi";
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Obsługa odpowiedzi serwera po zapisaniu urządzenia
                                // Tutaj możesz obsłużyć odpowiedź serwera, np. wyświetlić komunikat Toast
                                devices.add(new Device(nameStr, ipStr, device_uuid, password));
                                main_storage.putList(devices, "devices_list");
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), "Device saved", Toast.LENGTH_LONG).show();
                                //dialog.dismiss();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Obsługa błędu żądania HTTP
                                Toast.makeText(getApplicationContext(), "Error saving device", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        // Dodaj nagłówek dla podstawowej autentykacji
                        String credentials = "singlepotuser:" + password;
                        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                        headers.put("Authorization", auth);
                        return headers;
                    }
                };

                // Dodanie żądania do kolejki Volley
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                boolean BT_ok = checkBluetoothConnection();
                if(BT_ok) {
                    requestQueue.add(jsonObjectRequest);
                }
                else{
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(getApplicationContext(), "Bluetooth authentication error!!!", Toast.LENGTH_LONG).show();
                }

                dialog.dismiss();
            }
        });

        new_device_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

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
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            // Toast.makeText(getApplicationContext(), "Bluetooth is disabled!", Toast.LENGTH_LONG).show();
            // Toast.makeText(getApplicationContext(), "Bluetooth authentication error!!!", Toast.LENGTH_LONG).show();
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
}