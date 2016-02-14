package geeksammao.bingyan.net.mydownloader.ui;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import geeksammao.bingyan.net.mydownloader.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DownloadedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DownloadedFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static DownloadedFragment newInstance() {
        DownloadedFragment fragment = new DownloadedFragment();
        return fragment;
    }

    public DownloadedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_downloaded, container, false);
    }


}
