package kr.koogle.android.smartconstruction;

import android.app.Activity;
import android.app.ActivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by LeeSungWoo on 2016-06-28.
 */
public class OneFragment extends Fragment {

    //  ############## Fragment 통신 ##################  //
    OnHeadlineSelectedListener mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText("1111111111111111");

        return rootView;
    }

    //  ############## Fragment 통신 ##################  //
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnHeadlineSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement OnHeadlineSelectedListener");
        }
    }

    //  ############## Fragment 통신 ##################  //
    public interface OnHeadlineSelectedListener {
        public void onArticleSelected(int position);
    }

}
