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
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
    private List<String> downItemUrlList;
    private List<MultiThreadManager> downloaderList = new ArrayList<>();

    private NetspeedDetector netspeedDetector = new NetspeedDetector(this);

    private DrawerLayout drawerLayout;
    private CoordinatorLayout container;
    private ProgressDialog progressDialog;
    private RecyclerView downloadItemRecyclerView;
    private DownloadingItemAdapter recyclerAdapter;
    private MultiThreadManager downloadManager;

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
        downItemUrlList = new ArrayList<>();
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
        RecyclerView.ItemAnimator itemAnimator = downloadItemRecyclerView.getItemAnimator();
        if (itemAnimator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) itemAnimator).setSupportsChangeAnimations(false);
        }
        downloadItemRecyclerView.setAdapter(recyclerAdapter);

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

//        final MultiThreadManager downloadManager = new MultiThreadManager(threadNum, link, saveDir, MainActivity.this);
        if (downloadManager == null){
            downloadManager = new MultiThreadManager(threadNum, link, saveDir, MainActivity.this);
        } else {
            downloadManager.setTargetUrl(link);
        }

//        downloaderList.add(downloadManager);

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

//                final DownloadInfo downloadInfo = new DownloadInfo();
//                downloadInfo.url = editText.getText().toString();

                downloadManager.download(new OnProgressUpdateCallback() {
                    @Override
                    public void setProgress(int progress) {
                        int position = 0;
                        DownloadInfo downloadInfo = new DownloadInfo();
                        downloadInfo.url = editText.getText().toString();
                        downloadInfo.progress = progress;
                        downloadInfo.downloadSpeed = netspeedDetector.getNetworkSpeed();
                        downloadInfo.fileName = downloadManager.getFileName();
                        downloadInfo.fileSize = downloadManager.getFileLength();

                        // if not finish downloading
                        // then update the progress
                        if (progress < 100) {
                            downloadInfo.downloadState = DownloadInfo.DOWNLOAD_ONGOING;
                            // need to deal with image file
                            if (!downItemUrlList.contains(downloadInfo.url)) {
                                downItemUrlList.add(downloadInfo.url);
                                downloadInfoList.add(downloadInfo);
                            } else {
                                position = downItemUrlList.indexOf(downloadInfo.url);
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
                            position = downItemUrlList.indexOf(downloadInfo.url);
                            downloadInfoList.set(position, downloadInfo);

                            recyclerAdapter.setDownloadInfoList(downloadInfoList);
                            recyclerAdapter.notifyItemChanged(position);
                        }
                    }

                    @Override
                    public void onFail(String url) {
//                        downloaderList.get()
                        int position = downItemUrlList.indexOf(url);

                        DownloadInfo downloadInfo = new DownloadInfo();
                        downloadInfo.url = url;
                        downloadInfo.fileName = downloadManager.getFileName();
                        downloadInfo.fileSize = downloadManager.getFileLength();
                        downloadInfo.downloadState = DownloadInfo.DOWNLOAD_FAIL;

                        downloadInfoList.set(position,downloadInfo);
                        recyclerAdapter.setDownloadInfoList(downloadInfoList);
                        recyclerAdapter.notifyItemChanged(position);

                        Toast.makeText(MainActivity.this,url + " download failed",Toast.LENGTH_LONG).show();
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
