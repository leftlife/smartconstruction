package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kr.koogle.android.smartconstruction.http.SmartBuild;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class SmartBuildAdapter extends RecyclerView.Adapter<SmartBuildAdapter.ViewHolder> {
    // Store a member variable for the smartBuilds
    private static ArrayList<SmartBuild> mRows; // SmartSingleton.smartBuilds -> smartBuilds 변경 사용할 경우 !!
    // Store the context for easy access
    private Context mContext;

    // Pass in the smartBuild array into the constructor
    public SmartBuildAdapter(Context context, ArrayList<SmartBuild> smartBuilds) {
        mContext = context;
        mRows = smartBuilds;
    }

    // Clean all elements of the recycler
    public void clear() {
        mRows.clear();
        notifyDataSetChanged();
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public SmartBuildAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View smartBuildView = inflater.inflate(R.layout.row_smart_build, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(getContext(), smartBuildView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(SmartBuildAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        SmartBuild smartBuild = mRows.get(position);

        // Set item views based on your views and data model
        ImageView image = viewHolder.image;
        TextView tvName = viewHolder.name;
        TextView tvPeriod = viewHolder.period;
        TextView tvAddress = viewHolder.address;
        TextView tvSize = viewHolder.size;
        TextView tvArea = viewHolder.area;

        tvName.setText(smartBuild.strName);
        tvPeriod.setText(smartBuild.strStartDate + " ~ " + smartBuild.strEndDate);
        tvAddress.setText(smartBuild.strAddress);
        tvSize.setText("지상 " + smartBuild.intBuildGround + "층 , 지하 " + smartBuild.intBuildBasement + "층");
        tvArea.setText(smartBuild.strBuildArea11 + " 제곱미터");

        if( !smartBuild.strImageURL.isEmpty() ) {
            Picasso.with(getContext())
                    .load(smartBuild.strImageURL)
                    .fit() // resize(700,400)
                    .into(image);
        } else {
            image.setImageResource(R.drawable.img_no_image);
        }
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mRows.size();
    }

    /***************************************************************************/
    private static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    /***************************************************************************/


    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder { // implements View.OnClickListener
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public ImageView image;
        public TextView name;
        public TextView period;
        public TextView address;
        public TextView size;
        public TextView area;

        private Context context;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(Context context, final View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.r_sb_image);
            name = (TextView) itemView.findViewById(R.id.r_sb_name);
            period = (TextView) itemView.findViewById(R.id.r_sb_period);
            address = (TextView) itemView.findViewById(R.id.r_sb_address);
            size = (TextView) itemView.findViewById(R.id.r_sb_size);
            area = (TextView) itemView.findViewById(R.id.r_sb_area);

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

        /*
        itemView.setOnClickListener(this);
        @Override
        public void onClick(View view) {
            int position = getLayoutPosition(); // get item position
            SmartBuild smartBuild = mRows.get(position);

            Toast.makeText(context, smartBuild.strName, Toast.LENGTH_LONG).show();
        }
        */
    }

}