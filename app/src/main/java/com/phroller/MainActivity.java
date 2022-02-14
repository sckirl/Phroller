gitgit package com.phroller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // UI elements in variables
    Button xBtn, yBtn, aBtn, bBtn;
    Button topBtn, bottomBtn, leftBtn, rightBtn;
    Button connectBtn;
    TextView vals;
    TextInputEditText IPinput;
    TextInputEditText PORTinput;

    // Variables to access socket
    boolean connected = false;
    private String IP;
    private int PORT;
    PrintWriter output;
    Thread sendThread;

    // Variables to access bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private ListView listView;
    private Set<BluetoothDevice> pairedDevices;
    private HashMap<String, String> nameMAC;
    private Thread accept;

    // Variables to read Accelerometer data
    private Sensor accelerometer;
    private SensorManager sensorManager;
    private float X, Y, Z = 0;
    private float prevY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // -- Bind UI to here --
        bindUI(); // Bind each UI components to here
        bindButtons(); // Bind each Buttons here

        // Enable Bluetooth when user presses "CONNECT" button
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // BTconnect();
                if (!IPinput.getText().toString().equals("")) {
                    socketConnect();

                } else
                    // IP is empty
                    Toast.makeText(getApplicationContext(), "Get server IP from the desktop app", Toast.LENGTH_LONG).show();
            }
        });

        // Check for accelerometer in user's device
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(getApplicationContext(), "Accelerometer not available", Toast.LENGTH_LONG).show();
        }
    }

    // -- Bind all UI stuff to here --
    public void bindUI(){
        // rightmost buttons from UI (x, y, a, b)
        xBtn = findViewById(R.id.xBtn);
        yBtn = findViewById(R.id.yBtn);
        aBtn = findViewById(R.id.aBtn);
        bBtn = findViewById(R.id.bBtn);

        // leftmost buttons from UI (top, bottom, left, right)
        topBtn = findViewById(R.id.top);
        bottomBtn = findViewById(R.id.bottom);
        leftBtn = findViewById(R.id.left);
        rightBtn = findViewById(R.id.right);

        // socket inputs
        IPinput = findViewById(R.id.IPinput);
        PORTinput = findViewById(R.id.PORTinput);

        connectBtn = findViewById(R.id.connectBtn);    // connect button
        listView = findViewById(R.id.listView);   // listView for bluetooth devices
        vals = findViewById(R.id.vals);     // display of current accelerometer values
    }

    @SuppressLint("ClickableViewAccessibility")
    public void bindButtons() {
        // - XYAB Buttons -
        xBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (connected) {
                    Thread send = new Thread(new SendThread("X"));
                    send.start();
                } return false;
            }
        });

        yBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (connected) {
                    Thread send = new Thread(new SendThread("Y"));
                    send.start();
                } return false;
            }
        });

        aBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (connected) {
                    Thread send = new Thread(new SendThread("A"));
                    send.start();
                } return false;
            }
        });

        bBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (connected) {
                    Thread send = new Thread(new SendThread("B"));
                    send.start();
                } return false;
            }
        });

        // TOP, BOTTOM, LEFT, RIGHT Buttons

        topBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (connected) {
                    Thread send = new Thread(new SendThread("TOP"));
                    send.start();
                } return false;
            }
        });

        bottomBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (connected) {
                    Thread send = new Thread(new SendThread("BOT"));
                    send.start();
                } return false;
            }
        });

        leftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (connected) {
                    Thread send = new Thread(new SendThread("LFT"));
                    send.start();
                } return false;
            }
        });

        rightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (connected) {
                    Thread send = new Thread(new SendThread("RGT"));
                    send.start();
                } return false;
            }
        });
    }

    // ------------------------------- FOR SOCKET CONNECTION -------------------------------

    public void socketConnect() { // Call this after user click the "Connect" button
        // Connect to the given server IP
        IP = IPinput.getText().toString().trim();
        PORT = Integer.parseInt(PORTinput.getText().toString().trim());

        Thread connectThread = new Thread(new ConnectThread(IP, PORT));
        connectThread.start(); // connect

        Toast.makeText(getApplicationContext(), "Connecting to Socket", Toast.LENGTH_LONG).show();
    }

    public class TestThread implements Runnable{
        @Override
        public void run(){
            while (true) {
                String val = String.valueOf(Y);
                output.write(val + "\n");
            }
        }
    }

    // Start a new thread for connecting to socket
    public class ConnectThread implements Runnable{
        String _serverIP;
        int _serverPORT;

        public ConnectThread(String serverIP, int serverPORT){
            _serverIP = serverIP;
            _serverPORT = serverPORT;
        }

        @Override
        public void run() {
            Socket socket;
            try {
                // initialize socket, input, and output variables
                socket = new Socket(_serverIP, _serverPORT);
                output = new PrintWriter(socket.getOutputStream());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Connection successful
                        connected = true;
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                    }
                });

                // Exceptions...
            } catch (SocketTimeoutException e) {
                Toast.makeText(getApplicationContext(), "Connection Timeout", Toast.LENGTH_LONG).show();
                Log.d("_debug", "Connection Timeout", e);

            } catch (java.net.NoRouteToHostException e) {
                Toast.makeText(getApplicationContext(), "Server Disconnected", Toast.LENGTH_LONG).show();
                Log.d("_debug", "Server Disconnected", e);

            } catch (UnknownHostException e) {
                Toast.makeText(getApplicationContext(), "Socket Connection Failed", Toast.LENGTH_LONG).show();
                Log.d("_debug", "Socket Connection Failed", e);

            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Socket Connection Failed", Toast.LENGTH_LONG).show();
                Log.d("_debug", "Socket Connection Failed", e);
            }
        }
    }

    // Start new thread for sending Accelerometer Values
    public class SendThread implements Runnable{
        String val;

        SendThread(String inp) {
            this.val = inp;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @SuppressLint("DefaultLocale")
        @Override
        public void run() { // Call this in OnSensorChanged()
            // Constantly write XYZ value into socket server
            if (val.length() > 5) {
                val = val.substring(0, 6);

                output.write(val + "\n");
                output.flush();
            }

            if (Character.isAlphabetic(val.charAt(0))){
                output.write(val + "\n");
                output.flush();
            }

            // sendThread.interrupt();
        }
    }

    // ------------------------------ FOR READING ACCELEROMETER ------------------------------

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Read accelerometer values (X, Y, Z)
        X = sensorEvent.values[0];
        Y = sensorEvent.values[1];
        Z = sensorEvent.values[2];

        // display it in "vals" TextView
        displayAccelerometerValues();

        if (connected) {
            // Get ready to send values
            sendThread = new Thread(new SendThread(String.valueOf(Y)));
            sendThread.start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {/* Do nothing */}

    @SuppressLint("DefaultLocale")
    public void displayAccelerometerValues() {
        vals.setText(String.format("X: %f Y: %f Z: %f",
                X,    Y,    Z));
    }

}

// ********************* Failed/Unfinished Bluetooth Connection Feature *********************
/*
    // ------------------------------ FOR BLUETOOTH CONNECTION ------------------------------

    // Since onActivityResult was depreciated
    // This part uses code from: https://stackoverflow.com/a/63654043
    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // doSomeOperations();
                        bluetoothAdapter.enable();
                        discoverDevices();
                    }
                }
            });

    public void BTconnect() {
        // Called when "Connect" button is pressed
        if (!bluetoothAdapter.isEnabled()){
            // enable bluetooth if it isn't already
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityResult.launch(intent);

            // Start to accept nearby bluetooth connection(s)
            accept.start();

            Toast.makeText(getApplicationContext(), "Bluetooth on", Toast.LENGTH_LONG).show();
        } else {
            // Disable bluetooth if it's already on
            bluetoothAdapter.disable();

            accept.interrupt();
            Toast.makeText(getApplicationContext(), "Bluetooth off", Toast.LENGTH_LONG).show();
        }
    }

    public void discoverDevices() {
        pairedDevices = bluetoothAdapter.getBondedDevices();
        nameMAC= new HashMap<String, String>();

        ArrayList<String> devices = new ArrayList<String>();

        for (BluetoothDevice device : pairedDevices) {
            devices.add(device.getName());
            nameMAC.put(device.getName(), device.getAddress());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devices);
        listView.setAdapter(adapter);

        getClickedDevice();
    }

    public void getClickedDevice(){
        // Get clicked item in listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int pos, long id) {
                String UserInfo = listView.getItemAtPosition(pos).toString();
            }
        });
    }

    // ------------------------------ NEW THREAD TO ACCEPT BLUETOOTH CONNECTION ------------------------------
    private class AcceptThread extends Thread {
        // Accept incoming connection from other device
        private final BluetoothServerSocket serverSocket;
        private final UUID UUID =  java.util.UUID.fromString("101010");

        public AcceptThread() {
            BluetoothServerSocket temp = null;
            try {
                temp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("LAPTOP", UUID);
            } catch (IOException e) {
                Log.d("_debug", "listen() method failed", e);
            }
            serverSocket = temp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until socket is returned
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.d("_debug", "accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    try {
                        serverSocket.close();
                        Log.d("_debug", "Connection Successful");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();

            } catch (IOException e) {
                Log.d("_debug", "Could not close socket", e);
            }
        }
    }
    */