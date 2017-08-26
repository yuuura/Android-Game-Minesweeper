package com.example.yuuura87.minesweeper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Button;
import com.google.gson.Gson;

public class LevelActivity extends AppCompatActivity {

    private Button btnStart;
    private RadioGroup radioLevelGroup;
    private int level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Store store = new Store();
        store.setLevel(radioLevelGroup.getCheckedRadioButtonId());
        String json = new Gson().toJson(store);
        getSharedPreferences("Store", MODE_PRIVATE).edit().putString("store", json).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            String json = getSharedPreferences("Store", MODE_PRIVATE).getString("store", null);
            Store store = new Gson().fromJson(json, Store.class);
            radioLevelGroup.check(store.getLevel());
        }catch(Exception e){}
    }

    private void startGameActivity(int selection){
        Intent game = new Intent(this, GameActivity.class);
        game.putExtra("level", selection);
        startActivity(game);
    }

    private void setListeners(){
        btnStart.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                switch(radioLevelGroup.getCheckedRadioButtonId()){
                    case R.id.radioBtnEasy:
                        level = 1;
                        break;
                    case R.id.radioBtnNormal:
                        level = 2;
                        break;
                    case R.id.radioBtnExpert:
                        level = 3;
                        break;
                    default: level = 0;
                }
                startGameActivity(level);
            }
        });
    }

    private void init(){
        btnStart =  (Button)findViewById(R.id.btnStart);
        radioLevelGroup = (RadioGroup) findViewById(R.id.radioLevel);
        radioLevelGroup.check(R.id.radioBtnNormal);
        setListeners();
    }
}
