package com.example.myapp;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Mengyu on 2015/11/22.
 */

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback{

    public static final int WIDTH=908;
    public static final int HEIGHT =600;
    public static final int MOVESPEED=-5;
    private long smokeStartTimer;
    private long enemyStartTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smoke> smoke;
    private ArrayList<Enemy> enemies;
    private Random rand= new Random();

    public GamePanel(Context context){
        super(context);
    //add callback to surfaceholder to intercept events
        getHolder().addCallback(this);
        thread =new MainThread(getHolder(), this);
        //make gamePanel focusable to handle events
        setFocusable(true);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        bg=new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grass));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.roll), 48, 60, 4);
        smoke = new ArrayList<Smoke>();
        enemies = new ArrayList<Enemy>();
        smokeStartTimer = System.nanoTime();
        enemyStartTime = System.nanoTime();

        //start game loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry=true;
        int counter = 0;
        while (retry && counter<1000){
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry=false;
            }catch (InterruptedException e){e.printStackTrace();}
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (event.getAction()==MotionEvent.ACTION_DOWN){
            if (!player.getPlaying()){
                player.setPlaying(true);
            }
            else{
                player.setUp(true);
            }
            return true;
        }
        if (event.getAction()==MotionEvent.ACTION_UP){
            player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update()
    {
        if (player.getPlaying()) {
            bg.update();
            player.update();
            // enemy update
            long enemyElapsed = (System.nanoTime()-enemyStartTime)/1000000;
            if (enemyElapsed > (2000 - player.getScore() / 4)) {
                //first enemy always goes down the middle
                if (enemies.size()==0){
                    enemies.add(new Enemy(BitmapFactory.decodeResource(getResources(),R.drawable.enemy), WIDTH+10, HEIGHT/2, 87,122,player.getScore(),5));
                }
                else{
                    enemies.add(new Enemy(BitmapFactory.decodeResource(getResources(),R.drawable.enemy),WIDTH+10,(int)(rand.nextDouble()*(HEIGHT)),87,122,player.getScore(),5));
                }
                //reset timer
                enemyStartTime = System.nanoTime();
            }
            //loop through every enemy to check collision and remove
            for (int i=0; i<enemies.size();i++){
                //update enemy
                enemies.get(i).update();
                if (collision(enemies.get(i),player)){
                    //enemies.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //remove enemy if it is way off the screen
                if (enemies.get(i).getX()<-100){
                    enemies.remove(i);
                    break;
                }
            }
            //smoke update
            long elapsed = (System.nanoTime() - smokeStartTimer)/1000000;
            if (elapsed>120){
                smoke.add(new Smoke(player.getX(), player.getY()+30));
                smokeStartTimer = System.nanoTime();
            }
            for(int i = 0; i<smoke.size(); i++){
                smoke.get(i).update();
                if (smoke.get(i).getX()<-10){
                    smoke.remove(i);
                }
            }
        }
    }
    public boolean collision(GameObject a, GameObject b){
        if (Rect.intersects(a.getRectangle(), b.getRectangle())){
            return true;
        }
        return false;
    }
    @Override
    public void draw(Canvas canvas){
        final float scaleFactorX =getWidth()/(WIDTH*1.f);
        final float scaleFactorY =getHeight()/(HEIGHT*1.f);
        if (canvas!=null) {
            final int savedState = canvas.save();
            //scale canvas to the size of screen
            canvas.scale(scaleFactorX, scaleFactorY);
            //draw canvas
            bg.draw(canvas);
            player.draw(canvas);
            //draw smoke
            for (Smoke s:smoke){
                s.draw(canvas);
            }
            //draw enemies
            for (Enemy e:enemies){
                e.draw(canvas);
            }
            //return canvas to unscaled state (so it rescale in each loop)
            canvas.restoreToCount(savedState);
        }
    }
}
