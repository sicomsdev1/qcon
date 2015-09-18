package com.sicoms.smartplug.network.udp;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sicoms.smartplug.common.SPConfig;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by pc-11-user on 2015-03-27.
 */
public class UDPClient {
    private static final String TAG = "UDPClient";

    private DatagramSocket mSocket;
    private UDPResponseCallbacks mCallbacks;

    public void execute(final String... params) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // DatagramSocket 생성
                    mSocket = new DatagramSocket();
                    mSocket.setSoTimeout(3 * 1000);

                    // Send Broadcast Data
                    String ip = SPConfig.AP_IP;
                    int port = UDPConfig.UDP_Port;
                    String sendData = "";
                    if (params.length == 1) {
                        sendData = params[0] + "\n\n";
                    } else if (params.length == 2) {
                        ip = params[0];
                        sendData = params[1] + "\n\n";
                    } else if (params.length == 3) {
                        ip = params[0];
                        port = Integer.parseInt(params[1]);
                        sendData = params[2] + "\n\n";
                    }
                    if (ip.length() < 10) {
                        return;
                    }
                    Log.d(TAG, "UDP IP : " + ip + ", Port : " + port);

                    DatagramPacket sendPacket = new DatagramPacket(sendData.getBytes(), sendData.getBytes().length, InetAddress.getByName(ip), port);
                    mSocket.send(sendPacket);
                    Log.d(TAG, "Send : " + sendData);

                    byte[] inbuf = new byte[1024 * 100];
                    DatagramPacket receivePacket = new DatagramPacket(inbuf, inbuf.length);
                    mSocket.receive(receivePacket);
                    String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    Log.d(TAG, "Receive : " + receiveData);
                    if (mCallbacks != null) {
                        mCallbacks.onUDPResponseResultStatus(UDPConfig.UDP_SUCCESS, receiveData);
                    }
                    disconnect();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mCallbacks != null) {
                    mCallbacks.onUDPResponseResultStatus(UDPConfig.UDP_CONNECT_FAIL, null);
                }
                disconnect();
            }
        });

        thread.start();
    }

    private void disconnect(){
        if( mSocket != null) {
            mSocket.disconnect();
            mSocket.close();
        }
    }

    public interface UDPResponseCallbacks {
        void onUDPResponseResultStatus(int result, String response);
    }

    public void setOnUDPResponseCallbacks(final UDPResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }
}
