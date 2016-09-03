package bharatd.example.com.drum;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
//import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.unity.GoogleUnityActivity;
import com.unity3d.player.UnityPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//public class MainActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
public class  MainActivity extends GoogleUnityActivity  {

    private static final int REQUEST_ENABLE_BT = 1;
    private final String INFOTAG = "SENSORRECORDER";
    private final String ERRTAG = "SENSORRECORDER";


    private SocketDispatcher sockDispatcher;
    private BroadcastReceiver broadcastReceiver;

    private Handler handler;
    private int accelType;
    private int gyrosType;
    private int gravityType;
    private int magnetType;
    File outFile;
    FileOutputStream outFileStream;
    private boolean started;
    private int dataCount;
    float[] grav = new float[3];
    float[] accl = new float[3];
    // bharat float[] orientation = new float[3];

    Sensor mAccelerometer=null;

    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    final Runnable timer = new Runnable() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                  //  if(xbVisual.isChecked()) miGraph.invalidate();
                  //  tvStatus.setText("Received: " + dataCount + "B");
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("Unity DOCOMO", "**bharat** ");
        UnityPlayer.UnitySendMessage("Plane","PhoneOrientationChanged","Duck Nuggets!");
        //bharat
       // SensorManager sensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
       // mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
       // sensorManager.registerListener(MainActivity.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //bharat

        accelType = 0;
        gyrosType = 0;
        gravityType = 0;
        magnetType = 0;
        started = false;
        dataCount = 0;
        onoff();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int info = intent.getIntExtra(SocketDispatcher.INFO, 0);
            }
        };
        //bharat starts for watch
        //this.registerReceiver(broadcastReceiver, new IntentFilter(SocketDispatcher.SKTDPT));
                //LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(SocketDispatcher.SKTDPT));
        //bharat ends for watch
        try {
            Calendar calendar = Calendar.getInstance();
            int date = calendar.get(Calendar.DATE);
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            outFile = new File(Environment.getExternalStorageDirectory().getPath() + "/ERP/", "sensorlog_" + date + "_" + hour + "_" + minute + ".txt");
            if(outFile.exists()) outFile.delete();
            outFile.createNewFile();
            outFileStream = new FileOutputStream(outFile, true);
            Log.i("SensorServer: ", "Open file successfully");
        }catch(Exception e){
            Log.e("File error", "Cannot open file");
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }



    private void onoff(){
        if(started){
            sockDispatcher.clean();
            sockDispatcher.interrupt();
            try {
                sockDispatcher.join();
            }catch (InterruptedException e){
                Log.e(ERRTAG, "Cannot stop thread");
            }

            started = false;

            Log.i(INFOTAG, "Stop");
        }else{
            scheduler.scheduleWithFixedDelay(timer, 0, 30, TimeUnit.MILLISECONDS);
            sockDispatcher = new SocketDispatcher(this);
            sockDispatcher.start();

            started = true;

            Log.i(INFOTAG, "Start");
            dataCount = 0;
        }
    }



    public void rotateX(float DIn[], float GIn[], float DOut[]){
        float P1 = GIn[2]*GIn[2] + GIn[1]*GIn[1];
        float Az1 = (DIn[1]*GIn[1] + DIn[2]*GIn[2])/(float)Math.sqrt(P1);
        float Ay1 = (DIn[1]*GIn[2] - DIn[2]*GIn[1])/(float)Math.sqrt(P1);
        float Ax1 = DIn[0];
        DOut[0] = Ax1;
        DOut[1] = Ay1;
        DOut[2] = Az1;
    }

    public void processData(int wrist, int type, long timestamp, float x, float y, float z){
        //bharat public void processData(float x, float y, float z){

        String message = x +"," + y + "," +z ;

        Log.i("Android", message);
        UnityPlayer.UnitySendMessage("Plane", "WearOrientationChanged", message);

            //bharat orientation[0] = x; orientation[1] = y; orientation[2] = z;

        if(type == Sensor.TYPE_LINEAR_ACCELERATION) {
            accl[0] = x; accl[1] = y; accl[2] = z;
            rotateX(accl, grav, accl);
        } else if(type == Sensor.TYPE_GRAVITY) {
            grav[0] = x; grav[1] = y; grav[2] = z;
        }


        if((type == accelType) || (type == gyrosType) || (type == gravityType) || (type == magnetType)){
            String tem = "" + wrist + "," + type + "," + timestamp + "," + x + "," + y + "," + z + "\n";
            try {
                outFileStream.write(tem.getBytes());
            }catch (IOException e){

            }
            dataCount += 12;
        }

    }

    @Override  
    public void onPause(){
        super.onPause();
        Log.i("BTSERVER", "Paused");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        started = true;
        onoff();
        try {
            outFileStream.close();
        }catch (IOException e){
            Log.e("SOCKETDISPATCHER", "Cannot close streams");
        }
    }




}
