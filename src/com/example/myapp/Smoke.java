package com.example.myapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by Mengyu on 2016/2/16.
 */
public class Smoke extends GameObject{
    public int r;
    public Smoke(int x, int y){
        super.x = x;
        super.y = y;
        r = 10;
    }
    public void update(){
        x-=10;
    }
    public void draw(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x-r, y-r, r, paint);
        canvas.drawCircle(x-r+2, y-r-2, r, paint);
        canvas.drawCircle(x-r+4,y-r+1, r, paint);
    }
}
