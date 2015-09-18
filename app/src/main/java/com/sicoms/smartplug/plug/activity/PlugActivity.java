package com.sicoms.smartplug.plug.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 5. 28..
 */
public class PlugActivity extends ActionBarActivity {

    private static final String TAG = PlugActivity.class.getSimpleName();

    private void initialize(){
        PlaceVo placeVo = PlaceService.loadLastPlace(this);
        SPUtil.setBackgroundForLinear(this, placeVo.getPlaceImg());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bg);

        initialize();

        Intent intent = getIntent();
        PlugVo plugVo = new Gson().fromJson(intent.getStringExtra(SPActivity.ARG_PLUG_VO_NAME), PlugVo.class);

        SPFragment.intentPlugFragment(this, plugVo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_plug, menu);
        MenuItem item = menu.findItem(R.id.action_complete);
        item.setVisible(false);
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
        }

        return super.onOptionsItemSelected(item);
    }
}
