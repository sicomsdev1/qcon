package com.hollowsoft.library.slidingdrawer;

public interface OnDrawerScrollListener {

    /**
     * Invoked when the user starts dragging/flinging the drawer's handle.
     */
     void onScrollStarted();

    /**
     * Invoked when the user stops dragging/flinging the drawer's handle.
     */
     void onScrollEnded();
}
