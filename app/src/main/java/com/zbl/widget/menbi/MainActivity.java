package com.zbl.widget.menbi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final long TIME_PER_FRAME = 2;
    private static final int MAX_COUNT = 200;

    private SurfaceView surfaceview;
    private Bitmap menbiBmp;
    private int bmpW, bmpH;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    protected void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }

    private void init() {
        menbiBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.menbi);
        bmpW = menbiBmp.getWidth();
        bmpH = menbiBmp.getHeight();
        surfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        surfaceview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                isRunning = true;
                new DrawThread(holder).start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                isRunning = false;
            }
        });
    }

    class DrawThread extends Thread {

        private ArrayList<MenbiBean> dataList = new ArrayList<>();
        private LinkedList<MenbiBean> recycleList = new LinkedList<>();
        private Random random = new Random();

        private SurfaceHolder surfaceHolder;
        private float viewWidth, viewHeight;

        private long saveStartTime;

        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        @Override
        public void run() {
            while (isRunning) {
                saveStartTime = System.currentTimeMillis();
                viewWidth = surfaceview.getWidth();
                viewHeight = surfaceview.getHeight();
                //操作对象
                if (dataList.size() < MAX_COUNT) {
                    addMenbiBean();
                    addMenbiBean();
                }
                //绘制
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas == null) {
                    continue;
                }
                canvas.drawColor(0xFFFFFFFF);
                for (int i = dataList.size() - 1; i >= 0; i--) {
                    MenbiBean bean = dataList.get(i);
                    bean.distance += bean.v;
                    bean.v += bean.a;
                    float scale = 0.1f + bean.distance / (viewWidth / 2);
                    bean.x = (float) (viewWidth / 2 + bean.distance * Math.cos(bean.angle) * Math.sin(bean.angle_z));
                    bean.y = (float) (viewHeight / 2 + bean.distance * Math.sin(bean.angle) * Math.sin(bean.angle_z));
                    if (bmpW * scale > viewWidth / 3 || bmpH * scale > viewHeight / 3 || bean.x + bmpW < 0 || bean.x > viewWidth || bean.y + bmpH < 0 || bean.y > viewHeight) {
                        dataList.remove(bean);
                        recycleList.add(bean);
                    } else {
                        if (bean.distance > 100) {
                            bean.matrix.reset();
                            bean.matrix.postScale(scale, scale);
                            bean.matrix.postTranslate(bean.x - bmpW / 2 * scale, bean.y - bmpH / 2 * scale);
                            canvas.drawBitmap(menbiBmp, bean.matrix, null);
                        }
                    }
                }
                surfaceHolder.unlockCanvasAndPost(canvas);
                long delayTime = TIME_PER_FRAME - (System.currentTimeMillis() - saveStartTime);
                if (delayTime > 0) {
                    try {
                        sleep(delayTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void addMenbiBean() {
            MenbiBean bean = recycleList.poll();
            if (bean == null) {
                bean = new MenbiBean();
            }
            bean.reset();
            bean.angle = (float) (random.nextFloat() * Math.PI * 2);
            bean.angle_z = (float) ((random.nextFloat() * 0.8f + 0.2f) * (Math.PI / 2));
            bean.distance = 0;
            bean.startTime = System.currentTimeMillis();
            dataList.add(bean);
        }
    }

    class MenbiBean {
        private float INIT_V = 4f;
        private float INIT_A = 1.2f;

        public Matrix matrix = new Matrix();
        public float x, y, distance, v = INIT_V, a = INIT_A, angle, angle_z;
        public long startTime;

        public void reset() {
            matrix.reset();
            x = 0;
            y = 0;
            distance = 0;
            v = INIT_V;
            a = INIT_A;
            angle = 0;
            startTime = 0;
        }
    }
}
