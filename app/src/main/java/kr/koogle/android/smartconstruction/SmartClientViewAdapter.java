package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import kr.koogle.android.smartconstruction.http.SmartComment;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;

public class SmartClientViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "SmartClientViewAdapter";
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;

    private Context mContext;
    private ArrayList<SmartComment> mRows;
    private Context getContext() {
        return mContext;
    }

    public SmartClientViewAdapter(Context context, ArrayList<SmartComment> arrRows) {
        mContext = context;
        mRows = arrRows;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) SmartClientViewActivity.rvSmartComments.getLayoutManager();
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
            View view = LayoutInflater.from(context).inflate(R.layout.row_client_view_comment, parent, false);
            return new UserViewHolder(getContext(), view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_loading_item, parent, false);
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
        SmartComment row = mRows.get(position);

        // Set item views based on your views and data model
        if (holder instanceof UserViewHolder) {
            UserViewHolder userViewHolder = (UserViewHolder) holder;

            ImageView ivPhoto = userViewHolder.photo;
            TextView tvWriter = userViewHolder.writer;
            TextView tvDate = userViewHolder.date;
            TextView tvContent = userViewHolder.content;

            /*
            if( !mRows.strImageURL.isEmpty() ) {
                Picasso.with(getContext())
                        .load(mRows.strImageURL)
                        .fit() // resize(700,400)
                        .into(ivImage);
            }
            */
            tvWriter.setText(row.strWriter);
            tvDate.setText(row.datWrite);
            tvContent.setText(row.strContent);

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
        public ImageView photo;
        public TextView writer;
        public TextView date;
        public TextView content;

        public UserViewHolder(Context context, final View itemView) {
            super(itemView);
            photo = (ImageView) itemView.findViewById(R.id.r_client_view_comment_photo);
            writer = (TextView) itemView.findViewById(R.id.r_client_view_comment_writer);
            date = (TextView) itemView.findViewById(R.id.r_client_view_comment_date);
            content = (TextView) itemView.findViewById(R.id.r_client_view_comment_content);

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