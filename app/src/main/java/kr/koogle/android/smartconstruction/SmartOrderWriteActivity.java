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
import kr.koogle.android.smartconstruction.http.SmartEmployee;
import kr.koogle.android.smartconstruction.http.SmartOrder;
import kr.koogle.android.smartconstruction.http.SmartComment;
import kr.koogle.android.smartconstruction.http.SmartFile;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.RbPreference;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartOrderWriteActivity extends AppCompatActivity {
    private static final String TAG = "SmartOrderWriteActivity";
    private RbPreference pref;

    //private SmartOrder smartOrder;

    @Bind(R.id.input_build_name) EditText _buildName;
    @Bind(R.id.input_who) EditText _who;
    @Bind(R.id.input_content) EditText _content;
    @Bind(R.id.btn_add_photo) ImageView _addPhoto;
    @Bind(R.id.img_photo) ImageView _photo;

    // intent 로 넘어온 값 받기
    private Intent intent;

    // 담당자 배열값
    private Integer[] arrWho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_write);
        ButterKnife.bind(this);
        // intent 등록
        intent = getIntent();

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getApplicationContext());

        //smartOrder = new SmartOrder();
        arrWho = new Integer[]{};

        // 리스트 클릭시 넘어온값 받기 !!
        SmartSingleton.smartOrder.intId = getIntent().getExtras().getInt("intId");
        //Toast.makeText(SmartOrderWriteActivity.this, "intId : " + intId, Toast.LENGTH_SHORT).show();
        if( SmartSingleton.smartOrder.intId > 0 ) {

            final String strBuildCode = SmartSingleton.smartOrder.strBuildCode;
            String strBuildName = "";
            for (SmartBuild sb : SmartSingleton.arrSmartBuilds) {
                if( strBuildCode.equals(sb.strCode) ) {
                    strBuildName = sb.strName;
                }
            }
            _buildName.setText(strBuildName);

            String strWhoCode = "";
            String strWho = "";
            for (String whoCode : SmartSingleton.smartOrder.arrWhoCodes) {
                int i = 0;
                for (SmartEmployee se : SmartSingleton.arrSmartEmployees) {
                    if (se.strCode.equals(whoCode)) {
                        if(i > 0) strWho += ",";
                        strWho += se.strName;
                        i++;
                    }
                }
                strWhoCode += "|" + whoCode;
            }
            SmartSingleton.smartOrder.strWhoCode = strWhoCode;
            SmartSingleton.smartOrder.strWho = strWho;
            _who.setText(strWho);

            Spanned str = Html.fromHtml(SmartSingleton.smartOrder.strContent);
            // String str2 = Html.toHtml(str);
            _content.setText(str);

            if(SmartSingleton.smartOrder.arrFiles.size() > 0) {
                _photo.setVisibility(View.VISIBLE);
                Picasso.with(SmartOrderWriteActivity.this)
                        .load(SmartSingleton.smartOrder.arrFiles.get(0).strURL + SmartSingleton.smartOrder.arrFiles.get(0).strName)
                        .fit() // resize(700,400)
                        .into(_photo);
                _photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse(SmartSingleton.smartOrder.arrFiles.get(0).strURL + SmartSingleton.smartOrder.arrFiles.get(0).strName);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });
            }
        }

        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_order_write);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ico_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmartOrderWriteActivity.this.finish();
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

                    MaterialDialog md = new MaterialDialog.Builder(SmartOrderWriteActivity.this)
                            .title("현장선택")
                            .items(arrBuild)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    SmartSingleton.smartOrder.strBuildCode = SmartSingleton.arrSmartBuilds.get(which).strCode;
                                    _buildName.setText(text);
                                    _buildName.clearFocus();
                                }
                            })
                            .positiveText("창닫기").show();
                }
            }
        });

        _who.setInputType(0);
        _who.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    final ArrayList<String> arrEmployee = new ArrayList<String>();
                    if (!SmartSingleton.arrSmartEmployees.isEmpty()) {
                        //ArrayList<SmartBuild> arrBuild = SmartSingleton.arrSmartBuilds;
                        for (int i = 0; i < SmartSingleton.arrSmartEmployees.size(); i++) {
                            arrEmployee.add(SmartSingleton.arrSmartEmployees.get(i).strName);
                        }
                    }

                    new MaterialDialog.Builder(SmartOrderWriteActivity.this)
                            .title("담당직원 선택")
                            .items(arrEmployee)
                            .itemsCallbackMultiChoice(arrWho, new MaterialDialog.ListCallbackMultiChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                    StringBuilder str = new StringBuilder();
                                    SmartSingleton.smartOrder.arrWhoCodes.clear(); // arrWhoCodes 값 초기화
                                    for (int i = 0; i < which.length; i++) {
                                        if (i > 0) {
                                            str.append(',');
                                            str.append('\n');
                                        }
                                        str.append(text[i]);
                                        SmartSingleton.smartOrder.arrWhoCodes.add(SmartSingleton.arrSmartEmployees.get(i).strCode); // arrWhoCodes 에 값 추가
                                    }
                                    arrWho = which; // 선택된 which 배열값
                                    _who.setText(str);
                                    _who.clearFocus();
                                    return true; // allow selection
                                }
                            })
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.clearSelectedIndices();
                                }
                            })
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    dialog.dismiss();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                }

                                @Override
                                public void onNeutral(MaterialDialog dialog) {
                                    super.onNeutral(dialog);
                                }
                            })
                            .alwaysCallMultiChoiceCallback()
                            .positiveText("선택완료")
                            .autoDismiss(false)
                            .neutralText("초기화")
                            //.itemsDisabledIndices(0, 1)
                            .show();
                }
            }
        });

        // 이미지 추가 버튼 클릭시 이미리 리스트 페이지 이동
        _addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SmartOrderWriteActivity.this, CameraPicListActivity.class);
                //intent.putExtra("intId", SmartSingleton.smartOrder.intId);
                startActivityForResult(intent, 1001);
                //Toast.makeText(SmartOrderViewActivity.this, "intId : " + smartOrder.intId, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {

            switch (requestCode) {

                case 1001: // 첨부파일에 사진 추가하기

                    if (data != null) {
                        final int intId = Integer.valueOf(data.getStringExtra("intId"));
                        final String strFileURL = data.getStringExtra("strFileURL");
                        //Log.d("aaaa", "strFileURL : " + data.getStringExtra("strFileURL"));
                        if (!strFileURL.isEmpty()) {
                            if(SmartSingleton.smartOrder.arrFiles.size() == 0) SmartSingleton.smartOrder.arrFiles.add(new SmartFile());
                            SmartSingleton.smartOrder.arrFiles.get(0).intId = intId; // 첨부파일 이미지 코드값 저장 !!
                            Picasso.with(SmartOrderWriteActivity.this)
                                    .load(strFileURL)
                                    .fit() // resize(700,400)
                                    .into(_photo);
                            //_imgCommentPhoto.getLayoutParams().height = 200;
                            //_imgCommentPhoto.requestLayout();
                            _photo.setVisibility(View.VISIBLE);
                            //ScrollView scrollView = (ScrollView) findViewById(R.id.sv_order_write);
                            //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            //Toast.makeText(SmartOrderViewActivity.this, "strFileURL : " + strFileURL, Toast.LENGTH_SHORT).show();
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
                new MaterialDialog.Builder(SmartOrderWriteActivity.this)
                    .title("공사명 미등록")
                    .content("공사명을 먼저 등록해 주세요.")
                    .positiveText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        }
                    })
                    .show();
                return false;
            } else if(_content.getText().toString().equals("")) {
                new MaterialDialog.Builder(SmartOrderWriteActivity.this)
                    .title("내용 미등록")
                    .content("내용을 먼저 등록해 주세요.")
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
                SmartSingleton.smartOrder.strContent = Html.toHtml(_content.getText());

                Log.d("aaaa", "intId : " + SmartSingleton.smartOrder.intId);
                //Toast.makeText(getBaseContext(), "intId " + SmartSingleton.smartOrder.intId, Toast.LENGTH_SHORT).show();
                registOrder(SmartSingleton.smartOrder.intId);
                return true;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    public void registOrder(int intId) {

        String strWhoCode = "";
        String strWho = "";
        for (String whoCode : SmartSingleton.smartOrder.arrWhoCodes) {
            int i = 0;
            for (SmartEmployee se : SmartSingleton.arrSmartEmployees) {
                if (se.strCode.equals(whoCode)) {
                    if(i > 0) strWho += ",";
                    strWho += se.strName;
                    i++;
                }
            }
            strWhoCode += "|" + whoCode;
        }
        SmartSingleton.smartOrder.strWhoCode = strWhoCode;
        SmartSingleton.smartOrder.strWho = strWho;
        /******************************************************************************************/
        SmartService service = ServiceGenerator.createService(SmartService.class);

        Map<String, String> mapFields = new HashMap<String, String>();
        mapFields.put("intId", String.valueOf(SmartSingleton.smartOrder.intId));
        mapFields.put("strBuildCode", SmartSingleton.smartOrder.strBuildCode);
        mapFields.put("strWho", SmartSingleton.smartOrder.strWho);
        mapFields.put("strWhoCode", SmartSingleton.smartOrder.strWhoCode);
        mapFields.put("strContent", SmartSingleton.smartOrder.strContent);
        if(SmartSingleton.smartOrder.arrFiles.size() > 0) {
            mapFields.put("strFileCode", String.valueOf(SmartSingleton.smartOrder.arrFiles.get(0).intId));
        } else {
            mapFields.put("strFileCode", "");
        }

        if(intId > 0) { // 수정
            Call<ResponseBody> call = service.modifyOrder(String.valueOf(SmartSingleton.smartOrder.intId), mapFields);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        Log.v("registOrder", "수정 / " + response.body().string());

                        new MaterialDialog.Builder(SmartOrderWriteActivity.this)
                                .title("협의게시판 등록 완료")
                                .content("글이 정상적으로 등록 되었습니다.")
                                .positiveText("확인")
                                .onAny(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        SmartOrderWriteActivity.this.setResult(RESULT_OK, intent);
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
                }
            });
        } else { // 신규 등록
            Call<ResponseBody> call = service.registOrder(mapFields);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        Log.v("registOrder", "신규등록 / " + response.body().string());

                        new MaterialDialog.Builder(SmartOrderWriteActivity.this)
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
                }
            });
        }
        /******************************************************************************************/
    }

}
