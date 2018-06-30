package madcamp.kaist.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    // Local Variables for part 1
    private ArrayList<HashMap<String,String>> Data = new ArrayList<HashMap<String, String>>();
    private HashMap<String,String> InputData1 = new HashMap<>();
    private HashMap<String,String> InputData2 = new HashMap<>();
    private ListView listView = (ListView) findViewById(R.id.mylist);

    private void getContacts(){
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        TextView pnums = (TextView) findViewById(R.id.PNoList);
        String tmp = "";
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            tmp += name;
            tmp += "  ";
            tmp += phoneNumber;
            tmp += "\n";
        }
        pnums.setText(tmp);

        phones.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View.OnClickListener load_btn_action = new View.OnClickListener(){
            public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(),"전화번호를 로드합니다",LENGTH_LONG).show();
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(getApplicationContext(),"권한이 필요합니다",LENGTH_LONG).show();
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {
                        getContacts();
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                    }
                }
                else{
                    getContacts();
                }






                //

            }
        };

        // 레거시
        final TabHost tabHost = (TabHost) findViewById(R.id.tabHost1);
        tabHost.setup();
        TabHost.TabSpec ts1 = tabHost.newTabSpec("Tab Spec") ;
        ts1.setContent(R.id.content1) ;
        ts1.setIndicator("전화번호부") ;
        tabHost.addTab(ts1);
        TabHost.TabSpec ts2 = tabHost.newTabSpec("Tab Spec2") ;
        ts2.setContent(R.id.content2) ;
        ts2.setIndicator("갤러리") ;
        tabHost.addTab(ts2);
        TabHost.TabSpec ts3 = tabHost.newTabSpec("Tab Spec3") ;
        ts3.setContent(R.id.content3) ;
        ts3.setIndicator("무엇이될까");
        tabHost.addTab(ts3);
        tabHost.setCurrentTab(0);

        Button btn_load = (Button) findViewById(R.id.load_pno);
        btn_load.setOnClickListener(load_btn_action);



    }

}
