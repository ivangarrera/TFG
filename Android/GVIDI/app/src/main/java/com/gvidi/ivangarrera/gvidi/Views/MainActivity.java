package com.example.ivangarrera.example.Views;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivangarrera.example.Controller.BluetoothConnectionThread;
import com.example.ivangarrera.example.Controller.ExpeditionManagementController;
import com.example.ivangarrera.example.Controller.GuideManagementController;
import com.example.ivangarrera.example.Controller.ParticipantManagementController;
import com.example.ivangarrera.example.Controller.ReceiveBTMessageHandler;
import com.example.ivangarrera.example.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;


// Follow tutorial: https://developer.android.com/guide/topics/connectivity/bluetooth#Permissions

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private static final String TAG = "GVIDI";
    private static final int MENU_CREATE_EXPEDITION = Menu.FIRST;
    private static final int MENU_FINALIZE_EXPEDITION = Menu.FIRST + 1;
    private static final int MENU_JOIN_EXPEDITION = Menu.FIRST + 2;
    private static final int MENU_LEAVE_EXPEDITION = Menu.FIRST + 3;
    private static final int MENU_SHOW_ADVANCED_EXPEDITION = Menu.FIRST + 4;
    private static final int MENU_LOGOUT = Menu.FIRST + 5;
    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // MAC-address of Bluetooth module. This value depends on HC-06 device
    private String macAddress = null;
    public static boolean b_changedOutside;
    public static boolean b_canCreateExpedition, b_canCancelExpedition, b_canJoinExpedition, b_canLeaveExpedition;
    public static String expedition_name;
    public static String participant_selected_to_see_details;
    private Semaphore mutex;
    private TextView lbl_coordinates;
    private ImageView img_sos, img_stop;
    private ListView lv_bluetooth;
    private RelativeLayout rl_main;
    private RelativeLayout rl_bluetooth;
    private Handler handler;
    private GoogleMap mMap;
    private BluetoothConnectionThread mConnectedThread;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

    // This receiver is fired when the battery level changes
    private BroadcastReceiver battery_info_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int battery_level = intent.getIntExtra("level", 0);
            try {
                mutex.acquire(1);
                if (b_canCreateExpedition || b_canCancelExpedition) {
                    GuideManagementController guideManagement = new GuideManagementController(null);
                    guideManagement.updateBattery(expedition_name, battery_level);
                } else if (b_canJoinExpedition || b_canLeaveExpedition) {
                    ParticipantManagementController participantManagement = new ParticipantManagementController(null);
                    participantManagement.updateBattery(expedition_name, battery_level);
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            } finally {
                mutex.release(1);
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mutex = new Semaphore(1, true);
        setResult(0, null);

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ex) {
            Log.e(TAG, ex.getMessage());
        }

        lbl_coordinates = findViewById(R.id.label_coord_value);
        img_sos = findViewById(R.id.circle_sos);
        img_stop = findViewById(R.id.circle_stop);
        rl_main = findViewById(R.id.rl_main);
        rl_bluetooth = findViewById(R.id.rl_bluetooth);
        lv_bluetooth = findViewById(R.id.lv_bluetooth);

        // This listener will be called when there is a change in the user session
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // The user has been correctly logged out
                if (user == null) {
                    try {
                        btSocket.close();
                        mGoogleApiClient.disconnect();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    try {
                        mutex.acquire(1);
                        b_canCreateExpedition = false;
                        b_canCancelExpedition = false;
                        b_canJoinExpedition = false;
                        b_canLeaveExpedition = false;
                        expedition_name = null;
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    } finally {
                        mutex.release(1);
                    }
                    startActivityForResult(
                            new Intent(MainActivity.this, SignupActivity.class),
                            1);
                }
            }
        });

        img_sos.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onClick(View v) {
                img_sos.setBackground(getApplicationContext().getDrawable(R.drawable.round_shape));
                img_sos.setBackgroundTintList(ColorStateList.valueOf(getApplicationContext().
                        getResources().getColor(R.color.colorLogoLight)));
            }
        });

        img_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onClick(View v) {
                img_stop.setBackground(getApplicationContext().getDrawable(R.drawable.round_shape));
                img_stop.setBackgroundTintList(ColorStateList.valueOf(getApplicationContext().
                        getResources().getColor(R.color.colorLogoLight)));
            }
        });

        lv_bluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String label = (String) parent.getItemAtPosition(position);
                String[] words = label.split(" ");
                try {
                    btSocket = createBluetoothSocket(btAdapter.getRemoteDevice(words[1]));
                    btSocket.connect();
                } catch (Exception ex) {

                }
                if (btSocket.isConnected()) {
                    try {
                        btSocket.close();
                        btSocket = null;
                    } catch (Exception ex) {

                    }
                    macAddress = words[1];
                } else {
                    btAdapter.disable();
                    btSocket = null;
                }
                Log.e(TAG, macAddress == null ? "NULL" : macAddress);
                rl_bluetooth.setVisibility(View.GONE);
                rl_main.setVisibility(View.VISIBLE);
                onResume();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        try {
            mapFragment.getMapAsync(this);
        } catch (NullPointerException ex) {
            errorExit(TAG, "Error loading maps");
        }

        // Register the receiver that will be fired when the intent ACTION_BATTERY_CHANGED is fired
        this.registerReceiver(this.battery_info_receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        GuideManagementController guideManagement = new GuideManagementController(mutex);
        ParticipantManagementController participantManagement = new ParticipantManagementController(mutex);
        participantManagement.checkIfParticipantIsJoined();

        guideManagement.checkIfGuide();

        handler = new ReceiveBTMessageHandler(getApplicationContext(), this);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Get the bluetooth adapter. If it is not enabled, ask to enable it

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                try {
                    mutex.acquire(1);
                    if (b_canCreateExpedition || b_canCancelExpedition) {
                        Log.v(TAG, marker.getTitle());

                        if (!marker.getTag().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            participant_selected_to_see_details = marker.getTag().toString();
                            startActivity(new Intent(MainActivity.this, AdminParticipantDetailsActivity.class));
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                } finally {
                    mutex.release(1);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (macAddress == null) {
            checkBTState();
        } else {

            if (btAdapter.isEnabled()) {
                BluetoothDevice device = btAdapter.getRemoteDevice(macAddress);

                try {
                    // Create the socket that is going to show the information on the screen
                    btSocket = createBluetoothSocket(device);

                    // Cancel device discovery, because it is resource intensive.
                    btAdapter.cancelDiscovery();

                    // Establish the connection via bluetooth socket.  This will block until it connects.
                    btSocket.connect();
                    Log.e(TAG, "Connected");
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());

                    // https://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3
                    try {
                        Log.e(TAG, "Trying fallback...");
                        btSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 2);
                        btSocket.connect();

                    } catch (Exception e2) {
                        Log.e(TAG, "Couldnt connect...");
                    }
                }

                if (btSocket == null) {
                    Log.e(TAG, "btSocket es null");
                }
                if (handler == null) {
                    Log.e(TAG, "handler es null");
                }
                if (btSocket != null && handler != null) {
                    if (mConnectedThread == null || !mConnectedThread.isAlive()) {
                        mConnectedThread = new BluetoothConnectionThread(btSocket, handler);
                        mConnectedThread.start();
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            btSocket.close();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(battery_info_receiver);
        try {
            btSocket.close();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 0) {
            this.finish();
        }
        if (requestCode == 3 && resultCode == RESULT_OK) { // bluetooth adapter enabled
            // Search for paired devices
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                List<String> list = new ArrayList<>();
                for (BluetoothDevice device : pairedDevices) {
                    list.add("MAC: " + device.getAddress() + " Nombre: " + device.getName());
                }
                rl_main.setVisibility(View.GONE);
                rl_bluetooth.setVisibility(View.VISIBLE);
                lv_bluetooth.setAdapter(new ArrayAdapter<>(this, R.layout.bluetooth_item, R.id.tv_bluetooth, list));
            }
        } else if (requestCode == 3 && resultCode == RESULT_CANCELED) {
            errorExit("Fatal Error", "Esta aplicación no funciona si el Bluetooth no está activado");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        try {
            mutex.acquire(1);
            if (b_canCreateExpedition)
                menu.add(0, MENU_CREATE_EXPEDITION, Menu.NONE, R.string.menu_create_expedition);
            if (b_canCancelExpedition)
                menu.add(0, MENU_FINALIZE_EXPEDITION, Menu.NONE, R.string.menu_finalize_expedition);
            if (b_canJoinExpedition)
                menu.add(0, MENU_JOIN_EXPEDITION, Menu.NONE, R.string.menu_join_expedition);
            if (b_canLeaveExpedition)
                menu.add(0, MENU_LEAVE_EXPEDITION, Menu.NONE, R.string.menu_leave_expedition);

            if (b_canCreateExpedition || b_canCancelExpedition)
                menu.add(0, MENU_SHOW_ADVANCED_EXPEDITION, Menu.NONE, R.string.menu_show_expedition_advanced);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            mutex.release(1);
        }
        menu.add(0, MENU_LOGOUT, Menu.NONE, R.string.menu_logout);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case MENU_SHOW_ADVANCED_EXPEDITION:
                startActivity(new Intent(MainActivity.this, AdminExpeditionDetailsActivity.class));
                return true;
            case MENU_LOGOUT:
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signOut();
                return true;
            case MENU_JOIN_EXPEDITION:
                startActivity(new Intent(MainActivity.this, JoinExpeditionActivity.class));
                return true;
            case MENU_LEAVE_EXPEDITION:
                ParticipantManagementController participantManagement = new ParticipantManagementController(null);
                try {
                    mutex.acquire(1);
                    b_canLeaveExpedition = false;
                    b_canJoinExpedition = true;
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                } finally {
                    mutex.release(1);
                }
                participantManagement.removeParticipant(expedition_name);
                return true;
            case MENU_CREATE_EXPEDITION:
                startActivity(new Intent(MainActivity.this, CreateExpeditionActivity.class));
                return true;
            case MENU_FINALIZE_EXPEDITION:
                if (!expedition_name.isEmpty()) {
                    ExpeditionManagementController expeditionManagement = new ExpeditionManagementController();
                    expeditionManagement.closeExpedition(expedition_name);
                    try {
                        mutex.acquire(1);
                        b_canCancelExpedition = false;
                        b_canCreateExpedition = true;
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    } finally {
                        mutex.release(1);
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            errorExit("ERROR", "Debes permitir el acceso a la localización");
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLocation = location;
                            double latitude = mLocation.getLatitude();
                            double longitude = mLocation.getLongitude();
                            Log.i(TAG, "Latitude = " + latitude + "\nLongitude = " + longitude);
                        } else {
                            startLocationUpdates();
                        }
                    }
                });
        // mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(20000).setFastestInterval(20000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            errorExit("ERROR", "Debes permitir el acceso a la localización");
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);

                        for (Location location : locationResult.getLocations()) {
                            if (b_canCreateExpedition || b_canCancelExpedition) {
                                GuideManagementController guideManagement = new GuideManagementController(null);
                                if (expedition_name != null) {
                                    guideManagement.updatePosition(expedition_name,
                                            String.valueOf(location.getLatitude()),
                                            String.valueOf(location.getLongitude()));
                                    guideManagement.calculateInstantaneousPace(expedition_name, MainActivity.this);
                                }
                            } else if (b_canJoinExpedition || b_canLeaveExpedition) {
                                ParticipantManagementController participantManagement = new ParticipantManagementController(null);
                                if (expedition_name != null) {
                                    participantManagement.updatePosition(expedition_name,
                                            String.valueOf(location.getLatitude()),
                                            String.valueOf(location.getLongitude()));
                                    participantManagement.calculateInstantaneousPace(expedition_name, MainActivity.this);
                                }
                            }
                            lbl_coordinates.setText(String.format("(%f, %f)", location.getLatitude(), location.getLongitude()));

                            if (expedition_name != null) {
                                ExpeditionManagementController expeditionManagement = new ExpeditionManagementController();
                                expeditionManagement.updateMap(expedition_name, mMap);
                            }
                        }
                    }
                }, null);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "GPS Connection failed: " + connectionResult.getErrorMessage());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        BluetoothSocket my_rfcomm_socket;
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, MY_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }

        // If it is not possible to create an insecure RFCOMM socket, create a secure one
        my_rfcomm_socket = device.createRfcommSocketToServiceRecord(MY_UUID);
        return my_rfcomm_socket;
    }

    private void checkBTState() {
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth is not supported");
        } else {
            if (!btAdapter.isEnabled()) {
                // Ask user to turn on bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 3);
            }

            if (btAdapter.isEnabled() && macAddress == null) {
                // Search for paired devices
                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    List<String> list = new ArrayList<>();
                    for (BluetoothDevice device : pairedDevices) {
                        list.add("MAC: " + device.getAddress() + " Nombre: " + device.getName());
                    }
                    rl_main.setVisibility(View.GONE);
                    rl_bluetooth.setVisibility(View.VISIBLE);
                    lv_bluetooth.setAdapter(new ArrayAdapter<>(this, R.layout.bluetooth_item, R.id.tv_bluetooth, list));
                }
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }
}
