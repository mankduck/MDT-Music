package com.mdt.offlinemusic;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import java.util.*;

public class PlayerService extends Service {
 public static final String CHANNEL="mdt_playback", ACTION="com.mdt.offlinemusic.UPDATE";
 public static PlayerService instance; public MediaPlayer player; public MusicDb.Song song; public boolean repeat=false; public float speed=1f; public ArrayList<Long> queue=new ArrayList<>();
 private final Handler handler=new Handler(Looper.getMainLooper());private Runnable sleep;
 @Override public void onCreate(){super.onCreate();instance=this;speed=getSharedPreferences("settings",MODE_PRIVATE).getFloat("playback_speed",1f);NotificationChannel ch=new NotificationChannel(CHANNEL,"Phát nhạc",NotificationManager.IMPORTANCE_LOW);getSystemService(NotificationManager.class).createNotificationChannel(ch);}
 @Override public int onStartCommand(Intent i,int flags,int startId){String action=i==null?"":i.getAction();if("TOGGLE".equals(action))toggle();else if("NEXT".equals(action))next();else if("PREV".equals(action))previous();else if("STOP".equals(action)){stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();}else if("PLAY".equals(action)){ArrayList<Long> ids=(ArrayList<Long>)i.getSerializableExtra("queue");if(ids!=null)setQueue(ids);play(i.getLongExtra("id",-1));}else notifyState();return START_STICKY;}
 public void play(long id){MusicDb.Song s=new MusicDb(this).get(id);if(s==null)return;try{if(player!=null){player.reset();player.release();}player=new MediaPlayer();player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());if(s.path==null||!new java.io.File(s.path).isFile())throw new java.io.FileNotFoundException("Tệp nhạc không tồn tại: "+s.path);player.setDataSource(s.path);player.prepare();applySpeed();song=s;player.setOnCompletionListener(p->{if(repeat){p.seekTo(0);p.start();notifyState();}else next();});player.start();notifyState();}catch(Exception ex){android.util.Log.e("MDTPlayer","Không thể phát nhạc",ex);song=null;android.widget.Toast.makeText(getApplicationContext(),"Không thể phát nhạc: "+ex.getMessage(),android.widget.Toast.LENGTH_LONG).show();notifyState();stopSelf();}}
 public void applySpeed(){if(player==null)return;try{player.setPlaybackParams(player.getPlaybackParams().setSpeed(speed));}catch(IllegalStateException | IllegalArgumentException ex){android.util.Log.w("MDTPlayer","Không đặt được tốc độ phát",ex);}}
 public void setSpeed(float newSpeed){speed=newSpeed;getSharedPreferences("settings",MODE_PRIVATE).edit().putFloat("playback_speed",speed).apply();applySpeed();notifyState();}
 public void setQueue(ArrayList<Long> ids){queue=new ArrayList<>(ids);}
 public void toggle(){if(player==null)return;if(player.isPlaying())player.pause();else player.start();notifyState();}
 public void next(){if(song==null)return;int index=queue.indexOf(song.id);if(index<0){return;}if(index+1<queue.size())play(queue.get(index+1));else if(repeat&&!queue.isEmpty())play(queue.get(0));else {if(player!=null)player.pause();notifyState();}}
 public void previous(){if(song==null)return;if(player!=null&&player.getCurrentPosition()>3000){player.seekTo(0);notifyState();return;}int index=queue.indexOf(song.id);if(index>0)play(queue.get(index-1));else if(player!=null)player.seekTo(0);notifyState();}
 public void seek(int ms){if(player!=null){player.seekTo(ms);notifyState();}}
 public boolean playing(){return player!=null&&player.isPlaying();}
 public int duration(){return player==null?0:player.getDuration();}public int position(){return player==null?0:player.getCurrentPosition();}
 public void setSleep(int minutes){if(sleep!=null)handler.removeCallbacks(sleep);sleep=null;if(minutes>0){sleep=()->{if(player!=null&&player.isPlaying())player.pause();notifyState();};handler.postDelayed(sleep,minutes*60000L);}}
 public void notifyState(){sendBroadcast(new Intent(ACTION).setPackage(getPackageName()));if(song==null)return;Intent open=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);PendingIntent content=PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);Notification.Builder n=new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_media_play).setContentTitle(song.title).setContentText(song.artist).setContentIntent(content).setOnlyAlertOnce(true).setOngoing(playing()).setVisibility(Notification.VISIBILITY_PUBLIC).addAction(new Notification.Action.Builder(android.R.drawable.ic_media_previous,"Trước",pending("PREV",1)).build()).addAction(new Notification.Action.Builder(playing()?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play,playing()?"Tạm dừng":"Phát",pending("TOGGLE",2)).build()).addAction(new Notification.Action.Builder(android.R.drawable.ic_media_next,"Tiếp",pending("NEXT",3)).build()).setStyle(new Notification.MediaStyle().setShowActionsInCompactView(0,1,2));startForeground(11,n.build());}
 private PendingIntent pending(String action,int id){Intent i=new Intent(this,PlayerService.class).setAction(action);return PendingIntent.getService(this,id,i,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);}
 @Override public void onDestroy(){if(sleep!=null)handler.removeCallbacks(sleep);if(player!=null){player.release();player=null;}instance=null;super.onDestroy();}
 @Override public IBinder onBind(Intent i){return null;}
}
