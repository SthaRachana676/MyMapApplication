package com.example.mymapapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mymapapplication.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    Button btn,gotoDelhi,drawCircle;

    Location mlocation;
    Marker mCurrentLocationMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mlolocationrequest;

    private static int PERMISSION_REQUEST_CODE=12;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkRuntimePermission();



        btn=findViewById(R.id.gotoLondon);
        gotoDelhi=findViewById(R.id.gotoDelhi);
        drawCircle=findViewById(R.id.drawCircle);

        gotoDelhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng current= new LatLng(mlocation.getLatitude(),mlocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(current));
                Polyline line=mMap.addPolyline(new PolylineOptions()
                .add(current,new LatLng(28.7,77.1)).width(5).color(Color.RED));
            }
        });
         drawCircle.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 LatLng current= new LatLng(mlocation.getLatitude(),mlocation.getLongitude());
                 mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current,10));
                 Circle circle=mMap.addCircle(new CircleOptions()
                 .center(current)
                 .radius(10000)
                         .fillColor(Color.TRANSPARENT)
                 .strokeColor(Color.BLUE)
                 .strokeWidth(5));
             }
         });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng london = new LatLng(51.5072, 0.12);
                mMap.addMarker(new MarkerOptions().position(london).title("Marker in London"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(london,15));
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void checkRuntimePermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
          if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
              new AlertDialog.Builder(this).
              setTitle("Permission needed")
                      .setMessage("To receive current location ,you have to allow location access ")
                      .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_CODE);
                          }
                      }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                   dialogInterface.dismiss();
                  }
              });
          }else {
              ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                      Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_CODE);
          }
        }else {
            //leave it as it is
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //refres my activity
                Intent i=getIntent();
                finish();
                startActivity(i);
            }else {
                //Toast

            }
        }

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
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
          if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
             buildGoogleApiClient();
             mMap.setMyLocationEnabled(true);
          }
        }else{
            //ask users for permission
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }
    protected synchronized void buildGoogleApiClient(){
    mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    mGoogleApiClient.connect();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mlolocationrequest = new LocationRequest();
        mlolocationrequest.setInterval(1000);
        mlolocationrequest.setFastestInterval(1000);
        mlolocationrequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
             LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mlolocationrequest,this);


        }
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
    mlocation=location;
    if(mCurrentLocationMarker!=null){
      mCurrentLocationMarker.remove();
    }
    LatLng latLng=new LatLng(location.getLatitude(),location.getLatitude());
    MarkerOptions markerOptions=new MarkerOptions();
    markerOptions.position(latLng);
    markerOptions.title("My Current Location");
    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
    mCurrentLocationMarker=mMap.addMarker(markerOptions);


    }
}