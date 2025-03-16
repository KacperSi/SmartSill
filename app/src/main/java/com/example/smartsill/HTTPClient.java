package com.example.smartsill;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

//klasa nieużywana, aby stała się użyteczn potrzeb callbacków


public class HTTPClient {
    private RequestQueue queue;
    private Context context;
    private String ip;
    private String port;

    public HTTPClient(Context context, String ip, String port) {
        this.context = context;
        this.ip = ip;
        this.port = port;
        this.queue = Volley.newRequestQueue(this.context);
    }
    public void get_request(String endpoint_url){
        String url = "http://" + this.ip + ":" + this.port + "/" + endpoint_url;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        get_response_handling(endpoint_url, response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorHandling(error);
                    }
                });
        queue.add(jsonObjectRequest);
    }

    private void get_response_handling(String endpoint, JSONObject response) { //nieelastyczne rozwiązanie, tymczasowe(można użyć callback)
        switch(endpoint){
            case "set_watering":
                Toast.makeText(context.getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(context.getApplicationContext(), "Unknown endpoint", Toast.LENGTH_LONG).show();
                break;
        }

    }

    private void errorHandling(VolleyError error) { //TO DO
        Toast.makeText(this.context.getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
    }
}
