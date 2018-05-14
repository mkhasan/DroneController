package com.drone.pi.dronecontroller;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.jmedeisis.bugstick.JoystickListener;

/**
 * Created by hasan on 17. 8. 19.
 */

public class YawController extends FrameLayout {
    private static final String TAG = YawController.class.getSimpleName();

    private static final int STICK_SETTLE_DURATION_MS = 50;
    private static final Interpolator STICK_SETTLE_INTERPOLATOR = new DecelerateInterpolator();

    public static final float H_LIMIT = (float) 0.6;
    public static final float V_LIMIT = (float) 0.8;
    private static final float EPSILON = (float) 0.001;
    private static final float TOLERANCE = (float) 0.3;


    private int touchSlop;

    private float centerX, centerY;
    private float radius;

    private View draggedChild;
    private View draggedChild1;
    private boolean detectingDrag;
    private boolean dragInProgress;

    private float downX, downY;
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;

    private boolean locked;

    private boolean startOnFirstTouch = true;
    private boolean forceSquare = true;
    private boolean hasFixedRadius = false;

    private float stickPositionX;
    private float stickPositionY;

    private OnStickListener onStickListener = null;
    private GestureDetector gestureDetector = null;

    private JoystickListener listener;
    private Context context = null;




    public YawController(Context _context) {
        super(_context);
        context = _context;


    }

    public YawController(Context _context, AttributeSet attrs) {
        super(_context, attrs);
        context = context;



    }

    public YawController(Context _context, AttributeSet attrs, int defStyleAttr) {
        super(_context, attrs, defStyleAttr);
        context = _context;


    }

    public void setListener(JoystickListener _listener, Activity activity) {

        /* listener gets current value and send to the server periodically */

        listener = _listener;
        onStickListener = new OnStickListener(this);
        gestureDetector = new GestureDetector(context, onStickListener);

        final View stick = getChildAt(0);
        final View stickBar = getChildAt(1);

        if (stick != null && stickBar != null) {
            draggedChild = stick;
            draggedChild1 = stickBar;
            this.setOnTouchListener(new View.OnTouchListener() {


                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        Log.e(TAG, "FrameLaoyout onUp");
                        draggedChild.animate()
                                .translationX(0).translationY(stickPositionY)
                                .setDuration(STICK_SETTLE_DURATION_MS)
                                .setInterpolator(STICK_SETTLE_INTERPOLATOR)
                                .start();

                        draggedChild1.animate()
                                .translationX(0)
                                .setDuration(STICK_SETTLE_DURATION_MS)
                                .setInterpolator(STICK_SETTLE_INTERPOLATOR)
                                .start();

                        stickPositionX = 0;
                        if (listener != null)
                            listener.onUp();

                    } else if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        Log.e(TAG, "outsideMove()");
                        return false;
                    }
                    else {
                        gestureDetector.onTouchEvent(motionEvent);
                        //Log.e(TAG, "onToouch() " + motionEvent.getAction());
                    }
                    //Log.e(TAG, "yaw_controller onTouch " + motionEvent.getX());

                    return true;

                }
            });




        }

        if (stick != null) {



            /*
            stick.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    Log.e(TAG, "stick got it " + motionEvent.getAction());
                    return true;
                }
            });
            */


        }
        stickPositionX = stickPositionY = 0;

    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        return true;
    }





    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        Log.e(TAG, "changed: " + changed + "   Fixed Rad: " + hasFixedRadius);
        if (changed && !hasFixedRadius) {
            recalculateRadius(right - left, bottom - top);

            View stick = getChildAt(0);
            View stickBar = getChildAt(1);
            if(stick != null) {
                stick.setTranslationX((float) 0.0);
                if(stickBar != null)
                    stickBar.setTranslationX((float) 0.0);
                stick.setTranslationY(stickPositionY=radius*V_LIMIT); /* set the stick in the lowest mid point */
                Log.e(TAG, "stick placed = yes");
            }
            else
                Log.e(TAG, "stick placed = no");

            Log.e(TAG, "radius is " + radius);
        }
    }

    private void recalculateRadius(int width, int height) {
        float stickHalfWidth = 0;
        float stickHalfHeight = 0;
        if (hasStick()) {
            final View stick = getChildAt(0);
            stickHalfWidth = (float) stick.getWidth() / 2;
            stickHalfHeight = (float) stick.getHeight() / 2;
        }

        Log.e(TAG, "Stick width: " + stickHalfWidth + " Stick height " + stickHalfHeight);

        radius = (float) Math.min(width, height) / 2 - Math.max(stickHalfWidth, stickHalfHeight);

    }

    private boolean hasStick() {
        return getChildCount() > 0;
    }

    /*
 FORCE SQUARE
  */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!forceSquare) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int size;
        if (widthMode == MeasureSpec.EXACTLY && widthSize > 0) {
            size = widthSize;
        } else if (heightMode == MeasureSpec.EXACTLY && heightSize > 0) {
            size = heightSize;
        } else {
            size = widthSize < heightSize ? widthSize : heightSize;
        }

        int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(finalMeasureSpec, finalMeasureSpec);
    }
    /*
    CENTER CHILD BY DEFAULT
     */

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        LayoutParams params = new LayoutParams(getContext(), attrs);
        params.gravity = Gravity.CENTER;
        return params;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(@NonNull ViewGroup.LayoutParams p) {
        LayoutParams params = new LayoutParams(p);
        params.gravity = Gravity.CENTER;
        return params;
    }

    @Override
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }



    /*
    ENSURE MAX ONE DIRECT CHILD
     */
    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 1) {
            throw new IllegalStateException(TAG + " can host only two direct child");
        }

        super.addView(child, index, params);
    }




    private void onDragStart() {
        dragInProgress = true;
        draggedChild = getChildAt(0);
        draggedChild.animate().cancel();
        stickPositionX = 0;
        onDrag(OnSwipeListener.Direction.left, 0, 0);
    }

    private void onTouchEnded() {
        activePointerId = INVALID_POINTER_ID;
    }




    public void onDrag(OnStickListener.Direction direction, float distanceX, float distanceY) {


        final float scale = (float) 1.0;

        if (direction == OnStickListener.Direction.up || direction == OnStickListener.Direction.down) {
            Log.e(TAG, "Got vertical cmd at " + stickPositionX);
            //if(Math.abs(stickPositionX) < radius*TOLERANCE ) {
                Log.e(TAG, "Execute vertical cmd");
                stickPositionY -= distanceY * scale;
            //}
        }

        else
            stickPositionX -= distanceX*scale;

        if (stickPositionX > radius*H_LIMIT)
            stickPositionX = radius*H_LIMIT;
        if (stickPositionX < -radius*H_LIMIT)
            stickPositionX = -radius*H_LIMIT;

        if (stickPositionY > radius*V_LIMIT)
            stickPositionY = radius*V_LIMIT;
        if (stickPositionY < -radius*V_LIMIT)
            stickPositionY = -radius*V_LIMIT;

        if (null != listener) listener.onDragXY(stickPositionX/radius, -stickPositionY/radius);

        //if (direction == OnSwipeListener.Direction.left) {
           // stickPositionX = distanceX;
          //  stickPositionY = 0;
            draggedChild.setTranslationX(stickPositionX);
            draggedChild.setTranslationY(stickPositionY);

            if(draggedChild1 != null)
            draggedChild1.setTranslationX(stickPositionX);

        //}

        //draggedChild.setTranslationX(stickPositionX);
        //draggedChild.setTranslationY(stickPositionX);



        Log.e(TAG, "onDrag dir " + direction + " stickPositionX " + stickPositionX
                + " stickPositionY " + stickPositionY + " radius " + radius);


    }

    void onDown() {

        Log.e(TAG, "onDown()");

        stickPositionX = 0;
        draggedChild.animate().cancel();
        draggedChild1.animate().cancel();

    }

}



