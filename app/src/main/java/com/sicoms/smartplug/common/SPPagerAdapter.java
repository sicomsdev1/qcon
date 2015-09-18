package com.sicoms.smartplug.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.util.SPUtil;

public class SPPagerAdapter extends FragmentStatePagerAdapter implements OnPageChangeListener {
    private final int PAGE_NUM = 3;

    private Fragment[] mTargetFragment;
    private View mView = null;
    private LinearLayout[] mPageMark = null;
    private int[] mResId = null;
    int mPrevPosition = 0;	//이전 포지션 값 초기화

    public SPPagerAdapter(FragmentManager fm, View view, Fragment targetFragment[], int resId[]) {
        super(fm);
        mView = view;
        mTargetFragment = targetFragment;
        mPageMark = new LinearLayout[PAGE_NUM];
        mPageMark[0] = (LinearLayout) mView.findViewById(R.id.mark1);
        mPageMark[1] = (LinearLayout) mView.findViewById(R.id.mark2);
        mPageMark[2] = (LinearLayout) mView.findViewById(R.id.mark3);
        mResId = resId;
        initPageMark();
    }

    private void initPageMark() {
        for(int i=0; i<mPageMark.length; i++)
        {
            ImageView iv = new ImageView(mView.getContext());	//페이지 표시 이미지 뷰 생성
            iv.setLayoutParams(new LayoutParams(SPUtil.getDIPtoPixel(15), SPUtil.getDIPtoPixel(12)));

            //첫 페이지 표시 이미지 이면 선택된 이미지로
            if(i==0)
                iv.setBackgroundResource(mResId[0]);
            else	//나머지는 선택안된 이미지로
                iv.setBackgroundResource(mResId[1]);

            //LinearLayout에 추가
            mPageMark[i].addView(iv);
        }
    }

    public void movePageMark(int position){

        int nLinPosition = position;
        mPageMark[nLinPosition].getChildAt(0).setBackgroundResource(mResId[0]);		//현재 페이지에 해당하는 페이지 표시 이미지 변경
        if( nLinPosition != mPrevPosition)
            mPageMark[mPrevPosition].getChildAt(0).setBackgroundResource(mResId[1]);	//이전 페이지에 해당하는 페이지 표시 이미지 변경

        mPrevPosition = nLinPosition;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        Fragment fragment = mTargetFragment[position]; // Default

        return fragment;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return PAGE_NUM;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageSelected(int position) {
        // TODO Auto-generated method stub
        movePageMark(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // TODO Auto-generated method stub
        return super.instantiateItem(container, position);
    }
}

