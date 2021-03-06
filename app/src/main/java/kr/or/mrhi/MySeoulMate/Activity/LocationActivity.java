package kr.or.mrhi.MySeoulMate.Activity;

import static java.lang.Thread.sleep;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kr.or.mrhi.MySeoulMate.Entity.Attraction;
import kr.or.mrhi.MySeoulMate.GpsTracker;
import kr.or.mrhi.MySeoulMate.R;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    // widget
    private View view;
    private ImageView iv_marker_list;
    private FloatingActionButton fab_location;
    private FloatingActionButton fab1_location;
    private FloatingActionButton fab2_location;
    private FloatingActionButton fab3_location;

    // data
    private ArrayList<Attraction> arrayList = new ArrayList<>();
    private Attraction attraction;
    private String addr1;
    private String mapx;
    private String mapy;
    private String title;
    private String contentid;
    private Thread thread;

    // map
    private String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private GpsTracker gpsTracker;
    private GoogleMap googleMap;
    private Marker marker;
    private MarkerOptions markerOptions;
    private LatLng location;
    private double latitude;
    private double longitude;

    // animation
    private Animation fab_open;
    private Animation fab_close;
    private boolean isFabOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        gpsTracker = new GpsTracker(LocationActivity.this);
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();
        location = new LatLng(latitude, longitude);

        // ?????? ????????? ??? ?????? ??????(GPS ?????? OFF), ?????? Activity ??????
        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(getApplicationContext(), "?????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
            finish();
        }

        thread = new Thread() {
            @Override
            public void run() {
                getXmlData("12");
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        fab_location = findViewById(R.id.fab_location);
        fab1_location = findViewById(R.id.fab1_location);
        fab2_location = findViewById(R.id.fab2_location);
        fab3_location = findViewById(R.id.fab3_location);

        fab_location.setOnClickListener(this);
        fab1_location.setOnClickListener(this);
        fab2_location.setOnClickListener(this);
        fab3_location.setOnClickListener(this);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        isFabOpen = false;

        fragmentManager = getFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.fm_location);
        mapFragment.getMapAsync(this);
    }

    private void getXmlData(String contenttypeid) {
        String tag;
        String queryUrl = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/locationBasedList?serviceKey=" +
                MainActivity.KEY +
                "&MobileOS=AND&MobileApp=AppTest&arrange=E&contentTypeId=" + contenttypeid +
                "&mapX=" + longitude + "&mapY=" + latitude + "&radius=10000&listYN=Y";
        Log.d("??????", queryUrl);

        try {
            URL url = new URL(queryUrl);//???????????? ??? ?????? url??? URL ????????? ??????.
            InputStream is = url.openStream(); //url????????? ??????????????? ??????

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream ???????????? xml ????????????

            xpp.next();
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();//?????? ?????? ????????????

                        if (tag.equals("item")) ;// ????????? ????????????
                        else if (tag.equals("addr1")) {
                            xpp.next();
                            addr1 = xpp.getText();
                        } else if (tag.equals("mapx")) {
                            xpp.next();
                            mapx = xpp.getText();
                        } else if (tag.equals("mapy")) {
                            xpp.next();
                            mapy = xpp.getText();
                        } else if (tag.equals("title")) {
                            xpp.next();
                            title = xpp.getText();
                        } else if (tag.equals("contentid")) {
                            xpp.next();
                            contentid = xpp.getText();
                            break;
                        } else if (tag.equals("contenttypeid")) {
                            xpp.next();
                            contenttypeid = xpp.getText();
                            break;
                        }

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        tag = xpp.getName(); //?????? ?????? ????????????
                        if (tag.equals("item")) {
                            attraction = new Attraction(addr1, contentid, contenttypeid, mapx, mapy, title);
                            arrayList.add(attraction);
                        }
                        break;
                }

                eventType = xpp.next();
            }

        } catch (Exception e) {
            Log.e("??????", e.toString());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        location = new LatLng(latitude, longitude);
        markerOptions = new MarkerOptions();
        markerOptions.title("??? ??????");
        markerOptions.position(location);
        marker = googleMap.addMarker(markerOptions);

        setCustomMarkerView();
        drawMarker(googleMap);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_location:
                anim();
                break;
            case R.id.fab1_location:
                anim();
                gpsTracker = new GpsTracker(LocationActivity.this);
                marker.remove();
                location = new LatLng(latitude, longitude);
                markerOptions = new MarkerOptions();
                markerOptions.title("??? ??????");
                markerOptions.position(location);
                marker = googleMap.addMarker(markerOptions);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
                break;
            case R.id.fab2_location:
                anim();
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.fab3_location:
                anim();
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
        }
    }

    private void anim() {
        if (isFabOpen) {
            fab1_location.startAnimation(fab_close);
            fab2_location.startAnimation(fab_close);
            fab3_location.startAnimation(fab_close);
            fab1_location.setClickable(false);
            fab2_location.setClickable(false);
            fab3_location.setClickable(false);
            isFabOpen = false;
        } else {
            fab1_location.startAnimation(fab_open);
            fab2_location.startAnimation(fab_open);
            fab3_location.startAnimation(fab_open);
            fab1_location.setClickable(true);
            fab2_location.setClickable(true);
            fab3_location.setClickable(true);
            isFabOpen = true;
        }
    }

    private void setCustomMarkerView() {
        view = LayoutInflater.from(this).inflate(R.layout.marker_location, null);
        iv_marker_list = view.findViewById(R.id.iv_marker_list);
    }

    private void drawMarker(GoogleMap googleMap) {
        for (int i = 0; i < arrayList.size(); i++) {
            switch (arrayList.get(i).getContenttypeid()) {
                case "12": // ?????????
                    iv_marker_list.setImageResource(R.drawable.ic_check_circle);
                    break;
            }
            marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(arrayList.get(i).getMapy()), Double.parseDouble(arrayList.get(i).getMapx())))
                    .title(arrayList.get(i).getTitle())
                    .snippet(arrayList.get(i).getAddr1())
                    .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, view))));
        }
        return;
    }

    // View??? Bitmap?????? ??????
    private Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    // ActivityCompat.requestPermissions??? ????????? ????????? ????????? ????????? ???????????? ??????????????????.
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????
            boolean check_result = true;

            // ?????? ???????????? ??????????????? ???????????????.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            // ?????? ?????? ????????? ??? ??????
            if (check_result) {

            } else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????. 2?????? ????????? ????????????.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(LocationActivity.this, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????.", Toast.LENGTH_LONG).show();
                    finish();

                } else {
                    Toast.makeText(LocationActivity.this, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void checkRunTimePermission() {
        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(LocationActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(LocationActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)


            // 3.  ?????? ?????? ????????? ??? ??????


        } else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.

            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(LocationActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Toast.makeText(LocationActivity.this, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(LocationActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(LocationActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    private String getCurrentAddress(double latitude, double longitude) {
        //????????????... GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);

        } catch (IOException ioException) {
            //???????????? ??????
            Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";

        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";
    }


    //??????????????? GPS ???????????? ?????? ????????????

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("??????", "LocationActivity_onActivityResult(): GPS ON");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(LocationActivity.this, AreaActivity.class);
        startActivity(intent);
        finish();
    }
}