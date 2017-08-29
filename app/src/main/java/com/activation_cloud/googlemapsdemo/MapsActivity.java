package com.activation_cloud.googlemapsdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView textView;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 105 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED  )
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        textView = (TextView)findViewById(R.id.textView);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        if(locationManager != null)
            locationManager = null;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if(locationListener != null)
            locationManager = null;
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location!= null)
                {
                    UpdateLocale(location);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.i("loc", "onStatusChanged");
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.i("loc", "onProviderEnabled");
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.i("loc", "onProviderDisabled");
            }
        };


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            ActivityCompat.requestPermissions (this,
                    new String [] {Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE},103);

        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},105);
                ActivityCompat.requestPermissions (this,
                        new String [] {Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE},103);
            }
            else
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                ActivityCompat.requestPermissions (this,
                        new String [] {Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE},103);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastLocation != null) {
                    Log.i("INFO", "last:" + lastLocation.toString());
                    UpdateLocale(lastLocation);
                }
            }

        }
    }

    protected void UpdateLocale(Location location)
    {
        LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLoc).title("I am here!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLoc));

        Geocoder geocode = new Geocoder(getApplicationContext(), Locale.getDefault());
        if(!geocode.isPresent())
        {
            textView.setText("Geocoding service is not present");
        }

        try {
            List<Address> listAddress = geocode.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if(listAddress!=null && listAddress.size() >0)
            {
                String address = "Address info: ";

                if(listAddress.get(0).getSubThoroughfare()!=null)
                {
                    address += listAddress.get(0).getSubThoroughfare() + " ";
                }

                if(listAddress.get(0).getThoroughfare()!=null)
                {
                    address += listAddress.get(0).getThoroughfare() + ", ";
                }

                if(listAddress.get(0).getLocale()!=null)
                {
                    address += listAddress.get(0).getLocality() + ", ";
                }

                if(listAddress.get(0).getPostalCode()!=null)
                {
                    address += listAddress.get(0).getPostalCode() + ", ";
                }

                if(listAddress.get(0).getCountryName()!=null)
                {
                    address += listAddress.get(0).getCountryName() + ".";
                }

                textView.setText(address);
            }
        } catch (IOException e) {
            textView.setText("");
            Toast.makeText(this,"Check your access to the Internet and restart application.",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }
}
