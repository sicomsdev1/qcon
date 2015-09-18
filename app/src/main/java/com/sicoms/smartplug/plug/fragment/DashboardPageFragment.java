package com.sicoms.smartplug.plug.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;

/**
 * Created by pc-11-user on 2015-03-06.
 */
public class DashboardPageFragment extends Fragment {

    private static final String ARG_PAGE = "page";

    private LinearLayout linPage;
    private ImageView ivPage;

    public static DashboardPageFragment newInstance(int page) {
        DashboardPageFragment fragment = new DashboardPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_page, container, false);
        Bundle args = getArguments();
        int page = 0;
        if( args != null)
            page = args.getInt(ARG_PAGE);

        linPage = (LinearLayout) view.findViewById(R.id.lin_page);
        ivPage = (ImageView) view.findViewById(R.id.iv_page_bg);

        if( page == SPConfig.PLUG_EDIT_PAGE01) {
            ivPage.setBackgroundResource(R.drawable.dashboard_bg01);
        } else if( page == SPConfig.PLUG_EDIT_PAGE02) {
            ivPage.setBackgroundResource(R.drawable.dashboard_bg02);
        }  else if( page == SPConfig.PLUG_EDIT_PAGE03) {
            ivPage.setBackgroundResource(R.drawable.dashboard_bg03);
        }

        return view;
    }
}
