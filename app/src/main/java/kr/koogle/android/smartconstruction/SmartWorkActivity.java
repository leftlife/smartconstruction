package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
    private static final String TAG = "SmartWorkActivity";
    private RbPreference pref;

    public static RecyclerView rvSmartWorks;
    private SmartWorkAdapter adapter;

    private static String strBuildCode = "start";
    private static Boolean isNewBuild;
    private static String strWorkTitleTop = "";

    private static boolean isLoading;
    private static int visibleThreshold = 10;

    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);
        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(this);

        // RecyclerView 저장
        rvSmartWorks = (RecyclerView) findViewById(R.id.rvSmartWorks);
        // LayoutManager 저장
        layoutManager = new LinearLayoutManager(SmartWorkActivity.this);
        // RecycleView에 LayoutManager 세팅
        rvSmartWorks.setLayoutManager(layoutManager);

        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_work);
        setSupportActionBar(toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu_gallery);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 스크롤 이벤트 잡아내기 !!
        final NestedScrollView parentScrollView=(NestedScrollView)findViewById (R.id.nested_scroll_view_work);
        parentScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                final int totalItemCount = layoutManager.getItemCount();
                final int layoutManagerH = layoutManager.getHeight();
                final int parentH = parentScrollView.getHeight();
                final int itemH = layoutManager.getChildAt(0).getHeight();

                if ( layoutManagerH - parentH - scrollY < 10 ) {
                    isLoading = true;
                    Log.d(TAG, "현장 담당자만 등록이 가능합니다.");
                    /******************************************************************************************/
                    addItems();
                    /******************************************************************************************/
                }
                Log.d(TAG, "scrollY : " + scrollY + " / height : " + layoutManagerH + " / heightRV : " + parentH + " / viewHeight : " + itemH);
            }
        });

        // 스마트일보 등록 버튼
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_work);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "스마트 일보를 등록합니다.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        loadBackdrop();

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

            final ImageView imageTop = (ImageView) findViewById(R.id.img_work_top);

            if( !getIntent().getExtras().getString("strImageUrl").isEmpty() ) {
                Picasso.with(this)
                        .load(getIntent().getExtras().getString("strImageUrl"))
                        .fit() // resize(700,400)
                        .into(imageTop);
            }
        }

        // Adapter 생성
        adapter = new SmartWorkAdapter(this, SmartSingleton.arrSmartWorks);

        if(isNewBuild || SmartSingleton.arrSmartWorks.isEmpty()) {
            /******************************************************************************************/
            addItems();
            /******************************************************************************************/
        }

        // RecycleView 에 Adapter 세팅
        rvSmartWorks.setAdapter(adapter);
        // 리스트 표현하기 !!
        rvSmartWorks.setItemAnimator(new SlideInUpAnimator());

        /***************************************************************************/
        adapter.setOnItemClickListener(new SmartWorkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final String strCode = SmartSingleton.arrSmartWorks.get(position).strCode;
                final String strDate = SmartSingleton.arrSmartWorks.get(position).strDate;
                final String strImageUrl = SmartSingleton.arrSmartWorks.get(position).strImageURL;
                adapter.notifyItemChanged(position);

                Intent intentWorkView = new Intent(SmartWorkActivity.this, SmartWorkViewActivity.class);
                intentWorkView.putExtra("strBuildCode", strCode);
                intentWorkView.putExtra("strBuildDate", strDate);
                intentWorkView.putExtra("strImageUrl", strImageUrl);
                startActivityForResult(intentWorkView, 1002);
                Toast.makeText(getApplicationContext(), "strBuildCode : " + strCode, Toast.LENGTH_SHORT).show();
            }
        });
        /***************************************************************************/
        adapter.setmOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Log.e("haint", "Load More -------------------------------------------------------------------");

                /*
                mUserAdapter.notifyItemInserted(mUsers.size() - 1);

                //Remove loading item
                mUsers.remove(mUsers.size() - 1);
                mUserAdapter.notifyItemRemoved(mUsers.size());

                //Load data
                int index = mUsers.size();
                int end = index + 20;
                for (int i = index; i < end; i++) {
                    User user = new User();
                    user.setName("Name " + i);
                    user.setEmail("alibaba" + i + "@gmail.com");
                    mUsers.add(user);
                }
                mUserAdapter.notifyDataSetChanged();
                mUserAdapter.setLoaded();
                */
            }
        });
        /***************************************************************************/
    }

    public void setLoaded() {
        isLoading = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void addItems() {
        /******************************************************************************************/
        // SmartBuild 값 불러오기 (진행중인 현장)
        Log.d(TAG, "SmartService.getSmartWorks 실행!! / pref_access_token : " + pref.getValue("pref_access_token", ""));
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        final Map<String, String> mapOptions = new HashMap<String, String>();
        mapOptions.put("offset", String.valueOf(layoutManager.getItemCount()));

        Log.d(TAG, "getSmartWork START !!!");
        Call<ArrayList<SmartWork>> call = smartService.getSmartWorks(strBuildCode, mapOptions);
        Log.d(TAG, "getSmartWork END !!!");

        call.enqueue(new Callback<ArrayList<SmartWork>>() {
            @Override
            public void onResponse(Call<ArrayList<SmartWork>> call, Response<ArrayList<SmartWork>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final ArrayList<SmartWork> responseSmartWorks = response.body();

                    if(responseSmartWorks.size() != 0) {
                        Log.d(TAG, "responseSmartWorks : size " + responseSmartWorks.size());
                        SmartSingleton.arrSmartWorks.addAll(responseSmartWorks);
                        // 최근 카운트 체크
                        int curSize = adapter.getItemCount();
                        adapter.notifyItemRangeInserted(curSize, responseSmartWorks.size());
                    } else {
                        Snackbar.make(SmartWorkActivity.rvSmartWorks, "마지막 리스트 입니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } else {
                    Toast.makeText(getApplication(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "responseSmartWorks : 데이터가 정확하지 않습니다.");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<SmartWork>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "네트워크 상태가 좋지 않습니다!!!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());
            }
        });
        /******************************************************************************************/
    }

    // 스크롤 시 상단 이미지 투명하게 변경 !!
    private void loadBackdrop() {
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout_work);
        collapsingToolbar.setTitle(strWorkTitleTop);

        final int myDrawable = R.drawable.img_intro;
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
}