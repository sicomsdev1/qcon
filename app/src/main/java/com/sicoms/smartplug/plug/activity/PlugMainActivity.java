package com.sicoms.smartplug.plug.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.CommonService;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.group.fragment.GroupAllFragment;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.plug.fragment.PlugAllFragment;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class PlugMainActivity extends ActionBarActivity {

    private static final String TAG = PlugMainActivity.class.getSimpleName();

    private CharSequence mTitle = "전체 플러그";
    private Activity mActivity;

    private Thread mRealtimeThread;

    private Menu mMenu;


    private void initialize(){
        PlaceVo placeVo = PlaceService.loadLastPlace(this);
        SPUtil.setBackgroundForLinear(this, placeVo.getPlaceImg());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_main_bg);

        initialize();

        getSupportActionBar().setTitle(mTitle);

        String menuName = CommonService.loadLastMenu(mActivity);
        if( menuName.equalsIgnoreCase(PlugAllFragment.class.getSimpleName())) {
            SPFragment.intentPlugAllFragment(this);
        } else if( menuName.equalsIgnoreCase(GroupAllFragment.class.getSimpleName())){
            SPFragment.intentGroupAllFragment(this);
        } else {
            SPFragment.intentPlugAllFragment(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if( mRealtimeThread != null){
//            mRealtimeThread.interrupt();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_plug_main, menu);
        mMenu = menu;
        MenuItem wholeItem = mMenu.findItem(R.id.action_whole);
        MenuItem individualItem = mMenu.findItem(R.id.action_individual);

        String menuName = CommonService.loadLastMenu(mActivity);
        if( menuName.equalsIgnoreCase(PlugAllFragment.class.getSimpleName())) {
            individualItem.setIcon(R.drawable.icon_sort_individual_on);
            wholeItem.setIcon(R.drawable.icon_sort_whole_off);
        } else if( menuName.equalsIgnoreCase(GroupAllFragment.class.getSimpleName())){
            individualItem.setIcon(R.drawable.icon_sort_individual_off);
            wholeItem.setIcon(R.drawable.icon_sort_whole_on);
        } else {
            individualItem.setIcon(R.drawable.icon_sort_individual_on);
            wholeItem.setIcon(R.drawable.icon_sort_whole_off);
        }
        return super.onCreateOptionsMenu(menu);
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
            case R.id.action_individual :
                MenuItem wholeItem = mMenu.findItem(R.id.action_whole);
                wholeItem.setIcon(R.drawable.icon_sort_whole_off);
                item.setIcon(R.drawable.icon_sort_individual_on);

                SPFragment.intentPlugAllFragment(this);
                break;
            case R.id.action_whole :
                MenuItem individualItem = mMenu.findItem(R.id.action_individual);
                individualItem.setIcon(R.drawable.icon_sort_individual_off);
                item.setIcon(R.drawable.icon_sort_whole_on);

                SPFragment.intentGroupAllFragment(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
