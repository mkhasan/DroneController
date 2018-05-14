package com.drone.pi.dronecontroller;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hasan on 17. 8. 31.
 */

public class TestClass {
    final static String TAG = "TestClass";
    ArrayList<String> items = null;
    public TestClass(ArrayList<String> _items) {

        items = new ArrayList<String>();
        for(int i=0; i<_items.size(); i++)
            items.add(_items.get(i));

        
    }

    public void Modify() {
        items.add("Hello");
    }

    public void Show() {
        for (int i=0; i<items.size(); i++) {
            Log.e(TAG, items.get(i));
        }
    }
}
