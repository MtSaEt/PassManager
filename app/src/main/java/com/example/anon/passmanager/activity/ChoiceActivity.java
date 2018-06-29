package com.example.anon.passmanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.anon.passmanager.R;

import developer.shivam.library.DiagonalView;

public class ChoiceActivity extends AppCompatActivity {
    private DiagonalView diagonalView;
    private FloatingActionButton fabNext;
    private Button pinToggle, pwdToggle;
    private boolean pinChecked = false, isShown = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        
        //btnNext = (Button) findViewById(R.id.btn_next);
        fabNext = (FloatingActionButton) findViewById(R.id.fab_next);
        pinToggle = (Button) findViewById(R.id.pin_toggle);
        pwdToggle = (Button) findViewById(R.id.pwd_toggle);
        
        pinToggle.setOnClickListener(btnListener);
        pwdToggle.setOnClickListener(btnListener);
        fabNext.setOnClickListener(btnListener);

        //btnNext.setOnClickListener(btnListener);
    }

    private void toggleView() {
        if (pinChecked) {
            fabNext.hide();
            startTimer(pinChecked);
            
            pinToggle.setBackground(getResources().getDrawable(R.drawable.choice_title));
            pwdToggle.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            
            pinToggle.setTextColor(getResources().getColor(R.color.white));
            pwdToggle.setTextColor(getResources().getColor(android.R.color.black));
        } else {
            fabNext.hide();
            startTimer(pinChecked);
            
            pwdToggle.setBackground(getResources().getDrawable(R.drawable.choice_title));
            pinToggle.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            
            pwdToggle.setTextColor(getResources().getColor(R.color.white));
            pinToggle.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    private void startTimer(final boolean bool) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fabNext.show();
                if (bool) {
                    fabNext.setImageResource(R.drawable.ic_fiber_pin_white_24dp);
                } else {
                    fabNext.setImageResource(R.drawable.ic_sort_by_alpha_white_24dp);
                }
            }
        }, 200);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                case R.id.pin_toggle:
                    pinChecked = true;
                    toggleView();
                    pinToggle.setOnClickListener(null);
                    pwdToggle.setOnClickListener(btnListener);
                    break;
                case R.id.pwd_toggle:
                    pinChecked = false;
                    toggleView();
                    pinToggle.setOnClickListener(btnListener);
                    pwdToggle.setOnClickListener(null);
                    break;
                case R.id.fab_next:
                    if (pinChecked) {
                        intent = new Intent(ChoiceActivity.this, PinActivity.class);
                    } else {
                        intent = new Intent(ChoiceActivity.this, AlphanumericActivity.class);
                    }
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
                    break;
            }
        }
    };
}
