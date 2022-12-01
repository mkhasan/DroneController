package com.drone.pi.dronecontroller;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.LatLng;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.StringTokenizer;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

/**
 * Created by hasan on 17. 8. 27.
 */

public class PIdroneSubscribeCallback implements MqttCallback {

    static final String TAG = PIdroneSubscribeCallback.class.getSimpleName();
    static final String TOPIC_GSP = "pidrone/GPS";

    private LatLng curLocation = null;
    private int GPS_status = 0;

    private Runnable updateGPS = new Runnable() {

        @Override
        public void run() {

            if (piDroneListener != null) {
                piDroneListener.onGPSdata(curLocation);
            }

        }

    };

    private PIdroneListener piDroneListener = null;

    private Activity activity;


    public PIdroneSubscribeCallback(Activity _activigy) {
        activity = _activigy;


    }



    @Override
    public void connectionLost(Throwable cause) {
        //This is called when the connection is lost. We could reconnect here.
        Log.e(TAG, "Connection Lost");
        if(piDroneListener != null)
            piDroneListener.onConnectionLost();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.e(TAG, "Message arrived. Topic: " + topic + "  Message: " + message.toString());

        int i=0;


        Double latitude = 0.0;
        Double lognitude = 0.0;
        Double altitude = 0.0;
        int numofsatelite = 0;
        double speed = 0.0;
        int status = 0;

        String str;
        String msgStr = message.toString() + "";




        if (TOPIC_GSP.equals(topic)) {

            StringTokenizer st = new StringTokenizer(msgStr, ", \r");
            while (st.hasMoreTokens()) {
                str = st.nextToken();
                //Log.e(TAG, str);
                switch (i) {
                    case 0:
                        latitude = Double.parseDouble(str);
                        break;

                    case 1:
                        lognitude = Double.parseDouble(str);
                        break;

                    case 2:
                        altitude = Double.parseDouble(str);
                        break;

                    case 3:
                        numofsatelite = Integer.parseInt(str);
                        break;

                    case 4:
                        speed = Double.parseDouble(str);
                        break;

                    case 5:
                        status = Integer.parseInt(str);

                        break;
                }

                //Log.e(TAG, "index is " + i + " value is " + (i==0 ? latitude : (i==1 ? lognitude : status)));
                if(i == 6 && false) {
                    for (int k = 0; k < str.length(); k++)
                        Log.e(TAG, "char at "+ k + " is " + (int)str.charAt(k));
                }


                i++;

            }

            Log.e(TAG, "Index is " + i);
            if (i>=6 && status == 0) {

                curLocation = new LatLng(latitude, lognitude);

                activity.runOnUiThread(updateGPS);
            }

        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //no-op
    }

    void SetListener(PIdroneListener _piDroneListener) {
        piDroneListener = _piDroneListener;
    }



}
