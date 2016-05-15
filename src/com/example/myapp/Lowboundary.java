package com.example.myapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Mengyu on 2016-05-03.
 */
public class Lowboundary extends GameObject{
    private Bitmap image;
    public Lowboundary(Bitmap res, int x, int y){
        height = 200;
        width = 80;
        this.x = x;
        this.y = y;

        dx = GamePanel.MOVESPEED;
        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }
    public void update(){
        x+=dx;
        if(x<-GamePanel.WIDTH){
            x=0;
        }
    }
    public void draw(Canvas canvas){
        try{canvas.drawBitmap(image, x, y, null);}
        catch(Exception e){
            System.out.println("cannot draw upboundary");
        };
    }
}
