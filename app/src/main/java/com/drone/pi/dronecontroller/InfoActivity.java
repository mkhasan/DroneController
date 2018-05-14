package com.drone.pi.dronecontroller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import static com.drone.pi.dronecontroller.Def.MAX_CONNECTION_TRY_STEPS;
import static java.lang.Thread.sleep;

public class InfoActivity extends AppCompatActivity implements View.OnTouchListener {


    private static final String TAG = "InfoActivity";
    private static final String PREF_TITLE = "DroneInfo";

    public static final String CAM_ADDR = "camAddr";

    public static final String SERVER_ADDR = "serverAddr";

    public static final String MQTT_PORT = "mqttPort";

    private ImageView testView;

    EditText cameraLink;
    EditText serverIP;
    EditText mqttPort;
    SharedPreferences preferences;

    private InfoAsyncTask infoAsyncTask;

    public MyParcelable myParcelableObject = null;

    private GestureDetector gestureDetector;


    private class InfoAsyncTask extends AsyncTask<Void, String, String> {

        MyParcelable myParcelable;
        public boolean cancel = false;

        InfoAsyncTask(MyParcelable _myParcelable) {

            myParcelable = _myParcelable;
        }

        @Override
        protected String doInBackground(Void... params) {

            while (cancel == false) {
                try {
                    Log.e(TAG, "Par value is " + myParcelable.Get());

                    sleep(1000);
                } catch (Exception e) {
                    Log.e(TAG, "Sleep Error");
                }


            }
            return "";
        }

        @Override
        protected void onPostExecute(String str) {

            Log.e(TAG, "Finishing");
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        preferences = getSharedPreferences(PREF_TITLE, MODE_PRIVATE);

        cameraLink = (EditText) findViewById(R.id.camera_link);
        serverIP = (EditText) findViewById(R.id.server_ip);
        mqttPort = (EditText) findViewById(R.id.mqtt_port);


        String str = preferences.getString(CAM_ADDR, getString(R.string.default_camera_link));//"rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov");
        cameraLink.setText(str);

        str = preferences.getString(SERVER_ADDR, "58.224.86.126");
        serverIP.setText(str);

        final int port = preferences.getInt(MQTT_PORT, 1883);
        mqttPort.setText(Integer.toString(port));


        myParcelableObject = new MyParcelable(1);

        infoAsyncTask = new InfoAsyncTask(myParcelableObject);
        //infoAsyncTask.execute();

        Button startBtn = (Button) findViewById(R.id.start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String camAddr = cameraLink.getText().toString();
                String serverAddr = serverIP.getText().toString();
                int port = Integer.parseInt(mqttPort.getText().toString());



                preferences = getSharedPreferences(PREF_TITLE, MODE_PRIVATE);
                SharedPreferences.Editor edit= preferences.edit();
                edit.putString(CAM_ADDR, camAddr);
                edit.putString(SERVER_ADDR, serverAddr);
                edit.putInt(MQTT_PORT, port);
                edit.commit();




                Intent intent = new Intent(InfoActivity.this, MainActivity.class);

                //intent.putExtra("name_of_extra", myParcelableObject);
                intent.putExtra(CAM_ADDR, camAddr);
                intent.putExtra(SERVER_ADDR, serverAddr);
                intent.putExtra(MQTT_PORT, Integer.toString(port));

                startActivity(intent);

                //finish();


            }
        });

        Button testBtn = (Button) findViewById(R.id.test_btn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Log.e(TAG, "Cur value is " + myParcelableObject.Get());
                int i = myParcelableObject.Get();
                myParcelableObject.Set(i+1);
            }
        });

        /*
        gestureDetector=new GestureDetector(this,new OnSwipeListener(){

            @Override
            public boolean onSwipe(Direction direction, float x1, float y1, float x2, float y2) {
                if (direction==Direction.up){
                    //do your stuff
                    Log.e(TAG, "onSwipe: up " + "X1: " + x1 + " Y1: " + y1 + " X2: " + x2+ " Y2: " + y2);
                }

                if (direction==Direction.down){
                    //do your stuff
                    Log.e(TAG, "onSwipe: down " + "X1: " + x1 + " Y1: " + y1 + " X2: " + x2+ " Y2: " + y2);
                }

                if (direction==Direction.left){
                    //do your stuff
                    Log.e(TAG, "onSwipe: left " + "X1: " + x1 + " Y1: " + y1 + " X2: " + x2+ " Y2: " + y2);
                }

                if (direction==Direction.right){
                    //do your stuff
                    Log.e(TAG, "onSwipe: right " + "X1: " + x1 + " Y1: " + y1 + " X2: " + x2+ " Y2: " + y2);
                }


                return true;
            }

            @Override
            public boolean onSwipe(Direction direction, float incX, float incY) {
                if (direction==Direction.up){
                    //do your stuff
                    Log.e(TAG, "onSwipe: up " + "IncX: " + incX + " IncY: " + incY);
                }

                if (direction==Direction.down){
                    //do your stuff
                    Log.e(TAG, "onSwipe: down " + "IncX: " + incX + " IncY: " + incY);
                }

                if (direction==Direction.left){
                    //do your stuff
                    Log.e(TAG, "onSwipe: left " + "IncX: " + incX + " IncY: " + incY);
                }

                if (direction==Direction.right){
                    //do your stuff
                    Log.e(TAG, "onSwipe: right " + "IncX: " + incX + " IncY: " + incY);
                }


                return true;
            }



        });

        */



        testView = (ImageView) findViewById(R.id.test_view);
        testView.setOnTouchListener(this);

        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Remove it here unless you want to get this callback for EVERY
                //layout pass, which can get you into infinite loops if you ever
                //modify the layout from within this method.
                content.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Log.e(TAG, "Content Size is " + content.getWidth() + "x" + content.getHeight());

                double height = content.getHeight();
                double width = content.getWidth();


                RelativeLayout.LayoutParams relParams;
                relParams = (RelativeLayout.LayoutParams) testView.getLayoutParams();

                Log.e(TAG, "Test width " + relParams.width + " Test height " + relParams.height);


                ///////////////////////////////////////////////
            }
        });


    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch: ");
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.e(TAG, "onPuase()");
        //infoAsyncTask.cancel = true;
    }
}
