package com.example.write_1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private MyPaintView myView;
    ImageButton btnCal;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("APEC");
        myView = new MyPaintView(this);

        //permission
        verifystoragePermission(this);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        btnCal = findViewById(R.id.btnCal);

        ((LinearLayout) findViewById(R.id.paintLayout)).addView(myView);
        ((RadioGroup) findViewById(R.id.radioGroup)).setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                        switch (checkedId) {
                            case R.id.btnRed:
                                myView.mPaint.setColor(Color.rgb(255, 0, 0));
                                break;
                            case R.id.btnBlack:
                                myView.mPaint.setColor(Color.rgb(0, 0, 0));
                                break;
                            case R.id.btnGreen:
                                myView.mPaint.setColor(Color.rgb(0, 255, 0));
                                break;
                            case R.id.btnBlue:
                                myView.mPaint.setColor(Color.rgb(0, 0, 255));
                                break;
                        }
                    }
                });

        Button btnTh = findViewById(R.id.btnTh);
        btnTh.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count % 3 == 1) {
                    btnTh.setText("30P");
                    myView.mPaint.setStrokeWidth(30);
                    count++;
                } else if (count % 3 == 2) {
                    btnTh.setText("10P");
                    myView.mPaint.setStrokeWidth(10);
                    count++;
                } else {
                    btnTh.setText("20P");
                    myView.mPaint.setStrokeWidth(20);
                    count++;
                }
            }
        }));


        ((Button) findViewById(R.id.btnClear)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myView.mBitmap.eraseColor(Color.TRANSPARENT);
                myView.invalidate();
            }
        });


    }

    public void ScreenshotButton(View view) {
        View rootView = myView;
        File screenshot = ScreenShot(rootView);


        if (screenshot != null) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenshot)));
        }

        btnCal.setVisibility(View.VISIBLE);
        Toast.makeText(getApplicationContext(), "계산 결과를 반환합니다.", Toast.LENGTH_SHORT).show();

    }

    public File ScreenShot(View view) {
        btnCal.setVisibility(View.INVISIBLE);

        view.setDrawingCacheEnabled(true);
        Bitmap screenBitmap = view.getDrawingCache();

        String filename = "screenshot" + System.currentTimeMillis() + ".png";
        File file = new File(Environment.getExternalStorageDirectory()+"/Pictures" , filename);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            screenBitmap.compress(Bitmap.CompressFormat.PNG, 90, os);
            os.close();
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        view.setDrawingCacheEnabled(false);
        return file;

    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSION_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private void verifystoragePermission(MainActivity Activity) {
        int permission = ActivityCompat.checkSelfPermission(Activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Activity,
                    PERMISSION_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }



    private static class MyPaintView extends View {
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mPaint;
        public MyPaintView(Context context) {
            super(context);
            mPath = new Path();
            mPaint = new Paint(Paint.DITHER_FLAG);
            mPaint.setColor(Color.rgb(0,0,0));
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(10);
            mPaint.setStyle(Paint.Style.STROKE);

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(mBitmap, 0, 0, null); //지금까지 그려진 내용
            canvas.drawPath(mPath, mPaint); //현재 그리고 있는 내용
        }



        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int x = (int)event.getX();
            int y = (int)event.getY();
            float press = event.getPressure();
            int press_1 = Math.round(255 * press);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPath.reset();
                    mPaint.setAlpha(press_1);
                    mPath.moveTo(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    mPath.lineTo(x, y);
                    mPaint.setAlpha(press_1);
                    mCanvas.drawPath(mPath, mPaint); //mBitmap 에 기록
                    break;
                case MotionEvent.ACTION_UP:
                    mPath.lineTo(x, y);
                    mPath.reset();
                    break;
            }
            this.invalidate();
            return true;

        }
    }


}



