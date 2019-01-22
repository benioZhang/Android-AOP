package com.benio.aop.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.benio.binder.BindView;

public class SecondActivity extends AppCompatActivity {

    @BindView(R.id.content)
    LinearLayout mContentView;

    @BindView(R.id.title)
    TextView mTitleView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }
}
