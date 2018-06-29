package com.example.anon.passmanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.anon.passmanager.R;

public class SetupActivity extends AppCompatActivity {

    private Button startSetup;
    private ImageView logo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        startSetup = (Button) findViewById(R.id.start_setup);
        startSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Intent intent = new Intent(SetupActivity.this, ChoiceActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
            }
        });
    }
}
