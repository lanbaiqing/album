package com.lbq.album.browser.widget;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.lbq.album.browser.R;

/**
 * Created by 1174607250 on 2018/7/12.
 */

public class PopupWindow extends android.widget.PopupWindow
{
    public PopupWindow(int width, int height)
    {
        super(width, height);
    }
    @Override
    public void showAsDropDown(View anchor)
    {
        super.showAsDropDown(anchor);
        getContentView().startAnimation(AnimationUtils.loadAnimation(anchor.getContext(), R.anim.anim_20180712_popup_in));
    }
    @Override
    public void dismiss()
    {
        Animation animation = AnimationUtils.loadAnimation(getContentView().getContext(), R.anim.anim_20180712_popup_out);
        getContentView().startAnimation(animation);
        getContentView().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                PopupWindow.super.dismiss();
            }
        },animation.getDuration());
    }
}
