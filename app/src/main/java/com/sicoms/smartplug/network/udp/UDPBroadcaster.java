package com.sicoms.smartplug.network.udp;

import android.util.Log;

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
public class UDPBroadcaster {
    private static final String TAG = "UDPBroadcaster";

    private DatagramSocket mSocket;
    private UDPResponseCallbacks mCallbacks;

    public void sendBroadcast(final String param) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // DatagramSocket 생성
                        mSocket = new DatagramSocket();
                        mSocket.setSoTimeout(1*10000); // 여러개 받을 경우 대비
                        mSocket.setBroadcast(true);

                        // Send Broadcast Data
                        String sendData = param + "\n\n";
                        DatagramPacket sendPacket = new DatagramPacket(sendData.getBytes(), sendData.getBytes().length, InetAddress.getByName("255.255.255.255"), UDPConfig.UDP_Port);
                        mSocket.send(sendPacket);
                        while (true) {
                            byte[] inbuf = new byte[1024 * 100];
                            DatagramPacket receivePacket = new DatagramPacket(inbuf, inbuf.length);
                            mSocket.receive(receivePacket);
                            final String receiveData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                            final String serverIp = receivePacket.getAddress().getHostAddress();
                            Log.d(TAG, receiveData);
                            if (mCallbacks != null) {
                                mCallbacks.onUDPResponseResultStatus(serverIp, receiveData);
                            }
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch(SocketTimeoutException e){
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        disconnect();
    }

    public void disconnect(){
        if( mSocket != null) {
            mSocket.disconnect();
            mSocket.close();
        }
    }

    public interface UDPResponseCallbacks {
        void onUDPResponseResultStatus(String ip, String receiveData);
    }

    public void setOnUDPResponseCallbacks(final UDPResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }
}
