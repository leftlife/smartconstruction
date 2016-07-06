package kr.koogle.android.smartconstruction;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import kr.koogle.android.smartconstruction.http.SmartBuild;

public class SmartBuildFragment extends Fragment {

    private static final String TAG = "SmartBuildFragment";
    private View rootView;
    public static ArrayList<SmartBuild> smartBulids;
    private SmartBuildAdapter adapter;

    private ListView mListView;
    private LayoutInflater mInflater;
    private boolean mLockListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_smartbuild, container, false);
        mInflater = inflater; //getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Lookup the recyclerview in activity layout
        RecyclerView rvSmartBuilds = (RecyclerView) rootView.findViewById(R.id.rvSmartBuilds);

        // Initialize smartBulids - do not reinitialize an existing reference used by an adapter
        //smartBulids = SmartBuild.createSmartBuildsList(20);
        smartBulids = new ArrayList<>();
        smartBulids.addAll(SmartBuild.createSmartBuildsList(10));

        // Create adapter passing in the sample user data
        adapter = new SmartBuildAdapter(getContext(), smartBulids);

        // 최근 카운트 체크
        int curSize = adapter.getItemCount();

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

        rvSmartBuilds.setHasFixedSize(true);

        // Attach the adapter to the recyclerview to populate items
        //rvSmartBuilds.setAdapter(adapter);
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(adapter);
        alphaInAnimationAdapter.setDuration(1000);
        rvSmartBuilds.setAdapter(new ScaleInAnimationAdapter(alphaInAnimationAdapter));
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

        rvSmartBuilds.setItemAnimator(new SlideInUpAnimator());

        /***************************************************************************/
        adapter.setOnItemClickListener(new SmartBuildAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String name = smartBulids.get(position).strName;
                smartBulids.get(position).strName = "변경되었습니다.";
                adapter.notifyItemChanged(position);
                Toast.makeText(getContext(), name + " was clicked!", Toast.LENGTH_SHORT).show();
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

        return rootView;
    }
}
