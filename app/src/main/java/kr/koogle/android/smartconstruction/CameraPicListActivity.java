package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
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
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraPicListActivity extends AppCompatActivity {
    private static final String TAG = "CameraPicListActivity";
    private RbPreference pref;

    private static boolean isLoading;

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
        if(SmartSingleton.arrSmartPhotos.isEmpty()) {
            addRows();
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
                adapter.notifyItemChanged(position);

                intent.putExtra("intId", String.valueOf(intId));
                intent.putExtra("strFileURL", strURL + strName);
                CameraPicListActivity.this.setResult(RESULT_OK, intent);
                finish();
            }
        });
        /***************************************************************************/

        /*
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                Log.d(TAG, "onItemRangeChanged " + itemCount);
                int headerCount = getHeaderCount();
                notifyItemRangeChanged(positionStart + headerCount, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                int headerCount = getHeaderCount();
                notifyItemRangeInserted(positionStart + headerCount, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                Log.d(TAG, "onItemRangeRemoved " + "itemCount : " + itemCount);
                int headerCount = getHeaderCount();
                notifyItemRangeRemoved(positionStart + headerCount, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                int headerCount = getHeaderCount();
                notifyItemMoved(fromPosition + headerCount, toPosition + headerCount);
                // TODO itemcount가 1일 경우이므로 1보다 크면 제대로 동작하지 않는다.
            }
        });
        */
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
                    } else {
                        Snackbar.make(CameraPicListActivity.recyclerView, "마지막 리스트 입니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } else {
                    Toast.makeText(getApplication(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "responseSmartWorks : 데이터가 정확하지 않습니다.");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<SmartPhoto>> call, Throwable t) {
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
            Intent intent = new Intent(CameraPicListActivity.this, CameraPicActivity.class);
            intent.putExtra("intId", 0);
            startActivityForResult(intent, 2001);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {

            case 2001: // 리스트 다시 읽기
                SmartSingleton.arrSmartPhotos.clear();
                addRows();
                break;
        }
    }

}
