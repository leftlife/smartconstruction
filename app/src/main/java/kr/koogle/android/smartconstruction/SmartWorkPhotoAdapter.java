package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.SmartPhoto;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;

public class SmartWorkPhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "SmartWorkPhotoAdapter";
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;

    private Context mContext;
    private static ArrayList<SmartPhoto> mRows;
    private Context getContext() {
        return mContext;
    }

    public SmartWorkPhotoAdapter(Context context, ArrayList<SmartPhoto> arrRows) {
        mContext = context;
        mRows = arrRows;
    }

    // Clean all elements of the recycler
    public void clear() {
        mRows.clear();
        notifyDataSetChanged();
    }

    public void setmOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mRows.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_work_view_labor, parent, false);
            return new UserViewHolder(getContext(), view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_loading_item, parent, false);
            return new LoadingViewHolder(getContext(), view);
        }
        return null;

        /*
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View smartWorkView = inflater.inflate(R.layout.row_smart_work, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(getContext(), smartWorkView);
        return viewHolder;
        */
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get the data model based on position
        SmartPhoto row = mRows.get(position);

        // Set item views based on your views and data model
        if (holder instanceof UserViewHolder) {
            //User user = mRows.get(position);
            UserViewHolder userViewHolder = (UserViewHolder) holder;

            ImageView imgPhoto = userViewHolder.imgPhoto;
            TextView txtBuild = userViewHolder.txtBuild;
            TextView txtLocation = userViewHolder.txtLocation;
            TextView txtDate = userViewHolder.txtDate;
            TextView txtMemo = userViewHolder.txtMemo;
            Button btnDelete = userViewHolder.btnDelete;
            Button btnModify = userViewHolder.btnModify;

            if( !row.strThumbnail.isEmpty() ) {
                Picasso.with(getContext())
                        .load(row.strURL + row.strThumbnail)
                        .fit() // resize(700,400)
                        .into(imgPhoto);
            }

            txtBuild.setText(row.strBuildName);
            txtLocation.setText(row.strLocation);
            txtDate.setText(row.datRegist);
            txtMemo.setText(row.strMemo);

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
        public ImageView imgPhoto;
        public TextView txtBuild;
        public TextView txtLocation;
        public TextView txtDate;
        public TextView txtMemo;
        public Button btnDelete;
        public Button btnModify;

        public UserViewHolder(Context context, final View itemView) {
            super(itemView);
            imgPhoto = (ImageView) itemView.findViewById(R.id.img_work_view_photo);
            txtBuild = (TextView) itemView.findViewById(R.id.txt_work_view_photo_build);
            txtMemo = (TextView) itemView.findViewById(R.id.txt_work_view_photo_memo);
            txtLocation = (TextView) itemView.findViewById(R.id.txt_work_view_photo_location);
            txtDate = (TextView) itemView.findViewById(R.id.txt_work_view_photo_date);
            btnDelete = (Button) itemView.findViewById(R.id.btn_work_view_photo_delete);
            btnModify = (Button) itemView.findViewById(R.id.btn_work_view_photo_modify);

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