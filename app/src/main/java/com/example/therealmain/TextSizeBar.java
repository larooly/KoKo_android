package com.example.therealmain;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class TextSizeBar extends AppCompatActivity {
    TextView Title;
    TextView showsize;
    SeekBar Textsize;
    ImageView img;
    FrameLayout sizeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_text_size_bar);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_text_size_bar);
        //타이틀바 없애기
        Title =(TextView)findViewById(R.id.TTT);
        showsize=(TextView)findViewById(R.id.ShowSize);
        Textsize=(SeekBar)findViewById(R.id.sizeText);
        img=(ImageView)findViewById(R.id.imagesize);
        //sizeLayout = findViewById(R.id.sizeLayout);
        Intent i = getIntent();
        int currentSize= i.getExtras().getInt( "pensize");
        boolean isEraser = i.getExtras().getBoolean("IsEraser");
        if(isEraser){
            Title.setText("지우개 크기");
        }
        Textsize.setProgress(currentSize);
        showsize.setText(Integer.toString(currentSize));
        //img.getLayoutParams().height=currentSize*2;
        img.setScaleX((float) (currentSize/50.0));
        img.setScaleY((float) (currentSize/50.0));

        Textsize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //사용자가 바를 움직일때
                if(progress<1){
                    progress=1;
                }
                showsize.setText(Integer.toString(progress));
                img.setScaleX((float) (progress/50.0));
                img.setScaleY((float) (progress/50.0));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            //사용자가 바를 터치했을때


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            //사용자가 손을땠을때
            }
        });

    }


    public void ClicktoClose(View v){
        Intent sendChan =new Intent();
        if(Textsize.getProgress()<1){
            Textsize.setProgress(1);
        }
        sendChan.putExtra("result",Textsize.getProgress());
        setResult(RESULT_OK,sendChan);
        finish();
    }
}
