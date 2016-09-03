package bharatd.example.com.drum;

/**
 * Created by bharatd on 16/8/2016.
 */

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SocketThread extends Thread {
    BluetoothSocket socket;
    SocketDispatcher parent;
    private int TID;
    public SocketThread(BluetoothSocket sock, SocketDispatcher sdp, int ID){
        socket = sock;
        parent = sdp;
        TID = ID;
    }

    public void run(){
        try {
            InputStream iStream = socket.getInputStream();
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(iStream));
            int type = 0;
            float xVal = 0;
            float yVal = 0;
            float zVal = 0;
            long timestamp = 0;
            String vals[];
            String line;
            while(true){
                line = bufReader.readLine();
                vals = line.split(",");
//                Log.i("SocketThread", "Read new data " + vals[0]);
                if((vals != null) && (vals.length != 0)) {
               //bharat     //type = Integer.parseInt(vals[0]);
               //bharat     //timestamp = Long.parseLong(vals[1]);
                    xVal = Float.parseFloat(vals[2]);
                    yVal = Float.parseFloat(vals[3]);
                    zVal = Float.parseFloat(vals[4]);
               parent.processData(TID, type, timestamp, xVal, yVal, zVal);
                    //bharat parent.processData(xVal, yVal, zVal);
                }
            }
        }catch(IOException e){
            Log.e("SOCKETTHREAD", "Error get input stream");
        }
    }

    public void clean(){
        try {
            socket.close();
        }catch(IOException e){
            Log.e("SOCKETTHREAD", "Error closing socket");
        }
    }
}

