package com.drone.pi.dronecontroller;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by usrc on 17. 8. 16.
 */

public class CircleAngleAnimation extends Animation {

    private static final String TAG = "CircleAni";
    private Circle circle;

    private float oldAngle;
    private float newAngle;


    public CircleAngleAnimation(Circle circle, int newAngle) {
        this.oldAngle = circle.getAngle();
        this.newAngle = newAngle;
        this.circle = circle;

    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        float angle = oldAngle + ((newAngle - oldAngle) * interpolatedTime);

        circle.setAngle(angle);
        circle.requestLayout();
    }
}