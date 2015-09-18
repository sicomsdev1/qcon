package com.sicoms.smartplug.xmpp;
//
//import org.jivesoftware.smack.AbstractXMPPConnection;
//import org.jivesoftware.smack.ConnectionConfiguration;
//import org.jivesoftware.smack.SmackException;
//import org.jivesoftware.smack.XMPPConnection;
//import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smack.packet.Packet;
//import org.jivesoftware.smack.tcp.XMPPTCPConnection;
//import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
//
//import java.io.IOException;
//
///**
// * Created by pc-11-user on 2015-04-30.
// */
//public class XMPPManager {
//
//    private AbstractXMPPConnection mConnection;
//
//    public AbstractXMPPConnection getConnection(){
//        return mConnection;
//    }
//
//    public void connect(){
//        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
//        configBuilder.setUsernameAndPassword("gudnam", "gudnam123~!");
//        configBuilder.setHost(XMPPConfig.HOST);
//        configBuilder.setPort(XMPPConfig.PORT);
//        mConnection = new XMPPTCPConnection(configBuilder.build());
//        try {
//            mConnection.connect();
//            mConnection.login();
//        } catch (XMPPException ex){
//
//        } catch (SmackException ex){
//
//        } catch (IOException ex){
//
//        }
//    }
//
//    public void disconnect(){
//        mConnection.disconnect();
//    }
//}
