package com.drone.pi.dronecontroller;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.sql.BatchUpdateException;

/**
 * Created by hasan on 17. 8. 15.
 */

public class BatInfoReceiver extends BroadcastReceiver {

    private static final String TAG = BatInfoReceiver.class.getSimpleName();
    private BatteryBlinkListener batteryBlinkListener = null;

    private int previousLevel = -1;
    private BatteryState prevState = BatteryState.UNKNOWN;

    @Override
    public void onReceive(Context arg0, Intent intent) {
        // TODO Auto-generated method stub
        int level = intent.getIntExtra("level", 0);
        //contentTxt.setText(String.valueOf(level) + "%");
        Log.e(TAG, "Battery level " + level + "%");

        /*
        if (batteryBlinkListener != null) {
            batteryBlinkListener.onBlinkingStart(3);
        }
        */
        BatteryState state = BatteryState.get(level);

        if (level != previousLevel && batteryBlinkListener != null) {
            batteryBlinkListener.onBatteryLevelReceived(level);
            if (state != prevState) {
                batteryBlinkListener.onChangeBatteryState(state);
                prevState = state;
            }

            previousLevel = level;
        }

        if ((state == BatteryState.MID || state == BatteryState.LOW) && level%5 == 0 && batteryBlinkListener != null) {
            batteryBlinkListener.onBlinkingStart(3);
        }


    }

    public enum BatteryState {
        LOW,
        MID,
        HIGH,
        UNKNOWN;

        private static BatteryState get(int level) {
            if(level <= 100 && level > 30)
                return BatteryState.HIGH;
            else if (level <= 30 && level > 10)
                return BatteryState.MID;
            else if(level <= 10 && level >= 0)
                return BatteryState.LOW;
            else
                return BatteryState.UNKNOWN;
        }
    }



    public void SetListener(BatteryBlinkListener _batteryBlinkListener) {
        batteryBlinkListener = _batteryBlinkListener;
    }

}
