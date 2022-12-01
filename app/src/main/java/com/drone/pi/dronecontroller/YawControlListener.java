package com.drone.pi.dronecontroller;

import android.util.Log;

import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

/**
 * Created by hasan on 17. 7. 29.
 */

public class YawControlListener implements JoystickListener {

    private static final String TAG = JoystickListener.class.getSimpleName();

    private Joystick joystick = null;

    private float positionX;
    private float positionY;
    private PIdronePublisher piDronePublisher = null;

    YawControlListener() {


    }

    YawControlListener(Joystick _joystick) {
        joystick = _joystick;

    }

    void SetPubisher(PIdronePublisher _piDronePublisher) {
        piDronePublisher = _piDronePublisher;
    }

    @Override
    public void onDown() {

        if (joystick != null)
            joystick.lock();
        positionX = 0;
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
        Log.e(TAG, "Should not be here");

    }

    @Override
    public void onUp() {
                /*
                angleView.setText(angleNoneString);
                offsetView.setText(offsetNoneString);

                bugView.setVelocity(0, 0);
                */

        Log.e(TAG, "onUp() x " + 0 + " y " + positionY );

        positionX = 0;
        if (piDronePublisher != null) {     // converting position into actual value

            final float lowerBound = (float) ((-1.0)*YawController.V_LIMIT);
            final float throttle = (positionY-lowerBound)/((float) (2.0*YawController.V_LIMIT));
            piDronePublisher.SetRightMagnitude(positionX/YawController.H_LIMIT, throttle);
        }


    }

    @Override
    public void onDragXY(float x, float y) {


        positionX = x;
        positionY = y;

        if (piDronePublisher != null) {
            final float lowerBound = (float) ((-1.0)*YawController.V_LIMIT);
            final float throttle = (y-lowerBound)/((float) (2.0*YawController.V_LIMIT));
            piDronePublisher.SetRightMagnitude(x/YawController.H_LIMIT, throttle);

        }



        //Log.e(TAG, "(x, Y) : (" + x + ", " + y + ")");

    }
}
