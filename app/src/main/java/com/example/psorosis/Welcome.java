package com.example.psorosis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class Welcome extends AppCompatActivity {

    ImageView next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        next = findViewById(R.id.next_page_button_id);
        Intent intent = new Intent( this, MainActivity.class);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
                overridePendingTransition(R.anim.left_slide_in,R.anim.right_slide_out);
            }
        });
    }
}