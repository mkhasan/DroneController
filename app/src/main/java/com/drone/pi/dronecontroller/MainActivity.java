package com.drone.pi.dronecontroller;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.location.Location;
import android.media.Image;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.VideoView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.drone.pi.dronecontroller.GLSurface.ShapeGLSurfaceView;
import com.drone.pi.dronecontroller.GLSurface.ShapeRenderer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.vision.CameraSource;
import com.jmedeisis.bugstick.Joystick;
import com.railbot.usrc.mediaplayer.FFError;
import com.railbot.usrc.mediaplayer.FFListener;
import com.railbot.usrc.mediaplayer.NotPlayingException;
import com.railbot.usrc.mediaplayer.StreamInfo;
import com.railbot.usrc.mediaplayer.VideoDisplay;
import com.railbot.usrc.mediaplayer.VideoPlayer;

import java.util.HashMap;
import java.util.HashSet;

import static android.R.attr.bitmap;
import static android.R.attr.bottomLeftRadius;
import static android.R.attr.canRetrieveWindowContent;
import static android.R.attr.editTextBackground;
import static android.R.attr.fingerprintAuthDrawable;
import static android.R.attr.flipInterval;
import static android.R.attr.fragment;
import static android.R.attr.publicKey;
import static android.R.attr.type;
import static com.drone.pi.dronecontroller.Def.MAX_CONNECTION_TRY_STEPS;
import static com.drone.pi.dronecontroller.Def.MQTT_PASSWD;
import static com.drone.pi.dronecontroller.Def.MQTT_USERNAME;
import static com.drone.pi.dronecontroller.Def.searchIconBoxWidthToWidthRatio;
import static com.drone.pi.dronecontroller.GlobalData.armDialerHeight;
import static com.drone.pi.dronecontroller.GlobalData.armDialerWidth;
import static com.drone.pi.dronecontroller.GlobalData.gimbalControllerHeight;
import static com.drone.pi.dronecontroller.GlobalData.gimbalControllerWidth;
import static com.drone.pi.dronecontroller.GlobalData.imageDialerOriginal;
import static com.drone.pi.dronecontroller.GlobalData.landingControllerHeight;
import static com.drone.pi.dronecontroller.GlobalData.landingControllerWidth;
import static com.drone.pi.dronecontroller.GlobalData.menuDisp;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity
        implements FFListener, BatteryBlinkListener, ItemSelListener, PIdroneListener/*, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener*/{


    private static final String TAG = MainActivity.class.getSimpleName();

    PIdronePublisher piDronePublisher = null;
    PIdroneSubscribeCallback piDroneSubscribeCallback = null;
    ControlStickListener controlStickListener = null;
    YawControlListener yawControlListener = null;

    private String cameraLink;
    private String serverAddr;
    private String mqttPort;

    private MqttClient mqttClient;


    //public static String brokerURL = "tcp://143.248.204.35:1883";
    public static String brokerURL = "tcp://192.168.0.7:1883";

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;





    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;





    ////////////////////////////  Variable declaration   ////////////////////////
    public enum RightControl {
        YAW_CONTROLLER,
        ROBOT_ARM_CONTROLLER,
        LANDING_CONTROLLER,
        GIMBAL_CONTROLLER,

    }

    public enum ConnectionStatus {
        CONNECTED,
        CONNECTING,
        NOT_CONNECTED

    }

    public enum Cameratype {
        CAMERA_ONE,
        CAMERA_TWO
    }

    public enum Recordtype {
        STILL_CUT,
        VIDEO_CLIP
    }

    public enum VideoMode {
        PLAY,
        STOP
    }

    public enum Mode {
        MANUAL,
        AUTO_PILOT,
        DELIVERY,
        READY_TO_ARM,
        READY_TO_AUTO_PILOT,
        READY_TO_LANDING,
        READY_TO_RETURN_HOME,
        SETUP,
        SEARCH
    }

    public enum SetupMode {
        GENERAL,
        COMM,
        CAMERA,
        AUTO_PILOT,
        EMERGENCY,
        EXIT
    }

    public enum Menu {
        OFF,
        ON
    }

    public enum DialerType {
        ARM_DIALER,
        LANDING_CONTROLLER
    }

    boolean connectionPending = false;


    BatInfoReceiver batInfoReceiver;

    private boolean connectionCanceled = false;

    private RightControl rightControl = RightControl.YAW_CONTROLLER;

    private Cameratype cameratype = Cameratype.CAMERA_TWO;

    private Recordtype recordtype = Recordtype.STILL_CUT;

    private VideoMode videoMode = VideoMode.PLAY;

    private Mode currentMode = Mode.MANUAL;

    private Menu menu = Menu.OFF;



    private SetupMode currentSetup = SetupMode.GENERAL;


    private ImageView topBarVS;
    private ImageView topBarHS;
    private ImageView topBarH;
    private ImageButton topBarMenuBtn;
    private ImageView topBarStatus;
    private ImageView topBarGPS;
    private ImageView topBarGPSsignal;
    private ImageView topBarLTE;
    private ImageView topBarLTEsignal;
    private ImageView topBarBattery = null;
    private ImageView topBarBatteryText;
    private ImageButton topBarSetupBtn;



    private View surfaceView;
    private ImageView backgroundView;

    private VideoPlayer mMpegPlayer = null;


    public boolean connected = false;

    public ConnectionCheckTask connTask = null;

    public MqttTask mqttTask = null;


    public RelativeLayout menuLayout;


    private ImageButton armBtn;
    private ImageButton landingBtn;


    private ImageButton controlBtn;
    private ImageButton drGimBtn;
    private ImageButton cameraBtn;

    private ImageButton robotControl;
    private ImageButton cameraClick;




    private ImageView landingController;
    private ImageView gimbalController;

    private RelativeLayout gimbalOGL;





    private YawController yawController;

    com.jmedeisis.bugstick.Joystick joystick;

    ImageView packageDisplay;

    private ImageView armDialer;

    DialerListener armDialerListener = null;
    DialerListener landingControllerListener = null;

    private ImageButton setupGeneralBtn;
    private ImageButton setupCommBtn;
    private ImageButton setupCameraBtn;
    private ImageButton setupAutoBtn;
    private ImageButton setupEmergencyBtn;
    private ImageButton setupExitBtn;

    private ImageButton[] mapCamBtn;        // 0 is on Map 1 is on surfaceview
    private ImageButton[] reductionBtn;
    private ImageButton[] searchBtn;
    private ImageButton[] homeBtn;




    ConnectionStatus cameraStatus = ConnectionStatus.NOT_CONNECTED;
    ConnectionStatus mqttStatus = ConnectionStatus.NOT_CONNECTED;

    RelativeLayout connectionProgress;
    RelativeLayout setup;


    private OnGimbalListener onGimbalListener = null;
    private GestureDetector gestureDetector = null;



    private ImageButton stopBtn;
    private Circle aniCancelBtn;

    public LatLng currentLocation;

    private EditText searchText;

    ImageButton searchIcon;

    ImageButton searchCross;

    ImageButton myLocation;

    EditText packageWeight;
    ImageButton deliveryGo;

    EditText autoSpeed;
    ImageButton autoGo;
    ImageButton searchExit;
    ImageView cancelStatus;


    private ShapeGLSurfaceView aGLSurfaceView;
    private ShapeRenderer aRenderer;



    //////////////////////////  Variable declaration   ///////////////////////////

       /*
            private ImageButton[] mapCamBtn;        // 0 is on Map 1 is on surfaceview
            private ImageButton[] reductionBtn;
            private ImageButton[] searchBtn;
            private ImageButton[] homeBtn;

        */

    void SetCameraView(boolean show) {

        if (show == false) {
            surfaceView.setVisibility(View.GONE);
            backgroundView.setVisibility(View.VISIBLE);
        } else {
            backgroundView.setVisibility(View.GONE);
            surfaceView.setVisibility(View.VISIBLE);

        }

    }

    void UpdateView () {

        if(currentMode == Mode.SEARCH)
            return;

        if(currentMode == Mode.DELIVERY || currentMode == Mode.AUTO_PILOT) {

            yawController.setVisibility(View.GONE);
            armDialer.setVisibility(View.GONE);
            landingController.setVisibility(View.GONE);
            //gimbalController.setVisibility(View.VISIBLE);
            ShowGimbalControlelr(false);
            if (cameratype == Cameratype.CAMERA_TWO)
                ;//gimbalController.setBackgroundResource(R.drawable.gimbal_ball);
            else if (cameratype == Cameratype.CAMERA_ONE)
                ;

            aniCancelBtn.animateImmediately = false;

            if(currentMode == Mode.DELIVERY)
                aniCancelBtn.setBackgroundResource(R.drawable.icon_delivery_start_and_cancel);
            else
                aniCancelBtn.setBackgroundResource(R.drawable.icon_auto_start_and_cancel);

            aniCancelBtn.bringToFront();
            aniCancelBtn.setVisibility(View.VISIBLE);

            packageDisplay.setBackgroundResource((currentMode == Mode.DELIVERY ? R.drawable.joystick_box_faded : R.drawable.joystick_auto_faded));

            joystick.setVisibility(View.GONE);
            packageDisplay.setVisibility(View.VISIBLE);
            findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_brown);
            findViewById(R.id.top_bar_status).setBackgroundResource((currentMode == Mode.DELIVERY ? R.drawable.top_bar_status_ready_to_delivery : R.drawable.top_bar_status_ready_to_auto));


            if (mqttStatus == ConnectionStatus.CONNECTED && cameraStatus == ConnectionStatus.CONNECTED) {
                SetCameraView(true);
            }

            Log.e(TAG, "In delivery mode");



            return;


        }


        packageDisplay.setVisibility(View.GONE);
        joystick.setVisibility(View.VISIBLE);

        switch (rightControl) {
            case YAW_CONTROLLER:
                yawController.setVisibility(View.VISIBLE);
                aniCancelBtn.setVisibility(View.GONE);
                armDialer.setVisibility(View.GONE);
                landingController.setVisibility(View.GONE);
                gimbalController.setVisibility(View.GONE);
                ShowGimbalControlelr(false);


                controlBtn.setBackgroundResource(R.drawable.drone_control);


                break;
            case ROBOT_ARM_CONTROLLER:
                yawController.setVisibility(View.GONE);
                armDialer.setVisibility(View.VISIBLE);
                aniCancelBtn.setVisibility(View.VISIBLE);
                landingController.setVisibility(View.GONE);
                gimbalController.setVisibility(View.GONE);
                ShowGimbalControlelr(false);

                break;
            case LANDING_CONTROLLER:
                yawController.setVisibility(View.GONE);
                armDialer.setVisibility(View.GONE);
                landingController.setVisibility(View.VISIBLE);
                aniCancelBtn.setVisibility(View.VISIBLE);
                ShowGimbalControlelr(false);

                break;

            case GIMBAL_CONTROLLER:
                yawController.setVisibility(View.GONE);
                armDialer.setVisibility(View.GONE);
                landingController.setVisibility(View.GONE);
                //gimbalController.setVisibility(View.VISIBLE);
                ShowGimbalControlelr(true);
                if (cameratype == Cameratype.CAMERA_TWO)
                    ;//gimbalController.setBackgroundResource(R.drawable.gimbal_ball);
                else if (cameratype == Cameratype.CAMERA_ONE)
                    ;

                controlBtn.setBackgroundResource(R.drawable.camera_control);
                break;


        }

        Log.e(TAG, "current MOde : " + currentMode + " " + mqttStatus + " " + cameraStatus);

        if (cameraStatus == ConnectionStatus.CONNECTED)
            SetCameraView(true);

        if (mqttStatus == ConnectionStatus.CONNECTED) {

            switch (currentMode) {
                case MANUAL:
                    findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_green);
                    topBarStatus.setBackgroundResource(R.drawable.top_bar_status_manual);
                    break;
                case AUTO_PILOT:
                    topBarStatus.setBackgroundResource(R.drawable.top_bar_status_auto_pilot);
                    findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_green);
                    break;
                case DELIVERY:
                    findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_green);
                    topBarStatus.setBackgroundResource(R.drawable.top_bar_status_delivery);
                    break;
                case READY_TO_ARM:
                    findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_green);
                    topBarStatus.setBackgroundResource(R.drawable.top_bar_status_ready_to_arm);
                    break;
                case READY_TO_LANDING:
                    findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_green);
                    topBarStatus.setBackgroundResource(R.drawable.top_bar_status_ready_to_landing);
                    break;
                case READY_TO_RETURN_HOME:
                    findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_green);
                    topBarStatus.setBackgroundResource(R.drawable.top_bar_status_ready_to_return_home);
                    break;


            }
            connectionProgress.setVisibility(View.GONE);
        }
        else {
            SetCameraView(false);

            if(mqttStatus == ConnectionStatus.CONNECTING || cameraStatus == ConnectionStatus.CONNECTING) {
                connectionProgress.bringToFront();
                connectionProgress.setVisibility(View.VISIBLE);
                connectionCanceled = false;

            }
            else {

                if (cameraStatus == ConnectionStatus.CONNECTED)
                    ;
                else
                    ;



                connectionProgress.setVisibility(View.GONE);

            }

        }





    }


    void UpdateSetupIcon(ImageButton btn, Bitmap image, boolean isSelected) {

        /*
        if (isSelected)
            btn.setBackgroundResource(R.drawable.top_background_green);
        else
            btn.setBackgroundResource(R.drawable.top_background_gray);
        */
        btn.setImageBitmap(image);
    }

    void UpdateSetupView() {


        switch (currentSetup) {
            case GENERAL:
                UpdateSetupIcon(setupGeneralBtn, BitmapFactory.decodeResource(getResources(), R.drawable.general_touched), true);
                UpdateSetupIcon(setupCommBtn, BitmapFactory.decodeResource(getResources(), R.drawable.communication_no_touched), false);
                UpdateSetupIcon(setupCameraBtn, BitmapFactory.decodeResource(getResources(), R.drawable.camera_no_touched), false);
                UpdateSetupIcon(setupAutoBtn, BitmapFactory.decodeResource(getResources(), R.drawable.auto_no_touched), false);
                UpdateSetupIcon(setupEmergencyBtn, BitmapFactory.decodeResource(getResources(), R.drawable.emergency_no_touched), false);
                UpdateSetupIcon(setupExitBtn, BitmapFactory.decodeResource(getResources(), R.drawable.exit_no_touched), false);

                findViewById(R.id.general_row_one).setVisibility(View.VISIBLE);
                findViewById(R.id.general_row_two).setVisibility(View.VISIBLE);
                findViewById(R.id.general_row_three).setVisibility(View.VISIBLE);

                findViewById(R.id.setup_communication).setVisibility(View.GONE);

                findViewById(R.id.camera_row_one).setVisibility(View.GONE);
                findViewById(R.id.camera_row_two).setVisibility(View.GONE);

                findViewById(R.id.auto_pilot_layout).setVisibility(View.GONE);

                findViewById(R.id.emergency_layout).setVisibility(View.GONE);

                findViewById(R.id.setup_exit_layout).setVisibility(View.GONE);

                break;

            case COMM:
                UpdateSetupIcon(setupGeneralBtn, BitmapFactory.decodeResource(getResources(), R.drawable.general_no_touched), false);
                UpdateSetupIcon(setupCommBtn, BitmapFactory.decodeResource(getResources(), R.drawable.communication_touched), true);
                UpdateSetupIcon(setupCameraBtn, BitmapFactory.decodeResource(getResources(), R.drawable.camera_no_touched), false);
                UpdateSetupIcon(setupAutoBtn, BitmapFactory.decodeResource(getResources(), R.drawable.auto_no_touched), false);
                UpdateSetupIcon(setupEmergencyBtn, BitmapFactory.decodeResource(getResources(), R.drawable.emergency_no_touched), false);
                UpdateSetupIcon(setupExitBtn, BitmapFactory.decodeResource(getResources(), R.drawable.exit_no_touched), false);

                findViewById(R.id.general_row_one).setVisibility(View.GONE);
                findViewById(R.id.general_row_two).setVisibility(View.GONE);
                findViewById(R.id.general_row_three).setVisibility(View.GONE);

                findViewById(R.id.setup_communication).setVisibility(View.VISIBLE);

                findViewById(R.id.camera_row_one).setVisibility(View.GONE);
                findViewById(R.id.camera_row_two).setVisibility(View.GONE);

                findViewById(R.id.auto_pilot_layout).setVisibility(View.GONE);

                findViewById(R.id.emergency_layout).setVisibility(View.GONE);

                findViewById(R.id.setup_exit_layout).setVisibility(View.GONE);

                break;

            case CAMERA:
                UpdateSetupIcon(setupGeneralBtn, BitmapFactory.decodeResource(getResources(), R.drawable.general_no_touched), false);
                UpdateSetupIcon(setupCommBtn, BitmapFactory.decodeResource(getResources(), R.drawable.communication_no_touched), false);
                UpdateSetupIcon(setupCameraBtn, BitmapFactory.decodeResource(getResources(), R.drawable.camera_touched), true);
                UpdateSetupIcon(setupAutoBtn, BitmapFactory.decodeResource(getResources(), R.drawable.auto_no_touched), false);
                UpdateSetupIcon(setupEmergencyBtn, BitmapFactory.decodeResource(getResources(), R.drawable.emergency_no_touched), false);
                UpdateSetupIcon(setupExitBtn, BitmapFactory.decodeResource(getResources(), R.drawable.exit_no_touched), false);

                findViewById(R.id.general_row_one).setVisibility(View.GONE);
                findViewById(R.id.general_row_two).setVisibility(View.GONE);
                findViewById(R.id.general_row_three).setVisibility(View.GONE);

                findViewById(R.id.setup_communication).setVisibility(View.GONE);

                findViewById(R.id.camera_row_one).setVisibility(View.VISIBLE);
                findViewById(R.id.camera_row_two).setVisibility(View.VISIBLE);

                findViewById(R.id.auto_pilot_layout).setVisibility(View.GONE);

                findViewById(R.id.emergency_layout).setVisibility(View.GONE);

                findViewById(R.id.setup_exit_layout).setVisibility(View.GONE);

                break;

            case AUTO_PILOT:
                UpdateSetupIcon(setupGeneralBtn, BitmapFactory.decodeResource(getResources(), R.drawable.general_no_touched), false);
                UpdateSetupIcon(setupCommBtn, BitmapFactory.decodeResource(getResources(), R.drawable.communication_no_touched), false);
                UpdateSetupIcon(setupCameraBtn, BitmapFactory.decodeResource(getResources(), R.drawable.camera_no_touched), false);
                UpdateSetupIcon(setupAutoBtn, BitmapFactory.decodeResource(getResources(), R.drawable.auto_touched), true);
                UpdateSetupIcon(setupEmergencyBtn, BitmapFactory.decodeResource(getResources(), R.drawable.emergency_no_touched), false);
                UpdateSetupIcon(setupExitBtn, BitmapFactory.decodeResource(getResources(), R.drawable.exit_no_touched), false);

                findViewById(R.id.general_row_one).setVisibility(View.GONE);
                findViewById(R.id.general_row_two).setVisibility(View.GONE);
                findViewById(R.id.general_row_three).setVisibility(View.GONE);

                findViewById(R.id.setup_communication).setVisibility(View.GONE);

                findViewById(R.id.camera_row_one).setVisibility(View.GONE);
                findViewById(R.id.camera_row_two).setVisibility(View.GONE);

                findViewById(R.id.auto_pilot_layout).setVisibility(View.VISIBLE);

                findViewById(R.id.emergency_layout).setVisibility(View.GONE);

                findViewById(R.id.setup_exit_layout).setVisibility(View.GONE);

                break;

            case EMERGENCY:
                UpdateSetupIcon(setupGeneralBtn, BitmapFactory.decodeResource(getResources(), R.drawable.general_no_touched), false);
                UpdateSetupIcon(setupCommBtn, BitmapFactory.decodeResource(getResources(), R.drawable.communication_no_touched), false);
                UpdateSetupIcon(setupCameraBtn, BitmapFactory.decodeResource(getResources(), R.drawable.camera_no_touched), false);
                UpdateSetupIcon(setupAutoBtn, BitmapFactory.decodeResource(getResources(), R.drawable.auto_no_touched), false);
                UpdateSetupIcon(setupEmergencyBtn, BitmapFactory.decodeResource(getResources(), R.drawable.emergency_touched), true);
                UpdateSetupIcon(setupExitBtn, BitmapFactory.decodeResource(getResources(), R.drawable.exit_no_touched), false);

                findViewById(R.id.general_row_one).setVisibility(View.GONE);
                findViewById(R.id.general_row_two).setVisibility(View.GONE);
                findViewById(R.id.general_row_three).setVisibility(View.GONE);

                findViewById(R.id.setup_communication).setVisibility(View.GONE);

                findViewById(R.id.camera_row_one).setVisibility(View.GONE);
                findViewById(R.id.camera_row_two).setVisibility(View.GONE);

                findViewById(R.id.auto_pilot_layout).setVisibility(View.GONE);

                findViewById(R.id.emergency_layout).setVisibility(View.VISIBLE);

                findViewById(R.id.setup_exit_layout).setVisibility(View.GONE);

                break;

            case EXIT:
                UpdateSetupIcon(setupGeneralBtn, BitmapFactory.decodeResource(getResources(), R.drawable.general_no_touched), false);
                UpdateSetupIcon(setupCommBtn, BitmapFactory.decodeResource(getResources(), R.drawable.communication_no_touched), false);
                UpdateSetupIcon(setupCameraBtn, BitmapFactory.decodeResource(getResources(), R.drawable.camera_no_touched), false);
                UpdateSetupIcon(setupAutoBtn, BitmapFactory.decodeResource(getResources(), R.drawable.auto_no_touched), false);
                UpdateSetupIcon(setupEmergencyBtn, BitmapFactory.decodeResource(getResources(), R.drawable.emergency_no_touched), false);
                UpdateSetupIcon(setupExitBtn, BitmapFactory.decodeResource(getResources(), R.drawable.exit_touched), true);

                findViewById(R.id.general_row_one).setVisibility(View.GONE);
                findViewById(R.id.general_row_two).setVisibility(View.GONE);
                findViewById(R.id.general_row_three).setVisibility(View.GONE);

                findViewById(R.id.setup_communication).setVisibility(View.GONE);

                findViewById(R.id.camera_row_one).setVisibility(View.GONE);
                findViewById(R.id.camera_row_two).setVisibility(View.GONE);

                findViewById(R.id.auto_pilot_layout).setVisibility(View.GONE);

                findViewById(R.id.emergency_layout).setVisibility(View.GONE);

                findViewById(R.id.setup_exit_layout).setVisibility(View.VISIBLE);

                break;
        }
    }



    private class MqttTask extends AsyncTask<Void, String, String> {
        public boolean finish = false;
        MqttTask() {


        }

        @Override
        protected String doInBackground(Void... params) {


            while (finish == false) {
                try {

                    piDronePublisher.PublishMV();
                }
                catch (MqttException e) {
                    Log.e(TAG, "Mqtt publishing error");
                }

                try {
                    sleep(500);     // change this value to change frequency
                }

                catch (Exception e) {
                    Log.e(TAG, "Mqtt task sleep Error");
                }

                Log.e(TAG, "publishinng");
            }
            return null;



        }


        @Override
        protected void onProgressUpdate(String... values) {


        }

        @Override
        protected void onPostExecute(String str) {


        }

    }


    private class ConnectionCheckTask extends AsyncTask<Void, String, String> {
        TextView textView;

        public boolean terminate = false;
        public boolean finish = false;
        public boolean firstTime = true;
        int k=0;



        ConnectionCheckTask() {

        }


        @Override
        protected String doInBackground(Void... params) {

            int i = 0;

            if (mqttStatus == ConnectionStatus.CONNECTING) {


                boolean flag = false;


                while (flag == false && finish == false) {


                    if (locationHandler != null && locationHandler.IsInitialized() == true)
                        flag = true;
                    else
                        flag = false;

                    try {


                        if (mqttStatus != ConnectionStatus.CONNECTED) {
                            Log.e(TAG, "Trying to Connect");
                            MqttConnectOptions options = new MqttConnectOptions();
                            options.setCleanSession(true);
                            options.setConnectionTimeout(1);
                            options.setUserName(MQTT_USERNAME);
                            options.setPassword(MQTT_PASSWD.toCharArray());
                            mqttClient.connect(options);
                            Log.e(TAG, "Done");

                            mqttClient.subscribe(PIdroneSubscribeCallback.TOPIC_GSP);
                            Log.e(TAG, "Mqtt connected");
                            mqttStatus = ConnectionStatus.CONNECTED;
                            piDronePublisher = new PIdronePublisher(mqttClient);
                            if (controlStickListener != null)
                                controlStickListener.SetPubisher(piDronePublisher);
                            if (yawControlListener != null)
                                yawControlListener.SetPubisher(piDronePublisher);
                            if (onGimbalListener != null)
                                onGimbalListener.SetPubisher(piDronePublisher);
                        }


                    } catch (MqttException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Mqtt connection Error");
                        flag = false;


                    }
                    try {
                        sleep(1000);
                        Log.e(TAG, "ConnTask");
                    } catch (Exception e) {
                        Log.e(TAG, "Sleep Error");
                    }

                    this.publishProgress(Integer.toString(i));
                    if (i >= MAX_CONNECTION_TRY_STEPS || connectionCanceled) {
                        finish = true;
                        cameraStatus = ConnectionStatus.NOT_CONNECTED;
                        Log.e(TAG, "Timeout ");


                    }
                    i++;

                    if (terminate == true) {
                        return null;
                    }

                }


            }


            return null;

        }

        @Override
        protected void onProgressUpdate(String... values) {


            if (terminate == true)
                return;

            if (Integer.parseInt(values[0]) == MAX_CONNECTION_TRY_STEPS || connectionCanceled) {


                boolean commLost = (mqttStatus == ConnectionStatus.CONNECTING || cameraStatus == ConnectionStatus.CONNECTING || connectionCanceled);

                if(commLost) {
                    findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_red);
                    findViewById(R.id.top_bar_status).setBackgroundResource(R.drawable.top_bar_status_comm_lost);
                    mqttStatus = ConnectionStatus.NOT_CONNECTED;
                    cameraStatus = ConnectionStatus.NOT_CONNECTED;

                }


                findViewById(R.id.cancel_or_try).setVisibility(View.GONE);

                UpdateView();
            }



        }

        @Override
        protected void onPostExecute(String str) {


            Log.e(TAG, "Post Exec Finishing");

            if (cameraStatus == ConnectionStatus.CONNECTING) {
                ConnectNativePlayer();
            }






            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }


    }

    /*
    private class LongOperation extends AsyncTask<Void, Void, String> {

        MqttClient mqttClient;

        LongOperation(MqttClient client) {
            mqttClient = client;
        }

        @Override
        protected String doInBackground(Void... params) {


            try {

                mqttClient.connect();

                //Subscribe to all subtopics of home
                final String topic = "topic/led";
                mqttClient.subscribe(topic);


                //Log.e(TAG, "Subscriber is now listening to "+topic);

            } catch (MqttException e) {
                e.printStackTrace();
                Log.e(TAG, "Error");


            }
            return "";

        }



        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e(TAG, "Finishing");
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }


    }
    */

    public class BoolRef { public boolean value; }


    public class DialerListener implements View.OnTouchListener {

        private double startAngle;
        private double dialerPosition = 0.0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            BoolRef touched = new BoolRef();

            if (currentMode == Mode.SETUP)
                return false;

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    startAngle = getAngle(event.getX(), event.getY(), touched);
                    Log.e(TAG, "X: "+event.getX()+ " Y: "+event.getY()+" Start Angle: "+startAngle + " touched: "+touched.value);
                    break;

                case MotionEvent.ACTION_MOVE:
                    double currentAngle = getAngle(event.getX(), event.getY(), touched);
                    if (touched.value) {
                        dialerPosition += (currentAngle-startAngle);
                        if (dialerPosition > (float) 360.0)
                            dialerPosition = dialerPosition - (float) 360.0;

                        while (dialerPosition < (float) 0.0)
                            dialerPosition += 360.0;
                        rotateDialer((float) (startAngle - currentAngle));
                        startAngle = currentAngle;
                        Log.e(TAG, "Current Angle: " + currentAngle);
                    }
                    break;

                case MotionEvent.ACTION_UP:

                    break;
            }



            return touched.value;
        }

        public void SetDialPositon(double position) {
            dialerPosition = position;
        }

        public double GetDialPosition() {
            return dialerPosition;
        }

    }



    private void rotateDialer(float degrees) {

        if (rightControl == RightControl.ROBOT_ARM_CONTROLLER) {
            GlobalData.armDialerMatrix.postRotate(degrees, armDialerWidth / 2, armDialerHeight / 2);

            armDialer.setImageMatrix(GlobalData.armDialerMatrix);
        }
        else if (rightControl == RightControl.LANDING_CONTROLLER) {
            GlobalData.landingControllerMatrix.postRotate(degrees, landingControllerWidth / 2, landingControllerHeight / 2);

            landingController.setImageMatrix(GlobalData.landingControllerMatrix);


        }


    }



    private double getAngle(double xTouch, double yTouch, BoolRef dialerTouched) {
        double x = xTouch - (armDialerWidth / 2d);
        double y = armDialerHeight - yTouch - (armDialerHeight / 2d);


        dialerTouched.value = false;
        if (x*x+y*y < (armDialerWidth/2)*(armDialerWidth/2)*Def.armDialerRatio*Def.armDialerRatio)
          dialerTouched.value = true;

        switch (getQuadrant(x, y)) {
            case 1:
                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 2:
                return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 3:
                return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            case 4:
                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            default:
                return 0;
        }
    }

    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }


    public String getMacAddress(Context context) {
        WifiManager wimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String macAddress = wimanager.getConnectionInfo().getMacAddress();
        if (macAddress == null) {
            return null;
        }

        return macAddress;
    }

    private void ErrorMsg(String msg) {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name)
                .

        setMessage(msg)
                    .

        setOnCancelListener(
                            new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel (DialogInterface dialog){
                finish();
            }
        }).

        show();

    }

    //public GoogleApiClient mGoogleApiClient;

    private LocationHandler locationHandler;

    void initGL() {
        aGLSurfaceView.setEGLContextClientVersion(2);

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // Set the renderer to our demo renderer, defined below.
        aRenderer = new ShapeRenderer(this, aGLSurfaceView, 5);

        aGLSurfaceView.setZOrderOnTop(true);
        aGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        aGLSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);

        aGLSurfaceView.setRenderer(aRenderer, displayMetrics.density, 5);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();



        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().addFlags(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //getWindow().addFlags(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Log.e(TAG, "soft input " + getWindow().getAttributes().softInputMode + " target " + WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_main);

        aGLSurfaceView = (ShapeGLSurfaceView) findViewById(R.id.gl_surface_view);

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            /*

            aGLSurfaceView.setEGLContextClientVersion(2);

            final DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            // Set the renderer to our demo renderer, defined below.
            aRenderer = new ShapeRenderer(this, aGLSurfaceView, 5);
            aGLSurfaceView.setRenderer(aRenderer, displayMetrics.density, 5);
            */

            initGL();
        } else {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return;
        }

        settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        SharedSettings.getInstance(this).loadPrefSettings();
        SharedSettings.getInstance().savePrefSettings();

        //String url = "http://143.248.250.124:8080/?action=stream";

        /*
        LocationHandler.edtFormattedAddress = settings.getStringSet(LocationHandler.LOCATION_HISTORY_ADDRESS, new HashSet<String>());
        LocationHandler.edtLocationLat = settings.getStringSet(LocationHandler.LOCATION_HISTORY_LAT, new HashSet<String>());
        LocationHandler.edtLocationLng = settings.getStringSet(LocationHandler.LOCATION_HISTORY_LNG, new HashSet<String>());

        LocationHandler.ShowStrSet(LocationHandler.edtFormattedAddress, " Format addr ");
        LocationHandler.ShowStrSet(LocationHandler.edtLocationLat, " Lat ");
        LocationHandler.ShowStrSet(LocationHandler.edtLocationLng, " Lng ");
        */

        LocationHandler.edtLocaiotnHistory = settings.getStringSet(LocationHandler.LOCATION_HISTORY, new HashSet<String>());

        LocationHandler.ShowStrSet(LocationHandler.edtLocaiotnHistory, " Locaiton hist ");


        final float currentLatitude = settings.getFloat("currentLatitude", LocationHandler.DEFAULT_LATITUDE);
        float currentLongitude = settings.getFloat("currentLongitude", LocationHandler.DEFAULT_LONGITUDE);


        currentLocation = new LatLng(currentLatitude, currentLongitude);

        batInfoReceiver = new BatInfoReceiver();
        Log.e(TAG, "Registering receiver");
        try {

            this.registerReceiver(this.batInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
        catch (RuntimeRemoteException e) {
            Log.e(TAG, "Receiver not registered");
            connectionCanceled = true;
            finish();
        }

        Log.e(TAG, "Receiver registered");

        batInfoReceiver.SetListener(this);




        //mLocationSource = new LongPressLocationSource();
/*
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        */

        locationHandler = new LocationHandler(this, currentLocation);
        locationHandler.setListener(this);

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
  //              .enableAutoManage(this /* FragmentActivity */,
                        //this /* OnConnectionFailedListener *///)
                //.addConnectionCallbacks(this)
                //.addApi(LocationServices.API)
                //.addApi(Places.GEO_DATA_API)
                //.addApi(Places.PLACE_DETECTION_API)
                //.build();
//        mGoogleApiClient.connect();


        GlobalData.armDialerHeight = GlobalData.armDialerWidth = GlobalData.gimbalControllerHeight
                = GlobalData.gimbalControllerWidth = GlobalData.landingControllerHeight = GlobalData.landingControllerWidth = 0;

        topBarMenuBtn = (ImageButton) findViewById(R.id.top_bar_menu);
        topBarVS = (ImageView) findViewById(R.id.top_bar_vs);
        topBarHS = (ImageView) findViewById(R.id.top_bar_hs);
        topBarH = (ImageView) findViewById(R.id.top_bar_h);
        topBarLTE = (ImageView) findViewById(R.id.top_bar_lte);
        topBarGPS = (ImageView) findViewById(R.id.top_bar_gps);
        topBarBattery = (ImageView) findViewById(R.id.top_bar_battery);
        topBarBatteryText = (ImageView) findViewById(R.id.top_bar_battery_text);


        //topBarVS.setVisibility(View.GONE);
        //topBarHS.setVisibility(View.GONE);
        //topBarH.setVisibility(View.GONE);
        topBarStatus = (ImageView) findViewById(R.id.top_bar_status);



        menuLayout = (RelativeLayout) findViewById(R.id.top_menu);
        armBtn = (ImageButton) findViewById(R.id.robot_arm_btn);

        armDialer = (ImageView) findViewById(R.id.arm_dialer);
        landingBtn = (ImageButton) findViewById(R.id.landing_btn);

        controlBtn = (ImageButton) findViewById(R.id.control_btn);

        drGimBtn = (ImageButton) findViewById(R.id.dr_gim_btn);

        cameraBtn = (ImageButton) findViewById(R.id.camera_btn);


        landingController = (ImageView) findViewById(R.id.landing_controller);
        gimbalController = (ImageView) findViewById(R.id.gimbal_controller);
        gimbalOGL = (RelativeLayout) findViewById(R.id.gimbal_ogl);


        robotControl = (ImageButton) findViewById(R.id.robot_control);
        cameraClick = (ImageButton) findViewById(R.id.camera_click);

        topBarSetupBtn = (ImageButton) findViewById(R.id.top_bar_setup);



        yawController = (YawController) findViewById(R.id.yaw_controller);
        //yaw_controller.Set(this);


        if (GlobalData.imageDialerOriginal == null) {
            GlobalData.imageDialerOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.arm_dialer);
        }

        if (GlobalData.imageGimbalOriginal == null) {
            GlobalData.imageGimbalOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.gimbal_ball);
        }

        if (GlobalData.imageLandingOriginal == null) {
            GlobalData.imageLandingOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.landing_controller);
        }



        if (GlobalData.armDialerMatrix == null) {
            GlobalData.armDialerMatrix = new Matrix();
        } else {
            // not needed, you can also post the matrix immediately to restore the old state
            GlobalData.armDialerMatrix.reset();
        }

        if (GlobalData.landingControllerMatrix == null) {
            GlobalData.landingControllerMatrix = new Matrix();
        } else {
            // not needed, you can also post the matrix immediately to restore the old state
            GlobalData.landingControllerMatrix.reset();
        }



        connectionProgress = (RelativeLayout) findViewById(R.id.connection_progress);

        setup = (RelativeLayout) findViewById(R.id.setup);

        setupGeneralBtn = (ImageButton) findViewById(R.id.setup_general_btn);
        setupCommBtn = (ImageButton) findViewById(R.id.setup_comm_btn);
        setupCameraBtn = (ImageButton) findViewById(R.id.setup_camera_btn);
        setupAutoBtn = (ImageButton) findViewById(R.id.setup_auto_pilot_btn);
        setupEmergencyBtn = (ImageButton) findViewById(R.id.setup_emergency_btn);
        setupExitBtn = (ImageButton) findViewById(R.id.setup_exit_btn);


        surfaceView = findViewById(R.id.surface_view);
        backgroundView = (ImageView) findViewById(R.id.background_view);

        mapCamBtn = new ImageButton[2];
        reductionBtn = new ImageButton[2];
        searchBtn = new ImageButton[2];
        homeBtn = new ImageButton[2];

        /*
            private ImageButton[] mapCamBtn;        // 0 is on Map 1 is on surfaceview
            private ImageButton[] reductionBtn;
            private ImageButton[] searchBtn;
            private ImageButton[] homeBtn;

        */

        mapCamBtn[0] = (ImageButton) findViewById(R.id.map_pic_btn);
        mapCamBtn[1] = (ImageButton) findViewById(R.id.map_pic_cam_btn);

        reductionBtn[0] = (ImageButton) findViewById(R.id.reduction_btn);
        reductionBtn[1] = (ImageButton) findViewById(R.id.reduction_cam_btn);

        searchBtn[0] = (ImageButton) findViewById(R.id.search_btn);
        searchBtn[1] = (ImageButton) findViewById(R.id.search_cam_btn);

        homeBtn[0] = (ImageButton) findViewById(R.id.home_btn);
        homeBtn[1] = (ImageButton) findViewById(R.id.home_cam_btn);


        aniCancelBtn = (Circle) findViewById(R.id.ani_cancel_btn);

        joystick = (com.jmedeisis.bugstick.Joystick) findViewById(R.id.joystick);

        packageDisplay = (ImageView) findViewById(R.id.package_display);

        stopBtn = (ImageButton) findViewById(R.id.stop_btn);

        searchText = (EditText) findViewById(R.id.search_text);

        /////////////////////////////////////////////////////////////////////

        searchText.setText("woolpyong-dong 750 apt no 202 daejeon");
        /////////////////////////////////////////////////////////////////////

        searchIcon = (ImageButton) findViewById(R.id.search_icon);

        searchCross = (ImageButton) findViewById(R.id.search_cross);

        myLocation = (ImageButton) findViewById(R.id.my_location);

        packageWeight= (EditText) findViewById(R.id.package_weight);

        deliveryGo = (ImageButton) findViewById(R.id.delivery_go);

        autoSpeed = (EditText) findViewById(R.id.auto_speed);

        autoGo = (ImageButton) findViewById(R.id.auto_go);

        searchExit = (ImageButton) findViewById(R.id.search_exit);

        cancelStatus = (ImageView) findViewById(R.id.cancel_status);
        aniCancelBtn.setStatusView(cancelStatus);

        ////////////////// variable assignment ////////////////////





        //////////////////// listeners ///////////////////////

        topBarMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowMenu();
            }
        });


        armBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) armBtn.getLayoutParams();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(currentMode == Mode.SETUP || connectionProgress.getVisibility() == View.VISIBLE)
                        return false;

                    if(rightControl == RightControl.ROBOT_ARM_CONTROLLER)
                        rightControl = RightControl.YAW_CONTROLLER;
                    else
                        rightControl = RightControl.ROBOT_ARM_CONTROLLER;

                    int prevWidth = lp.width;
                    lp.height = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnMaxHeightToWidthRatio);
                    lp.width = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnMaxHeightToWidthRatio);
                    lp.leftMargin = lp.leftMargin - (lp.width-prevWidth)/2;
                    armBtn.setLayoutParams(lp);
                    aniCancelBtn.setBackgroundResource(R.drawable.image_arm);
                    UpdateView();

                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    lp.height = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnMinHeightToWidthRatio);
                    lp.width = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnMinHeightToWidthRatio);
                    lp.leftMargin = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnLeftMargin0ToWidthRatio);
                    armBtn.setLayoutParams(lp);
                }
                return true;
            }
        });


        armDialerListener = new DialerListener();
        armDialer.setOnTouchListener(armDialerListener);

        landingControllerListener = new DialerListener();
        landingController.setOnTouchListener(landingControllerListener);


        landingBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) landingBtn.getLayoutParams();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(currentMode == Mode.SETUP || connectionProgress.getVisibility() == View.VISIBLE)
                        return false;

                    if(rightControl == RightControl.LANDING_CONTROLLER)
                        rightControl = RightControl.YAW_CONTROLLER;
                    else
                        rightControl = RightControl.LANDING_CONTROLLER;

                    int prevWidth = lp.width;
                    lp.height = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnMaxHeightToWidthRatio);
                    lp.width = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnMaxHeightToWidthRatio);
                    lp.leftMargin = lp.leftMargin - (lp.width-prevWidth)/2;
                    landingBtn.setLayoutParams(lp);
                    aniCancelBtn.setBackgroundResource(R.drawable.image_landing);
                    UpdateView();

                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    lp.height = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnMinHeightToWidthRatio);
                    lp.width = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnMinHeightToWidthRatio);
                    lp.leftMargin = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnLeftMargin1ToWidthRatio);
                    landingBtn.setLayoutParams(lp);
                }
                return true;
            }
        });


        controlBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) controlBtn.getLayoutParams();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(currentMode == Mode.SETUP)
                        return false;

                    if(rightControl == RightControl.GIMBAL_CONTROLLER)
                        rightControl = RightControl.YAW_CONTROLLER;

                    else {

                        rightControl = RightControl.GIMBAL_CONTROLLER;
                        cameratype = Cameratype.CAMERA_TWO;


                    }
                    UpdateView();
                    int prevWidth = lp.width;
                    lp.height = (int)((double)GlobalData.screenWidth*(double) Def.rightMenuMaxHeightToWidthRatio);
                    lp.width = (int)((double)GlobalData.screenWidth*(double) Def.rightMenuMaxHeightToWidthRatio);

                    lp.rightMargin = lp.rightMargin - (lp.width-prevWidth)/2;

                    controlBtn.setLayoutParams(lp);
                    UpdateView();

                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    lp.height = (int)((double)GlobalData.screenWidth*(double) Def.rightMenuMinHeightToWidthRatio);
                    lp.width = (int)((double)GlobalData.screenWidth*(double) Def.rightMenuMinHeightToWidthRatio);
                    lp.rightMargin = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnRightMargin2ToWidthRatio);

                    controlBtn.setLayoutParams(lp);
                }
                return true;
            }
        });


        drGimBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) drGimBtn.getLayoutParams();
                Bitmap image;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(currentMode == Mode.SETUP)
                        return false;

                    if(cameratype == Cameratype.CAMERA_ONE) {
                        cameratype = Cameratype.CAMERA_TWO;
                        //image = BitmapFactory.decodeResource(getResources(), R.drawable.camera_2);
                    }
                    else {
                        cameratype = Cameratype.CAMERA_ONE;
                        //image = BitmapFactory.decodeResource(getResources(), R.drawable.camera_1);
                    }

                    UpdateView();
                    int prevWidth = lp.width;
                    lp.height = (int)((double)GlobalData.screenWidth*(double) Def.rightMenuMaxHeightToWidthRatio);
                    lp.width = (int)((double)GlobalData.screenWidth*(double) Def.rightMenuMaxHeightToWidthRatio);

                    lp.rightMargin = lp.rightMargin - (lp.width-prevWidth)/2;

                    drGimBtn.setLayoutParams(lp);




                    UpdateView();

                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    lp.height = (int)((double)GlobalData.screenWidth*(double) Def.rightMenuMinHeightToWidthRatio);
                    lp.width = (int)((double)GlobalData.screenWidth*(double) Def.rightMenuMinHeightToWidthRatio);
                    lp.rightMargin = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnRightMargin1ToWidthRatio);

                    drGimBtn.setLayoutParams(lp);
                }
                return true;
            }
        });

        cameraBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) cameraBtn.getLayoutParams();
                Bitmap image;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(currentMode == Mode.SETUP)
                        return false;

                    if(recordtype == Recordtype.STILL_CUT) {
                        videoMode = VideoMode.PLAY;
                        cameraClick.setBackgroundResource(R.drawable.icon_video_button);
                        cameraBtn.setBackgroundResource(R.drawable.video_cam);
                        recordtype = Recordtype.VIDEO_CLIP;

                    }


                    else {
                        cameraClick.setBackgroundResource(R.drawable.icon_camera_button);
                        recordtype = Recordtype.STILL_CUT;
                        cameraBtn.setBackgroundResource(R.drawable.still_cam);

                    }

                    //UpdateView();
                    int prevWidth = lp.width;
                    lp.height = (int)((double)GlobalData.screenWidth*(double) Def.rightMenuMaxHeightToWidthRatio);
                    lp.width = (int)((double)GlobalData.screenWidth*(double) Def.rightMenuMaxHeightToWidthRatio);

                    lp.rightMargin = lp.rightMargin - (lp.width-prevWidth)/2;

                    cameraBtn.setLayoutParams(lp);



                    
                    UpdateView();

                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    lp.height = (int)((double)GlobalData.screenWidth*(double) Def.rightMenuMinHeightToWidthRatio);
                    lp.width = (int)((double)GlobalData.screenWidth*(double) Def.rightMenuMinHeightToWidthRatio);
                    lp.rightMargin = (int)((double)GlobalData.screenWidth*(double) Def.menuBtnRightMargin0ToWidthRatio);

                    cameraBtn.setLayoutParams(lp);
                }
                return true;
            }
        });




        onGimbalListener = new OnGimbalListener();
        gestureDetector=new GestureDetector(this,onGimbalListener);


        gimbalController.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                gestureDetector.onTouchEvent(motionEvent);

                return true;
            }
        });








        final ImageButton cancelButton = (ImageButton) findViewById(R.id.cancel_or_try);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                if (mMpegPlayer != null)
                    mMpegPlayer.stop();
                Log.e(TAG, "Cancel clicked");
                */
                if (currentMode == Mode.SETUP)
                    return;

                connectionCanceled = true;

                //findViewById(R.id.connection_progress).setVisibility(View.GONE);

                Log.e(TAG, "Cancel clicked");
            }

        });
/*

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentMode == Mode.SETUP)
                    return;

                if (recordtype == Recordtype.STILL_CUT) {
                    cameraClick.setBackgroundResource(R.drawable.play_button);
                    recordtype = Recordtype.VIDEO_CLIP;
                }
                else {
                    cameraClick.setBackgroundResource(R.drawable.camera_button);
                    recordtype = Recordtype.STILL_CUT;
                }


            }
        });
        */

        cameraClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentMode == Mode.SETUP)
                    return;

                //////////////////// do later ////////////////////////

                if(recordtype == Recordtype.VIDEO_CLIP) {
                    if(videoMode == VideoMode.PLAY) {
                        cameraClick.setBackgroundResource(R.drawable.icon_video_button);
                        videoMode = VideoMode.STOP;
                    }
                    else {
                        cameraClick.setBackgroundResource(R.drawable.icon_save_button);
                        videoMode = VideoMode.PLAY;
                    }
                }

                /////////////////////////////////////////////////////

            }
        });


        topBarSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentMode == Mode.SETUP || currentMode == Mode.SEARCH)
                    return;

                setup.bringToFront();
                setup.setVisibility(View.VISIBLE);

                currentMode = Mode.SETUP;
                joystick.setEnabled(false);
                yawController.setEnabled(false);
                UpdateSetupView();
            }
        });




        //////////////////// setup listeners /////////////////////////


        setupGeneralBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSetup != SetupMode.GENERAL) {
                    currentSetup = SetupMode.GENERAL;
                    UpdateSetupView();
                }

            }
        });

        setupCommBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentSetup != SetupMode.COMM) {
                    currentSetup = SetupMode.COMM;
                    UpdateSetupView();
                }

            }
        });

        setupCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentSetup != SetupMode.CAMERA) {
                    currentSetup = SetupMode.CAMERA;
                    UpdateSetupView();
                }

            }
        });

        setupAutoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentSetup != SetupMode.AUTO_PILOT) {
                    currentSetup = SetupMode.AUTO_PILOT;
                    UpdateSetupView();
                }

            }
        });
        setupEmergencyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentSetup != SetupMode.EMERGENCY) {
                    currentSetup = SetupMode.EMERGENCY;
                    UpdateSetupView();
                }

            }
        });
        setupExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentSetup != SetupMode.EXIT) {
                    currentSetup = SetupMode.EXIT;
                    UpdateSetupView();
                }

            }
        });

        Button setupCancelBtn = (Button) findViewById(R.id.setup_cancel_btn);
        setupCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setup.setVisibility(View.GONE);
                currentMode = Mode.MANUAL;
                joystick.setEnabled(true);
                yawController.setEnabled(true);


            }
        });


        findViewById(R.id.setup_exit_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.setup_exit_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setup.setVisibility(View.GONE);
                currentMode = Mode.MANUAL;
                joystick.setEnabled(true);
                yawController.setEnabled(true);

            }
        });



        //////////////////////////////////////////////////////////////


        for (int i=0; i<2; i++) {
            mapCamBtn[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OnMapCamOrientationChange();
                }
            });
        }

        searchBtn[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



            EnterSearchMode();

                //Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                //startActivity(intent);

            }
        });


        yawControlListener = new YawControlListener();
        yawController.setListener(yawControlListener, this);

        controlStickListener = new ControlStickListener(joystick);
        joystick.setJoystickListener(controlStickListener);

        aniCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(currentMode == Mode.DELIVERY || currentMode == Mode.AUTO_PILOT) {
                    if(aniCancelBtn.getStatus() == Circle.Status.START) {
                        aniCancelBtn.animateNow();
                        packageDisplay.setBackgroundResource((currentMode == Mode.DELIVERY ? R.drawable.joystick_box : R.drawable.joystick_auto));
                        findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_green);
                        findViewById(R.id.top_bar_status).setBackgroundResource((currentMode == Mode.DELIVERY ? R.drawable.top_bar_status_delivery : R.drawable.top_bar_status_auto_pilot));
                    }
                    else if (aniCancelBtn.getStatus() == Circle.Status.CANCEL) {
                        currentMode = Mode.MANUAL;
                        rightControl = RightControl.YAW_CONTROLLER;
                        if(mqttStatus != ConnectionStatus.CONNECTED || cameraStatus != ConnectionStatus.CONNECTED) {
                            findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_red);
                            findViewById(R.id.top_bar_status).setBackgroundResource(R.drawable.top_bar_status_comm_lost);
                        }
                        else {
                            findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_green);
                            findViewById(R.id.top_bar_status).setBackgroundResource(R.drawable.top_bar_status_manual);
                        }
                        UpdateView();
                    }

                    return;
                }

                if(rightControl != RightControl.YAW_CONTROLLER) {
                    rightControl = RightControl.YAW_CONTROLLER;
                    UpdateView();
                    Log.e(TAG, "aniCancelBtn.onClick()");
                }

            }
        });


        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(locationHandler != null) {
                    String str = searchText.getText().toString();
                    if (str != null && !str.equals(""))
                        locationHandler.Search(str);
                }

            }
        });

        searchCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText.setText("");
            }
        });

        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationHandler != null)
                    locationHandler.centerMyLation();
            }
        });


        deliveryGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String weightStr = packageWeight.getText().toString();
                if(weightStr.equals(""))
                    return;

                Float wt = Float.parseFloat(weightStr);
                if(wt < 0.001)
                    return;


                currentMode = Mode.DELIVERY;
                LeaveSearchMode();
                UpdateView();

            }
        });

        autoGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String speedStr = autoSpeed.getText().toString();
                if(speedStr.equals(""))
                    return;

                Float spd = Float.parseFloat(speedStr);
                if(spd < 0.001)
                    return;


                currentMode = Mode.AUTO_PILOT;
                LeaveSearchMode();
                UpdateView();

            }
        });


        searchExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationHandler.getKeyboardStatus() == LocationHandler.KeyboardStatus.KEYBOARD_OPEN) {
                    locationHandler.closeKeyboard();
                    return;
                }

                currentMode = Mode.MANUAL;
                LeaveSearchMode();
                //currentMode = Mode.MANUAL;
                //UpdateView();
            }
        });

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(locationHandler != null) {
                        String str = searchText.getText().toString();
                        if (str != null && !str.equals(""))
                            locationHandler.Search(str);
                    }

                    return true;
                }
                return false;
            }
        });

        //////////////////////// listeners /////////////////////



        UpdateView();




        ////////////// Resizing Windows ///////////////

        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Remove it here unless you want to get this callback for EVERY
                //layout pass, which can get you into infinite loops if you ever
                //modify the layout from within this method.
                content.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Log.e(TAG, "Content Size is "+content.getWidth() + "x" + content.getHeight());

                double height = content.getHeight();
                double width = content.getWidth();

                GlobalData.screenHeight = content.getHeight();
                GlobalData.screenWidth = content.getWidth();



                RelativeLayout.LayoutParams relParams;
                LinearLayout.LayoutParams linParams;

                    //////////////////////// top bar resizing ////////////////////////////
                int menuWidth= (int)(width*(double) Def.topBarMenuWidthToWidthRatio);
                int VSwidth= (int)(width*(double) Def.topBarVSwidthToWidthRatio);
                int HSwidth= (int)(width*(double) Def.topBarHSwidthToWidthRatio);
                int Hwidth= (int)(width*(double) Def.topBarHwidthToWidthRatio);

                int statusWidth= (int)(width*(double) Def.topBarStatusWidthToWidthRatio);
                int line1Width= (int)(width*(double) Def.topBarLineThicknessToWidthRatio);
                int LTEwidth= (int)(width*(double) Def.topBarLTEwidthToWidthRatio);
                int GPSwidth= (int)(width*(double) Def.topBarGPSwidthToWidthRatio);
                int batteryWdith= (int)(width*(double) Def.topBarBatteryWidthtoWidth);
                int batteryTextWdith= (int)(width*(double) Def.topBarBatteryTextWidthtoWidth);
                int setupWidth= (int)(width*(double) Def.topBarSetupWidthToWidthRatio);
                int line2Width, line3Width, line4Width;
                line2Width = line3Width = line4Width = line1Width;
                int tinyGap= (int)(width*(double) Def.topBarTinyGapToWidthRatio);
                int smallGap= (int)(width*(double) Def.topBarSmallGapToWidthRatio);

                int topBarHeight = (int)(height*(double) Def.topBarHeightToHeightRatio);
                int topBarLineHeightGap = (int)(width*(double) Def.topBarLineHeightGapToWidthRatio);

                int topBarCommonHeight = (int)(width*(double) Def.topBarCommonHeihgtToWidthRatio);
                int topBarHheight = (int)(width*(double) Def.topBarHheightToWidthRatio);

                int topBarBatteryTextHeight = (int)(width*(double) Def.topBarBatteryTextHeightToWidthRatio);


                RelativeLayout appBar = (RelativeLayout) findViewById(R.id.app_bar);
                linParams = (LinearLayout.LayoutParams) appBar.getLayoutParams();
                linParams.height = topBarHeight;
                appBar.setLayoutParams(linParams);



                /*
                    top_bar_menu -> 52 x 46
                */



                int margin = tinyGap;
                relParams = (RelativeLayout.LayoutParams) topBarMenuBtn.getLayoutParams();
                relParams.width = menuWidth;
                relParams.height = (int)(width*(double) Def.topBarMenuWidthToWidthRatio*(46.0/52.0));//topBarCommonHeight;
                relParams.leftMargin=margin;
                topBarMenuBtn.setLayoutParams(relParams);

                margin += menuWidth;
                margin += tinyGap;

                ImageView topBarLine1 = (ImageView) findViewById(R.id.top_bar_line1);
                relParams = (RelativeLayout.LayoutParams) topBarLine1.getLayoutParams();
                relParams.width = line1Width;
                relParams.height = topBarHeight-topBarLineHeightGap;
                relParams.leftMargin=margin;
                topBarLine1.setLayoutParams(relParams);

                margin += line1Width;
                margin += smallGap;

                /*
                    top_bar_vs -> 244 x 35
                */

                relParams = (RelativeLayout.LayoutParams) topBarVS.getLayoutParams();
                relParams.width = VSwidth;
                relParams.height = (int)(width*(double) Def.topBarVSwidthToWidthRatio*(35.0 / 244.0));//topBarCommonHeight;
                relParams.leftMargin=margin;
                topBarVS.setLayoutParams(relParams);

                margin += VSwidth;
                margin += smallGap;


                /*
                    top_bar_hs -> 245 x 35
                */

                relParams = (RelativeLayout.LayoutParams) topBarHS.getLayoutParams();
                relParams.width = HSwidth;
                relParams.height = (int)(width*(double) Def.topBarHSwidthToWidthRatio*(35.0 / 245.0));//topBarCommonHeight;
                relParams.leftMargin=margin;
                topBarHS.setLayoutParams(relParams);

                margin += HSwidth;
                margin += smallGap;

                /*
                    top_bar_h -> 167 x 28
                */

                relParams = (RelativeLayout.LayoutParams) topBarH.getLayoutParams();
                relParams.width = Hwidth;
                relParams.height = (int)(width*(double) Def.topBarHwidthToWidthRatio*(28.0 / 167.0));//topBarHheight;
                relParams.leftMargin=margin;
                topBarH.setLayoutParams(relParams);



                margin = tinyGap;

                /*
                    top_bar_set_icon -> 71 x 69
                */


                relParams = (RelativeLayout.LayoutParams) topBarSetupBtn.getLayoutParams();
                relParams.width = setupWidth;
                relParams.height = (int)(width*(double) Def.topBarSetupWidthToWidthRatio*(69.0/71.0));//topBarCommonHeight;
                relParams.rightMargin=margin;
                topBarSetupBtn.setLayoutParams(relParams);

                margin += setupWidth;
                margin += tinyGap;

                ImageView topBarLine4 = (ImageView) findViewById(R.id.top_bar_line4);
                relParams = (RelativeLayout.LayoutParams) topBarLine4.getLayoutParams();
                relParams.width = line4Width;
                relParams.height = topBarHeight-topBarLineHeightGap;
                relParams.rightMargin=margin;
                topBarLine4.setLayoutParams(relParams);

                margin += line1Width;
                margin += smallGap;

                /*
                    text_55percent -> 78 x 30
                */

                relParams = (RelativeLayout.LayoutParams) topBarBatteryText.getLayoutParams();
                relParams.width = batteryTextWdith;
                relParams.height = (int)(width*(double) Def.topBarBatteryTextWidthtoWidth*(30.0/78.0));//topBarBatteryTextHeight;
                relParams.rightMargin=margin;
                topBarBatteryText.setLayoutParams(relParams);

                margin += batteryTextWdith;
                margin += tinyGap;

                /*
                    battery_icon_yeallow -> 336 x 168
                */


                relParams = (RelativeLayout.LayoutParams) topBarBattery.getLayoutParams();
                relParams.width = batteryWdith;
                relParams.height = (int)(width*(double) Def.topBarBatteryWidthtoWidth*(168.0 / 336.0));//topBarCommonHeight;
                relParams.rightMargin=margin;
                topBarBattery.setLayoutParams(relParams);

                margin += batteryWdith;
                margin += smallGap;

                ImageView topBarLine3 = (ImageView) findViewById(R.id.top_bar_line3);
                relParams = (RelativeLayout.LayoutParams) topBarLine3.getLayoutParams();
                relParams.width = line3Width;
                relParams.height = topBarHeight-topBarLineHeightGap;
                relParams.rightMargin=margin;
                topBarLine3.setLayoutParams(relParams);

                margin += line3Width;
                margin += smallGap;

                /*
                    top_bar_gps -> 181 x 62
                */
                relParams = (RelativeLayout.LayoutParams) topBarGPS.getLayoutParams();
                relParams.width = GPSwidth;
                relParams.height = (int)(width*(double) Def.topBarGPSwidthToWidthRatio*(62.0 / 181.0));//topBarCommonHeight;
                relParams.rightMargin=margin;
                topBarGPS.setLayoutParams(relParams);

                margin += GPSwidth;
                margin += smallGap;

                ImageView topBarLine2 = (ImageView) findViewById(R.id.top_bar_line2);
                relParams = (RelativeLayout.LayoutParams) topBarLine2.getLayoutParams();
                relParams.width = line2Width;
                relParams.height = topBarHeight-topBarLineHeightGap;
                relParams.rightMargin=margin;
                topBarLine2.setLayoutParams(relParams);

                margin += line2Width;
                margin += smallGap;

                /*
                    top_bar_lte -> 190 x 56
                */


                relParams = (RelativeLayout.LayoutParams) topBarLTE.getLayoutParams();
                relParams.width = LTEwidth;
                relParams.height = (int)(width*(double) Def.topBarLTEwidthToWidthRatio*(56.0 / 190.0));//topBarCommonHeight;
                relParams.rightMargin=margin;
                topBarLTE.setLayoutParams(relParams);

                margin += LTEwidth;
                margin += smallGap;

                /*
                    top_bar_status -> 629 x 107
                */
                int topBarStatusWidth = (int) (width/2-margin)*2;

                RelativeLayout topBarStatusLayout = (RelativeLayout) findViewById(R.id.top_bar_status_layout);
                relParams = (RelativeLayout.LayoutParams) topBarStatusLayout.getLayoutParams();
                relParams.width = topBarStatusWidth;
                relParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                topBarStatusLayout.setLayoutParams(relParams);


                relParams = (RelativeLayout.LayoutParams) topBarStatus.getLayoutParams();

                relParams.width = topBarStatusWidth;// (int)(width*(double) Def.topBarHeightToWidthRatio*(629.0/107.0));;

                relParams.height = (int) ((double)topBarStatusWidth * (107.0 / 629.0));

                topBarStatus.setLayoutParams(relParams);

                ///////////////////////////// top bar resizing ////////////////////////////

                relParams = (RelativeLayout.LayoutParams) joystick.getLayoutParams();
                final int joystickWidth = relParams.width =  (int) ((double) height*((63.2-24.1)/100.0));
                        //(int)(width*(double) Def.joyStictDiaToWidthRatio);
                relParams.height = relParams.width;
                final int joystickBottom = relParams.bottomMargin = (int) ((double) height*(24.1/100.0));

                        //(int)(height*(double) Def.joystickLowerMarginToHeightRatio);
                joystick.setLayoutParams(relParams);

                relParams = (RelativeLayout.LayoutParams) packageDisplay.getLayoutParams();
                relParams.width = joystickWidth;//(int)(width*(double) Def.joyStictDiaToWidthRatio);
                relParams.height = relParams.width;
                relParams.bottomMargin = joystickBottom;//(int)(height*(double) Def.joystickLowerMarginToHeightRatio);
                packageDisplay.setLayoutParams(relParams);

                GlobalData.deliveryIconWidth = (int)((double) height*((63.2-24.1)/100.0) * Def.packageIconWidthToDisplayWidthRatio);

                        //(int) (width*(double) Def.joyStictDiaToWidthRatio*Def.packageIconWidthToDisplayWidthRatio);



                Button joystickBtn = (Button) findViewById(R.id.joystick_button);
                FrameLayout.LayoutParams relParamsBtn = (FrameLayout.LayoutParams) joystickBtn.getLayoutParams();
                relParamsBtn.width = (int)(width*(double) Def.joyStictBtnDiaToWidthRatio);
                relParamsBtn.height = relParamsBtn.width;
                joystickBtn.setLayoutParams(relParamsBtn);


                /*  right_stick_back.png -> 566x566
                    right_stick_back.png ->393x22
                 */

                relParams = (RelativeLayout.LayoutParams) yawController.getLayoutParams();
                relParams.width = joystickWidth;//(int)(width*(double) Def.yawControllerDiaToWidthRatio);
                final int yawControllerHeight = relParams.height = relParams.width;
                relParams.bottomMargin = joystickBottom;//(int)(height*(double) Def.joystickLowerMarginToHeightRatio);
                yawController.setLayoutParams(relParams);

                Button yawBtn = (Button) findViewById(R.id.yaw_btn);
                relParamsBtn = (FrameLayout.LayoutParams) yawBtn.getLayoutParams();
                relParamsBtn.width = (int)(width*(double) Def.yawBtnDiaToWidthRatio);
                relParamsBtn.height = relParamsBtn.width;
                yawBtn.setLayoutParams(relParamsBtn);

                ImageView yawBar = (ImageView) findViewById(R.id.yaw_bar);
                relParamsBtn = (FrameLayout.LayoutParams) yawBar.getLayoutParams();
                relParamsBtn.height = (int)((double)yawControllerHeight*393.0/566.0);
                relParamsBtn.width = (int)((double)yawControllerHeight*22.0/566.0);

                yawBar.setLayoutParams(relParamsBtn);






                appBar = (RelativeLayout) findViewById(R.id.app_bar);
                linParams = (LinearLayout.LayoutParams) appBar.getLayoutParams();
                GlobalData.topBarHeight = linParams.height;
                GlobalData.menuDisp = 2*GlobalData.topBarHeight;

                relParams = (RelativeLayout.LayoutParams) armDialer.getLayoutParams();
                relParams.width = joystickWidth;//(int)(width*(double) Def.yawControllerDiaToWidthRatio);
                relParams.height = relParams.width;
                relParams.bottomMargin = joystickBottom;//(int)(height*(double) Def.joystickLowerMarginToHeightRatio);
                armDialer.setLayoutParams(relParams);


                relParams = (RelativeLayout.LayoutParams) landingController.getLayoutParams();
                relParams.width = joystickWidth;//(int)(width*(double) Def.yawControllerDiaToWidthRatio);
                relParams.height = relParams.width;
                relParams.bottomMargin = joystickBottom;//(int)(height*(double) Def.joystickLowerMarginToHeightRatio);
                landingController.setLayoutParams(relParams);

                relParams = (RelativeLayout.LayoutParams) gimbalController.getLayoutParams();
                relParams.width = joystickWidth;//(int)(width*(double) Def.yawControllerDiaToWidthRatio);
                relParams.height = relParams.width;
                relParams.bottomMargin = joystickBottom;//(int)(height*(double) Def.joystickLowerMarginToHeightRatio);
                gimbalController.setLayoutParams(relParams);

                relParams = (RelativeLayout.LayoutParams) gimbalOGL.getLayoutParams();
                relParams.width = joystickWidth;//(int)(width*(double) Def.yawControllerDiaToWidthRatio);
                relParams.height = relParams.width;
                relParams.bottomMargin = joystickBottom;//(int)(height*(double) Def.joystickLowerMarginToHeightRatio);
                gimbalOGL.setLayoutParams(relParams);



                relParams = (RelativeLayout.LayoutParams) armBtn.getLayoutParams();
                relParams.width = (int)(width*(double) Def.menuBtnMinHeightToWidthRatio);
                relParams.height = relParams.width;
                relParams.leftMargin = (int)(width*(double) Def.menuBtnLeftMargin0ToWidthRatio);
                armBtn.setLayoutParams(relParams);

                relParams = (RelativeLayout.LayoutParams) landingBtn.getLayoutParams();
                relParams.width = (int)(width*(double) Def.menuBtnMinHeightToWidthRatio);
                relParams.height = relParams.width;
                relParams.leftMargin = (int)(width*(double) Def.menuBtnLeftMargin1ToWidthRatio);
                landingBtn.setLayoutParams(relParams);

                ImageButton emotionBtn = (ImageButton) findViewById(R.id.emotion_btn);
                relParams = (RelativeLayout.LayoutParams) emotionBtn.getLayoutParams();
                relParams.width = (int)(width*(double) Def.menuBtnMinHeightToWidthRatio);
                relParams.height = relParams.width;
                relParams.leftMargin = (int)(width*(double) Def.menuBtnLeftMargin2ToWidthRatio);
                emotionBtn.setLayoutParams(relParams);

                /////////////////////////////////////////////////////////////////////


                relParams = (RelativeLayout.LayoutParams) menuLayout.getLayoutParams();

                // 0.112 comes from the ratio file "Image_Ratio.png"

                relParams.height = (int)(width*(double) Def.menuBtnMaxHeightToWidthRatio);
                int topMargin = (int) ((double) height*0.112) - relParams.height;
                if(topMargin < 0 )
                    topMargin = 0;

                relParams.topMargin = topMargin;
                menuLayout.setLayoutParams(relParams);




                /////////////////////////////////////////////////////////////////////

                relParams = (RelativeLayout.LayoutParams) controlBtn.getLayoutParams();
                relParams.width = (int)(width*(double) Def.rightMenuMinHeightToWidthRatio);
                relParams.height = relParams.width;//(int)(width*(double) Def.rightMenuMinHeightToWidthRatio);
                relParams.rightMargin = (int)(width*(double) Def.menuBtnRightMargin2ToWidthRatio);
                controlBtn.setLayoutParams(relParams);

                relParams = (RelativeLayout.LayoutParams) drGimBtn.getLayoutParams();
                relParams.width = (int)(width*(double) Def.rightMenuMinHeightToWidthRatio);
                relParams.height = relParams.width;//(int)(width*(double) Def.rightMenuMinHeightToWidthRatio);
                relParams.rightMargin = (int)(width*(double) Def.menuBtnRightMargin1ToWidthRatio);
                drGimBtn.setLayoutParams(relParams);

                relParams = (RelativeLayout.LayoutParams) cameraBtn.getLayoutParams();
                relParams.width = (int)(width*(double) Def.rightMenuMinHeightToWidthRatio);
                relParams.height = relParams.width;//(int)(width*(double) Def.rightMenuMinHeightToWidthRatio);
                relParams.rightMargin = (int)(width*(double) Def.menuBtnRightMargin0ToWidthRatio);
                cameraBtn.setLayoutParams(relParams);

                /*
                    Using fiile Image_Ratio.png
                */

                relParams = (RelativeLayout.LayoutParams) cameraClick.getLayoutParams();
                relParams.width = (int)(width*(double) Def.cameraClickWidthToWidthRatio);
                relParams.height = relParams.width;
                final int cameraClickTop = relParams.topMargin = 2*topMargin;


                GlobalData.menuDisp = (int)((double)height*((92.1-67.3)/100.0)) - relParams.height - cameraClickTop;

                if(GlobalData.menuDisp < 0)
                    GlobalData.menuDisp *= (-1);

                        //(int)(width*(double) Def.cameraClickTopMarginToWidthRatio);
                cameraClick.setLayoutParams(relParams);


                robotControl = (ImageButton) findViewById(R.id.robot_control);

                relParams = (RelativeLayout.LayoutParams) robotControl.getLayoutParams();
                relParams.width = (int)(width*(double) Def.robotControlWidthToWidthRatio);
                relParams.height = relParams.width;
                relParams.topMargin = cameraClickTop;//(int)(width*(double) Def.robotControlTopMarginToWidthRatio);
                robotControl.setLayoutParams(relParams);


                    ///////////////////// resizing setup //////////////////////////

                relParams = (RelativeLayout.LayoutParams) setup.getLayoutParams();
                final int  setupDlgWidth = relParams.width = (int)(width*(double) Def.setupWidthToWidthRatio);
                relParams.height = (int)(height*(double) Def.setupHeightToHeightRatio);
                setup.setLayoutParams(relParams);




                margin = (int) (getResources().getDimension(R.dimen.setup_icon_margin));

                int iconWidth = (int)(width*(double) Def.setupIconWidthToWidthRatio);
                int iconHeight = (int)(width*(double) Def.setupIconHeightToWidthRatio);

                final int setupMenuWidth = 6*iconWidth+12*margin;
                final int setupMenuHeight = iconHeight+2*margin;

                Log.e(TAG, "Icon width: " + iconWidth);

                LinearLayout setupMenu = (LinearLayout) findViewById(R.id.setup_menu);
                relParams = (RelativeLayout.LayoutParams) setupMenu.getLayoutParams();
                relParams.width = setupMenuWidth;
                relParams.height = setupMenuHeight;
                setupMenu.setLayoutParams(relParams);

                linParams = (LinearLayout.LayoutParams) setupGeneralBtn.getLayoutParams();
                linParams.width = iconWidth;
                linParams.height = iconHeight;
                setupGeneralBtn.setLayoutParams(linParams);

                linParams = (LinearLayout.LayoutParams) setupCommBtn.getLayoutParams();
                linParams.width = iconWidth;
                linParams.height = iconHeight;
                setupCommBtn.setLayoutParams(linParams);

                linParams = (LinearLayout.LayoutParams) setupCameraBtn.getLayoutParams();
                linParams.width = iconWidth;
                linParams.height = iconHeight;
                setupCameraBtn.setLayoutParams(linParams);

                linParams = (LinearLayout.LayoutParams) setupAutoBtn.getLayoutParams();
                linParams.width = iconWidth;
                linParams.height = iconHeight;
                setupAutoBtn.setLayoutParams(linParams);

                linParams = (LinearLayout.LayoutParams) setupEmergencyBtn.getLayoutParams();
                linParams.width = iconWidth;
                linParams.height = iconHeight;
                setupEmergencyBtn.setLayoutParams(linParams);

                linParams = (LinearLayout.LayoutParams) setupExitBtn.getLayoutParams();
                linParams.width = iconWidth;
                linParams.height = iconHeight;
                setupExitBtn.setLayoutParams(linParams);


                int generalRowOneTopGap = (int)(width*(double) Def.setupRowTopMarginToWidthRatio)
                        + setupMenuHeight;






                RelativeLayout generalRowOne = (RelativeLayout) findViewById(R.id.general_row_one);
                relParams = (RelativeLayout.LayoutParams) generalRowOne.getLayoutParams();
                relParams.width = setupMenuWidth;
                relParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                relParams.topMargin = generalRowOneTopGap;
                generalRowOne.setLayoutParams(relParams);





                ImageView languageLabel = (ImageView) findViewById(R.id.language_label);

                final int languageLableWidth = (int)(width*(double) Def.setupLabelWidthToWidthRatio);

                relParams = (RelativeLayout.LayoutParams) languageLabel.getLayoutParams();
                relParams.width = (int) languageLableWidth;
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = 0;
                languageLabel.setLayoutParams(relParams);




                final int languageBtnWidth = (int) (width*(double) Def.setupButtonWidthToWidthRatio);




                ImageButton languageBtn = (ImageButton) findViewById(R.id.language_btn);

                relParams = (RelativeLayout.LayoutParams) languageBtn.getLayoutParams();
                relParams.width = languageBtnWidth;
                relParams.height = (int)(width*(double) Def.setupButtonHeightToWidthRatio);
                relParams.leftMargin = languageLableWidth + (int)(width*(double) Def.setupLabelGapToWidthRatio);
                languageBtn.setLayoutParams(relParams);






                  /*
                    Image_Gauge_Bar 529 x 63
                    Image_Unit 801 x 63
                */







                ImageView unitLabel = (ImageView) findViewById(R.id.unit_label);

                relParams = (RelativeLayout.LayoutParams) unitLabel.getLayoutParams();
                final int unitLabelWidth = relParams.width = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(801.0/529.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);

                relParams.rightMargin = 0;
                //      + (int)(width*(double) Def.setupLabelGapToWidthRatio);
                unitLabel.setLayoutParams(relParams);

                RelativeLayout unitSwitchLayout = (RelativeLayout) findViewById(R.id.unit_switch_layout);

                relParams = (RelativeLayout.LayoutParams) unitSwitchLayout.getLayoutParams();
                final int generalSwitchLayoutWidth = relParams.width = (int)(width*((double) Def.setupLabelWidthToWidthRatio)*(801.0/529.0)*(137.0/801.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                final int genralSwitchLayoutRight = relParams.rightMargin = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(801.0/529.0)*(47.0/801.0));
                unitSwitchLayout.setLayoutParams(relParams);





                RelativeLayout generalRowTwo = (RelativeLayout) findViewById(R.id.general_row_two);
                relParams = (RelativeLayout.LayoutParams) generalRowTwo.getLayoutParams();
                relParams.width = setupMenuWidth;
                relParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                relParams.topMargin = generalRowOneTopGap
                        + (int)(width*(double) Def.setupRowInnerGapToWidthRatio)
                        + (int)(width*(double) Def.setupLabelHeightToWidthRatio);

                generalRowTwo.setLayoutParams(relParams);

                /*
                    Image_Language -> 530 x 64
                    Image_Battery_Display -> 828 x 63
                */



                ImageView batteryDisplayLabel = (ImageView) findViewById(R.id.battery_display_label);

                relParams = (RelativeLayout.LayoutParams) batteryDisplayLabel.getLayoutParams();
                relParams.width = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(828.0/530.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = 0;
                batteryDisplayLabel.setLayoutParams(relParams);


                final int batteryDispSwitchLeft = (int)(width*(double) Def.setupBatterySwitchLeftMarginToWidthRatio);




                RelativeLayout battDisplaySwitchLayout = (RelativeLayout) findViewById(R.id.batt_disp_switch_layout);

                relParams = (RelativeLayout.LayoutParams) battDisplaySwitchLayout.getLayoutParams();
                relParams.width = generalSwitchLayoutWidth;//(int)(width*((double) Def.setupLabelWidthToWidthRatio)*(828.0/530.0)*(160.0/828.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                final int genralSwitchLayoutLeft = relParams.leftMargin = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(828.0/530.0)*(674.0/828.0))-generalSwitchLayoutWidth/2;
                battDisplaySwitchLayout.setLayoutParams(relParams);


                ImageView gaugeBarLabel = (ImageView) findViewById(R.id.gauge_bar_label);

                relParams = (RelativeLayout.LayoutParams) gaugeBarLabel.getLayoutParams();
                final int gaugeBarWidth = relParams.width = (int)(width*(double) Def.setupLabelWidthToWidthRatio);
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);

                final int gaugeBarRight = relParams.rightMargin = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(801.0/529.0 - 1.0));


                //      + (int)(width*(double) Def.setupLabelGapToWidthRatio);
                gaugeBarLabel.setLayoutParams(relParams);

                RelativeLayout gaugeBarSwitchLayout = (RelativeLayout) findViewById(R.id.gauge_bar_switch_layout);

                relParams = (RelativeLayout.LayoutParams) gaugeBarSwitchLayout.getLayoutParams();
                relParams.width = generalSwitchLayoutWidth;
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.rightMargin = genralSwitchLayoutRight;
                gaugeBarSwitchLayout.setLayoutParams(relParams);


                RelativeLayout generalRowThree = (RelativeLayout) findViewById(R.id.general_row_three);
                relParams = (RelativeLayout.LayoutParams) generalRowThree.getLayoutParams();
                relParams.width = setupMenuWidth;
                relParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                relParams.topMargin = generalRowOneTopGap
                        + (int)(width*(double) Def.setupRowInnerGapToWidthRatio*2.0)
                        + (int)(width*(double) Def.setupLabelHeightToWidthRatio*2.0);

                generalRowThree.setLayoutParams(relParams);


                ImageView mapDispLabel = (ImageView) findViewById(R.id.map_display);


                relParams = (RelativeLayout.LayoutParams) mapDispLabel.getLayoutParams();
                relParams.width = languageLableWidth;
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = 0;
                mapDispLabel.setLayoutParams(relParams);

                RelativeLayout mapDisplaySwitchLayout = (RelativeLayout) findViewById(R.id.map_display_switch_layout);

                relParams = (RelativeLayout.LayoutParams) mapDisplaySwitchLayout.getLayoutParams();
                relParams.width = generalSwitchLayoutWidth;//(int)(width*((double) Def.setupLabelWidthToWidthRatio)*(828.0/530.0)*(160.0/828.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = genralSwitchLayoutLeft;
                mapDisplaySwitchLayout.setLayoutParams(relParams);


                ImageView homeLabel = (ImageView) findViewById(R.id.home_label);

                relParams = (RelativeLayout.LayoutParams) homeLabel.getLayoutParams();
                relParams.width = (int)(width*(double) Def.setupLabelWidthToWidthRatio);
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);

                relParams.rightMargin = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(801.0/529.0 - 1.0));


                //      + (int)(width*(double) Def.setupLabelGapToWidthRatio);
                homeLabel.setLayoutParams(relParams);

                RelativeLayout homeSwitchLayout = (RelativeLayout) findViewById(R.id.home_switch_layout);

                relParams = (RelativeLayout.LayoutParams) homeSwitchLayout.getLayoutParams();
                relParams.width = generalSwitchLayoutWidth;
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.rightMargin = genralSwitchLayoutRight;
                homeSwitchLayout.setLayoutParams(relParams);


                RelativeLayout setupCommunication = (RelativeLayout) findViewById(R.id.setup_communication);
                relParams = (RelativeLayout.LayoutParams) setupCommunication.getLayoutParams();
                relParams.width = setupMenuWidth;
                relParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                relParams.topMargin = //generalRowOneTopGap;

                generalRowOneTopGap
                        + (int)(width*(double) Def.setupRowInnerGapToWidthRatio)
                        + (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                setupCommunication.setLayoutParams(relParams);

                ImageView commLabel = (ImageView) findViewById(R.id.comm_label);

                relParams = (RelativeLayout.LayoutParams) commLabel.getLayoutParams();
                relParams.width = languageLableWidth;
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = 0;
                commLabel.setLayoutParams(relParams);



                ImageButton commBtn = (ImageButton) findViewById(R.id.comm_btn);

                relParams = (RelativeLayout.LayoutParams) commBtn.getLayoutParams();
                relParams.width = languageBtnWidth;
                relParams.height = (int)(width*(double) Def.setupButtonHeightToWidthRatio);
                relParams.leftMargin = languageLableWidth
                        + (int)(width*(double) Def.setupLabelGapToWidthRatio);
                commBtn.setLayoutParams(relParams);

                ImageView gpsLabel = (ImageView) findViewById(R.id.gps_label);

                relParams = (RelativeLayout.LayoutParams) gpsLabel.getLayoutParams();
                relParams.width = (int)(width*(double) Def.setupLabelWidthToWidthRatio);
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);

                relParams.rightMargin = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(801.0/529.0 - 1.0));


                //      + (int)(width*(double) Def.setupLabelGapToWidthRatio);
                gpsLabel.setLayoutParams(relParams);

                RelativeLayout gpsSwitchLayout = (RelativeLayout) findViewById(R.id.gps_switch_layout);

                relParams = (RelativeLayout.LayoutParams) gpsSwitchLayout.getLayoutParams();
                relParams.width = generalSwitchLayoutWidth;
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.rightMargin = genralSwitchLayoutRight;
                gpsSwitchLayout.setLayoutParams(relParams);


                RelativeLayout cameraRowOne = (RelativeLayout) findViewById(R.id.camera_row_one);
                relParams = (RelativeLayout.LayoutParams) cameraRowOne.getLayoutParams();
                relParams.width = setupMenuWidth;
                relParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                final int cameraRowOneTopMargin = relParams.topMargin = generalRowOneTopGap
                        + (int)(width*(double) Def.setupRowInnerGapToWidthRatio/2.0)
                        + (int)(width*(double) Def.setupLabelHeightToWidthRatio/2.0);

                cameraRowOne.setLayoutParams(relParams);



                ImageView fixedCamLabel = (ImageView) findViewById(R.id.fixed_cam_label);


                relParams = (RelativeLayout.LayoutParams) fixedCamLabel.getLayoutParams();
                relParams.width = languageLableWidth;
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = 0;
                fixedCamLabel.setLayoutParams(relParams);


                final int imgSizeLableWidth = (int)(width*(double) Def.setupLabelWidthToWidthRatio*1.05*1.482);


                final int fixedCamSwitchWidth = (int)((double) imgSizeLableWidth * (150.0/784.0) * (2.2/2.6));


                com.rm.rmswitch.RMSwitch fixedCamSwitch = (com.rm.rmswitch.RMSwitch) findViewById(R.id.fixed_cam_switch);

                relParams = (RelativeLayout.LayoutParams) fixedCamSwitch.getLayoutParams();

                relParams.width = (int)((double) imgSizeLableWidth * (150.0/784.0) * (2.2/2.6));
                relParams.leftMargin = languageLableWidth
                        + (int)(width*(double) Def.setupLabelGapToWidthRatio);
                fixedCamSwitch.setLayoutParams(relParams);

                ImageView gimbalCamLabel = (ImageView) findViewById(R.id.gimbal_cam_label);


                relParams = (RelativeLayout.LayoutParams) gimbalCamLabel.getLayoutParams();
                relParams.width = gaugeBarWidth;
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.rightMargin = gaugeBarRight;
                gimbalCamLabel.setLayoutParams(relParams);


                /*

                image_image_size -> 784x102
                image_gimbal_camera -> 529x63

                1.05 comes from previous fixing

                */




                RelativeLayout gimbalLayout = (RelativeLayout) findViewById(R.id.gimbal_layout);

                relParams = (RelativeLayout.LayoutParams) gimbalLayout.getLayoutParams();
                //relParams.width = (int)(width*(double) Def.setupButtonWidthToWidthRatio);
                relParams.width = (int)((double) imgSizeLableWidth * (150.0/784.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.rightMargin = 0;//(int)(width*(double) Def.setupBatterySwitchLeftMarginToWidthRatio);
                gimbalLayout.setLayoutParams(relParams);

                /*
                image_image_size -> 784x102
                image_gimbal_camera -> 529x63

                1.05 comes from previous fixing

                SWITCH_STANDARD_ASPECT_RATIO = 2.2f  for two state
                SWITCH_STANDARD_ASPECT_RATIO = 2.6f  for three state
                */




                com.rm.rmswitch.RMSwitch gimbalCamSwitch = (com.rm.rmswitch.RMSwitch) findViewById(R.id.gimbal_cam_switch);

                relParams = (RelativeLayout.LayoutParams) gimbalCamSwitch.getLayoutParams();
                relParams.width = (int)((double) imgSizeLableWidth * (150.0/784.0) * (2.2/2.6));
                relParams.rightMargin = 0;
                gimbalCamSwitch.setLayoutParams(relParams);


                RelativeLayout cameraRowTwo = (RelativeLayout) findViewById(R.id.camera_row_two);
                relParams = (RelativeLayout.LayoutParams) cameraRowTwo.getLayoutParams();
                relParams.width = setupMenuWidth;
                relParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                relParams.topMargin = cameraRowOneTopMargin
                        + (int)(width*(double) Def.setupRowInnerGapToWidthRatio*1.0)
                        + (int)(width*(double) Def.setupLabelHeightToWidthRatio*1.0);
                cameraRowTwo.setLayoutParams(relParams);

                ImageView storageLabel = (ImageView) findViewById(R.id.storage_label);

                relParams = (RelativeLayout.LayoutParams) storageLabel.getLayoutParams();
                relParams.width = languageLableWidth;
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = 0;
                storageLabel.setLayoutParams(relParams);



                ImageButton storageBtn = (ImageButton) findViewById(R.id.storage_btn);

                relParams = (RelativeLayout.LayoutParams) storageBtn.getLayoutParams();
                relParams.width = languageBtnWidth;
                relParams.height = (int)(width*(double) Def.setupButtonHeightToWidthRatio);
                final int storageBtnLeft = relParams.leftMargin = languageLableWidth
                        + (int)(width*(double) Def.setupLabelGapToWidthRatio);
                storageBtn.setLayoutParams(relParams);


                ImageView imgSizeLabel = (ImageView) findViewById(R.id.img_size_label);

                relParams = (RelativeLayout.LayoutParams) imgSizeLabel.getLayoutParams();
                relParams.width = imgSizeLableWidth;
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio*1.62);
                relParams.leftMargin = setupMenuWidth - gaugeBarRight - gaugeBarWidth;
                imgSizeLabel.setLayoutParams(relParams);


                com.rm.rmswitch.RMTristateSwitch imgSizeSwitch = (com.rm.rmswitch.RMTristateSwitch) findViewById(R.id.img_size_switch);

                relParams = (RelativeLayout.LayoutParams) imgSizeSwitch.getLayoutParams();
                relParams.width = (int)((double) imgSizeLableWidth * 150.0/784.0);
                relParams.rightMargin = 0;
                imgSizeSwitch.setLayoutParams(relParams);



                RelativeLayout autoPilotRowOne = (RelativeLayout) findViewById(R.id.auto_pilot_row_one);
                relParams = (RelativeLayout.LayoutParams) autoPilotRowOne.getLayoutParams();
                relParams.width = setupMenuWidth;
                relParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                relParams.topMargin = generalRowOneTopGap;
                autoPilotRowOne.setLayoutParams(relParams);

                ImageView altitudeLabel = (ImageView) findViewById(R.id.altitude_label);

                relParams = (RelativeLayout.LayoutParams) altitudeLabel.getLayoutParams();
                relParams.width = languageLableWidth;
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = 0;
                altitudeLabel.setLayoutParams(relParams);



                /*
                    Auto pilot edit aspect ratio 210/35
                */

                EditText editAltitudeFrom = (EditText) findViewById(R.id.edit_altitude_from);

                relParams = (RelativeLayout.LayoutParams) editAltitudeFrom.getLayoutParams();
                final int editAltitudeHeight = relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                final int editAltitudeWidth = relParams.width = ((int)(width*(double) Def.setupLabelHeightToWidthRatio*0.75*(210.0/35.0)));
                relParams.leftMargin = storageBtnLeft;
                editAltitudeFrom.setLayoutParams(relParams);


                /*
                    wave aspect ratio 22/9
                */

                ImageView altitudeWave = (ImageView) findViewById(R.id.altitude_wave);

                relParams = (RelativeLayout.LayoutParams) altitudeWave.getLayoutParams();
                final double widthInDouble = (width*(double) Def.setupLabelGapToWidthRatio*0.75);
                relParams.width = (int)(widthInDouble);
                relParams.height = (int)(widthInDouble*(9.0/22.0));
                altitudeWave.setLayoutParams(relParams);


                EditText editAltitudeTo = (EditText) findViewById(R.id.edit_altitude_to);

                relParams = (RelativeLayout.LayoutParams) editAltitudeTo.getLayoutParams();
                relParams.height = editAltitudeHeight;
                relParams.width = editAltitudeWidth;
                editAltitudeTo.setLayoutParams(relParams);

                RelativeLayout autoPilotRowTwo = (RelativeLayout) findViewById(R.id.auto_pilot_row_two);
                relParams = (RelativeLayout.LayoutParams) autoPilotRowTwo.getLayoutParams();
                relParams.width = setupMenuWidth;
                relParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                relParams.topMargin = generalRowOneTopGap
                        + (int)(width*(double) Def.setupRowInnerGapToWidthRatio)
                        + (int)(width*(double) Def.setupLabelHeightToWidthRatio);

                autoPilotRowTwo.setLayoutParams(relParams);


                /*
                    Image_Altitude_Range 530 x 64
                    Image_Measure 899 x 64
                */



                ImageView measureLabel = (ImageView) findViewById(R.id.measure_label);

                relParams = (RelativeLayout.LayoutParams) measureLabel.getLayoutParams();
                relParams.width = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(899.0/530.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = 0;
                measureLabel.setLayoutParams(relParams);



                RelativeLayout measureSwitchLayout = (RelativeLayout) findViewById(R.id.measure_switch_layout);

                relParams = (RelativeLayout.LayoutParams) measureSwitchLayout.getLayoutParams();
                relParams.width = generalSwitchLayoutWidth;//(int)(width*((double) Def.setupLabelWidthToWidthRatio)*(828.0/530.0)*(160.0/828.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(899.0/530.0)*(750.0/899.0))-generalSwitchLayoutWidth/2;
                measureSwitchLayout.setLayoutParams(relParams);


                RelativeLayout autoPilotRowThree = (RelativeLayout) findViewById(R.id.auto_pilot_row_three);
                relParams = (RelativeLayout.LayoutParams) autoPilotRowThree.getLayoutParams();
                relParams.width = setupMenuWidth;
                relParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                relParams.topMargin = generalRowOneTopGap
                        + (int)(width*(double) Def.setupRowInnerGapToWidthRatio*2.0)
                        + (int)(width*(double) Def.setupLabelHeightToWidthRatio*2.0);

                autoPilotRowThree.setLayoutParams(relParams);

                ImageView timeDispLabel = (ImageView) findViewById(R.id.time_disp_label);

                relParams = (RelativeLayout.LayoutParams) timeDispLabel.getLayoutParams();
                relParams.width = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(899.0/530.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = 0;
                timeDispLabel.setLayoutParams(relParams);



                RelativeLayout timeDispSwitchLayout = (RelativeLayout) findViewById(R.id.time_disp_switch_layout);

                relParams = (RelativeLayout.LayoutParams) timeDispSwitchLayout.getLayoutParams();
                relParams.width = generalSwitchLayoutWidth;//(int)(width*((double) Def.setupLabelWidthToWidthRatio)*(828.0/530.0)*(160.0/828.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(899.0/530.0)*(756.0/899.0))-generalSwitchLayoutWidth/2;
                timeDispSwitchLayout.setLayoutParams(relParams);



                RelativeLayout emergencyRowOne = (RelativeLayout) findViewById(R.id.emergency_row_one);
                relParams = (RelativeLayout.LayoutParams) emergencyRowOne.getLayoutParams();
                relParams.width = setupMenuWidth;
                relParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                relParams.topMargin = cameraRowOneTopMargin;
                emergencyRowOne.setLayoutParams(relParams);

                /*
                    Image_Altitude_Range 530 x 64
                    Image_Communication_Lost 1151 x 64
                */


                ImageView commLostLabel = (ImageView) findViewById(R.id.comm_lost_label);

                relParams = (RelativeLayout.LayoutParams) commLostLabel.getLayoutParams();
                relParams.width = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(1151.0/530.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = 0;
                commLostLabel.setLayoutParams(relParams);



                RelativeLayout commLostSwitchLayout = (RelativeLayout) findViewById(R.id.comm_lost_switch_layout);

                relParams = (RelativeLayout.LayoutParams) commLostSwitchLayout.getLayoutParams();
                relParams.width = generalSwitchLayoutWidth;//(int)(width*((double) Def.setupLabelWidthToWidthRatio)*(828.0/530.0)*(160.0/828.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(1151.0/530.0)*(805.0/1151.0))-generalSwitchLayoutWidth/2;
                commLostSwitchLayout.setLayoutParams(relParams);


                RelativeLayout emergencyRowTwo = (RelativeLayout) findViewById(R.id.emergency_row_two);
                relParams = (RelativeLayout.LayoutParams) emergencyRowTwo.getLayoutParams();
                relParams.width = setupMenuWidth;
                relParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                relParams.topMargin = cameraRowOneTopMargin
                        + (int)(width*(double) Def.setupRowInnerGapToWidthRatio*1.0)
                        + (int)(width*(double) Def.setupLabelHeightToWidthRatio*1.0);

                emergencyRowTwo.setLayoutParams(relParams);


                ImageView lowBattLabel = (ImageView) findViewById(R.id.low_batt_label);

                relParams = (RelativeLayout.LayoutParams) lowBattLabel.getLayoutParams();
                relParams.width = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(1151.0/530.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = 0;
                lowBattLabel.setLayoutParams(relParams);



                RelativeLayout lowBattSwitchLayout = (RelativeLayout) findViewById(R.id.low_batt_switch_layout);

                relParams = (RelativeLayout.LayoutParams) lowBattSwitchLayout.getLayoutParams();
                relParams.width = generalSwitchLayoutWidth;//(int)(width*((double) Def.setupLabelWidthToWidthRatio)*(828.0/530.0)*(160.0/828.0));
                relParams.height = (int)(width*(double) Def.setupLabelHeightToWidthRatio);
                relParams.leftMargin = (int)(width*(double) Def.setupLabelWidthToWidthRatio*(1151.0/530.0)*(805.0/1151.0))-generalSwitchLayoutWidth/2;
                lowBattSwitchLayout.setLayoutParams(relParams);


                ImageView setupExitMsg = (ImageView) findViewById(R.id.setup_exit_msg);

                relParams = (RelativeLayout.LayoutParams) setupExitMsg.getLayoutParams();
                relParams.width = (int)((double) setupDlgWidth*(450.0/1000.0));
                relParams.height = (int)((double) setupDlgWidth*(450.0/1000.0)*(58.0/843.0));
                relParams.topMargin = (int)((double) setupDlgWidth*((330.0-130.0)/1000.0));
                setupExitMsg.setLayoutParams(relParams);


                LinearLayout exitBtnLayout = (LinearLayout) findViewById(R.id.exit_button_layout);

                relParams = (RelativeLayout.LayoutParams) exitBtnLayout.getLayoutParams();
                relParams.bottomMargin = (int)((double) setupDlgWidth*((116.0 - 58.0)/1000.0));
                exitBtnLayout.setLayoutParams(relParams);



                ImageButton setupExitYes = (ImageButton) findViewById(R.id.setup_exit_yes);

                linParams = (LinearLayout.LayoutParams) setupExitYes.getLayoutParams();
                linParams.width = (int)((double) setupDlgWidth*(70.0/1000.0));
                linParams.height = (int)((double) setupDlgWidth*(70.0/1000.0)*(113.0/142.0));
                final int exit_margin = (int)((double) setupDlgWidth*(58.0/1000.0));
                linParams.topMargin = linParams.bottomMargin = linParams.leftMargin = linParams.rightMargin = exit_margin;

                setupExitYes.setLayoutParams(linParams);


                ImageButton setupExitNo = (ImageButton) findViewById(R.id.setup_exit_no);

                linParams = (LinearLayout.LayoutParams) setupExitNo.getLayoutParams();
                linParams.width = (int)((double) setupDlgWidth*(70.0/1000.0));
                linParams.height = (int)((double) setupDlgWidth*(70.0/1000.0)*(113.0/142.0));
                linParams.topMargin = linParams.bottomMargin = linParams.leftMargin = linParams.rightMargin = exit_margin;
                setupExitNo.setLayoutParams(linParams);






                /////////////////////////// resizing setup ends ///////////////////////////


                /*
                    map_layout -> 725 x 400
                */

                RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.map_layout);
                relParams = (RelativeLayout.LayoutParams) mapLayout.getLayoutParams();
                relParams.height = (int) ((double) height * (28.4/100.0));
                relParams.width = (int) ((double) height * (28.4/100.0)*(725.0/400.0));
                mapLayout.setLayoutParams(relParams);

                locationHandler.SetMapOriginalDimension(relParams.width, relParams.height);

                relParams = (RelativeLayout.LayoutParams) aniCancelBtn.getLayoutParams();
                relParams.height = (int)(height*(double) Def.aniCancelBtnDiamToHeihtRatio);
                GlobalData.aniCancelDiameter = relParams.width = relParams.height ;
                GlobalData.aniCancelTopMargin = relParams.topMargin = (int)(height*(double) (Def.aniCancelBtnCenterToHeightRatio-Def.aniCancelBtnDiamToHeihtRatio/2));


                int targetDim = relParams.width;
                final float ratio = (float) 0.074074;
                final int strokeSize = (int) (ratio*targetDim);
                aniCancelBtn.setLayoutParams(relParams);
                aniCancelBtn.Set(targetDim, strokeSize, getResources().getColor(R.color.ani_cancel_gauge_color), MainActivity.this);


                //////////////////  resizing search /////////////////////


                RelativeLayout searchTop = (RelativeLayout) findViewById(R.id.search_top);
                relParams = (RelativeLayout.LayoutParams) searchTop.getLayoutParams();
                relParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                final int searchTopHeight = relParams.height = (int)(width*(double) Def.searchLayoutHeightToWidthRatio);
                searchTop.setLayoutParams(relParams);

                relParams = (RelativeLayout.LayoutParams) searchExit.getLayoutParams();
                final int searchExitWidth = relParams.width = (int)(width*(double) Def.searchExitWidthToWidthRatio);
                relParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                searchExit.setLayoutParams(relParams);


                relParams = (RelativeLayout.LayoutParams) searchIcon.getLayoutParams();
                final int searchIconWidth = relParams.width = (int)(width*(double) Def.searchIconWidthToWidthRatio);
                relParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                searchIcon.setLayoutParams(relParams);


                LinearLayout searchTextLayout = (LinearLayout) findViewById(R.id.search_text_layout);
                relParams = (RelativeLayout.LayoutParams) searchTextLayout.getLayoutParams();
                final int seachEditTextWidth = relParams.width = (int) (width-searchExitWidth-searchIconWidth);
                relParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                relParams.leftMargin = searchExitWidth;
                searchTextLayout.setLayoutParams(relParams);

                linParams = (LinearLayout.LayoutParams) searchCross.getLayoutParams();
                linParams.width = searchTopHeight;
                linParams.height = searchTopHeight;
                searchCross.setLayoutParams(linParams);


                int divInPixels = (int) getResources().getDimension(R.dimen.search_list_div_height);

                int searchItemHeight = (int)(width*(double) Def.searchItemHeightToWidthRatio);

                ListView searchList = (ListView) findViewById(R.id.search_list);
                relParams = (RelativeLayout.LayoutParams) searchList.getLayoutParams();
                GlobalData.searchListItemWidth = relParams.width = seachEditTextWidth;
                relParams.height = (searchItemHeight + divInPixels) * Def.numberOfSearchItems;
                relParams.topMargin = searchTopHeight;
                searchList.setLayoutParams(relParams);


                GlobalData.searchListItemHeight = (int)(GlobalData.screenWidth*(double) Def.searchItemHeightToWidthRatio);

                RelativeLayout searchInput = (RelativeLayout) findViewById(R.id.search_input);
                relParams = (RelativeLayout.LayoutParams) searchInput.getLayoutParams();
                relParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                relParams.height = (int)(width*(double) Def.searchIconBoxHeightToWidthRatio);
                searchInput.setLayoutParams(relParams);


                RelativeLayout searchIconBox = (RelativeLayout) findViewById(R.id.search_icon_box);
                relParams = (RelativeLayout.LayoutParams) searchIconBox.getLayoutParams();
                final int searchIconBoxWidth = relParams.width = (int)(width*(double) Def.searchIconBoxWidthToWidthRatio);
                relParams.height = (int)(width*(double) Def.searchIconBoxHeightToWidthRatio);
                relParams.leftMargin = 0;
                searchIconBox.setLayoutParams(relParams);

                ImageView searchIconDelivery = (ImageView) findViewById(R.id.search_icon_delivery);

                relParams = (RelativeLayout.LayoutParams) searchIconDelivery.getLayoutParams();
                final int searchIconDeliveryWidth = relParams.width = (int)(width*(double) Def.searchIconDeliveryWidthToWidthRatio);
                relParams.height = searchIconDeliveryWidth;
                searchIconDelivery.setLayoutParams(relParams);

                ImageView searchIconAuto = (ImageView) findViewById(R.id.search_icon_auto);

                relParams = (RelativeLayout.LayoutParams) searchIconAuto.getLayoutParams();
                relParams.width = searchIconDeliveryWidth;
                relParams.height = searchIconDeliveryWidth;
                searchIconAuto.setLayoutParams(relParams);




                final int gap = (int)(width*(double) Def.searchIconEditGapToWidthRatio);



                relParams = (RelativeLayout.LayoutParams) packageWeight.getLayoutParams();
                final int packageWeightWidth = relParams.width = (int)(width*(double) Def.searchIconDeliveryEditWidthToWidthRatio);
                relParams.height = searchIconDeliveryWidth;
                relParams.leftMargin = searchIconBoxWidth+gap;
                packageWeight.setLayoutParams(relParams);

                TextView packageUnit= (TextView) findViewById(R.id.package_unit);

                relParams = (RelativeLayout.LayoutParams) packageUnit.getLayoutParams();
                relParams.width = searchIconDeliveryWidth;
                relParams.height = searchIconDeliveryWidth;
                relParams.leftMargin = searchIconBoxWidth+gap+packageWeightWidth;
                packageUnit.setLayoutParams(relParams);




                relParams = (RelativeLayout.LayoutParams) deliveryGo.getLayoutParams();
                relParams.width = searchIconDeliveryWidth;
                relParams.height = searchIconDeliveryWidth;
                relParams.leftMargin = searchIconBoxWidth+gap+packageWeightWidth+searchIconDeliveryWidth+gap;
                deliveryGo.setLayoutParams(relParams);





                relParams = (RelativeLayout.LayoutParams) autoSpeed.getLayoutParams();
                relParams.width = packageWeightWidth;
                relParams.height = searchIconDeliveryWidth;
                relParams.leftMargin = searchIconBoxWidth+gap;
                autoSpeed.setLayoutParams(relParams);

                TextView autoUnit = (TextView) findViewById(R.id.auto_unit);

                relParams = (RelativeLayout.LayoutParams) autoUnit.getLayoutParams();
                relParams.width = searchIconDeliveryWidth;
                relParams.height = searchIconDeliveryWidth;
                relParams.leftMargin = searchIconBoxWidth+gap+packageWeightWidth;
                autoUnit.setLayoutParams(relParams);



                relParams = (RelativeLayout.LayoutParams) autoGo.getLayoutParams();
                relParams.width = searchIconDeliveryWidth;
                relParams.height = searchIconDeliveryWidth;
                relParams.leftMargin = searchIconBoxWidth+gap+packageWeightWidth+searchIconDeliveryWidth+gap;
                autoGo.setLayoutParams(relParams);







                //////////////////  resizing search /////////////////////


            }
        });

        armDialer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // method called more than once, but the values only need to be initialized one time

                if (armDialerHeight == 0 || armDialerWidth == 0) {

                    armDialerHeight = armDialer.getHeight();
                    armDialerWidth = armDialer.getWidth();

                    if (armDialerHeight > 0  && armDialerWidth > 0) {
                        // resize
                        Matrix resize = new Matrix();
                        resize.postScale((float) Def.armDialerRatio*Math.min(armDialerWidth, armDialerHeight) / (float) GlobalData.imageDialerOriginal.getWidth(), Def.armDialerRatio*(float) Math.min(armDialerWidth, armDialerHeight) / (float) GlobalData.imageDialerOriginal.getHeight());
                        GlobalData.imageDialerScaled = Bitmap.createBitmap(GlobalData.imageDialerOriginal, 0, 0, GlobalData.imageDialerOriginal.getWidth(), GlobalData.imageDialerOriginal.getHeight(), resize, false);

                        // translate to the image view's center
                        Log.e(TAG, "Dialer Width " + armDialerWidth + "Dialer Height " + armDialerHeight + " ImageScaled Width " + GlobalData.imageDialerScaled.getWidth() + " ImageScaled Height " + GlobalData.imageDialerScaled.getHeight());
                        float translateX = armDialerWidth / 2 - GlobalData.imageDialerScaled.getWidth() / 2;
                        float translateY = armDialerHeight / 2 - GlobalData.imageDialerScaled.getHeight() / 2;
                        GlobalData.armDialerMatrix.postTranslate(translateX, translateY);



                        armDialer.setImageBitmap(GlobalData.imageDialerScaled);
                        armDialer.setImageMatrix(GlobalData.armDialerMatrix);


                    }
                }





            }
        });

        gimbalController.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // method called more than once, but the values only need to be initialized one time

                if (gimbalControllerHeight == 0 || gimbalControllerWidth == 0) {

                    gimbalControllerHeight = gimbalController.getHeight();
                    gimbalControllerWidth = gimbalController.getWidth();

                    if (gimbalControllerHeight > 0  && gimbalControllerWidth > 0) {

                        Matrix matrix = new Matrix();
                        // resize
                        Matrix resize = new Matrix();
                        resize.postScale((float) Def.gimbalControllerRatio*Math.min(gimbalControllerWidth, gimbalControllerHeight) / (float) GlobalData.imageGimbalOriginal.getWidth(), Def.gimbalControllerRatio*(float) Math.min(gimbalControllerWidth, gimbalControllerHeight) / (float) GlobalData.imageGimbalOriginal.getHeight());
                        GlobalData.imageGimbalScaled = Bitmap.createBitmap(GlobalData.imageGimbalOriginal, 0, 0, GlobalData.imageGimbalOriginal.getWidth(), GlobalData.imageGimbalOriginal.getHeight(), resize, false);

                        // translate to the image view's center
                        Log.e(TAG, "Gimbal Width " + gimbalControllerWidth + "Gimbal Height " + gimbalControllerHeight + " ImageScaled Width " + GlobalData.imageGimbalScaled.getWidth() + " ImageScaled Height " + GlobalData.imageGimbalScaled.getHeight());


                        float translateX = gimbalControllerWidth / 2 - GlobalData.imageGimbalScaled.getWidth() / 2;
                        float translateY = gimbalControllerHeight / 2 - GlobalData.imageGimbalScaled.getHeight() / 2;

                        matrix.postTranslate(translateX, translateY);

                        gimbalController.setImageBitmap(GlobalData.imageGimbalScaled);
                        gimbalController.setImageMatrix(matrix);



                    }
                }




            }
        });

        landingController.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // method called more than once, but the values only need to be initialized one time

                if (landingControllerHeight == 0 || landingControllerWidth == 0) {

                    landingControllerHeight = landingController.getHeight();
                    landingControllerWidth = landingController.getWidth();

                    if (landingControllerHeight > 0  && landingControllerWidth > 0) {
                        // resize
                        Matrix resize = new Matrix();
                        resize.postScale((float) Def.landingControllerRatio*Math.min(landingControllerWidth, landingControllerHeight) / (float) GlobalData.imageLandingOriginal.getWidth(), Def.landingControllerRatio*(float) Math.min(landingControllerWidth, landingControllerHeight) / (float) GlobalData.imageLandingOriginal.getHeight());
                        GlobalData.imageLandingScaled = Bitmap.createBitmap(GlobalData.imageLandingOriginal, 0, 0, GlobalData.imageLandingOriginal.getWidth(), GlobalData.imageLandingOriginal.getHeight(), resize, false);

                        // translate to the image view's center
                        Log.e(TAG, "landingController Width " + landingControllerWidth + "landingController Height " + landingControllerHeight + " ImageScaled Width " + GlobalData.imageLandingScaled.getWidth() + " ImageScaled Height " + GlobalData.imageLandingScaled.getHeight());
                        float translateX = landingControllerWidth / 2 - GlobalData.imageLandingScaled.getWidth() / 2;
                        float translateY = landingControllerHeight / 2 - GlobalData.imageLandingScaled.getHeight() / 2;
                        GlobalData.landingControllerMatrix.postTranslate(translateX, translateY);



                        landingController.setImageBitmap(GlobalData.imageLandingScaled);
                        landingController.setImageMatrix(GlobalData.landingControllerMatrix);


                    }
                }





            }
        });

        ///////////////////////////////////////////////




        cameraLink = getString(R.string.default_camera_link);
        serverAddr = "168.188.40.28";//"58.224.86.126";
        mqttPort = "1883";
        /*
        cameraLink = getIntent().getStringExtra(InfoActivity.CAM_ADDR);
        serverAddr = getIntent().getStringExtra(InfoActivity.SERVER_ADDR);
        mqttPort = getIntent().getStringExtra(InfoActivity.MQTT_PORT);
        */

        brokerURL = "tcp://" + serverAddr+":"+mqttPort; //143.248.204.35:1883";

        cameraLink = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";


        //brokerURL = "tcp://143.248.204.35:1883";

        //Connect();

        String macAddress = getMacAddress(this);


        if (macAddress == null)
            ErrorMsg("Wifi device not found !!!");

        String clientId = macAddress + "-sub";






        try {


            mqttClient = //new MqttClient(BROKER_URL, clientId);
                    new MqttClient(brokerURL, clientId, new MemoryPersistence());

            piDroneSubscribeCallback = new PIdroneSubscribeCallback(this);
            piDroneSubscribeCallback.SetListener(this);
            mqttClient.setCallback(piDroneSubscribeCallback);
            //Log.e(TAG, "Subscriber is now listening to "+topic);


        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "Error");


        }


        cameraStatus = ConnectionStatus.NOT_CONNECTED;
        mqttStatus = ConnectionStatus.NOT_CONNECTED;


        surfaceView = findViewById(R.id.surface_view);

        mMpegPlayer = new VideoPlayer((VideoDisplay) surfaceView, MainActivity.this);


        mMpegPlayer.setListener(this);

        surfaceView.setVisibility(View.VISIBLE);

        ////////////////////////////////////// for testing ////////////////////////////////


        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (piDronePublisher != null)
                    try {
                        piDronePublisher.PublishStopCmd();
                    }
                    catch (MqttException e) {
                        Log.e(TAG, "Error in sending stop cmd");
                    }

            }
        });

        ImageButton robotControl = (ImageButton) findViewById(R.id.robot_control);
        robotControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "Robot control clicked");

                //ShowMenu();
                /*
                if (surfaceView.getVisibility() == View.VISIBLE)
                    surfaceView.setVisibility(View.GONE);
                else
                    surfaceView.setVisibility(View.VISIBLE);
                    */

            }
        });

        ////////////////////////////////////// for testing ////////////////////////////////


        mqttStatus = cameraStatus = ConnectionStatus.CONNECTING;
        UpdateView();
        connTask = new ConnectionCheckTask();
        connTask.execute();









    }


    private void ConnectNativePlayer() {


        String url = cameraLink;//"rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";

        Log.e(TAG, "url size is: "+url.length());



        Log.e("Init", "Initiating");


        HashMap<String, String> params = new HashMap<String, String>();



        //cameraStatus = ConnectionStatus.CONNECTING;
        //UpdateView();

        mMpegPlayer.setDataSource(url, params, VideoPlayer.UNKNOWN_STREAM, VideoPlayer.NO_STREAM,
                VideoPlayer.NO_STREAM);


        String str = Long.toHexString(mMpegPlayer.NativePlayer()) + " ";
        Log.e(TAG, str);



    }


    @Override
    public void onFFUpdateTime(long currentTimeUs, long videoDurationUs, boolean isFinished) {
        /*
        mCurrentTimeUs = currentTimeUs;
        if (!mTracking) {
            int currentTimeS = (int)(currentTimeUs / 1000 / 1000);
            int videoDurationS = (int)(videoDurationUs / 1000 / 1000);
            mSeekBar.setMax(videoDurationS);
            mSeekBar.setProgress(currentTimeS);
        }

        if (isFinished) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_end_of_video_title)
                    .setMessage(R.string.dialog_end_of_video_message)
                    .setCancelable(true).show();
        }
        */

        //Log.e(TAG, "CurrentTime " + currentTimeUs + " us video duraiton " + videoDurationUs + " us is it  finishe ? " + isFinished );

        if (isFinished) {
            mMpegPlayer.stop();
            cameraStatus = ConnectionStatus.NOT_CONNECTED;
            {   findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_red);
                findViewById(R.id.top_bar_status).setBackgroundResource(R.drawable.top_bar_status_comm_lost);
            }

        }
    }

    @Override
    public void onFFError(FFError error) {


        if (error == null) {
            //Toast.makeText(this, "hello", Toast.LENGTH_LONG).show();
            //this.mLoadingView.setVisibility(View.GONE);
            //imageCamConnected = true;

            cameraStatus = ConnectionStatus.CONNECTED;
            UpdateView();

            if(mqttStatus == ConnectionStatus.CONNECTED && mqttTask == null) {
                mqttTask = new MqttTask();
                mqttTask.execute();
                Log.e(TAG, "strting MqttTask");
            }

            Log.e(TAG, "connected: " + mqttStatus);
            return;
        }

        if (error != null) {
            String format = getResources().getString(
                    R.string.main_could_not_open_image_stream);
            String message = String.format(format, error.getMessage());

            Log.e(TAG, error.getMessage());
            /*
            new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                    .setTitle("Error")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }
                    })
                    .setOnCancelListener(
                            new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    MainActivity.this.finish();
                                }
                            })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
                    */

            //cameraStatus = ConnectionStatus.NOT_CONNECTED;
            //UpdateView();
            //Log.e(TAG, "not connected");

            Log.e(TAG, "Native player retry");
            ConnectNativePlayer();


            return;
        }




    }


    @Override
    public void onFFDataSourceLoaded(FFError err, StreamInfo[] streams) {
        if (err != null) {
            String format = getResources().getString(
                    R.string.main_could_not_open_ir_stream);


            Log.e(TAG, err.getMessage());
            /*
            new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                    .setTitle("Error")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }
                    })
                    .setOnCancelListener(
                            new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    MainActivity.this.finish();
                                }
                            })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
                    */
            //cameraStatus = ConnectionStatus.NOT_CONNECTED;
            //UpdateView();

            if(connectionCanceled == false) {
                Log.e(TAG, "Native player retry");
                ConnectNativePlayer();
            }
            else {
                connectionProgress.setVisibility(View.GONE);
                cameraStatus = ConnectionStatus.NOT_CONNECTED;
                UpdateView();
            }




            return;
        }

        if(mqttStatus == ConnectionStatus.CONNECTED && mqttTask == null) {
            mqttTask = new MqttTask();
            mqttTask.execute();
            Log.e(TAG, "strting MqttTask");
        }


        if (connectionCanceled == false) {

            Log.e(TAG, "connected: " + currentMode);

            cameraStatus = ConnectionStatus.CONNECTED;
            UpdateView();



            //connTask = new ConnectionCheckTask();
            //connTask.execute();
            ///mqttTask = new MqttTask();
            //mqttTask.execute();

        }
        else {
            mMpegPlayer.stop();
            cameraStatus = ConnectionStatus.NOT_CONNECTED;
            findViewById(R.id.top_bar_status_layout).setBackgroundResource(R.color.status_bk_red);
            findViewById(R.id.top_bar_status).setBackgroundResource(R.drawable.top_bar_status_comm_lost);
        }




        //mPlayPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
        //mPlayPauseButton.setEnabled(true);
        //this.mControlsView.setVisibility(View.VISIBLE);


        /*

        for (FFmpegStreamInfo streamInfo : streams) {
            CodecType mediaType = streamInfo.getMediaType();
            Locale locale = streamInfo.getLanguage();
            String languageName = locale == null ? getString(
                    R.string.unknown) : locale.getDisplayLanguage();
            if (FFmpegStreamInfo.CodecType.AUDIO.equals(mediaType)) {
                audio.addRow(new Object[] {languageName, streamInfo.getStreamNumber()});
            } else if (FFmpegStreamInfo.CodecType.SUBTITLE.equals(mediaType)) {
                subtitles.addRow(new Object[] {languageName, streamInfo.getStreamNumber()});
            }
        }
        mLanguageAdapter.swapCursor(audio);
        mSubtitleAdapter.swapCursor(subtitles);

        */
    }

    @Override
    public void onFFResume(NotPlayingException result)
    {
    //    mPlay = true;
    }

    public void onFFPause(NotPlayingException err)
    {
    //    mPlay = false;
    }


    @Override
    public void onFFStop() {


        //mMpegPlayer.DellaocatePlayer();
        Log.e(TAG, "mMpegPlayer stopped");
        cameraStatus = ConnectionStatus.NOT_CONNECTED;


    }

    @Override
    public void onFFSeeked(NotPlayingException result) {
//		if (result != null)
//			throw new RuntimeException(result);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();


        connectionCanceled = true;
        if(mqttTask != null) {
            mqttTask.finish = true;
            mqttTask.cancel(false);
        }




        if (batInfoReceiver != null) {
            unregisterReceiver(batInfoReceiver);
            Log.e(TAG, "bat unregistered");
        }



        Log.e(TAG, "onDestroy()");

        if (cameraStatus == ConnectionStatus.CONNECTING)
            cameraStatus = ConnectionStatus.NOT_CONNECTED;


        if (connTask != null) {
            connTask.finish = true;
            connTask.cancel(false);
        }




        if (mMpegPlayer != null && (cameraStatus == ConnectionStatus.CONNECTED) && connectionCanceled == false) {
            //Log.e(TAG, "mPegPlayer.stop()");
            //mMpegPlayer.stop();

            mMpegPlayer.DellaocatePlayer();

        }


        if(locationHandler != null && locationHandler.mUnregistrar != null)
            locationHandler.mUnregistrar.unregister();



        try {
           mqttClient.disconnect();
        }
        catch (MqttException e){

        }





    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        if (locationHandler == null)
            return;

        locationHandler.mLocationPermissionGranted = false;
        switch (requestCode) {
            case LocationHandler.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationHandler.mLocationPermissionGranted = true;
                    locationHandler.updateLocationUI();
                }
                else {
                    connectionCanceled = true;
                    finish();
                }
            }
        }


    }


    /*
    private ImageButton[] mapCamBtn;        // 0 is on Map 1 is on surfaceview
    private ImageButton[] reductionBtn;
    private ImageButton[] searchBtn;
    private ImageButton[] homeBtn;

    */
    private void OnMapCamOrientationChange() {



        if(locationHandler == null || mqttStatus == ConnectionStatus.CONNECTING || cameraStatus == ConnectionStatus.CONNECTING || currentMode == Mode.SETUP )
            return;



        RelativeLayout camLayout = (RelativeLayout) findViewById(R.id.cam_layout);
        RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.map_layout);
        if(locationHandler.camMapOrientation == LocationHandler.CamMapOrientation.MAP_ON_CAM) {

            Log.e(TAG, "MAP on CAM");

            RelativeLayout.LayoutParams relParams = (RelativeLayout.LayoutParams) camLayout.getLayoutParams();
            relParams.width = locationHandler.GetMapOriginalWidth();
            relParams.height = locationHandler.GetMapOriginalHeight();
            camLayout.setLayoutParams(relParams);
            mapCamBtn[1].setVisibility(View.VISIBLE);
            reductionBtn[1].setVisibility(View.VISIBLE);
            searchBtn[1].setVisibility(View.VISIBLE);
            homeBtn[1].setVisibility(View.VISIBLE);




            relParams = (RelativeLayout.LayoutParams) mapLayout.getLayoutParams();
            relParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            relParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            mapLayout.setLayoutParams(relParams);

            findViewById(R.id.top_bar_menu).bringToFront();
            findViewById(R.id.robot_arm_btn).bringToFront();
            findViewById(R.id.camera_click).bringToFront();
            findViewById(R.id.robot_control).bringToFront();
            findViewById(R.id.joystick).bringToFront();
            findViewById(R.id.yaw_controller).bringToFront();
            findViewById(R.id.arm_dialer).bringToFront();
            findViewById(R.id.gimbal_controller).bringToFront();
            findViewById(R.id.gimbal_ogl).bringToFront();
            findViewById(R.id.landing_controller).bringToFront();
            findViewById(R.id.connection_progress).bringToFront();
            findViewById(R.id.ani_cancel_btn).bringToFront();
            camLayout.bringToFront();
            findViewById(R.id.robot_arm_btn).bringToFront();


            if (menu == Menu.ON) {
                menuLayout.setVisibility(View.GONE);
                menu = Menu.OFF;
                ShowMenu();
            }


            mapCamBtn[0].setVisibility(View.GONE);
            reductionBtn[0].setVisibility(View.GONE);
            searchBtn[0].setVisibility(View.GONE);
            homeBtn[0].setVisibility(View.GONE);

            //mapLayout.sendTo
            locationHandler.camMapOrientation = LocationHandler.CamMapOrientation.CAM_ON_MAP;

        }
        else {

            Log.e(TAG, "CAM on MAP");

            RelativeLayout.LayoutParams relParams = (RelativeLayout.LayoutParams) mapLayout.getLayoutParams();
            relParams.width = locationHandler.GetMapOriginalWidth();
            relParams.height = locationHandler.GetMapOriginalHeight();
            mapLayout.setLayoutParams(relParams);

            mapCamBtn[0].setVisibility(View.VISIBLE);
            reductionBtn[0].setVisibility(View.VISIBLE);
            searchBtn[0].setVisibility(View.VISIBLE);
            homeBtn[0].setVisibility(View.VISIBLE);





            relParams = (RelativeLayout.LayoutParams) camLayout.getLayoutParams();
            relParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            relParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            camLayout.setLayoutParams(relParams);

            findViewById(R.id.camera_click).bringToFront();
            findViewById(R.id.robot_control).bringToFront();
            findViewById(R.id.joystick).bringToFront();
            findViewById(R.id.yaw_controller).bringToFront();
            findViewById(R.id.arm_dialer).bringToFront();
            findViewById(R.id.gimbal_controller).bringToFront();
            findViewById(R.id.gimbal_ogl).bringToFront();
            findViewById(R.id.landing_controller).bringToFront();
            findViewById(R.id.connection_progress).bringToFront();

            findViewById(R.id.ani_cancel_btn).bringToFront();
            mapLayout.bringToFront();
            findViewById(R.id.top_bar_menu).bringToFront();
            findViewById(R.id.robot_arm_btn).bringToFront();

            if (menu == Menu.ON) {
                menuLayout.setVisibility(View.GONE);
                menu = Menu.OFF;
                ShowMenu();
            }

            mapCamBtn[1].setVisibility(View.GONE);
            reductionBtn[1].setVisibility(View.GONE);
            searchBtn[1].setVisibility(View.GONE);
            homeBtn[1].setVisibility(View.GONE);
            //mapLayout.sendTo
            locationHandler.camMapOrientation = LocationHandler.CamMapOrientation.MAP_ON_CAM;


        }

    }


    void ShowMenu() {
        if(currentMode == Mode.SETUP)
            return;

        Log.e(TAG, "menu clicked()");

        if (menu == Menu.OFF) {
            menuLayout.bringToFront();
            menuLayout.setVisibility(View.VISIBLE);
            TranslateAnimation anim = new TranslateAnimation(0, 0, -GlobalData.menuDisp, 0); //first 0 is start point, 150 is end point horizontal
            anim.setDuration(Def.menuDropTimeInMS); // 1000 ms = 1second
            menuLayout.startAnimation(anim); // your imageview that you want to give the animation. call this when you want it to take effect
            //robotControl.startAnimation(anim);
            robotControl.animate().setDuration(Def.menuDropTimeInMS);
            robotControl.animate().translationX(0);
            robotControl.animate().translationY(GlobalData.menuDisp);
            robotControl.animate().start();

            cameraClick.animate().setDuration(Def.menuDropTimeInMS);
            cameraClick.animate().translationX(0);
            cameraClick.animate().translationY(GlobalData.menuDisp);
            cameraClick.animate().start();

            //anim.setFillAfter(true);
            //menuLayout.setVisibility(View.VISIBLE);

            menu = Menu.ON;
        }
        else {

            TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -GlobalData.menuDisp); //first 0 is start point, 150 is end point horizontal
            anim.setDuration(Def.menuDropTimeInMS); // 1000 ms = 1second
            menuLayout.startAnimation(anim); // your imageview that you want to give the animation. call this when you want it to take effect

            robotControl.animate().setDuration(Def.menuDropTimeInMS);
            robotControl.animate().translationX(0);
            robotControl.animate().translationY(0);
            robotControl.animate().start();

            cameraClick.animate().setDuration(Def.menuDropTimeInMS);
            cameraClick.animate().translationX(0);
            cameraClick.animate().translationY(0);
            cameraClick.animate().start();


            menuLayout.setVisibility(View.GONE);

            menu = Menu.OFF;

        }

    }


    @Override
    public void onBlinkingStart(int durationSec) {

        Log.e(TAG, "onBlinkingStart() started for " + durationSec + "sec");


        topBarBattery.clearAnimation();

        final Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(1000);        // 1 sec
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(durationSec);
        animation.setRepeatMode(Animation.REVERSE);

        topBarBattery.startAnimation(animation);

        Log.e(TAG, "onBlinkingStart() finished");
    }

    public void onChangeBatteryState(BatInfoReceiver.BatteryState state) {

    }


    public void onBatteryLevelReceived(int level) {

    }



    @Override
    protected void onPause()
    {

        super.onPause();

        aGLSurfaceView.onPause();
        editor = settings.edit();
        //editor.putString("cur", edtIpAddress.getText().toString());

        editor.putFloat("currentLatitude", (float) currentLocation.latitude);
        editor.putFloat("currentLongitude", (float) currentLocation.longitude);

        if(locationHandler != null)
            locationHandler.updateHistoryList(false);
        /*
        editor.putStringSet(LocationHandler.LOCATION_HISTORY_ADDRESS, LocationHandler.edtFormattedAddress);
        editor.putStringSet(LocationHandler.LOCATION_HISTORY_LAT, LocationHandler.edtLocationLat);
        editor.putStringSet(LocationHandler.LOCATION_HISTORY_LNG, LocationHandler.edtLocationLng);
        */
        editor.putStringSet(LocationHandler.LOCATION_HISTORY, LocationHandler.edtLocaiotnHistory);

        editor.commit();

        if(mMpegPlayer != null)
            mMpegPlayer.setListener(null);
        Log.e(TAG, "onPause()");


    }

    @Override
    protected void onResume() {
        // The activity must call the GL surface view's onResume() on activity
        // onResume().
        super.onResume();
        aGLSurfaceView.onResume();
    }

    void EnterSearchMode() {



        if (currentMode != Mode.MANUAL || rightControl != RightControl.YAW_CONTROLLER)
            return;

        if (findViewById(R.id.connection_progress).getVisibility() == View.VISIBLE)
            return;

        if(locationHandler == null || locationHandler.mLocationPermissionGranted == false)
            return;

        if(locationHandler.getKeyboardStatus() == LocationHandler.KeyboardStatus.KEYBOARD_OPEN)
            return;

        if (currentMode != Mode.SEARCH) {
            currentMode = Mode.SEARCH;



            if (mMpegPlayer != null) {
                mMpegPlayer.stop();
                cameraStatus = ConnectionStatus.NOT_CONNECTED;


            }

            if(mqttTask != null) {
                mqttTask.finish = true;
                mqttTask.cancel(false);
                mqttTask = null;
            }







            findViewById(R.id.app_bar).setVisibility(View.GONE);
            findViewById(R.id.cam_layout).setVisibility(View.GONE);
            findViewById(R.id.top_menu).setVisibility(View.GONE);
            findViewById(R.id.robot_control).setVisibility(View.GONE);
            findViewById(R.id.camera_click).setVisibility(View.GONE);
            findViewById(R.id.joystick).setVisibility(View.GONE);
            findViewById(R.id.yaw_controller).setVisibility(View.GONE);
            findViewById(R.id.gimbal_controller).setVisibility(View.GONE);
            findViewById(R.id.ani_cancel_btn).setVisibility(View.GONE);




            RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.map_layout);
            RelativeLayout.LayoutParams relParams = (RelativeLayout.LayoutParams) mapLayout.getLayoutParams();
            relParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            relParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;

            mapLayout.setLayoutParams(relParams);

            mapCamBtn[0].setVisibility(View.GONE);
            searchBtn[0].setVisibility(View.GONE);
            homeBtn[0].setVisibility(View.GONE);
            reductionBtn[0].setVisibility(View.GONE);

            locationHandler.SetMode(LocationHandler.Mode.SEARCH);
            findViewById(R.id.search_layout).setVisibility(View.VISIBLE);

        }




        connectionPending = false;
    }



    public void onItemSelected(String str) {
        searchText.setText(str);

    }


    void LeaveSearchMode() {



        locationHandler.LeaveSearchMode();

        findViewById(R.id.search_layout).setVisibility(View.GONE);

        findViewById(R.id.background_view).setVisibility(View.VISIBLE);
        findViewById(R.id.app_bar).setVisibility(View.VISIBLE);




        findViewById(R.id.robot_control).setVisibility(View.VISIBLE);
        findViewById(R.id.camera_click).setVisibility(View.VISIBLE);
        findViewById(R.id.joystick).setVisibility(View.VISIBLE);



        RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.map_layout);
        RelativeLayout camLayout = (RelativeLayout) findViewById(R.id.cam_layout);


        RelativeLayout.LayoutParams relParams = (RelativeLayout.LayoutParams) camLayout.getLayoutParams();
        relParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        relParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        camLayout.setLayoutParams(relParams);

        camLayout.setVisibility(View.VISIBLE);

        relParams = (RelativeLayout.LayoutParams) mapLayout.getLayoutParams();
        relParams.width = locationHandler.GetMapOriginalWidth();
        relParams.height = locationHandler.GetMapOriginalHeight();
        mapLayout.setLayoutParams(relParams);




        mapCamBtn[0].setVisibility(View.VISIBLE);
        reductionBtn[0].setVisibility(View.VISIBLE);
        searchBtn[0].setVisibility(View.VISIBLE);
        homeBtn[0].setVisibility(View.VISIBLE);

        if(menu == Menu.ON) {
            findViewById(R.id.top_menu).setVisibility(View.VISIBLE);
        }

        UpdateView();


        if(connectionPending) {
            connectionPending = false;

            if (mqttTask != null) {
                mqttTask.finish = true;
                mqttTask.cancel(false);
                mqttTask = null;
            }

            connTask.finish = true;
            connTask.cancel(false);
            connTask = new ConnectionCheckTask();
            mqttStatus = ConnectionStatus.CONNECTING;
            cameraStatus = ConnectionStatus.CONNECTING;
            connTask.execute();

        }
        else {
            if (cameraStatus != ConnectionStatus.CONNECTED) {
                cameraStatus = ConnectionStatus.CONNECTING;
                ConnectNativePlayer();
            }
        }





        //connTask = new ConnectionCheckTask();
        //connTask.execute();



    }


    /*
                        if (armDialerHeight > 0  && armDialerWidth > 0) {
                        // resize
                        Matrix resize = new Matrix();
                        resize.postScale((float) Def.armDialerRatio*Math.min(armDialerWidth, armDialerHeight) / (float) GlobalData.imageDialerOriginal.getWidth(), Def.armDialerRatio*(float) Math.min(armDialerWidth, armDialerHeight) / (float) GlobalData.imageDialerOriginal.getHeight());
                        GlobalData.imageDialerScaled = Bitmap.createBitmap(GlobalData.imageDialerOriginal, 0, 0, GlobalData.imageDialerOriginal.getWidth(), GlobalData.imageDialerOriginal.getHeight(), resize, false);

                        // translate to the image view's center
                        Log.e(TAG, "Dialer Width " + armDialerWidth + "Dialer Height " + armDialerHeight + " ImageScaled Width " + GlobalData.imageDialerScaled.getWidth() + " ImageScaled Height " + GlobalData.imageDialerScaled.getHeight());
                        float translateX = armDialerWidth / 2 - GlobalData.imageDialerScaled.getWidth() / 2;
                        float translateY = armDialerHeight / 2 - GlobalData.imageDialerScaled.getHeight() / 2;
                        GlobalData.armDialerMatrix.postTranslate(translateX, translateY);



                        armDialer.setImageBitmap(GlobalData.imageDialerScaled);
                        armDialer.setImageMatrix(GlobalData.armDialerMatrix);


                    }



    private void fitIconIntoLeftFrame(Bitmap image, int targetWidth, int targetHeight) {

        Matrix matrix;

        Bitmap scaledImage = Bitmap.createBitmap(image, 0, 0, targetWidth, targetHeight);
    }
    */

    @Override
    public void onGPSdata(LatLng location) {

        Log.e(TAG, "onGPS");
        if (locationHandler != null)
            locationHandler.updateLocationUI(location);
    }

    @Override
    public void onConnectionLost() {

        if(currentMode == Mode.SEARCH) {
            connectionPending = true;
            Log.e(TAG, "Connection Lost in Search mode");
            return;
        }

        mqttTask.finish = true;
        mqttTask.cancel(false);
        mqttTask = null;
        connTask.finish = true;
        connTask.cancel(false);
        connTask = new ConnectionCheckTask();
        mqttStatus = ConnectionStatus.CONNECTING;
        cameraStatus = ConnectionStatus.CONNECTING;
        connTask.execute();
        Log.e(TAG, "Connection Lost");
    }

    void ShowGimbalControlelr(boolean show) {
        if(show) {
            gimbalOGL.setVisibility(View.VISIBLE);
            findViewById(R.id.gl_surface_view).setVisibility(View.VISIBLE);
        }
        else {
            gimbalOGL.setVisibility(View.GONE);
            findViewById(R.id.gl_surface_view).setVisibility(View.GONE);
        }
    }

}
