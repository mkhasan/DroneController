package com.drone.pi.dronecontroller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by usrc on 17. 8. 16.
 */

public class Circle extends View {

    public enum Status {
        CANCEL,
        START
    }

    private Status status = Status.CANCEL;
    private static final String TAG = "Circle";

    private final Paint paint;
    private RectF rect;

    private float angle, angle1;

    private CircleAngleAnimation animation = null;

    private Activity activity = null;

    private ImageView cancleStatus = null;

    public boolean animateImmediately = true;

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }



    public Circle(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();

        final int strokeWidth = 10;

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        //Circle color
        paint.setColor(Color.RED);

        //size 200x200 example
        rect = new RectF(strokeWidth/2, strokeWidth/2, 200-strokeWidth/2, 200-strokeWidth/2);

        //Initial Angle (optional, it can be zero)
        angle = 0;
        //circle.startAnimation(animation);




    }

    public void Set(int diam, int boundary, int color, Activity _activity) {

        final int strokeWidth = boundary;

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        //Circle color
        paint.setColor(color);



        //size 200x200 example
        rect = new RectF(strokeWidth/2, strokeWidth/2, diam-strokeWidth/2, diam-strokeWidth/2);

        //Initial Angle (optional, it can be zero)
        angle = 0;
        activity = _activity;

        if (activity != null) {
            animation = new CircleAngleAnimation(this, 360);

            animation.setDuration(5000);
            animation.setRepeatCount(-1);
            animation.setInterpolator(activity, android.R.anim.linear_interpolator);
        }


    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        canvas.drawArc(rect, angle, 60, false, paint);



    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (animation != null) {
            if (visibility == View.VISIBLE) {
                if(animateImmediately == true)
                    updateStatus(Status.CANCEL);
                else
                    updateStatus(Status.START);
                cancleStatus.setVisibility(visibility);
                if(animateImmediately)
                    this.startAnimation(animation);
            }
            else if (visibility == View.GONE){
                cancleStatus.setVisibility(visibility);
                this.clearAnimation();
                animateImmediately = true;
            }
        }
        Log.e(TAG, "Visibility set");
    }


    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void setStatusView(ImageView _cancelStatus) {
        cancleStatus = _cancelStatus;
    }

    public void animateNow() {
        if (this.getVisibility() == View.GONE)
            return;

        updateStatus(Status.CANCEL);
        this.startAnimation(animation);
    }

    private void updateStatus(Status _status) {
        status = _status;
        final float ratio = (float) 0.8;
        final int gap = 10;
        if(status == Status.CANCEL) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) cancleStatus.getLayoutParams();
            lp.width = (int)((float) GlobalData.aniCancelDiameter*ratio);
            lp.height =  (int)((float) GlobalData.aniCancelDiameter*ratio*(36.0/130.0));     // cancel -> 130 x 36
            lp.topMargin = GlobalData.aniCancelTopMargin + GlobalData.aniCancelDiameter+gap;
            cancleStatus.setLayoutParams(lp);

            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.text_cancel);
            cancleStatus.setImageBitmap(image);
            Log.e(TAG, "width " + lp.width + " height " + lp.height);
        }
        else if(status == Status.START) {

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) cancleStatus.getLayoutParams();
            lp.width = (int)((float) GlobalData.aniCancelDiameter*ratio*(90.0/130.0));
            lp.height =  (int)((float) GlobalData.aniCancelDiameter*ratio*(37.0/130.0));     // start -> 90 x 37
            lp.topMargin = GlobalData.aniCancelTopMargin + GlobalData.aniCancelDiameter+gap;
            cancleStatus.setLayoutParams(lp);

            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.text_start);
            cancleStatus.setImageBitmap(image);


            Log.e(TAG, "width " + lp.width + " height " + lp.height);


        }
    }

    public Status getStatus() {
        return status;
    }



}
