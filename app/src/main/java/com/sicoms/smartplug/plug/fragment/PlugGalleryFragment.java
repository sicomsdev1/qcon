package com.sicoms.smartplug.plug.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.menu.interfaces.ImageSelectedResultCallbacks;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.plug.adapter.PlugGalleryAdapter;
import com.sicoms.smartplug.plug.event.PlugEvent;
import com.sicoms.smartplug.util.DividerItemDecoration;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class PlugGalleryFragment extends Fragment {

    private static final String TAG = PlugGalleryFragment.class.getSimpleName();

    private CharSequence mTitle = "플러그 기본 이미지";

    private Context mContext;
    private static ImageSelectedResultCallbacks mCallbacks;

    private PlugEvent mEvent;

    private RecyclerView mRecyclerView;
    private PlugGalleryAdapter mAdapter;

    public static PlugGalleryFragment newInstance(ImageSelectedResultCallbacks callbacks) {
        PlugGalleryFragment fragment = new PlugGalleryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        mCallbacks = callbacks;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_default_gallery, container, false);

        mContext = getActivity();
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);

        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
        if( placeVo != null) {
            SPUtil.setBackgroundForLinear(mContext, placeVo.getPlaceImg());
        }

        mEvent = new PlugEvent(mContext);
        mEvent.setOnImageSelectedResultCallbacks(mCallbacks);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_gallery);

        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 4));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL_LIST));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.getItemAnimator().setAddDuration(1000);
        mRecyclerView.getItemAnimator().setChangeDuration(1000);
        mRecyclerView.getItemAnimator().setMoveDuration(1000);
        mRecyclerView.getItemAnimator().setRemoveDuration(1000);

        mAdapter = new PlugGalleryAdapter(mContext);
        mAdapter.SetOnItemClickListener(mEvent);

        mRecyclerView.setAdapter(mAdapter);

        fillAdapterData();

        return view;
    }

    private void fillAdapterData(){
        mAdapter.addItem("dppbg_00");
        mAdapter.addItem("dppbg_01");
        mAdapter.addItem("dppbg_02");
        mAdapter.addItem("dppbg_03");
        mAdapter.addItem("dppbg_04");
        mAdapter.addItem("dppbg_05");
        mAdapter.addItem("dppbg_06");
        mAdapter.addItem("dppbg_07");
        mAdapter.addItem("dppbg_08");
        mAdapter.addItem("dppbg_09");
        mAdapter.addItem("dppbg_10");
        mAdapter.addItem("dppbg_11");
        mAdapter.addItem("dppbg_12");
        mAdapter.addItem("dppbg_13");
        mAdapter.addItem("dppbg_14");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
