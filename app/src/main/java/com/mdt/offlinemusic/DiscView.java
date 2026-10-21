package com.mdt.offlinemusic;

import android.content.*;import android.graphics.*;import android.view.*;

public class DiscView extends View {
 private final Paint p=new Paint(3);private Bitmap cover;private float angle=0;private boolean running=false;
 public DiscView(Context c){super(c);}public void setCover(Bitmap b){cover=b;invalidate();}public void setRunning(boolean r){running=r;invalidate();}
 @Override protected void onDraw(Canvas c){super.onDraw(c);float size=Math.min(getWidth(),getHeight())*.92f;float x=getWidth()/2f,y=getHeight()/2f,r=size/2;c.save();c.rotate(angle,x,y);p.setColor(Color.rgb(15,19,33));c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.STROKE);for(int i=0;i<10;i++){p.setColor(i%2==0?0xFF30394C:0xFF1D2637);p.setStrokeWidth(2);c.drawCircle(x,y,r*(.65f+i*.031f),p);}p.setStyle(Paint.Style.FILL);if(cover!=null){Path mask=new Path();mask.addCircle(x,y,r*.61f,Path.Direction.CW);c.save();c.clipPath(mask);c.drawBitmap(cover,null,new RectF(x-r*.61f,y-r*.61f,x+r*.61f,y+r*.61f),p);c.restore();}else{p.setColor(0xFFB56CFF);c.drawCircle(x,y,r*.59f,p);p.setColor(0xFF6D5AE5);c.drawCircle(x,y,r*.34f,p);}p.setColor(0xFFF3F4F7);c.drawCircle(x,y,r*.075f,p);p.setColor(0xFF181B2C);c.drawCircle(x,y,r*.035f,p);c.restore();if(running){angle=(angle+0.24f)%360;postInvalidateDelayed(16);}}
}
