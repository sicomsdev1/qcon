package com.sicoms.smartplug.menu.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.menu.adapter.PlaceGalleryAdapter;
import com.sicoms.smartplug.menu.event.PlaceEvent;
import com.sicoms.smartplug.menu.interfaces.ImageSelectedResultCallbacks;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.util.DividerItemDecoration;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class PlaceGalleryFragment extends Fragment {

    private static final String TAG = PlaceGalleryFragment.class.getSimpleName();

    private CharSequence mTitle = "플레이스 기본 이미지";

    private Context mContext;
    private static ImageSelectedResultCallbacks mCallbacks;

    private PlaceEvent mEvent;

    private RecyclerView mRecyclerView;
    private PlaceGalleryAdapter mAdapter;private ImageView mIvFinishBtn;

    public static PlaceGalleryFragment newInstance(ImageSelectedResultCallbacks callbacks) {
        PlaceGalleryFragment fragment = new PlaceGalleryFragment();
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

        mEvent = new PlaceEvent(mContext);
        mEvent.setOnImageSelectedResultCallbacks(mCallbacks);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_gallery);

        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 1));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL_LIST));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.getItemAnimator().setAddDuration(1000);
        mRecyclerView.getItemAnimator().setChangeDuration(1000);
        mRecyclerView.getItemAnimator().setMoveDuration(1000);
        mRecyclerView.getItemAnimator().setRemoveDuration(1000);

        mAdapter = new PlaceGalleryAdapter(mContext);
        mAdapter.SetOnItemClickListener(mEvent);

        mRecyclerView.setAdapter(mAdapter);

        fillAdapterData();

        return view;
    }

    private void fillAdapterData(){
        mAdapter.addItem("dpbg_01");
        mAdapter.addItem("dpbg_02");
        mAdapter.addItem("dpbg_03");
        mAdapter.addItem("dpbg_04");
        mAdapter.addItem("dpbg_05");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem syncItem = menu.findItem(R.id.action_sync);
        syncItem.setVisible(false);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mContext.getString(R.string.menu_place_title));
    }
}
