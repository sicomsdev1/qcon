package it.neokree.materialnavigationdrawer.elements;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import it.neokree.materialnavigationdrawer.R;
import it.neokree.materialnavigationdrawer.util.Utils;

/**
 * Created by neokree on 17/01/15.
 */
public class MaterialListView extends ListView{

    private CharSequence title;
    private int titleColor;

    private TextView text;
    private View view;

    public MaterialListView(Context ctx) {
        super(ctx);



    }

}
