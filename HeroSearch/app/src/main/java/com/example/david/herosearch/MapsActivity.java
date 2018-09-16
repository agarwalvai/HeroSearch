package com.example.david.herosearch;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Address> addressList;
    private String call_id;
    private int mode;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private int REQUEST_CHECK_SETTINGS = 0;
    private String TAG = "MapsActivity";

    private ArrayList<Marker> activeTrackingMarkers = new ArrayList<>();
    private ArrayList<Marker> activeCallerTrackingMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        mode = getIntent().getIntExtra("tracking", 0);

        if (mode == 0) {
            String location = getIntent().getStringExtra("address");
            call_id = getIntent().getStringExtra("call_id");
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.US);

            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
        }
        else
        {
            call_id = getIntent().getStringExtra("id");
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    locationUpdate(location);
                }
            };
        };

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
    protected void onResume() {
        super.onResume();
        if ((mode == 1) || (mode == 2)) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        createLocationRequest();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Starting location updates", Toast.LENGTH_SHORT).show();
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mode == 0) {
            Address address = addressList.get(0);

            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Location of Call " + call_id));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            mMap.moveCamera(CameraUpdateFactory.zoomTo(10));

            // Add a marker in Sydney and move the camera
        /*
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */
        }

        else if (mode == 1) {

            Toast.makeText(this, "Tracking Caller Location", Toast.LENGTH_SHORT).show();

        }

        else if (mode == 2) {

            Toast.makeText(this, "Tracking Hero Location(s)", Toast.LENGTH_SHORT).show();
        }

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
// ...

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MapsActivity.this, "Getting Location", Toast.LENGTH_SHORT).show();
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        // Logic to handle location object
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                                        mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
                                    }
                                }
                            });
                }
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    private void locationUpdate(Location location)
    {
        if (mode == 1) {
            db.collection("calls")
                    .document(call_id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {

                                final double caller_latitude = task.getResult().getDouble("caller_latitude");
                                final double caller_longitude = task.getResult().getDouble("caller_longitude");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (activeCallerTrackingMarkers.size() > 0) {
                                            Marker previous_marker = activeCallerTrackingMarkers.get(activeCallerTrackingMarkers.size() - 1);
                                            activeCallerTrackingMarkers.remove(previous_marker);
                                            previous_marker.remove();
                                        }

                                        LatLng latLng = new LatLng(caller_latitude, caller_longitude);

                                        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Caller location"));

                                        /*
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                        mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
                                        */

                                        marker.showInfoWindow();

                                        activeTrackingMarkers.add(marker);
                                    }
                                });
                            } else {
                                Log.d(TAG, "Error accessing database");
                            }
                        }
                    });

            if (activeTrackingMarkers.size() > 0) {
                Marker previous_marker = activeTrackingMarkers.get(activeTrackingMarkers.size() - 1);
                activeTrackingMarkers.remove(previous_marker);
                previous_marker.remove();
            }

            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();

            LatLng latLng = new LatLng(latitude, longitude);

            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Current location"));

            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(10));

            marker.showInfoWindow();

            activeTrackingMarkers.add(marker);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    db.collection("calls")
                            .document(call_id)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful())
                                    {
                                        ArrayList<String> responder_document_ids = (ArrayList<String>) task.getResult().get("answering_heroes_ids");

                                        boolean is_responder = false;
                                        int index = 0;
                                        for (String id : responder_document_ids)
                                        {
                                            if (id.equals(HomeActivity.documentID))
                                            {
                                                is_responder = true;
                                                break;
                                            }
                                            index++;
                                        }

                                        ArrayList<Double> latitudes = (ArrayList<Double>) task.getResult().get("responder_latitudes");
                                        ArrayList<Double> longitudes = (ArrayList<Double>) task.getResult().get("responder_longitudes");

                                        latitudes.set(index, latitude);
                                        longitudes.set(index, longitude);

                                        HashMap<String, Object> data = new HashMap<>();
                                        data.put("responder_latitudes", latitudes);
                                        data.put("responder_longitudes", longitudes);

                                        db.collection("calls")
                                                .document(call_id)
                                                .set(data, SetOptions.merge())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            //Do Nothing
                                                        }
                                                        else
                                                        {
                                                            Log.d(TAG, "Error writing to database");
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }).start();
        }
    }
}
