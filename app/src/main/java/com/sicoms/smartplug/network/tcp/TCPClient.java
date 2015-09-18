package com.sicoms.smartplug.network.tcp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.sicoms.smartplug.domain.WifiVo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by pc-11-user on 2015-03-26.
 */
public class TCPClient extends AsyncTask<String, Void, String> {
    private static final String TAG = "TCPClient";

    private final int CONNECT_TIME_OUT = 10000;
    private final int SEND_TIME_OUT = 10000;
    private Socket mSocket;
    private BufferedReader networkReader;

    private TCPResponseCallbacks mCallbacks;

    @Override
    protected String doInBackground(String... params) {
        try {
            mSocket = new Socket();
            mSocket.connect(new InetSocketAddress(TCPConfig.TCP_IP, TCPConfig.TCP_Port), CONNECT_TIME_OUT);
            mSocket.setSoTimeout(SEND_TIME_OUT);
            writeSocket(params[0]);
            String responseMessage = readSocket();
            Log.d(TAG, responseMessage);

            if( responseMessage == TCPConfig.TCP_RESPONSE_OK) {
                mCallbacks.onTCPResponseResultStatus(TCPConfig.TCP_SUCCESS, responseMessage);
            } else {
                mCallbacks.onTCPResponseResultStatus(TCPConfig.TCP_RESPONSE_FAIL, responseMessage);
            }
        } catch (IOException e) {
            mCallbacks.onTCPResponseResultStatus(TCPConfig.TCP_CONNECT_FAIL, e.getMessage());
            e.printStackTrace();
        }
        disconnect();
        return null;
    }

    private void writeSocket(String jsonMessage) throws IOException{
        BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
        PrintWriter out = new PrintWriter(bufferWriter, true);
        out.println(jsonMessage);
    }
    private String readSocket() throws IOException{
        BufferedReader networkReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
        String responseMessage = "";
        while(!Thread.interrupted()){
            responseMessage = networkReader.readLine();
            if( responseMessage != ""){
                break;
            }
        }
        return responseMessage;
    }

    public void disconnect(){
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public interface TCPResponseCallbacks {
        void onTCPResponseResultStatus(int result, String response);
    }
    public void setOnTCPResponseCallbacks(final TCPResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }
}
