package com.mdt.offlinemusic;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import java.util.*;

public class MusicDb extends SQLiteOpenHelper {
 public static class Song { public long id; public String title,artist,album,topic,description,lyrics,path,cover; public Song(){} }
 public MusicDb(Context c){ super(c,"music.db",null,3); }
 @Override public void onCreate(SQLiteDatabase d){ d.execSQL("CREATE TABLE songs(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT NULL,artist TEXT,album TEXT,topic TEXT,description TEXT,lyrics TEXT,path TEXT NOT NULL,cover TEXT,deleted INTEGER NOT NULL DEFAULT 0)"); d.execSQL("CREATE TABLE albums(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE COLLATE NOCASE)"); }
 @Override public void onUpgrade(SQLiteDatabase d,int old,int ver){if(old<2){d.execSQL("CREATE TABLE IF NOT EXISTS albums(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE COLLATE NOCASE)");d.execSQL("INSERT OR IGNORE INTO albums(name) SELECT DISTINCT album FROM songs WHERE album IS NOT NULL AND TRIM(album) != ''");}if(old<3){d.execSQL("ALTER TABLE songs ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0");}}
 public ArrayList<String> albums(){ArrayList<String> a=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT name FROM albums ORDER BY name COLLATE NOCASE",null)){while(c.moveToNext())a.add(c.getString(0));}return a;}
 public boolean addAlbum(String name){if(name==null||name.trim().isEmpty())return false;ContentValues v=new ContentValues();v.put("name",name.trim());return getWritableDatabase().insertWithOnConflict("albums",null,v,SQLiteDatabase.CONFLICT_IGNORE)!=-1;}
 public boolean renameAlbum(String oldName,String newName){
  if(oldName==null||newName==null||newName.trim().isEmpty())return false;
  String cleaned=newName.trim();if(oldName.equals(cleaned))return true;
  SQLiteDatabase d=getWritableDatabase();d.beginTransaction();
  try{
   ContentValues values=new ContentValues();values.put("name",cleaned);
   if(d.updateWithOnConflict("albums",values,"name=?",new String[]{oldName},SQLiteDatabase.CONFLICT_IGNORE)!=1)return false;
   ContentValues newValue=new ContentValues();newValue.put("album",cleaned);
   d.update("songs",newValue,"album=?",new String[]{oldName});
   d.setTransactionSuccessful();return true;
  }finally{d.endTransaction();}
 }
 public void assign(long id,String album){ContentValues v=new ContentValues();v.put("album",album);getWritableDatabase().update("songs",v,"id=?",new String[]{String.valueOf(id)});}
 public void removeAlbum(String name){getWritableDatabase().delete("albums","name=?",new String[]{name});ContentValues v=new ContentValues();v.putNull("album");getWritableDatabase().update("songs",v,"album=?",new String[]{name});}
 public long save(Song s){ ContentValues v=new ContentValues(); v.put("title",s.title);v.put("artist",s.artist);v.put("album",s.album);v.put("topic",s.topic);v.put("description",s.description);v.put("lyrics",s.lyrics);v.put("path",s.path);v.put("cover",s.cover); SQLiteDatabase d=getWritableDatabase(); if(s.id>0){d.update("songs",v,"id=?",new String[]{""+s.id});return s.id;}return d.insert("songs",null,v); }
 public void softDelete(long id){ContentValues v=new ContentValues();v.put("deleted",1);getWritableDatabase().update("songs",v,"id=?",new String[]{""+id});}
 public void restore(Collection<Long> ids){setDeleted(ids,0);}
 public void setDeleted(Collection<Long> ids,int deleted){SQLiteDatabase d=getWritableDatabase();d.beginTransaction();try{for(Long id:ids){ContentValues v=new ContentValues();v.put("deleted",deleted);d.update("songs",v,"id=?",new String[]{String.valueOf(id)});}d.setTransactionSuccessful();}finally{d.endTransaction();}}
 public void permanentDelete(Collection<Long> ids){SQLiteDatabase d=getWritableDatabase();d.beginTransaction();try{for(Long id:ids)d.delete("songs","id=? AND deleted=1",new String[]{String.valueOf(id)});d.setTransactionSuccessful();}finally{d.endTransaction();}}
 public ArrayList<Song> deletedSongs(){return list(true);}
 public ArrayList<Song> all(){return list(false);}
 private ArrayList<Song> list(boolean deleted){ArrayList<Song> out=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT id,title,artist,album,topic,description,lyrics,path,cover FROM songs WHERE deleted="+(deleted?"1":"0")+" ORDER BY title COLLATE NOCASE",null)){while(c.moveToNext()){Song s=new Song();s.id=c.getLong(0);s.title=c.getString(1);s.artist=c.getString(2);s.album=c.getString(3);s.topic=c.getString(4);s.description=c.getString(5);s.lyrics=c.getString(6);s.path=c.getString(7);s.cover=c.getString(8);out.add(s);}}return out;}
 public Song get(long id){for(Song s:all())if(s.id==id)return s;return null;}

}
