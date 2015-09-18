package com.sicoms.smartplug.group.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPFragment;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class GroupCreatorActivity extends ActionBarActivity  {

    private static final String TAG = GroupCreatorActivity.class.getSimpleName();

    private CharSequence mTitle = "그룹 생성";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bg);

        getSupportActionBar().setTitle(mTitle);

        SPFragment.intentCreateGroupFragment(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_group, menu);

        return super.onCreateOptionsMenu(menu);
    }
}
