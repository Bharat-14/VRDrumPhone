package bharatd.example.com.drum;

/**
 * Created by bharatd on 16/8/2016.
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
//import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class SocketDispatcher extends Thread {
    private BluetoothAdapter mBTAdapter = null;
    private BluetoothServerSocket mServerSocket = null;
    //private LocalBroadcastManager broadcaster;
    private final String MY_UUID = "852159da-a17b-4057-983d-830c4537851c";
    public static final String SKTDPT = "SKTDPT";
    public static final String INFO = "INFO";
    public static final String ACCEL = "ACCEL";

    MainActivity parent;

    SocketThread sockThread1;
    SocketThread sockThread2;

    Intent intent = new Intent(SKTDPT);

    public SocketDispatcher(MainActivity activity){
        Log.i("SocketDispatcher", "Created");
        parent = activity;
       // broadcaster = LocalBroadcastManager.getInstance(activity);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
//        if(!mBTAdapter.isEnabled()) mBTAdapter.enable();
        if(!mBTAdapter.isEnabled()){
            activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
        }
    }

    public void run(){
        BluetoothSocket sock1;
        BluetoothSocket sock2;
        Log.i("SocketDispatcher", "Started");
        try {
            mServerSocket = mBTAdapter.listenUsingRfcommWithServiceRecord("BTSERVER", UUID.fromString(MY_UUID));
            Log.i("SocketDispatcher", "Waiting for device 1");
            sock1 = mServerSocket.accept();
            sockThread1 = new SocketThread(sock1, this, 0);
            sockThread1.start();
//            parent.setText("Watch 0 is connected");
            Log.i("SocketDispatcher", "Waiting for device 2");
            sock2 = mServerSocket.accept();
            sockThread2 = new SocketThread(sock2, this, 1);
            sockThread2.start();
//            parent.setText("Watch 1 is connected");
            try {
                sockThread1.join();
                sockThread2.join();
            }catch(InterruptedException e){
                Log.e("SocketDispatcher", "Thread join error");
            }
        }catch (IOException e){
            Log.e("SocketDispatcher", "Error create server socket");
        }
    }

    public void clean(){
        Log.i("SocketDispatcher", "Destroyed");
        if((sockThread1 != null) && (sockThread1.isAlive())) {
            sockThread1.interrupt();
            sockThread1.clean();
        }
        if((sockThread2 != null) && (sockThread2.isAlive())) {
            sockThread2.interrupt();
            sockThread2.clean();
        }
        try {
            mServerSocket.close();
        }catch (IOException e){
            Log.e("SocketDispatcher", "Error closing server socket");
        }
    }

    public void processData(int id, int type, long timestamp, float xValue, float yValue, float zValue){
        parent.processData(id, type, timestamp, xValue, yValue, zValue);
    }
   /* bharat start
   public void processData( float xValue, float yValue, float zValue){
       parent.processData( xValue, yValue, zValue);
   }
*/  //bharat end
}
