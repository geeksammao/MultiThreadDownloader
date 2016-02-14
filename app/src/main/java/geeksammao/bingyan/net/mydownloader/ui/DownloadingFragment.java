package geeksammao.bingyan.net.mydownloader.ui;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import geeksammao.bingyan.net.mydownloader.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadingFragment extends Fragment {
    public DownloadingFragment() {
        // Required empty public constructor
    }

    public static DownloadingFragment newInstance(int id){
        DownloadingFragment fragment = new DownloadingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("fragment_id", id);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_content, container, false);

        int fragmentId = getArguments().getInt("fragment_id");

        return rootView;
    }


}
