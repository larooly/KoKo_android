package com.example.therealmain;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.NinePatch;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import yuku.ambilwarna.AmbilWarnaDialog;

import static android.os.Environment.getExternalStoragePublicDirectory;

import static java.security.AccessController.getContext;
import static java.util.Collections.rotate;

public class MainActivity extends AppCompatActivity {
    private PaintView paintView;
    ConstraintLayout total;


    private Menu mainmenu;
    boolean PenOrEraser=false;
    int PickDefaultColor;

    FileOutputStream outputStream;

    public File image;//삭제를위해추

    Bitmap showPenColor=Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);//ㅇㅏ이콘용
    Canvas iconPen = new Canvas(showPenColor);
    int PenSize;
    int EraserSize=10;



    String pathtoFile;

    private NotificationManagerCompat notificationManager;

    ImageView rBack;
    Bitmap Nimg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent loading = new Intent(this,LoadingGif.class);
        startActivity(loading);
        //ㅇㅕ기까지가 로딩관련

        paintView =(PaintView)findViewById(R.id.paintView);
        total = findViewById(R.id.Totaldraw);
        DisplayMetrics metrics= new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);

        PickDefaultColor = Color.BLACK;

        notificationManager = NotificationManagerCompat.from(this);

        rBack=(ImageView)findViewById(R.id.RealBack);
        rBack.setBackgroundResource(R.drawable.resetbackground);
        Nimg=null;

        PenSize= paintView.strokeWidth;




//        mainmenu.getItem(0).setIcon(R.drawable.ic_mode_edit_black_24dp);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main,menu);

        this.mainmenu =menu;

        iconPen.drawColor(paintView.currentColor);
        mainmenu.getItem(0).setIcon(new BitmapDrawable(getResources(),showPenColor));
        mainmenu.getItem(1).setTitle(String.valueOf( paintView.strokeWidth));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.normal:
                paintView.normal();
                item.setChecked(true);
                return true;
            case R.id.emboss:
                item.setChecked(true);
                paintView.emboss();
                return true;
            case R.id.blur:
              //  mainmenu.getItem(0).setIcon(R.drawable.ic_mode_edit_black_24dp);
                item.setChecked(true);
                paintView.blur();
                return true;
            case R.id.color:
                openColorPicker();

                return true;
            case R.id.pensize:
               // paintView.strokeWidth = 400;
                Intent i = new Intent(this, TextSizeBar.class);
                if(PenOrEraser){
                    i.putExtra("IsEraser",true);
                }
                else {
                    i.putExtra("IsEraser",false);


                }
                i.putExtra("pensize",paintView.strokeWidth);
                startActivityForResult(i,1);
                return true;
            case R.id.toGallery://바탕 갤러리 사진 가져오


                Intent bring = new Intent();
                bring.setType("image/*");
                bring.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(bring,2);
                return true;
            case R.id.toCamera:
                if(Build.VERSION.SDK_INT>=23){
                    requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},4);
                }
                dispatchPictureTakeAction();

            case R.id.resetBack://바탕 원상복귀
                //기능 없음
                rBack.setBackgroundResource(R.drawable.resetbackground);
                paintView.invalidate();
                return true;
            case R.id.AllClear://전체 날리기

                Nimg = null;
                rBack.setBackgroundResource(R.drawable.resetbackground);

                rBack.setImageResource(R.drawable.resetbackground);
                paintView.clear();
               // paintView.setBackgroundResource(R.drawable.tesingcolorfulll);
                return true;
            case R.id.onlyPath://그린것만 날리기
                paintView.onlyPathclear();
                // paintView.setBackgroundResource(R.drawable.tesingcolorfulll);
                return true;
            case R.id.saveFile:
                if(Build.VERSION.SDK_INT>=23){
                    requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},4);
                }
                SaveMediaStore(getBitmapFromView(total));

                return true;
            case R.id.share:
                if(Build.VERSION.SDK_INT>=23){
                    requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},4);
                }

                shareImage(getBitmapFromView(total));

                //paintView.clear();
                return true;

            case R.id.warning:

                sendNotification();

                //paintView.clear();
                return true;
            case R.id.left:
                if(Nimg!=null) {
                    Nimg=rotateImage(Nimg, -90);
                    rBack.setImageBitmap(Nimg);
                }
                //paintView.clear();
                return true;
            case R.id.right:
                if(Nimg!=null) {
                    Nimg=rotateImage(Nimg, 90);
                    rBack.setImageBitmap(Nimg);

                }
                //paintView.clear();
                return true;
            case R.id.SetEraserPencil:
                if(PenOrEraser){
               // mainmenu.getItem(0).setIcon(R.drawable.ic_mode_edit_black_24dp);
                    iconPen.drawColor(paintView.currentColor);
                    mainmenu.getItem(0).setIcon(new BitmapDrawable(getResources(),showPenColor));
                    paintView.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    paintView.strokeWidth = PenSize;
                    mainmenu.getItem(1).setTitle(String.valueOf( paintView.strokeWidth));
                //토스트메세지
                PenOrEraser=false;
                }
                else {
                    item.setIcon(R.drawable.erasericon);
                    paintView.Eraser();
                    PenOrEraser=true;
                    paintView.strokeWidth = EraserSize;
                    mainmenu.getItem(1).setTitle(String.valueOf( paintView.strokeWidth));

                }
                return true;



        }
        return super.onOptionsItemSelected(item);
    }

    public void openColorPicker(){

        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, PickDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                paintView.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));//안하면 지우개 고정됨
                PickDefaultColor = color;

                paintView.currentColor=(PickDefaultColor);
                iconPen.drawColor(paintView.currentColor);
                mainmenu.getItem(0).setIcon(new BitmapDrawable(getResources(),showPenColor));

            }
        });
        colorPicker.show();
    }
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;
    @Override
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();
        }
    }

////////////add Camera///////////////////
    private  void dispatchPictureTakeAction(){
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getPackageManager())!= null) {
            File photoFile = null;
            photoFile = createPhotofile();
            if (photoFile != null) {
                pathtoFile = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,"com.thecodecity.cameraandroid.fileprovider",photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(takePic,3);
            }
        }
    }

    private File createPhotofile() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);// getExternalStoragePublicDirectory
         image=null;//원래이앞에 File있었음
        try {
            image = File.createTempFile(name,".jpg",storageDir);
        }catch (IOException e) {
            Log.d("mylog","Excep : "+ e.toString());
        }
        return image;
    }

    /////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                int chansize = data.getExtras().getInt("result");
                Toast.makeText(MainActivity.this, Integer.toString( chansize), Toast.LENGTH_LONG).show();
//                paintView.strokeWidth = chansize;
//                mainmenu.getItem(1).setTitle(String.valueOf( paintView.strokeWidth));
                if(PenOrEraser){
                    //지우개사이즈
                    EraserSize=chansize;
                    paintView.strokeWidth = EraserSize;
                    mainmenu.getItem(1).setTitle(String.valueOf( paintView.strokeWidth));

                }
                else {
                    PenSize=chansize;
                    paintView.strokeWidth = PenSize;
                    mainmenu.getItem(1).setTitle(String.valueOf( paintView.strokeWidth));
                }//펜사이즈




            }
        }
        else if (requestCode==2){
            if(resultCode == RESULT_OK){
                try{
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    Bitmap img = BitmapFactory.decodeStream(in);
                    in.close();
                   // BitmapDrawable ob= new BitmapDrawable(getResources(),img);
                    //paintView.newBack=img;
                    //rBack.setImageBitmap(rotateImage(img,90));
                    rBack.setImageBitmap(img);
                    Nimg=img;
                    paintView.invalidate();

                }catch(Exception e)
                {

                }
            }
        }
        else if(requestCode==3){
            if(resultCode == RESULT_OK){
                Bitmap bitmap = BitmapFactory.decodeFile(pathtoFile);
                paintView.newBack=  bitmap;

                rBack.setImageBitmap(bitmap);
                Nimg=bitmap;
                    paintView.invalidate();
                    image.delete();//카메라 내부저장소 삭제


            }
        }
    }



    ////////////////ddddd


    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    ////////////////ddddd
    public void Savefile(Bitmap bitmap ){

        try {
            File filepath = Environment.getExternalStorageDirectory();
            File dir = new File(filepath.getAbsolutePath()+"/Download/");


            dir.mkdir();

            File file = new File(dir, "catPaint"+System.currentTimeMillis()+".jpg");
            file.createNewFile();
            try{
                outputStream = new FileOutputStream(file);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "망함", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "망함", Toast.LENGTH_LONG).show();
        }

    }

    public static Bitmap getBitmapFromView(View view) {
//        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);
        return bitmap;
    }

    public void SaveMediaStore(Bitmap bitmap)  {//그대신 좀 느리다//picture 안에 저장 됨
        ContentValues values = new ContentValues();
        //values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_1024.JPG");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        ContentResolver contentResolver = getContentResolver();
        Uri item = getImageUri(this,bitmap);//contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);//MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values

        try {
            ParcelFileDescriptor pdf = contentResolver.openFileDescriptor(item, "w", null);

            if (pdf == null) {
               // Log.d("asdf", "null");
            } else {
                //String str = "a";
                //byte[] strToByte = str.getBytes();
                FileOutputStream fos = new FileOutputStream(pdf.getFileDescriptor());
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                Toast.makeText(MainActivity.this, "저장중.....", Toast.LENGTH_LONG).show();
                //fos.write(strToByte);
                fos.flush();
                fos.close();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    contentResolver.update(item, values, null, null);
                }
                Toast.makeText(MainActivity.this, "저장 완료!", Toast.LENGTH_LONG).show();

            }
        } catch (FileNotFoundException e) {
            Toast.makeText(MainActivity.this, "망함1", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "망함2", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        //ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        //inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);

        //case2


        



    }

    private void shareImage(Bitmap bitmap){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        Uri screenshotUri =  getImageUri(this,bitmap);	// android image path
        sharingIntent.setType("image/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        startActivity(Intent.createChooser(sharingIntent, "Share image using"));
    }




    private void sendNotification(){
        //알람을 클릭할 경우의 intent
        Intent activityIntent = new Intent(this,MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,activityIntent,0);
        //창모양이나 내용같은거 물론 인텐트 연결도 해야함
        Notification notification = new NotificationCompat.Builder(this,App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_pets_24dp)
                .setContentTitle("까꿍!")
                .setContentText("근데 클릭하지 말라고 했을텐뎅~~~~~~")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)

                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)

                .build();
        notificationManager.notify(1,notification);



    }



}






