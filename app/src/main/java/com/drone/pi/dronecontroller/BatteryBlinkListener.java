package com.drone.pi.dronecontroller;

/**
 * Created by hasan on 17. 8. 15.
 */

public interface BatteryBlinkListener {

    public void onBlinkingStart(int duraionSec);

    public void onChangeBatteryState(BatInfoReceiver.BatteryState state);

    public void onBatteryLevelReceived(int level);
}
