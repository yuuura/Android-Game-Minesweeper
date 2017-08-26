package com.example.yuuura87.minesweeper;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    private int level;
    private int cntMines;
    private final int[] easyMINE = {5, 10, 10};                 // {mines, x, y}
    private final int[] normalMINE = {10 ,10, 10};
    private final int[] expertMINE = {10, 5, 5};
    private int[][] arrMines;
    private GridView gridView;
    private volatile boolean contGame;
    private boolean timer;
    private TextView txtTimer, txtMines;
    private int counter;
    private Thread threadTimer;
    private Button btnSmile;
    private Integer[] images = {R.drawable.one, R.drawable.two, R.drawable.three, R.drawable.four, R.drawable.five, R.drawable.six, R.drawable.seven, R.drawable.eight,
            R.drawable.to_click, R.drawable.empty, R.drawable.flag, R.drawable.mine, R.drawable.mine_boom};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        btnSmile = (Button) findViewById(R.id.btnSmile);
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setBackgroundColor(Color.BLACK);

        txtTimer = (TextView) findViewById(R.id.timer);
        txtMines = (TextView) findViewById(R.id.cntMines);
        Bundle levelsData = getIntent().getExtras();
        if(levelsData == null)
            return;
        level = levelsData.getInt("level");
        buildGame();
        setListener();
        setTimer();
    }

    // CODES: 0 - empty, 1 - mine, 2 - flag on empty, 3 - flag on mine, 4 - opened
    void onClick(View view, int position) {
        int i = position/arrMines.length;
        int j = position%arrMines.length;

        if(arrMines[i][j] == 1) {                   // Lost! End game.
            ((ImageView) view).setImageResource(R.drawable.mine_boom);
            endGame(false);
        }
        else if(arrMines[i][j] == 0) {
            spread(view ,i, j);
        }
    }

    void endGame(boolean state) {
        contGame = false;
        timer = false;
        threadTimer.interrupt();
        if(state)
            btnSmile.setBackgroundResource(R.drawable.glasses);
        else btnSmile.setBackgroundResource(R.drawable.dead);
    }

    void prepareNewGame() {
        buildGame();
    }

    void spread(View view, int i, int j) {

        int mult = arrMines.length;

        if(checkOneCell(view, i, j))
            return;

        if (i != 0 && arrMines[i - 1][j] != 4 && arrMines[i - 1][j] != 2 && arrMines[i - 1][j] != 3)
            spread(gridView.getChildAt((i - 1)*mult + j), i - 1, j);
        if (i != 0 && j != arrMines.length-1 && arrMines[i - 1][j + 1] != 4 && arrMines[i - 1][j + 1] != 2 && arrMines[i - 1][j + 1] != 3)
            spread(gridView.getChildAt((i - 1)*mult + j + 1), i - 1, j + 1);
        if (j != arrMines.length-1 && arrMines[i][j + 1] != 4 && arrMines[i][j + 1] != 2 && arrMines[i][j + 1] != 3)
            spread(gridView.getChildAt(i*mult + j + 1), i, j + 1);
        if (i != arrMines.length-1 && j != arrMines.length-1 && arrMines[i + 1][j + 1] != 4 && arrMines[i + 1][j + 1] != 2 && arrMines[i + 1][j + 1] != 3)
            spread(gridView.getChildAt((i + 1)*mult + j + 1), i + 1, j + 1);
        if (i != arrMines.length-1 && arrMines[i + 1][j] != 4 && arrMines[i + 1][j] != 2 && arrMines[i + 1][j] != 3)
            spread(gridView.getChildAt((i + 1)*mult + j), i + 1, j);
        if (i != arrMines.length-1 && j > 0 && arrMines[i + 1][j - 1] != 4 && arrMines[i + 1][j - 1] != 2 && arrMines[i + 1][j - 1] != 3)
            spread(gridView.getChildAt((i + 1)*mult + j - 1), i + 1, j - 1);
        if (j > 0 && arrMines[i][j - 1] != 4 && arrMines[i][j - 1] != 2 && arrMines[i][j - 1] != 3)
            spread(gridView.getChildAt(i*mult + j - 1), i, j - 1);
        if (i > 0 && j > 0 && arrMines[i - 1][j - 1] != 4 && arrMines[i - 1][j - 1] != 2 && arrMines[i - 1][j - 1] != 3)
            spread(gridView.getChildAt((i - 1)*mult + j - 1), i - 1, j - 1);
    }

    boolean checkOneCell(View view, int i, int j) {

        int countMines = 0;

        if(i == 0 && j == 0) {
            if(arrMines[i][j+1] == 1 || arrMines[i][j+1] == 3) { countMines++; }
            if(arrMines[i+1][j+1] == 1 || arrMines[i+1][j+1] == 3) { countMines++; }
            if(arrMines[i+1][j] == 1 || arrMines[i+1][j] == 3) { countMines++; }
        }
        else if(i == 0 && j < arrMines.length - 1) {
            if(arrMines[i][j-1] == 1 || arrMines[i][j-1] == 3) { countMines++; }
            if(arrMines[i+1][j-1] == 1 || arrMines[i+1][j-1] == 3) { countMines++; }
            if(arrMines[i+1][j] == 1 || arrMines[i+1][j] == 3) { countMines++; }
            if(arrMines[i+1][j+1] == 1 || arrMines[i+1][j+1] == 3) { countMines++; }
            if(arrMines[i][j+1] == 1 || arrMines[i][j+1] == 3) { countMines++; }
        }
        else if (i == 0 && j == arrMines.length - 1) {
            if(arrMines[i][j-1] == 1 || arrMines[i][j-1] == 3) { countMines++; }
            if(arrMines[i+1][j-1] == 1 || arrMines[i+1][j-1] == 3) { countMines++; }
            if(arrMines[i+1][j] == 1 || arrMines[i+1][j] == 3) { countMines++; }
        }
        else if(i < arrMines.length-1 && j == 0) {
            if(arrMines[i-1][j] == 1 || arrMines[i-1][j] == 3) { countMines++; }
            if(arrMines[i-1][j+1] == 1 || arrMines[i-1][j+1] == 3) { countMines++; }
            if(arrMines[i][j+1] == 1 || arrMines[i][j+1] == 3) { countMines++; }
            if(arrMines[i+1][j+1] == 1 || arrMines[i+1][j+1] == 3) { countMines++; }
            if(arrMines[i+1][j] == 1 || arrMines[i+1][j] == 3) { countMines++; }
        }
        else if(i < arrMines.length-1 && j < arrMines.length - 1) {
            if(arrMines[i-1][j] == 1 || arrMines[i-1][j] == 3) { countMines++; }
            if(arrMines[i-1][j+1] == 1 || arrMines[i-1][j+1] == 3) { countMines++; }
            if(arrMines[i][j+1] == 1 || arrMines[i][j+1] == 3) { countMines++; }
            if(arrMines[i+1][j+1] == 1 || arrMines[i+1][j+1] == 3) { countMines++; }
            if(arrMines[i+1][j] == 1 || arrMines[i+1][j] == 3) { countMines++; }
            if(arrMines[i+1][j-1] == 1 || arrMines[i+1][j-1] == 3) { countMines++; }
            if(arrMines[i][j-1] == 1 || arrMines[i][j-1] == 3) { countMines++; }
            if(arrMines[i-1][j-1] == 1 || arrMines[i-1][j-1] == 3) { countMines++; }
        }
        else if(i < arrMines.length-1 && j == arrMines.length - 1) {
            if(arrMines[i-1][j] == 1 || arrMines[i-1][j] == 3) { countMines++; }
            if(arrMines[i-1][j-1] == 1 || arrMines[i-1][j-1] == 3) { countMines++; }
            if(arrMines[i][j-1] == 1 || arrMines[i][j-1] == 3) { countMines++; }
            if(arrMines[i+1][j-1] == 1 || arrMines[i+1][j-1] == 3) { countMines++; }
            if(arrMines[i+1][j] == 1 || arrMines[i+1][j] == 3) { countMines++; }
        }
        else if(i == arrMines.length-1 && j == 0) {
            if(arrMines[i-1][j] == 1 || arrMines[i-1][j] == 3) { countMines++; }
            if(arrMines[i-1][j+1] == 1 || arrMines[i-1][j+1] == 3) { countMines++; }
            if(arrMines[i][j+1] == 1 || arrMines[i][j+1] == 3) { countMines++; }
        }
        else if(i == arrMines.length-1 && j < arrMines.length - 1) {
            if(arrMines[i][j-1] == 1 || arrMines[i][j-1] == 3) { countMines++; }
            if(arrMines[i-1][j-1] == 1 || arrMines[i-1][j-1] == 3) { countMines++; }
            if(arrMines[i-1][j] == 1 || arrMines[i-1][j] == 3) { countMines++; }
            if(arrMines[i-1][j+1] == 1 || arrMines[i-1][j+1] == 3) { countMines++; }
            if(arrMines[i][j+1] == 1 || arrMines[i][j+1] == 3) { countMines++; }
        }
        else if(i == arrMines.length-1 && j == arrMines.length - 1) {
            if(arrMines[i][j-1] == 1 || arrMines[i][j-1] == 3) { countMines++; }
            if(arrMines[i-1][j-1] == 1 || arrMines[i-1][j-1] == 3) { countMines++; }
            if(arrMines[i-1][j] == 1 || arrMines[i-1][j] == 3) { countMines++; }
        }

        switch(countMines) {
            case 0: ((ImageView) view).setImageResource(R.drawable.empty);
                arrMines[i][j] = 4;
                return false;
            case 1:  ((ImageView) view).setImageResource(R.drawable.one);
                break;
            case 2:  ((ImageView) view).setImageResource(R.drawable.two);
                break;
            case 3:  ((ImageView) view).setImageResource(R.drawable.three);
                break;
            case 4:  ((ImageView) view).setImageResource(R.drawable.four);
                break;
            case 5:  ((ImageView) view).setImageResource(R.drawable.five);
                break;
            case 6:  ((ImageView) view).setImageResource(R.drawable.six);
                break;
            case 7: ((ImageView) view).setImageResource(R.drawable.seven);
                break;
            case 8: ((ImageView) view).setImageResource(R.drawable.eight);
        }
        arrMines[i][j] = 4;
        return true;
    }

    void setListener(){

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(view instanceof ImageView && contGame) {
                    if(!timer) threadTimer.start();
                    timer = true;
                    onClick(view, position);
                }
            }
        });
        gridView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
                if(view instanceof ImageView && contGame) {
                    int i = position/arrMines.length;
                    int j = position%arrMines.length;
                    if(arrMines[i][j] == 2 || arrMines[i][j] == 3) {
                        if(arrMines[i][j] == 2) arrMines[i][j] = 0;
                        else if(arrMines[i][j] == 3) arrMines[i][j] = 1;
                        ((ImageView) view).setImageResource(R.drawable.to_click);
                        int numMines = Integer.parseInt(txtMines.getText().toString());
                        txtMines.setText(String.valueOf(numMines+1));
                        if(arrMines[i][j] == 1) cntMines++;
                        Toast.makeText(GameActivity.this, "Flag UNSET", Toast.LENGTH_SHORT).show();
                    }
                    else if(arrMines[i][j] == 0 || arrMines[i][j] == 1) {
                        if(arrMines[i][j] == 0) arrMines[i][j] = 2;
                        else if(arrMines[i][j] == 1) arrMines[i][j] = 3;
                        ((ImageView) view).setImageResource(R.drawable.flag);
                        int numMines = Integer.parseInt(txtMines.getText().toString());
                        txtMines.setText(String.valueOf(numMines-1));
                        if(arrMines[i][j] == 3)
                            cntMines--;
                        Toast.makeText(GameActivity.this, "Flag SET", Toast.LENGTH_SHORT).show();
                    }
                    if(!timer) threadTimer.start();
                    timer = true;
                    if(cntMines == 0)                 // Win! End game.
                        endGame(true);
                }
                return true;
            }
        });

        btnSmile.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer = false;
                threadTimer.interrupt();
                prepareNewGame();
            }
        });
    }

    void setTimer() {

        threadTimer = new Thread() {
            @Override
            public void run() {
                while(timer) {
                    try{
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                counter++;
                                txtTimer.setText(String.valueOf(counter));
                            }
                        });
                    }catch(InterruptedException e){}
                }
            }
        };
    }

    void buildGame() {
        btnSmile.setBackgroundResource(R.drawable.smile);
        switch(level) {
            case 1:                     // Easy
                cntMines = easyMINE[0];
                arrMines = new int[easyMINE[1]][easyMINE[2]];
                gridView.setAdapter(new GridAdapter(this, images[8], easyMINE[1]*easyMINE[2]));
                gridView.setNumColumns(easyMINE[1]);// Width(easyMINE[2]);
                txtMines.setText(String.valueOf(easyMINE[0]));
                createMineArray(easyMINE);

                break;
            case 2:                     // Normal
                cntMines = normalMINE[0];
                arrMines = new int[normalMINE[1]][normalMINE[2]];
                gridView.setAdapter(new GridAdapter(this, images[8], normalMINE[1]*normalMINE[2]));
                gridView.setNumColumns(normalMINE[1]);
                txtMines.setText(String.valueOf(normalMINE[0]));
                createMineArray(normalMINE);
                break;
            case 3:                     // Expert
                cntMines = expertMINE[0];
                arrMines = new int[expertMINE[1]][expertMINE[2]];
                gridView.setAdapter(new GridAdapter(this, images[8], expertMINE[1]*expertMINE[2]));
                gridView.setNumColumns(expertMINE[1]);
                txtMines.setText(String.valueOf(expertMINE[0]));
                createMineArray(expertMINE);
                break;
        }
        contGame = true;
        timer = false;
        counter = 0;
        setTimer();
        txtTimer.setText("0");
    }

    void createMineArray(int[] mineParam) {
        // init array
        for(int i = 0; i < mineParam[1]; i++) {
            for(int j = 0 ; j < mineParam[2]; j++) {
                arrMines[i][j] = 0;
            }
        }

        // set "mines"
        for(int k = 0; k < mineParam[0]; k++) {
            int i = (int)(Math.random() * mineParam[1]);
            int j = (int)(Math.random() * mineParam[2]);
            if(arrMines[i][j] != 1)
                arrMines[i][j] = 1;
            else k--;
        }
    }
}
