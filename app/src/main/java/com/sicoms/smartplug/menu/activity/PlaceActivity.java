package com.sicoms.smartplug.menu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class PlaceActivity extends ActionBarActivity {

    private static final String TAG = PlaceActivity.class.getSimpleName();

    private CharSequence mTitle = "플레이스";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_bg);

        getSupportActionBar().setTitle(mTitle);

        PlaceVo placeVo = PlaceService.loadLastPlace(this);
        if( placeVo != null) {
            SPUtil.setBackgroundForLinear(this, placeVo.getPlaceImg());
        }


        SPFragment.intentPlaceListFragment(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_place, menu);
        MenuItem outItem = menu.findItem(R.id.action_out);
        outItem.setVisible(false);
        MenuItem syncItem = menu.findItem(R.id.action_sync);
        syncItem.setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case android.R.id.home :
                // 해당 플레이스 정보 로드
                // 로컬 플레이스 이미지와 클라우드의 플레이스 이미지 이름(placeid_time_place.jpg) 비교 후 다르면 다운로드
//                mService.requestCheckPlaceImage(mPlaceVo.getPlaceImg());
//
//                // 유저 정보
//                mService.requestCheckUserInfo(mPlaceVo.getPlaceId());
//
//                // 그룹 정보
//                mService.requestCheckGroupInfo(mPlaceVo.getPlaceId());


                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if( getSupportFragmentManager().getBackStackEntryCount()>0) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }
}
