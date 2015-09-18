package com.sicoms.smartplug.group.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.group.fragment.GroupFragment;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class GroupActivity extends ActionBarActivity {

    private static final String TAG = GroupActivity.class.getSimpleName();

    private CharSequence mTitle = "Group";
    private GroupVo mGroupVo;
    private View mDrawerView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Intent intent = getIntent();
        mGroupVo = new Gson().fromJson(intent.getStringExtra(SPActivity.ARG_GROUP_VO_NAME), GroupVo.class);
        mTitle = mGroupVo.getGroupName();
        getSupportActionBar().setTitle(mTitle);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerView = findViewById(R.id.navigation_drawer);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, GroupFragment.newInstance(mGroupVo));
        ft.commit();
        //SPFragment.intentGroupFragment(this, mGroupVo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case android.R.id.home :
                if( getSupportFragmentManager().getBackStackEntryCount()>0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
                break;
            case R.id.action_group_menu :
                if( mDrawerLayout.isDrawerOpen(mDrawerView)) {
                    mDrawerLayout.closeDrawer(mDrawerView);
                } else {
                    mDrawerLayout.openDrawer(mDrawerView);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_group, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
