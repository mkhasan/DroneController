package com.drone.pi.dronecontroller;

/**
 * Created by usrc on 17. 8. 6.
 */

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import static android.graphics.BitmapFactory.decodeResource;


/**
 * Created by usrc on 17. 8. 6.
 */

interface SearchListener {
    public void onLocationFound(List<HashMap<String, String>> _locationList);
}


public class LocationHandler implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        ItemsAapdeterListener,
        SearchListener{


    ////////////////////  variable declaration /////////////////////////
    static final String TAG = LocationHandler.class.getSimpleName();

    public static final float DEFAULT_LATITUDE = (float) 36.358275;
    public static final float DEFAULT_LONGITUDE = (float) 127.364675;
    private static final int MY_LOCAITON_BTN_MARGIN_RIGHT = 50;
    private static final int MY_LOCAITON_BTN_MARGIN_BOTTOM = 50;

    public static final String FORMATTED_ADDR = "formatted_address";
    private static final int MAX_HISTORY_SIZE = 20;

    public static final String LOCATION_HISTORY_ADDRESS = "locationHistoryAddress";
    public static final String LOCATION_HISTORY_LAT = "locationHistoryLat";
    public static final String LOCATION_HISTORY_LNG = "locationHistoryLng";
    public static final String LOCATION_HISTORY = "locationHistory";

    public static Set<String>       edtLocaiotnHistory;
    public static Set<String>       edtFormattedAddress;
    public static Set<String>       edtLocationLat;
    public static Set<String>       edtLocationLng;


    private final LatLng mDefaultLocation = new LatLng(36.358275, 127.364675);//-33.8523341, 151.2106085);

    private Marker myLocMarker = null;
    private Marker target = null;


    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final int DEFAULT_ZOOM = 15;

    private AppCompatActivity activity;


    private boolean isConnected = false;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;



    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;

    private LatLng currLocation;
    private LatLng currTarget;

    public boolean mLocationPermissionGranted = false;

    public enum CamMapOrientation {
        MAP_ON_CAM,
        CAM_ON_MAP
    }

    public enum Mode {
        NORMAL,
        SEARCH,

    }

    public enum TargetMode {
        NONE,
        DELIVERY,
        AUTO_PILOT
    }


    public CamMapOrientation camMapOrientation = CamMapOrientation.MAP_ON_CAM;

    private Mode currentMode = Mode.NORMAL;

    public TargetMode targetMode = TargetMode.NONE;

    private int mapOriginalWidth = 0;
    private int mapOriginalHeight = 0;


    private ItemsAdapter adapter;

    private List<SearchHistory> historyList;

    private List<HashMap<String, String>> locationList;

    final ListView listView;

    ItemSelListener listener = null;

    public ImageButton testBtn = null;

    private EditText searchText;

    private RelativeLayout searchIconBox;

    private RelativeLayout searchInput;

    ImageButton myLocation;

    ImageView searchIconDelivery;

    ImageView searchIconAuto;

    EditText packageWeight;

    TextView packageUnit;

    ImageButton deliveryGo;

    EditText autoSpeed;

    TextView autoUnit;

    ImageButton autoGo;


    ////////////////////////// kb handler ////////////////////////////

    public Unregistrar mUnregistrar = null;

    public enum KeyboardStatus {
        KEYBOARD_OPEN,
        KEYBOARD_CLOSE,
    }

    private KeyboardStatus keyboardStatus;

    ////////////////////////// kb handler ////////////////////////////

    ////////////////////////////////////////////////////////////////////

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        }catch(Exception e){
            return "";
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    ///////////////////// class declaration ///////////////////////////


    class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;
        SearchListener listener;

        ParserTask(SearchListener _listener) {
            listener = _listener;
        }

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String,String>> doInBackground(String...url) {//} jsonData) {

            List<HashMap<String, String>> places = null;
            GeocodeJSONParser parser = new GeocodeJSONParser();

            String data = null;

            Log.e(TAG, "Download started " + url[0]);

            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.e(TAG,e.toString());
            }

            Log.e(TAG, "Download finished " + data.toString());



            try{
                jObject = new JSONObject(data);

                /** Getting the parsed data as a an ArrayList */
                places = parser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){


            if (listener != null) {

                listener.onLocationFound(list);
            }
        }
    }



    ///////////////////// class declaration ///////////////////////////





    public LocationHandler(AppCompatActivity _activity, LatLng _currLocation) {
        activity = _activity;
        currLocation = _currLocation;
        currentMode = Mode.NORMAL;


        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        mGoogleApiClient.connect();

        /*
        historyList.add("Woolpyong-dong 750 atp no 202 daejeon");
        historyList.add("history 1");
        historyList.add("history 2");
        historyList.add("history 3");
        historyList.add("history 4");
        historyList.add("history 5");

        */

        updateHistoryList(true);
        locationList = new ArrayList<HashMap<String, String>>();


        listView = (ListView) activity.findViewById(R.id.search_list);

        adapter = new ItemsAdapter(LayoutInflater.from(activity));
        adapter.swapHistoryItems(historyList);
        adapter.swapLocationItems(locationList);
        adapter.setListener(this);

        listView.setAdapter(adapter);
        listView.setVisibility(View.GONE);

        searchText = (EditText) activity.findViewById(R.id.search_text);
        searchIconBox = (RelativeLayout) activity.findViewById(R.id.search_icon_box);
        searchInput = (RelativeLayout) activity.findViewById(R.id.search_input);
        myLocation = (ImageButton) activity.findViewById(R.id.my_location);
        searchIconDelivery = (ImageView) activity.findViewById(R.id.search_icon_delivery);
        searchIconAuto = (ImageView) activity.findViewById(R.id.search_icon_auto);
        packageWeight= (EditText) activity.findViewById(R.id.package_weight);
        packageUnit= (TextView) activity.findViewById(R.id.package_unit);
        deliveryGo = (ImageButton) activity.findViewById(R.id.delivery_go);
        autoSpeed = (EditText) activity.findViewById(R.id.auto_speed);
        autoUnit = (TextView) activity.findViewById(R.id.auto_unit);
        autoGo = (ImageButton) activity.findViewById(R.id.auto_go);

        searchIconDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentMode == Mode.NORMAL)
                    return;

                autoSpeed.setVisibility(View.GONE);
                autoUnit.setVisibility(View.GONE);
                autoGo.setVisibility(View.GONE);
                packageWeight.setVisibility(View.VISIBLE);
                packageUnit.setVisibility(View.VISIBLE);

                if(packageWeight.getText().toString().length() > 0)
                    deliveryGo.setVisibility(View.VISIBLE);
                else
                    deliveryGo.setVisibility(View.GONE);

                targetMode = TargetMode.DELIVERY;

            }
        });

        searchIconAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                packageWeight.setVisibility(View.GONE);
                packageUnit.setVisibility(View.GONE);
                deliveryGo.setVisibility(View.GONE);

                autoSpeed.setVisibility(View.VISIBLE);
                autoUnit.setVisibility(View.VISIBLE);
                if(autoSpeed.getText().toString().length() > 0)
                    autoGo.setVisibility(View.VISIBLE);
                else
                    autoGo.setVisibility(View.GONE);

                targetMode = TargetMode.AUTO_PILOT;

            }
        });

        packageWeight.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    if(packageWeight.getText().length() > 0)
                        deliveryGo.setVisibility(View.VISIBLE);
                    else
                        deliveryGo.setVisibility(View.GONE);
                }
                return false;
            }
        });

        autoSpeed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    if(autoSpeed.getText().length() > 0)
                        autoGo.setVisibility(View.VISIBLE);
                    else
                        autoGo.setVisibility(View.GONE);
                }
                return false;
            }
        });



        mUnregistrar = KeyboardVisibilityEvent.registerEventListener(activity, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {

                if(currentMode == Mode.SEARCH) {

                    ImageButton searchExit = (ImageButton) activity.findViewById(R.id.search_exit);
                    Bitmap image = BitmapFactory.decodeResource(activity.getResources(), ((isOpen == true) ? R.drawable.back_arrow : R.drawable.search_exit));
                    searchExit.setImageBitmap(image);


                }
                keyboardStatus = (isOpen ? KeyboardStatus.KEYBOARD_OPEN : KeyboardStatus.KEYBOARD_CLOSE);

                Log.e(TAG, " Openn ? " + (keyboardStatus == KeyboardStatus.KEYBOARD_OPEN ? "yes" : "no"));
            }
        });

        keyboardStatus = (KeyboardVisibilityEvent.isKeyboardVisible(activity) ? KeyboardStatus.KEYBOARD_OPEN : KeyboardStatus.KEYBOARD_CLOSE);


    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;


        mMap.setOnMyLocationButtonClickListener(this);
        updateLocationUI();

        mMap.setOnMarkerClickListener(this);

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if(currentMode == Mode.NORMAL)
                    return;
                if(target != null)
                    followMarker(target);

                /*
            if(target != null && testBtn.getVisibility() == View.VISIBLE)
                 followMarker(target);
                 */

             //if()
            }
        });

    }



    @Override
    public void onConnected(Bundle connectionHint) {
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) activity.getSupportFragmentManager()
                .findFragmentById(R.id.map);



        mapFragment.getMapAsync(this);


        View mapView = mapFragment.getView();
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.rightMargin = MY_LOCAITON_BTN_MARGIN_RIGHT;
            layoutParams.bottomMargin = MY_LOCAITON_BTN_MARGIN_BOTTOM;
            locationButton.setLayoutParams(layoutParams);


            //Log.e(TAG, "MyLocation Dimension: " + locationButton.getWidth() + " x " + locationButton.getHeight());
         }



        /*
        mapOriginalWidth = mapFragment.getView().getWidth();
        mapOriginalHeight = mapFragment.getView().getHeight();

        Log.e(TAG, "map dim "+mapOriginalWidth+" "+mapOriginalHeight );
        */


        isConnected = true;
        Log.e(TAG, "map Connected");
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());

        isConnected = false;
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */


    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Play services connection suspended");

        isConnected = false;
    }

    public void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */






        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

            //mMap.setMyLocationEnabled(currentMode == Mode.SEARCH ? true : false);

            if (currentMode == Mode.SEARCH) {
                //mMap.setMyLocationEnabled(true);
                mMap.setMyLocationEnabled(false);

            }
            else
                mMap.setMyLocationEnabled(false);

        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            mMap.setMyLocationEnabled(false);
        }


        CameraPosition position = new CameraPosition.Builder()
                .target(currLocation)
                .zoom(DEFAULT_ZOOM).build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));


        Bitmap image = decodeResource(activity.getResources(), currentMode == Mode.NORMAL ? R.drawable.marker_my_location_small : R.drawable.marker_my_location_big);
        myLocMarker = mMap.addMarker(new MarkerOptions()
                .position(currLocation)
                .icon(BitmapDescriptorFactory.fromBitmap(image))
                // Specifies the anchor to be at a particular point in the marker image.
                .anchor(0.5f, 0.5f));




    }

    public void updateLocationUI(LatLng location) {

        if (mLocationPermissionGranted == false)
            return;

        Log.e(TAG, "updateLocUI");

        currLocation = location;
        if (myLocMarker != null)
            myLocMarker.remove();
        updateLocationUI();
    }



   public boolean IsInitialized() {

        return (isConnected && (mapOriginalWidth > 0) && (mapOriginalHeight > 0));
    }

    public void SetMapOriginalDimension(int width, int height) {
        mapOriginalWidth = width;
        mapOriginalHeight = height;
    }

    public int GetMapOriginalWidth() {
        return mapOriginalWidth;
    }

    public int GetMapOriginalHeight() {
        return mapOriginalHeight;
    }


    public void onGPSdata(LatLng location) {

        updateLocationUI(location);
    }

    public void SetMode (Mode mode) {
        currentMode = mode;
        if (mLocationPermissionGranted && currentMode == Mode.SEARCH) {

            updateLocationUI();
            EnterSearchMode();

        }

    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "MyLocation clicked()");
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        ShowHistory();

        CameraPosition position = new CameraPosition.Builder()
                .target(currLocation)
                .zoom(DEFAULT_ZOOM).build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

        /*

        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)



            Log.e(TAG, "MyLocation Dimension: " + locationButton.getWidth() + " x " + locationButton.getHeight());
         }
        */

        return true;
    }

    private void EnterSearchMode() {
        listView.setVisibility(View.VISIBLE);
        if(myLocMarker != null)
            myLocMarker.remove();

        Bitmap image = decodeResource(activity.getResources(), R.drawable.marker_my_location_big);
        myLocMarker = mMap.addMarker(new MarkerOptions()
                .position(currLocation)
                .icon(BitmapDescriptorFactory.fromBitmap(image))
                // Specifies the anchor to be at a particular point in the marker image.
                .anchor(0.5f, 0.5f));

        if(target != null)
            target.remove();

        searchInput.setVisibility(View.GONE);
        searchIconBox.setVisibility(View.VISIBLE);
        packageWeight.setVisibility(View.GONE);
        packageUnit.setVisibility(View.GONE);
        deliveryGo.setVisibility(View.GONE);
        autoSpeed.setVisibility(View.GONE);
        autoUnit.setVisibility(View.GONE);
        autoGo.setVisibility(View.GONE);

        targetMode = TargetMode.NONE;

        myLocation.setVisibility(View.VISIBLE);

        ImageButton searchExit = (ImageButton) activity.findViewById(R.id.search_exit);
        image = BitmapFactory.decodeResource(activity.getResources(), R.drawable.search_exit);
        searchExit.setImageBitmap(image);


    }


    public void LeaveSearchMode() {
        listView.setVisibility(View.GONE);
        if(myLocMarker != null)
            myLocMarker.remove();

        Bitmap image = decodeResource(activity.getResources(), R.drawable.marker_my_location_small);
        myLocMarker = mMap.addMarker(new MarkerOptions()
                .position(currLocation)
                .icon(BitmapDescriptorFactory.fromBitmap(image))
                // Specifies the anchor to be at a particular point in the marker image.
                .anchor(0.5f, 0.5f));

        if(target != null) {
            target.remove();
            Bitmap image1 = decodeResource(activity.getResources(), R.drawable.icon_target_mini);
            target = mMap.addMarker(new MarkerOptions()
                    .position(currTarget)
                    .icon(BitmapDescriptorFactory.fromBitmap(image1))
                    // Specifies the anchor to be at a particular point in the marker image.
                    .anchor(0.5f, 0.5f));
        }


        searchInput.setVisibility(View.GONE);
        myLocation.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);

        closeKeyboard();


    }




    public void onItemSelected(int position) {



        String str = null;

        if (adapter.getMode() == ItemsAdapter.Mode.HISTORY) {


            if (historyList == null)
                return;
            SearchHistory item = historyList.get(position);
            if (item != null) {
                str = item.formattedAddress;
                historyList.add(0, item);
                int size = historyList.size();

                while (size > MAX_HISTORY_SIZE) {
                    historyList.remove(size - 1);
                    size = historyList.size();

                }

                int i = 1;
                while (i < size) {


                    SearchHistory temp = historyList.get(i);
                    if (temp != null) {
                        String tempStr = temp.formattedAddress;
                        if (tempStr.equals(str)) {
                            size--;
                            historyList.remove(i);

                        } else
                            i++;
                    }

                }

                adapter.swapHistoryItems(historyList);

                if (target != null)
                    target.remove();



                Log.e(TAG, "Lat Lng " + item.location.longitude + " " + item.location.longitude );

                currTarget = new LatLng(item.location.latitude, item.location.longitude);

                CameraPosition camPos = new CameraPosition.Builder()
                        .target(currTarget)
                        .zoom(DEFAULT_ZOOM).build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));


                Bitmap image = decodeResource(activity.getResources(), R.drawable.icon_target);
                target = mMap.addMarker(new MarkerOptions()
                        .position(currTarget)
                        .icon(BitmapDescriptorFactory.fromBitmap(image))
                        // Specifies the anchor to be at a particular point in the marker image.
                        .anchor(0.5f, 0.5f));



            }

        }
        else {
            if (locationList == null)
                return;
            HashMap<String, String> item = locationList.get(position);
            if (item != null)
                str = item.get(FORMATTED_ADDR);
            if (str != null) {

                Double lat = Double.parseDouble(item.get("lat"));
                Double lng = Double.parseDouble(item.get("lng"));

                SearchHistory hist = new SearchHistory();
                hist.location = new LatLng(lat, lng);
                hist.formattedAddress = str;

                historyList.add(0, hist);
                int size = historyList.size();

                while (size > MAX_HISTORY_SIZE) {
                    historyList.remove(size - 1);
                    size = historyList.size();

                }

                int i = 1;
                while (i < size) {


                    SearchHistory temp = historyList.get(i);
                    if (temp != null) {
                        String tempStr = temp.formattedAddress;
                        if (tempStr.equals(str)) {
                            size--;
                            historyList.remove(i);

                        } else
                            i++;
                    }

                }

                adapter.swapHistoryItems(historyList);
                if (target != null)
                    target.remove();

                Log.e(TAG, "Lat Lng " + lat + " " + lng );

                currTarget = new LatLng(lat, lng);
                CameraPosition camPos = new CameraPosition.Builder()
                        .target(currTarget)
                        .zoom(DEFAULT_ZOOM).build();


                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));


                Bitmap image = decodeResource(activity.getResources(), R.drawable.icon_target);
                target = mMap.addMarker(new MarkerOptions()
                        .position(currTarget)
                        .icon(BitmapDescriptorFactory.fromBitmap(image))
                        // Specifies the anchor to be at a particular point in the marker image.
                        .anchor(0.5f, 0.5f));




            }


        }





        if(listener != null) {

            if (str == null || str.equals(""))
                str = "Not found";

            searchText.setText(str);
            //listener.onItemSelected(str);
        }
    }

    public void Search(String location) {
        if(currentMode != Mode.SEARCH)
            return;

        Log.e(TAG, "Searchiing " + location);

        closeKeyboard();
        String url = "https://maps.googleapis.com/maps/api/geocode/json?";

        try {
            // encoding special characters like space in the user input place
            location = URLEncoder.encode(location, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return;
        }



        String address = "address=" + location;

        String sensor = "sensor=false";

        // url , from where the geocoding data is fetched
        url = url + address + "&" + sensor;

        Log.e(TAG, "url is " + url);
        //DownloadTask downloadTask = new DownloadTask(this);
        ParserTask parserTask = new ParserTask(this);

        // Start downloading the geocoding places
        parserTask.execute(url);



    }

    public void onLocationFound(List<HashMap<String, String>> list) {

        if (list != null) {
            Log.e(TAG, "ParaseTask finished " + list.size());
            locationList = new ArrayList<HashMap<String, String>>();


            for (int i = 0; i < list.size(); i++) {
                locationList.add(list.get(i));
            }

            if (adapter != null) {
                adapter.setMode(ItemsAdapter.Mode.LOCATION);
                adapter.swapLocationItems(locationList);
            }
        }
    }

    public void setListener(ItemSelListener _listener) {
        listener = _listener;
    }

    public void ShowHistory() {
        for (int i=0; i<historyList.size(); i++)
            Log.e (TAG, "positoin " + i + " location " + historyList.get(i).formattedAddress
                + " lat " + historyList.get(i).location.latitude + " lng " + historyList.get(i).location.longitude);

    }

    public void centerMyLation() {
        if (currentMode == Mode.SEARCH)
            onMyLocationButtonClick();
    }

    public void updateHistoryList(boolean get) {

        if (get) {
            historyList = new ArrayList<SearchHistory>();


            Double lat, lng;

            if(edtLocaiotnHistory == null)
                return;

            int size = edtLocaiotnHistory.size();
            if(size == 0)
                return;

            for(Iterator<String > it = edtLocaiotnHistory.iterator(); it.hasNext(); ) {
                String str;
                String addr;
                String histStr = it.next();

                int i = 0;
                int k=0;
                double latitude, lognitude;

                for (k = 0; k < histStr.length(); k++) {
                    if(histStr.charAt(k) == ' ') {

                        i++;

                    }

                    if(i==2) {

                        break;
                    }


                }

                if (i < 2)
                    return;

                addr = histStr.substring(k);

                Log.e(TAG, "addr is " + addr);

                StringTokenizer st = new StringTokenizer(histStr.toString(), " ");
                boolean finished = false;
                latitude = -1.0;
                lognitude = -1.0;

                i = 0;
                while (st.hasMoreTokens() && finished == false) {
                    str = st.nextToken();
                    Log.e(TAG, str);

                    switch (i) {
                        case 0:
                            latitude = Double.parseDouble(str);
                            break;

                        case 1:
                            lognitude = Double.parseDouble(str);
                            finished = true;
                            break;

                        case 2:
                            finished = true;
                            break;
                    }
                    i++;
                }

                Log.e(TAG, " lat " + latitude + " lon " + lognitude);

                if (addr == null || addr.equals("") || latitude < 0 || lognitude < 0)
                    return;

                SearchHistory hist = new SearchHistory();
                hist.formattedAddress = addr;
                hist.location = new LatLng(latitude, lognitude);
                historyList.add(hist);



            }

        }
        else {

            edtLocaiotnHistory.clear();
            for(int i=0; i<historyList.size(); i++) {
                SearchHistory hist = historyList.get(i);
                String str = Double.toString(hist.location.latitude) + " "
                        + Double.toString(hist.location.longitude) + " " + hist.formattedAddress;

                edtLocaiotnHistory.add(str);
            }

            LocationHandler.ShowStrSet(edtLocaiotnHistory, " hist lock inside");
            /*
            edtFormattedAddress.clear();
            edtLocationLat.clear();
            edtLocationLng.clear();

            for(int i=0; i<historyList.size(); i++) {
                SearchHistory hist = historyList.get(i);
                //Log.e(TAG, "Aaddr " + hist.formattedAddress + " lat " + hist.location.latitude + " lng " + hist.location.longitude);
                edtFormattedAddress.add(hist.formattedAddress);
                edtLocationLat.add(Double.toString(hist.location.latitude));
                edtLocationLng.add(Double.toString(hist.location.longitude));
            }

            LocationHandler.ShowStrSet(LocationHandler.edtFormattedAddress, " Format addr ");
            LocationHandler.ShowStrSet(LocationHandler.edtLocationLat, " Lat ");
            LocationHandler.ShowStrSet(LocationHandler.edtLocationLng, " Lng ");
            */


        }
    }

    static void ShowStrSet(Set<String> list, String msg) {

        Log.e(TAG, msg);

        for (Iterator<String> it = list.iterator(); it.hasNext(); ) {
            Log.e(TAG, msg + it.next());
        }
        Log.e(TAG, " ");
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {


        if (marker.equals(target))
        {
            //marker.showInfoWindow();
            Log.e(TAG, "Got it");

            if(currentMode == Mode.NORMAL)
                return true;

            if(searchInput.getVisibility() == View.GONE)
                searchInput.setVisibility(View.VISIBLE);
            else
                searchInput.setVisibility(View.GONE);
            //handle click here
            followMarker(marker);


            return true;
        }


        return false;

    }

    public void followMarker(Marker marker) {

        final int gap = 50;
        if(searchInput.getVisibility() == View.GONE)
            return;

        Projection projection = mMap.getProjection();



        LatLng markerLocation = marker.getPosition();



        Point screenPosition = projection.toScreenLocation(markerLocation);


        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) searchInput.getLayoutParams();

        lp.leftMargin = screenPosition.x+gap;
        lp.topMargin = screenPosition.y - (int)(GlobalData.screenWidth*Def.searchIconBoxHeightToWidthRatio)/2;
        searchInput.setLayoutParams(lp);



    }


    public void closeKeyboard() {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }


    }

    public KeyboardStatus getKeyboardStatus() {
        return keyboardStatus;
    }


}
