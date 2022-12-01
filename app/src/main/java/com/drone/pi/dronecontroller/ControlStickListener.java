package com.drone.pi.dronecontroller;

import android.util.Log;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

/**
 * Created by hasan on 17. 8. 12.
 */

public class ControlStickListener implements JoystickListener {

    private static final String TAG = JoystickListener.class.getSimpleName();

    private final Joystick joystick;
    private PIdronePublisher piDronePublisher = null;
    ControlStickListener(Joystick _joystick) {
        joystick = _joystick;

    }

    void SetPubisher(PIdronePublisher _piDronePublisher) {
        piDronePublisher = _piDronePublisher;
    }

    @Override
    public void onDown() {


        Log.e(TAG, "onDown()");
    }

    @Override
    public void onDrag(float degrees, float offset) {
                /*
                angleView.setText(String.format(angleValueString, degrees));
                offsetView.setText(String.format(offsetValueString, offset));

                bugView.setVelocity(
                        (float) Math.cos(degrees * Math.PI / 180f) * offset * MAX_BUG_SPEED_DP_PER_S,
                        -(float) Math.sin(degrees * Math.PI / 180f) * offset * MAX_BUG_SPEED_DP_PER_S);
                        */
        float x = (float) Math.cos(degrees * Math.PI / 180f);
        float y = (float) Math.sin(degrees * Math.PI / 180f);

        if (piDronePublisher != null)
            piDronePublisher.SetLeftMagnitude(x, y);

        Log.e(TAG, "Aangle (deg): " + degrees + "Offset: " + offset);
    }

    @Override
    public void onUp() {
                /*
                angleView.setText(angleNoneString);
                offsetView.setText(offsetNoneString);

                bugView.setVelocity(0, 0);
                */
        if (piDronePublisher != null)
            piDronePublisher.SetLeftMagnitude(0, 0);

        Log.e(TAG, "onUp()");

    }

    @Override
    public void onDragXY(float x, float y) {

    }
}


