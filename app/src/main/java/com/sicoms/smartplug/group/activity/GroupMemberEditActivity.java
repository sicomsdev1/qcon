package com.sicoms.smartplug.group.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.gson.Gson;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.UserVo;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class GroupMemberEditActivity extends ActionBarActivity  {

    private static final String TAG = GroupMemberEditActivity.class.getSimpleName();

    private CharSequence mTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bg);

        Intent intent = getIntent();
        UserVo userVo = new Gson().fromJson(intent.getStringExtra(SPActivity.ARG_USER_VO_NAME), UserVo.class);

        mTitle = userVo.getUserName();
        getSupportActionBar().setTitle(mTitle);

        SPFragment.intentMemberEditAuthFragment(this, userVo);
    }
}
