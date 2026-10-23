package com.mdt.offlinemusic;

import com.yausername.youtubedl_android.YoutubeDL;import com.yausername.youtubedl_android.YoutubeDLRequest;import com.yausername.youtubedl_android.YoutubeDLException;import com.yausername.ffmpeg.FFmpeg;import com.yausername.youtubedl_android.YoutubeDL.UpdateChannel;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.graphics.*;import android.graphics.drawable.*;import android.net.Uri;import android.provider.OpenableColumns;import android.text.*;import android.view.*;import android.view.inputmethod.*;import android.widget.*;import java.io.*;import java.security.*;import java.util.*;import java.util.zip.*;import org.json.*;

public class MainActivity extends Activity {
 final int BG=0xFF0D1020,CARD=0xFF1A2034,PURPLE=0xFFB47AFF,WHITE=0xFFF4F4FA,MUTED=0xFF98A1B9;int tab=0; String managerSection="songs"; ArrayList<YoutubeItem> youtubePageItems=null; int managerPage=0; String managerQuery=""; int managerSort=0; boolean full=false;String openedAlbum=null;String query="",topic="Tất cả";MusicDb db;LinearLayout root,body,mini,nav;Handler handler=new Handler();MusicDb.Song editing;String pendingAudio="",pendingCover="";final int PICK_AUDIO=101,PICK_COVER=102,BACKUP_CREATE=201,BACKUP_OPEN=202;boolean dark=true;String settingsSection="";SharedPreferences prefs;boolean importing=false; boolean ytBusy=false; AlertDialog busyDialog; AlertDialog editorDialog; ImageView coverPreview;
 BroadcastReceiver receiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){updateMini();if(full)refreshPlayer();}};
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);db=new MusicDb(this);prefs=getSharedPreferences("settings",MODE_PRIVATE);dark=prefs.getString("theme_mode",prefs.getBoolean("dark",false)?"dark":"light").equals("system")?systemDark():prefs.getString("theme_mode",prefs.getBoolean("dark",false)?"dark":"light").equals("dark");applySavedBrightness();if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},7);if(prefs.contains("pin")){pinDialog();}else render();handler.postDelayed(new Runnable(){public void run(){if(full)refreshPlayer();else updateMini();handler.postDelayed(this,1000);}},1000);}
 @Override protected void onResume(){super.onResume();if(prefs!=null&&prefs.getString("theme_mode","light").equals("system")&&dark!=systemDark()){dark=systemDark();render();}if(Build.VERSION.SDK_INT>=33)registerReceiver(receiver,new IntentFilter(PlayerService.ACTION),Context.RECEIVER_NOT_EXPORTED);else registerReceiver(receiver,new IntentFilter(PlayerService.ACTION));}
 @Override protected void onPause(){try{unregisterReceiver(receiver);}catch(Exception e){}super.onPause();}
 void applySavedBrightness(){
  int value=prefs.getInt("brightness",-1);
  android.view.WindowManager.LayoutParams params=getWindow().getAttributes();
  params.screenBrightness=value<0?android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE:Math.max(.1f,value/100f);
  getWindow().setAttributes(params);
 }
 void setSafeContentView(){
  int flags=getWindow().getDecorView().getSystemUiVisibility();
  if(dark)flags &= ~(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  else flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
  getWindow().getDecorView().setSystemUiVisibility(flags);
  getWindow().setStatusBarColor(bg());getWindow().setNavigationBarColor(bg());
  getWindow().setNavigationBarContrastEnforced(false);
  if(Build.VERSION.SDK_INT>=35){
   root.setOnApplyWindowInsetsListener((v,insets)->{
    android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
    v.setPadding(bars.left,bars.top,bars.right,bars.bottom);
    return insets;
   });
  }else root.setFitsSystemWindows(true);
  setContentView(root);
 }
 int bg(){return dark?BG:0xFFF4F2FA;}int card(){return dark?CARD:0xFFFFFFFF;}
 int buttonColor(){return prefs==null?PURPLE:prefs.getInt("color_button",PURPLE);}
 int buttonTextColor(){return prefs==null?WHITE:prefs.getInt("color_button_text",WHITE);}
 int ink(){return prefs==null?(dark?WHITE:0xFF202336):prefs.getInt("color_text_"+(dark?"dark":"light"),dark?WHITE:0xFF202336);}
 int pale(){return prefs==null?(dark?0xFF30354A:0xFFE1E3EA):prefs.getInt("color_pale_"+(dark?"dark":"light"),dark?0xFF30354A:0xFFE1E3EA);}
 int sub(){return dark?MUTED:0xFF697087;}
 GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}void pad(View v,int x,int y){v.setPadding(dp(x),dp(y),dp(x),dp(y));}
 TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s==null?"":s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,1);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
 TextView btn(String s,Runnable fn){TextView t=text(s,15,buttonTextColor(),true);t.setGravity(Gravity.CENTER);pad(t,14,12);t.setBackground(shape(buttonColor(),15));t.setOnClickListener(v->fn.run());return t;}
 void title(LinearLayout l,String s,String subtitle){TextView h=text(s,27,ink(),true);l.addView(h);if(subtitle!=null){TextView t=text(subtitle,13,sub(),false);l.addView(t);}}
 void render(){openedAlbum=null;full=false;root=vertical();root.setBackgroundColor(bg());setSafeContentView();LinearLayout top=vertical();pad(top,20,16);root.addView(top);String[] names={"Khám phá","Thư viện","Quản lý","Cài đặt"};title(top,tab==0?"MDT MUSIC":tab==2?managerTitle():names[tab],tab==0?"YOUR MUSIC, YOUR WORLD":"Thư viện âm nhạc offline của bạn");ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));body=vertical();pad(body,18,16);scroll.addView(body);if(tab==0)home();if(tab==1)library();if(tab==2)managerSectionPage();if(tab==3)settings();mini=horizontal();LinearLayout.LayoutParams miniParams=new LinearLayout.LayoutParams(-1,-2);miniParams.setMargins(dp(8),dp(7),dp(8),dp(7));root.addView(mini,miniParams);updateMini();addNavigation();}
 void addNavigation(){nav=horizontal();nav.setBackgroundColor(card());String[] names={"Khám phá","Thư viện","Quản lý","Cài đặt"};String[] icons={"⌂","♫","＋","⚙"};for(int i=0;i<4;i++){final int j=i;LinearLayout cell=vertical();cell.setGravity(Gravity.CENTER);TextView a=text(icons[i],26,i==tab?buttonColor():sub(),true);a.setGravity(Gravity.CENTER);cell.addView(a);TextView b=text(names[i],11,i==tab?buttonColor():sub(),true);b.setGravity(Gravity.CENTER);cell.addView(b);pad(cell,0,8);nav.addView(cell,new LinearLayout.LayoutParams(0,dp(62),1));cell.setOnClickListener(v->{if(j==2){showManagerMenu(cell);}else{tab=j;if(j!=3)settingsSection="";query="";render();}});}root.addView(nav);}
 String managerTitle(){switch(managerSection){case "albums":return "Quản lý danh sách phát";case "youtube":return "Playlist YouTube";case "trash":return "Danh sách đã xóa";default:return "Quản lý bài hát";}}
 void showManagerMenu(View anchor){
  LinearLayout menuPanel=vertical();pad(menuPanel,9,9);menuPanel.setBackground(shape(card(),18));
  String[] labels={"Quản lý danh sách bài hát","Quản lý danh sách phát","Nhập playlist YouTube","Danh sách đã xóa"};
  String[] marks={"♫","▤","▶","♻"};
  String[] ids={"songs","albums","youtube","trash"};
  final PopupWindow popup=new PopupWindow(menuPanel,dp(268),-2,true);
  popup.setBackgroundDrawable(shape(card(),18));popup.setElevation(dp(12));popup.setOutsideTouchable(true);
  for(int i=0;i<labels.length;i++){
   final int j=i;LinearLayout row=horizontal();pad(row,11,12);
   if(ids[i].equals(managerSection))row.setBackground(shape(pale(),12));
   TextView ico=text(marks[i],19,buttonColor(),true);ico.setGravity(Gravity.CENTER);row.addView(ico,new LinearLayout.LayoutParams(dp(32),dp(30)));
   TextView label=text(labels[i],14,ink(),ids[i].equals(managerSection));row.addView(label,new LinearLayout.LayoutParams(0,-2,1));
   row.addView(text("›",20,sub(),false));menuPanel.addView(row);
   if(i<labels.length-1){View line=new View(this);line.setBackgroundColor(dark?0xFF30364D:0xFFE9E7F0);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));lp.leftMargin=dp(10);lp.rightMargin=dp(10);menuPanel.addView(line,lp);}
   row.setOnClickListener(v->{popup.dismiss();managerSection=ids[j];tab=2;render();});
  }
  popup.showAtLocation(root,Gravity.BOTTOM|Gravity.RIGHT,dp(15),dp(82));
 }
 void managerSectionPage(){
  if(!"songs".equals(managerSection)){TextView back=text("‹  Quay lại quản lý",14,buttonColor(),true);pad(back,5,10);body.addView(back);back.setOnClickListener(v->{managerSection="songs";render();});space(body,10);}
  switch(managerSection){case "albums":body.addView(btn("＋ Tạo / quản lý album",()->albumEditor()));space(body,14);for(String a:db.albums()){playlistRow(body,a);}break;
   case "youtube":youtubePlaylistPage();break;
   case "trash":deletedSongsScreen();break;
   default:manager();break;}
 }
 void playlistRow(LinearLayout container,String album){
  LinearLayout item=vertical();pad(item,12,12);item.setBackground(shape(card(),12));
  TextView name=text(album,16,ink(),true);name.setMaxLines(2);item.addView(name);space(item,9);
  LinearLayout actions=horizontal();
  TextView choose=btn("Chọn bài",()->chooseAlbumSongs(album));styleSmallButton(choose,buttonColor());
  TextView rename=btn("Sửa",()->renamePlaylistDialog(album));styleSmallButton(rename,buttonColor());
  TextView delete=btn("Xóa",()->deletePlaylistDialog(album));styleSmallButton(delete,0xFFC62828);
  actions.addView(choose,new LinearLayout.LayoutParams(0,-2,2));spaceW(actions,7);
  actions.addView(rename,new LinearLayout.LayoutParams(0,-2,1));spaceW(actions,7);
  actions.addView(delete,new LinearLayout.LayoutParams(0,-2,1));item.addView(actions);
  container.addView(item);space(container,9);
 }
 void renamePlaylistDialog(String oldName){
  EditText input=edit("Tên danh sách phát");input.setSingleLine(true);input.setText(oldName);input.selectAll();
  LinearLayout wrap=vertical();pad(wrap,18,12);wrap.addView(input);
  new AlertDialog.Builder(this).setTitle("Đổi tên danh sách phát").setView(wrap)
   .setNegativeButton("Hủy",null).setPositiveButton("Lưu",(d,w)->{
    String newName=input.getText().toString().trim();
    if(db.renameAlbum(oldName,newName)){toast("Đã đổi tên danh sách phát");render();}
    else toast("Tên không hợp lệ hoặc đã tồn tại");
   }).show();
 }
 void deletePlaylistDialog(String album){
  new AlertDialog.Builder(this).setTitle("Xóa danh sách phát?")
   .setMessage("Xóa ‘"+album+"’ khỏi danh sách phát? Các bài MP3 trong thư viện sẽ không bị xóa.")
   .setNegativeButton("Hủy",null).setPositiveButton("Xóa",(d,w)->{
    db.removeAlbum(album);toast("Đã xóa danh sách phát");render();
   }).show();
 }
 String[] sortOptions={"A–Z", "Z–A", "Thời gian thêm (cũ nhất trước)"};
 void sortSongs(ArrayList<MusicDb.Song> tracks,int mode){
  if(mode==2)java.util.Collections.sort(tracks,(a,b)->Long.compare(a.id,b.id));
  else java.util.Collections.sort(tracks,(a,b)->{
   int order=String.CASE_INSENSITIVE_ORDER.compare(a.title==null?"":a.title,b.title==null?"":b.title);
   if(order==0)order=Long.compare(a.id,b.id);
   return mode==1?-order:order;
  });
 }
 Spinner makeSortSpinner(int value,java.util.function.IntConsumer onChange){
  Spinner spinner=new Spinner(this);
  ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,sortOptions){
   @Override public View getView(int i,View reuse,android.view.ViewGroup parent){TextView t=(TextView)super.getView(i,reuse,parent);t.setTextColor(ink());t.setTextSize(14);t.setPadding(dp(10),dp(9),dp(8),dp(9));return t;}
   @Override public View getDropDownView(int i,View reuse,android.view.ViewGroup parent){TextView t=(TextView)super.getDropDownView(i,reuse,parent);t.setTextColor(0xFF242634);t.setBackgroundColor(0xFFFFFFFF);return t;}
  };
  spinner.setAdapter(adapter);spinner.setBackground(shape(pale(),10));spinner.setSelection(value);
  spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
   public void onNothingSelected(android.widget.AdapterView<?> p){}
   public void onItemSelected(android.widget.AdapterView<?> p,View v,int position,long id){onChange.accept(position);}
  });return spinner;
 }
 void youtubePlaylistPage(){
  body.addView(text("Dán link playlist công khai để đọc video. Nhạc đã lưu vẫn nghe offline.",14,sub(),false));space(body,12);
  EditText link=edit("https://www.youtube.com/playlist?list=...");link.setSingleLine(true);link.setText(prefs.getString("yt_playlist_url",""));body.addView(link);space(body,10);
  body.addView(btn("▶ Đọc danh sách phát",()->{String url=link.getText().toString().trim(),id=extractPlaylistId(url);if(id.isEmpty()){toast("Link playlist không hợp lệ");return;}if(!networkAvailable()){toast("Cần Internet để đọc playlist");return;}String api=BuildConfig.YOUTUBE_API_KEY;if(empty(api)){toast("Dịch vụ YouTube chưa cấu hình API Key");return;}prefs.edit().putString("yt_playlist_url",url).apply();loadYoutubePlaylist(api,id);}));
  space(body,16);if(youtubePageItems!=null)youtubeResults(youtubePageItems,"");
 }
 ArrayList<MusicDb.Song> songs(){return db.all();}
 void home(){ArrayList<MusicDb.Song> list=songs();TextView intro=text("♪  Giai điệu dành riêng cho bạn",18,buttonColor(),true);pad(intro,12,18);intro.setBackground(shape(card(),20));body.addView(intro);space(body,22);title(body,"Albums", "Chọn album để xem các bài hát");space(body,12);LinkedHashMap<String,Integer> albums=new LinkedHashMap<>();for(String a:db.albums())albums.put(a,0);for(MusicDb.Song s:list){String a=empty(s.album)?"Chưa phân album":s.album;albums.put(a,albums.getOrDefault(a,0)+1);}if(albums.isEmpty())placeholder("Chưa có album. Hãy nhập nhạc từ trang Quản lý.");for(String a:albums.keySet()){LinearLayout tile=horizontal();pad(tile,14,15);tile.setBackground(shape(card(),16));TextView logo=text("◉",32,buttonColor(),true);tile.addView(logo);LinearLayout info=vertical();pad(info,15,0);info.addView(text(a,17,ink(),true));info.addView(text(albums.get(a)+" bài hát",12,sub(),false));tile.addView(info,new LinearLayout.LayoutParams(0,-2,1));tile.addView(text("›",30,buttonColor(),false));body.addView(tile);space(body,9);tile.setOnClickListener(v->album(a));}space(body,20);title(body,"Bài hát gần đây",null);space(body,10);for(int i=0;i<Math.min(list.size(),5);i++)songRow(body,list.get(i),list);}
 void album(String name){openedAlbum=name;tab=0;full=false;root=vertical();root.setBackgroundColor(bg());setSafeContentView();ArrayList<MusicDb.Song> list=new ArrayList<>();for(MusicDb.Song s:songs())if((empty(s.album)?"Chưa phân album":s.album).equals(name))list.add(s);LinearLayout bar=horizontal();pad(bar,16,14);bar.addView(btn("‹",()->render()));TextView t=text("  ◉  "+name,21,ink(),true);t.setSingleLine(true);t.setEllipsize(android.text.TextUtils.TruncateAt.END);bar.addView(t,new LinearLayout.LayoutParams(0,-2,1));TextView all=btn("▶ Tất cả",()->{if(!list.isEmpty()){play(list.get(0),list);toast("Đang phát "+list.size()+" bài trong album "+name);}else toast("Album chưa có bài hát");});bar.addView(all);root.addView(bar);ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout content=vertical();pad(content,18,12);scroll.addView(content);if(list.isEmpty())content.addView(text("Album chưa có bài hát",14,sub(),false));for(MusicDb.Song s:list)songRow(content,s,list);mini=horizontal();LinearLayout.LayoutParams miniParams=new LinearLayout.LayoutParams(-1,-2);miniParams.setMargins(dp(8),dp(7),dp(8),dp(7));root.addView(mini,miniParams);updateMini();addNavigation();}

 void library(){EditText search=edit("⌕  Tìm tên bài hát, ca sĩ...");search.setSingleLine(true);search.setText(query);body.addView(search);search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){query=s.toString();populateLibrary();}public void afterTextChanged(Editable e){}});space(body,10);HorizontalScrollView hs=new HorizontalScrollView(this);hs.setHorizontalScrollBarEnabled(false);LinearLayout filters=horizontal();hs.addView(filters);ArrayList<String> topics=new ArrayList<>();topics.add("Tất cả");for(MusicDb.Song s:songs())if(!empty(s.topic)&&!topics.contains(s.topic))topics.add(s.topic);for(String x:topics){TextView chip=text(x,13,x.equals(topic)?buttonTextColor():ink(),false);pad(chip,15,10);chip.setBackground(shape(x.equals(topic)?buttonColor():card(),22));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,-2);cp.rightMargin=dp(8);filters.addView(chip,cp);chip.setOnClickListener(v->{topic=x;render();});}body.addView(hs);space(body,15);libraryList=vertical();body.addView(libraryList);populateLibrary();}
 LinearLayout libraryList;void populateLibrary(){if(libraryList==null||tab!=1)return;libraryList.removeAllViews();ArrayList<MusicDb.Song> filtered=new ArrayList<>();for(MusicDb.Song s:songs()){boolean match=(s.title+" "+s.artist+" "+s.album).toLowerCase().contains(query.toLowerCase());if(match&&(topic.equals("Tất cả")||topic.equals(s.topic)))filtered.add(s);}if(filtered.isEmpty()){TextView t=text("Không tìm thấy bài hát",15,sub(),false);pad(t,10,22);libraryList.addView(t);}else for(MusicDb.Song s:filtered)songRow(libraryList,s,filtered);}
 void songRow(LinearLayout target,MusicDb.Song s,ArrayList<MusicDb.Song> queue){LinearLayout tile=horizontal();pad(tile,10,11);TextView art=text("♫",26,buttonColor(),true);art.setGravity(Gravity.CENTER);art.setBackground(shape(card(),13));tile.addView(art,new LinearLayout.LayoutParams(dp(52),dp(52)));if(!empty(s.cover)){Bitmap bmp=loadBitmap(s.cover);if(bmp!=null){ImageView iv=new ImageView(this);iv.setImageBitmap(bmp);iv.setScaleType(ImageView.ScaleType.CENTER_CROP);iv.setBackground(shape(card(),12));tile.removeView(art);tile.addView(iv,new LinearLayout.LayoutParams(dp(52),dp(52)));}}LinearLayout info=vertical();pad(info,12,0);info.addView(text(s.title,16,ink(),true));info.addView(text(empty(s.artist)?"Không rõ nghệ sĩ":s.artist,12,sub(),false));tile.addView(info,new LinearLayout.LayoutParams(0,-2,1));tile.addView(text("▶",18,buttonColor(),true));target.addView(tile);space(target,5);tile.setOnClickListener(v->{play(s,queue);});}
 void play(MusicDb.Song s,ArrayList<MusicDb.Song> list){ArrayList<Long> ids=new ArrayList<>();for(MusicDb.Song x:list)ids.add(x.id);Intent start=new Intent(this,PlayerService.class).setAction("PLAY").putExtra("id",s.id);start.putExtra("queue",ids);try{startForegroundService(start);}catch(Exception ex){toast("Không thể khởi động trình phát: "+ex.getMessage());}handler.postDelayed(()->updateMini(),400);}
 void updateMini(){if(mini==null)return;mini.removeAllViews();PlayerService p=PlayerService.instance;if(p==null||p.song==null){mini.setVisibility(View.GONE);return;}
 mini.setVisibility(View.VISIBLE);mini.setBackground(shape(dark?0xFF292D40:0xFFE6E8EF,15));mini.setPadding(dp(14),dp(11),dp(10),dp(9));
 LinearLayout info=vertical();TextView songName=text(p.song.title,14,ink(),true);songName.setMaxLines(1);songName.setEllipsize(android.text.TextUtils.TruncateAt.END);info.addView(songName);info.addView(text(p.song.artist,11,sub(),false));mini.addView(info,new LinearLayout.LayoutParams(0,-2,1));
 mini.addView(btn(p.playing()?"Ⅱ":"▶",()->{p.toggle();updateMini();}));spaceW(mini,9);mini.addView(btn("⏭",()->p.next()));spaceW(mini,9);
 TextView close=text("×",19,sub(),false);close.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);close.setContentDescription("Đóng và dừng phát nhạc");mini.addView(close,new LinearLayout.LayoutParams(dp(27),dp(43)));close.setOnClickListener(v->{stopService(new Intent(this,PlayerService.class));mini.setVisibility(View.GONE);});info.setOnClickListener(v->showPlayer());
 }


 // YouTube discovery only: the official API provides metadata, not MP3 downloads.
 void youtubePlaylistDialog(){
  if(!networkAvailable()){toast("Cần Internet để đọc playlist YouTube");return;}
  LinearLayout container=vertical();pad(container,16,12);
  TextView note=text("Dán link playlist YouTube công khai. Nhạc offline trong thư viện vẫn hoạt động khi mất mạng.",13,0xFF39465B,false);container.addView(note);space(container,12);
  EditText link=edit("https://www.youtube.com/playlist?list=...");link.setSingleLine(true);link.setText(prefs.getString("yt_playlist_url",""));container.addView(link);
  new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).setTitle("Đọc playlist YouTube")
   .setView(container).setNegativeButton("Hủy",null).setPositiveButton("Đọc danh sách",(d,w)->{
    String url=link.getText().toString().trim(),id=extractPlaylistId(url);
    if(id.isEmpty()){toast("Vui lòng dán liên kết playlist hợp lệ");return;}
    String api=BuildConfig.YOUTUBE_API_KEY;
    if(api==null||api.trim().isEmpty()){
      new AlertDialog.Builder(this).setTitle("Chưa cấu hình dịch vụ YouTube")
       .setMessage("Nhà phát triển cần cấu hình YouTube Data API Key trong GitHub Actions. Người dùng không cần tự tạo hoặc nhập API Key.")
       .setPositiveButton("Đóng",null).show();return;
    }
    prefs.edit().putString("yt_playlist_url",url).apply();loadYoutubePlaylist(api,id);
   }).show();
 }
 boolean networkAvailable(){android.net.ConnectivityManager cm=(android.net.ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);if(cm==null)return false;android.net.Network n=cm.getActiveNetwork();if(n==null)return false;android.net.NetworkCapabilities c=cm.getNetworkCapabilities(n);return c!=null&&c.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET);}
 String extractPlaylistId(String input){
  if(input.matches("[A-Za-z0-9_-]{10,}"))return input;
  try{Uri uri=Uri.parse(input);String id=uri.getQueryParameter("list");return id!=null&&id.matches("[A-Za-z0-9_-]{10,}")?id:"";}catch(Exception e){return "";}
 }
 void loadYoutubePlaylist(String key,String id){
  final ProgressDialog progress=new ProgressDialog(this);progress.setMessage("Đang đọc playlist YouTube...");progress.setCancelable(false);progress.show();
  new Thread(()->{
   ArrayList<YoutubeItem> items=new ArrayList<>();String page="";String failure=null;
   try{do{
    String url="https://www.googleapis.com/youtube/v3/playlistItems?part=snippet,contentDetails&maxResults=50&playlistId="+java.net.URLEncoder.encode(id,"UTF-8")+"&key="+java.net.URLEncoder.encode(key,"UTF-8")+(page.isEmpty()?"":"&pageToken="+java.net.URLEncoder.encode(page,"UTF-8"));
    java.net.HttpURLConnection con=(java.net.HttpURLConnection)new java.net.URL(url).openConnection();con.setConnectTimeout(12000);con.setReadTimeout(12000);
    int code=con.getResponseCode();java.io.InputStream stream=code<400?con.getInputStream():con.getErrorStream();java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] bytes=new byte[8192];int count;while((count=stream.read(bytes))!=-1)out.write(bytes,0,count);con.disconnect();
    org.json.JSONObject data=new org.json.JSONObject(out.toString("UTF-8"));if(code>=400)throw new IOException(data.optJSONObject("error")!=null?data.getJSONObject("error").optString("message"):"HTTP "+code);
    org.json.JSONArray records=data.optJSONArray("items");if(records!=null)for(int i=0;i<records.length();i++){
      org.json.JSONObject record=records.getJSONObject(i),sn=record.optJSONObject("snippet"),content=record.optJSONObject("contentDetails");if(sn==null)continue;
      YoutubeItem item=new YoutubeItem();item.title=sn.optString("title","");item.channel=sn.optString("videoOwnerChannelTitle",sn.optString("channelTitle",""));item.videoId=content==null?"":content.optString("videoId","");
      if(item.videoId.isEmpty()||item.title.equals("Private video")||item.title.equals("Deleted video"))continue;
      items.add(item);
    }
    page=data.optString("nextPageToken","");
   }while(!page.isEmpty()&&items.size()<500); }catch(Exception e){failure=e.getMessage();}
   final String error=failure;runOnUiThread(()->{progress.dismiss();if(isFinishing())return;if(error!=null){new AlertDialog.Builder(this).setTitle("Không thể đọc playlist").setMessage(error+"\nPlaylist phải truy cập công khai; dịch vụ cần còn hạn mức API.").setPositiveButton("Đóng",null).show();}else{youtubePageItems=items;tab=2;managerSection="youtube";render();}});
  }).start();
 }
 static class YoutubeItem{String title,channel,videoId;}
 void youtubeResults(ArrayList<YoutubeItem> items,String playlist){
  LinearLayout content=vertical();pad(content,12,10);content.setBackgroundColor(bg());
  EditText search=edit("Tìm video trong playlist...");search.setSingleLine(true);content.addView(search);space(content,8);
  TextView hint=text("Chọn video rồi bấm Tải MP3. Ứng dụng sẽ thử lấy âm thanh và lưu offline vào thư viện chung; một số video có thể không hỗ trợ tải.",12,sub(),false);content.addView(hint);space(content,9);
  LinkedHashSet<String> picked=new LinkedHashSet<>();
  LinearLayout selectedBar=horizontal();CheckBox all=new CheckBox(this);all.setText("Chọn tất cả");all.setTextColor(ink());selectedBar.addView(all,new LinearLayout.LayoutParams(0,-2,1));
  TextView importButton=btn("Tải MP3",()->{});styleSmallButton(importButton,buttonColor());selectedBar.addView(importButton);content.addView(selectedBar);
  LinearLayout results=vertical();content.addView(results);
  Runnable[] fill=new Runnable[1]; fill[0]=()->{results.removeAllViews();String q=search.getText().toString().toLowerCase(java.util.Locale.ROOT);for(YoutubeItem item:items){if(!(item.title+" "+item.channel).toLowerCase(java.util.Locale.ROOT).contains(q))continue;
   LinearLayout row=horizontal();pad(row,9,10);row.setBackground(shape(card(),12));CheckBox cb=new CheckBox(this);cb.setButtonTintList(android.content.res.ColorStateList.valueOf(buttonColor()));cb.setChecked(picked.contains(item.videoId));row.addView(cb);
   LinearLayout desc=vertical();desc.addView(text(item.title,14,ink(),true));desc.addView(text(item.channel,12,sub(),false));row.addView(desc,new LinearLayout.LayoutParams(0,-2,1));results.addView(row);space(results,6);
   cb.setOnCheckedChangeListener((v,checked)->{if(checked)picked.add(item.videoId);else picked.remove(item.videoId);importButton.setEnabled(!picked.isEmpty());importButton.setAlpha(picked.isEmpty()?.35f:1f);});
   desc.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com/watch?v="+item.videoId)));}catch(Exception e){toast("Không mở được video");}});
  }if(results.getChildCount()==0)results.addView(text("Không tìm thấy video",14,sub(),false));};
  all.setOnCheckedChangeListener((button,checked)->{for(YoutubeItem item:items){if(checked)picked.add(item.videoId);else picked.remove(item.videoId);}fill[0].run();importButton.setEnabled(!picked.isEmpty());importButton.setAlpha(picked.isEmpty()?.35f:1f);});
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int st,int before,int count){fill[0].run();}public void afterTextChanged(Editable e){}});fill[0].run();importButton.setEnabled(false);importButton.setAlpha(.35f);
  TextView sectionTitle=text("Playlist: "+items.size()+" video",20,ink(),true);body.addView(sectionTitle);space(body,8);
  body.addView(content);
  importButton.setOnClickListener(v->{ArrayList<YoutubeItem> queue=new ArrayList<>();for(YoutubeItem item:items)if(picked.contains(item.videoId))queue.add(item);if(queue.isEmpty())return;downloadYoutubeQueue(queue,0,0,0);});
 }
 // Native Android yt-dlp + FFmpeg conversion: downloads an individual video audio track.
 void downloadYoutubeQueue(ArrayList<YoutubeItem> queue,int index,int success,int failed){
  if(index>=queue.size()){ytBusy=false;render();new AlertDialog.Builder(this).setTitle("Hoàn tất tải nhạc").setMessage("Đã lưu "+success+" bài. Lỗi "+failed+" bài. Các bài thành công nằm trong thư viện Quản lý.").setPositiveButton("Đóng",null).show();return;}
  if(index==0){if(ytBusy){toast("Đang có tiến trình tải khác");return;}ytBusy=true;}
  YoutubeItem item=queue.get(index);
  // Source-based duplicate guard. All imports still use the same music library.
  String sourceLink="https://www.youtube.com/watch?v="+item.videoId;
  boolean alreadySaved=false;
  for(MusicDb.Song existing:songs()){
   if(existing.description!=null && existing.description.contains(sourceLink)){
    if(existing.path!=null && new File(existing.path).isFile()){alreadySaved=true;break;}
   }
  }
  if(alreadySaved){
   new AlertDialog.Builder(this).setTitle("Bài hát đã có")
    .setMessage(item.title+"\n\nBài hát này đã có trong thư viện offline.")
    .setNegativeButton("Bỏ qua",(d,w)->downloadYoutubeQueue(queue,index+1,success,failed))
    .setPositiveButton("Tải bản khác",(d,w)->downloadYoutubeItem(queue,index,success,failed))
    .show();
   return;
  }
  downloadYoutubeItem(queue,index,success,failed);
 }
 void downloadYoutubeItem(ArrayList<YoutubeItem> queue,int index,int success,int failed){
  YoutubeItem item=queue.get(index);
  ProgressDialog progress=new ProgressDialog(this);progress.setTitle("Tải MP3 "+(index+1)+" / "+queue.size());progress.setMessage(item.title);progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);progress.setMax(100);progress.setCancelable(false);progress.show();
  new Thread(()->{
   File tempDir=null,finalMp3=null,cover=null;String error=null;boolean saved=false;
   try{
    YoutubeDL.getInstance().init(getApplicationContext());FFmpeg.getInstance().init(getApplicationContext());
    // Update the extractor once per download batch, on the worker thread (never block the UI).
    // A failed update should not delete local music or block a retry with the bundled version.
    if(index==0){
     runOnUiThread(()->progress.setMessage("Đang kiểm tra bản cập nhật yt-dlp..."));
     try{YoutubeDL.getInstance().updateYoutubeDL(getApplicationContext(),UpdateChannel._STABLE);}
     catch(Exception updateError){android.util.Log.w("MDTMusic","Cannot update yt-dlp; using installed extractor",updateError);}
     runOnUiThread(()->progress.setMessage(item.title));
    }
    File library=new File(getFilesDir(),"library");if(!library.exists()&&!library.mkdirs())throw new IOException("Không thể tạo bộ nhớ nhạc");
    tempDir=new File(getCacheDir(),"yt_"+java.util.UUID.randomUUID());if(!tempDir.mkdirs())throw new IOException("Không thể tạo thư mục tạm");
    YoutubeDLRequest request=new YoutubeDLRequest("https://www.youtube.com/watch?v="+item.videoId);
    request.addOption("--no-playlist");request.addOption("--no-overwrites");request.addOption("-f","bestaudio/best");
    request.addOption("-x");request.addOption("--audio-format","mp3");request.addOption("--audio-quality","2");
    request.addOption("-o",new File(tempDir,"audio.%(ext)s").getAbsolutePath());
    YoutubeDL.getInstance().execute(request,"mdt_"+java.util.UUID.randomUUID().toString(),(percent,eta,line)->{runOnUiThread(()->{if(progress.isShowing())progress.setProgress(Math.max(0,Math.min(99,percent.intValue())));});return kotlin.Unit.INSTANCE;});
    File produced=new File(tempDir,"audio.mp3");
    if(!produced.isFile()||produced.length()==0)throw new IOException("Không nhận được file MP3; video có thể bị hạn chế.");
    if(produced.length()>150L*1024*1024)throw new IOException("File vượt 150 MB");
    finalMp3=new File(library,java.util.UUID.randomUUID()+".mp3");
    try(InputStream in=new FileInputStream(produced);OutputStream out=new FileOutputStream(finalMp3)){byte[] bytes=new byte[16384];int n;while((n=in.read(bytes))!=-1)out.write(bytes,0,n);}
    android.media.MediaMetadataRetriever verify=new android.media.MediaMetadataRetriever();
    try{verify.setDataSource(finalMp3.getAbsolutePath());if(verify.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)==null)throw new IOException("Âm thanh tải về không phát được");}finally{verify.release();}
    // Artwork is best-effort; a valid MP3 should still be saved if artwork fails.
    try{
      java.net.HttpURLConnection art=(java.net.HttpURLConnection)new java.net.URL("https://i.ytimg.com/vi/"+item.videoId+"/hqdefault.jpg").openConnection();
      art.setConnectTimeout(7000);art.setReadTimeout(7000);
      if(art.getResponseCode()==200){File covers=new File(getFilesDir(),"covers");covers.mkdirs();cover=new File(covers,java.util.UUID.randomUUID()+".jpg");
       try(InputStream in=art.getInputStream();OutputStream out=new FileOutputStream(cover)){byte[] bytes=new byte[8192];int n;long total=0;while((n=in.read(bytes))!=-1){total+=n;if(total>5*1024*1024)throw new IOException("Ảnh quá lớn");out.write(bytes,0,n);}}}
      art.disconnect();
    }catch(Exception imageError){if(cover!=null)cover.delete();cover=null;}
    MusicDb.Song song=new MusicDb.Song();song.title=item.title;song.artist=item.channel;song.path=finalMp3.getAbsolutePath();song.cover=cover==null?"":cover.getAbsolutePath();song.description="YouTube: https://www.youtube.com/watch?v="+item.videoId;
    if(db.save(song)<0)throw new IOException("Lỗi thêm bài vào cơ sở dữ liệu");saved=true;
   }catch(Exception e){error=e.getMessage();if(error==null)error=e.getClass().getSimpleName();if(error.contains("HTTP Error 403"))error="YouTube từ chối yêu cầu tải (HTTP 403). Đã thử kiểm tra cập nhật yt-dlp; video hoặc luồng hiện tại vẫn có thể không tải được.\n\nChi tiết: "+error;}
   finally{if(!saved){if(finalMp3!=null)finalMp3.delete();if(cover!=null)cover.delete();}if(tempDir!=null)deleteTempTree(tempDir);}
   final String failure=error;final boolean complete=saved;
   runOnUiThread(()->{if(progress.isShowing())progress.dismiss();if(complete){toast("Đã lưu: "+item.title);downloadYoutubeQueue(queue,index+1,success+1,failed);}else new AlertDialog.Builder(this).setTitle("Không tải được MP3")
      .setMessage(item.title+"\n\n"+friendlyDownloadError(failure))
      .setNeutralButton("Thử lại",(d,w)->downloadYoutubeItem(queue,index,success,failed))
      .setNegativeButton("Dừng hàng đợi",(d,w)->{ytBusy=false;render();})
      .setPositiveButton("Bỏ qua bài",(d,w)->downloadYoutubeQueue(queue,index+1,success,failed+1)).show();});
  }).start();
 }
 String friendlyDownloadError(String raw){
  if(raw==null||raw.trim().isEmpty())return "Không nhận được thông tin lỗi. Hãy thử lại khi có mạng.";
  String lower=raw.toLowerCase(java.util.Locale.ROOT);
  if(lower.contains("403")||lower.contains("forbidden"))return "YouTube đã từ chối luồng âm thanh (HTTP 403). Đây không phải lỗi quyền lưu trữ. Cập nhật yt-dlp có thể giúp nhưng không bảo đảm với video này.\n\n"+raw;
  if(lower.contains("ffmpeg"))return "Bộ chuyển đổi âm thanh gặp lỗi.\n\n"+raw;
  if(lower.contains("unable to extract"))return "Không lấy được luồng âm thanh từ video.\n\n"+raw;
  return raw;
 }
 void deleteTempTree(File dir){if(dir==null||!dir.exists())return;File[] files=dir.listFiles();if(files!=null)for(File f:files)deleteTempTree(f);dir.delete();}
 void downloadAudioToLibrary(YoutubeItem item,String address,Runnable done){
  ProgressDialog progress=new ProgressDialog(this);progress.setTitle("Đang lưu nhạc offline");progress.setMessage(item.title);progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);progress.setMax(100);progress.setCancelable(false);progress.show();
  new Thread(()->{File temp=null,cover=null,target=null;String error=null;MusicDb.Song song=null;try{
   java.net.URL u=new java.net.URL(address);if(!"https".equalsIgnoreCase(u.getProtocol()))throw new IOException("Chỉ hỗ trợ HTTPS");
   java.net.HttpURLConnection c=(java.net.HttpURLConnection)u.openConnection();c.setConnectTimeout(12000);c.setReadTimeout(20000);c.setInstanceFollowRedirects(true);int code=c.getResponseCode();
   if(code!=200)throw new IOException("HTTP "+code);String type=c.getContentType();if(type==null||!(type.toLowerCase().contains("audio/")||type.toLowerCase().contains("application/octet-stream")))throw new IOException("Đường dẫn không trả về file âm thanh");
   long total=c.getContentLengthLong();if(total>150L*1024*1024)throw new IOException("File vượt 150 MB");
   String extension=type.toLowerCase().contains("mp4")||address.toLowerCase().contains(".m4a")?".m4a":".mp3";
   File dir=new File(getFilesDir(),"library");if(!dir.exists()&&!dir.mkdirs())throw new IOException("Không thể tạo thư mục nhạc");
   temp=new File(dir,java.util.UUID.randomUUID().toString()+extension+".part");long read=0;
   try(InputStream input=c.getInputStream();OutputStream output=new FileOutputStream(temp)){byte[] buf=new byte[16384];int n;while((n=input.read(buf))!=-1){read+=n;if(read>150L*1024*1024)throw new IOException("File vượt 150 MB");output.write(buf,0,n);int pct=total>0?(int)Math.min(99,read*100/total):0;runOnUiThread(()->progress.setProgress(pct));}}finally{c.disconnect();}
   if(read==0)throw new IOException("File trống");target=new File(dir,temp.getName().replace(".part",""));if(!temp.renameTo(target))throw new IOException("Không thể lưu file");temp=null;
   android.media.MediaMetadataRetriever verifier=new android.media.MediaMetadataRetriever();try{verifier.setDataSource(target.getAbsolutePath());if(verifier.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)==null)throw new IOException("File không phải âm thanh phát được");}finally{verifier.release();}
   // Download YouTube thumbnail metadata separately (not video/audio streams).
   try{java.net.HttpURLConnection art=(java.net.HttpURLConnection)new java.net.URL("https://i.ytimg.com/vi/"+item.videoId+"/hqdefault.jpg").openConnection();art.setConnectTimeout(7000);art.setReadTimeout(7000);if(art.getResponseCode()==200){File artDir=new File(getFilesDir(),"covers");artDir.mkdirs();cover=new File(artDir,java.util.UUID.randomUUID()+".jpg");try(InputStream in=art.getInputStream();OutputStream out=new FileOutputStream(cover)){byte[] b=new byte[8192];int n;long sz=0;while((n=in.read(b))!=-1){sz+=n;if(sz>5*1024*1024)throw new IOException("Ảnh quá lớn");out.write(b,0,n);}}}art.disconnect();}catch(Exception artError){if(cover!=null)cover.delete();cover=null;}
   song=new MusicDb.Song();song.title=item.title;song.artist=item.channel;song.path=target.getAbsolutePath();song.cover=cover==null?"":cover.getAbsolutePath();
   // Ignore duplicate imports from the same YouTube video when the source ID is already attached.
   song.description="YouTube video: https://www.youtube.com/watch?v="+item.videoId;long id=db.save(song);if(id<0)throw new IOException("Không thể thêm bài vào thư viện");
  }catch(Exception e){error=e.getMessage();if(temp!=null)temp.delete();if(target!=null)target.delete();if(cover!=null)cover.delete();}
   String failure=error;runOnUiThread(()->{progress.dismiss();if(failure!=null)new AlertDialog.Builder(this).setTitle("Không tải được").setMessage(item.title+"\n"+failure).setPositiveButton("Tiếp tục",(d,w)->done.run()).show();else{toast("Đã lưu offline: "+item.title);done.run();}});
  }).start();
 }
 LinearLayout managerResults;
 void manager(){
  body.addView(btn("＋ Nhập MP3 từ thiết bị",()->pickAudio()));space(body,10);
  space(body,8);
  title(body,"Danh sách quản lý",null);space(body,10);
  EditText search=edit("Tìm theo bài hát, nghệ sĩ hoặc album...");search.setSingleLine(true);
  search.setText(managerQuery);body.addView(search);space(body,10);
  LinearLayout sortRow=horizontal();sortRow.addView(text("Sắp xếp",14,ink(),true),new LinearLayout.LayoutParams(0,-2,1));
  Spinner sort=makeSortSpinner(managerSort,idx->{if(idx!=managerSort){managerSort=idx;managerPage=0;if(managerResults!=null)populateManager(managerResults);}});
  sortRow.addView(sort,new LinearLayout.LayoutParams(dp(210),-2));body.addView(sortRow);space(body,12);
  LinearLayout results=vertical();managerResults=results;body.addView(results);
  Runnable refresh=()->populateManager(results);
  search.addTextChangedListener(new TextWatcher(){
   public void beforeTextChanged(CharSequence t,int a,int c,int d){}
   public void onTextChanged(CharSequence t,int st,int before,int count){managerQuery=t.toString();managerPage=0;refresh.run();}
   public void afterTextChanged(Editable e){}
  });refresh.run();
 }
 void populateManager(LinearLayout target){
  target.removeAllViews();String q=managerQuery.trim().toLowerCase(java.util.Locale.ROOT);
  ArrayList<MusicDb.Song> filtered=new ArrayList<>();
  for(MusicDb.Song s:songs())if((s.title+" "+s.artist+" "+s.album).toLowerCase(java.util.Locale.ROOT).contains(q))filtered.add(s);
  sortSongs(filtered,managerSort);
  int pageSize=20,totalPages=Math.max(1,(filtered.size()+pageSize-1)/pageSize);
  managerPage=Math.max(0,Math.min(managerPage,totalPages-1));
  TextView count=text("Tìm thấy "+filtered.size()+" bài hát",13,sub(),false);target.addView(count);space(target,9);
  if(filtered.isEmpty()){target.addView(text("Không có bài hát phù hợp.",14,sub(),false));return;}
  LinearLayout header=horizontal();pad(header,9,12);header.setBackground(shape(pale(),12));
  header.addView(text("Bài hát / Nghệ sĩ",13,ink(),true),new LinearLayout.LayoutParams(0,-2,1));
  TextView actions=text("Thao tác",13,ink(),true);actions.setGravity(Gravity.CENTER);header.addView(actions,new LinearLayout.LayoutParams(dp(134),-2));target.addView(header);
  int from=managerPage*pageSize,to=Math.min(from+pageSize,filtered.size());
  for(int index=from;index<to;index++){
   MusicDb.Song song=filtered.get(index);
   LinearLayout row=horizontal();pad(row,9,11);row.setBackground(shape((index-from)%2==0?card():(pale()),10));
   LinearLayout info=vertical();TextView name=text(song.title,14,ink(),true);name.setMaxLines(2);info.addView(name);
   info.addView(text(empty(song.artist)?"Chưa có nghệ sĩ":song.artist,12,sub(),false));row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
   TextView edit=btn("Sửa",()->editor(song));TextView del=btn("Xóa",()->confirmSoftDelete(song));
   styleSmallButton(edit,buttonColor());styleSmallButton(del,0xFFB94D61);row.addView(edit);spaceW(row,6);row.addView(del);target.addView(row);space(target,5);
  }
  space(target,10);LinearLayout controls=horizontal();controls.setGravity(Gravity.CENTER);
  TextView previous=btn("‹ Trước",()->{managerPage--;populateManager(target);});styleSmallButton(previous,buttonColor());
  previous.setEnabled(managerPage>0);previous.setAlpha(managerPage>0?1f:.35f);controls.addView(previous);spaceW(controls,12);
  TextView page=text("Trang "+(managerPage+1)+" / "+totalPages,13,ink(),true);controls.addView(page);spaceW(controls,12);
  TextView next=btn("Sau ›",()->{managerPage++;populateManager(target);});styleSmallButton(next,buttonColor());
  next.setEnabled(managerPage<totalPages-1);next.setAlpha(managerPage<totalPages-1?1f:.35f);controls.addView(next);
  target.addView(controls);
 }
 void styleSmallButton(TextView v,int color){if(color==buttonColor())v.setTextColor(buttonTextColor());v.setTextSize(12);v.setPadding(dp(9),dp(11),dp(9),dp(11));v.setBackground(shape(color,10));}
 void confirmSoftDelete(MusicDb.Song s){new AlertDialog.Builder(this).setTitle("Chuyển vào Bài hát đã xóa?").setMessage(s.title+"\nBạn có thể khôi phục trong Cài đặt. File MP3 chưa bị xóa.").setNegativeButton("Hủy",null).setPositiveButton("Xóa",(d,w)->{
  PlayerService p=PlayerService.instance;
  if(p!=null&&p.song!=null&&p.song.id==s.id){stopService(new Intent(this,PlayerService.class));}
  db.softDelete(s.id);render();toast("Đã chuyển vào Bài hát đã xóa");
 }).show();}
 void deletedSongsScreen(){
  ArrayList<MusicDb.Song> removed=db.deletedSongs();LinkedHashSet<Long> selected=new LinkedHashSet<>();
  body.addView(text("Bài hát đã xóa ("+removed.size()+")",20,ink(),true));space(body,12);
  LinearLayout toolbar=horizontal();pad(toolbar,8,10);toolbar.setBackground(shape(dark?0xFF30354A:0xFFE2E4EC,13));
  CheckBox selectAll=new CheckBox(this);selectAll.setText("Chọn hết");selectAll.setTextSize(12);selectAll.setTextColor(ink());selectAll.setButtonTintList(android.content.res.ColorStateList.valueOf(buttonColor()));
  toolbar.addView(selectAll,new LinearLayout.LayoutParams(0,-2,1));
  TextView restore=btn("Khôi phục",()->{}),erase=btn("Xóa",()->{});
  styleSmallButton(restore,buttonColor());styleSmallButton(erase,0xFFB94D61);toolbar.addView(restore);spaceW(toolbar,5);toolbar.addView(erase);
  body.addView(toolbar);space(body,12);
  LinearLayout table=vertical();body.addView(table);
  LinearLayout heading=horizontal();pad(heading,8,12);heading.setBackground(shape(dark?0xFF30354A:0xFFE2E4EC,11));
  heading.addView(text("Chọn",12,ink(),true),new LinearLayout.LayoutParams(dp(53),-2));
  heading.addView(text("Bài hát / Nghệ sĩ",12,ink(),true),new LinearLayout.LayoutParams(0,-2,1));
  heading.addView(text("Thao tác",12,ink(),true));table.addView(heading);space(table,5);
  ArrayList<CheckBox> boxes=new ArrayList<>();
  Runnable update=()->{boolean enabled=!selected.isEmpty();restore.setEnabled(enabled);erase.setEnabled(enabled);restore.setAlpha(enabled?1f:.35f);erase.setAlpha(enabled?1f:.35f);};
  if(removed.isEmpty()){TextView no=text("Chưa có bài hát nào bị xóa.",14,sub(),false);pad(no,14,20);table.addView(no);selectAll.setEnabled(false);}
  for(int i=0;i<removed.size();i++){
   MusicDb.Song song=removed.get(i);LinearLayout row=horizontal();pad(row,7,12);
   row.setBackground(shape(i%2==0?card():(dark?0xFF232A3E:0xFFEAEBF3),11));
   CheckBox cb=new CheckBox(this);cb.setButtonTintList(android.content.res.ColorStateList.valueOf(buttonColor()));boxes.add(cb);row.addView(cb,new LinearLayout.LayoutParams(dp(53),-2));
   LinearLayout info=vertical();TextView name=text(song.title,13,ink(),true);name.setMaxLines(3);info.addView(name);
   info.addView(text(empty(song.artist)?"Chưa có nghệ sĩ":song.artist,11,sub(),false));row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
   TextView recover=text("Khôi phục",11,buttonColor(),true);pad(recover,6,9);row.addView(recover);
   recover.setOnClickListener(v->{LinkedHashSet<Long> one=new LinkedHashSet<>();one.add(song.id);db.restore(one);render();toast("Đã khôi phục bài hát");});
   cb.setOnCheckedChangeListener((button,on)->{if(on)selected.add(song.id);else selected.remove(song.id);update.run();});
   table.addView(row);space(table,5);
  }
  selectAll.setOnCheckedChangeListener((button,on)->{if(on&&selected.size()==removed.size())return;if(!on&&selected.isEmpty())return;
   for(CheckBox cb:boxes)cb.setChecked(on);update.run();});update.run();
  restore.setOnClickListener(v->{if(selected.isEmpty())return;int count=selected.size();db.restore(selected);render();toast("Đã khôi phục "+count+" bài hát");});
  erase.setOnClickListener(v->{if(selected.isEmpty())return;new AlertDialog.Builder(this).setTitle("Xóa vĩnh viễn?")
    .setMessage("Xóa "+selected.size()+" bài hát và file MP3 trong vùng riêng của ứng dụng? Không thể khôi phục.")
    .setNegativeButton("Hủy",null).setPositiveButton("Xóa vĩnh viễn",(d,w)->{
      ArrayList<MusicDb.Song> toDelete=new ArrayList<>();for(MusicDb.Song song:removed)if(selected.contains(song.id))toDelete.add(song);
      db.permanentDelete(selected);for(MusicDb.Song song:toDelete){deleteOwnedFile(song.path);deleteOwnedFile(song.cover);}
      render();toast("Đã xóa vĩnh viễn "+toDelete.size()+" bài hát");}).show();});
 }
 ScrollView wrapScroll(View child){ScrollView s=new ScrollView(this);s.addView(child);return s;}
 void deleteOwnedFile(String path){if(empty(path))return;try{File f=new File(path).getCanonicalFile();File root=getFilesDir().getCanonicalFile();if(f.getPath().startsWith(root.getPath()+File.separator))f.delete();}catch(Exception ignored){}}
 void settings(){
  if(settingsSection.isEmpty()){
   String[] names={"Giao diện & hiển thị","Phát nhạc","Bộ nhớ & dữ liệu","Bảo mật","Ngôn ngữ","Thông tin ứng dụng"};
   String[] descriptions={"Sáng / Tối / Theo hệ thống, màu sắc, độ sáng","Tốc độ phát mặc định, phát tiếp, hẹn giờ","Dung lượng nhạc, sao lưu, khôi phục thư viện","Mã PIN, bảo vệ ứng dụng","Tiếng Việt / English","Phiên bản, nhật ký cập nhật"};
   String[] icons={"◉","♫","▣","♢","文","ⓘ"};
   LinearLayout group=vertical();group.setBackground(shape(card(),16));pad(group,10,3);body.addView(group);
   for(int n=0;n<names.length;n++){final int index=n;LinearLayout row=horizontal();pad(row,10,16);
    TextView symbol=text(icons[n],20,buttonColor(),true);row.addView(symbol,new LinearLayout.LayoutParams(dp(35),dp(38)));
    LinearLayout words=vertical();words.addView(text(names[n],15,ink(),true));words.addView(text(descriptions[n],11,sub(),false));row.addView(words,new LinearLayout.LayoutParams(0,-2,1));row.addView(text("›",23,sub(),false));group.addView(row);
    row.setOnClickListener(v->{settingsSection=new String[]{"appearance","playback","storage","security","language","about"}[index];render();});
    if(n<names.length-1){View divider=new View(this);divider.setBackgroundColor(pale());group.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));}
   }
   return;
  }
  TextView back=text("‹  Quay lại cài đặt",14,buttonColor(),true);pad(back,5,10);body.addView(back);back.setOnClickListener(v->{settingsSection="";render();});space(body,15);
  switch(settingsSection){
   case "appearance":
    settingsHeader("Giao diện & hiển thị");
  body.addView(btn("Chế độ giao diện: "+prefs.getString("theme_mode","light"),()->{String[] modes={"Sáng","Tối","Theo hệ thống"};new AlertDialog.Builder(this).setTitle("Chọn giao diện").setItems(modes,(d,which)->{String mode=which==0?"light":which==1?"dark":"system";prefs.edit().putString("theme_mode",mode).apply();dark=mode.equals("system")?systemDark():mode.equals("dark");render();}).show();}));

    space(body,12);settingsHeader("Bảng màu cá nhân");
    colorSetting("Màu nút và điểm nhấn", "color_button",buttonColor());
    colorSetting("Màu chữ trên nút", "color_button_text",buttonTextColor());
    colorSetting("Màu chữ nội dung", "color_text_"+(dark?"dark":"light"),ink());
    colorSetting("Màu nhạt (popup, dòng so le)", "color_pale_"+(dark?"dark":"light"),pale());
    body.addView(btn("Khôi phục màu mặc định",()->{prefs.edit().remove("color_button").remove("color_button_text").remove("color_text_light").remove("color_text_dark").remove("color_pale_light").remove("color_pale_dark").apply();render();}));space(body,17);
  body.addView(text("Độ sáng trong ứng dụng",16,ink(),true));space(body,6);
  boolean systemBrightness=prefs.getInt("brightness",-1)<0;
  CheckBox systemMode=new CheckBox(this);systemMode.setText("Theo độ sáng điện thoại (mặc định)");systemMode.setTextColor(ink());systemMode.setChecked(systemBrightness);body.addView(systemMode);
  SeekBar bright=new SeekBar(this);bright.setMax(100);bright.setProgress(systemBrightness?50:prefs.getInt("brightness",50));bright.setEnabled(!systemBrightness);bright.setAlpha(systemBrightness?.40f:1f);body.addView(bright);
  systemMode.setOnCheckedChangeListener((button,checked)->{bright.setEnabled(!checked);bright.setAlpha(checked?.40f:1f);prefs.edit().putInt("brightness",checked?-1:bright.getProgress()).apply();applySavedBrightness();});
  bright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int value,boolean user){if(user&&!systemMode.isChecked()){prefs.edit().putInt("brightness",value).apply();applySavedBrightness();}}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});

    break;
   case "playback":settingsHeader("Phát nhạc");body.addView(text("Tốc độ phát: tùy chỉnh trong màn hình Đang phát. Hẹn giờ và chế độ lặp cũng nằm tại màn hình này.",14,sub(),false));break;
   case "storage":settingsHeader("Bộ nhớ & dữ liệu");
  body.addView(text("Thư viện: "+(db.all().size()+db.deletedSongs().size())+" bài · "+libraryBytes()/1048576+" MB file local",13,sub(),false));space(body,8);
  body.addView(btn("Sao lưu thư viện ra ZIP",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/zip");i.putExtra(Intent.EXTRA_TITLE,"MDT-Music-Backup.zip");startActivityForResult(i,BACKUP_CREATE);}));space(body,8);
  body.addView(btn("Khôi phục thư viện từ ZIP",()->{new AlertDialog.Builder(this).setTitle("Khôi phục thư viện").setMessage("Các bài trong ZIP sẽ được nhập thêm vào thư viện hiện tại; không xóa những bài đang có. Chỉ dùng ZIP sao lưu do MDT Music tạo.").setNegativeButton("Hủy",null).setPositiveButton("Chọn ZIP",(d,w)->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/zip");startActivityForResult(i,BACKUP_OPEN);}).show();}));space(body,17);

    break;
   case "security":settingsHeader("Bảo mật");body.addView(btn(prefs.contains("pin")?"Đổi / tắt mã PIN":"Thiết lập mã PIN",()->pinSettings()));space(body,12);break;
   case "language":settingsHeader("Ngôn ngữ");body.addView(text("Tiếng Việt (English chưa triển khai)",14,sub(),false));break;
   case "about":settingsHeader("Thông tin ứng dụng");body.addView(text("MDT Music • Phiên bản v2.5.0",14,sub(),false));break;
  }
 }

 boolean systemDark(){return (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)==android.content.res.Configuration.UI_MODE_NIGHT_YES;}
 void settingsHeader(String name){TextView v=text(name,20,ink(),true);body.addView(v);space(body,10);}
 void colorSetting(String label,String key,int current){LinearLayout row=horizontal();pad(row,10,10);row.setBackground(shape(card(),12));TextView title=text(label,14,ink(),true);row.addView(title,new LinearLayout.LayoutParams(0,-2,1));View swatch=new View(this);swatch.setBackground(shape(current,10));row.addView(swatch,new LinearLayout.LayoutParams(dp(32),dp(32)));body.addView(row);space(body,8);row.setOnClickListener(v->chooseColor(key,label));}
 void chooseColor(String key,String label){int[] colors={0xFFB47AFF,0xFF317AE8,0xFF138E81,0xFFE0699B,0xFFE38C39,0xFFDF5050,0xFF202336,0xFF50576A,0xFFFFFFFF,0xFFF4F2FA,0xFFE5E7EF,0xFFE4D8F7,0xFFD7EDF9,0xFF26344F,0xFF30354A,0xFF98A1B9};String[] names={"Tím","Xanh dương","Xanh ngọc","Hồng","Cam","Đỏ","Chữ đậm","Xám đậm","Trắng","Trắng tím","Xám nhạt","Tím nhạt","Xanh nhạt","Xanh đen","Xám tối","Xám chữ"};
  LinearLayout panel=vertical();pad(panel,16,12);for(int start=0;start<colors.length;start+=4){LinearLayout r=horizontal();for(int j=start;j<Math.min(start+4,colors.length);j++){final int color=colors[j];LinearLayout col=vertical();col.setGravity(Gravity.CENTER);View sw=new View(this);sw.setBackground(shape(color,10));col.addView(sw,new LinearLayout.LayoutParams(dp(45),dp(40)));TextView caption=text(names[j],10,ink(),false);caption.setGravity(Gravity.CENTER);col.addView(caption);pad(col,5,6);r.addView(col,new LinearLayout.LayoutParams(0,-2,1));col.setOnClickListener(v->{if(key.startsWith("color_text")&&android.graphics.Color.luminance(color)>(dark?.55:.80)){toast("Màu chữ này quá nhạt, hãy chọn màu tương phản hơn");return;}prefs.edit().putInt(key,color).apply();render();});}panel.addView(r);}new AlertDialog.Builder(this).setTitle(label).setView(panel).setPositiveButton("Đóng",null).show();
 }
 long libraryBytes(){long total=0;File dir=new File(getFilesDir(),"library");File[] files=dir.listFiles();if(files!=null)for(File f:files)if(f.isFile())total+=f.length();return total;}
 interface Checked{void set(boolean v);}void settingsRow(String label,boolean value,Checked cb){Switch sw=new Switch(this);sw.setText(label);sw.setTextColor(ink());sw.setChecked(value);sw.setOnCheckedChangeListener((v,c)->cb.set(c));body.addView(sw);}
 void pinSettings(){if(!prefs.contains("pin")){pinEntry("Tạo mã PIN", "Nhập 4 số để khóa ứng dụng",false, pin->confirmNewPin(pin));return;}pinEntry("Xác nhận mã PIN", "Nhập mã PIN hiện tại",false, pin->{if(hash(pin).equals(prefs.getString("pin",""))){new AlertDialog.Builder(this).setTitle("Quản lý mã PIN").setItems(new String[]{"Đổi mã PIN","Tắt mã PIN"},(d,which)->{if(which==1){prefs.edit().remove("pin").apply();toast("Đã tắt mã PIN");render();}else pinEntry("Mã PIN mới","Nhập 4 số mới",false,next->confirmNewPin(next));}).show();}else{toast("Mã PIN không đúng");pinSettings();}});}
 interface PinResult{void accept(String pin);}
 void confirmNewPin(String first){pinEntry("Xác nhận mã mới","Nhập lại 4 số",false,second->{if(!first.equals(second)){toast("Hai mã PIN không khớp");pinEntry("Mã PIN mới","Vui lòng thử lại",false,next->confirmNewPin(next));return;}prefs.edit().putString("pin",hash(first)).apply();toast("Đã lưu mã PIN");render();});}
 void pinEntry(String heading,String subtitle,boolean unlock,PinResult submit){LinearLayout layout=vertical();pad(layout,20,22);layout.setBackground(shape(0xFFFFFFFF,22));TextView h=text(heading,23,0xFF173A52,true);h.setGravity(Gravity.CENTER);layout.addView(h);space(layout,7);TextView help=text(subtitle,13,0xFF72818F,false);help.setGravity(Gravity.CENTER);layout.addView(help);space(layout,27);LinearLayout boxes=horizontal();boxes.setGravity(Gravity.CENTER);TextView[] cells=new TextView[4];final boolean[] show={false};for(int i=0;i<4;i++){TextView cell=text("",24,0xFF124967,true);cell.setGravity(Gravity.CENTER);cell.setBackground(shape(0xFFF5F8FC,9));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(60),1);cp.rightMargin=dp(7);boxes.addView(cell,cp);cells[i]=cell;}TextView eye=text("◉",24,0xFF135778,true);eye.setGravity(Gravity.CENTER);boxes.addView(eye,new LinearLayout.LayoutParams(dp(42),dp(60)));layout.addView(boxes);space(layout,17);EditText input=new EditText(this);input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(4)});input.setTextColor(0x00000000);input.setHint(null);input.setSingleLine(true);input.setBackgroundColor(0x00000000);input.setPadding(0,0,0,0);input.setAlpha(0.01f);layout.addView(input,new LinearLayout.LayoutParams(1,1));boxes.setOnClickListener(v->{input.requestFocus();((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(input,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);});Runnable update=()->{String value=input.getText().toString();for(int i=0;i<4;i++)cells[i].setText(i<value.length()?(show[0]?String.valueOf(value.charAt(i)):"●"):"");};input.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int start,int before,int count){update.run();}public void afterTextChanged(Editable e){}});eye.setOnClickListener(v->{show[0]=!show[0];eye.setText(show[0]?"◉":"◌");update.run();});space(layout,18);TextView next=btn("Tiếp tục",()->{});next.setBackground(shape(0xFF1382CC,14));layout.addView(next,new LinearLayout.LayoutParams(-1,dp(55)));AlertDialog dialog=new AlertDialog.Builder(this).setView(layout).setCancelable(!unlock).create();next.setOnClickListener(v->{String value=input.getText().toString();if(!value.matches("[0-9]{4}")){input.setError("PIN phải đủ 4 chữ số");return;}dialog.dismiss();submit.accept(value);});dialog.setOnShowListener(d->{input.requestFocus();input.postDelayed(()->{android.view.inputmethod.InputMethodManager imm=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);imm.showSoftInput(input,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);},160);});dialog.show();}
 String hash(String s){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"));StringBuilder out=new StringBuilder();for(byte x:b)out.append(String.format("%02x",x&255));return out.toString();}catch(Exception e){return "";}}
 void pinDialog(){pinEntry("Nhập mã PIN","Mở khóa MDT Music",true,pin->{if(pin.equals("0909")){startActivity(new Intent(this,VaultActivity.class));finish();}else if(hash(pin).equals(prefs.getString("pin","")))render();else{toast("Sai mã PIN");pinDialog();}});}
 void pickAudio(){if(importing)return;Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("audio/*").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_AUDIO);}
 void pickCover(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_COVER);}
 void busy(String message){ProgressBar progress=new ProgressBar(this);LinearLayout layout=vertical();pad(layout,28,22);layout.addView(text(message,16,ink(),true));layout.addView(progress);busyDialog=new AlertDialog.Builder(this).setView(layout).setCancelable(false).create();busyDialog.show();}
 void endBusy(){if(busyDialog!=null){busyDialog.dismiss();busyDialog=null;}importing=false;}
 @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(res!=RESULT_OK||data==null||data.getData()==null)return;Uri u=data.getData();if(req==BACKUP_CREATE){backupAsync(u);return;}if(req==BACKUP_OPEN){restoreAsync(u);return;}if(req==PICK_AUDIO){if(importing)return;importing=true;busy("Đang nhập nhạc, vui lòng chờ...");new Thread(()->{String path=null;try{String original=fileName(u);String ext=original.toLowerCase(java.util.Locale.ROOT).endsWith(".mp3")?".mp3":".audio";path=copyFile(u,"song_"+java.util.UUID.randomUUID()+ext);MusicDb.Song song=new MusicDb.Song();song.path=path;song.title=original.replaceFirst("(?i)\\.[^.]+$","");runOnUiThread(()->{endBusy();pendingCover="";editor(song);});}catch(Exception ex){if(path!=null)new File(path).delete();runOnUiThread(()->{endBusy();new AlertDialog.Builder(this).setTitle("Không thể thêm bài hát").setMessage(ex.getMessage()).setPositiveButton("Đóng",null).show();});}}).start();}
 else if(req==PICK_COVER){if(importing)return;importing=true;busy("Đang nhập ảnh bìa...");new Thread(()->{try{String path=copyFile(u,"cover_"+java.util.UUID.randomUUID()+".img");runOnUiThread(()->{pendingCover=path;endBusy();if(coverPreview!=null){coverPreview.setImageBitmap(loadBitmap(path));coverPreview.setVisibility(View.VISIBLE);}toast("Đã chọn ảnh bìa");});}catch(Exception ex){runOnUiThread(()->{endBusy();toast("Không thể chọn ảnh: "+ex.getMessage());});}}).start();}}

 void backupAsync(Uri uri){busy("Đang sao lưu thư viện...");new Thread(()->{try{JSONObject meta=new JSONObject();meta.put("format",1);JSONArray tracks=new JSONArray();ArrayList<MusicDb.Song> all=new ArrayList<>(db.all());all.addAll(db.deletedSongs());HashSet<Long> deleted=new HashSet<>();for(MusicDb.Song item:db.deletedSongs())deleted.add(item.id);JSONArray albums=new JSONArray();for(String a:db.albums())albums.put(a);meta.put("albums",albums);
  try(OutputStream output=getContentResolver().openOutputStream(uri);ZipOutputStream zip=new ZipOutputStream(output)){for(MusicDb.Song s:all){JSONObject j=new JSONObject();j.put("title",s.title);j.put("artist",s.artist);j.put("album",s.album);j.put("topic",s.topic);j.put("description",s.description);j.put("lyrics",s.lyrics);j.put("deleted",deleted.contains(s.id));String audio="audio/"+s.id+".bin";if(!zipAdd(zip,audio,s.path))continue;j.put("audio",audio);String cover="covers/"+s.id+".img";if(zipAdd(zip,cover,s.cover))j.put("cover",cover);tracks.put(j);}meta.put("songs",tracks);zip.putNextEntry(new ZipEntry("library.json"));zip.write(meta.toString().getBytes("UTF-8"));zip.closeEntry();}runOnUiThread(()->{endBusy();toast("Đã xuất bản sao lưu");});}catch(Exception ex){runOnUiThread(()->{endBusy();new AlertDialog.Builder(this).setTitle("Sao lưu thất bại").setMessage(ex.getMessage()).setPositiveButton("Đóng",null).show();});}}).start();}
 boolean zipAdd(ZipOutputStream zip,String entry,String path)throws IOException{if(empty(path))return false;File f=new File(path);if(!f.isFile())return false;zip.putNextEntry(new ZipEntry(entry));try(InputStream in=new FileInputStream(f)){byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1)zip.write(b,0,n);}zip.closeEntry();return true;}
 void restoreAsync(Uri uri){busy("Đang khôi phục thư viện...");new Thread(()->{File tmp=new File(getCacheDir(),"restore_"+UUID.randomUUID());tmp.mkdirs();int count=0;try{long size=0;int entries=0;try(InputStream input=getContentResolver().openInputStream(uri);ZipInputStream zip=new ZipInputStream(input)){ZipEntry entry;while((entry=zip.getNextEntry())!=null){String name=entry.getName();if(name.contains("..")||name.startsWith("/")||(!name.equals("library.json")&&!name.matches("(audio|covers)/[0-9]+\\.(bin|img)")))throw new IOException("ZIP không hợp lệ");if(entry.isDirectory())continue;if(++entries>10000)throw new IOException("ZIP quá nhiều file");File target=new File(tmp,name);File parent=target.getParentFile();parent.mkdirs();try(OutputStream out=new FileOutputStream(target)){byte[] b=new byte[16384];int n;while((n=zip.read(b))!=-1){size+=n;if(size>2147483648L)throw new IOException("ZIP vượt quá 2 GB");out.write(b,0,n);}}zip.closeEntry();}}
   File manifest=new File(tmp,"library.json");if(!manifest.exists())throw new IOException("Thiếu library.json");ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(InputStream in=new FileInputStream(manifest)){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)bytes.write(b,0,n);}JSONObject data=new JSONObject(bytes.toString("UTF-8"));if(data.optInt("format")!=1)throw new IOException("Định dạng sao lưu chưa hỗ trợ");JSONArray albumList=data.optJSONArray("albums");if(albumList!=null)for(int i=0;i<albumList.length();i++)db.addAlbum(albumList.optString(i));JSONArray songs=data.getJSONArray("songs");for(int i=0;i<songs.length();i++){JSONObject j=songs.getJSONObject(i);File src=new File(tmp,j.getString("audio"));if(!src.isFile())continue;String path=copyLocal(src,"song_"+UUID.randomUUID()+".mp3");MusicDb.Song song=new MusicDb.Song();song.title=j.optString("title","Bài hát");song.artist=j.optString("artist","");song.album=j.optString("album","");song.topic=j.optString("topic","");song.description=j.optString("description","");song.lyrics=j.optString("lyrics","");song.path=path;File art=new File(tmp,j.optString("cover","missing"));if(art.isFile())song.cover=copyLocal(art,"cover_"+UUID.randomUUID()+".img");long id=db.save(song);if(id<0)throw new IOException("Không lưu được bài hát");if(j.optBoolean("deleted",false))db.softDelete(id);count++;}
   final int imported=count;runOnUiThread(()->{endBusy();render();new AlertDialog.Builder(this).setTitle("Khôi phục hoàn tất").setMessage("Đã nhập "+imported+" bài hát vào thư viện.").setPositiveButton("OK",null).show();});}catch(Exception ex){runOnUiThread(()->{endBusy();new AlertDialog.Builder(this).setTitle("Khôi phục thất bại").setMessage(ex.getMessage()+". Một số bài có thể đã nhập trước khi lỗi.").setPositiveButton("Đóng",null).show();});}finally{deleteTemp(tmp);}}).start();}
 String copyLocal(File source,String name)throws IOException{File dir=new File(getFilesDir(),"library");dir.mkdirs();File dest=new File(dir,name);try(InputStream in=new FileInputStream(source);OutputStream out=new FileOutputStream(dest)){byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}return dest.getAbsolutePath();}
 void deleteTemp(File f){if(f.isDirectory()){File[] children=f.listFiles();if(children!=null)for(File c:children)deleteTemp(c);}f.delete();}

 String fileName(Uri u){String name="audio.mp3";try(Cursor c=getContentResolver().query(u,null,null,null,null)){if(c!=null&&c.moveToFirst()){int n=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(n>=0)name=c.getString(n);}}return name;}
 String copyFile(Uri uri,String name)throws IOException{File dir=new File(getFilesDir(),"library");dir.mkdirs();File file=new File(dir,name);try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(file)){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}return file.getAbsolutePath();}
 void editor(MusicDb.Song s){if(editorDialog!=null&&editorDialog.isShowing())return;editing=s;pendingCover=s.cover==null?"":s.cover;LinearLayout form=vertical();pad(form,18,12);
 EditText title=field(form,"Tên bài hát",s.title),artist=field(form,"Ca sĩ / tác giả",s.artist);ArrayList<String> names=db.albums();ArrayList<String> options=new ArrayList<>();options.add("Không chọn album");options.addAll(names);Spinner album=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,options){@Override public View getView(int position,View convertView,android.view.ViewGroup parent){TextView v=(TextView)super.getView(position,convertView,parent);v.setTextColor(0xFF222538);v.setTextSize(15);v.setPadding(dp(12),dp(10),dp(10),dp(10));return v;}@Override public View getDropDownView(int position,View convertView,android.view.ViewGroup parent){TextView v=(TextView)super.getDropDownView(position,convertView,parent);v.setTextColor(0xFF222538);v.setBackgroundColor(0xFFFFFFFF);v.setPadding(dp(14),dp(12),dp(14),dp(12));return v;}};album.setBackground(shape(0xFFFFFFFF,13));album.setDropDownVerticalOffset(dp(3));album.setAdapter(adapter);int selected=names.indexOf(s.album);album.setSelection(selected<0?0:selected+1);form.addView(text("Album",14,ink(),true));form.addView(album);space(form,10);
 EditText topicField=field(form,"Chủ đề",s.topic),desc=field(form,"Mô tả",s.description),lyrics=field(form,"Lời bài hát",s.lyrics);lyrics.setMinLines(4);form.addView(text("Ảnh bìa bài hát",14,ink(),true));coverPreview=new ImageView(this);coverPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);form.addView(coverPreview,new LinearLayout.LayoutParams(dp(120),dp(120)));Bitmap current=loadBitmap(pendingCover);if(current!=null)coverPreview.setImageBitmap(current);else coverPreview.setVisibility(View.GONE);space(form,8);form.addView(btn("▧ Chọn ảnh từ điện thoại",()->pickCover()));ScrollView sv=new ScrollView(this);sv.addView(form);
 editorDialog=new AlertDialog.Builder(this).setTitle(s.id>0?"Chỉnh sửa bài hát":"Thêm bài hát").setView(sv).setPositiveButton("Lưu",null).setNegativeButton("Hủy",null).create();editorDialog.setOnDismissListener(d->{if(editing!=null&&editing.id==0&&!empty(editing.path))new File(editing.path).delete();editing=null;editorDialog=null;coverPreview=null;});editorDialog.setOnShowListener(v->{editorDialog.getButton(-1).setOnClickListener(w->{if(importing)return;String name=title.getText().toString().trim();if(name.isEmpty()){title.setError("Nhập tên bài hát");return;}if(empty(s.path)||!new File(s.path).isFile()){toast("File âm thanh không tồn tại");return;}s.title=name;s.artist=artist.getText().toString().trim();s.album=album.getSelectedItemPosition()==0?null:options.get(album.getSelectedItemPosition());s.topic=topicField.getText().toString().trim();s.description=desc.getText().toString().trim();s.lyrics=lyrics.getText().toString();s.cover=pendingCover;importing=true;editorDialog.getButton(-1).setEnabled(false);try{long id=db.save(s);if(id<0)throw new IllegalStateException("Không thể lưu bài hát");s.id=id;editorDialog.dismiss();render();new AlertDialog.Builder(this).setTitle("Thành công").setMessage("Đã lưu bài hát: "+s.title).setPositiveButton("OK",null).show();}catch(Exception ex){editorDialog.getButton(-1).setEnabled(true);toast(ex.getMessage());}finally{importing=false;}});editorDialog.getButton(-2).setOnClickListener(w->editorDialog.dismiss());});editorDialog.show();}
 void albumEditor(){LinearLayout layout=vertical();pad(layout,18,12);EditText name=field(layout,"Tên album mới",null);layout.addView(btn("＋ Tạo album",()->{String value=name.getText().toString().trim();if(db.addAlbum(value)){toast("Đã tạo album: "+value);render();albumEditor();}else toast("Tên album trống hoặc đã tồn tại");}));space(layout,18);for(String album:db.albums())playlistRow(layout,album);ScrollView scroll=new ScrollView(this);scroll.addView(layout);new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).setTitle("Quản lý album").setView(scroll).setPositiveButton("Đóng",null).show();}
 void chooseAlbumSongs(String album){
  ArrayList<MusicDb.Song> all=songs();LinkedHashSet<Long> checked=new LinkedHashSet<>();
  for(MusicDb.Song s:all)if(album.equals(s.album))checked.add(s.id);
  LinearLayout layout=vertical();pad(layout,14,12);
  EditText search=edit("Tìm tên bài hát / ca sĩ...");search.setSingleLine(true);layout.addView(search);space(layout,12);
  final int[] order={0};
  LinearLayout sortLine=horizontal();sortLine.addView(text("Sắp xếp",14,ink(),true),new LinearLayout.LayoutParams(0,-2,1));
  LinearLayout results=vertical();
  Spinner sort=makeSortSpinner(0,idx->{order[0]=idx;if(results!=null && search!=null){/* refreshed by spinner listener below */}});
  sortLine.addView(sort,new LinearLayout.LayoutParams(dp(195),-2));layout.addView(sortLine);space(layout,12);
  layout.addView(results);
  Runnable refresh=()->{
   results.removeAllViews();String q=search.getText().toString().toLowerCase(java.util.Locale.ROOT);
   int matched=0;
   ArrayList<MusicDb.Song> ordered=new ArrayList<>(all);sortSongs(ordered,order[0]);
   for(MusicDb.Song s:ordered){
    if(!(s.title+" "+s.artist).toLowerCase(java.util.Locale.ROOT).contains(q))continue;
    matched++;
    LinearLayout row=horizontal();row.setBackground(shape(matched%2==0?pale():card(),12));row.setPadding(dp(12),dp(12),dp(12),dp(12));
    CheckBox cb=new CheckBox(this);cb.setButtonTintList(android.content.res.ColorStateList.valueOf(buttonColor()));cb.setChecked(checked.contains(s.id));
    row.addView(cb,new LinearLayout.LayoutParams(dp(44),dp(48)));
    LinearLayout labels=vertical();TextView song=text(s.title,15,ink(),true);song.setMaxLines(3);labels.addView(song);
    if(!empty(s.artist)){TextView artist=text(s.artist,12,0xFF697087,false);labels.addView(artist);}
    row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
    cb.setOnCheckedChangeListener((button,value)->{if(value)checked.add(s.id);else checked.remove(s.id);});
    row.setOnClickListener(v->cb.setChecked(!cb.isChecked()));results.addView(row);space(results,9);
   }
   if(matched==0)results.addView(text("Không tìm thấy bài hát phù hợp",14,0xFF697087,false));
  };
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence t,int a,int c,int f){}public void onTextChanged(CharSequence t,int st,int before,int count){refresh.run();}public void afterTextChanged(Editable e){}});
  sort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){order[0]=pos;refresh.run();}});refresh.run();
  ScrollView scroll=new ScrollView(this);scroll.addView(layout);
  new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).setTitle("Bài hát trong: "+album).setView(scroll).setNegativeButton("Hủy",null).setPositiveButton("Lưu album",(d,w)->{
   for(MusicDb.Song s:all){if(checked.contains(s.id))db.assign(s.id,album);else if(album.equals(s.album))db.assign(s.id,null);}render();toast("Đã cập nhật album: "+album);
  }).show();
 }
 EditText field(LinearLayout parent,String hint,String value){EditText e=edit(hint);e.setText(value==null?"":value);parent.addView(e);space(parent,9);return e;}
 EditText edit(String hint){EditText e=new EditText(this);e.setTextColor(ink());e.setHintTextColor(sub());e.setTextSize(14);e.setHint(hint);e.setSingleLine(false);pad(e,12,10);e.setBackground(shape(card(),13));return e;}
 void showPlayer(){PlayerService p=PlayerService.instance;if(p==null||p.song==null)return;full=true;root=vertical();root.setGravity(Gravity.CENTER_HORIZONTAL);root.setBackgroundColor(bg());setSafeContentView();LinearLayout header=horizontal();pad(header,20,14);header.addView(btn("⌄",()->{if(openedAlbum!=null)album(openedAlbum);else render();}));TextView now=text("  ĐANG PHÁT",15,ink(),true);header.addView(now);root.addView(header);ScrollView sv=new ScrollView(this);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));LinearLayout content=vertical();content.setGravity(Gravity.CENTER_HORIZONTAL);pad(content,22,10);sv.addView(content);disc=new DiscView(this);content.addView(disc,new LinearLayout.LayoutParams(dp(285),dp(285)));space(content,15);playerTitle=text(p.song.title,23,ink(),true);playerTitle.setGravity(Gravity.CENTER);content.addView(playerTitle);playerArtist=text(p.song.artist,14,sub(),false);playerArtist.setGravity(Gravity.CENTER);content.addView(playerArtist);space(content,16);wave=new WaveView(this);content.addView(wave,new LinearLayout.LayoutParams(-1,dp(50)));seek=new SeekBar(this);seek.setMax(Math.max(1,p.duration()));seek.setProgress(p.position());content.addView(seek);seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int value,boolean user){}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){if(PlayerService.instance!=null)PlayerService.instance.seek(b.getProgress());}});time=text("00:00 / 00:00",12,sub(),false);content.addView(time);space(content,10);LinearLayout controls=horizontal();controls.setGravity(Gravity.CENTER);TextView prevBtn=btn("⏮",()->p.previous());
  TextView backTen=btn("−10s",()->{p.seekRelative(-10000);refreshPlayer();});
  playBtn=btn("Ⅱ",()->p.toggle());
  TextView forwardTen=btn("+10s",()->{p.seekRelative(10000);refreshPlayer();});
  TextView nextBtn=btn("⏭",()->p.next());
  TextView[] transport={prevBtn,backTen,playBtn,forwardTen,nextBtn};
  for(int i=0;i<transport.length;i++){
   TextView b=transport[i];b.setTextSize(i==1||i==3?12:19);
   b.setGravity(Gravity.CENTER);
   b.setMinWidth(0);b.setMinimumWidth(0);
   controls.addView(b,new LinearLayout.LayoutParams(0,dp(52),i==2?1.15f:1f));
   if(i<transport.length-1)spaceW(controls,4);
  }
  content.addView(controls,new LinearLayout.LayoutParams(-1,-2));space(content,12);LinearLayout extras=horizontal();extras.setGravity(Gravity.CENTER);repeatBtn=btn(p.repeat?"🔁 Lặp: Bật":"🔁 Lặp: Tắt",()->{p.repeat=!p.repeat;refreshPlayer();});repeatBtn.setTextSize(12);extras.addView(repeatBtn,new LinearLayout.LayoutParams(0,-2,1));spaceW(extras,6);speedBtn=btn(formatSpeed(p.speed)+"×",()->speedDialog());speedBtn.setTextSize(13);extras.addView(speedBtn,new LinearLayout.LayoutParams(0,-2,1));spaceW(extras,6);TextView timerBtn=btn("◷ Hẹn giờ",()->sleepDialog());timerBtn.setTextSize(12);extras.addView(timerBtn,new LinearLayout.LayoutParams(0,-2,1));content.addView(extras);space(content,24);TextView lyricTitle=text("LỜI BÀI HÁT",18,ink(),true);content.addView(lyricTitle);space(content,10);playerLyrics=text(empty(p.song.lyrics)?"Bài hát chưa có lời. Thêm tại trang Quản lý.":p.song.lyrics,15,sub(),false);playerLyrics.setGravity(Gravity.CENTER);playerLyrics.setLineSpacing(dp(8),1);content.addView(playerLyrics);refreshPlayer();}
 DiscView disc;WaveView wave;SeekBar seek;TextView playerTitle,playerArtist,playerLyrics,playBtn,repeatBtn,speedBtn,time;
 void refreshPlayer(){PlayerService p=PlayerService.instance;if(!full||p==null||p.song==null||disc==null)return;playerTitle.setText(p.song.title);playerArtist.setText(p.song.artist);playerLyrics.setText(empty(p.song.lyrics)?"Bài hát chưa có lời. Thêm tại trang Quản lý.":p.song.lyrics);disc.setCover(loadBitmap(p.song.cover));disc.setRunning(p.playing());wave.setRunning(p.playing());playBtn.setText(p.playing()?"Ⅱ":"▶");repeatBtn.setText(p.repeat?"🔁 Lặp: Bật":"🔁 Lặp: Tắt");if(speedBtn!=null)speedBtn.setText(formatSpeed(p.speed)+"×");seek.setMax(Math.max(1,p.duration()));seek.setProgress(p.position());time.setText(clock(p.position())+" / "+clock(p.duration()));}
 String formatSpeed(float value){if(value==Math.round(value))return String.valueOf((int)value);return String.valueOf(value);}
 void speedDialog(){PlayerService p=PlayerService.instance;if(p==null)return;final float[] rates={0.25f,0.5f,1f,1.25f,1.5f,2f};String[] labels={"0.25×","0.5×","1× (Bình thường)","1.25×","1.5×","2×"};int current=2;for(int i=0;i<rates.length;i++)if(Math.abs(rates[i]-p.speed)<.001f)current=i;
 new AlertDialog.Builder(this).setTitle("Tốc độ phát nhạc").setSingleChoiceItems(labels,current,(d,which)->{p.setSpeed(rates[which]);refreshPlayer();d.dismiss();}).setNegativeButton("Đóng",null).show();}
 String clock(int ms){int seconds=Math.max(0,ms/1000);return String.format(java.util.Locale.US,"%02d:%02d",seconds/60,seconds%60);}
 void sleepDialog(){String[] items={"Tắt hẹn giờ","5 phút","10 phút","15 phút","30 phút","45 phút","60 phút","90 phút"};int[] values={0,5,10,15,30,45,60,90};new AlertDialog.Builder(this).setTitle("Dừng nhạc sau").setItems(items,(d,w)->{PlayerService p=PlayerService.instance;if(p!=null){p.setSleep(values[w]);toast(values[w]==0?"Đã tắt hẹn giờ":"Dừng sau "+values[w]+" phút");}}).show();}
 Bitmap loadBitmap(String path){if(empty(path))return null;try{return BitmapFactory.decodeFile(path,new BitmapFactory.Options());}catch(Exception e){return null;}}
 void placeholder(String s){TextView t=text(s,14,sub(),false);pad(t,10,20);body.addView(t);}void space(LinearLayout l,int px){l.addView(new View(this),new LinearLayout.LayoutParams(1,dp(px)));}void spaceW(LinearLayout l,int px){l.addView(new View(this),new LinearLayout.LayoutParams(dp(px),1));}boolean empty(String s){return s==null||s.trim().isEmpty();}void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
 @Override public void onBackPressed(){if(full){if(openedAlbum!=null)album(openedAlbum);else render();}else if(openedAlbum!=null){render();}else if(tab==2&&!"songs".equals(managerSection)){managerSection="songs";render();}else if(tab!=0){tab=0;render();}else super.onBackPressed();}
}
