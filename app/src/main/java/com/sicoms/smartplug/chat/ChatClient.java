package com.sicoms.smartplug.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sicoms.smartplug.R;

/**
 * Created by pc-11-user on 2015-04-30.
 */
public class ChatClient extends Fragment {
    private TextView mTvMessage;
    private EditText mEtSendMessage;
    private Button mBtnSend;

    public static ChatClient newInstance() {
        ChatClient fragment = new ChatClient();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null){

        }
        View view = inflater.inflate(R.layout.fragment_plug, container, false);

        mTvMessage = (TextView) view.findViewById(R.id.tv_message);
        mEtSendMessage = (EditText) view.findViewById(R.id.et_send_message);
        mBtnSend = (Button) view.findViewById(R.id.btn_send);

        //XMPPManager xmppManager = new XMPPManager();
        //xmppManager.connect();

        //XMPPConnection connection = xmppManager.getConnection();

        return view;
    }
}
