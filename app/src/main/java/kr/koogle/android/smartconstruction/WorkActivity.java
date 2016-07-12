package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartBuild;
import kr.koogle.android.smartconstruction.http.SmartBuildService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.SmartWork;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkActivity extends AppCompatActivity {
    private static final String TAG = "WorkActivity";

    public static RecyclerView rvSmartWorks;
    private SmartWorkAdapter adapter;
    private LayoutInflater mInflater;

    private static String strBuildCode = "";
    private static Boolean isNewBuild;
    private static String strWorkTitleTop = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);
        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        RbPreference pref = new RbPreference(this);

        // Lookup the recyclerview in activity layout
        rvSmartWorks = (RecyclerView) findViewById(R.id.rvSmartWorks);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(WorkActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // Set layout manager to position the items
        rvSmartWorks.setLayoutManager(layoutManager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_work);
        setSupportActionBar(toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu_gallery);

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
            Picasso.with(this)
                    .load( getIntent().getExtras().getString("strImageUrl") )
                    .fit() // resize(700,400)
                    .into(imageTop);
        }

        // Create adapter passing in the sample user data
        adapter = new SmartWorkAdapter(this, SmartSingleton.arrSmartWorks);

        if(isNewBuild || SmartSingleton.arrSmartWorks.isEmpty()) {
            /******************************************************************************************/
            // SmartBuild 값 불러오기 (진행중인 현장)
            Log.d(TAG, "SmartBuildService.getSmartWorks 실행!! / pref_access_token : " + pref.getValue("pref_access_token", ""));
            SmartBuildService smartBuildService = ServiceGenerator.createService(SmartBuildService.class, pref.getValue("pref_access_token", ""));

            Log.d(TAG, "getSmartWork START !!!");
            Call<ArrayList<SmartWork>> call = smartBuildService.getSmartWorks(strBuildCode);
            Log.d(TAG, "getSmartWork END !!!");

            call.enqueue(new Callback<ArrayList<SmartWork>>() {
                @Override
                public void onResponse(Call<ArrayList<SmartWork>> call, Response<ArrayList<SmartWork>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final ArrayList<SmartWork> responseSmartWorks = response.body();

                        Log.d(TAG, "HashMap : size " + responseSmartWorks.size());
                        SmartSingleton.arrSmartWorks.addAll(responseSmartWorks);
                        // 최근 카운트 체크
                        int curSize = adapter.getItemCount();
                        adapter.notifyItemRangeInserted(curSize, responseSmartWorks.size());
                    } else {
                        Toast.makeText(getApplication(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "HashMap : 데이터가 정확하지 않습니다.");
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

        rvSmartWorks.setAdapter(adapter);
        rvSmartWorks.setItemAnimator(new SlideInUpAnimator());

        /***************************************************************************/
        adapter.setOnItemClickListener(new SmartWorkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                adapter.notifyItemChanged(position);
                // String name = SmartSingleton.arrSmartWorks.get(position).strName;
                // SmartSingleton.arrSmartWorks.get(position).strName = "변경되었습니다.";
                //Intent intentWorkView = new Intent(this, WorkViewActivity.class);
                //startActivity(intentWorkView);
                // Toast.makeText(getApplicationContext(), name + " was clicked!", Toast.LENGTH_SHORT).show();
            }
        });
        /***************************************************************************/
        adapter.setmOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Log.e("haint", "Load More");

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

    @Override
    protected void onResume() {
        super.onResume();

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
                    iv.animate().alpha(1f).setDuration(600);    // 1.0f means opaque
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