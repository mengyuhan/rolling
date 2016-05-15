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
    private ArrayList<Upboundary> upboundaries;
    private ArrayList<Lowboundary> lowboundaries;
    private boolean newGameCreated;
    private int maxboundary;
    private int minboundary;
    private boolean topDown;
    private boolean botDown;
    private boolean isNewGameCreated;
    private int difficulty = 20;

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
        upboundaries = new ArrayList<>();
        lowboundaries = new ArrayList<>();
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

            //create boundaries
            maxboundary = 30 + player.getScore()/difficulty;
            //cap max boundaries height
            if(maxboundary > HEIGHT/4) maxboundary = HEIGHT/4;
            minboundary = 30 + player.getScore()/difficulty;
            //check collision
            for (int i=0; i<lowboundaries.size(); i++){
                if (collision(lowboundaries.get(i), player))
                    player.setPlaying(false);
            }
            for (int i=0; i<upboundaries.size(); i++){
                if (collision(upboundaries.get(i), player))
                    player.setPlaying(false);
            }
            //update boundaries
            this.updateUpBoundary();
            this.updateLowBoundary();

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
        else{
            newGameCreated = false;
            if(!newGameCreated) {
                newGame();
            }
        }
    }
    public void updateUpBoundary(){
//        if(player.getScore()%50 == 0){
//            upboundaries.add(new Upboundary(BitmapFactory.decodeResource(getResources(),R.drawable.brick), upboundaries.get(upboundaries.size()-1).getX()+20,0,(int)((rand.nextDouble()*(maxboundary))+1)));
//        }
        for (int i=0; i<upboundaries.size();i++){
            upboundaries.get(i).update();
            if (upboundaries.get(i).getX()<-80){
                upboundaries.remove(i);
                if(upboundaries.get(upboundaries.size()-1).getHeight()>=maxboundary){
                    topDown = false;
                }
                if (upboundaries.get(upboundaries.size()-1).getHeight()<=minboundary){
                    topDown = true;
                }
                if (topDown){
                    upboundaries.add(new Upboundary(BitmapFactory.decodeResource(getResources(),R.drawable.brick),upboundaries.get(upboundaries.size()-1).getX()+80,0,upboundaries.get(upboundaries.size()-1).getHeight()+1));
                }
                else{
                    upboundaries.add(new Upboundary(BitmapFactory.decodeResource(getResources(),R.drawable.brick),upboundaries.get(upboundaries.size()-1).getX()+80,0,upboundaries.get(upboundaries.size()-1).getHeight()-1));
                }
            }
        }
    }
    public void updateLowBoundary(){
//        if(player.getScore()%40 == 0){
//            upboundaries.add(new Upboundary(BitmapFactory.decodeResource(getResources(),R.drawable.brick), upboundaries.get(upboundaries.size()-1).getX()+80,0,(int)((rand.nextDouble()*(maxboundary))+(HEIGHT-maxboundary))));
//        }
        for (int i=0; i<lowboundaries.size();i++){
            lowboundaries.get(i).update();
            if (lowboundaries.get(i).getX()<-80){
                lowboundaries.remove(i);
                if(lowboundaries.get(lowboundaries.size()-1).getY()<=HEIGHT-maxboundary){
                    botDown = true;
                }
                if (lowboundaries.get(lowboundaries.size()-1).getY()>=HEIGHT-minboundary){
                    botDown = false;
                }
                if (botDown){
                    lowboundaries.add(new Lowboundary(BitmapFactory.decodeResource(getResources(),R.drawable.brick),lowboundaries.get(lowboundaries.size()-1).getX()+80,lowboundaries.get(lowboundaries.size()-1).getY()+1));
                }
                else{
                    lowboundaries.add(new Lowboundary(BitmapFactory.decodeResource(getResources(),R.drawable.brick),lowboundaries.get(lowboundaries.size()-1).getX()+80,lowboundaries.get(lowboundaries.size()-1).getY()-1));
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
            //draw boundaries
            for (Upboundary u: upboundaries){
                u.draw(canvas);
            }
            for (Lowboundary l: lowboundaries){
                l.draw(canvas);
            }
        }
    }
    public void newGame(){
        upboundaries.clear();
        lowboundaries.clear();
        enemies.clear();
        smoke.clear();
        minboundary = 30;
        maxboundary = 30;
        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT/2);

        //create initial boundary
        for(int i = 0; i*80<WIDTH+340; i++) {
            if (i == 0) {
                upboundaries.add(new Upboundary(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i * 80, 0, 10));
            } else {
                upboundaries.add(new Upboundary(BitmapFactory.decodeResource(getResources(),R.drawable.brick),i*80,0, upboundaries.get(i-1).getHeight()+3));
            }
        }
        for(int i = 0; i*80<WIDTH+340; i++){
            if(i==0)
            {
                lowboundaries.add(new Lowboundary(BitmapFactory.decodeResource(getResources(),R.drawable.brick),i*80,HEIGHT+100 ));
            }
            //adding borders until the initial screen is filed
            else
            {
                lowboundaries.add(new Lowboundary(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i * 80, lowboundaries.get(i - 1).getY() - 3));
            }
        }
        newGameCreated = true;
    }
}
