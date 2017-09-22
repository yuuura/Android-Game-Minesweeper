package com.example.yuuura87.minesweeper;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

public class GameActivity extends AppCompatActivity implements SensorService.ServiceCallbacks {

    private static final String TAG = GameActivity.class.getSimpleName();
    private SensorService sensorService;
    private boolean isBound = false;
    private LocationFinder location;
    private Store store;
    private int cntMines, addedMines = 0;
    private final int[] easyMINE = {5, 10, 10};                 // {mines, x, y}
    private final int[] normalMINE = {10, 10, 10};
    private final int[] expertMINE = {10, 5, 5};
    private int[][] arrMines;
    private GridView gridView;
    private int mult;
    private volatile boolean contGame;
    private boolean timer;
    private TextView txtTimer, txtMines;
    private AlertDialog.Builder alertDialog;
    private int counter;
    private Thread threadTimer, addMinesThread;
    private Button btnSmile, btnGameRecords;
    private Integer storedX = null, storedY = null, storedZ = null;
    private Integer[] images = {R.drawable.one, R.drawable.two, R.drawable.three, R.drawable.four, R.drawable.five, R.drawable.six, R.drawable.seven, R.drawable.eight,
            R.drawable.to_click, R.drawable.empty, R.drawable.flag, R.drawable.mine, R.drawable.mine_boom};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(sensorConnection);
    }

    @Override
    protected void onStop() {
        super.onStop();
        location.onStop();
        sensorService.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            String json = getSharedPreferences("Store", MODE_PRIVATE).getString("store", null);
            store = new Gson().fromJson(json, Store.class);
        } catch (Exception e) {}
        if(isBound)
            sensorService.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        btnSmile.callOnClick();
        String json = new Gson().toJson(store);
        getSharedPreferences("Store", MODE_PRIVATE).edit().putString("store", json).apply();

    }

    // CODES: 0 - empty, 1 - mine, 2 - flag on empty, 3 - flag on mine, 4 - opened
    void onClick(View view, int position) {
        int i = position / arrMines.length;
        int j = position % arrMines.length;

        if (arrMines[i][j] == 1) {                   // Lost! End game.
            ((ImageView) view).setImageResource(R.drawable.mine_boom);
            endGame(i, j, false);
        } else if (arrMines[i][j] == 0) {
            spread(view, i, j);
        }
    }

    private void buildPopUp(final ArrayList<Person> arr, final int level, final int time) {
        alertDialog = new AlertDialog.Builder(GameActivity.this);
        alertDialog.setTitle("You're lucky today");
        alertDialog.setMessage("Write you name:");

        final EditText input = new EditText(GameActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int cnt = arr.size();
                double[] place = {location.getMyLocation().getLatitude(), location.getMyLocation().getLongitude()};
                String address = location.getAddress();
                if (cnt == 0) {
                    Person per = new Person(input.getText().toString(), time, place, address, level);
                    arr.add(per);
                } else if ((arr.get(cnt - 1).getTime() > time) || cnt < store.getNUM_PERSONS()) {
                    int size = 0;
                    while (size < cnt) {
                        if (arr.get(size).getTime() > time) {
                            if (cnt == store.getNUM_PERSONS()) arr.remove(cnt - 1);
                            Person per = new Person(input.getText().toString(), time, place, address, level);
                            arr.add(size, per);
                            break;
                        }
                        size++;
                    }
                    if (size == cnt) {
                        Person per = new Person(input.getText().toString(), time, place, address, level);
                        arr.add(size, per);
                    }
                }
            }
        });
        alertDialog.show();
    }

    private void recordWinner() {
        switch (store.getLevel()) {
            case 1:
                buildPopUp(store.getRecordsArrEasy(), 1, counter);
                break;
            case 2:
                buildPopUp(store.getRecordsArrNormal(), 2, counter);
                break;
            case 3:
                buildPopUp(store.getRecordsArrExpert(), 3, counter);
                break;
        }
    }

    void endGame(int i, int j, boolean state) {
        contGame = false;
        timer = false;
        threadTimer.interrupt();
        if (state) {
            btnSmile.setBackgroundResource(R.drawable.glasses);
            runAnimationWin();
            recordWinner();
        } else {
            btnSmile.setBackgroundResource(R.drawable.dead);
            runAnimationLose(i, j);
        }
    }

    void runAnimationWin() {

    }

    void animateTile(View view, String translation, int multT, String rotation, int multR) {
        int m = 10 + (int) (Math.random() * 300f);
        int r = (int) (Math.random() * 720f);
        ObjectAnimator a = ObjectAnimator.ofFloat(view, translation, m * multT);
        ObjectAnimator b = ObjectAnimator.ofFloat(view, rotation, 0, r * multR);
        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new DecelerateInterpolator());//)BounceInterpolator());
        set.play(a).with(b);
        int time = 500 + (int) (Math.random() * 2000);
        set.setDuration(time);
        set.start();
    }

    void animateTile(View view, String translation1, int multT1, String rotation1, int multR1, String translation2, int multT2, String rotation2, int multR2) {
        int m1 = 10 + (int) (Math.random() * 300f);
        int m2 = 10 + (int) (Math.random() * 300f);
        int r1 = (int) (Math.random() * 720f);
        int r2 = (int) (Math.random() * 720f);
        ObjectAnimator a = ObjectAnimator.ofFloat(view, translation1, m1 * multT1);
        ObjectAnimator b = ObjectAnimator.ofFloat(view, rotation1, 0, r1 * multR1);
        ObjectAnimator c = ObjectAnimator.ofFloat(view, translation2, m2 * multT2);
        ObjectAnimator d = ObjectAnimator.ofFloat(view, rotation2, 0, r2 * multR2);
        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new DecelerateInterpolator());//)BounceInterpolator());
        set.play(a).with(b).with(c).with(d);
        int time = 500 + (int) (Math.random() * 2000);
        set.setDuration(time);
        set.start();
    }

    void runAnimationLose(int i, int j) {

        View view = gridView.getChildAt(i * mult + j);
        TransitionManager.beginDelayedTransition(gridView);
        view.setVisibility(view.GONE);
        View right = null, left = null, up = null, bottom = null, up_right = null, right_bottom = null, up_left = null, left_bottom = null;

        if (i == 0 && j == 0) {
            right = gridView.getChildAt(i * mult + j + 1);
            bottom = gridView.getChildAt((i + 1) * mult + j);
            right_bottom = gridView.getChildAt((i + 1) * mult + j + 1);
        } else if (i == 0 && j < arrMines.length - 1) {
            left = gridView.getChildAt(i * mult + j - 1);
            right = gridView.getChildAt(i * mult + j + 1);
            bottom = gridView.getChildAt((i + 1) * mult + j);
            right_bottom = gridView.getChildAt((i + 1) * mult + j + 1);
            left_bottom = gridView.getChildAt((i + 1) * mult + j - 1);
        } else if (i == 0 && j == arrMines.length - 1) {
            left = gridView.getChildAt(i * mult + j - 1);
            bottom = gridView.getChildAt((i + 1) * mult + j);
            right_bottom = gridView.getChildAt((i + 1) * mult + j + 1);
        } else if (i < arrMines.length - 1 && j == 0) {
            up = gridView.getChildAt((i - 1) * mult + j);
            up_right = gridView.getChildAt((i - 1) * mult + j + 1);
            right = gridView.getChildAt(i * mult + j + 1);
            bottom = gridView.getChildAt((i + 1) * mult + j);
            right_bottom = gridView.getChildAt((i + 1) * mult + j + 1);
        } else if (i < arrMines.length - 1 && j < arrMines.length - 1) {
            up = gridView.getChildAt((i - 1) * mult + j);
            up_right = gridView.getChildAt((i - 1) * mult + j + 1);
            right = gridView.getChildAt(i * mult + j + 1);
            bottom = gridView.getChildAt((i + 1) * mult + j);
            right_bottom = gridView.getChildAt((i + 1) * mult + j + 1);
            left_bottom = gridView.getChildAt((i + 1) * mult + j - 1);
            left = gridView.getChildAt(i * mult + j - 1);
            up_left = gridView.getChildAt((i - 1) * mult + j - 1);
        } else if (i < arrMines.length - 1 && j == arrMines.length - 1) {
            up = gridView.getChildAt((i - 1) * mult + j);
            up_left = gridView.getChildAt((i - 1) * mult + j - 1);
            left_bottom = gridView.getChildAt((i + 1) * mult + j - 1);
            left = gridView.getChildAt(i * mult + j - 1);
            bottom = gridView.getChildAt((i + 1) * mult + j);
        } else if (i == arrMines.length - 1 && j == 0) {
            up = gridView.getChildAt((i - 1) * mult + j);
            up_right = gridView.getChildAt((i - 1) * mult + j + 1);
            right = gridView.getChildAt(i * mult + j + 1);
        } else if (i == arrMines.length - 1 && j < arrMines.length - 1) {
            up = gridView.getChildAt((i - 1) * mult + j);
            up_right = gridView.getChildAt((i - 1) * mult + j + 1);
            right = gridView.getChildAt(i * mult + j + 1);
            left = gridView.getChildAt(i * mult + j - 1);
            up_left = gridView.getChildAt((i - 1) * mult + j - 1);
        } else if (i == arrMines.length - 1 && j == arrMines.length - 1) {
            up = gridView.getChildAt((i - 1) * mult + j);
            left = gridView.getChildAt(i * mult + j - 1);
            up_left = gridView.getChildAt((i - 1) * mult + j - 1);
        }

        if (up != null)
            animateTile(up, "translationY", -1, "rotationX", 1);
        if (left != null)
            animateTile(left, "translationX", -1, "rotationY", -1);
        if (right != null)
            animateTile(right, "translationX", 1, "rotationY", 1);
        if (bottom != null)
            animateTile(bottom, "translationY", 1, "rotationX", -1);
        if (up_left != null)
            animateTile(up_left, "translationY", -1, "rotationX", 1, "translationX", -1, "rotationY", -1);
        if (up_right != null)
            animateTile(up_right, "translationY", -1, "rotationX", 1, "translationX", 1, "rotationY", 1);
        if (left_bottom != null)
            animateTile(left_bottom, "translationY", 1, "rotationX", -1, "translationX", -1, "rotationY", -1);
        if (right_bottom != null)
            animateTile(right_bottom, "translationY", 1, "rotationX", -1, "translationX", 1, "rotationY", 1);
    }

    void prepareNewGame() {
        buildGame();
    }

    void spread(View view, int i, int j) {

        if (checkOneCell(view, i, j))
            return;

        if (i != 0 && arrMines[i - 1][j] != 4 && arrMines[i - 1][j] != 2 && arrMines[i - 1][j] != 3)
            spread(gridView.getChildAt((i - 1) * mult + j), i - 1, j);
        if (i != 0 && j != arrMines.length - 1 && arrMines[i - 1][j + 1] != 4 && arrMines[i - 1][j + 1] != 2 && arrMines[i - 1][j + 1] != 3)
            spread(gridView.getChildAt((i - 1) * mult + j + 1), i - 1, j + 1);
        if (j != arrMines.length - 1 && arrMines[i][j + 1] != 4 && arrMines[i][j + 1] != 2 && arrMines[i][j + 1] != 3)
            spread(gridView.getChildAt(i * mult + j + 1), i, j + 1);
        if (i != arrMines.length - 1 && j != arrMines.length - 1 && arrMines[i + 1][j + 1] != 4 && arrMines[i + 1][j + 1] != 2 && arrMines[i + 1][j + 1] != 3)
            spread(gridView.getChildAt((i + 1) * mult + j + 1), i + 1, j + 1);
        if (i != arrMines.length - 1 && arrMines[i + 1][j] != 4 && arrMines[i + 1][j] != 2 && arrMines[i + 1][j] != 3)
            spread(gridView.getChildAt((i + 1) * mult + j), i + 1, j);
        if (i != arrMines.length - 1 && j > 0 && arrMines[i + 1][j - 1] != 4 && arrMines[i + 1][j - 1] != 2 && arrMines[i + 1][j - 1] != 3)
            spread(gridView.getChildAt((i + 1) * mult + j - 1), i + 1, j - 1);
        if (j > 0 && arrMines[i][j - 1] != 4 && arrMines[i][j - 1] != 2 && arrMines[i][j - 1] != 3)
            spread(gridView.getChildAt(i * mult + j - 1), i, j - 1);
        if (i > 0 && j > 0 && arrMines[i - 1][j - 1] != 4 && arrMines[i - 1][j - 1] != 2 && arrMines[i - 1][j - 1] != 3)
            spread(gridView.getChildAt((i - 1) * mult + j - 1), i - 1, j - 1);
    }

    boolean checkOneCell(View view, int i, int j) {

        int countMines = 0;

        if (i == 0 && j == 0) {
            if (arrMines[i][j + 1] == 1 || arrMines[i][j + 1] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j + 1] == 1 || arrMines[i + 1][j + 1] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j] == 1 || arrMines[i + 1][j] == 3) {
                countMines++;
            }
        } else if (i == 0 && j < arrMines.length - 1) {
            if (arrMines[i][j - 1] == 1 || arrMines[i][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j - 1] == 1 || arrMines[i + 1][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j] == 1 || arrMines[i + 1][j] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j + 1] == 1 || arrMines[i + 1][j + 1] == 3) {
                countMines++;
            }
            if (arrMines[i][j + 1] == 1 || arrMines[i][j + 1] == 3) {
                countMines++;
            }
        } else if (i == 0 && j == arrMines.length - 1) {
            if (arrMines[i][j - 1] == 1 || arrMines[i][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j - 1] == 1 || arrMines[i + 1][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j] == 1 || arrMines[i + 1][j] == 3) {
                countMines++;
            }
        } else if (i < arrMines.length - 1 && j == 0) {
            if (arrMines[i - 1][j] == 1 || arrMines[i - 1][j] == 3) {
                countMines++;
            }
            if (arrMines[i - 1][j + 1] == 1 || arrMines[i - 1][j + 1] == 3) {
                countMines++;
            }
            if (arrMines[i][j + 1] == 1 || arrMines[i][j + 1] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j + 1] == 1 || arrMines[i + 1][j + 1] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j] == 1 || arrMines[i + 1][j] == 3) {
                countMines++;
            }
        } else if (i < arrMines.length - 1 && j < arrMines.length - 1) {
            if (arrMines[i - 1][j] == 1 || arrMines[i - 1][j] == 3) {
                countMines++;
            }
            if (arrMines[i - 1][j + 1] == 1 || arrMines[i - 1][j + 1] == 3) {
                countMines++;
            }
            if (arrMines[i][j + 1] == 1 || arrMines[i][j + 1] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j + 1] == 1 || arrMines[i + 1][j + 1] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j] == 1 || arrMines[i + 1][j] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j - 1] == 1 || arrMines[i + 1][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i][j - 1] == 1 || arrMines[i][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i - 1][j - 1] == 1 || arrMines[i - 1][j - 1] == 3) {
                countMines++;
            }
        } else if (i < arrMines.length - 1 && j == arrMines.length - 1) {
            if (arrMines[i - 1][j] == 1 || arrMines[i - 1][j] == 3) {
                countMines++;
            }
            if (arrMines[i - 1][j - 1] == 1 || arrMines[i - 1][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i][j - 1] == 1 || arrMines[i][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j - 1] == 1 || arrMines[i + 1][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i + 1][j] == 1 || arrMines[i + 1][j] == 3) {
                countMines++;
            }
        } else if (i == arrMines.length - 1 && j == 0) {
            if (arrMines[i - 1][j] == 1 || arrMines[i - 1][j] == 3) {
                countMines++;
            }
            if (arrMines[i - 1][j + 1] == 1 || arrMines[i - 1][j + 1] == 3) {
                countMines++;
            }
            if (arrMines[i][j + 1] == 1 || arrMines[i][j + 1] == 3) {
                countMines++;
            }
        } else if (i == arrMines.length - 1 && j < arrMines.length - 1) {
            if (arrMines[i][j - 1] == 1 || arrMines[i][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i - 1][j - 1] == 1 || arrMines[i - 1][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i - 1][j] == 1 || arrMines[i - 1][j] == 3) {
                countMines++;
            }
            if (arrMines[i - 1][j + 1] == 1 || arrMines[i - 1][j + 1] == 3) {
                countMines++;
            }
            if (arrMines[i][j + 1] == 1 || arrMines[i][j + 1] == 3) {
                countMines++;
            }
        } else if (i == arrMines.length - 1 && j == arrMines.length - 1) {
            if (arrMines[i][j - 1] == 1 || arrMines[i][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i - 1][j - 1] == 1 || arrMines[i - 1][j - 1] == 3) {
                countMines++;
            }
            if (arrMines[i - 1][j] == 1 || arrMines[i - 1][j] == 3) {
                countMines++;
            }
        }

        switch (countMines) {
            case 0:
                ((ImageView) view).setImageResource(R.drawable.empty);
                arrMines[i][j] = 4;
                return false;
            case 1:
                ((ImageView) view).setImageResource(R.drawable.one);
                break;
            case 2:
                ((ImageView) view).setImageResource(R.drawable.two);
                break;
            case 3:
                ((ImageView) view).setImageResource(R.drawable.three);
                break;
            case 4:
                ((ImageView) view).setImageResource(R.drawable.four);
                break;
            case 5:
                ((ImageView) view).setImageResource(R.drawable.five);
                break;
            case 6:
                ((ImageView) view).setImageResource(R.drawable.six);
                break;
            case 7:
                ((ImageView) view).setImageResource(R.drawable.seven);
                break;
            case 8:
                ((ImageView) view).setImageResource(R.drawable.eight);
        }
        arrMines[i][j] = 4;
        return true;
    }

    void setListener() {

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof ImageView && contGame) {
                    if (!timer) threadTimer.start();
                    timer = true;
                    onClick(view, position);
                }
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long arg3) {
                if (view instanceof ImageView && contGame) {
                    int i = position / arrMines.length;
                    int j = position % arrMines.length;
                    if (arrMines[i][j] == 2 || arrMines[i][j] == 3) {
                        if (arrMines[i][j] == 2) arrMines[i][j] = 0;
                        else if (arrMines[i][j] == 3) arrMines[i][j] = 1;
                        ((ImageView) view).setImageResource(R.drawable.to_click);
                        int numMines = Integer.parseInt(txtMines.getText().toString());
                        txtMines.setText(String.valueOf(numMines + 1));
                        if (arrMines[i][j] == 1) cntMines++;
                        Toast.makeText(GameActivity.this, "Flag UNSET", Toast.LENGTH_SHORT).show();
                    } else if (arrMines[i][j] == 0 || arrMines[i][j] == 1) {
                        if (arrMines[i][j] == 0) arrMines[i][j] = 2;
                        else if (arrMines[i][j] == 1) arrMines[i][j] = 3;
                        ((ImageView) view).setImageResource(R.drawable.flag);
                        int numMines = Integer.parseInt(txtMines.getText().toString());
                        txtMines.setText(String.valueOf(numMines - 1));
                        if (arrMines[i][j] == 3)
                            cntMines--;
                        Toast.makeText(GameActivity.this, "Flag SET", Toast.LENGTH_SHORT).show();
                    }
                    if (!timer) threadTimer.start();
                    timer = true;
                    if (cntMines == 0)                 // Win! End game.
                        endGame(i, j, true);
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

        btnGameRecords.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecordsActivity();
            }
        });
    }

    private void startRecordsActivity() {
        Intent records = new Intent(this, RecordsActivity.class);
        records.putExtra("Store", store);
        startActivity(records);
    }

    void setTimer() {

        threadTimer = new Thread() {
            @Override
            public void run() {
                while (timer) {
                    try {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                counter++;
                                txtTimer.setText(String.valueOf(counter));
                            }
                        });
                    } catch (InterruptedException e) {}
                }
            }
        };
    }

    void buildGame() {
        btnSmile.setBackgroundResource(R.drawable.smile);
        storedX = null;
        storedY = null;
        storedZ = null;
        switch (store.getLevel()) {
            case 1:                     // Easy
                addedMines = easyMINE[0];
                cntMines = easyMINE[0];
                arrMines = new int[easyMINE[1]][easyMINE[2]];
                gridView.setAdapter(new GridAdapter(this, images[8], easyMINE[1] * easyMINE[2]));
                gridView.setNumColumns(easyMINE[1]);// Width(easyMINE[2]);
                txtMines.setText(String.valueOf(easyMINE[0]));
                createMineArray(easyMINE);

                break;
            case 2:                     // Normal
                addedMines = normalMINE[0];
                cntMines = normalMINE[0];
                arrMines = new int[normalMINE[1]][normalMINE[2]];
                gridView.setAdapter(new GridAdapter(this, images[8], normalMINE[1] * normalMINE[2]));
                gridView.setNumColumns(normalMINE[1]);
                txtMines.setText(String.valueOf(normalMINE[0]));
                createMineArray(normalMINE);
                break;
            case 3:                     // Expert
                addedMines = expertMINE[0];
                cntMines = expertMINE[0];
                arrMines = new int[expertMINE[1]][expertMINE[2]];
                gridView.setAdapter(new GridAdapter(this, images[8], expertMINE[1] * expertMINE[2]));
                gridView.setNumColumns(expertMINE[1]);
                txtMines.setText(String.valueOf(expertMINE[0]));
                createMineArray(expertMINE);
                break;
        }
        mult = arrMines.length;
        contGame = true;
        timer = false;
        counter = 0;
        setTimer();
        txtTimer.setText("0");
    }

    void createMineArray(int[] mineParam) {
        // init array
        for (int i = 0; i < mineParam[1]; i++) {
            for (int j = 0; j < mineParam[2]; j++) {
                arrMines[i][j] = 0;
            }
        }

        // set "mines"
        for (int k = 0; k < mineParam[0]; k++) {
            int i = (int) (Math.random() * mineParam[1]);
            int j = (int) (Math.random() * mineParam[2]);
            if (arrMines[i][j] != 1)
                arrMines[i][j] = 1;
            else k--;
        }
    }

    private void init() {
        btnSmile = (Button) findViewById(R.id.btnSmile);
        btnGameRecords = (Button) findViewById(R.id.btnGameRecords);
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setBackgroundColor(Color.BLACK);

        txtTimer = (TextView) findViewById(R.id.timer);
        txtMines = (TextView) findViewById(R.id.cntMines);

        store = (Store) getIntent().getSerializableExtra("Store");
        buildGame();
        setListener();
        setTimer();
        location = new LocationFinder(this);
        Intent intent = new Intent(this, SensorService.class);
        bindService(intent, sensorConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (location.getLocationPermissionRequestCode() == requestCode) {
            Log.d(TAG, "onRequestPermissionsResult: user granted location permissions " + Arrays.toString(grantResults));
        }
    }

    public ServiceConnection sensorConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            SensorService.MyLocalBinder binder = (SensorService.MyLocalBinder) iBinder;
            sensorService = binder.getService();
            isBound = true;
            sensorService.setCallbacks(GameActivity.this);
            sensorService.onResume();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    @Override
    public void sensorChanged(int x, int y, int z) {
        if(timer) {
            if (storedX == null || storedY == null || storedZ == null) {
                storedX = x;
                storedY = y;
                storedZ = z;
            }
            else if (storedX != x || storedY != y || storedZ != z) {
                int num = Math.abs(storedX - x) + Math.abs(storedY - y) + Math.abs(storedZ - z);
                if (num > 1 && (addMinesThread == null || !addMinesThread.isAlive())) {
                    addMinesSensor(num);
                }
            }
        }
    }

    void addMinesSensor(final int mines) {
        addedMines += mines;
        cntMines += mines;
        final int newNumMines = cntMines;
        switch (store.getLevel()) {
            case 1:                     // Easy
                if(addedMines >= (easyMINE[1] * easyMINE[2])) {
                    endGame(easyMINE[1]/2, easyMINE[2]/2, false);
                    return;
                }
                addMinesToArraySensor(mines, easyMINE);

                break;
            case 2:                     // Normal
                if(addedMines >= (normalMINE[1] * normalMINE[2])) {
                    endGame(normalMINE[1]/2, normalMINE[2]/2, false);
                    return;
                }
                addMinesToArraySensor(mines, normalMINE);
                break;
            case 3:                     // Expert
                if(addedMines >= (expertMINE[1] * expertMINE[2])) {
                    endGame(expertMINE[1]/2, expertMINE[2]/2, false);
                    return;
                }
                addMinesToArraySensor(mines, expertMINE);
                break;
        }
        txtMines.setText(String.valueOf(newNumMines));
        addMinesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) { }
            }
        });
        addMinesThread.start();
    }

    public void addMinesToArraySensor(int numMines, int[] mineParam) {
        // CODES: 0 - empty, 1 - mine, 2 - flag on empty, 3 - flag on mine, 4 - opened
        // set "mines"
        for (int k = 0; k < numMines; k++) {
            int i = (int) (Math.random() * mineParam[1]);
            int j = (int) (Math.random() * mineParam[2]);
            if (arrMines[i][j] == 0 || arrMines[i][j] == 4)
                arrMines[i][j] = 1;
            else if(arrMines[i][j] == 2) {
                arrMines[i][j] = 3;
                cntMines--;
            }
            else k--;
        }

        for(int i = 0; i < mineParam[1]; i++) {
            for(int j = 0; j < mineParam[2]; j++) {
                if(arrMines[i][j] == 4 || arrMines[i][j] == 1) {
                    ((ImageView) gridView.getChildAt((i * mult) + j)).setImageResource(R.drawable.to_click);
                    if(arrMines[i][j] == 4) arrMines[i][j] = 0;
                }
            }
        }
    }
}
