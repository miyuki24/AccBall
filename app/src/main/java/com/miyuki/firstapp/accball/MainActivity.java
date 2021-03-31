package com.miyuki.firstapp.accball;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener, SurfaceHolder.Callback {

    SensorManager mSensorManager;
    Sensor mAccSensor;
    SurfaceHolder mHolder;
    int mSurfaceWidth;
    int mSurfaceHeight;

    static final float RADIUS = 150.0f;  //ボールの半径
    static final int DIA = (int)RADIUS * 2;
    static final float COEF = 1000.0f;  //ボールに移動を調節する

    float mBallX; //ボールの現在のx標
    float mBally; //ボールの現在のy標
    float mVX; //ボールのx軸方向への加速度
    float mVY; //ボールのy軸方向への加速度

    long mT0; //前回センサーから加速度を取得した時間

    Bitmap mBallBitmap; //ボールの画像

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = surfaceView.getHolder();

        mHolder.addCallback(this);

        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceView.setZOrderMediaOverlay(true);

        Bitmap ball = BitmapFactory.decodeResource(getResources(),R.drawable.ball);
        mBallBitmap = Bitmap.createScaledBitmap(ball,DIA,DIA,false);
    }

    //加速度センサーに変化があった時に呼ばれる
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //画像の描画方向に合わせるため反転
        float x = -sensorEvent.values[0];
        float y = sensorEvent.values[1];

        //時間を求める処理
        if (mT0 == 0) {
            mT0 = sensorEvent.timestamp;
            return;
        }
        float t = sensorEvent.timestamp - mT0;
        mT0 = sensorEvent.timestamp;
        t = t/1000000000.0f;  //ナノ秒を秒に単位変換

        //移動距離を求める
        float dx = (mVX * t) + ( x * t * t / 2.0f);
        float dy = (mVY * t) + ( y * t * t / 2.0f);

        //移動距離からボールの今の位置を更新
        mBallX = mBally + dx * COEF;
        mBally = mBally + dy * COEF;

        //現在のボールの移動速度を更新
        mVX = mVX + (x * t);
        mVY = mVY + (y * t);

        //ボールが画面内の外に出ないようにする処理
        if (mBallX - RADIUS < 0 && mVX < 0) {
            mVX = -mVX / 1.5f;
            mBallX = RADIUS;
        } else if (mBallX + RADIUS > mSurfaceWidth && mVX > 0) {
            mVX = -mVX / 1.5f;
            mBallX = mSurfaceWidth - RADIUS;
        }
        if (mBally - RADIUS < 0 && mVY < 0) {
            mVY = -mVY / 1.5f;
            mBally = RADIUS;
        } else if (mBally + RADIUS > mSurfaceHeight && mVY > 0) {
            mVY = -mVY / 1.5f;
            mBally = mSurfaceHeight - RADIUS;
        }

        //加速度から算出したボールの現在位置で、ボールをキャンバスに描画し直す
        drawCanvas();
    }

    private void drawCanvas() {
        //画面にボールを表示する処理
        Canvas c = mHolder.lockCanvas();
        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Paint paint = new Paint();
        c.drawBitmap(mBallBitmap,mBallX - RADIUS, mBally - RADIUS, paint);
        mHolder.unlockCanvasAndPost(c);
    }

    //加速度センサーの精度が変化した時に呼ばれる
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //画面が表示された時に呼ばれる
    @Override
    protected void onResume() {
        super.onResume();

    }

    //画面が閉じられた時に呼ばれる
    @Override
    protected void onPause() {
        super.onPause();

    }

    //サーフェスが作成された時に呼ばれる
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mSensorManager.registerListener(this,mAccSensor, SensorManager.SENSOR_DELAY_GAME);

    }

    //サーフェスが変更された時に呼ばれる
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHeight = height;
        mSurfaceWidth = width;
        //ボールの初期位置を指定する
        mBallX = mSurfaceWidth / 2;
        mBally = mSurfaceHeight / 2;

        //ボールの初期速度・初期時間
        mVY = 0;
        mVY = 0;
        mT0 = 0;
    }

    //サーフェスが破棄された時に呼ばれる
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mSensorManager.unregisterListener(this);
    }
}