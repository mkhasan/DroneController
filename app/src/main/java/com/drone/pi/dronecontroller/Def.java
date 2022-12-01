package com.drone.pi.dronecontroller;

/**
 * Created by hasan on 17. 7. 27.
 */

public class Def {

    public static final String MQTT_USERNAME = "pi";
    public static final String MQTT_PASSWD = "vkdlemfhs";
    public static final float joyStictDiaToWidthRatio = (float) 0.264;
    //public static final float JOYSTICK_DIA_TO_WIDTH_RATIO = (float) 0.264;
    public static final float joyStictBtnDiaToWidthRatio = (float) 0.08;
    //public static final float JOYSTICK_DIA_TO_WIDTH_RATIO = (float) 0.08;

    public static final float menuBtnMinHeightToWidthRatio = (float) 0.05;
    public static final float menuBtnMaxHeightToWidthRatio = (float) 0.06;

    public static final float menuBtnLeftMargin0ToWidthRatio = (float) 0.01;
    public static final float menuBtnLeftMargin1ToWidthRatio = (float) 0.08;
    public static final float menuBtnLeftMargin2ToWidthRatio = (float) 0.146;

    public static final float menuBtnRightMargin0ToWidthRatio = (float) 0.01;
    public static final float menuBtnRightMargin1ToWidthRatio = menuBtnLeftMargin1ToWidthRatio;//(float) 0.097;
    public static final float menuBtnRightMargin2ToWidthRatio = menuBtnLeftMargin2ToWidthRatio;//(float) 0.184;


    public static final float joystickLowerMarginToHeightRatio = (float) 0.2;

    public static final float rightMenuMinHeightToWidthRatio = menuBtnMinHeightToWidthRatio;
    public static final float rightMenuMaxHeightToWidthRatio = menuBtnMaxHeightToWidthRatio;
    public static final float rightMenuMinWidthToWidthRatio = (float) 0.067;
    public static final float rightMenuMaxWidthToWidthRatio = (float) 0.08;

    public static final float cameraClickWidthToWidthRatio = rightMenuMaxWidthToWidthRatio;
    public static final float robotControlWidthToWidthRatio = cameraClickWidthToWidthRatio;

    public static final float cameraClickTopMarginToWidthRatio = (float) 0.09;
    public static final float robotControlTopMarginToWidthRatio = cameraClickTopMarginToWidthRatio;


    public static final float yawControllerDiaToWidthRatio = joyStictDiaToWidthRatio;
    public static final float yawBtnDiaToWidthRatio = joyStictBtnDiaToWidthRatio;
    public static final float armDialerRatio = (float) 0.66;
    public static final float gimbalControllerRatio = (float) 0.75;
    public static final float landingControllerRatio = armDialerRatio;

    public static final float setupWidthToWidthRatio = (float) 0.75;
    public static final float setupHeightToHeightRatio = (float) 0.6;
    public static final float setupIconWidthToWidthRatio = (float) 0.12*(float) 0.83;        // 1200 x 800
    public static final float setupIconHeightToWidthRatio = (float) 0.08*(float) 0.83;

    public static final float setupLabelWidthToWidthRatio = (float) 0.2009;
    public static final float setupButtonSpaceWidthToWidthRatio = (float) 0.15;//0.1164;
    public static final float setupButtonWidthToWidthRatio = (float) 0.08;//0.0465;
    public static final float setupUnitTextWidthToWidthRatio = (float) 0.05;//0.04;

    public static final float setupLabelHeightToWidthRatio = (float) 0.03;//0.0201;
    public static final float setupButtonHeightToWidthRatio = setupLabelHeightToWidthRatio;
    public static final float setupUnitTextHeightToWidthRatio = (float) 0.03;//0.015;
    public static final float setupUnitSwitchRightMarginToWidthRatio = (float) 0.0;

    public static final float setupBatterySwitchLeftMarginToWidthRatio = setupLabelWidthToWidthRatio - (float) 0.018;

    public static final float setupGaugeBarRightMarginToWidthRatio = (float) 0.107;

    public static final float setupLabelGapToWidthRatio = (float) 0.03;//0.0201;



    public static final float setupUnitWidthToUnitHeightRatio = (float) 13.35;//0.0201;
    public static final float setupBatteryDisplayWidthToUnitHeightRatio = (float) 13.14;//0.0201;

    public static final float setupRowTopMarginToWidthRatio = (float) 0.0412;
    public static final float setupRowInnerGapToWidthRatio = (float) 0.0225;


    public static final long menuDropTimeInMS = (long)500.0;

    public static final float topBarHeightToWidthRatio = (float) (0.05*0.9);
    public static final float topBarHeightToHeightRatio = (float) (0.079);
    public static final float topBarMenuWidthToWidthRatio = (float) 0.03;
    public static final float topBarLineThicknessToWidthRatio = (float) 0.002;
    public static final float topBarLineHeightGapToWidthRatio = (float) 0.004;
    public static final float topBarVSwidthToWidthRatio = (float) 0.12;
    public static final float topBarHSwidthToWidthRatio = topBarVSwidthToWidthRatio;
    public static final float topBarHwidthToWidthRatio = (float) 0.0705;


    public static final float topBarTinyGapToWidthRatio = (float) 0.008;
    public static final float topBarSmallGapToWidthRatio = (float) 0.014;

    public static final float topBarStatusWidthToWidthRatio = (float) (2*(0.5-(topBarMenuWidthToWidthRatio+topBarLineThicknessToWidthRatio
                            +2*topBarTinyGapToWidthRatio+topBarVSwidthToWidthRatio+topBarSmallGapToWidthRatio
                        +topBarHSwidthToWidthRatio+topBarSmallGapToWidthRatio+topBarHwidthToWidthRatio+topBarSmallGapToWidthRatio)));

    public static final float topBarLTEwidthToWidthRatio = (float) 0.10;
    public static final float topBarGPSwidthToWidthRatio = (float) 0.09;
    public static final float topBarBatteryWidthtoWidth = (float) 0.049;
    public static final float topBarBatteryTextWidthtoWidth = (float) 0.035;



    public static final float topBarCommonHeihgtToWidthRatio = (float) 0.03;

    public static final float topBarSetupWidthToWidthRatio = topBarCommonHeihgtToWidthRatio;

    public static final float topBarHheightToWidthRatio = (float) 0.023;

    public static final float topBarBatteryTextHeightToWidthRatio = (float) 0.025;

    public static final int MAX_CONNECTION_TRY_STEPS = 15;

    public static final int CODE_REQUEST = 1;

    public static final float aniCancelBtnCenterToHeightRatio = (float) 0.3;

    public static final float aniCancelBtnDiamToHeihtRatio = (float) 0.116;

    public static final float searchLayoutHeightToWidthRatio = (float) 0.045;

    public static final float searchItemHeightToWidthRatio = (float) 0.045;

    public static final float searchExitWidthToWidthRatio = (float) 0.060;

    public static final float searchIconWidthToWidthRatio = (float) 0.063;

    public static final float searchItemSeparatorHeightToWidthRatio = searchItemHeightToWidthRatio/8;

    public static final int numberOfSearchItems = 4;

    public static final float searchIconBoxHeightToWidthRatio = (float) 0.08671875;

    public static final float searchIconBoxWidthToWidthRatio = (float) (0.08671875*135.0/221.0);

    public static final float searchIconDeliveryWidthToWidthRatio = (float) (0.08671875*105.0/221.0);

    public static final float searchIconDeliveryEditWidthToWidthRatio = (float) (0.1328125) - searchIconDeliveryWidthToWidthRatio;

    public static final float searchIconEditGapToWidthRatio = (float) (0.0078125);

    public static final float packageIconWidthToDisplayWidthRatio = (float) 0.4;







}


