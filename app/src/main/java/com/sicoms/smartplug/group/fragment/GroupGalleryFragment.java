package com.sicoms.smartplug.group.fragment;

import android.app.Activity;
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

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.group.service.GroupAllService;
import com.sicoms.smartplug.group.service.GroupService;
import com.sicoms.smartplug.menu.interfaces.ImageSelectedResultCallbacks;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.plug.adapter.PlugGalleryAdapter;
import com.sicoms.smartplug.plug.event.PlugEvent;
import com.sicoms.smartplug.util.DividerItemDecoration;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class GroupGalleryFragment extends Fragment {

    private static final String TAG = GroupGalleryFragment.class.getSimpleName();

    private CharSequence mTitle = "그룹 기본 이미지";

    private Context mContext;
    private static ImageSelectedResultCallbacks mCallbacks;

    private PlugEvent mEvent;

    private RecyclerView mRecyclerView;
    private PlugGalleryAdapter mAdapter;

    public static GroupGalleryFragment newInstance(ImageSelectedResultCallbacks callbacks) {
        GroupGalleryFragment fragment = new GroupGalleryFragment();
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
            SPUtil.setBackgroundForLinear(mContext, view, placeVo.getPlaceImg());
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
        mAdapter.addItem("dgpbg_00");
        mAdapter.addItem("dgpbg_01");
        mAdapter.addItem("dgpbg_02");
        mAdapter.addItem("dgpbg_03");
        mAdapter.addItem("dgpbg_04");
        mAdapter.addItem("dgpbg_05");
        mAdapter.addItem("dgpbg_06");
        mAdapter.addItem("dgpbg_07");
        mAdapter.addItem("dgpbg_08");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_complete);
        if( item != null) {
            item.setVisible(false);
        }
        MenuItem item2 = menu.findItem(R.id.action_group_menu);
        if( item2 != null){
            item2.setVisible(false);
        }
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

        GroupVo groupVo = new GroupService(mContext).loadLastGroup();
        mTitle = groupVo.getGroupName();
        ((ActionBarActivity) mContext).getSupportActionBar().setTitle(mTitle);
    }
}
