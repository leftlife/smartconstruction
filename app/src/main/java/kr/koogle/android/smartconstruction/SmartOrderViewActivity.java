package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.FileUploadService;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartOrder;
import kr.koogle.android.smartconstruction.http.SmartComment;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.SmartWork;
import kr.koogle.android.smartconstruction.util.HtmlRemoteImageGetterLee;
import kr.koogle.android.smartconstruction.util.RbPreference;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class SmartOrderViewActivity extends AppCompatActivity {
    private static final String TAG = "SmartOrderViewActivity";
    private RbPreference pref;

    @Bind(R.id.txt_order_view_writer) TextView _txtWriter;
    @Bind(R.id.txt_order_view_date) TextView _txtDate;
    @Bind(R.id.txt_order_view_content) HtmlTextView _txtContent;
    @Bind(R.id.ll_attach_file) LinearLayout _llAttachFile;
    @Bind(R.id.txt_attach_file) TextView _txtAttachFile;

    @Bind(R.id.btn_order_view_modify) Button _btnModify;
    @Bind(R.id.btn_order_view_delete) Button _btnDelete;
    @Bind(R.id.btn_order_view_top) Button _btnTop;
    @Bind(R.id.btn_order_view_regist_comment) Button _btnRegistComment;
    @Bind(R.id.img_order_view_camera) ImageView _btnAddPhoto;

    @Bind(R.id.input_order_view_comment) TextView _txtComment;
    @Bind(R.id.input_order_view_comment_photo) ImageView _imgCommentPhoto;
    private String commentPhotoCode = "";

    // recycleViewer
    private static RecyclerView recyclerView;
    private SmartOrderViewAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    // intent 로 넘어온 값 받기
    private Intent intent;
    private String orderCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_view);
        ButterKnife.bind(this);
        // intent 등록
        intent = getIntent();

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getApplicationContext());

        // 리스트 클릭시 넘어온값 받기 !!
        orderCode = String.valueOf(getIntent().getExtras().getInt("intId"));

        // RecyclerView 저장
        recyclerView = (RecyclerView) findViewById(R.id.rv_order_view_comments);
        // LayoutManager 저장
        layoutManager = new LinearLayoutManager(SmartOrderViewActivity.this);
        // RecycleView에 LayoutManager 세팅
        recyclerView.setLayoutManager(layoutManager);

        final LinearLayout empLayout = (LinearLayout) findViewById(R.id.emp_layout); // 내용없을때 보이는 레이아웃
        // 리스트 표현하기 !!
        if (SmartSingleton.smartOrder.arrComments.isEmpty()) {
            //empLayout.setVisibility(View.VISIBLE);
        } else {
            //empLayout.setVisibility(View.GONE);
            //recyclerView.setItemAnimator(new SlideInUpAnimator());
        }

        // arrComments 초기화
        SmartSingleton.arrComments.clear();

        // Adapter 생성
        adapter = new SmartOrderViewAdapter(this, SmartSingleton.arrComments);
        //if(SmartSingleton.arrComments.isEmpty() || true) {
        addRows();
        //}
        // RecycleView 에 Adapter 세팅
        recyclerView.setAdapter(adapter);
        // 리스트 표현하기 !!
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        recyclerView.setItemAnimator(itemAnimator);
        /*
        notifyItemChanged(int)
        notifyItemInserted(int)
        notifyItemRemoved(int)
        notifyItemRangeChanged(int, int)
        notifyItemRangeInserted(int, int)
        notifyItemRangeRemoved(int, int)
         */

        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_order_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ico_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmartOrderViewActivity.this.finish();
            }
        });

        // 건축주 협의 등록버튼
        _btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SmartOrderViewActivity.this, SmartOrderWriteActivity.class);
                intent.putExtra("intId", SmartSingleton.smartOrder.intId);
                startActivityForResult(intent, 1001);
            }
        });

        // 건축주 협의 삭제버튼
        _btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(SmartOrderViewActivity.this)
                        .title("게시글 삭제")
                        .content("글이 삭제되면 복구할 수 없습니다. 정말로 삭제하시겠습니까?")
                        .positiveText("확인")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                deleteOrder(SmartSingleton.smartOrder.intId);
                            }
                        })
                        .show();
            }
        });

        // Top 버튼 클릭시 상단 이동
        _btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScrollView scrollView = (ScrollView) findViewById(R.id.sv_order_view);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });

        // 이미지 추가 버튼 클릭시 이미리 리스트 페이지 이동
        _btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SmartOrderViewActivity.this, CameraPicListActivity.class);
                intent.putExtra("intId", SmartSingleton.smartOrder.intId);
                startActivityForResult(intent, 2001);
                //Toast.makeText(SmartOrderViewActivity.this, "intId : " + smartOrder.intId, Toast.LENGTH_SHORT).show();
            }
        });

        // 코멘트 등록 버튼
        _btnRegistComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( _txtComment.getText().toString().isEmpty() ) {

                    new MaterialDialog.Builder(SmartOrderViewActivity.this)
                            .title("답변등록확인")
                            .content("답변을 정확하게 입력하세요.")
                            .positiveText("확인")
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            })
                            .show();

                } else {
                    registComment();
                }
            }
        });

        /***************************************************************************/
        // 리스트 클릭시 상세 페이지 보기
        adapter.setOnItemClickListener(new SmartOrderViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                adapter.notifyItemChanged(position);

                Intent intext = new Intent(SmartOrderViewActivity.this, SmartOrderViewActivity.class);
                final int intId = SmartSingleton.arrComments.get(position).intId;
                intext.putExtra("intId", intId);
                //startActivityForResult(intext, 1002);
                //Toast.makeText(SmartOrderViewActivity.this, "intId : " + intId, Toast.LENGTH_SHORT).show();
            }
        });

        // 리스트 X버튼 클릭시 해당 글 삭제
        adapter.setOnItemXClickListener(new SmartOrderViewAdapter.OnItemXClickListener() {
            @Override
            public void onItemXClick(View itemView, int position) {

                final String commentCode = String.valueOf(SmartSingleton.arrComments.get(position).intId);
                SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
                Call<ResponseBody> call = smartService.deleteComment(commentCode);
                Toast.makeText(SmartOrderViewActivity.this, "commentCode : " + commentCode, Toast.LENGTH_SHORT).show();

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            //smartOrder = response.body();
                            Log.d(TAG, response.body().toString());
                            //Log.d(TAG, "title : " + smartOrder.arrComments.get(0).strContent.toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(SmartOrderViewActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                        Log.d("Error", t.getMessage());
                    }
                });

                adapter.remove(position);
                //Toast.makeText(SmartOrderViewActivity.this, "position : " + position, Toast.LENGTH_SHORT).show();
            }
        });
        /***************************************************************************/

    }

    private void deleteOrder(int intId) {
        /******************************************************************************************/
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        Call<ResponseBody> call = smartService.deleteOrder(String.valueOf(intId));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SmartOrderViewActivity.this.setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(SmartOrderViewActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());
            }
        });
        /******************************************************************************************/
    }

    private void addRows() {
        /******************************************************************************************/
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        Call<SmartOrder> call = smartService.getSmartBBSOrder(orderCode);

        call.enqueue(new Callback<SmartOrder>() {
            @Override
            public void onResponse(Call<SmartOrder> call, Response<SmartOrder> response) {
                Log.d("aaaa", "success : " + response.body().toString());
                if (response.isSuccessful() && response.body() != null) {
                    SmartSingleton.smartOrder = response.body();

                    if (SmartSingleton.smartOrder.intId != 0) {
                        _txtWriter.setText(SmartSingleton.smartOrder.strUserId);
                        _txtDate.setText(SmartSingleton.smartOrder.datWrite);
                        /*
                        HtmlRemoteImageGetter imgGetter = new HtmlRemoteImageGetter(_txtContent, smartOrder.strContent);
                        _txtContent.setText(Html.fromHtml(smartOrder.strContent, imgGetter, null));
                        */
                        _txtContent.setHtml(SmartSingleton.smartOrder.strContent, new HtmlRemoteImageGetterLee(_txtContent, null, true, _txtContent.getWidth()));
                        //Toast.makeText(SmartOrderViewActivity.this, response.body().toString(), Toast.LENGTH_SHORT).show();

                        if(SmartSingleton.smartOrder.arrFiles.size() > 0) {
                            _llAttachFile.setVisibility(View.VISIBLE);
                            _txtAttachFile.setText(SmartSingleton.smartOrder.arrFiles.get(0).strNameOrigin);
                            _txtAttachFile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Uri uri = Uri.parse(SmartSingleton.smartOrder.arrFiles.get(0).strURL + SmartSingleton.smartOrder.arrFiles.get(0).strName);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                }
                            });
                        }

                        SmartSingleton.arrComments.addAll(SmartSingleton.smartOrder.arrComments);
                        adapter.notifyDataSetChanged();
                        // comment 리스트 출력 !!
                        //int curSize = adapter.getItemCount();
                        //adapter.notifyItemRangeInserted(curSize, smartOrder.arrComments.size());
                        Log.d(TAG, "curSize : " + adapter.getItemCount() + " / arrComments.size : " + SmartSingleton.smartOrder.arrComments.size());

                    } else {
                        Toast.makeText(getApplication(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "smartOrder : 데이터가 정확하지 않습니다.");
                    }
                }
            }

            @Override
            public void onFailure(Call<SmartOrder> call, Throwable t) {
                Toast.makeText(SmartOrderViewActivity.this, "네트워크 상태가 좋지 않습니다!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());
            }
        });
        /******************************************************************************************/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {

            case 1001: // 내용 수정 페이지에서 온 경우 내용 새로 고침

                _txtWriter.setText(SmartSingleton.smartOrder.strUserId);
                _txtDate.setText(SmartSingleton.smartOrder.datWrite);
                _txtContent.setHtml(SmartSingleton.smartOrder.strContent, new HtmlRemoteImageGetterLee(_txtContent, null, true, _txtContent.getWidth()));

                ScrollView scrollView = (ScrollView) findViewById(R.id.sv_order_view);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
                break;

            case 2001: // 댓글에서 사진 추가하기

                if( data != null) {
                    final String intId = data.getStringExtra("intId");
                    final String strFileURL = data.getStringExtra("strFileURL");
                    if (!strFileURL.isEmpty()) {
                        commentPhotoCode = intId; // 답글 이미지 코드값 저장 !!
                        Picasso.with(SmartOrderViewActivity.this)
                                .load(strFileURL)
                                .fit() // resize(700,400)
                                .into(_imgCommentPhoto);
                        //_imgCommentPhoto.getLayoutParams().height = 200;
                        //_imgCommentPhoto.requestLayout();
                        _imgCommentPhoto.setVisibility(View.VISIBLE);
                        scrollView = (ScrollView) findViewById(R.id.sv_order_view);
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        //Toast.makeText(SmartOrderViewActivity.this, "strFileURL : " + strFileURL, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    public void registComment() {
        /******************************************************************************************/
        SmartService service = ServiceGenerator.createService(SmartService.class);
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // File file = fileImage; //FileUtils.getFile(this, fileUri); // Uri 값을 받을 경우 !!

        final Map<String, String> mapFields = new HashMap<String, String>();
        final String content = _txtComment.getText().toString();
        mapFields.put("strBBS", "order");
        mapFields.put("intBBSId", orderCode);
        mapFields.put("strContent", content);
        mapFields.put("strFileCode", commentPhotoCode);
        Call<ResponseBody> call = service.registComment(mapFields);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    //Log.v("Upload", "success / " + response.body().string());

                    final SmartComment sc = new SmartComment();
                    Log.d(TAG, "_txtComment 완료전 : " + _txtComment.getText().toString());
                    sc.intId = Integer.parseInt(orderCode);
                    sc.strContent = _txtComment.getText().toString();
                    sc.strWriter = pref.getValue("pref_user_id", "");
                    sc.strName = pref.getValue("pref_user_name", "");
                    sc.datWrite = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    sc.strFileURL = response.body().string();
                    SmartSingleton.arrComments.add(sc);

                    int curSize = adapter.getItemCount();
                    //Log.d(TAG, "전 : curSize : " + adapter.getItemCount() + " / arrComments.size : " + smartOrder.arrComments.size());
                    adapter.notifyItemRangeInserted(curSize, 1);
                    //Log.d(TAG, "후 : curSize : " + adapter.getItemCount() + " / arrComments.size : " + smartOrder.arrComments.size());
                    /*
                    SmartOrderViewActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    */

                    _txtComment.setText("");
                    _imgCommentPhoto.setVisibility(View.GONE);
                    ScrollView scrollView = (ScrollView) findViewById(R.id.sv_order_view);
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    Log.d(TAG, "_txtComment 완료후 : " + _txtComment.getText().toString());

                    new MaterialDialog.Builder(SmartOrderViewActivity.this)
                            .title("답변등록 완료")
                            .content("답변이 정상적으로 등록 되었습니다.")
                            .positiveText("확인")
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
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
        /******************************************************************************************/
    }

    @UiThread
    protected void dataSetChanged() {
        adapter.notifyDataSetChanged();
    }
}
