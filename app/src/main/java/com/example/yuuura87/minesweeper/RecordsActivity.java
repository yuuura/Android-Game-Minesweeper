package com.example.yuuura87.minesweeper;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;

import java.util.ArrayList;

public class RecordsActivity extends AppCompatActivity implements FragmentTable.FragmentTableListener {

    private Button btnEasy, btnNormal, btnExpert;
    private Store store;
    private FragmentTable fragmentTable;
    private FragmentMap fragmentMap;
    private final String COL_1 = "#fdd561";     //yellow
    private final String COL_2 = "#d0d3d4";     //grey

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(store.getRecordsArrEasy().size() != 0) {
            btnEasy.callOnClick();
        }
        else if(store.getRecordsArrNormal().size() != 0) {
            btnNormal.callOnClick();
        }
        else {
            btnExpert.callOnClick();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        String json = new Gson().toJson(store);
        getSharedPreferences("Store", MODE_PRIVATE).edit().putString("store", json).apply();
    }

    private void setListeners() {
        btnEasy.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEasy.setBackgroundColor(Color.parseColor(COL_1));
                btnNormal.setBackgroundColor(Color.parseColor(COL_2));
                btnExpert.setBackgroundColor(Color.parseColor(COL_2));
                fillTable(1);
            }
        });

        btnNormal.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEasy.setBackgroundColor(Color.parseColor(COL_2));
                btnNormal.setBackgroundColor(Color.parseColor(COL_1));
                btnExpert.setBackgroundColor(Color.parseColor(COL_2));
                fillTable(2); }
        });

        btnExpert.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEasy.setBackgroundColor(Color.parseColor(COL_2));
                btnNormal.setBackgroundColor(Color.parseColor(COL_2));
                btnExpert.setBackgroundColor(Color.parseColor(COL_1));
                fillTable(3); }
        });
    }

    @Override
    public void sendRecordToMap(String name, int time, String address, double[] place) {
        fragmentMap.setPosition(name, time, address, place);
    }

    private void fillTable(int level){
        fragmentTable.deleteAllRows();
        int cnt = 0;
        ArrayList<Person> arr = null;
        switch(level) {
            case 1:
                cnt = store.getRecordsArrEasy().size();
                arr = store.getRecordsArrEasy();
                break;
            case 2:
                cnt = store.getRecordsArrNormal().size();
                arr = store.getRecordsArrNormal();
                break;
            case 3:
                cnt = store.getRecordsArrExpert().size();
                arr = store.getRecordsArrExpert();
                break;
        }
        for(int i = 0; i < cnt; i++) {
            fragmentTable.addRow(arr.get(i).getName(), arr.get(i).getTime(), arr.get(i).getAddress(), i, arr.get(i).getPlace());
        }
        fragmentMap.setArray(arr, cnt);
    }

    private void init(){
        btnEasy = (Button) findViewById(R.id.btnEasy);
        btnNormal = (Button) findViewById(R.id.btnNormal);
        btnExpert = (Button) findViewById(R.id.btnExpert);
        fragmentMap = (FragmentMap) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        fragmentTable = (FragmentTable) getSupportFragmentManager().findFragmentById(R.id.fragmentTable);
        store = (Store) getIntent().getSerializableExtra("Store");
        fillTable(store.getLevel());
        setListeners();
    }
}
