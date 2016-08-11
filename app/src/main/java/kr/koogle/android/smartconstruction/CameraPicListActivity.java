package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.graphics.Camera;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartPhoto;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.SmartWork;
import kr.koogle.android.smartconstruction.util.EndlessScrollListener;
import kr.koogle.android.smartconstruction.util.EndlessScrollView;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraPicListActivity extends AppCompatActivity {
    private static final String TAG = "CameraPicListActivity";
    private RbPreference pref;

    private static boolean isLoading;

    // Pull to Refresh 4-1
    private SwipeRefreshLayout swipeContainer;

    private LinearLayout layoutEmpty;
    private View viewEmpty;
    // recycleViewer
    private static RecyclerView recyclerView;
    private CameraPicListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    // intent 로 넘어온 값 받기
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_pic_list);
        ButterKnife.bind(this);
        // intent 등록
        intent = getIntent();

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(this);

        //layoutEmpty = (LinearLayout) findViewById(R.id.emp_layout_camera_pic_list);
        // RecyclerView 저장
        recyclerView = (RecyclerView) findViewById(R.id.rv_camera_pics);
        // LayoutManager 저장
        layoutManager = new LinearLayoutManager(CameraPicListActivity.this);
        // RecycleView에 LayoutManager 세팅
        recyclerView.setLayoutManager(layoutManager);

        final LinearLayout empLayout = (LinearLayout) findViewById(R.id.emp_layout); // 내용없을때 보이는 레이아웃
        // 리스트 표현하기 !!
        if (SmartSingleton.arrSmartPhotos.isEmpty()) {
            //empLayout.setVisibility(View.VISIBLE);
        } else {
            //empLayout.setVisibility(View.GONE);
            recyclerView.setItemAnimator(new SlideInUpAnimator());
        }

        /******************************************************************************************/
        // Adapter 생성
        adapter = new CameraPicListAdapter(this, SmartSingleton.arrSmartPhotos);
        SmartSingleton.arrSmartPhotos.clear();
        if(SmartSingleton.arrSmartPhotos.isEmpty()) {
            addRows();
            Log.d(TAG, "최초실행 : SmartSingleton.arrSmartPhotos.size() : " + SmartSingleton.arrSmartPhotos.size());
        }
        // RecycleView 에 Adapter 세팅
        recyclerView.setAdapter(adapter);
        // 리스트 표현하기 !!
        recyclerView.setItemAnimator(new SlideInUpAnimator());

        // 스크롤 이벤트 잡아내기 !!
        final NestedScrollView parentScrollView=(NestedScrollView)findViewById (R.id.nsv_camera_pic_list);
        parentScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                final int totalItemCount = layoutManager.getItemCount();
                final int layoutManagerH = layoutManager.getHeight();
                final int parentH = parentScrollView.getHeight();
                final int itemH = layoutManager.getChildAt(0).getHeight();

                if ( layoutManagerH - parentH - scrollY < 10 ) {
                    isLoading = true;
                    /******************************************************************************************/
                    addRows();
                    /******************************************************************************************/
                }
                Log.d(TAG, "scrollY : " + scrollY + " / height : " + layoutManagerH + " / heightRV : " + parentH + " / viewHeight : " + itemH);
            }
        });

        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_camera_pic_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ico_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPicListActivity.this.finish();
            }
        });

        /***************************************************************************/
        adapter.setOnItemClickListener(new CameraPicListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                // 클릭된 사진정보 부모 엑티비티에 전달
                final int intId = SmartSingleton.arrSmartPhotos.get(position).intId;
                final String strURL = SmartSingleton.arrSmartPhotos.get(position).strURL;
                final String strName = SmartSingleton.arrSmartPhotos.get(position).strName;
                final String strThumbnail = SmartSingleton.arrSmartPhotos.get(position).strThumbnail;

                final String strBuildCode = SmartSingleton.arrSmartPhotos.get(position).strBuildCode;
                final String strBuildName = SmartSingleton.arrSmartPhotos.get(position).strBuildName;
                final String strLavorCode = SmartSingleton.arrSmartPhotos.get(position).strLavorCode;
                final String strLocation = SmartSingleton.arrSmartPhotos.get(position).strLocation;
                final String strMemo = SmartSingleton.arrSmartPhotos.get(position).strMemo;
                final String datRegist = SmartSingleton.arrSmartPhotos.get(position).datRegist;
                adapter.notifyItemChanged(position);

                intent.putExtra("intId", String.valueOf(intId));
                intent.putExtra("strFileURL", strURL + strName);
                intent.putExtra("strURL", strURL);
                intent.putExtra("strName", strName);
                intent.putExtra("strThumbnail", strThumbnail);
                intent.putExtra("strBuildCode", strBuildCode);
                intent.putExtra("strBuildName", strBuildName);
                intent.putExtra("strLavorCode", strLavorCode);
                intent.putExtra("strLocation", strLocation);
                intent.putExtra("strMemo", strMemo);
                intent.putExtra("datRegist", datRegist);
                CameraPicListActivity.this.setResult(RESULT_OK, intent);
                finish();
            }
        });
        /***************************************************************************/

        // Pull to Refresh 4-2
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.srl_camera_pic_list);
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
        adapter.clear();
        addRows();
    }

    private void addRows() {
        /******************************************************************************************/
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));

        // QueryMap 생성하기
        final Map<String, String> mapOptions = new HashMap<String, String>();
        mapOptions.put("offset", String.valueOf(SmartSingleton.arrSmartPhotos.size()));
        Call<ArrayList<SmartPhoto>> call = smartService.getSmartPhotos(mapOptions);

        call.enqueue(new Callback<ArrayList<SmartPhoto>>() {
            @Override
            public void onResponse(Call<ArrayList<SmartPhoto>> call, Response<ArrayList<SmartPhoto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final ArrayList<SmartPhoto> responses = response.body();

                    if(responses.size() != 0) {
                        Log.d(TAG, "CameraPicListActivity 추가된 리스트 : size " + responses.size());
                        SmartSingleton.arrSmartPhotos.addAll(responses);
                        // 최근 카운트 체크
                        //int curSize = SmartSingleton.arrSmartPhotos.size();
                        //adapter.notifyItemRangeInserted(curSize, responses.size());
                        adapter.notifyDataSetChanged();

                        //if(viewEmpty != null) viewEmpty.setVisibility(View.GONE);
                    } else {
                        Snackbar.make(CameraPicListActivity.recyclerView, "마지막 리스트 입니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                        //viewEmpty = View.inflate(CameraPicListActivity.this, R.layout.row_empty, layoutEmpty);
                    }
                } else {
                    Toast.makeText(getApplication(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "responseSmartWorks : 데이터가 정확하지 않습니다.");
                }

                // Pull to Refresh 4-4
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ArrayList<SmartPhoto>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "네트워크 상태가 좋지 않습니다!!!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());

                // Pull to Refresh 4-4
                swipeContainer.setRefreshing(false);
            }

        });
        /******************************************************************************************/
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
            Intent intent = new Intent(CameraPicListActivity.this, CameraPicActivity.class);
            intent.putExtra("intId", 0);
            startActivityForResult(intent, 40001);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {

            case 40001: // 리스트 다시 읽기
                SmartSingleton.arrSmartPhotos.clear();
                addRows();
                break;
        }
    }

}
