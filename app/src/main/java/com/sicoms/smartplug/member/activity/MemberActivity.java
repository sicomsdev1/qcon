package com.sicoms.smartplug.member.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPFragment;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class MemberActivity extends ActionBarActivity {

    private static final String TAG = MemberActivity.class.getSimpleName();

    private CharSequence mTitle = "플레이스 사용자";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bg);

        getSupportActionBar().setTitle(mTitle);

        SPFragment.intentMemberFragment(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sync, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setTitle(mTitle);
    }


}
