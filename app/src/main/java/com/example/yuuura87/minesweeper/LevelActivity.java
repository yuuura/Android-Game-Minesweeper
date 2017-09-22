package com.example.yuuura87.minesweeper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

public class LevelActivity extends AppCompatActivity {

    private Button btnStart, btnRecords;
    private RadioGroup radioLevelGroup;
    private Store store;
    private int level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            String json = getSharedPreferences("Store", MODE_PRIVATE).getString("store", null);
            store = new Gson().fromJson(json, Store.class);
            setCheckBox(store.getLevel());
        }catch(Exception e){
            radioLevelGroup.check(R.id.radioBtnNormal);
            store = new Store();
            Toast.makeText(getApplicationContext(),"No file loaded",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        String json = new Gson().toJson(store);
        getSharedPreferences("Store", MODE_PRIVATE).edit().putString("store", json).apply();
    }

    private void startGameActivity(){
        Intent game = new Intent(this, GameActivity.class);
        game.putExtra("Store", store);
        startActivity(game);
    }

    private void startRecordsActivity() {
        Intent records = new Intent(this, RecordsActivity.class);
        records.putExtra("Store", store);
        startActivity(records);
    }

    private void setListeners(){
        btnStart.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                setLevel();
                startGameActivity();
            }
        });

        btnRecords.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                setLevel();
                startRecordsActivity();
            }
        });
    }

    private void setCheckBox(int level) {
        switch(level) {
            case 1:
                radioLevelGroup.check(R.id.radioBtnEasy);
                break;
            case 2:
                radioLevelGroup.check(R.id.radioBtnNormal);
                break;
            case 3:
                radioLevelGroup.check(R.id.radioBtnExpert);
                break;
        }
    }
    private void setLevel() {
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
        store.setLevel(level);
    }

    private void init(){
        btnStart = (Button)findViewById(R.id.btnStart);
        btnRecords = (Button)findViewById(R.id.btnRecords);
        radioLevelGroup = (RadioGroup) findViewById(R.id.radioLevel);
        radioLevelGroup.check(R.id.radioBtnNormal);
        setListeners();
    }
}
