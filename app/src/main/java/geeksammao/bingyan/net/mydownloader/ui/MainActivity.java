package geeksammao.bingyan.net.mydownloader.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import geeksammao.bingyan.net.mydownloader.BaseActivity;
import geeksammao.bingyan.net.mydownloader.R;
import geeksammao.bingyan.net.mydownloader.model.DownloadInfo;
import geeksammao.bingyan.net.mydownloader.network.MultiThreadManager;
import geeksammao.bingyan.net.mydownloader.network.OnDownloadCallback;
import geeksammao.bingyan.net.mydownloader.network.OnProgressUpdateCallback;
import geeksammao.bingyan.net.mydownloader.util.NetspeedDetector;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private CharSequence[] titles;
    private int threadNum;
    private List<DownloadInfo> downloadInfoList;
    private List<String> downItemNameList;
    private List<MultiThreadManager> downloaderList = new ArrayList<>();

    private NetspeedDetector netspeedDetector = new NetspeedDetector(this);

    private DrawerLayout drawerLayout;
    private CoordinatorLayout container;
    private ProgressDialog progressDialog;
    private RecyclerView downloadItemRecyclerView;
    private DownloadingItemAdapter recyclerAdapter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void initData() {
        titles = getResources().getStringArray(R.array.titles);
        threadNum = 3;
        // init the downloadInfoList if previous download exists
        downloadInfoList = new ArrayList<>();
        downItemNameList = new ArrayList<>();
        recyclerAdapter = new DownloadingItemAdapter(
                MainActivity.this, downloadInfoList
        );
    }

    @Override
    protected void initView() {
        // init toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        progressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.STYLE_SPINNER);

        container = (CoordinatorLayout) findViewById(R.id.content);
        downloadItemRecyclerView = (RecyclerView) findViewById(R.id.down_item_rv);
        downloadItemRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        downloadItemRecyclerView.setAdapter(recyclerAdapter);
//        downloadItemRecyclerView.setAdapter();

        // init drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        // init the first drawer item as the fragment
        setFragmentItem(0);
        // init fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLinkInputDialog();
            }
        });
    }

    private void showLinkInputDialog() {
        Log.e("", "dialog");

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_layout
                , null);
        final EditText editText = (EditText) dialogLayout.findViewById(R.id.dialog_edittext);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.input_dialog_title).setView(dialogLayout).
                setPositiveButton(getString(R.string.dialog_positive),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startDownload(editText);
                            }
                        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startDownload(final EditText editText) {
        String link = editText.getText().toString();
        File saveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        final MultiThreadManager downloadManager = new MultiThreadManager(threadNum, link, saveDir, MainActivity.this);
        downloaderList.add(downloadManager);

        downloadManager.fetchDownloadFileLength(new OnDownloadCallback() {
            @Override
            public void onPreDownload() {
                progressDialog.setTitle("Download info");
                progressDialog.setMessage("Start downloading");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            public void onDownloadStart() {
                progressDialog.dismiss();

                downloadManager.download(new OnProgressUpdateCallback() {
                    @Override
                    public void setProgress(int progress) {
                        int position = 0;

                        DownloadInfo downloadInfo = new DownloadInfo();
                        downloadInfo.progress = progress;
                        downloadInfo.url = editText.getText().toString();
                        downloadInfo.fileName = downloadManager.getFileName();
                        downloadInfo.fileSize = downloadManager.getFileLength();
                        downloadInfo.downloadSpeed = netspeedDetector.getNetworkSpeed();

                        // if not finish downloading
                        // then update the progress
                        if (progress < 100) {
                            downloadInfo.downloadState = DownloadInfo.DOWNLOAD_ONGOING;
                            // need to deal with image file
                            if (!downItemNameList.contains(downloadInfo.fileName)) {
                                downItemNameList.add(downloadInfo.fileName);
                                downloadInfoList.add(downloadInfo);
                            } else {
                                position = downItemNameList.indexOf(downloadInfo.fileName);
                                downloadInfoList.set(position, downloadInfo);
                            }

                            if (downloadItemRecyclerView.getAdapter() == null) {
                                recyclerAdapter = new DownloadingItemAdapter(
                                        MainActivity.this, downloadInfoList
                                );
                                downloadItemRecyclerView.setAdapter(recyclerAdapter);
                            } else {
                                recyclerAdapter.setDownloadInfoList(downloadInfoList);
                                recyclerAdapter.notifyItemChanged(position);
                            }
                        } else {
                            downloadInfo.downloadState = DownloadInfo.DOWNLOAD_FINISH;
                            position = downItemNameList.indexOf(downloadInfo.fileName);
                            downloadInfoList.set(position, downloadInfo);

                            recyclerAdapter.setDownloadInfoList(downloadInfoList);
                            recyclerAdapter.notifyItemChanged(position);
                        }
                    }
                });
            }

            @Override
            public void onDownloadError() {
                progressDialog.setCancelable(true);
                progressDialog.dismiss();

                Snackbar.make(container, "Download failed.", Snackbar.LENGTH_LONG).
                        setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startDownload(editText);
                            }
                        }).show();
            }
        });
    }

    private void setFragmentItem(int position) {
        setTitle(titles[position]);

        Fragment contentFragment = null;
        switch (position) {
            case 0:
                contentFragment = DownloadingFragment.newInstance(R.id.drawer_downloading);
                break;
            case 1:
                contentFragment = DownloadedFragment.newInstance();
                break;
            case 2:
                contentFragment = SettingFragment.newInstance();
                break;
        }
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.main_container, contentFragment).commit();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        drawerLayout.closeDrawers();

        switch (menuItem.getItemId()) {
            case R.id.drawer_downloading:
                setFragmentItem(0);
                break;
            case R.id.drawer_downloaded:
                setFragmentItem(1);
                break;
            case R.id.drawer_setting:
                setFragmentItem(2);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (MultiThreadManager downloader : downloaderList) {
            downloader.setIsExist(false);
            downloader.shutdownNow();
        }

    }
}
