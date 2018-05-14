package com.drone.pi.dronecontroller;

import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by hasan on 17. 8. 12.
 */

public class OnGimbalListener extends OnSwipeListener {
    private static final String TAG = "GimbalListener";
    PIdronePublisher piDronePublisher = null;

    private static final float SCALE_X = (float) 0.1;
    private static final float SCALE_Y = (float) 0.1;

    private static final float ABS_MAX_X = (float) 100.0;
    private static final float ABS_MAX_Y = (float) 100.0;

    float magnitudeX=(float) 0.0;
    float magnitudeY=(float) 0.0;

    public void SetPubisher(PIdronePublisher _piDronePublisher) {
        piDronePublisher = _piDronePublisher;
    }

    @Override
    public boolean onSwipe(Direction direction, float incX, float incY) {


        return false;
    }

    @Override
    public boolean onScroll(Direction direction, float distanceX, float distanceY) {

        if (direction==Direction.up){
            //do your stuff
            Log.e(TAG, "onScroll: up " + "DistanceX: " + distanceX + " DistanceY: " + distanceY);
        }

        if (direction==Direction.down){
            //do your stuff
            Log.e(TAG, "onScroll: down " + "DistanceX: " + distanceX + " DistanceY: " + distanceY);
        }

        if (direction==Direction.left){
            //do your stuff
            Log.e(TAG, "onScroll: left " + "DistanceX: " + distanceX + " DistanceY: " + distanceY);
        }

        if (direction==Direction.right){
            //do your stuff
            Log.e(TAG, "onScroll: right " + "DistanceX: " + distanceX + " DistanceY: " + distanceY);
        }

        if (direction == Direction.right || direction == Direction.left)
            magnitudeX = magnitudeX - distanceX*SCALE_X;
        else
            magnitudeY = magnitudeY + distanceY*SCALE_Y;

        if (magnitudeX > ABS_MAX_X) magnitudeX = ABS_MAX_X;
        if (magnitudeX < -ABS_MAX_X) magnitudeX = -ABS_MAX_X;
        if (magnitudeY > ABS_MAX_Y) magnitudeY = ABS_MAX_Y;
        if (magnitudeY < -ABS_MAX_Y) magnitudeY = -ABS_MAX_Y;

        if (piDronePublisher != null)
            piDronePublisher.SetSecondRightMagnitude(magnitudeX, magnitudeY);



        return true;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }


}
