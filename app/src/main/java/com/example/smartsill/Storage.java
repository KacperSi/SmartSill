package com.example.smartsill;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Storage {
    private SharedPreferences sp;
    private Context context;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public Storage(Context context, String sp_name) {
        this.context = context;
        this.sp = context.getSharedPreferences(sp_name, Context.MODE_PRIVATE);
        this.editor = sp.edit();
        this.gson = new Gson();
    }

    public SharedPreferences getSp() {
        return sp;
    }

    public Context getContext() {
        return context;
    }

    public SharedPreferences.Editor getEditor() {
        return editor;
    }

    public void putList(List list, String list_name){
        String json_string = this.gson.toJson(list);
        this.editor.putString(list_name, json_string);
        //this.editor.apply();
        this.editor.commit();
    }
    public List getDevicesList(String list_name){
        List<Device> devices = new ArrayList<>();
        String json_string = sp.getString(list_name, null);
        Type type = new TypeToken<ArrayList<Device>>(){}.getType();
        List list_from_file = gson.fromJson(json_string, type);
        if (list_from_file != null) {
            devices = gson.fromJson(json_string, type);
        }
        return devices;
    }
}
