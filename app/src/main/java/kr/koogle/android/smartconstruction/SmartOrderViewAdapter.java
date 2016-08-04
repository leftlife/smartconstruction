package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.ArrayList;

import kr.koogle.android.smartconstruction.http.SmartComment;
import kr.koogle.android.smartconstruction.http.SmartPhoto;
import kr.koogle.android.smartconstruction.util.OnLoadMoreListener;
import kr.koogle.android.smartconstruction.util.RbPreference;

public class SmartOrderViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG = "SmartOrderViewAdapter";
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

    public SmartOrderViewAdapter(Context context, ArrayList<SmartComment> arrRows) {
        mContext = context;
        mRows = arrRows;
        // Settings 값 !!
        pref = new RbPreference(context.getApplicationContext());
    }

    public void setmOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    public void add(SmartComment item, int position) {
        mRows.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        mRows.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemViewType(int position) {
        return mRows.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    // 제네릭 형식의 변수로 ViewHolder를 생성 -----------------------------------------------------
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

    // 만들어진 ViewHolder에 데이터를 넣는 작업, ListView의 getView()와 동일 -----------------------------------------------------
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

            if( !row.strFileURL.isEmpty() ) {
                //Picasso.with(getContext()).load(row.strFileURL).fit().into(ivFile);
                new PicassoDelegate(ivFile, Picasso.with(getContext()).load(row.strFileURL).centerCrop());
                ivFile.setVisibility(View.VISIBLE);
            } else {
                ivFile.setVisibility(View.GONE);
            }

            tvWriter.setText(row.strWriter);
            tvDate.setText(row.datWrite);
            tvContent.setText(row.strContent);
            if (row.strWriter.equals(pref.getValue("pref_user_id",""))) {
                ivDelete.setVisibility(View.VISIBLE);
            } else {
                ivDelete.setVisibility(View.GONE);
            }

            Log.d(TAG, " 리스트 생성 : " +position+ " / " +row.datWrite+ " / " +row.strContent+ " / " +row.strFileURL+ " / ");

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);

            Log.d(TAG, " 리스트 생성 : 실패 !! ");
        }

    }

    // 데이터의 갯수 -----------------------------------------------------
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
    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener  {

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

            commentDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listenerX != null)
                        listenerX.onItemXClick(itemView, getLayoutPosition());
                }
            });
            /***************************************************************************/

            // 내부 이벤트 적용을 위한 코드 3-2
            //commentDelete.setOnClickListener(this);
            commentDelete.setOnLongClickListener(this);
        }

        // 내부 이벤트 적용을 위한 코드 3-3
        @Override
        public void onClick(View v) {
            if (v.getId() == commentDelete.getId()){

            }
            Toast.makeText(v.getContext(), "CLICK : " + String.valueOf(position) + " / " + String.valueOf(commentDelete.getId()), Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onLongClick(View v) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle ("Hello Dialog")
                    .setMessage ("LONG CLICK : " + String.valueOf(position) + " / " + String.valueOf(commentDelete.getId()))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            builder.create().show();
            return true;
        }
    }

    // PicassoDelegate 클래스 ###############################################################################
    public class PicassoDelegate {

        private RequestCreator mRequestCreator;

        public PicassoDelegate(ImageView target, RequestCreator requestCreator) {
            if (target.getWidth() > 0 && target.getHeight() > 0) {
                complete(target, requestCreator);
            } else {
                mRequestCreator = requestCreator;
                target.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        v.removeOnLayoutChangeListener(this);
                        complete((ImageView) v, mRequestCreator);
                    }
                });
            }
        }

        private void complete(ImageView target, RequestCreator requestCreator) {
            if (target.getWidth() > 0 && target.getHeight() > 0) {
                requestCreator.resize(target.getWidth(), target.getHeight());
            }
            requestCreator.into(target);
        }
    }
    // new PicassoDelegate(customerPhoto, Picasso.with(getActivity()).load(user.getPhotoUrl()).centerCrop());

}