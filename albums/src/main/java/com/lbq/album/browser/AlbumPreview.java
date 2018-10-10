package com.lbq.album.browser;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.lbq.album.browser.util.Img;
import com.lbq.album.browser.widget.ViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 1174607250 on 2018/7/12.
 */

public class AlbumPreview extends AppCompatActivity implements View.OnClickListener
{

    private boolean toolbar = true;
    private CheckBox checkBox;
    private ViewPager viewPager;
    private PagerAdapter adapter;
    private RelativeLayout toolbar_bottom;
    private LinearLayout button,toolbar_top;
    private TextView tv_finish, tv_index, tv_total;

    private List<String> sList = new ArrayList<>();
    private List<Boolean> xList = new ArrayList<>();

    private Handler handler = new Handler();
    private ExecutorService service = Executors.newCachedThreadPool();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_20180712_preview);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            findViewById(R.id.status_bar).setVisibility(View.GONE);
        else
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.tv_finish).setOnClickListener(this);
        button = findViewById(R.id.button);
        tv_index = findViewById(R.id.tv_index);
        tv_total = findViewById(R.id.tv_total);
        tv_finish = findViewById(R.id.tv_finish);
        checkBox = findViewById(R.id.checkbox);
        viewPager = findViewById(R.id.viewPager);
        toolbar_top = findViewById(R.id.toolbar_top);
        toolbar_bottom = findViewById(R.id.toolbar_bottom);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                xList.set(viewPager.getCurrentItem(), isChecked);

                int total = 0;
                for (int i = 0 ; i < xList.size() ; i ++ )
                {
                    if (xList.get(i))
                        total ++;
                }
                if (total == 0)
                {
                    tv_finish.setSelected(false);
                    tv_finish.setText("完成");
                }
                else
                {
                    tv_finish.setSelected(true);
                    tv_finish.setText(String.format("完成(%s)", total));
                }
            }
        });
        viewPager.addOnPageChangeListener(new android.support.v4.view.ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }
            @Override
            public void onPageSelected(int position)
            {
                checkBox.setChecked(xList.get(position));
                tv_index.setText(String.valueOf(position + 1));
            }
            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });
        viewPager.setOnClickListener(new ViewPager.OnClickListener()
        {
            @Override
            public void onClick(int type)
            {
                switch (type)
                {
                    case 1:
                        if (toolbar)
                            hideToolbar();
                        else
                            showToolbar();
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }
        });
        initData();
    }
    private Animation getAnimation(int id)
    {
        return AnimationUtils.loadAnimation(this, id);
    }
    private void showToolbar()
    {
        toolbar = true;
        toolbar_top.setVisibility(View.VISIBLE);
        toolbar_bottom.setVisibility(View.VISIBLE);
        toolbar_top.startAnimation(getAnimation(R.anim.anim_20180712_show_from_top_to_bottom));
        toolbar_bottom.startAnimation(getAnimation(R.anim.anim_20180712_show_from_bottom_to_top));
    }
    private void hideToolbar()
    {
        toolbar = false;
        toolbar_top.setVisibility(View.GONE);
        toolbar_bottom.setVisibility(View.GONE);
        toolbar_top.startAnimation(getAnimation(R.anim.anim_20180712_hide_from_bottom_to_top));
        toolbar_bottom.startAnimation(getAnimation(R.anim.anim_20180712_hide_from_top_to_bottom));
    }
    private void initData()
    {
        if (!getIntent().hasExtra("list"))
            finish();
        else
        {
            sList = getIntent().getStringArrayListExtra("list");
            if (sList.size() == 0)
                finish();
            else
            {
                if (!getIntent().getBooleanExtra("button", false))
                {
                    button.setVisibility(View.GONE);
                    tv_finish.setVisibility(View.GONE);
                }
                else
                {
                    tv_finish.setSelected(true);
                    tv_finish.setText(String.format("完成(%s)", sList.size()));
                }
                tv_total.setText(String.valueOf(sList.size()));
                viewPager.setAdapter((adapter = new PagerAdapter()));
                viewPager.setCurrentItem(getIntent().getIntExtra("position", 0));
            }
        }
    }
    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        if (id == R.id.btn_back)
            this.onBackPressed();
        else if (id == R.id.tv_finish && tv_finish.isSelected())
        {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0 ; i < xList.size() ; i ++ )
            {
                if (xList.get(i))
                    list.add(sList.get(i));
            }
            Intent intent = new Intent();
            intent.putStringArrayListExtra("list", list);
            setResult(1, intent);
            finish();
        }
    }
    @Override
    public void onBackPressed()
    {
        if (getIntent().getBooleanExtra("button",false))
        {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0 ; i < xList.size() ; i ++ )
            {
                if (xList.get(i))
                    list.add(sList.get(i));
            }
            Intent intent = new Intent();
            intent.putStringArrayListExtra("list", list);
            setResult(2, intent);
            finish();
        }
        else
            finish();
    }
    private class PagerAdapter extends android.support.v4.view.PagerAdapter
    {
        private List<View> views = new ArrayList<>();
        public PagerAdapter()
        {
            for (int i = 0 ; i < sList.size() ; i ++ )
            {
                xList.add(true);
                if (isGif(i))
                    loadingGif(i);
                else
                    loadingImg(i);
            }
        }
        private boolean isGif(int position)
        {
            return sList.get(position).toLowerCase().endsWith(".gif");
        }
        private void loadingImg(int position)
        {
            final String path = sList.get(position);
            final PhotoView photoView = (PhotoView) LayoutInflater.from(AlbumPreview.this).inflate(R.layout.layout_20180712_image, null);
            if (new File(path).length() /1024 > 2048)
            {
                service.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final Bitmap bitmap = Img.scaling(path, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                photoView.setImageBitmap(bitmap);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
            else
            {
                Glide.with(photoView).load(path).into(photoView);
            }
            views.add(photoView);
        }
        private void loadingGif(int position)
        {
            PhotoView photoView = (PhotoView) LayoutInflater.from(AlbumPreview.this).inflate(R.layout.layout_20180712_image, null);
            Glide.with(photoView).asGif().load(sList.get(position)).into(photoView);
            views.add(photoView);
        }
        @Override
        public int getCount()
        {
            return sList.size();
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView((View) object);
        }
        @Override
        public boolean isViewFromObject(View view, Object object)
        {
            return view == object;
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            container.addView(views.get(position));
            return views.get(position);
        }
    }

}
