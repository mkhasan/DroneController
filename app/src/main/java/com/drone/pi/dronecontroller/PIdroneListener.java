package com.drone.pi.dronecontroller;

import com.google.android.gms.maps.model.LatLng;
import com.railbot.usrc.mediaplayer.FFError;
import com.railbot.usrc.mediaplayer.NotPlayingException;
import com.railbot.usrc.mediaplayer.StreamInfo;

/**
 * Created by usrc on 17. 8. 29.
 */

public interface PIdroneListener {

    void onGPSdata(LatLng location);
    void onConnectionLost();

}