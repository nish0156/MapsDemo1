package dk.tec.googlemapdemo;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final int FINE_LOCATION_PERMISSION_REQUEST = 1;
    private GoogleMap gMap;

    private LocationDbHelper dbHelper;

    Button btn_save;


    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new LocationDbHelper(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(view -> {
            saveLocationToDb(currentLocation);
        });

        getUpdates();


    }
    private void saveLocationToDb(Location location){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocationContract.LocationEntry.COLUMN_LATITUDE, location.getLatitude());
        values.put(LocationContract.LocationEntry.COLUMN_LONGITUDE, location.getLongitude());

        long newRowId = db.insert(LocationContract.LocationEntry.TABLE_NAME, null, values);
        if (newRowId != -1) {
            Log.d("LocationSaved", "Location saved successfully, Row ID: " + newRowId);
        } else {
            Log.e("LocationSaved", "Error saving location");
        }
        db.close();
    }


    private void getUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted
            return;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();

        // Requesting location updates
        fusedLocationProviderClient.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                currentLocation =  locationResult.getLastLocation();
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                if(mapFragment != null){
                    mapFragment.getMapAsync(MainActivity.this);
                }

                // super.onLocationResult(locationResult);

            }
        }, Looper.myLooper());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        if(currentLocation!= null){
            LatLng locationName = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            gMap.addMarker(new MarkerOptions().position(locationName).title("Copenhagen"));
            gMap.moveCamera(CameraUpdateFactory.newLatLng(locationName));
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUpdates();
            }else{
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


}