package com.example.tiltproject;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    //Code from this program has been used from Beginning Android Games
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;
    MediaPlayer background;
    //double accelY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);
        //fixes orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        background = MediaPlayer.create(this,R.raw.perry);
        background.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Restart the MediaPlayer after it finishes playing
                background.seekTo(12000);
                background.start();
            }
        });
        background.seekTo(12000);
        background.start();






//        gameSurface.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // handle the click event here
//                Toast.makeText(MainActivity.this, "GameSurface clicked", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }




    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends SurfaceView implements Runnable,SensorEventListener{


        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap ball;
        Bitmap enemy;
        int ballX=0;
        int enemyMove = 300;
        int x=200;
        String sensorOutput="Score: ";
        String timer = "Time: ";
        Paint paintProperty;
        double accelx;
        float ballPos;
        float ballLeft;
        float ballTop;
        float enemyLeft;
        float enemyTop;
        int score;
        Bitmap currentBit;
        Bitmap hurt;
        int clicked;
        int enemspeed;
        private CountDownTimer gameTimer;
        MediaPlayer collide;
        Bitmap gameOverBitmap; // Add this variable
        Paint textPaint; // Add this variable
        private Bitmap backimage;
        Bitmap extra;
        //boolean dead = false;








        int screenWidth;
        int screenHeight;

        public GameSurface(Context context) {
            super(context);
            accelx = 0;
            holder=getHolder();
            ball= BitmapFactory.decodeResource(getResources(),R.drawable.ball);
            enemy = BitmapFactory.decodeResource(getResources(),R.drawable.doofensh);
            hurt = BitmapFactory.decodeResource(getResources(),R.drawable.hurt);
            extra = BitmapFactory.decodeResource(getResources(),R.drawable.extra);

            collide = MediaPlayer.create(context,R.raw.doofmad);

            enemspeed =4;

            gameOverBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gameover); // Load the bitmap

            backimage = BitmapFactory.decodeResource(getResources(), R.drawable.tree);


            textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(100);

            gameTimer = new CountDownTimer(60000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long secondsRemaining = millisUntilFinished / 1000;
                    // sensorOutput = "Score: " + score + " Time: " + secondsRemaining;
                    timer = "Time: "+secondsRemaining;
                }

                public void onFinish() {
                    running = false;
                    sensorOutput = "Game Over! Final score: " + score;
                    background.stop();
                    collide.stop();
                    // dead = true;
                }
            };




            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;
            enemyLeft = (screenWidth / 2) - enemy.getWidth() / 2;

            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this,accelerometerSensor,sensorManager.SENSOR_DELAY_NORMAL);

            paintProperty= new Paint();
            paintProperty.setTextSize(100);
            currentBit = ball;

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clicked +=1;
                    // handle the click event here
                    //Toast.makeText(getContext(), "GameSurface clicked", Toast.LENGTH_SHORT).show();
                    if(clicked%2 == 0){
                        enemspeed =4;
                    }
                    else {
                        enemspeed = 15;
                    }
                }
            });

        }

        @Override
        public void run() {
            while (running == true){
                if (holder.getSurface().isValid() == false)
                    continue;
                Canvas canvas= holder.lockCanvas();





                ballLeft = (screenWidth/2) - ball.getWidth()/2 +ballX;
                ballTop = (screenHeight/2) - ball.getHeight()+900;

                if (ballLeft < 0){
                    ballLeft = 0;
                }
                if (ballLeft > screenWidth - 450){
                    ballLeft = screenWidth- 450;
                }


                enemyTop = (screenHeight / 2) - enemy.getHeight() - enemyMove;

                canvas.drawRGB(255,0,0);
                canvas.drawBitmap(backimage, 0, 0, null);

                canvas.drawText(sensorOutput,x-200,100,paintProperty);
                canvas.drawText(timer,x-200,200,paintProperty);

                canvas.drawBitmap( currentBit,ballLeft,ballTop,null);



                //if(enemyTop<=ballTop-200) {
                if(isCollisionDetected(ball,(int)ballLeft+200,(int)ballTop+200,enemy,(int)enemyLeft+350,(int)enemyTop+100)){
                    //if(!dead) {
                    collide.start();
                    // }
                    score +=1;


                    currentBit = hurt;
                    enemyLeft = (float) (Math.random()*1201);

                    enemyMove = 300;

                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            currentBit = ball;
                        }
                    },500);
//background music media player
                    // soundpool for short sounds
                }else{
                    canvas.drawBitmap(enemy,enemyLeft ,enemyTop , null);
                }

                if(enemyTop>screenHeight - 100){
                    enemyLeft = (float) (Math.random()*1201);

                    enemyMove = 500;
                    score -=1;
                }

                ballX-=accelx;
                enemyMove -= enemspeed;

//                if(score >= 3){
//                    currentBit = extra;
//
//
//                    postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            currentBit = ball;
//                        }
//                    },1000);
//                }

                sensorOutput = "Score: "+score;





                //RectF hitbox = new RectF(ballX)

                holder.unlockCanvasAndPost(canvas);

                if (!running) {
                    canvas.drawBitmap(gameOverBitmap, screenWidth/2 - gameOverBitmap.getWidth()/2,
                            screenHeight/2 - gameOverBitmap.getHeight()/2, null);

                    String scoreText = "Score: " + score;
                    float textWidth = textPaint.measureText(scoreText);
                    canvas.drawText(scoreText, screenWidth/2 - textWidth/2,
                            screenHeight/2 + gameOverBitmap.getHeight()/2 + 100, textPaint);
                }

            }

        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
            gameTimer.start();

        }

        public void pause() {
            running = false;
            gameTimer.cancel();
            //background.stop();
            while (true) {
                try {
                    gameThread.join();
                    return;
                } catch (InterruptedException e) {
                    // retry
                }
            }
        }


        @Override
        public void onSensorChanged(SensorEvent event) {
            //tilt phone and change position
            accelx = event.values[0];

            if (accelx < -1.0) {
                // tilt left a lot, move 2 pixels
                ballX += 2;
            } else if (accelx > 1.0) {
                // tilt right a lot, move 2 pixels
                ballX -= 2;
            } else if (accelx < -0.5) {
                // tilt left a little, move 1 pixel
                ballX += 1;
            } else if (accelx > 0.5) {
                // tilt right a little, move 1 pixel
                ballX -= 1;
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public boolean isCollisionDetected(Bitmap bitmap1, int x1, int y1,
                                           Bitmap bitmap2, int x2, int y2) {

            Rect bounds1 = new Rect(x1, y1, x1+bitmap1.getWidth(), y1+bitmap1.getHeight());
            Rect bounds2 = new Rect(x2, y2, x2+bitmap2.getWidth(), y2+bitmap2.getHeight());

            if (Rect.intersects(bounds1, bounds2)) {
                return true;
            }
            return false;
        }
    }//GameSurface
}//Activit