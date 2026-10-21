package com.mdt.offlinemusic;
import android.content.*;import android.graphics.*;import android.view.*;import java.util.*;
public class WaveView extends View {private Paint p=new Paint(3);private boolean running=false;private float phase=0;public WaveView(Context c){super(c);}public void setRunning(boolean r){running=r;invalidate();}
 @Override protected void onDraw(Canvas c){super.onDraw(c);int n=40;float w=getWidth()/(float)n;for(int i=0;i<n;i++){float a=(float)(.18+.75*Math.abs(Math.sin(i*.52+phase)*Math.cos(i*.19-phase*.7)));float h=getHeight()*a;p.setColor(i%3==0?0xFFB76DFF:0xFF6CD7D5);c.drawRoundRect(i*w+2,(getHeight()-h)/2,i*w+w*.64f,(getHeight()+h)/2,5,5,p);}if(running){phase+=.085f;postInvalidateDelayed(40);}}
}
