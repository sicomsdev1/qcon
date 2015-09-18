package com.sicoms.smartplug.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.ImgFileVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.util.progress.SPProgressDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pc-11-user on 2015-03-06.
 */
public class SPUtil {

    private static final String TAG = "SPUtil";

    public static int getDIPtoPixel(int dipValue) {
        int px = (int) (dipValue * Resources.getSystem().getDisplayMetrics().density);
        return px;
    }

    /**
     * 단말기 density 구함
     * @param con
     * 사용법 : if(getDensity(context) == 2f && (float으로 형변환해서 사용 해야함.)
     */
    public static float getDensity(Context con) {
        float density = 0.0f;
        density  = con.getResources().getDisplayMetrics().density;
        Log.d(TAG, "density = " + density);
        return density;
    }

    /**
     * px을 dp로 변환
     * @param con
     * @param px
     * @return dp
     */
    public static int getPxToDp(Context con, int px) {
        float density = 0.0f;
        density  = con.getResources().getDisplayMetrics().density;
        Log.d(TAG, "density = " + density);
        return (int)(px / density);
    }

    /**
     * dp를 px로 변환
     * @param con
     * @param dp
     * @return px
     */
    public static int getDpToPix(Context con, double dp) {
        float density = 0.0f;
        density  = con.getResources().getDisplayMetrics().density;
        Log.d(TAG, "density = " + density);
        return (int)(dp * density + 0.5);
    }

    /**
     * 단말기 가로 해상도 구하기
     * @param activity
     * @return width
     */
    public static int getScreenWidth(Activity activity) {
        int width = 0;
        width = activity.getWindowManager().getDefaultDisplay().getWidth();
        Log.i(TAG, "Screen width = " + width);
        return width;
    }

    /**
     * 단말기 세로 해상도 구하기
     * @param activity
     * @return hight
     */
    public static int getScreenHeight(Activity activity) {
        int height = 0;
        height = activity.getWindowManager().getDefaultDisplay().getHeight();
        Log.i(TAG, "Screen height = " + height);
        return height;
    }

    /**
     * 단말기 가로 해상도 구하기
     * @param context
     */
    public static int getScreenWidth(Context context) {
        Display dis = ((WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = dis.getWidth();
        Log.i(TAG, "Screen Width = " + width);
        return width;
    }

    /**
     * 단말기 세로 해상도 구하기
     * @param context
     */
    public static int getScreenHeight(Context context) {
        Display dis = ((WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int height = dis.getHeight();
        Log.i(TAG, "Screen height = " + height);
        return height;
    }

    public static void sleep(int msec){
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setBackgroundForLinear(Context context, String placeImage){
        LinearLayout bg = (LinearLayout) ((Activity) context).findViewById(R.id.bg);

        Bitmap bitmap = SPUtil.getBackgroundImage(context);
        if (bitmap != null) {
            bitmap = BlurEffect.blur(context, bitmap, SPConfig.BLUR_RADIUS);
            bg.setBackground(new BitmapDrawable(context.getResources(), bitmap));
        } else {
            if( placeImage.contains(SPConfig.PLACE_DEFAULT_IMAGE_NAME)){
                int resId = SPUtil.getDrawableResourceId(context, placeImage);
                bg.setBackgroundResource(resId);
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setBackgroundForLinear(Context context, View view, String placeImage){
        LinearLayout bg = (LinearLayout) view.findViewById(R.id.bg);

        if( placeImage.contains(SPConfig.PLACE_DEFAULT_IMAGE_NAME)){
            int resId = SPUtil.getDrawableResourceId(context, placeImage);
            bg.setBackgroundResource(resId);
        } else {
            String imagePath = SPConfig.FILE_PATH + placeImage;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                bg.setBackground(new BitmapDrawable(context.getResources(), bitmap));
            } else {
                bg.setBackgroundResource(R.drawable.dpbg_01);
            }
        }
    }

    public static Bitmap getBackgroundImage(Context context){
        PlaceVo placeVo = PlaceService.loadLastPlace(context);
        if( placeVo == null){
            return null;
        }
        Bitmap bitmap;
        String imageName = placeVo.getPlaceImg();
        if( imageName.contains(SPConfig.PLACE_DEFAULT_IMAGE_NAME)){
            int resId = SPUtil.getDrawableResourceId(context, imageName);
            bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        } else {
            String imagePath = SPConfig.FILE_PATH + imageName;
            bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.dpbg_01);
            }
        }

        return bitmap;
    }

    public static File createFile(String path, String fileName){
        File file = new File(path);
        if( !file.exists()){
            file.mkdirs();
        }

        return new File(path, fileName);
    }

    public static byte[] getByte(String value){
        String data = value;
        data = data.replaceAll("\\s{1,}+", "");

        byte [] output = new byte[(data.length() + 1) / 2];
        int i = 0;
        int idx = 0;


        for ( i = 0; i < data.length(); ++i ) {
            if ( (i % 2) != 0 ) {
                try {
                    output[idx] = (byte)(Integer.parseInt(data.substring(i-1, i+1), 16) & 0xFF);
                } catch ( NumberFormatException e ) {
                    // insert 0 here as this is an invalid character
                    output[idx] = 0;
                }
                ++idx;
            }
        }

        if ( (i % 2) != 0 ) {
    			/* We must have missed out the last character, as there are odd number of characters */
            try {
                output[idx] = Byte.parseByte(data.substring(i-1,  i), 16);
            } catch ( NumberFormatException e ) {
                // insert 0 here as this is an invalid character
                output[idx] = 0;
            }
            ++idx;
        }
        return output;
    }

    public static float getLastWh(String wh){
        int nWh = Integer.parseInt(wh, 16);
        String strWh = String.valueOf(nWh);
        float fWh = Float.parseFloat(strWh);
        return fWh;
    }

    public static float getLastW(String w){
        int nW = Integer.parseInt(w, 16) / 100;
        String strW = String.valueOf(nW);
        float fW = Float.parseFloat(strW);
        return fW;
    }

    public static void showToast(final Context context, final String message){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }

    public static boolean isEmail(String id){
        String mail_rge = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern pattern = Pattern.compile(mail_rge);
        Matcher matcher = pattern.matcher(id);

        return matcher.matches();
    }

    public static boolean isCharacters(String id){ // 특수문자 있을 경우 false
        return id.matches("[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝]*");
    }

    public static void fileCopy(String inFileName, String outFileName){
        try {
            FileInputStream fis = new FileInputStream(inFileName);
            FileOutputStream fos = new FileOutputStream(outFileName);

            int data = 0;
            while((data=fis.read())!=-1) {
                fos.write(data);
            }
            fis.close();
            fos.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static SPProgressDialog mDlg;
    private static Handler mDlgHandler = new Handler(Looper.getMainLooper());
    public static void showDialog(Context context){
        mDlg = new SPProgressDialog(context);
        mDlg.show();
        mDlgHandler.postDelayed(mDialogTimeout, SPConfig.DIALOG_TIMEOUT);
    }
    public static void showDialog(Context context, int delayMilis){
        mDlg = new SPProgressDialog(context);
        mDlg.show();
        mDlgHandler.postDelayed(mDialogTimeout, delayMilis);
    }
    public static void dismissDialog(){
        if( mDlg != null) {
            mDlg.dismiss();
        }
    }
    private static Runnable mDialogTimeout = new Runnable() {
        @Override
        public void run() {
            mDlg.dismiss();
        }
    };

    public static boolean isNetwork(Context context) { // network 연결 상태 확인
        try {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState(); // wifi
            if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                return true;
            }

            NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState(); // mobile ConnectivityManager.TYPE_MOBILE
            if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
                return true;
            }

        } catch (NullPointerException e) {
            return false;
        }

        return false;
    }

    public static String getConvertPowerToCharge(float wh, int basic){
        float fCharge = (wh/1000) * basic;
        String charge = String.format("%,d", (int) fCharge);

        return charge;
    }
    public static float getForecastPower(float wh){
        float forecastWatt = 0.0f;

        return forecastWatt;
    }

    public static File saveBitmapImage(String fileName, Bitmap bitmap){
        if( bitmap != null) {
            FileOutputStream fos = null;
            File file = new File(SPConfig.FILE_PATH, fileName);
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
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            return file;
        }
        return null;
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (view == null)
            return;

        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getRandomId(){
        int a = (int) (Math.random()*9);
        int b = (int) (Math.random()*9);
        int c = (int) (Math.random()*9);
        int d = (int) (Math.random()*9);
        int e = (int) (Math.random()*9);

        return String.valueOf(a+b+c+d+e);
    }

    public static int getDrawableResourceId(Context context, String imageName){
        String drawableName = "@drawable/" + imageName;
        int resId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
        return resId;
    }

    public static boolean isIntentAvailable( Context context, String action){
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent( action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    // 기본 내장 카메라로만 실행 될 수 있게
    public static Intent getIntentDefaultCamera( Context context){
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE);
        final ResolveInfo info = packageManager.resolveActivity(intent, 0);
        intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));

        return intent;
    }
}
