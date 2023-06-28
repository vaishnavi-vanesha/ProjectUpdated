package com.example.myapplication;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity implements VideoRVAdapter.VideoClickInterface{
    private RecyclerView videoRV;
    private ArrayList<VideoRVModal> videoRVModalArrayList;
    private VideoRVAdapter videoRVAdapter;
    private static final int STORAGE_PERMISSION=101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoRV=findViewById(R.id.idRVVideos);
        videoRVModalArrayList=new ArrayList<>();
        videoRVAdapter=new VideoRVAdapter(videoRVModalArrayList,this,this::onVideoClick);
        videoRV.setLayoutManager(new GridLayoutManager(this,2));
        videoRV.setAdapter(videoRVAdapter);
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION);
        }
        else
        {
            getVideos();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==STORAGE_PERMISSION)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show();
                getVideos();
            }
            else
            {
                Toast.makeText(this,"The App will not work without permission...", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void getVideos()
    {
        ContentResolver contentResolver=getContentResolver();
        Uri uri= MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor= contentResolver.query(uri,null,null,null,null);
        if(cursor!=null && cursor.moveToFirst())
        {
            int titleIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
            int pathIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
            if(titleIndex!=-1 && pathIndex!=-1) {
                do {
                    String videoTitle = cursor.getString(titleIndex);
                    String videoPath = cursor.getString(titleIndex);
                    Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);

                    videoRVModalArrayList.add(new VideoRVModal(videoTitle, videoPath, videoThumbnail));
                } while (cursor.moveToNext());

                videoRVAdapter.notifyDataSetChanged();
            }
            else
            {
                Log.e("CursorColumned","Column indices are not found for TITLE and DATA");
            }
            cursor.close();
        }
        else
        {
            Log.e("Cursor","Cursor is null or empty");
        }

    }

    @Override
    public void onVideoClick(int position)
    {
        Intent i=new Intent(MainActivity.this,VideoPlayerActivity.class);
        i.putExtra("videoName",videoRVModalArrayList.get(position).getVideoName());
        i.putExtra("videoPath",videoRVModalArrayList.get(position).getVideoPath());
        startActivity(i);
    }

}