package com.lbq.album.browser;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.lbq.album.browser.util.Img;
import com.lbq.album.browser.widget.Dialog;
import com.lbq.album.browser.widget.PopupWindow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 1174607250 on 2018/7/12 0012.
 */

public class AlbumBrowser extends AppCompatActivity implements View.OnClickListener
{
    private int max = 1;
    private Toast toast;
    private PopupWindow window;
    private GridView gridView;
    private GridAdapter adapter;
    private ProgressBar progressBar;
    private TextView tv_finish, tv_album, tv_preview;

    private final String cachePath = "/Albums/";
    private final String diskPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private final String rootKey = "全部图片";

    private String defaultKey = rootKey;

    private ArrayList<String> sList = new ArrayList<>();
    private LinkedHashMap<String, String> imgMap = new LinkedHashMap<>();
    private LinkedHashMap<String, ArrayList<File>> fileMap = new LinkedHashMap<>();

    private ExecutorService service = Executors.newCachedThreadPool();

    private void showToast(String str)
    {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        toast.show();
    }
    private Handler handler = new Handler(new Handler.Callback()
    {
        private int progress;
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    progress = 0;
                    break;
                case 1:
                    progress ++;
                    adapter.notifyDataSetChanged();
                    break;
                case 2:
                    progress = 0;
                    break;
            }
            if (fileMap.containsKey(rootKey))
                progressBar.setMax(fileMap.get(rootKey).size());
            progressBar.setProgress(progress);
            return false;
        }
    });
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_20180712_browser);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            findViewById(R.id.status_bar).setVisibility(View.GONE);
        else
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.tv_finish).setOnClickListener(this);
        findViewById(R.id.btn_album).setOnClickListener(this);
        findViewById(R.id.tv_preview).setOnClickListener(this);
        tv_album = findViewById(R.id.tv_album);
        tv_finish = findViewById(R.id.tv_finish);
        tv_preview = findViewById(R.id.tv_preview);
        progressBar = findViewById(R.id.progressBar);
        gridView = findViewById(R.id.gridView);
        gridView.setAdapter((adapter = new GridAdapter(this)));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            new ScanFile().start();
        else
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 117460);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (sList.contains(adapter.getItem(position).toString()))
                    sList.remove(adapter.getItem(position).toString());
                else if (max == 1)
                {
                    sList.clear();
                    sList.add(adapter.getItem(position).toString());
                }
                else if (sList.size() >= max)
                    showToast(String.format("最多只能选择%s个文件", max));
                else
                    sList.add(adapter.getItem(position).toString());
                adapter.notifyDataSetChanged();
                refresh();
            }
        });
        max = getIntent().getIntExtra("max", 1);
        if (getIntent().hasExtra("list"))
            sList = getIntent().getStringArrayListExtra("list");
        refresh();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 7250)
        {
            if (resultCode == 1)
            {
                setResult(RESULT_OK, data);
                finish();
            }
            else if (resultCode == 2)
            {
                sList = data.getStringArrayListExtra("list");
                adapter.notifyDataSetChanged();
                refresh();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 117460)
        {
            boolean isGrantAll = true;
            for (int i : grantResults)
            {
                if (i == PackageManager.PERMISSION_DENIED)
                    isGrantAll = false;
            }
            if (isGrantAll)
                new ScanFile().start();
            else
            {
                final Dialog.Query query = new Dialog.Query(this);
                query.create();
                query.show();
                query.setCancelable(true);
                query.cancel.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        query.cancel();
                        finish();
                    }
                });
                query.confirm.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        query.cancel();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }
    }
    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        if (id == R.id.btn_back)
            super.onBackPressed();
        else if (id == R.id.btn_album)
            popupWindow();
        else if (id == R.id.tv_finish && tv_finish.isSelected())
        {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("list", sList);
            setResult(RESULT_OK, intent);
            finish();
        }
        else if (id == R.id.tv_preview && tv_preview.isSelected())
        {
            Intent intent = getIntent();
            intent.setClass(this, AlbumPreview.class);
            intent.putStringArrayListExtra("list", sList);
            startActivityForResult(intent, 7250);
        }
    }
    private void popupWindow()
    {
        if (window == null)
        {
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = gridView.getHeight() - (int)((50 * getResources().getDisplayMetrics().density) + 0.5f);
            View view = LayoutInflater.from(this).inflate(R.layout.popup_20180712_window, null);
            ListView listView = view.findViewById(R.id.list_item);
            listView.setAdapter(new ListAdapter(this));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    defaultKey = fileMap.keySet().toArray()[position].toString();
                    adapter.notifyDataSetChanged();
                    window.dismiss();
                    refresh();
                }
            });
            window = new PopupWindow(width, height);
            window.setContentView(view);
            window.setFocusable(true);
            window.setOutsideTouchable(true);
            window.setBackgroundDrawable(new ColorDrawable(0x00000000));
            window.update();
        }
        if (window.isShowing())
            window.dismiss();
        else
            window.showAsDropDown(findViewById(R.id.popupWindow));
    }
    public int getWidth()
    {
        int width = getResources().getDisplayMetrics().widthPixels;
        float density = getResources().getDisplayMetrics().density;
        return (width - (int) ((9 * density) + 0.5f)) / 4;
    }
    private File getCachePath(File file)
    {
        File newFile = new File(diskPath + cachePath + file.getParent().replace(diskPath,""));

        if (!newFile.exists())
            newFile.mkdirs();

        return newFile;
    }
    private void readBitmap(File file)
    {
        final File newFile = new File(getCachePath(file).getAbsolutePath() + "/." + file.getName());

        if (newFile.exists())
            imgMap.put(file.getAbsolutePath(), newFile.getAbsolutePath());
        else
        {
            int width = getWidth();
            int height = getWidth();
            Bitmap bitmap1 = Img.scaling(file.getAbsolutePath(), width, height);
            Bitmap bitmap2 = Img.thumbnail(bitmap1, width, height);

            imgMap.put(file.getAbsolutePath(), newFile.getAbsolutePath());
            try
            {
                Img.save(newFile.getPath(), bitmap2, true);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            if (!bitmap1.isRecycled())
                bitmap1.recycle();
            if (!bitmap2.isRecycled())
                bitmap2.recycle();
        }
    }
    private class ScanFile extends Thread
    {
        @Override
        public void run()
        {
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null, null);
            if (cursor != null)
            {
                if (!fileMap.containsKey(rootKey))
                    fileMap.put(rootKey, new ArrayList<File>());
                while (cursor.moveToNext())
                {
                    File file = new File(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));

                    if (file.getParent().startsWith(diskPath + cachePath.substring(0,cachePath.length()-1)) || !file.exists())
                        continue;

                    if (!fileMap.get(rootKey).contains(file))
                        fileMap.get(rootKey).add(file);

                    if (!fileMap.containsKey(file.getParent()))
                        fileMap.put(file.getParent(), new ArrayList<File>());

                    if (!fileMap.get(file.getParent()).contains(file))
                        fileMap.get(file.getParent()).add(file);

                }
                cursor.close();
                Collections.sort(fileMap.get(rootKey), new Comparator<File>()
                {
                    @Override
                    public int compare(File o1,File o2)
                    {
                        if (o1.lastModified() > o2.lastModified())
                            return -1;
                        else if (o1.lastModified() < o2.lastModified())
                            return 1;
                        else
                            return 0;
                    }
                });
                for (String key : fileMap.keySet())
                {
                    Collections.sort(fileMap.get(key), new Comparator<File>()
                    {
                        @Override
                        public int compare(File o1, File o2)
                        {
                            if (o1.lastModified() > o2.lastModified())
                                return -1;
                            else if (o1.lastModified() < o2.lastModified())
                                return 1;
                            else
                                return 0;
                        }
                    });
                }
                handler.obtainMessage(0).sendToTarget();
                for (File file : fileMap.get(rootKey))
                {
                    try
                    {
                        readBitmap(file);
                        handler.obtainMessage(1).sendToTarget();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                handler.obtainMessage(2).sendToTarget();
            }
        }
    }
    private void refresh()
    {
        if (sList.size() == 0)
        {
            tv_finish.setText("完成");
            tv_finish.setSelected(false);

            tv_preview.setText("预览");
            tv_preview.setSelected(false);
        }
        else
        {
            tv_finish.setText(String.format("完成(%s/%s)", sList.size(), max));
            tv_finish.setSelected(true);

            tv_preview.setText(String.format("预览(%s)", sList.size()));
            tv_preview.setSelected(true);
        }
        tv_album.setText(defaultKey.split("/")[defaultKey.split("/").length - 1]);
    }
    private class GridAdapter extends BaseAdapter
    {
        private LayoutInflater inflater;
        public GridAdapter(Context context)
        {
            this.inflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount()
        {
            return fileMap.containsKey(defaultKey) ? fileMap.get(defaultKey).size() : 0;
        }
        @Override
        public Object getItem(int position)
        {
            return fileMap.get(defaultKey).get(position).getAbsolutePath();
        }
        @Override
        public long getItemId(int position)
        {
            return position;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            final ViewHolder holder;
            if (convertView == null)
            {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_20180712_browser, null);
                holder.view = convertView.findViewById(R.id.view);
                holder.checkBox = convertView.findViewById(R.id.checkbox);
                holder.imageView = convertView.findViewById(R.id.iv_image);
                holder.imageViewGif = convertView.findViewById(R.id.iv_image_gif);
                holder.imageView.setMinimumHeight(getWidth());
                holder.imageView.setMinimumWidth(getWidth());
                convertView.setTag(holder);
            }
            else holder = (ViewHolder) convertView.getTag();
            holder.checkBox.setChecked(sList.contains(getItem(position).toString()));
            holder.imageViewGif.setVisibility(isGif(position) ? View.VISIBLE : View.GONE);
            holder.view.setVisibility(sList.contains(getItem(position).toString()) ? View.VISIBLE : View.GONE);
            holder.checkBox.setVisibility(max == 1 ? sList.contains(getItem(position).toString()) ? View.VISIBLE : View.GONE : View.VISIBLE);
            service.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    final Bitmap bitmap = BitmapFactory.decodeFile(imgMap.get(getItem(position).toString()));
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {

                            holder.imageView.setImageBitmap(bitmap);
                        }
                    });
                }
            });
            return convertView;
        }
        private class ViewHolder
        {
            View view;
            ImageView imageView;
            ImageView imageViewGif;
            CheckBox checkBox;
        }
        private boolean isGif(int position)
        {
            return fileMap.get(defaultKey).get(position).getName().toLowerCase().endsWith(".gif");
        }
    }
    private class ListAdapter extends BaseAdapter
    {
        private LayoutInflater inflater;
        public ListAdapter(Context context)
        {
            this.inflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount()
        {
            return fileMap.size();
        }
        @Override
        public Object getItem(int position)
        {
            return fileMap.keySet().toArray()[position];
        }
        @Override
        public long getItemId(int position)
        {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            final ViewHolder holder;
            final String key = getItem(position).toString();
            if (convertView == null)
            {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_20180712_popup, null);
                holder.tv_name = convertView.findViewById(R.id.tv_name);
                holder.tv_total = convertView.findViewById(R.id.tv_total);
                holder.iv_image = convertView.findViewById(R.id.iv_image);
                holder.iv_checked = convertView.findViewById(R.id.iv_checked);
                convertView.setTag(holder);
            } else holder = (ViewHolder) convertView.getTag();

            holder.tv_total.setText((fileMap.get(key).size() + "张"));

            holder.tv_name.setText(key.split("/")[key.split("/").length -1]);
            holder.iv_checked.setVisibility(defaultKey.equals(key) ? View.VISIBLE : View.GONE);
            if (fileMap.get(key).size() > 0)
            {
                service.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final Bitmap bitmap = BitmapFactory.decodeFile(imgMap.get(fileMap.get(key).get(0).getAbsolutePath()));
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                holder.iv_image.setImageBitmap(bitmap);
                            }
                        });
                    }
                });
            }
            return convertView;
        }
        private class ViewHolder
        {
            TextView tv_name, tv_total;
            ImageView iv_image, iv_checked;
        }
    }
}
