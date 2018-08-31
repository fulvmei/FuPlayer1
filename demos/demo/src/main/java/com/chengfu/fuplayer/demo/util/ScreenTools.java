package com.chengfu.fuplayer.demo.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class ScreenTools {

    public static float getTextViewFontWidth(TextView tv,boolean isGetEveryCh){
        String text= tv.getText().toString();
        float width = 0;
        Paint paint = new Paint();
        paint.setTextSize(tv.getTextSize());
        float text_width = paint.measureText(text);//得到总体长度
        if (isGetEveryCh) {
            width = text_width/text.length();//每一个字符的长度其中paint有很多属性可以设置，会影响长度
            return width;
        }
        return text_width;//返回整个长度

    }

    public static boolean TextViewIsOverlayScreen(Context context,TextView tv,float space){
        float tw=getTextViewFontWidth(tv, false);
        float sw=getScreenWidth(context)-space;
        return tw>sw;
    }

    /**
     * 锟斤拷锟斤拷锟侥伙拷叨锟�
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context)
    {
        WindowManager wm = (WindowManager) context

                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

//	public static int getScreenWidth(Context context){
//		DisplayMetrics dm = new DisplayMetrics();
//		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
//	}


    /**
     * 锟斤拷锟斤拷锟侥伙拷锟斤拷
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context)
    {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 锟斤拷锟阶刺拷锟斤拷母叨锟�
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context)
    {

        int statusHeight = -1;
        try
        {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 锟斤拷取锟斤拷前锟斤拷幕锟斤拷图锟斤拷锟斤拷状态锟斤拷
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithStatusBar(Activity activity)
    {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;

    }

    /**
     * 锟斤拷取锟斤拷前锟斤拷幕锟斤拷图锟斤拷锟斤拷锟斤拷状态锟斤拷
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity)
    {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height
                - statusBarHeight);
        view.destroyDrawingCache();
        return bp;

    }

    public static float getScreenDensity(Context context){
        return context.getResources().getDisplayMetrics().density;
    }
    public static int dip2px(Context context, float dpValue)
    {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    public static int sp2px(Context context, float spValue){
        float scale = context.getResources().getDisplayMetrics().density;
        System.out.println(">>>>>>>>>>>>>>>>>>屏幕密度："+scale);
        return (int) (spValue * scale + 0.5f);
    }
    public static float sp2px_fix(Context context, float spValue){
        float scale = context.getResources().getDisplayMetrics().density;
        System.out.println(">>>>>>>>>>>>>>>>>>屏幕密度："+scale);
        return getRawSize(context,TypedValue.COMPLEX_UNIT_SP, spValue)/scale;
    }
    public static int dp2px(Context context,int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,context.getResources().getDisplayMetrics());
    }
    public static int getDimenPix(Context context,int res){
        return context.getResources().getDimensionPixelSize(res);
    }
    /**
     * 获取当前分辨率下指定单位对应的像素大小（根据设备信息）
     * px,dip,sp转换 px
     *
     * Paint.setTextSize()单位为px
     *
     * 代码摘自：TextView.setTextSize()
     *
     * @param unit  TypedValue.COMPLEX_UNIT_xxx
     * @param size
     */
    public static float getRawSize(Context c,int unit, float size) {
        Resources r;

        if (c == null)
            r = Resources.getSystem();
        else
            r = c.getResources();

        return TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
    }
    public static int px2dip(Context context, float pxValue)
    {
        if (context!=null){
            float mDensity = context.getResources().getDisplayMetrics().density;
            if(Math.abs(mDensity-0)<0.0001){
                mDensity = context.getResources().getDisplayMetrics().density;
            }
            return (int)(pxValue/mDensity + 0.5f);

        }
        return 0;
    }

}

