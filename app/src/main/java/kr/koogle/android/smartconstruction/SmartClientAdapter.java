package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kr.koogle.android.smartconstruction.http.SmartClient;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;

public class SmartClientAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "SmartClientAdapter";
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;

    private Context mContext;
    private static ArrayList<SmartClient> mRows;
    private List<SmartClient> mUsers = SmartSingleton.arrSmartClients;

    private Context getContext() {
        return mContext;
    }

    public SmartClientAdapter(Context context, ArrayList<SmartClient> smartClients) {
        mContext = context;
        mRows = smartClients;
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
        return mUsers.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_smart_client, parent, false);
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
        SmartClient smartClient = mRows.get(position);

        // Set item views based on your views and data model
        if (holder instanceof UserViewHolder) {
            //User user = mUsers.get(position);
            UserViewHolder userViewHolder = (UserViewHolder) holder;

            ImageView ivImage = userViewHolder.image;
            TextView tvTitle = userViewHolder.title;
            TextView tvBuild = userViewHolder.build;
            TextView tvDate = userViewHolder.date;
            TextView tvWriter = userViewHolder.writer;

            /*
            Picasso.with(getContext())
                    .load(smartClient.strImageURL)
                    .fit() // resize(700,400)
                    .into(ivImage);
            */
            tvTitle.setText(smartClient.strTitle);
            tvBuild.setText(smartClient.strCate1);
            tvWriter.setText(smartClient.strUserId);
            tvDate.setText(smartClient.datWrite.substring(0, 10));

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

        public ImageView image;
        public TextView title;
        public TextView build;
        public TextView writer;
        public TextView date;
        private Context context;

        public UserViewHolder(Context context, final View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.r_sbc_btn_image);
            title = (TextView) itemView.findViewById(R.id.r_sbc_title);
            build = (TextView) itemView.findViewById(R.id.r_sbc_build);
            date = (TextView) itemView.findViewById(R.id.r_sbc_date);
            writer = (TextView) itemView.findViewById(R.id.r_sbc_writer);

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