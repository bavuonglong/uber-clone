package com.codeko.userclone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;
    Button btnCallAnUber;
    boolean requestActive = false;

    TextView tvInfo;

    boolean driverActive = false;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btnCallAnUber = findViewById(R.id.btnCallUber);
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        requestActive = true;
                        btnCallAnUber.setText("Cancel Uber");
                        checkForUpdate();
                    }
                }
            }
        });

        tvInfo = findViewById(R.id.tvInfo);
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

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateMap(location);
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

        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    updateMap(lastKnownLocation);
                }
            }
        }
    }

    private void updateMap(Location location) {
        if (driverActive == false) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 9));
            mMap.addMarker(new MarkerOptions().position(userLocation).title("Your location"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateMap(lastKnownLocation);
            }
        }
    }

    public void callAnUber(View view) {
        if (requestActive) {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
            query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                            for (ParseObject object : objects) {
                                object.deleteInBackground();
                            }
                            requestActive = false;
                            btnCallAnUber.setText("Call An Uber");

                            checkForUpdate();
                        }
                    }
                }
            });
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    ParseObject request = new ParseObject("Request");
                    request.put("username", ParseUser.getCurrentUser().getUsername());

                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    request.put("location", parseGeoPoint);
                    request.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                btnCallAnUber.setText("Cancel Uber");
                                requestActive = true;
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Could not find location. Please try again", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void checkForUpdate() {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.whereExists("driverUsername");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {

                        driverActive = true;

                        ParseQuery<ParseUser> query1 = ParseUser.getQuery();
                        query1.whereEqualTo("username", objects.get(0).getString("driverUsername"));
                        query1.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> objects, ParseException e) {
                                if (e == null && objects.size() > 0) {
                                    final ParseGeoPoint driverLocation = objects.get(0).getParseGeoPoint("location");

                                    if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                        if (lastKnownLocation != null) {
                                            ParseGeoPoint userLocation = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                            Double distanceInMiles = driverLocation.distanceInMilesTo(userLocation);

                                            if (distanceInMiles < 0.01) {
                                                tvInfo.setText("Your driver is here");

                                                final ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
                                                query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                                                query.findInBackground(new FindCallback<ParseObject>() {
                                                    @Override
                                                    public void done(List<ParseObject> objects, ParseException e) {
                                                        if (e == null && objects.size() > 0) {
                                                            for (ParseObject object : objects) {
                                                                object.deleteInBackground();
                                                            }
                                                        }
                                                    }
                                                });

                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        tvInfo.setText("");
                                                        btnCallAnUber.setVisibility(View.VISIBLE);
                                                        btnCallAnUber.setText("Call An Uber");
                                                        requestActive = false;
                                                        driverActive = false;

                                                    }
                                                }, 5000);

                                            } else {
                                                Double distanceInDP = Double.valueOf(Math.round(distanceInMiles * 10) / 10);
                                                tvInfo.setText("Your driver is " + distanceInDP.toString() + " miles way");

                                                LatLng driverLocationLatLng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
                                                LatLng requestLocationLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

                                                ArrayList<Marker> markers = new ArrayList<>();

                                                mMap.clear();

                                                markers.add(mMap.addMarker(new MarkerOptions().position(driverLocationLatLng).title("Driver Location")));
                                                markers.add(mMap.addMarker(new MarkerOptions().position(requestLocationLatLng).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

                                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                for (Marker marker : markers) {
                                                    builder.include(marker.getPosition());
                                                }

                                                LatLngBounds bounds = builder.build();

                                                int padding = 30;
                                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                                                mMap.animateCamera(cu);

                                                btnCallAnUber.setVisibility(View.INVISIBLE);

                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        checkForUpdate();
                                                    }
                                                }, 2000);
                                            }
                                        }
                                    }
                                }
                            }
                        });

                    }
                }
            }
        });
    }

    public void logout(View view) {
        ParseUser.logOut();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
