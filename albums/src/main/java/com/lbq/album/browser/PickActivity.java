package com.lbq.album.browser;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lbq.album.browser.widget.Dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 1174607250 on 2018/7/17 0017.
 */

public abstract class PickActivity extends AppCompatActivity
{
    private String path;
    private int max = 9;
    public final int cameraCode = 717;
    public final int albumsCode = 718;
    private ArrayList<String> sList = new ArrayList<>();
    private Dialog.Pick dialog = new Dialog.Pick(this);
    public abstract void onPickCamera(String path);
    public abstract void onPickAlbums(List<String> paths);
    @Override
    protected void onStart()
    {
        super.onStart();
        if (dialog.dialog == null)
        {
            dialog.create();
            dialog.camera.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    openCamera();
                    dialog.cancel();
                }
            });
            dialog.albums.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    openAlbums();
                    dialog.cancel();
                }
            });
        }
    }
    public void showPick()
    {
        dialog.show();
    }
    public void startCamera(int requestCode)
    {
        File file = new File(getCameraPath());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT < 23)
        {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }
        else
        {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, getPackageName() + ".provider",file));
        }
        startActivityForResult(intent, requestCode);
    }
    public void startAlbums(int requestCode)
    {
        Intent intent = new Intent(this, AlbumBrowser.class);
        intent.putExtra("max", max);
        intent.putExtra("button", true);
        intent.putStringArrayListExtra("list", sList);
        startActivityForResult(intent, requestCode);
    }
    public void openCamera()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            startCamera(cameraCode);
        else
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, cameraCode);

    }
    public void openAlbums()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            startAlbums(albumsCode);
        else
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, albumsCode);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            if (requestCode == cameraCode)
            {
                onPickCamera(path);
            }
            else if (requestCode == albumsCode)
            {
                onPickAlbums(data.getStringArrayListExtra("list"));
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGrantAll = true;
        for (int i : grantResults)
        {
            if (i == PackageManager.PERMISSION_DENIED)
            {
                isGrantAll = false;
                break;
            }
        }
        if (isGrantAll)
        {
            if (requestCode == cameraCode)
            {
                startCamera(requestCode);
            }
            else if (requestCode == albumsCode)
            {
                startAlbums(requestCode);
            }
        }
        else
        {
            if (requestCode == cameraCode)
            {
                final Dialog.Query query = new Dialog.Query(this);
                query.create();
                query.content.setText("您已取消相机拍照的授权\n点击确认立即前往设置打开权限");
                query.confirm.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        query.cancel();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.fromParts("package", getPackageName(),null));
                        startActivity(intent);
                    }
                });
                query.show();
            }
            else if (requestCode == albumsCode)
            {
                final Dialog.Query query = new Dialog.Query(this);
                query.create();
                query.content.setText("您已取消文件读写的授权\n点击确认立即前往设置打开权限");
                query.confirm.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        query.cancel();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.fromParts("package", getPackageName(),null));
                        startActivity(intent);
                    }
                });
                query.show();
            }
        }
    }
    public void setMax(int max)
    {
        this.max = max;
    }
    public void setList(ArrayList<String> list)
    {
        this.sList = list;
    }

    private String getCameraPath()
    {
        return (path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
    }
}
