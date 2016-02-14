package geeksammao.bingyan.net.mydownloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init(){
        setContentView();
        initData();
        initView();
    }

    protected abstract void setContentView();
    protected abstract void initData();
    protected abstract void initView();

}
