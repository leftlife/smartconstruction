package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.SmartWork;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartWorkActivity extends AppCompatActivity {
    private static final String TAG = "SmartWork";
    private RbPreference pref;

    public static RecyclerView recyclerView;
    private SmartWorkAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private static String strBuildCode = "start";
    private static Boolean isNewBuild;
    private static String strWorkTitleTop = "";
    private static ImageView imageTop;
    private static String imageTopUrl = "";

    private static boolean isLoading;
    private static boolean isTop;
    private static int visibleThreshold = 10;

    // Pull to Refresh 4-1
    private SwipeRefreshLayout swipeContainer;

    private ProgressWheel wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(this);

        wheel = (ProgressWheel) findViewById(R.id.progress_wheel);

        // RecyclerView 저장
        recyclerView = (RecyclerView) findViewById(R.id.rv_smart_work);
        // LayoutManager 저장
        layoutManager = new LinearLayoutManager(SmartWorkActivity.this);
        // RecycleView에 LayoutManager 세팅
        recyclerView.setLayoutManager(layoutManager);

        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_work);
        setSupportActionBar(toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu_gallery);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 스크롤 이벤트 잡아내기 !!
        final NestedScrollView parentScrollView=(NestedScrollView)findViewById (R.id.nsv_smart_work);
        parentScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                final int totalItemCount = layoutManager.getItemCount();
                final int layoutManagerH = layoutManager.getHeight();
                final int parentH = parentScrollView.getHeight();
                //final int itemH = layoutManager.getChildAt(0).getHeight();

                if ( layoutManagerH - parentH - scrollY < 10 ) {
                    isLoading = true;
                    Log.d(TAG, "현장 담당자만 등록이 가능합니다.");
                    /******************************************************************************************/
                    addRows();
                    /******************************************************************************************/
                }
                Log.d(TAG, "scrollY : " + scrollY + " / height : " + layoutManagerH + " / heightRV : " + parentH);
            }
        });

        // 스마트일보 등록 버튼
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_work);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 쓰기 권한 체크
                if( !pref.getValue("pref_user_type","").equals("employee") ) {
                    new MaterialDialog.Builder(SmartWorkActivity.this).content("현장소장만 등록이 가능합니다.").positiveText("확인")
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                    return;
                }

                //Snackbar.make(view, "스마트 일보를 등록합니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent intent = new Intent(SmartWorkActivity.this, SmartWorkViewActivity.class);
                intent.putExtra("strCode", "");
                //SmartSingleton.smartClient = new SmartClient();
                startActivityForResult(intent, 1001);
            }
        });
        if( !pref.getValue("pref_user_type","").equals("employee") ) {
            fab.setVisibility(View.GONE);
        }

        loadBackdrop();

        // 현장 선택시 코드/이름값 저장!!
        SmartSingleton.smartBuild.strCode = getIntent().getExtras().getString("strBuildCode");
        SmartSingleton.smartBuild.strName = getIntent().getExtras().getString("strBuildName");

        // SmartBuildFragment 에서 넘어온 값 받기 !!
        if ( strBuildCode.equals(getIntent().getExtras().getString("strBuildCode")) ) { // 새로운 현장이 아니면
            isNewBuild = false;
        } else { // 새로운 현장이면
            SmartSingleton.arrSmartWorks = new ArrayList<SmartWork>();
            isNewBuild = true;

            // 상단부분 내용 변경 !!
            strBuildCode = getIntent().getExtras().getString("strBuildCode");
            strWorkTitleTop = getIntent().getExtras().getString("strBuildName");

            final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout_work);
            collapsingToolbar.setTitle(strWorkTitleTop);
        }
        imageTop = (ImageView) findViewById(R.id.img_work_top);

        /******************************************************************************************/
        // Adapter 생성
        adapter = new SmartWorkAdapter(this, SmartSingleton.arrSmartWorks);
        if(isNewBuild || SmartSingleton.arrSmartWorks.isEmpty()) {

            wheel.setVisibility(View.VISIBLE);
            wheel.setBarColor(R.color.colorPrimary);
            wheel.spin();

            addRows();
        }
        // RecycleView 에 Adapter 세팅
        recyclerView.setAdapter(adapter);
        // 리스트 표현하기 !!
        recyclerView.setItemAnimator(new SlideInUpAnimator());

        /***************************************************************************/
        adapter.setOnItemClickListener(new SmartWorkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final String strBuildCode = SmartSingleton.arrSmartWorks.get(position).strBuildCode;
                final String strCode = SmartSingleton.arrSmartWorks.get(position).strCode;
                adapter.notifyItemChanged(position);

                Intent intentWorkView = new Intent(SmartWorkActivity.this, SmartWorkViewActivity.class);
                intentWorkView.putExtra("strBuildCode", strBuildCode);
                intentWorkView.putExtra("strCode", strCode);
                startActivityForResult(intentWorkView, 1002);
            }
        });
        /***************************************************************************/

        // Pull to Refresh 4-2
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.srl_smart_work);
        /*
        swipeContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "onTouch : " + motionEvent.toString());
                return false;
            }
        });
        */
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright);
    }

    // Pull to Refresh 4-3
    public void fetchTimelineAsync(int page) {
        SmartSingleton.arrSmartWorks.clear();
        adapter.clear();
        addRows();
    }

    public void setLoaded() {
        isLoading = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        imageTopUrl = getIntent().getExtras().getString("strImageUrl");
        if( !imageTopUrl.isEmpty() ) {
            new DownloadImageTask(imageTop).execute(imageTopUrl);
        }
    }
    // AsyncTask 탑 이미지 로딩용
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon = null;
            try {
                //Bitmap image = Picasso.with(this).load(imageTopUrl).get();
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private void addRows() {
        /******************************************************************************************/
        // SmartBuild 값 불러오기 (진행중인 현장)
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        final Map<String, String> mapOptions = new HashMap<String, String>();
        mapOptions.put("offset", String.valueOf(layoutManager.getItemCount()));

        Call<ArrayList<SmartWork>> call = smartService.getSmartWorks(strBuildCode, mapOptions);
        call.enqueue(new Callback<ArrayList<SmartWork>>() {
            @Override
            public void onResponse(Call<ArrayList<SmartWork>> call, Response<ArrayList<SmartWork>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final ArrayList<SmartWork> responses = response.body();

                    if(responses.size() != 0) {
                        Log.d(TAG, "responses : size " + responses.size());
                        Log.d(TAG,"arrSmartLabors size : " + responses.get(0).arrSmartLabors.size());
                        SmartSingleton.arrSmartWorks.addAll(responses);
                        // 최근 카운트 체크
                        int curSize = adapter.getItemCount();
                        //adapter.notifyItemRangeInserted(curSize, responses.size());
                        adapter.notifyDataSetChanged();
                    } else {
                        //Snackbar.make(SmartWorkActivity.recyclerView, "마지막 리스트 입니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } else {
                    Toast.makeText(getApplication(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "responses : 데이터가 정확하지 않습니다.");
                }

                // Pull to Refresh 4-4
                swipeContainer.setRefreshing(false);
                wheel.stopSpinning();
                wheel.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ArrayList<SmartWork>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "네트워크 상태가 좋지 않습니다!!!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());

                // Pull to Refresh 4-4
                swipeContainer.setRefreshing(false);
                wheel.stopSpinning();
                wheel.setVisibility(View.GONE);
            }
        });
        /******************************************************************************************/
    }

    // 스크롤 시 상단 이미지 투명하게 변경 !!
    private void loadBackdrop() {
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout_work);
        collapsingToolbar.setTitle(strWorkTitleTop);

        final int myDrawable = R.drawable.img_no_image; // 상단 초기 이미지 설정!!
        final ImageView iv = (ImageView)findViewById(R.id.img_work_top);
        if (iv != null) {
            iv.setImageResource(myDrawable);
        }

        AppBarLayout.OnOffsetChangedListener listener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if(collapsingToolbar.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsingToolbar)) {
                    // collapsed
                    iv.animate().alpha(0.3f).setDuration(600);
                } else {
                    // extended
                    iv.animate().alpha(1f).setDuration(600); // 1.0f means opaque
                }
                Log.d(TAG, "height : " + collapsingToolbar.getHeight() + " / vertical : " + verticalOffset + " / getMinimumHeight : " + ViewCompat.getMinimumHeight(collapsingToolbar));
                isTop = false;
            }
        };

        final AppBarLayout appbar = (AppBarLayout) findViewById(R.id.app_bar_work);
        appbar.addOnOffsetChangedListener(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_work, menu);
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {

            case 1002: // 리스트 다시 읽기
                SmartSingleton.arrSmartWorks.clear();
                addRows();
                break;
        }
    }
}