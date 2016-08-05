package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.net.Uri;
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
import kr.koogle.android.smartconstruction.http.SmartEmployee;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.SmartWork;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartEmployeeActivity extends AppCompatActivity {
    private static final String TAG = "SmartEmployeeActivity";
    private RbPreference pref;

    private static boolean isLoading;

    // Pull to Refresh 4-1
    private SwipeRefreshLayout swipeContainer;

    // recycleViewer
    private static RecyclerView recyclerView;
    private SmartEmployeeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    // intent 로 넘어온 값 받기
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_employee);
        ButterKnife.bind(this);
        // intent 등록
        intent = getIntent();

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(this);

        // RecyclerView 저장
        recyclerView = (RecyclerView) findViewById(R.id.rv_smart_employee);
        // LayoutManager 저장
        layoutManager = new LinearLayoutManager(SmartEmployeeActivity.this);
        // RecycleView에 LayoutManager 세팅
        recyclerView.setLayoutManager(layoutManager);

        final LinearLayout empLayout = (LinearLayout) findViewById(R.id.emp_layout); // 내용없을때 보이는 레이아웃
        // 리스트 표현하기 !!
        if (SmartSingleton.arrSmartEmployees.isEmpty()) {
            //empLayout.setVisibility(View.VISIBLE);
        } else {
            //empLayout.setVisibility(View.GONE);
            recyclerView.setItemAnimator(new SlideInUpAnimator());
        }

        /******************************************************************************************/
        // Adapter 생성
        adapter = new SmartEmployeeAdapter(this, SmartSingleton.arrSmartEmployees);
        SmartSingleton.arrSmartEmployees.clear();
        if(SmartSingleton.arrSmartEmployees.isEmpty()) {
            addRows();
            Log.d(TAG, "최초실행 : SmartSingleton.arrSmartEmployees.size() : " + SmartSingleton.arrSmartEmployees.size());
        }
        // RecycleView 에 Adapter 세팅
        recyclerView.setAdapter(adapter);
        // 리스트 표현하기 !!
        recyclerView.setItemAnimator(new SlideInUpAnimator());

        // 스크롤 이벤트 잡아내기 !!
        final NestedScrollView parentScrollView=(NestedScrollView)findViewById (R.id.nsv_smart_employee);
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
                    //addRows();
                    /******************************************************************************************/
                }
                Log.d(TAG, "scrollY : " + scrollY + " / height : " + layoutManagerH + " / heightRV : " + parentH + " / viewHeight : " + itemH);
            }
        });

        // 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_smart_employee);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ico_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmartEmployeeActivity.this.finish();
            }
        });

        /***************************************************************************/
        adapter.setOnItemClickListener(new SmartEmployeeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                // 클릭된 사진정보 부모 엑티비티에 전달
                final String strCode = SmartSingleton.arrSmartEmployees.get(position).strCode;
                final String strPhone = SmartSingleton.arrSmartEmployees.get(position).strPhone;
                adapter.notifyItemChanged(position);

                Intent intent = new Intent( Intent.ACTION_CALL );
                intent.setData( Uri.parse( "tel:" + strPhone ) );
                SmartEmployeeActivity.this.startActivity( intent );
            }
        });
        /***************************************************************************/

        // Pull to Refresh 4-2
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.srl_smart_employee);
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
        /*
        final Map<String, String> mapOptions = new HashMap<String, String>();
        mapOptions.put("offset", String.valueOf(SmartSingleton.arrSmartEmployees.size()));
        */
        Call<ArrayList<SmartEmployee>> call = smartService.getSmartEmployees();

        call.enqueue(new Callback<ArrayList<SmartEmployee>>() {
            @Override
            public void onResponse(Call<ArrayList<SmartEmployee>> call, Response<ArrayList<SmartEmployee>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final ArrayList<SmartEmployee> responses = response.body();

                    if(responses.size() != 0) {
                        Log.d(TAG, "SmartEmployeeActivity 추가된 리스트 : size " + responses.size());
                        SmartSingleton.arrSmartEmployees.addAll(responses);
                        // 최근 카운트 체크
                        //int curSize = SmartSingleton.arrSmartEmployees.size();
                        //adapter.notifyItemRangeInserted(curSize, responses.size());
                        adapter.notifyDataSetChanged();


                    } else {
                        Snackbar.make(SmartEmployeeActivity.recyclerView, "마지막 리스트 입니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } else {
                    Toast.makeText(getApplication(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "responseSmartWorks : 데이터가 정확하지 않습니다.");
                }

                // Pull to Refresh 4-4
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ArrayList<SmartEmployee>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "네트워크 상태가 좋지 않습니다!!!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());
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
            /*
            Intent intent = new Intent(SmartEmployeeActivity.this, CameraPicActivity.class);
            intent.putExtra("intId", 0);
            startActivityForResult(intent, 2001);
            */
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {

            case 2001: // 리스트 다시 읽기
                SmartSingleton.arrSmartEmployees.clear();
                addRows();
                break;
        }
    }

}
