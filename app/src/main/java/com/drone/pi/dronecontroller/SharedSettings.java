package com.drone.pi.dronecontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by usrc on 17. 7. 19.
 */

public class SharedSettings {
    public float speed = (float) 0.0;
    public float verticalSpeed = (float) 0.0;
    public float altitude = (float) 0.0;
    public float gpsLevel = (float) 0.0;
    public float lteLevel = (float) 0.0;
    public int batteryLevel = 0;


    private Context m_Context = null;
    private SharedPreferences settings = null;
    private  SharedPreferences.Editor editor = null;

    private static volatile SharedSettings _inst = null;
    private SharedSettings()
    {
        m_Context = null;
    }

    private SharedSettings(final Context mContext)
    {
        m_Context = mContext;
    }

    public static synchronized SharedSettings getInstance(final Context mContext)
    {
        if (_inst == null)
        {
            _inst = new SharedSettings(mContext);
            _inst.loadPrefSettings();
            _inst.savePrefSettings();
            Log.e("ShSettings", "SharedSettings: getInstance.");
        }

        return _inst;
    }

    public static synchronized SharedSettings getInstance()
    {
        return _inst;
    }

    public void loadPrefSettings() {
        if (settings == null)
            settings = PreferenceManager.getDefaultSharedPreferences(m_Context);

        speed = settings.getFloat("speed", (float) 0.0);
        verticalSpeed = settings.getFloat("verticalSpeed", (float) 0.0);
        altitude = settings.getFloat("altitude", (float) 0.0);
        gpsLevel = settings.getFloat("gpsLevel", (float) 0.0);
        lteLevel = settings.getFloat("lteLevel", (float) 0.0);
        batteryLevel = settings.getInt("batteryLevel", 0);
    }

    public void savePrefSettings() {
        if (settings == null)
            settings = PreferenceManager.getDefaultSharedPreferences(m_Context);

        if(editor == null)
            editor = settings.edit();

        editor.putFloat("speed", speed);
        editor.putFloat("verticalSpeed", verticalSpeed);
        editor.putFloat("altitude", altitude);
        editor.putFloat("gpsLevel", gpsLevel);
        editor.putFloat("lteLevel", lteLevel);
        editor.putInt("batteryLevel", batteryLevel);

        editor.commit();

        Log.e("ShSettings", "SharedSettings: saveSettings.");

    }



}
