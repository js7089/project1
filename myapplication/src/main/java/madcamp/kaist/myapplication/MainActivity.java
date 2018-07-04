package madcamp.kaist.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {


    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS = 4;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 23;

    // Local Variables for part 3
    Vibrator v;
    private Set<Integer> set = new HashSet<>();
    private int turns = 0;
    private int Nbullet, Npeople, victim;
    Uri u;
    ContentValues cv;

    private static final int PICK_IMAGE_REQUEST = 1;
    // Local Variables for part 1
    private ArrayList<HashMap<String,String>> Data2 = new ArrayList<HashMap<String, String>>();
    private ListView listView;
    private String basePath;


    public void insertContact(Context ctx, String tophone, String toname){
        try {
            // insert part
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            if (tophone != null) {
                ops.add(ContentProviderOperation.
                        newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, tophone)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());
            }
            if (toname != null) {
                ops.add(ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                toname).build());
            }
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            getContacts();
        }
        catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    public static boolean deleteContact(Context ctx, String phone, String name) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor cur = ctx.getContentResolver().query(contactUri, null, null, null, null);
        try {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        ctx.getContentResolver().delete(uri, null, null);
                        return true;
                    }

                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        } finally {
            cur.close();
        }
        return false;
    }

    public boolean updateContact(Context ctx, String fromphone, String fromname, String tophone, String toname) {
        deleteContact(ctx,fromphone,fromname);
        insertContact(ctx,tophone,toname);
        getContacts();
        return true;
    }

    protected void clearContact() {
        //update phone number
        ContentResolver contentResolver = MainActivity.this.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            contentResolver.delete(uri, null, null);
        }
    }

    private void getContacts(){
        //데이터 초기화
        listView =   (ListView) findViewById(R.id.mylist);
        Data2.clear();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            HashMap<String,String> element = new HashMap<>();
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            element.put("name",name);
            element.put("pNo",phoneNumber);
            Data2.add(element);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,Data2,android.R.layout.simple_list_item_2,new String[]{"name","pNo"},new int[]{android.R.id.text1,android.R.id.text2});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                v.vibrate(40);
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.multitab_tmp);
                dialog.setTitle("Title");

                final EditText NameField = (EditText) dialog.findViewById(R.id.pName);
                final EditText pNoField = (EditText) dialog.findViewById(R.id.pNo);
                NameField.setText(Data2.get(i).get("name"));
                pNoField.setText(Data2.get(i).get("pNo"));
                Button Rewrite = (Button) dialog.findViewById(R.id.rewrite);
                Button Delete = (Button) dialog.findViewById(R.id.delete);

                Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        v.vibrate(40);
                        deleteContact(getApplicationContext(),Data2.get(i).get("pNo"),Data2.get(i).get("name"));
                        Data2.remove(i);
                        dialog.dismiss();
                        getContacts();
                    }
                });
                dialog.show();

                Rewrite.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        v.vibrate(40);
                        if(pNoField.getText().toString().equals("") || NameField.getText().toString().equals("")){
                            Toast.makeText(getApplicationContext(),"빈 칸을 모두 입력해 주세요!",LENGTH_SHORT).show();
                            return;
                        }
                        updateContact(getApplicationContext(),Data2.get(i).get("pNo"),Data2.get(i).get("name"),
                                pNoField.getText().toString(),NameField.getText().toString());
                        Toast.makeText(getApplicationContext(),"연락처 수정 완료.",LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });
        phones.close();
    }

    // Part2
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    //
    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }
    private Bitmap rotate(Bitmap src, float degree) {

        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }

    private void galleryload(){
        File file = new File(basePath);
        imgs = file.list();
        for(int i=0; i<imgs.length; i++){
            imgPath.setText(imgs[i]);
        }

        customGallery = (Gallery)findViewById(R.id.customgallery); // activity_main.xml에서 선언한 Gallery를 연결
        customGalAdapter = new CustomGalleryAdapter(getApplicationContext(), basePath); // 위 Gallery에 대한 Adapter를 선언
        customGallery.setAdapter(customGalAdapter); // Gallery에 위 Adapter를 연결
        // Gallery의 Item을 Click할 경우 ImageView에 보여주도록 함
        customGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Bitmap bm = BitmapFactory.decodeFile(basePath+ File.separator +imgs[position+1]);
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(basePath+ File.separator + imgs[position+1] );
                    int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int exifDegree = exifOrientationToDegrees(exifOrientation);
                    bm= rotate(bm, exifDegree);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "로드 오류", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                final Bitmap bm2 = ThumbnailUtils.extractThumbnail(bm, bm.getWidth() / inSampleSize, bm.getHeight() / inSampleSize);
                resultView.setImageBitmap(bm2);
                imgPath.setText(basePath+File.separator+imgs[position+1]);
                resultView.setVisibility(View.VISIBLE);

                resultView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        final Dialog dialog1 = new Dialog(MainActivity.this);
                        dialog1.setContentView(R.layout.gallery_dia);
                        dialog1.setTitle("Title");

                        Button IDelete = (Button) dialog1.findViewById(R.id.idelete);

                        IDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Uri target = getUriFromPath(basePath+File.separator+imgs[position+1]);
                                getContentResolver().delete(target,null,null);
                                galleryload();
                                resultView.setVisibility(View.INVISIBLE);
                                dialog1.dismiss();

                            }
                        });
                        dialog1.show();
                    }
                });


            }
        });



        //

    }

    public Uri getUriFromPath(String path){
        Uri fileUri = Uri.parse( path );
        String filePath = fileUri.getPath();
        Cursor c = getContentResolver().query( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null, "_data = '" + filePath + "'", null, null );
        c.moveToNext();
        int id = c.getInt( c.getColumnIndex( "_id" ) );
        Uri uri = ContentUris.withAppendedId( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id );

        return uri;
    }

    // 갤러리 로드
    public int inSampleSize = 1;
    private ImageView resultView;
    private TextView imgPath;
    private Gallery customGallery;
    private CustomGalleryAdapter customGalAdapter;
    private String[] imgs;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 권한 요구
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final SoundPool sp = new SoundPool(2, AudioManager.STREAM_MUSIC,0);
        final int reload = sp.load(this,R.raw.reload, 1);
        final int shoot = sp.load(this, R.raw.shoot, 2);

        basePath = "/storage/emulated/0/DCIM/CAMERA";

        imgPath = (TextView)findViewById(R.id.imgpath);
        resultView = (ImageView)findViewById(R.id.resultview);

        View.OnClickListener load_btn_action = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                v.vibrate(40);
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                try{
                    getContacts();
                    Toast.makeText(getApplicationContext(),"전화번호를 로드합니다",LENGTH_LONG).show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        // 앨범 로드 버튼
        Button album_load = (Button) findViewById(R.id.load_album);
        album_load.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                v.vibrate(40);
                galleryload();
            }
        });

        // 새 연락처 버튼
        Button newcontact_btn = (Button) findViewById(R.id.new_pno);
        newcontact_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_CONTACTS}, MY_PERMISSIONS_REQUEST_WRITE_CONTACTS);
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.multitab_tmp);
                dialog.setTitle("Title");

                final EditText NameField = (EditText) dialog.findViewById(R.id.pName);
                final EditText pNoField = (EditText) dialog.findViewById(R.id.pNo);
                Button Rewrite = (Button) dialog.findViewById(R.id.rewrite);
                Button Delete = (Button) dialog.findViewById(R.id.delete);

                Rewrite.setText("등록");
                Delete.setVisibility(View.INVISIBLE);
                Delete.setEnabled(false);
                Rewrite.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        v.vibrate(40);
                        if(pNoField.getText().toString().equals("") || NameField.getText().toString().equals("")){
                            Toast.makeText(getApplicationContext(),"빈 칸을 모두 입력해 주세요!",LENGTH_SHORT).show();
                            return;
                        }
                        insertContact(getApplicationContext(),pNoField.getText().toString(),NameField.getText().toString());
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });



        // 레거시
        final TabHost tabHost = (TabHost) findViewById(R.id.tabHost1);
        tabHost.setup();
        final TabHost.TabSpec ts1 = tabHost.newTabSpec("TabSpec") ;
        ts1.setContent(R.id.content1) ;
        ts1.setIndicator("전화번호부") ;
        tabHost.addTab(ts1);
        final TabHost.TabSpec ts2 = tabHost.newTabSpec("TabSpec2") ;
        ts2.setContent(R.id.content2) ;
        ts2.setIndicator("갤러리") ;
        tabHost.addTab(ts2);
        final TabHost.TabSpec ts3 = tabHost.newTabSpec("TabSpec3") ;
        ts3.setContent(R.id.content3) ;
        ts3.setIndicator("러시안 룰렛");
        tabHost.addTab(ts3);
        tabHost.setCurrentTab(0);

        Button btn_load = (Button) findViewById(R.id.load_pno); // 전화번호부 로드 버튼
        btn_load.setOnClickListener(load_btn_action);

        final Button rullet_start = (Button) findViewById(R.id.btnstart);
        final ImageView rullet_shoot = (ImageView) findViewById(R.id.btnshoot);
        final Button rullet_reset = (Button) findViewById(R.id.btnreset);
        final EditText numofbullets = (EditText) findViewById(R.id.numofbullets);
        final EditText numofpeople = (EditText) findViewById(R.id.numofpeople);
        final ImageView rulletkilled = (ImageView) findViewById(R.id.imgkilled);
        final TextView contextview = (TextView) findViewById(R.id.rullet_context);

        // rullet start 버튼 리스너
        View.OnClickListener rulletstartaction = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(numofbullets.getText().toString().equals("") || numofpeople.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"빈 칸을 모두 입력해 주세요.",LENGTH_SHORT).show();
                }
                else if(Integer.parseInt(numofbullets.getText().toString()) >= Integer.parseInt(numofpeople.getText().toString())){
                    Toast.makeText(getApplicationContext(),"총알의 수는 사람 수보다 적게 설정해야 합니다!",LENGTH_SHORT).show();
                }
                else{ // 룰렛 로직 시작.
                    rullet_shoot.setVisibility(View.VISIBLE);
                    Npeople = Integer.parseInt(numofpeople.getText().toString());
                    Nbullet = Integer.parseInt(numofbullets.getText().toString());
                    numofpeople.setEnabled(false);
                    numofbullets.setEnabled(false);
                    rullet_start.setEnabled(false);
                    rullet_shoot.setEnabled(true);
                    rullet_reset.setEnabled(true);
                    rullet_reset.setVisibility(View.VISIBLE);
                    turns = 0;
                    set.clear();
                    while(set.size()<Nbullet){
                        set.add( (int) Math.floor((Math.random()*Npeople))  +1);
                    }
                }
            }
        };
        rullet_start.setOnClickListener(rulletstartaction);

        View.OnClickListener rulletshootaction = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turns+=1;
                if(set.contains(turns)){
                    Toast.makeText(getApplicationContext(),"탕탕탕탕탕탕탕!", LENGTH_SHORT).show();
                    v.vibrate(600);
                    rulletkilled.setVisibility(View.VISIBLE);
                    victim +=1;
                    int streamId = sp.play(shoot, 1.0F, 1.0F,  2,  0,  1.0F);
                }
                else{
                    v.vibrate(70);
                    rulletkilled.setVisibility(View.INVISIBLE);
                    int streamId2 = sp.play(reload, 1.0F, 1.0F,  1,  0,  1.0F);
                }
                contextview.setText("남은 인원 수 : " + String.valueOf(Npeople-turns) + " / 남은 총알 수 : " + String.valueOf(Nbullet-victim));
                if(Nbullet == victim){
                    rullet_shoot.setEnabled(false);
                    rullet_reset.setVisibility(View.VISIBLE);
                }
            }
        };
        rullet_shoot.setOnClickListener(rulletshootaction);

        View.OnClickListener rulletresetaction = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.vibrate(40);
                rullet_shoot.setVisibility(View.INVISIBLE);
                rullet_reset.setVisibility(View.INVISIBLE);
                numofpeople.setText("");
                numofbullets.setText("");
                numofpeople.setEnabled(true);
                numofbullets.setEnabled(true);
                rullet_start.setEnabled(true);
                rulletkilled.setVisibility(View.INVISIBLE);
                Nbullet=0;
                victim=0;
                set.clear();
                contextview.setText("");
            }
        };
        rullet_reset.setOnClickListener(rulletresetaction);


        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                v.vibrate(40);
            if(ts1.getTag().equals(s)){
                //DIALOG TEST
                hideKeyboard(MainActivity.this);
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_CONTACTS}, MY_PERMISSIONS_REQUEST_WRITE_CONTACTS);
                //
            }else if(ts2.getTag().equals(s)){
                hideKeyboard(MainActivity.this);
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    //galleryload();
  //
                } else if(ts3.getTag().equals(s)){
                    // 룰렛 초기 조건
                    // 숫자필드 둘, 시작버튼 하나 = 활성화&초기화
                    rullet_shoot.setVisibility(View.INVISIBLE);
                    rullet_reset.setVisibility(View.INVISIBLE);
                    numofpeople.setText("");
                    numofbullets.setText("");
                    numofpeople.setEnabled(true);
                    numofbullets.setEnabled(true);
                    rullet_start.setEnabled(true);
                    rulletkilled.setVisibility(View.INVISIBLE);
                    Nbullet=0;
                    victim=0;
                    set.clear();
                }
            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
