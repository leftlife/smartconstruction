package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kr.koogle.android.smartconstruction.http.SmartPhoto;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;
import kr.koogle.android.smartconstruction.util.RbPreference;

public class CameraPicListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "CameraPicListAdapter";
    private RbPreference pref;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;

    private Context mContext;
    private ArrayList<SmartPhoto> mRows;
    private Context getContext() {
        return mContext;
    }

    public CameraPicListAdapter(Context context, ArrayList<SmartPhoto> arrRows) {
        mContext = context;
        mRows = arrRows;
        // Settings 값 !!
        pref = new RbPreference(context.getApplicationContext());
    }

    public void setmOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    public void add(SmartPhoto item, int position) {
        mRows.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(SmartPhoto item) {
        int position = mRows.indexOf(item);
        mRows.remove(position);
        notifyItemRemoved(position);
    }

    // Clean all elements of the recycler
    public void clear() {
        mRows.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(ArrayList<SmartPhoto> list) {
        mRows.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mRows.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        if (viewType == VIEW_TYPE_ITEM) {
            // 리스트 목록 디자인 설정
            View view = LayoutInflater.from(context).inflate(R.layout.row_camera_pic_list, parent, false);
            return new UserViewHolder(getContext(), view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_loading_item, parent, false);
            return new LoadingViewHolder(getContext(), view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get the data model based on position
        SmartPhoto row = mRows.get(position);

        // Set item views based on your views and data model
        if (holder instanceof UserViewHolder) {
            UserViewHolder userViewHolder = (UserViewHolder) holder;

            ImageView ivPhoto = userViewHolder.picPhoto;
            TextView tvBuildName = userViewHolder.buildName;
            TextView tvLaborName = userViewHolder.laborName;
            TextView tvContent = userViewHolder.content;
            TextView tvDate = userViewHolder.date;

            if ( !row.strName.isEmpty() ) {
                Picasso.with(getContext())
                        .load(row.strURL + row.strThumbnail)
                        .fit() // resize(700,400)
                        .into(ivPhoto);
            }
            tvBuildName.setText(row.strBuildName);
            tvLaborName.setText(row.strLavorCode);
            tvContent.setText(row.strMemo);
            tvDate.setText(row.datRegist);

            Log.d(TAG, " 리스트 생성 : " +position+ " / " +row.strBuildName+ " / " +row.strLavorCode+ " / " +row.strWriter+ " / ");
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return mRows.size();
    }

    public void setLoaded() { isLoading = false; }

    /***************************************************************************/
    private static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    /***************************************************************************/

    // 로딩용 뷰홀더 클래스 ########################################################################
    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        private Context context;

        public LoadingViewHolder(Context context, final View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
            this.context = context;
        }
    }
    // 뷰홀더 클래스 ###############################################################################
    public static class UserViewHolder extends RecyclerView.ViewHolder  { // implements View.OnClickListener

        private Context context;
        public ImageView picPhoto;
        public TextView buildName;
        public TextView laborName;
        public TextView content;
        public TextView date;

        public UserViewHolder(Context context, final View itemView) {
            super(itemView);
            picPhoto = (ImageView) itemView.findViewById(R.id.r_camera_pic_photo);
            buildName = (TextView) itemView.findViewById(R.id.r_camera_pic_build_name);
            laborName = (TextView) itemView.findViewById(R.id.r_camera_pic_labor_name);
            content = (TextView) itemView.findViewById(R.id.r_camera_pic_content);
            date = (TextView) itemView.findViewById(R.id.r_camera_pic_date);

            this.context = context;

            /***************************************************************************/
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onItemClick(itemView, getLayoutPosition());
                }
            });
            /***************************************************************************/
        }
    }

}