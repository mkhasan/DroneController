package com.drone.pi.dronecontroller;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/**
 * Created by hasan on 17. 8. 12.
 */

public class PIdronePublisher {

    public static final String TOPIC_MV = "pidrone/CMD/MV";
    private float left_Xmagnitude;
    private float left_Ymagnitude;
    float right_Xmagnitude;
    float right_Ymagnitude;
    float second_right_Xmagnitude;
    float second_right_Ymagnitude;

    private final MqttClient mqttClient;

    PIdronePublisher(MqttClient _mqttClient) {
        mqttClient = _mqttClient;

        left_Xmagnitude = (float) 0.0;
        left_Ymagnitude = (float) 0.0;
        right_Xmagnitude = (float) 0.0;
        right_Ymagnitude = (float) 0.0;
        second_right_Xmagnitude = (float) 0.0;
        second_right_Ymagnitude = (float) 0.0;

    }

    void Set(float _left_Xmagnitude, float _left_Ymagnitude, float _right_Xmagnitude,
                   float _right_Ymagnitude, float _second_right_Xmagnitude, float _second_right_Ymagnitude) {

        left_Xmagnitude = _left_Xmagnitude;
        left_Ymagnitude = _left_Ymagnitude;
        right_Xmagnitude = _right_Xmagnitude;
        right_Ymagnitude = _right_Ymagnitude;
        second_right_Xmagnitude = _second_right_Xmagnitude;
        second_right_Ymagnitude = _second_right_Ymagnitude;
    }

    void SetLeftMagnitude(float _left_Xmagnitude, float _left_Ymagnitude) {
        left_Xmagnitude = _left_Xmagnitude;
        left_Ymagnitude = _left_Ymagnitude;
    }

    void SetRightMagnitude(float _right_Xmagnitude, float _right_Ymagnitude) {
        right_Xmagnitude = _right_Xmagnitude;
        right_Ymagnitude = _right_Ymagnitude;
    }

    void SetSecondRightMagnitude(float _second_right_Xmagnitude, float _second_right_Ymagnitude) {
        second_right_Xmagnitude = _second_right_Xmagnitude;
        second_right_Ymagnitude = _second_right_Ymagnitude;
    }



    void PublishMV()
            throws MqttException
    {

        final MqttTopic pidroneTopic = mqttClient.getTopic(TOPIC_MV);


        final String msg = ""+left_Xmagnitude + "," + left_Ymagnitude + ","
                                + right_Xmagnitude + "," + right_Ymagnitude + ","
                                + second_right_Xmagnitude + "," + second_right_Ymagnitude + " \r";

        pidroneTopic.publish(new MqttMessage(msg.getBytes()));
    }

    void PublishStopCmd() throws MqttException
    {
        final String msg = "0 \r";
        final MqttTopic pidroneTopic = mqttClient.getTopic(TOPIC_MV);
        pidroneTopic.publish(new MqttMessage(msg.getBytes()));
    }
}
