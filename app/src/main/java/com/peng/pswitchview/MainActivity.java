package com.peng.pswitchview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.peng.pswitch.PSwitchView;

public class MainActivity extends AppCompatActivity {

    private PSwitchView mViewSwitch1;
    private PSwitchView mViewSwitch2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mViewSwitch1 = findViewById(R.id.view_switch1);
        mViewSwitch2 = findViewById(R.id.view_switch2);

        mViewSwitch1.setOnSwitchCheckListener(new PSwitchView.SwitchCheckListener() {
            @Override
            public void onCheckedChanged(boolean isChecked) {
                Toast.makeText(MainActivity.this, "mViewSwitch1 切换:" + isChecked, Toast.LENGTH_SHORT).show();
            }
        });

        //状态设置, 以下方式都可以
        //mViewSwitch1.toggle();
        mViewSwitch1.setChecked(true);
    }

    public void toggleTest(View view) {
        mViewSwitch1.toggle();
        mViewSwitch2.toggle();
    }


}