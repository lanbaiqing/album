package com.lbq.album.browser.widget;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.lbq.album.browser.R;

/**
 * Created by 1174607250 on 2018/7/16 0016.
 */

public class Dialog extends AppCompatDialog
{
    public Dialog(Context context, int theme)
    {
        super(context, theme);
    }
    public static class Query
    {
        private Dialog dialog;
        private Context context;
        public TextView title, content, confirm, cancel;
        public Query(Context context)
        {
            this.context = context;
        }
        public void show()
        {
            dialog.show();
        }
        public void cancel()
        {
            dialog.cancel();
        }
        public void setCancelable(boolean flag)
        {
            dialog.setCancelable(flag);
        }
        public void create()
        {
            dialog = new Dialog(context, R.style.dialog_20180712_style);
            dialog.setContentView(R.layout.dialog_20180712_query);
            title = dialog.findViewById(R.id.tv_title);
            content = dialog.findViewById(R.id.tv_content);
            confirm = dialog.findViewById(R.id.btn_confirm);
            cancel = dialog.findViewById(R.id.btn_cancel);
            cancel.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dialog.cancel();
                }
            });
        }
    }
    public static class Pick
    {
        public Dialog dialog;
        private Context context;
        public TextView camera, albums, cancel;
        public Pick(Context context)
        {
            this.context = context;
        }
        public void show()
        {
            dialog.show();
        }
        public void cancel()
        {
            dialog.cancel();
        }
        public void setCancelable(boolean flag)
        {
            dialog.setCancelable(flag);
        }
        public void create()
        {
            dialog = new Dialog(context, R.style.dialog_20180712_style);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setWindowAnimations(R.style.dialog_20180712_anim_from_bottom_to_top);
            dialog.setContentView(R.layout.dialog_20180712_pick);
            camera = dialog.findViewById(R.id.btn_camera);
            albums = dialog.findViewById(R.id.btn_albums);
            cancel = dialog.findViewById(R.id.btn_cancel);
            cancel.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dialog.cancel();
                }
            });
        }
    }
}
