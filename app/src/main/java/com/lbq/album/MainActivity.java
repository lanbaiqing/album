package com.lbq.album;

import android.os.Bundle;
import android.view.View;

import com.lbq.album.browser.PickActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends PickActivity
{
    private ArrayList<String> sList = new ArrayList<>();
    @Override
    public void onPickCamera(String path)
    {
        this.sList.clear();
        this.sList.add(path);
        this.setList(sList);
    }
    @Override
    public void onPickAlbums(List<String> paths)
    {
        this.sList.clear();
        this.sList.addAll(paths);
        this.setList(sList);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onClick(View view)
    {
        if (view.getId() == R.id.open)
        {
            showPick();
        }
    }
}
