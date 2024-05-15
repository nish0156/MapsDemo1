package dk.tec.googlemapdemo;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final int FINE_LOCATION_PERMISSION_REQUEST = 1;
    private GoogleMap gMap;

    private LocationDbHelper dbHelper;

    private Button btn_save;


    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new LocationDbHelper(this);

        // Asking for permissions
        permissionsList = new ArrayList<>();
        permissionsList.addAll(Arrays.asList(permissionsStr));
        askForPermissions(permissionsList);

        // Setting up the map
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(view -> {
            saveLocationToDb(currentLocation);
            Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show();
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
                LatLng locationName = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                gMap.moveCamera(CameraUpdateFactory.newLatLng(locationName));


                // super.onLocationResult(locationResult);

            }
        }, Looper.myLooper());
    }





    //region Permission

    // ArrayList to store permissions
    ArrayList<String> permissionsList;
    // Array of permissions
    String[] permissionsStr = {Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };


    int permissionsCount = 0;
    // Activity result launcher for requesting permissions
    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @Override
                        public void onActivityResult(Map<String,Boolean> result) {
                            ArrayList<Boolean> list = new ArrayList<>(result.values());
                            permissionsList = new ArrayList<>();
                            permissionsCount = 0;
                            for (int i = 0; i < list.size(); i++) {
                                if (shouldShowRequestPermissionRationale(permissionsStr[i])) {
                                    permissionsList.add(permissionsStr[i]);
                                } else if (!hasPermission(MainActivity.this, permissionsStr[i])) {
                                    permissionsCount++;
                                }
                            }
                            if (permissionsList.size() > 0) {
                                // Some permissions are denied and can be asked again.
                                askForPermissions(permissionsList);
                            } else if (permissionsCount > 0) {
                                // Show alert dialog
                                showPermissionDialog();
                            } else {
                                // All permissions granted. Do your stuff
                                getUpdates();
                            }

                        }
                    });

    // Method to check if permission is granted
    private boolean hasPermission(Context context, String permissionStr) {
        return ContextCompat.checkSelfPermission(context, permissionStr) == PackageManager.PERMISSION_GRANTED;
    }

    // Method to ask for permissions
    private void askForPermissions(ArrayList<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        for (int i = 0; i < newPermissionStr.length; i++) {
            newPermissionStr[i] = permissionsList.get(i);
        }
        if (newPermissionStr.length > 0) {
            permissionsLauncher.launch(newPermissionStr);
        } else {
            // User has pressed 'Deny & Don't ask again' so we have to show the enable permissions dialog
            // which will lead them to app details page to enable permissions from there.
            showPermissionDialog();
        }
    }

    // AlertDialog to show permission dialog
    AlertDialog alertDialog;
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission required")
                .setMessage("Some permissions are needed to be allowed to use this app without any problems.")
                .setPositiveButton("Continue", (dialog, which) -> {
                    dialog.dismiss();
                });
        if (alertDialog == null) {
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    //endregion

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        if(currentLocation!= null){
            double latitude = currentLocation.getLatitude();
            LatLng locationName = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            gMap.addMarker(new MarkerOptions().position(locationName).title("Location"));
            gMap.moveCamera(CameraUpdateFactory.newLatLng(locationName));
            Log.d("Map ready", "Map is ready"+locationName.toString());
        } else{
            Log.d("Map ready", "Map is ready but location is null");
        }


    }

}