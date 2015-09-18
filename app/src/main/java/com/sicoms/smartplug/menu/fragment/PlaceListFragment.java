package com.sicoms.smartplug.menu.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.CommonService;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.ImgFileVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.menu.adapter.PlaceAdapter;
import com.sicoms.smartplug.menu.event.PlaceEvent;
import com.sicoms.smartplug.menu.interfaces.PlaceResultCallbacks;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpBitmapResponseCallbacks;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.util.DividerItemDecoration;
import com.sicoms.smartplug.util.SPUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gudnam on 2015. 5. 19..
 */
public class PlaceListFragment extends Fragment implements PlaceResultCallbacks, HttpResponseCallbacks, HttpBitmapResponseCallbacks {

    private static final String TAG = PlaceListFragment.class.getSimpleName();

    private Activity mActivity;
    private View mView;

    private UserVo mUserVo;
    private PlaceEvent mEvent;
    private PlaceService mService;
    private CommonService mCommonService;

    private ImageView mIvAddPlaceBtn;
    private RecyclerView mRecyclerView;
    private PlaceAdapter mAdapter;

    public static PlaceListFragment newInstance() {
        PlaceListFragment fragment = new PlaceListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void initialize() {
        Bitmap bitmap = SPUtil.getBackgroundImage(mActivity);
        if (bitmap != null) {
            mView.setBackground(new BitmapDrawable(getResources(), bitmap));
        } else {
            // 이미지 다운로드
            PlaceVo placeVo = PlaceService.loadLastPlace(mActivity);
            if( placeVo == null){
                return;
            }
            String placeImgPath = placeVo.getPlaceImg();
            ImgFileVo imgFileVo = new ImgFileVo(placeImgPath);
            mCommonService.requestDownloadImage(imgFileVo);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();

        mUserVo = LoginService.loadLastLoginUser(mActivity);
        mEvent = new PlaceEvent(mActivity, this);
        mService = new PlaceService(mActivity);
        mService.setOnHttpResponseCallbacks(this);
        mCommonService = new CommonService(mActivity);
        mCommonService.setOnHttpBitmapResponseCallbacks(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_place_list, container, false);
        mView = view;

        initialize();
        mIvAddPlaceBtn = (ImageView) view.findViewById(R.id.iv_add_place_btn);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_place);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.HORIZONTAL_LIST));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.getItemAnimator().setAddDuration(1000);
        mRecyclerView.getItemAnimator().setChangeDuration(1000);
        mRecyclerView.getItemAnimator().setMoveDuration(1000);
        mRecyclerView.getItemAnimator().setRemoveDuration(1000);

        mAdapter = new PlaceAdapter(mActivity);
        mAdapter.setPlaceResultCallbacks(this);

        mRecyclerView.setAdapter(mAdapter);
        mEvent.setAdapter(mAdapter);

        mIvAddPlaceBtn.setOnClickListener(mEvent);
        mAdapter.SetOnItemClickListener(mEvent);

        SPUtil.showDialog(mActivity);

        mService.requestSelectPlaceList(mUserVo);

        return view;
    }

    private void fillAdapterData() {
        List<PlaceVo> voList = mService.selectDbPlaceList(); // Local DB 에서 플레이스 리스트 로드
        if (voList != null) {
            Collections.sort(voList, placeComparator);
            mAdapter.removeAll();
            mAdapter.addAll(voList);
            mAdapter.notifyDataSetChanged();
        }

        SPUtil.dismissDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        fillAdapterData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem outItem = menu.findItem(R.id.action_out);
        outItem.setVisible(false);
        MenuItem syncItem = menu.findItem(R.id.action_sync);
        syncItem.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (((ActionBarActivity) mActivity).getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    ((ActionBarActivity) mActivity).getSupportFragmentManager().popBackStack();
                } else {
                    mActivity.finish();
                }
                break;
            case R.id.action_sync:
                mService.requestSelectPlaceList(mUserVo);
                SPUtil.showDialog(mActivity);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAddPlaceResult(final PlaceVo placeVo) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!mService.updateDbPlace(placeVo)) {
                    // 저장 실패
                    Toast.makeText(mActivity, "장소를 저장하지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
                SPUtil.showToast(mActivity, placeVo.getPlaceName() + "장소를 저장하였습니다.");
                mAdapter.addItem(placeVo);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onModPlaceResult(final PlaceVo placeVo) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!mService.updateDbPlace(placeVo)) {
                    // 저장 실패
                    Toast.makeText(mActivity, "장소를 저장하지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
                SPUtil.showToast(mActivity, placeVo.getPlaceName() + "장소를 수정하였습니다.");
                mAdapter.updateItem(placeVo);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onOutPlaceResult(final PlaceVo placeVo) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PlaceVo lastPlaceVo = PlaceService.loadLastPlace(mActivity);
                if (lastPlaceVo != null) {
                    if (lastPlaceVo.getPlaceId().equalsIgnoreCase(placeVo.getPlaceId())) {
                        PlaceService.removeLastPlace(mActivity);
                    }
                }
                if (!mService.removeDbPlace(placeVo)) {
                    SPUtil.showToast(mActivity, placeVo.getPlaceName() + "장소에서 나가지 못했습니다.");
                }
                SPUtil.showToast(mActivity, placeVo.getPlaceName() + "장소를 나갔습니다.");
                mAdapter.removeItem(placeVo);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onSelecteComplete(PlaceVo placeVo) {
        PlaceService.saveLastPlace(mActivity, placeVo);
        SPConfig.IS_FIRST = true;
        initialize();
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {

        if (result == HttpConfig.HTTP_SUCCESS) {
            try {
                if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_SELECT_PLACE_LIST) {
                    HttpResponseVo responseVo = new Gson().fromJson(value, HttpResponseVo.class);
                    int resultNum = Integer.parseInt(responseVo.getResult());
                    if (resultNum == HttpConfig.HTTP_SUCCESS) {
                        List<PlaceVo> placeVoList = new Gson().fromJson(responseVo.getJsonStr(), new TypeToken<List<PlaceVo>>() {
                        }.getType());

                        if (placeVoList != null) {
                            mService.updateDbPlaceList(placeVoList); // Local DB 에 플레이스 리스트 업데이트
                        }
                        fillAdapterData();
                    } else {
                        Toast.makeText(mActivity, "플레이스 요청에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JsonParseException jpe) {
                jpe.printStackTrace();
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(mActivity, "서버와의 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
        SPUtil.dismissDialog();
    }

    @Override
    public void onHttpBitmapResponseResultStatus(int type, int result, String fileName, Bitmap bitmap) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            if (bitmap != null) {
                FileOutputStream fos = null;
                File file = new File(SPConfig.FILE_PATH + fileName);
                try {
                    file.createNewFile();
                    fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                            initialize();
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        }
    }

    private final static Comparator<PlaceVo> placeComparator = new Comparator<PlaceVo>() {
        private final Collator collator = Collator.getInstance();
        @Override
        public int compare(PlaceVo object1,PlaceVo object2) {
            return collator.compare(object1.getPlaceName(), object2.getPlaceName());
        }
    };
}
