package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartBuild;
import kr.koogle.android.smartconstruction.http.SmartClient;
import kr.koogle.android.smartconstruction.http.SmartComment;
import kr.koogle.android.smartconstruction.http.SmartFile;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.RbPreference;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartClientWriteActivity extends AppCompatActivity {
    private static final String TAG = "SmartClientWriteActivity";
    private RbPreference pref;

    //private SmartClient smartClient;

    @Bind(R.id.input_build_name) EditText _buildName;
    @Bind(R.id.input_title) EditText _title;
    @Bind(R.id.input_content) EditText _content;
    @Bind(R.id.btn_add_photo) ImageView _addPhoto;
    @Bind(R.id.img_photo) ImageView _photo;

    // intent 로 넘어온 값 받기
    private Intent intent;

    // UTILITY METHODS
    private Toast mToast;
    private MaterialDialog md;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_write);
        ButterKnife.bind(this);
        // intent 등록
        intent = getIntent();

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getApplicationContext());

        //smartClient = new SmartClient();

        // 리스트 클릭시 넘어온값 받기 !!
        SmartSingleton.smartClient.intId = getIntent().getExtras().getInt("intId");
        //Toast.makeText(SmartClientWriteActivity.this, "intId : " + intId, Toast.LENGTH_SHORT).show();
        if( SmartSingleton.smartClient.intId > 0 ) {

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

            if(SmartSingleton.smartClient.arrFiles.size() > 0) {
                _photo.setVisibility(View.VISIBLE);
                Picasso.with(SmartClientWriteActivity.this)
                        .load(SmartSingleton.smartClient.arrFiles.get(0).strURL + SmartSingleton.smartClient.arrFiles.get(0).strName)
                        .fit() // resize(700,400)
                        .into(_photo);
                _photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse(SmartSingleton.smartClient.arrFiles.get(0).strURL + SmartSingleton.smartClient.arrFiles.get(0).strName);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });
            }
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
                //intent.putExtra("intId", SmartSingleton.smartClient.intId);
                startActivityForResult(intent, 22001);
                //Toast.makeText(SmartClientViewActivity.this, "intId : " + smartClient.intId, Toast.LENGTH_SHORT).show();
            }
        });

        _buildName.setInputType(0);
        _buildName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    final ArrayList<String> arrBuild = new ArrayList<String>();
                    if (!SmartSingleton.arrSmartBuilds.isEmpty()) {
                        //ArrayList<SmartBuild> arrBuild = SmartSingleton.arrSmartBuilds;
                        for (int i = 0; i < SmartSingleton.arrSmartBuilds.size(); i++) {
                            arrBuild.add(SmartSingleton.arrSmartBuilds.get(i).strName);
                        }
                    }

                    MaterialDialog md = new MaterialDialog.Builder(SmartClientWriteActivity.this)
                            .title("현장선택")
                            .cancelable(false)
                            .items(arrBuild)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    SmartSingleton.smartClient.strCate1 = SmartSingleton.arrSmartBuilds.get(which).strCode;
                                    _buildName.setText(text);
                                    _buildName.clearFocus();
                                }
                            })
                            .positiveText("창닫기").show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {

            switch (requestCode) {

                case 22001: // 첨부파일에 사진 추가하기

                    if (data != null) {
                        final int intId = Integer.valueOf(data.getStringExtra("intId"));
                        final String strFileURL = data.getStringExtra("strFileURL");
                        //Log.d("aaaa", "strFileURL : " + data.getStringExtra("strFileURL"));
                        if (!strFileURL.isEmpty()) {
                            if(SmartSingleton.smartClient.arrFiles.size() == 0) SmartSingleton.smartClient.arrFiles.add(new SmartFile());
                            SmartSingleton.smartClient.arrFiles.get(0).intId = intId; // 첨부파일 이미지 코드값 저장 !!
                            Picasso.with(SmartClientWriteActivity.this)
                                    .load(strFileURL)
                                    .fit() // resize(700,400)
                                    .into(_photo);
                            //_imgCommentPhoto.getLayoutParams().height = 200;
                            //_imgCommentPhoto.requestLayout();
                            _photo.setVisibility(View.VISIBLE);
                            //ScrollView scrollView = (ScrollView) findViewById(R.id.sv_client_write);
                            //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            //Toast.makeText(SmartClientViewActivity.this, "strFileURL : " + strFileURL, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
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

            if(_buildName.getText().toString().equals("")) {
                new MaterialDialog.Builder(SmartClientWriteActivity.this)
                        .title("공사명 미등록")
                        .content("공사명을 먼저 등록해 주세요.")
                        .cancelable(false)
                        .positiveText("확인")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        })
                        .show();
                return false;
            } else if(_title.getText().toString().equals("")) {
                new MaterialDialog.Builder(SmartClientWriteActivity.this)
                        .title("제목 미등록")
                        .content("제목을 먼저 등록해 주세요.")
                        .cancelable(false)
                        .positiveText("확인")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        })
                        .show();
                return false;
            } else if(_content.getText().toString().equals("")) {
                new MaterialDialog.Builder(SmartClientWriteActivity.this)
                        .title("내용 미등록")
                        .content("내용을 먼저 등록해 주세요.")
                        .cancelable(false)
                        .positiveText("확인")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        })
                        .show();
                return false;
            } else {
                // 건축주 협의 게시판 수정하기
                SmartSingleton.smartClient.strTitle = _title.getText().toString();
                SmartSingleton.smartClient.strContent = Html.toHtml(_content.getText());

                md = new MaterialDialog.Builder(this)
                        .title("서버 전송중")
                        .content("서버 전송중 입니다..")
                        .cancelable(false)
                        .progress(true, 0)
                        .progressIndeterminateStyle(true)
                        .show();
                //Toast.makeText(getBaseContext(), "intId " + SmartSingleton.smartClient.intId, Toast.LENGTH_SHORT).show();
                registClient(SmartSingleton.smartClient.intId);
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    public void registClient(int intId) {
        /******************************************************************************************/
        SmartService service = ServiceGenerator.createService(SmartService.class);

        Map<String, String> mapFields = new HashMap<String, String>();
        mapFields.put("intId", String.valueOf(SmartSingleton.smartClient.intId));
        mapFields.put("strCate1", SmartSingleton.smartClient.strCate1);
        mapFields.put("strTitle", SmartSingleton.smartClient.strTitle);
        mapFields.put("strContent", SmartSingleton.smartClient.strContent);
        if(SmartSingleton.smartClient.arrFiles.size() > 0) {
            mapFields.put("strFileCode", String.valueOf(SmartSingleton.smartClient.arrFiles.get(0).intId));
        } else {
            mapFields.put("strFileCode", "");
        }

        if(intId > 0) { // 수정
            Call<ResponseBody> call = service.modifyClient(String.valueOf(SmartSingleton.smartClient.intId), mapFields);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        Log.v("registClient", "수정 / " + response.body().string());

                        md.dismiss();
                        new MaterialDialog.Builder(SmartClientWriteActivity.this)
                                .title("협의게시판 등록 완료")
                                .content("글이 정상적으로 등록 되었습니다.")
                                .positiveText("확인")
                                .onAny(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        SmartClientWriteActivity.this.setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                })
                                .show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Upload error:", t.getMessage());

                    md.dismiss();
                }
            });
        } else { // 신규 등록
            Call<ResponseBody> call = service.registClient(mapFields);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        Log.v("registClient", "신규등록 / " + response.body().string());

                        md.dismiss();
                        new MaterialDialog.Builder(SmartClientWriteActivity.this)
                                .title("협의게시판 등록 완료")
                                .content("글이 정상적으로 등록 되었습니다.")
                                .positiveText("확인")
                                .onAny(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        finish();
                                    }
                                })
                                .show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Upload error:", t.getMessage());

                    md.dismiss();
                }
            });
        }
        /******************************************************************************************/
    }

}
