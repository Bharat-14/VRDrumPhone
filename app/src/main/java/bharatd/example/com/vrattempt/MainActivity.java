package bharatd.example.com.vrattempt;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    private static final int REQUEST_ENABLE_BT = 1;
    private final String INFOTAG = "SENSORRECORDER";
    private final String ERRTAG = "SENSORRECORDER";

    private Button btStartStop;
    private CheckBox xbRecord;
    private CheckBox xbVisual;
    private CheckBox xbAccel;
    private CheckBox xbGyros;
    private CheckBox xbGravity;
    private CheckBox xbMagnet;
    private TextView tvStatus;
    private MainImage miGraph;
    private SocketDispatcher sockDispatcher;
    private BroadcastReceiver broadcastReceiver;
    private Drawable playIcon;
    private Drawable stopIcon;
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

    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    final Runnable timer = new Runnable() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(xbVisual.isChecked()) miGraph.invalidate();
                    tvStatus.setText("Received: " + dataCount + "B");
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btStartStop = (Button)findViewById(R.id.btStartStop);
        playIcon = getResources().getDrawable(R.drawable.btplay128);
        stopIcon = getResources().getDrawable(R.drawable.btstop128);
        btStartStop.setBackground(playIcon);
        btStartStop.setOnClickListener(this);
        xbRecord = (CheckBox)findViewById(R.id.xbRecord);
        xbRecord.setOnCheckedChangeListener(this);
        xbVisual = (CheckBox)findViewById(R.id.xbVisual);
        xbVisual.setOnCheckedChangeListener(this);
        xbAccel = (CheckBox)findViewById(R.id.xbAccel);
        xbAccel.setOnCheckedChangeListener(this);
        xbGyros = (CheckBox)findViewById(R.id.xbGyros);
        xbGyros.setOnCheckedChangeListener(this);
        xbGravity = (CheckBox)findViewById(R.id.xbGravity);
        xbGravity.setOnCheckedChangeListener(this);
        xbMagnet = (CheckBox)findViewById(R.id.xbMagnet);
        xbMagnet.setOnCheckedChangeListener(this);
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        miGraph = (MainImage)findViewById(R.id.miGraph);
        handler = new Handler();
        accelType = 0;
        gyrosType = 0;
        gravityType = 0;
        magnetType = 0;
        started = false;
        dataCount = 0;
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int info = intent.getIntExtra(SocketDispatcher.INFO, 0);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(SocketDispatcher.SKTDPT));
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

    @Override
    public void onClick(View v) {
        if(v == btStartStop){
            onoff();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
        if(buttonView == xbRecord){

        }else if(buttonView == xbVisual){

        }else if(buttonView == xbAccel){
            if(isChecked){
                accelType = Sensor.TYPE_LINEAR_ACCELERATION;
            } else {
                accelType = 0;
            }
        }else if(buttonView == xbGyros){
            if(isChecked){
                gyrosType = Sensor.TYPE_GYROSCOPE;
            } else {
                gyrosType = 0;
            }
        }else if(buttonView == xbGravity){
            if(isChecked){
                gravityType = Sensor.TYPE_GRAVITY;
            } else {
                gravityType = 0;
            }
        }else if(buttonView == xbMagnet){
            if(isChecked){
                magnetType = Sensor.TYPE_MAGNETIC_FIELD;
            } else {
                magnetType = 0;
            }
        }
        Log.i(INFOTAG, "Check/uncheck" + buttonView.getText());
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
            btStartStop.setBackground(playIcon);
            started = false;
            tvStatus.setText("Stopped");
            Log.i(INFOTAG, "Stop");
        }else{
            scheduler.scheduleWithFixedDelay(timer, 0, 30, TimeUnit.MILLISECONDS);
            sockDispatcher = new SocketDispatcher(this);
            sockDispatcher.start();
            btStartStop.setBackground(stopIcon);
            started = true;
            tvStatus.setText("Waiting for watches");
            Log.i(INFOTAG, "Start");
            dataCount = 0;
        }
    }

    public void setText(String text){
        tvStatus.setText("\n" + text);
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
        if(type == Sensor.TYPE_LINEAR_ACCELERATION) {
            accl[0] = x; accl[1] = y; accl[2] = z;
            rotateX(accl, grav, accl);
        } else if(type == Sensor.TYPE_GRAVITY) {
            grav[0] = x; grav[1] = y; grav[2] = z;
        }
        if(xbVisual.isChecked()) {
            if ((type == accelType) || (type == gyrosType) || (type == gravityType) || (type == magnetType)) {
                miGraph.appendAccelerometer(wrist, accl[0], accl[1], accl[2]);
            }
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
        onoff();
        try {
            outFileStream.close();
        }catch (IOException e){
            Log.e("SOCKETDISPATCHER", "Cannot close streams");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

