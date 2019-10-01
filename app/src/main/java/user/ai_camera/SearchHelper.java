package user.ai_camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.libraries.places.api.net.PlacesClient;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SearchHelper {

    public static final String TAG = "CurrentLocNearByPlaces";
    private static final int LOC_REQ_CODE = 1;
    private PlacesClient placesClient;
    private String name[];
    public  HttpAsyncTask httpConnect = new HttpAsyncTask();

    public SearchHelper(){

    }

    @SuppressLint("MissingPermission")
    public boolean isLocationAccessPermitted(Context context) {
        if (ContextCompat.checkSelfPermission(context,
                ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    public void requestLocationAccessPermission(Context context) {
        ActivityCompat.requestPermissions((Activity) context,
                new String[]{ACCESS_FINE_LOCATION},
                LOC_REQ_CODE);
    }

    public String[] FindNearbyPlace(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationAccessPermission(context);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        final double[] longitude = new double[1];//{location.getLongitude()};
        final double[] latitude = new double[1];//{location.getLatitude()};

        final LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                longitude[0] = location.getLongitude();
                latitude[0] = location.getLatitude();
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }
            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

        // setting  search API

        //for test
        latitude[0] = 121.5622835;
        longitude[0] = 25.0339687;

        Log.d("lat",longitude[0]+"  "+latitude[0]);
        String searchURL ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+
                longitude[0]+","+latitude[0]+"&radius=500&types=restaurant&key="+MainActivity.KEY;

        //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location= -122.08400000000002,37.421998333333335&radius=15000&type=restaurant&key=AIzaSyDrfOr8hxLrb1LhLn28fJvQvdfHu53oXPc"

        httpConnect.execute(searchURL);
        name = httpConnect.resultBack();
            Log.d("loading","loading namelist : "+name.length);


        return  name;
    }
}
