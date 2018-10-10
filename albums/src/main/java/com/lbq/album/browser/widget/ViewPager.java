package com.lbq.album.browser.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPager extends android.support.v4.view.ViewPager
{
    public ViewPager(Context context)
    {
        super(context);
    }
    public ViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    public interface OnClickListener
    {
        void onClick(int type);
    }
    private OnClickListener onClickListener;
    public void setOnClickListener(OnClickListener onClickListener)
    {
        this.onClickListener = onClickListener;
    }

    private float y1,x1;
    private final int click = 1, doubleClick = 2, longClick = 3;
    private Handler handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            if (onClickListener != null)
                onClickListener.onClick(msg.what);
            handler.removeMessages(msg.what);
            return false;
        }
    });
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        if (ev.getPointerCount() == 1)
        {
            switch (ev.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    y1 = ev.getRawY();
                    x1 = ev.getRawX();
                    if (handler.hasMessages(click))
                    {
                        handler.removeMessages(click);
                        handler.sendEmptyMessage(doubleClick);
                    }
                    else
                    {
                        handler.sendEmptyMessageDelayed(longClick, 500);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (y1 != ev.getRawY() && x1 != ev.getRawX())
                    {
                        handler.removeMessages(longClick);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (handler.hasMessages(longClick))
                    {
                        handler.removeMessages(longClick);
                        if (y1 == ev.getRawY() && x1 == ev.getRawX())
                        {
                            handler.sendEmptyMessageDelayed(click, 350);
                        }
                    }
                    break;
            }
        }
        else
        {
            handler.removeMessages(click);
            handler.removeMessages(longClick);
            handler.removeMessages(doubleClick);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        try
        {
            return super.onInterceptTouchEvent(ev);
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

}
