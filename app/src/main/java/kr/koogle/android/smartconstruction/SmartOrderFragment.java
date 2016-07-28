package kr.koogle.android.smartconstruction;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.ServiceGenerator;
import kr.koogle.android.smartconstruction.http.SmartOrder;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartOrderFragment extends Fragment {

    private static final String TAG = "SmartOrderFragment";
    private RbPreference pref;

    public static RecyclerView recyclerView;
    private SmartOrderAdapter adapter;

    private static String strBuildCode = "start";
    private static Boolean isNewBuild = false;
    private static String strWorkTitleTop = "";

    private static boolean isLoading;
    private static int visibleThreshold = 10;

    private RecyclerView.LayoutManager layoutManager;

    private View rootView;
    private LayoutInflater mInflater;
    private View viewEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_smart_order, container, false);
        mInflater = inflater; //getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getContext());

        // RecyclerView 저장
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_smart_orders);
        // LayoutManager 저장
        layoutManager = new LinearLayoutManager(getActivity());
        // RecycleView에 LayoutManager 세팅
        recyclerView.setLayoutManager(layoutManager);

        // Adapter 생성
        adapter = new SmartOrderAdapter(getContext(), SmartSingleton.arrSmartOrders);

        if(isNewBuild || SmartSingleton.arrSmartOrders.isEmpty()) {
            /******************************************************************************************/
            addItems();
            /******************************************************************************************/
        }

        // RecycleView 에 Adapter 세팅
        recyclerView.setAdapter(adapter);

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

        final LinearLayout empLayout = (LinearLayout) rootView.findViewById(R.id.emp_layout);

        // 리스트 표현하기 !!
        if (SmartSingleton.arrSmartBuilds.isEmpty()) {
            //empLayout.setVisibility(View.VISIBLE);
        } else {
            //empLayout.setVisibility(View.GONE);
            recyclerView.setItemAnimator(new SlideInUpAnimator());
        }
        /***************************************************************************/
        adapter.setOnItemClickListener(new SmartOrderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                final String strCode = SmartSingleton.arrSmartOrders.get(position).strSiteId;
                final String strDate = SmartSingleton.arrSmartOrders.get(position).datWrite;
                final String strImageUrl = SmartSingleton.arrSmartOrders.get(position).strContent;
                adapter.notifyItemChanged(position);

                Intent intentWorkView = new Intent(getActivity(), SmartClientViewActivity.class);
                intentWorkView.putExtra("strBuildCode", strCode);
                intentWorkView.putExtra("strBuildDate", strDate);
                intentWorkView.putExtra("strImageUrl", strImageUrl);
                startActivityForResult(intentWorkView, 1002);
                Toast.makeText(getContext(), "strBuildCode : " + strCode, Toast.LENGTH_SHORT).show();
            }
        });
        /***************************************************************************/

        // Handling Touch Events
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
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
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        return rootView;
    }

    private void addItems() {
        /******************************************************************************************/
        // SmartBuild 값 불러오기 (진행중인 현장)
        Log.d(TAG, "SmartService.getSmartBBSOrders 실행!! / pref_access_token : " + pref.getValue("pref_access_token", ""));
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        final Map<String, String> mapOptions = new HashMap<String, String>();
        mapOptions.put("offset", String.valueOf(layoutManager.getItemCount()));

        Log.d(TAG, "getSmartBBSOrder START !!!");
        Call<ArrayList<SmartOrder>> call = smartService.getSmartBBSOrders();
        Log.d(TAG, "getSmartBBSOrder END !!!");

        call.enqueue(new Callback<ArrayList<SmartOrder>>() {
            @Override
            public void onResponse(Call<ArrayList<SmartOrder>> call, Response<ArrayList<SmartOrder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final ArrayList<SmartOrder> responseSmartOrders = response.body();

                    if(responseSmartOrders.size() != 0) {
                        Log.d(TAG, "responseSmartOrders : size " + responseSmartOrders.size());
                        SmartSingleton.arrSmartOrders.addAll(responseSmartOrders);
                        // 최근 카운트 체크
                        int curSize = adapter.getItemCount();
                        adapter.notifyItemRangeInserted(curSize, responseSmartOrders.size());
                    } else {
                        if(SmartSingleton.arrSmartOrders.isEmpty()) {
                            viewEmpty = mInflater.inflate(R.layout.row_empty, null);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
                            viewEmpty.setLayoutParams(params);
                            RelativeLayout rmSmartBBSClient = (RelativeLayout) rootView.findViewById(R.id.fm_smart_bbs_order);
                            rmSmartBBSClient.addView(viewEmpty);
                        } else {
                            RelativeLayout rmSmartBBSClient = (RelativeLayout) rootView.findViewById(R.id.fm_smart_bbs_order);
                            rmSmartBBSClient.removeView(viewEmpty);
                        }

                        Snackbar.make(SmartOrderFragment.recyclerView, "마지막 리스트 입니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } else {
                    Toast.makeText(getContext(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "responseSmartBBSOrders : 데이터가 정확하지 않습니다.");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<SmartOrder>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 상태가 좋지 않습니다!!!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());
            }
        });
        /******************************************************************************************/
    }

}
