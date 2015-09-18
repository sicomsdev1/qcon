package com.sicoms.smartplug.menu.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class PlaceSettingActivity extends ActionBarActivity {

    private static final String TAG = PlaceSettingActivity.class.getSimpleName();

    private CharSequence mTitle = "플레이스 설정";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_bg);

        mTitle = PlaceService.loadLastPlace(this).getPlaceName() + " " + mTitle;
        getSupportActionBar().setTitle(mTitle);

        PlaceVo placeVo = PlaceService.loadLastPlace(this);
        if( placeVo != null) {
            SPUtil.setBackgroundForLinear(this, placeVo.getPlaceImg());
        }

        SPFragment.intentPlaceSettingFragment(this);
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
