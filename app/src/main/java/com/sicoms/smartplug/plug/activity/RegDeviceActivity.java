package com.sicoms.smartplug.plug.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class RegDeviceActivity extends ActionBarActivity {

    private static final String TAG = RegDeviceActivity.class.getSimpleName();

    private CharSequence mTitle = "새로운 장치 추가";

    private Menu mMenu;

    private void initialize(){
        PlaceVo placeVo = PlaceService.loadLastPlace(this);
        SPUtil.setBackgroundForLinear(this, placeVo.getPlaceImg());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bg);

        getSupportActionBar().setTitle(mTitle);
        initialize();

        SPFragment.intentRegDeviceFragment(this);
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
        }

        return super.onOptionsItemSelected(item);
    }
}
