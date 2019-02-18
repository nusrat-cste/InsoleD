package com.example.nusra.insoledevice2;

        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.content.pm.PackageManager;
        import android.os.Build;
        import android.support.annotation.NonNull;
        import android.util.Log;
        import android.Manifest;
        import android.view.View;
        import android.widget.TextView;
        import android.widget.Toast;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;

        import java.util.Calendar;


        import java.util.ArrayList;

        import hk.advanpro.android.sdk.AdvanproAndroidSDK;
        import hk.advanpro.android.sdk.commons.AConfig;
        import hk.advanpro.android.sdk.device.Device;
        import hk.advanpro.android.sdk.device.ble.BLEDeviceManager;
        import hk.advanpro.android.sdk.device.ble.BLEInsoleDevice;
        import hk.advanpro.android.sdk.device.callback.DeviceCallbackException;
        import hk.advanpro.android.sdk.device.callback.DeviceConnectCallback;
        import hk.advanpro.android.sdk.device.callback.DeviceManagerScanCallback;
        import hk.advanpro.android.sdk.device.callback.MainThreadDeviceEventCallback;
        import hk.advanpro.android.sdk.device.enumeration.ConnectType;
        import hk.advanpro.android.sdk.device.result.DeviceConnectResult;
        import hk.advanpro.android.sdk.device.result.ble.BLEDeviceScanResult;
        import hk.advanpro.android.sdk.device.result.ble.insole.BLEInsoleRealGaitEventResult;
        import hk.advanpro.android.sdk.device.result.ble.insole.BLEInsoleRealStepEventResult;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Insole" ;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION= 241;
    private ArrayList<BLEDeviceScanResult> Devices = new ArrayList<>();
    private ArrayList<Device> _ConnectedDevices = new ArrayList<>();
    public Device r_device;
    public Device l_device;
    TextView connectdevices;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Calendar _calender;
    String lName;
    String rName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing the sdk
        AdvanproAndroidSDK.init(getApplicationContext());
        //set advanpro android sdk config
        AConfig config = AdvanproAndroidSDK.getConfig();
        //enable print debug log
        config.setDebugLog(true);
        AdvanproAndroidSDK.setConfig(config);
        // end of Initializing the sdk
        connectdevices=findViewById(R.id.tv_connection_prompt);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }

        boolean enable = AdvanproAndroidSDK.getDeviceManager(BLEDeviceManager.class).isEnable(ConnectType.BLE);
        final BLEDeviceManager manager = AdvanproAndroidSDK.getDeviceManager(BLEDeviceManager.class);

        try {
            manager.scan(5, new DeviceManagerScanCallback<BLEDeviceScanResult>(){
                @Override
                public void onScanning(BLEDeviceScanResult result) {
                    Log.d(TAG, String.format("Detected Device Name: %s ",result.getRecord().getLocalName()));
                    //Return only insoles device type
                    //                Log.d(TAG, String.format("Detected Device Name: %s, Address: %s,Type:%s",
                    //                        result.getRecord().getLocalName(), result.getRecord().getAddress(),
                    //                            result.getRecord().getManufacturer().getType()));

                    if(result.getRecord().getManufacturer().getType().toString().equals("Insole")){
                        Devices.add(result);
                    }
                }
                @Override
                public void onStop() {

                    Log.d(TAG, "stop scan");
                    l_device = Devices.get(1).create();
                    if(Devices.get(0).getRecord().getLocalName().equals("6X1CSV"))
                    {
                        r_device = Devices.get(0).create();
                        rName = "6X1CSV";
                        Log.d("Left Right", "Right sole is: " + Devices.get(0).getRecord().getLocalName());
                        writeToDB(Devices.get(0).getRecord().getLocalName(),Devices.get(0).getRecord().getAddress(),"R");
                    }
                    else if(Devices.get(0).getRecord().getLocalName().equals("BBJE8I"))
                    {
                        l_device = Devices.get(0).create();
                        lName = "BBJE8I";
                        Log.d("Left Right", "Left sole is: " + Devices.get(0).getRecord().getLocalName());
                        writeToDB(Devices.get(0).getRecord().getLocalName(),Devices.get(0).getRecord().getAddress(),"L");
                    }


                    if(Devices.get(1).getRecord().getLocalName().equals("BBJE8I"))
                    {
                        l_device = Devices.get(1).create();
                        lName = "BBJE8I";
                        Log.d("Left Right", "Left sole is: " + Devices.get(1).getRecord().getLocalName());
                        writeToDB(Devices.get(1).getRecord().getLocalName(),Devices.get(1).getRecord().getAddress(),"L");
                    }
                    else if(Devices.get(1).getRecord().getLocalName().equals("6X1CSV"))
                    {
                        r_device = Devices.get(1).create();
                        rName = "6X1CSV";
                        Log.d("Left Right", "Right sole is: " + Devices.get(1).getRecord().getLocalName());
                        writeToDB(Devices.get(1).getRecord().getLocalName(),Devices.get(1).getRecord().getAddress(),"R");
                    }

                    connectdevices.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            connectToDevices();
                        }
                    });
                }
                @Override
                public void onError(DeviceCallbackException error) {
                    Log.d(TAG, error.getCause().getMessage());
                    error.printStackTrace();
                    //handler err...
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            onStop();
        }


    }

    private void connectToDevices() {
//        final Intent intent = (new Intent(getApplicationContext(),ConnectedDeviceActivity.class));
//        final Bundle bundle = new Bundle();

        if(r_device.isConnected()==false)
        {
            r_device.connect(new DeviceConnectCallback() {
                @Override
                public void onSucceed(Device device, DeviceConnectResult result) {
                    Log.d(TAG,"The connection is successful");
                    _ConnectedDevices.add(r_device);

//                    MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult> callback = new MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult>() {
//                        @Override
//                        public void onMainThreadData(Device device,BLEInsoleRealStepEventResult data)
//                        {
//                            Log.d("InsoleD","stepcount Device1"+ data.getWalkStep());
//                        }
//                    };
//                    r_device.on(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, callback);

                    //gets the data from the device like step counts...
                    MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult> cb = new MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult>() {
                        @Override
                        public void onMainThreadData(Device device,BLEInsoleRealStepEventResult data)
                        {
                            writeToDB(lName,data.getWalkStep(),data.getWalkDuration(), data.getRunStep(),data.getRunDuration(),data.getGait());
                            Log.d(TAG,"step "+data.getWalkStep()+" gait "+data.getGait()+" isrun "+data.getIsRun());
                            Log.d(TAG,"status is "+data.getStatus());
                        }
                    };
                    r_device.on(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, cb);

                    MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult> cb2 = new MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult>() {
                        @Override
                        public void onMainThreadData(Device device,BLEInsoleRealGaitEventResult data)
                        {

                            writeToDB(rName,data.getTouchA(),data.getTouchB(),
                                    data.getTouchC(), data.getTouchD(), data.getEctropion(),
                                    data.getForefoot(), data.getHeel(),
                                    data.getSole(), data.getVarus(), data.getImpact());

                            Log.d(TAG,"step A "+data.getTouchA()+" B "+data.getTouchB()+" C "+data.getTouchC()+" D "+data.getTouchD());
                            Log.d(TAG,"step "+data.getVarus()+" forefoot "+data.getForefoot()+" sole "+data.getSole());
                        }
                    };
                    r_device.on(BLEInsoleDevice.EVENT_INSOLE_REAL_GAIT, cb2);
//Cancel to monitor
                    //device.un(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, callback);
                    if(l_device.isConnected()){
                        //startActivity(intent);
                        connectdevices.setText("Connected");
                    }
                }

                @Override
                public void onError(Device device, DeviceCallbackException e) {
                    Log.d(TAG,"The connection failed");
                }
            });
        }
        else if(r_device.isConnected()){
            Log.d(TAG,"r_device is already connected");
        }

        if(l_device.isConnected()==false)
        {
            l_device.connect(new DeviceConnectCallback() {
                @Override
                public void onSucceed(Device device, DeviceConnectResult result) {
                    Log.d(TAG,"The connection is successful");
                    _ConnectedDevices.add(l_device);

                    MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult> callback = new MainThreadDeviceEventCallback<BLEInsoleRealStepEventResult>() {
                        @Override
                        public void onMainThreadData(Device device,BLEInsoleRealStepEventResult data)
                        {
                            Log.d("InsoleD","stepcount Device1"+ data.getWalkStep());
                            writeToDB(rName,data.getWalkStep(),data.getWalkDuration(), data.getRunStep(),data.getRunDuration(),data.getGait());

                        }
                    };
                    l_device.on(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, callback);

                    MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult> cb2 = new MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult>() {
                        @Override
                        public void onMainThreadData(Device device,BLEInsoleRealGaitEventResult data)
                        {

                            writeToDB(lName,data.getTouchA(),data.getTouchB(),
                                    data.getTouchC(), data.getTouchD(), data.getEctropion(),
                                    data.getForefoot(), data.getHeel(),
                                    data.getSole(), data.getVarus(), data.getImpact());
                            Log.d(TAG,"step A "+data.getTouchA()+" B "+data.getTouchB()+" C "+data.getTouchC()+" D "+data.getTouchD());
                            Log.d(TAG,"step "+data.getVarus()+" forefoot "+data.getForefoot()+" sole "+data.getSole());
                        }
                    };
                    l_device.on(BLEInsoleDevice.EVENT_INSOLE_REAL_GAIT, cb2);

                    if(r_device.isConnected()){
                        //startActivity(intent);
                        connectdevices.setText("Connected");
                    }
                }

                @Override
                public void onError(Device device2, DeviceCallbackException error) {
                    Log.d(TAG, "The connection failed");
                    // The connection is fails...
                }
            });
        }
        else if(l_device.isConnected()){
            Log.d(TAG,"l_device is already connected");
//                        l_device.on(DefaultBLEDevice.EVENT_BATTERY_CHANGE, new DeviceEventCallback() {
//                            @Override
//                            public void onData(Device device,DeviceEventResult data) {
//                                Log.d(TAG, "Electricity changes： "+device.getInfo(DefaultBLEDevice.BLE_DEVICE_INFO_BATTERY));
//                                //Note: update the UI need to switch to the main thread
//                            }
//                        });
        }
    }

    private void writeToDB(String SoleName,String SoleMac, String Key) {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Insole");
        if (Key.equals("L")){
            myRef.child("Left").child("DeviceName").setValue(SoleName);
            myRef.child("Left").child("MacAddress").setValue(SoleMac);
        }
        else if(Key.equals("R")){
            myRef.child("Right").child("DeviceName").setValue(SoleName);
            myRef.child("Right").child("MacAddress").setValue(SoleMac);}
    }

    private void writeToDB(String SoleName,int wc, int wd, int rc, int rd, int gait) {

        Calendar  _time= _calender.getInstance();
        database = FirebaseDatabase.getInstance();
        Long _tStamp = _time.getTimeInMillis();
        Log.e("Insole",_tStamp.toString());

        myRef = database.getReference("Data").child(SoleName).child("StepData");

        myRef.child(_tStamp.toString()).child("WalkCount").setValue(wc);
        myRef.child(_tStamp.toString()).child("WalkDuration").setValue(wd);
        myRef.child(_tStamp.toString()).child("RunCount").setValue(rc);
        myRef.child(_tStamp.toString()).child("RunDuration").setValue(rd);
        myRef.child(_tStamp.toString()).child("Gait").setValue(gait);

//        myRef.child("Right").child("MacAddress").setValue(SoleMac);
    }

    private void writeToDB(String SoleName,Boolean _touchA, Boolean _touchB,
                           Boolean _touchC, Boolean _touchD, int ectropion,
                           int forefoot, int heel, int sole, int varus, int impact) {

        Calendar  _time= _calender.getInstance();
        database = FirebaseDatabase.getInstance();
        Long _tStamp = _time.getTimeInMillis();
        Log.e("Insole",_tStamp.toString());

        myRef = database.getReference("Data").child(SoleName).child("GaitData");

        myRef.child(_tStamp.toString()).child("ectropion").setValue(ectropion);
        myRef.child(_tStamp.toString()).child("forefoot").setValue(forefoot);
        myRef.child(_tStamp.toString()).child("heel").setValue(heel);
        myRef.child(_tStamp.toString()).child("impact").setValue(impact);
        myRef.child(_tStamp.toString()).child("sole").setValue(sole);
        myRef.child(_tStamp.toString()).child("varus").setValue(varus);
        myRef.child(_tStamp.toString()).child("touchA").setValue(_touchA);
        myRef.child(_tStamp.toString()).child("touchB").setValue(_touchB);
        myRef.child(_tStamp.toString()).child("touchC").setValue(_touchC);
        myRef.child(_tStamp.toString()).child("touchD").setValue(_touchD);

//        myRef.child("Right").child("MacAddress").setValue(SoleMac);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),"Permission granted! Bluetooth device scan started",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),"Permission denied,this application requires the location permission to perform the scan",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}



//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.Manifest;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.util.ArrayList;
//
//import hk.advanpro.android.sdk.AdvanproAndroidSDK;
//import hk.advanpro.android.sdk.commons.AConfig;
//import hk.advanpro.android.sdk.commons.ALog;
//import hk.advanpro.android.sdk.device.Device;
//import hk.advanpro.android.sdk.device.ble.BLEDevice;
//import hk.advanpro.android.sdk.device.ble.BLEDeviceManager;
//import hk.advanpro.android.sdk.device.ble.BLEInsoleDevice;
//import hk.advanpro.android.sdk.device.callback.DeviceCallbackException;
//import hk.advanpro.android.sdk.device.callback.DeviceConnectCallback;
//import hk.advanpro.android.sdk.device.callback.DeviceEventCallback;
//import hk.advanpro.android.sdk.device.callback.DeviceManagerScanCallback;
//import hk.advanpro.android.sdk.device.callback.MainThreadDeviceCommandCallback;
//import hk.advanpro.android.sdk.device.callback.MainThreadDeviceEventCallback;
//import hk.advanpro.android.sdk.device.enumeration.ConnectType;
//import hk.advanpro.android.sdk.device.params.ble.insole.BLEInsoleEnableAccCommandParams;
//import hk.advanpro.android.sdk.device.result.DeviceConnectResult;
//import hk.advanpro.android.sdk.device.result.ble.BLEDeviceScanResult;
//import hk.advanpro.android.sdk.device.result.ble.insole.BLEInsoleEnableAccCommandResult;
//import hk.advanpro.android.sdk.device.result.ble.insole.BLEInsoleRealAccEventResult;
//import hk.advanpro.android.sdk.device.result.ble.insole.BLEInsoleRealGaitEventResult;
//
//public class MainActivity extends AppCompatActivity {
//
//    private static final String TAG = "Insole" ;
//    private static final int PERMISSION_REQUEST_COARSE_LOCATION= 241;
//    private ArrayList<BLEDeviceScanResult> Devices = new ArrayList<>();
//    private ArrayList<Device> _ConnectedDevices = new ArrayList<>();
//    public Device device1;
//    public Device device2;
//    TextView connectdevices;
//    private ArrayList<BLEDevice> lrd = new ArrayList<>();
//
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        writeToDB();
//        //Initializing the sdk
//        AdvanproAndroidSDK.init(getApplicationContext());
//        //set advanpro android sdk config
//        AConfig config = AdvanproAndroidSDK.getConfig();
//        //enable print debug log
//        config.setDebugLog(true);
//        AdvanproAndroidSDK.setConfig(config);
//        // end of Initializing the sdk
//        connectdevices=findViewById(R.id.tv_connection_prompt);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
//        }
//
//        boolean enable = AdvanproAndroidSDK.getDeviceManager(BLEDeviceManager.class).isEnable(ConnectType.BLE);
//        final BLEDeviceManager manager = AdvanproAndroidSDK.getDeviceManager(BLEDeviceManager.class);
//
//        try {
//            manager.scan(5, new DeviceManagerScanCallback<BLEDeviceScanResult>(){
//                @Override
//                public void onScanning(BLEDeviceScanResult result) {
//                    Log.d(TAG, String.format("Detected Device Name: %s ",result.getRecord().getLocalName()));
//                    //Return only insoles device type
//                    //                Log.d(TAG, String.format("Detected Device Name: %s, Address: %s,Type:%s",
//                    //                        result.getRecord().getLocalName(), result.getRecord().getAddress(),
//                    //                            result.getRecord().getManufacturer().getType()));
//
//                    if(result.getRecord().getManufacturer().getType().toString().equals("Insole")){
//                        Devices.add(result);
//                    }
//                }
//                @Override
//                public void onStop() {
//
//                    Log.d(TAG, "stop scan");
//                    device1 = Devices.get(0).create();
//                    device2 = Devices.get(1).create();
////
//                    connectdevices.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            connectToDevices();
//                            if(_ConnectedDevices.size()>=1){
//                                Log.e(TAG, String.valueOf(_ConnectedDevices.size()));
//
//                                _ConnectedDevices.get(0).command(BLEInsoleDevice.CMD_INSOLE_ENABLE_ACC, new BLEInsoleEnableAccCommandParams(1), new MainThreadDeviceCommandCallback<BLEInsoleEnableAccCommandResult>() {
//                                    @Override
//                                    public void onMainThreadSuccess(Device device,BLEInsoleEnableAccCommandResult result) {
//                                        if(result.isSuccess()){
//                                            ALog.d(TAG,"Axis "+"Successful");
//                                        }
//                                    }
//                                    @Override
//                                    public void onMainThreadError(Device device,DeviceCallbackException error) {
//                                        error.printStackTrace();
//                                    }
//                                });
//
//                                _ConnectedDevices.get(0).on(BLEInsoleDevice.EVENT_INSOLE_REAL_ACC, new DeviceEventCallback<BLEInsoleRealAccEventResult>() {
//                                    @Override
//                                    public void onData(Device device,BLEInsoleRealAccEventResult data) {
//                                        ALog.d(TAG,"Axis Ax,Ay,Az"+data.getAx()+","+data.getAy()+","+data.getAz());
//                                        ALog.d(TAG,"Axis Bx,By,Bz"+data.getBx()+","+data.getBy()+","+data.getBz());
//                                        ALog.d(TAG,"Axis Ax,Ay,Az"+data.getCx()+","+data.getCy()+","+data.getCz());
//                                    }
//                                });
//                            }
//
//                        }
//                    });
//                }
//                @Override
//                public void onError(DeviceCallbackException error) {
//                    Log.d(TAG, error.getCause().getMessage());
//                    error.printStackTrace();
//                    //handler err...
//                }
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            onStop();
//        }
//
//
//    }
//
//    private void connectToDevices() {
//        final Intent intent = (new Intent(getApplicationContext(),ConnectedDeviceActivity.class));
//        final Bundle bundle = new Bundle();
//        if(device1.isConnected()==false)
//        {
//            device1.connect(new DeviceConnectCallback() {
//                @Override
//                public void onSucceed(Device device, DeviceConnectResult result) {
//                    Log.d(TAG,"The device1 connection is successful");
//                    _ConnectedDevices.add(device1);
//                    device1.command(BLEInsoleDevice.CMD_INSOLE_ENABLE_ACC, new BLEInsoleEnableAccCommandParams(1), new MainThreadDeviceCommandCallback<BLEInsoleEnableAccCommandResult>() {
//                        @Override
//                        public void onMainThreadSuccess(Device device,BLEInsoleEnableAccCommandResult result) {
//                            if(result.isSuccess()){
//                                ALog.d(TAG,"Axis "+"Successful");
//                            }
//                        }
//                        @Override
//                        public void onMainThreadError(Device device,DeviceCallbackException error) {
//                            error.printStackTrace();
//                        }
//                    });
//
//                    device1.on(BLEInsoleDevice.EVENT_INSOLE_REAL_ACC, new DeviceEventCallback<BLEInsoleRealAccEventResult>() {
//                        @Override
//                        public void onData(Device device,BLEInsoleRealAccEventResult data) {
//                            ALog.d(TAG,"Axis Ax,Ay,Az"+data.getAx()+","+data.getAy()+","+data.getAz());
//                            ALog.d(TAG,"Axis Bx,By,Bz"+data.getBx()+","+data.getBy()+","+data.getBz());
//                            ALog.d(TAG,"Axis Ax,Ay,Az"+data.getCx()+","+data.getCy()+","+data.getCz());
//                        }
//                    });
////
//                    MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult> cb2 = new MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult>() {
//                        @Override
//                        public void onMainThreadData(Device device,BLEInsoleRealGaitEventResult data)
//                        {
//                            String d = data.getDate()+"Sensors:"+ " A "+data.getTouchA()+" B "+data.getTouchB()+" C "+data.getTouchC()+" D "+data.getTouchD();
//                            ALog.d(TAG, "Sensors:"+ " A "+data.getTouchA()+" B "+data.getTouchB()+" C "+data.getTouchC()+" D "+data.getTouchD());
//                            ALog.d(TAG, "impact "+ data.getImpact());
//                            writeToFile(d,getApplicationContext());
//                        }
//                    };
//                    device1.on(BLEInsoleDevice.EVENT_INSOLE_REAL_GAIT, cb2);
////Cancel to monitor
//                    //device.un(BLEInsoleDevice.EVENT_INSOLE_REAL_STEP, callback);
//                    if(device2.isConnected()){
//                        //startActivity(intent);
//                        connectdevices.setText("Connected");
//                        device2.on(BLEInsoleDevice.EVENT_INSOLE_REAL_ACC, new DeviceEventCallback<BLEInsoleRealAccEventResult>() {
//                            @Override
//                            public void onData(Device device,BLEInsoleRealAccEventResult data) {
//                                ALog.d(TAG,"Axis Ax,Ay,Az"+data.getAx()+","+data.getAy()+","+data.getAz());
//                                ALog.d(TAG,"Axis Bx,By,Bz"+data.getBx()+","+data.getBy()+","+data.getBz());
//                                ALog.d(TAG,"Axis Ax,Ay,Az"+data.getCx()+","+data.getCy()+","+data.getCz());
//                            }
//                        });
//                    }
//                }
//
//                @Override
//                public void onError(Device device, DeviceCallbackException e) {
//                    Log.d(TAG,"The connection failed");
//                }
//            });
//        }
////        else if(device1.isConnected()){
////            Log.d(TAG,"device1 is already connected");
////        }
//        if(device2.isConnected()==false)
//        {
//            device2.connect(new DeviceConnectCallback() {
//                @Override
//                public void onSucceed(Device device, DeviceConnectResult result) {
//                    Log.d(TAG,"The connection is successful");
//                    _ConnectedDevices.add(device2);
//                    device2.command(BLEInsoleDevice.CMD_INSOLE_ENABLE_ACC, new BLEInsoleEnableAccCommandParams(1), new MainThreadDeviceCommandCallback<BLEInsoleEnableAccCommandResult>() {
//                    @Override
//                    public void onMainThreadSuccess(Device device,BLEInsoleEnableAccCommandResult result) {
//                        if(result.isSuccess()){
//                            ALog.d(TAG,"Axis "+"Successful");
//                        }
//                    }
//                    @Override
//                    public void onMainThreadError(Device device,DeviceCallbackException error) {
//                        error.printStackTrace();
//                    }
//                });
//                    device2.on(BLEInsoleDevice.EVENT_INSOLE_REAL_ACC, new DeviceEventCallback<BLEInsoleRealAccEventResult>() {
//                        @Override
//                        public void onData(Device device,BLEInsoleRealAccEventResult data) {
//                            ALog.d(TAG,"Axis Ax,Ay,Az"+data.getAx()+","+data.getAy()+","+data.getAz());
//                            ALog.d(TAG,"Axis Bx,By,Bz"+data.getBx()+","+data.getBy()+","+data.getBz());
//                            ALog.d(TAG,"Axis Ax,Ay,Az"+data.getCx()+","+data.getCy()+","+data.getCz());
//                        }
//                    });
////
//                    MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult> cb2 = new MainThreadDeviceEventCallback<BLEInsoleRealGaitEventResult>() {
//                        @Override
//                        public void onMainThreadData(Device device,BLEInsoleRealGaitEventResult data)
//                        {
//                            String d = data.getDate()+"Sensors:"+ " A "+data.getTouchA()+" B "+data.getTouchB()+" C "+data.getTouchC()+" D "+data.getTouchD();
//                            ALog.d(TAG, "Sensors:"+ " A "+data.getTouchA()+" B "+data.getTouchB()+" C "+data.getTouchC()+" D "+data.getTouchD());
//                            ALog.d(TAG, "impact "+ data.getImpact());
//                            writeToFile(d, getApplicationContext());
//                        }
//                    };
//                    device2.on(BLEInsoleDevice.EVENT_INSOLE_REAL_GAIT, cb2);
////
//                    if(device1.isConnected()){
//                        //startActivity(intent);
//                        connectdevices.setText("Connected");
//                        device1.on(BLEInsoleDevice.EVENT_INSOLE_REAL_ACC, new DeviceEventCallback<BLEInsoleRealAccEventResult>() {
//                            @Override
//                            public void onData(Device device,BLEInsoleRealAccEventResult data) {
//                                ALog.d(TAG,"Axis Ax,Ay,Az"+data.getAx()+","+data.getAy()+","+data.getAz());
//                                ALog.d(TAG,"Axis Bx,By,Bz"+data.getBx()+","+data.getBy()+","+data.getBz());
//                                ALog.d(TAG,"Axis Ax,Ay,Az"+data.getCx()+","+data.getCy()+","+data.getCz());
//                            }
//                        });
//
//                    }
//                }
//                @Override
//                public void onError(Device device, DeviceCallbackException error) {
//                    Log.d(TAG, "The connection failed");
//                    // The connection is fails...
//                }
//            });
//        }
//        if(device2.isConnected()){
//            device2.command(BLEInsoleDevice.CMD_INSOLE_ENABLE_ACC, new BLEInsoleEnableAccCommandParams(1), new MainThreadDeviceCommandCallback<BLEInsoleEnableAccCommandResult>() {
//                @Override
//                public void onMainThreadSuccess(Device device,BLEInsoleEnableAccCommandResult result) {
//                    if(result.isSuccess()){
//                        ALog.d(TAG,"Axis "+"Successful");
//                    }
//                }
//                @Override
//                public void onMainThreadError(Device device,DeviceCallbackException error) {
//                    error.printStackTrace();
//                }
//            });
//            device2.on(BLEInsoleDevice.EVENT_INSOLE_REAL_ACC, new DeviceEventCallback<BLEInsoleRealAccEventResult>() {
//                @Override
//                public void onData(Device device,BLEInsoleRealAccEventResult data) {
//                    ALog.d(TAG,"Axis Ax,Ay,Az"+data.getAx()+","+data.getAy()+","+data.getAz());
//                    ALog.d(TAG,"Axis Bx,By,Bz"+data.getBx()+","+data.getBy()+","+data.getBz());
//                    ALog.d(TAG,"Axis Ax,Ay,Az"+data.getCx()+","+data.getCy()+","+data.getCz());
//                }
//            });
//            Log.d(TAG,"device2 is already connected");
//        }
//    }
//
//    private void writeToFile(String data,Context context) {
//        try {
//            Log.e(TAG, String.valueOf(getFileStreamPath("sensordata.txt")));
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("sensordata.txt", Context.MODE_PRIVATE));
//            outputStreamWriter.write(data);
//            outputStreamWriter.close();
//        }
//        catch (IOException e) {
//            Log.e("Exception", "File write failed: " + e.toString());
//        }
//    }
//    private void writeToDB() {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("message");
//
//        myRef.setValue("Hello, World!");
//
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
//                Log.d("tag", "Value is: " + value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w("tag", "Failed to read value.", error.toException());
//            }
//        });
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case PERMISSION_REQUEST_COARSE_LOCATION: {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getApplicationContext(),"Permission granted! Bluetooth device scan started",Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(),"Permission denied,this application requires the location permission to perform the scan",Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    }
//}
