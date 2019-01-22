package com.benio.aop.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.benio.binder.BindView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.content)
    LinearLayout mContentView;

    @BindView(R.id.title)
    TextView mTitleView;

    @BindView(R.id.image)
    ImageView mImageView;

    @BindView(R.id.button)
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
