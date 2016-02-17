package com.example.myapp;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.sql.SQLOutput;

/**
 * Created by Mengyu on 2015/11/22.
 */
public class MainThread extends Thread {
    private int FPS =30;
    private double averageFPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    public  static Canvas canvas;

    public MainThread(SurfaceHolder surfaceHolder,GamePanel gamePanel){
        super();
        this.surfaceHolder=surfaceHolder;
        this.gamePanel=gamePanel;
    }
    @Override
    public void  run() {
        long startTime;
        long timeMillis;
        long waitTime;
        long totalTime = 0;
        int framCount = 0;
        long targetTime = 1000 / FPS;

        while (running) {

            startTime = System.nanoTime();
            canvas = null;
            //try locking the canvas for pixel editing
            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);
                }
            } catch (Exception e) {
            }
            finally {
                if (canvas!=null){
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                    catch (Exception e){e.printStackTrace();}
                }
            }
            //the time need to update and draw canvas once
            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime=targetTime-timeMillis;

            try{
                this.sleep(waitTime);
            }catch(Exception e){}

            totalTime+=System.nanoTime()-startTime;
            framCount++;
            if (framCount==FPS){
                //calculate average FPS
                averageFPS=100/((totalTime/framCount)/1000000);
                framCount=0;
                totalTime=0;
                System.out.println(averageFPS);
            }
        }
    }
    public void setRunning(boolean b){
        running=b;
    }
}
