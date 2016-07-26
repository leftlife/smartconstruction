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
import kr.koogle.android.smartconstruction.http.SmartBBSClient;
import kr.koogle.android.smartconstruction.http.SmartService;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;
import kr.koogle.android.smartconstruction.util.RbPreference;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartBBSClientFragment extends Fragment {

    private static final String TAG = "SmartBBSClientFragment";
    private RbPreference pref;

    public static RecyclerView rvSmartBBSClients;
    private SmartBBSClientAdapter adapter;

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
        rootView = inflater.inflate(R.layout.fragment_smart_bbs_client, container, false);
        mInflater = inflater; //getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // SmartSingleton 생성 !!
        SmartSingleton.getInstance();
        // Settings 값 !!
        pref = new RbPreference(getContext());

        // RecyclerView 저장
        rvSmartBBSClients = (RecyclerView) rootView.findViewById(R.id.rvSmartBBSClients);
        // LayoutManager 저장
        layoutManager = new LinearLayoutManager(getActivity());
        // RecycleView에 LayoutManager 세팅
        rvSmartBBSClients.setLayoutManager(layoutManager);

        // Lookup the recyclerview in activity layout
        RecyclerView rvSmartBBSClients = (RecyclerView) rootView.findViewById(R.id.rvSmartBBSClients);

        // Adapter 생성
        adapter = new SmartBBSClientAdapter(getContext(), SmartSingleton.arrSmartBBSClients);

        if(isNewBuild || SmartSingleton.arrSmartBBSClients.isEmpty()) {
            /******************************************************************************************/
            addItems();
            /******************************************************************************************/
        }

        // RecycleView 에 Adapter 세팅
        rvSmartBBSClients.setAdapter(adapter);

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
        if (SmartSingleton.arrSmartBBSClients.isEmpty()) {
            //empLayout.setVisibility(View.VISIBLE);
        } else {
            //empLayout.setVisibility(View.GONE);
            rvSmartBBSClients.setItemAnimator(new SlideInUpAnimator());
        }
        /***************************************************************************/
        // 리스트 클릭시 상세 페이지 보기 !!
        adapter.setOnItemClickListener(new SmartBBSClientAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                adapter.notifyItemChanged(position);

                Intent intentWorkView = new Intent(getActivity(), SmartClientViewActivity.class);
                final int intId = SmartSingleton.arrSmartBBSClients.get(position).intId;
                intentWorkView.putExtra("intId", intId);
                startActivityForResult(intentWorkView, 1002);
                Toast.makeText(getContext(), "intId : " + intId, Toast.LENGTH_SHORT).show();
            }
        });
        /***************************************************************************/

        // Handling Touch Events
        rvSmartBBSClients.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
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
        ItemClickSupport.addTo(rvSmartBBSClients).setOnItemClickListener(
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
        rvSmartBBSClients.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        // SmartBBSClient 값 불러오기 (진행중인 현장)
        Log.d(TAG, "SmartService.getSmartBBSClients 실행!! / pref_access_token : " + pref.getValue("pref_access_token", ""));
        SmartService smartService = ServiceGenerator.createService(SmartService.class, pref.getValue("pref_access_token", ""));
        final Map<String, String> mapOptions = new HashMap<String, String>();
        mapOptions.put("offset", String.valueOf(layoutManager.getItemCount()));

        Log.d(TAG, "getSmartBBSClient START !!!");
        Call<ArrayList<SmartBBSClient>> call = smartService.getSmartBBSClients();
        Log.d(TAG, "getSmartBBSClient END !!!");

        call.enqueue(new Callback<ArrayList<SmartBBSClient>>() {
            @Override
            public void onResponse(Call<ArrayList<SmartBBSClient>> call, Response<ArrayList<SmartBBSClient>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final ArrayList<SmartBBSClient> responseSmartBBSClients = response.body();

                    if(responseSmartBBSClients.size() != 0) {
                        Log.d(TAG, "responseSmartBBSClients : size " + responseSmartBBSClients.size());
                        SmartSingleton.arrSmartBBSClients.addAll(responseSmartBBSClients);
                        // 최근 카운트 체크
                        int curSize = adapter.getItemCount();
                        adapter.notifyItemRangeInserted(curSize, responseSmartBBSClients.size());
                    } else {
                        if(SmartSingleton.arrSmartBBSClients.isEmpty()) {
                            viewEmpty = mInflater.inflate(R.layout.row_empty, null);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
                            viewEmpty.setLayoutParams(params);
                            RelativeLayout rmSmartBBSOrder = (RelativeLayout) rootView.findViewById(R.id.fm_smart_bbs_client);
                            rmSmartBBSOrder.addView(viewEmpty);
                        } else {
                            RelativeLayout rmSmartBBSOrder = (RelativeLayout) rootView.findViewById(R.id.fm_smart_bbs_client);
                            rmSmartBBSOrder.removeView(viewEmpty);
                        }

                        Snackbar.make(SmartBBSClientFragment.rvSmartBBSClients, "마지막 리스트 입니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                } else {
                    Toast.makeText(getContext(), "데이터가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "responseSmartBBSClients : 데이터가 정확하지 않습니다.");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<SmartBBSClient>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 상태가 좋지 않습니다!!!", Toast.LENGTH_SHORT).show();
                Log.d("Error", t.getMessage());
            }
        });
        /******************************************************************************************/
    }

}
