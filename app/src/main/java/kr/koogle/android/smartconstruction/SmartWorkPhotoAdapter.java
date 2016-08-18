package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kr.koogle.android.smartconstruction.http.SmartLabor;
import kr.koogle.android.smartconstruction.http.SmartSingleton;
import kr.koogle.android.smartconstruction.http.SmartPhoto;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;
import kr.koogle.android.smartconstruction.util.RbPreference;

public class SmartWorkPhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "SmartWorkPhotoAdapter";
    private RbPreference pref;
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
        // Settings 값 !!
        pref = new RbPreference(context.getApplicationContext());
    }

    // Clean all elements of the recycler
    public void clear() {
        mRows.clear();
        notifyDataSetChanged();
    }

    public void add(SmartPhoto item, int position) {
        mRows.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        mRows.remove(position);
        notifyItemRemoved(position);
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
            View view = LayoutInflater.from(context).inflate(R.layout.row_work_view_photo, parent, false);
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
            TextView txtLabor = userViewHolder.txtLabor;
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

            txtLabor.setText(row.strLavorCode);
            txtLocation.setText(row.strLocation);
            txtDate.setText(row.datRegist);
            txtMemo.setText(row.strMemo);

            if( !SmartSingleton.smartWork.strId.equals(pref.getValue("pref_user_id","")) ) {
                btnDelete.setVisibility(View.GONE);
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

    private static OnItemXClickListener listenerX;
    public interface OnItemXClickListener {
        void onItemXClick(View itemView, int position);
    }
    public void setOnItemXClickListener(OnItemXClickListener listenerX) { this.listenerX = listenerX; }
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
    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private Context context;
        public int position;
        public ImageView imgPhoto;
        public TextView txtLabor;
        public TextView txtLocation;
        public TextView txtDate;
        public TextView txtMemo;
        public Button btnDelete;
        public Button btnModify;
        // 내부 이벤트 적용을 위한 코드 3-1
        public ImageView commentDelete;

        public UserViewHolder(Context context, final View itemView) {
            super(itemView);

            this.context = context;
            imgPhoto = (ImageView) itemView.findViewById(R.id.img_work_view_photo);
            txtLabor = (TextView) itemView.findViewById(R.id.txt_work_view_photo_labor);
            txtMemo = (TextView) itemView.findViewById(R.id.txt_work_view_photo_memo);
            txtLocation = (TextView) itemView.findViewById(R.id.txt_work_view_photo_location);
            txtDate = (TextView) itemView.findViewById(R.id.txt_work_view_photo_date);
            btnDelete = (Button) itemView.findViewById(R.id.btn_work_view_photo_delete);
            btnModify = (Button) itemView.findViewById(R.id.btn_work_view_photo_modify);

            /***************************************************************************/
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onItemClick(itemView, getLayoutPosition());
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listenerX != null)
                        listenerX.onItemXClick(itemView, getLayoutPosition());
                }
            });
            /***************************************************************************/
        }

        // 내부 이벤트 적용을 위한 코드 3-3
        @Override
        public void onClick(View v) {
            if (v.getId() == btnDelete.getId()){

            }
            Toast.makeText(v.getContext(), "CLICK : " + String.valueOf(position) + " / " + String.valueOf(btnDelete.getId()), Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onLongClick(View v) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle ("Hello Dialog")
                    .setMessage ("LONG CLICK : " + String.valueOf(position) + " / " + String.valueOf(btnDelete.getId()))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            builder.create().show();
            return true;
        }
    }

}