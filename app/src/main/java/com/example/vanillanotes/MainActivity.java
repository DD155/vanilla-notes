package com.example.vanillanotes;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<TextView> textViewList = new ArrayList<>();
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final LinearLayout linear = findViewById(R.id.linear);

        Button b = findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView text = new TextView(getApplicationContext());
                text.setTextSize(15);
                text.setBackgroundResource(R.drawable.back);
                text.setText("Hello");
                text.setWidth(1500);
                text.setPadding(5, 70, 5, 70);
                textViewList.add(text);
                linear.addView(textViewList.get(textViewList.size()-1));
            }
        });
        /*
        for(int i = 0; i < textViewList.size(); i++) {

            textViewArray[i] = new TextView(this);
            //textViewArray[i].setGravity(Gravity.CENTER);
            textViewArray[i].setTextSize(15);
            textViewArray[i].setBackgroundResource(R.drawable.back);
            //textViewArray[i].setBackgroundColor(Color.DKGRAY);
            textViewArray[i].setText("Hello");
            textViewArray[i].setWidth(1500);
            textViewArray[i].setPadding(5,70,5,70);


            linear.addView(textViewList.get(i));
        }*/
    }

    public void displayText(LinearLayout v){
        for(int i = 0; i < textViewList.size(); i++) {
            v.addView(textViewList.get(i));
        }
    }


}
