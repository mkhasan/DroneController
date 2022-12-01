package com.drone.pi.dronecontroller;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by hasan on 17. 8. 8.
 */

public class GlobalData {
    public static int screenWidth = 0;
    public static int screenHeight = 0;
    public static int topBarHeight = 0;

    public static int armDialerWidth = 0;
    public static int armDialerHeight = 0;

    public static int gimbalControllerWidth = 0;
    public static int gimbalControllerHeight = 0;

    public static int landingControllerWidth = 0;
    public static int landingControllerHeight = 0;


    public static Bitmap imageDialerOriginal, imageDialerScaled;
    public static Bitmap imageGimbalOriginal, imageGimbalScaled;
    public static Bitmap imageLandingOriginal, imageLandingScaled;

    public static Matrix armDialerMatrix;
    public static Matrix landingControllerMatrix;

    public static int searchListItemHeight = 0;
    public static int searchListItemWidth = 0;

    public static int aniCancelDiameter = 0;
    public static int aniCancelTopMargin = 0;

    public static int deliveryIconWidth = 0;

    public static int menuDisp = 0;




}
