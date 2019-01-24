package com.benio.aop.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.benio.binder.BindView;
import com.benio.binder.ViewBinder;

public class SecondActivity extends AppCompatActivity {

    @BindView(R.id.content)
    ViewGroup mContentView;

    @BindView(R.id.title)
    TextView mTitleView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        ViewBinder.bind(this);
        mContentView.setBackgroundColor(Color.GREEN);
        mTitleView.setText("SecondActivity");
    }
}
