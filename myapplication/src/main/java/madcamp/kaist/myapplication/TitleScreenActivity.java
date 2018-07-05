package madcamp.kaist.myapplication;

        import android.app.Activity;
        import android.content.Intent;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Handler;
        import android.support.annotation.RequiresApi;
        import android.widget.ImageView;

        import com.bumptech.glide.Glide;


public class TitleScreenActivity extends Activity {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.titlescreen);

        ImageView imageView = (ImageView) findViewById(R.id.openimg);

        Glide.with(this).load(R.raw.opening).into(imageView);

        int secondsDelayed = 2500;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(TitleScreenActivity.this, MainActivity.class));
                finish();
            }
        }, secondsDelayed);
    }
}