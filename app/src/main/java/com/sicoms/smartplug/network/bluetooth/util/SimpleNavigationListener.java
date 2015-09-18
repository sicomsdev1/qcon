/******************************************************************************
 *  Copyright (C) Cambridge Silicon Radio Limited 2014
 *
 *  This software is provided to the customer for evaluation
 *  purposes only and, as such early feedback on performance and operation
 *  is anticipated. The software source code is subject to change and
 *  not intended for production. Use of developmental release software is
 *  at the user's own risk. This software is provided "as is," and CSR
 *  cautions users to determine for themselves the suitability of using the
 *  beta release version of this software. CSR makes no warranty or
 *  representation whatsoever of merchantability or fitness of the product
 *  for any particular purpose or use. In no event shall CSR be liable for
 *  any consequential, incidental or special damages whatsoever arising out
 *  of the use of or inability to use this software, even if the user has
 *  advised CSR of the possibility of such damages.
 *
 ******************************************************************************/

package com.sicoms.smartplug.network.bluetooth.util;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.Toast;

import com.sicoms.smartplug.R;

/**
 * Handles displaying the correct fragment when options are selected from the navigation menu in the action bar.
 * 
 */
public class SimpleNavigationListener implements ActionBar.OnNavigationListener {
    private FragmentManager mFragmentManager;
    private Activity mActivity;

    // True if navigation away from network security settings is allowed.
    private boolean mEnableNavigation;
    
    public static final int POSITION_LIGHT_CONTROL = 0;    
    public static final int POSITION_ASSOCIATION = 1;
    public static final int POSITION_GROUP_CONFIG = 2;
    public static final int POSITION_NETWORK_SETTINGS = 3;        
    public static final int POSITION_ABOUT = 4;

    public SimpleNavigationListener(FragmentManager fragmentManager, Activity activity) {
        mFragmentManager = fragmentManager;
        mEnableNavigation = true;
        mActivity = activity;
    }

    public void setNavigationEnabled(boolean enabled) {
        mEnableNavigation = enabled;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Fragment fragment = null;

        if (mEnableNavigation || itemPosition == POSITION_NETWORK_SETTINGS) {
            switch (itemPosition) {           
            case POSITION_LIGHT_CONTROL:
                //fragment = new LightControlFragment();
                break;
            case POSITION_ASSOCIATION:
                //fragment = new AssociationFragment();
                break;            	
            case POSITION_GROUP_CONFIG:
                //fragment = new GroupAssignFragment();
                break;
            case POSITION_NETWORK_SETTINGS:
                //fragment = new SecuritySettingsFragment();
                break;                        
            case POSITION_ABOUT:
                //fragment = new AboutFragment();
                break;
            }

            if (fragment != null) {
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.replace(android.R.id.content, fragment);
                ft.commit();
            }
        }
        else {
            Toast.makeText(mActivity, "key required", Toast.LENGTH_SHORT)
                    .show();
            mActivity.getActionBar().setSelectedNavigationItem(POSITION_NETWORK_SETTINGS);
        }

        return true;
    }

}
