package com.example.therealmain;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class LoadingGif extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_gif);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //안드로이드 gif 재생하는 방법->애니메이션 안해도 됨
        ImageView cat =(ImageView)findViewById(R.id.imageView);

        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(cat);
        Glide.with(this).load(R.drawable.cat).into(gifImage);
       // cat.setImageDrawable(R.drawable.lin);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },3000);//이방식이 더빠르다

    }

//    private void startLoading(){
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                finish();
//            }
//        },1000);
//    }
}
