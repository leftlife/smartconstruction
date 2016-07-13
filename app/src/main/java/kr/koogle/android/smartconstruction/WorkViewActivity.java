package kr.koogle.android.smartconstruction;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.RbPreference;

public class WorkViewActivity extends AppCompatActivity {
    private static final String TAG = "WorkViewActivity";
    private RbPreference pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_view);
        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(this);


        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_work_view);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ico_back);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }
}
