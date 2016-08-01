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

import java.util.ArrayList;
import java.util.List;

import kr.koogle.android.smartconstruction.http.SmartClient;
import kr.koogle.android.smartconstruction.http.SmartComment;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;
import kr.koogle.android.smartconstruction.util.RbPreference;

public class SmartClientViewCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "SmartClientViewCommentAdapter";
    private RbPreference pref;
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

    public SmartClientViewCommentAdapter(Context context, ArrayList<SmartComment> smartClients) {
        mContext = context;
        mRows = smartClients;
        // Settings 값 !!
        pref = new RbPreference(context.getApplicationContext());
        Log.d("aaaa", "size : " + mRows.size() + " -------------------------------------------------------------------");
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
            View view = LayoutInflater.from(context).inflate(R.layout.row_loading_item, parent, false);
            return new LoadingViewHolder(getContext(), view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get the data model based on position
        SmartComment row = mRows.get(position);

        if (holder instanceof UserViewHolder) {
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            // 생성되는 View에 position값 저장
            userViewHolder.position = position;

            ImageView ivPhoto = userViewHolder.photo;
            TextView tvWriter = userViewHolder.writer;
            TextView tvDate = userViewHolder.date;
            TextView tvContent = userViewHolder.content;
            ImageView ivFile = userViewHolder.commentFile;
            ImageView ivDelete = userViewHolder.commentDelete;

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
            ivFile.setVisibility(View.GONE);
            if (row.strWriter.equals(pref.getValue("pref_user_id",""))) {
                ivDelete.setVisibility(View.VISIBLE);
            } else {
                ivDelete.setVisibility(View.GONE);
            }

            Log.d("aaaa", " 리스트 생성 : " +position+ " / " +row.datWrite+ " / " +row.strContent+ " / " +row.strWriter+ " / ");

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
        public int position;
        public ImageView photo;
        public TextView writer;
        public TextView date;
        public TextView content;
        public ImageView commentFile;
        // 내부 이벤트 적용을 위한 코드 3-1
        public ImageView commentDelete;

        public UserViewHolder(Context context, final View itemView) {
            super(itemView);
            this.context = context;

            photo = (ImageView) itemView.findViewById(R.id.r_client_view_comment_photo);
            writer = (TextView) itemView.findViewById(R.id.r_client_view_comment_writer);
            date = (TextView) itemView.findViewById(R.id.r_client_view_comment_date);
            content = (TextView) itemView.findViewById(R.id.r_client_view_comment_content);
            commentFile = (ImageView) itemView.findViewById(R.id.r_client_view_comment_file);
            commentDelete = (ImageView) itemView.findViewById(R.id.r_client_view_comment_x);

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