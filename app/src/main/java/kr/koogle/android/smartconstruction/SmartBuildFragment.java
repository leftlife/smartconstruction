package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartBuild;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartBuildFragment extends Fragment {

    private static final String TAG = "SmartBuild";
    private RbPreference pref;
    private View rootView;
    private SmartBuildAdapter adapter;
    private RecyclerView rvSmartBuilds;
    private LayoutInflater mInflater;
    private View viewEmpty;

    // Pull to Refresh 4-1
    private SwipeRefreshLayout swipeContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_smart_build, container, false);
        mInflater = inflater; //getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getContext());

        // Lookup the recyclerview in activity layout
        rvSmartBuilds = (RecyclerView) rootView.findViewById(R.id.rvSmartBuilds);
        // Create adapter passing in the sample user data
        adapter = new SmartBuildAdapter(getContext(), SmartSingleton.arrSmartBuilds);

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

        rvSmartBuilds.setHasFixedSize(true);

        /*
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(adapter);
        alphaInAnimationAdapter.setDuration(1000);
        rvSmartBuilds.setAdapter(new ScaleInAnimationAdapter(alphaInAnimationAdapter));
        */
        rvSmartBuilds.setAdapter(adapter);

        // Set layout manager to position the items
        rvSmartBuilds.setLayoutManager(new LinearLayoutManager(getContext()));

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
        if(SmartSingleton.arrSmartBuilds.isEmpty()) {
            /******************************************************************************************/
            addItems();
            /******************************************************************************************/
        }

        final LinearLayout empLayout = (LinearLayout) rootView.findViewById(R.id.emp_layout);

        if (SmartSingleton.arrSmartBuilds.isEmpty()) {
            //empLayout.setVisibility(View.VISIBLE);
        } else {
            //empLayout.setVisibility(View.GONE);
            rvSmartBuilds.setItemAnimator(new SlideInUpAnimator());
        }
        /***************************************************************************/
        adapter.setOnItemClickListener(new SmartBuildAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final String strCode = SmartSingleton.arrSmartBuilds.get(position).strCode;
                final String strBuildName = SmartSingleton.arrSmartBuilds.get(position).strName;
                final String strImageUrl = SmartSingleton.arrSmartBuilds.get(position).strImageURL;
                // SmartSingleton.arrSmartBuilds.get(position).strName = "직접 내용 변경";
                adapter.notifyItemChanged(position);

                Intent intentWork = new Intent(getContext(), SmartWorkActivity.class);
                intentWork.putExtra("strBuildCode", strCode);
                intentWork.putExtra("strBuildName", strBuildName);
                intentWork.putExtra("strImageUrl", strImageUrl);
                startActivity(intentWork);
                // Toast.makeText(getContext(), name + " was clicked!", Toast.LENGTH_SHORT).show();
            }
        });
        /***************************************************************************/

        // Handling Touch Events
        rvSmartBuilds.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                // Handle on touch events here
                //Log.d(TAG, "onTouchEvent : touched !!");
            }

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        /* 가장 쉽게 클릭 이벤츠 핸들러 만들기
        ItemClickSupport.addTo(rvSmartBuilds).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        // do it
                        Log.d("LLL", "position: " + position);
                    }
                }
        );
         */

        // 스크롤시 FAB 버튼 숨기기 !!
        rvSmartBuilds.addOnScrollListener(new RecyclerView.OnScrollListener() {
            final FloatingActionButton fab = MainActivity.fab;
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx,int dy){
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 3) {
                    // Scroll Down
                    if (fab.isShown()) {
                        fab.hide();
                    }
                } else {
                    // Scroll Up
                    if (!fab.isShown()) {
                        fab.show();
                    }
                }
                Log.d(TAG, "dy : " + dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState == 0) fab.show();
                Log.d(TAG, "newState : " + newState);
            }
        });

        // Pull to Refresh 4-2
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_smart_build);
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

        return rootView;
    }

    // Pull to Refresh 4-3
    public void fetchTimelineAsync(int page) {
        adapter.clear();
        addItems();
    }

    public void addItems() {
        // SmartBuild 값 불러오기 (진행중인 현장)
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        Call<ArrayList<SmartBuild>> call = smartService.getSmartBuilds();

        call.enqueue(new Callback<ArrayList<SmartBuild>>() {
            @Override
            public void onResponse(Call<ArrayList<SmartBuild>> call, Response<ArrayList<SmartBuild>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final ArrayList<SmartBuild> responses = response.body();

                    SmartSingleton.arrSmartBuilds.addAll(responses);
                    // 최근 카운트 체크
                    int curSize = SmartSingleton.arrSmartBuilds.size();
                    adapter.notifyItemRangeInserted(curSize, responses.size());
                    //adapter.notifyDataSetChanged();

                    if(SmartSingleton.arrSmartBuilds.isEmpty()) {
                        viewEmpty = mInflater.inflate(R.layout.row_empty, null);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
                        viewEmpty.setLayoutParams(params);
                        RelativeLayout rmSmartBuild = (RelativeLayout) rootView.findViewById(R.id.fm_smart_build);
                        rmSmartBuild.addView(viewEmpty);

                    }
                } else {
                    Toast.makeText(getContext(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();

                    Intent intentLogin = new Intent(getContext(), LoginActivity.class);
                    startActivity(intentLogin);
                }

                // Pull to Refresh 4-4
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ArrayList<SmartBuild>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 상태가 좋지 않습니다.", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());

                // Pull to Refresh 4-4
                swipeContainer.setRefreshing(false);
            }
        });
    }
}
