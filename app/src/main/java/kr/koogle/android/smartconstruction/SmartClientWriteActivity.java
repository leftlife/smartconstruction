package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import kr.koogle.android.smartconstruction.http.SmartBuild;
import kr.koogle.android.smartconstruction.http.SmartClient;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.RbPreference;

public class SmartClientWriteActivity extends AppCompatActivity {
    private static final String TAG = "SmartClientWriteActivity";
    private RbPreference pref;

    private String intId;
    private String strBuildCode;
    private String photoCode;

    @Bind(R.id.input_build_name) EditText _buildName;
    @Bind(R.id.input_title) EditText _title;
    @Bind(R.id.input_content) EditText _content;
    @Bind(R.id.btn_add_photo) ImageView _addPhoto;
    @Bind(R.id.img_photo) ImageView _photo;

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
        //Toast.makeText(SmartClientWriteActivity.this, "intId : " + intId, Toast.LENGTH_SHORT).show();
        if( !intId.isEmpty() ) {

            final String strBuildCode = SmartSingleton.smartClient.strCate1;
            String strBuildName = "";
            for (SmartBuild sb : SmartSingleton.arrSmartBuilds) {
                if( strBuildCode.equals(sb.strCode) ) {
                    strBuildName = sb.strName;
                }
            }
            _buildName.setText(strBuildName);

            _title.setText(SmartSingleton.smartClient.strTitle);
            Spanned str = Html.fromHtml(SmartSingleton.smartClient.strContent);
            // String str2 = Html.toHtml(str);
            _content.setText(str);
        }

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

        // 이미지 추가 버튼 클릭시 이미리 리스트 페이지 이동
        _addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SmartClientWriteActivity.this, CameraPicListActivity.class);
                intent.putExtra("intId", SmartSingleton.smartClient.intId);
                startActivityForResult(intent, 1001);
                //Toast.makeText(SmartClientViewActivity.this, "intId : " + smartClient.intId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {

            case 1001: // 첨부파일에 사진 추가하기

                if( data != null) {
                    final String intId = data.getStringExtra("intId");
                    final String strFileURL = data.getStringExtra("strFileURL");
                    if (!strFileURL.isEmpty()) {
                        photoCode = intId; // 답글 이미지 코드값 저장 !!
                        Picasso.with(SmartClientWriteActivity.this)
                                .load(strFileURL)
                                .fit() // resize(700,400)
                                .into(_photo);
                        //_imgCommentPhoto.getLayoutParams().height = 200;
                        //_imgCommentPhoto.requestLayout();
                        _photo.setVisibility(View.VISIBLE);
                        ScrollView scrollView = (ScrollView) findViewById(R.id.sv_client_write);
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        //Toast.makeText(SmartClientViewActivity.this, "strFileURL : " + strFileURL, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
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
            // 건축주 협의 게시판 수정하기
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
