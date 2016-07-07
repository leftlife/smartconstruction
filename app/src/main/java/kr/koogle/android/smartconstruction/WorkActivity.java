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

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartBuild;
import kr.koogle.android.smartconstruction.http.SmartBuildService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.SmartWork;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkActivity extends AppCompatActivity {

    private static final String TAG = "WorkActivity";
    private SmartWorkAdapter adapter;
    // public static HashMap<String, SmartWork> smartWorks; // SmartSingleton.smartBuilds -> smartBuilds 변경 사용할 경우 !!
    private static RecyclerView rvSmartWorks;
    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();

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


        // Settings 값 !!
        RbPreference pref = new RbPreference(this);

        // Initialize smartBulids - do not reinitialize an existing reference used by an adapter
        // smartBulids = SmartBuild.createSmartBuildsList(20);
        // SmartSingleton.getInstance();
        // SmartSingleton.arrSmartBuilds = new ArrayList<SmartBuild>();

        // Create adapter passing in the sample user data
        adapter = new SmartWorkAdapter(this, SmartSingleton.arrSmartWorks);

        if(SmartSingleton.arrSmartWorks.size() <= 0) {
            /******************************************************************************************/
            // SmartBuild 값 불러오기 (진행중인 현장)
            Log.d(TAG, "SmartBuildService.getSmartWorks 실행!! / accessToken : " + pref.getValue("accessToken", ""));
            SmartBuildService smartBuildService = ServiceGenerator.createService(SmartBuildService.class, pref.getValue("accessToken", ""));
            String buildCode = "22561cb4f743881";
            Log.d(TAG, "getSmartWork START !!!");
            Call<ArrayList<SmartWork>> call = smartBuildService.getSmartWorks(buildCode);
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
        /*
        smartBulids.addAll(SmartBuild.createSmartBuildsList(10));

        // 새로운 리스트 추가하기!!
        ArrayList<SmartBuild> newItems = SmartBuild.createSmartBuildsList(3);
        smartBulids.addAll(newItems);

        // 기존 리스트에 추가하기!!
        adapter.notifyItemRangeInserted(curSize, newItems.size());

        // 중간에 아이템 추가하기!!
        smartBulids.add(0, new SmartBuild());
        adapter.notifyItemInserted(0);

        // Scrolling to New Items
        adapter.notifyItemInserted(12);
        rvSmartBuilds.scrollToPosition(12);

        adapter.notifyItemInserted(smartBulids.size() - 1); // Last element position
        rvSmartBuilds.scrollToPosition(adapter.getItemCount() - 1); // update based on adapter
        */

        //rvSmartWorks.setHasFixedSize(true);

        // Attach the adapter to the recyclerview to populate items
        /*
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(adapter);
        alphaInAnimationAdapter.setDuration(1000);
        rvSmartBuilds.setAdapter(new ScaleInAnimationAdapter(alphaInAnimationAdapter));
        */
        rvSmartWorks.setAdapter(adapter);

        /*
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.scrollToPosition(0);
        rvSmartBuilds.setLayoutManager(layoutManager);

        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        */
/*
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        rvSmartBuilds.addItemDecoration(itemDecoration);
*/

        rvSmartWorks.setItemAnimator(new SlideInUpAnimator());

        /***************************************************************************/
        adapter.setOnItemClickListener(new SmartWorkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // String name = SmartSingleton.arrSmartWorks.get(position).strName;
                // SmartSingleton.arrSmartWorks.get(position).strName = "변경되었습니다.";
                adapter.notifyItemChanged(position);

                //Intent intentWorkView = new Intent(this, WorkViewActivity.class);
                //startActivity(intentWorkView);

                // Toast.makeText(getApplicationContext(), name + " was clicked!", Toast.LENGTH_SHORT).show();
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
        collapsingToolbar.setTitle("동구 국민체육문화센터 신축공사");

        final int myDrawable = R.drawable.img_intro;
        final ImageView iv = (ImageView)findViewById(R.id.backdrop);
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