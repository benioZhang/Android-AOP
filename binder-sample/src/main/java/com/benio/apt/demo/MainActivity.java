package com.benio.apt.demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.benio.binder.BindView;
import com.benio.binder.OnClick;
import com.benio.binder.ViewBinder;

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
        ViewBinder.bind(this);
        mContentView.setBackgroundColor(Color.YELLOW);
        mTitleView.setText("I'm a title");
        mImageView.setAlpha(0.5f);
        mButton.setText("Start");
    }

    @OnClick({R.id.image, R.id.button})
    void onClick(View view) {
        int id = view.getId();
        if (id == R.id.image) {
            Toast.makeText(this, "click image", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.button) {
            startActivity(new Intent(MainActivity.this, SecondActivity.class));
        }
    }
}
