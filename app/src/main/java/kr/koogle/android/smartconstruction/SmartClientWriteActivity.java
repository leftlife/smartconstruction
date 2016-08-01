package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import butterknife.ButterKnife;
import kr.koogle.android.smartconstruction.http.SmartClient;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.RbPreference;

public class SmartClientWriteActivity extends AppCompatActivity {
    private static final String TAG = "SmartClientWriteActivity";
    private RbPreference pref;
    private String intId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_write);
        ButterKnife.bind(this);

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getApplicationContext());

        // 리스트 클릭시 넘어온값 받기 !!
        intId = String.valueOf(getIntent().getExtras().getInt("intId"));
        Toast.makeText(SmartClientWriteActivity.this, "intId : " + intId, Toast.LENGTH_SHORT).show();

        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_client_write);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ico_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmartClientWriteActivity.this.finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // 사진 촬영 엑티비티 열기 !!
            Intent intent = new Intent(SmartClientWriteActivity.this, CameraPicActivity.class);
            intent.putExtra("intId", 0);
            startActivityForResult(intent, 2001);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
