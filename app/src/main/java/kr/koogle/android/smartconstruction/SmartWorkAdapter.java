package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
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
import java.util.List;

import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.SmartWork;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;

public class SmartWorkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "SmartWorkAdapter";
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;

    private Context mContext;
    private static ArrayList<SmartWork> mRows;
    private Context getContext() {
        return mContext;
    }

    public SmartWorkAdapter(Context context, ArrayList<SmartWork> arrRows) {
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
            View view = LayoutInflater.from(context).inflate(R.layout.row_smart_work, parent, false);
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
        SmartWork row = mRows.get(position);

        // Set item views based on your views and data model
        if (holder instanceof UserViewHolder) {
            //User user = mRows.get(position);
            UserViewHolder userViewHolder = (UserViewHolder) holder;

            ImageView ivImage = userViewHolder.image;
            TextView tvDate = userViewHolder.date;
            TextView tvWork0 = userViewHolder.work1;
            TextView tvWork1 = userViewHolder.work2;
            TextView tvWork2 = userViewHolder.work3;

            if( row.arrSmartPhotos.size() > 0 ) {
                row.strImageURL = row.arrSmartPhotos.get(0).strURL + row.arrSmartPhotos.get(0).strThumbnail;
                Picasso.with(getContext())
                        .load(row.strImageURL)
                        .fit() // resize(700,400)
                        .into(ivImage);
            }
            tvDate.setText(row.strDate);
            if ( row.arrSmartLabors.size() == 1 ) {
                tvWork0.setText(row.arrSmartLabors.get(0).strCate1 + " " + row.arrSmartLabors.get(0).strMemo);
            } else if ( row.arrSmartLabors.size() == 2 ) {
                tvWork0.setText(row.arrSmartLabors.get(0).strCate1 + " " + row.arrSmartLabors.get(0).strMemo);
                tvWork1.setText(row.arrSmartLabors.get(1).strCate1 + " " + row.arrSmartLabors.get(1).strMemo);
            } else if ( row.arrSmartLabors.size() == 3 ) {
                tvWork0.setText(row.arrSmartLabors.get(0).strCate1 + " " + row.arrSmartLabors.get(0).strMemo);
                tvWork1.setText(row.arrSmartLabors.get(1).strCate1 + " " + row.arrSmartLabors.get(1).strMemo);
                tvWork2.setText(row.arrSmartLabors.get(2).strCate1 + " " + row.arrSmartLabors.get(2).strMemo);
            }
            if ( row.arrSmartLabors.size() > 0 ) {
                Log.d(TAG, " 리스트 생성 : " + row.arrSmartLabors.size() + " / " + row.strDate + " / " + row.arrSmartLabors.get(0).strCate1 + " / " + row.arrSmartLabors.get(0).strMemo + " / ");
            }

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
        public TextView date;
        public TextView work1;
        public TextView work2;
        public TextView work3;
        private Context context;

        public UserViewHolder(Context context, final View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.r_sw_image);
            date = (TextView) itemView.findViewById(R.id.r_sw_date);
            work1 = (TextView) itemView.findViewById(R.id.r_sw_work_1);
            work2 = (TextView) itemView.findViewById(R.id.r_sw_work_2);
            work3 = (TextView) itemView.findViewById(R.id.r_sw_work_3);

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