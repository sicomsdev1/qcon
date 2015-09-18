package com.sicoms.smartplug.member.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.member.adapter.MemberAdapter;
import com.sicoms.smartplug.member.event.MemberEvent;
import com.sicoms.smartplug.member.service.MemberService;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.BlurEffect;
import com.sicoms.smartplug.util.DividerItemDecoration;
import com.sicoms.smartplug.util.SPUtil;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class MemberFragment extends Fragment implements HttpResponseCallbacks {

    private static final String TAG = MemberFragment.class.getSimpleName();

    private Context mContext;
    private View mView;

    private MemberEvent mEvent;
    private MemberService mService;
    private List<UserVo> mVoList;

    private TextView mTvOnCount;
    private TextView mTvAllCount;
    private ImageView mIvAddMemberBtn;

    private RecyclerView mRecyclerView;
    private MemberAdapter mAdapter;

    private ActionMode mActionMode;

    public static MemberFragment newInstance() {
        MemberFragment fragment = new MemberFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void initialize(){
        Bitmap bitmap = SPUtil.getBackgroundImage(mContext);
        if( bitmap != null) {
            bitmap = BlurEffect.blur(mContext, bitmap, SPConfig.BLUR_RADIUS);
            mView.setBackground(new BitmapDrawable(getResources(), bitmap));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mContext = getActivity();
        mVoList = new ArrayList<>();
        mEvent = new MemberEvent(mContext);
        mService = new MemberService(mContext);
        mService.setOnHttpResponseCallbacks(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_member, container, false);
        mView = view;
        initialize();

        mTvOnCount = (TextView) view.findViewById(R.id.tv_on_count);
        mTvAllCount = (TextView) view.findViewById(R.id.tv_all_count);
        mIvAddMemberBtn = (ImageView)view.findViewById(R.id.iv_add_member_btn);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_member);

        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL_LIST));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.getItemAnimator().setAddDuration(1000);
        mRecyclerView.getItemAnimator().setChangeDuration(1000);
        mRecyclerView.getItemAnimator().setMoveDuration(1000);
        mRecyclerView.getItemAnimator().setRemoveDuration(1000);

        mAdapter = new MemberAdapter(mContext);
        mAdapter.SetOnItemClickListener(mEvent);


        try {
            int auth = Integer.parseInt(PlaceService.loadLastPlace(mContext).getAuth());
            if (auth == SPConfig.MEMBER_MASTER) {
                mIvAddMemberBtn.setOnClickListener(mEvent);
                mAdapter.SetOnItemLongClickListener(new MemberAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(View view, int position) {
                        if (mActionMode != null) {
                            return;
                        }

                        mActionMode = ((Activity) mContext).startActionMode(mActionModeCallback);
                        view.setSelected(true);

                        mAdapter.setMode(SPConfig.MODE_CHECK);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            } else {
                mIvAddMemberBtn.setVisibility(View.GONE);
            }
        } catch (NumberFormatException nfe){
            nfe.printStackTrace();
        }

        mRecyclerView.setAdapter(mAdapter);

        fillAdapterData();

        return view;
    }

    private void fillAdapterData(){
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVoList = mService.selectDbMemberList();

                if (mVoList == null || mVoList.size() < 1) {
                    mService.requestSelectMemberList();
                    return;
                }
                Collections.sort(mVoList, memberComparator);

                mAdapter.removeAll();
                mAdapter.addAll(mVoList);
                mAdapter.notifyDataSetChanged();

                List<UserVo> onVoList = new ArrayList<>();
                for (int cnt = 0; cnt < mVoList.size(); cnt++) {
                    if (mVoList.get(cnt).isOn()) {
                        onVoList.add(mVoList.get(cnt));
                    }
                }
                mTvOnCount.setText(String.valueOf(onVoList.size()));
                mTvAllCount.setText(String.valueOf(mVoList.size()));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if( mActionMode != null){
            mActionMode.finish();
        }
        mAdapter.setMode(SPConfig.MODE_NORMAL);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                if( ((ActionBarActivity) mContext).getSupportFragmentManager().getBackStackEntryCount()>0) {
                    ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
                } else {
                    ((Activity) mContext).finish();
                }
                break;
            case R.id.action_sync :
                mService.requestSelectMemberList();
                SPUtil.showDialog(mContext);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_delete, menu);
            mAdapter.SetOnItemClickListener(null);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    mService.requestDeleteMember(mAdapter.getCheckedItems());
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mAdapter.setMode(SPConfig.MODE_NORMAL);
            mAdapter.notifyDataSetChanged();
            mAdapter.SetOnItemClickListener(mEvent);
        }
    };

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(responseVo.getResult());
                if(CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_SELECT_USER_LIST) {
                    if (resultNum == HttpConfig.HTTP_SUCCESS) {
                        List<UserVo> userVoList = new Gson().fromJson(responseVo.getJsonStr(), new TypeToken<List<UserVo>>() {
                        }.getType());
                        if (userVoList == null) {
                            SPUtil.dismissDialog();
                            SPUtil.showToast(mContext, "동기화에 실패하였습니다.");
                            return;
                        }
                        if (mService.insertDbMemberList(userVoList)) {
                            fillAdapterData();
                            //Toast.makeText(mContext, "동기화를 완료하였습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            SPUtil.showToast(mContext, "동기화에 실패하였습니다.");
                        }
                    } else {
                        SPUtil.showToast(mContext, "동기화에 실패하였습니다.");
                    }
                } else if( CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_DELETE_USER){
                    if (resultNum == HttpConfig.HTTP_SUCCESS) {
                        List<UserVo> userVoList = new Gson().fromJson(responseVo.getJsonStr(), new TypeToken<List<UserVo>>() {
                        }.getType());
                        if (userVoList == null) {
                            SPUtil.dismissDialog();
                            SPUtil.showToast(mContext, "사용자를 삭제하지 못했습니다.");
                            return;
                        }
                        mService.deleteDbMemberList(userVoList);
                        mAdapter.removeCheckedItem();
                        mAdapter.notifyDataSetChanged();
                        fillAdapterData();
                    } else {
                        SPUtil.showToast(mContext, "사용자를 삭제하지 못했습니다.");
                    }
                }
            } catch (JsonParseException jpe){
                jpe.printStackTrace();
            } catch (NumberFormatException nfe){
                nfe.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            Toast.makeText(mContext, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
        SPUtil.dismissDialog();
    }


    private final static Comparator<UserVo> memberComparator = new Comparator<UserVo>() {
        private final Collator collator = Collator.getInstance();
        @Override
        public int compare(UserVo object1,UserVo object2) {
            return collator.compare(object1.getUserName(), object2.getUserName());
        }
    };
}
