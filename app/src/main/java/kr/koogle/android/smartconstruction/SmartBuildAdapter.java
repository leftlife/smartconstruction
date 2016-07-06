package kr.koogle.android.smartconstruction;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import kr.koogle.android.smartconstruction.http.SmartBuild;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class SmartBuildAdapter extends RecyclerView.Adapter<SmartBuildAdapter.ViewHolder> {
    // Store a member variable for the smartBuilds
    private static ArrayList<SmartBuild> mSmartBuilds;
    // Store the context for easy access
    private Context mContext;

    // Pass in the smartBuild array into the constructor
    public SmartBuildAdapter(Context context, ArrayList<SmartBuild> smartBuilds) {
        mContext = context;
        mSmartBuilds = smartBuilds;
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
        View smartBuildView = inflater.inflate(R.layout.row_smartbuild, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(getContext(), smartBuildView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(SmartBuildAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        SmartBuild smartBuild = mSmartBuilds.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(smartBuild.strName);
        Button button = viewHolder.messageButton;
        button.setText("Message");
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mSmartBuilds.size();
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
    public static class ViewHolder extends RecyclerView.ViewHolder  { // implements View.OnClickListener
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public Button messageButton;

        private Context context;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(Context context, final View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.contact_name);
            messageButton = (Button) itemView.findViewById(R.id.message_button);

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
            SmartBuild smartBuild = mSmartBuilds.get(position);

            Toast.makeText(context, smartBuild.strName, Toast.LENGTH_LONG).show();
        }
        */
    }

}