package com.sicoms.smartplug.util.progress;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.Window;

import com.sicoms.smartplug.R;

/**
 * Created by gudnam on 2015. 5. 22..
 */
public class SPProgressDialog extends Dialog {

    public SPProgressDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_progress_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
