package com.example.yuuura87.minesweeper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FragmentTable extends Fragment {

    private final String COL_1 = "#fdd561";     //yellow
    private final String COL_2 = "#ffff66";    //green
    private final String COL_SEL = "#b4eeb4";
    private TableLayout tblRecords;
    private boolean flagColor = true;
    private TableRow selRow;
    private ArrayList<double[]> locations;
    private ArrayList<String> names;
    private ArrayList<Integer> times;
    private ArrayList<String> addresses;

    FragmentTableListener fragmentTableListener;

    public interface FragmentTableListener {
        void sendRecordToMap(String name, int time, String address, double[] place);        // Activity will receive this from fragment
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;
        if(context instanceof Activity) {
            activity = (Activity) context;
            try {
                fragmentTableListener = (FragmentTableListener) activity;
            }catch(ClassCastException e) {
                throw new ClassCastException(activity.toString());
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_table, container, false);
        init(view);
        return view;
    }

    public void deleteAllRows() {
        tblRecords.removeAllViews();
        flagColor = true;
    }

    public void addRow(String name, int time, String address, int id, double[] location) {

        locations.add(location);
        names.add(name);
        times.add(time);
        addresses.add(address);
        final TableRow row = new TableRow(getContext());

        row.setClickable(true);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        if(flagColor) { row.setBackgroundColor(Color.parseColor(COL_1)); }
        else { row.setBackgroundColor(Color.parseColor(COL_2)); }
        flagColor = !flagColor;

        TextView tv1 = new TextView(getContext());
        tv1.setText(name);
        tv1.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView tv2 = new TextView(getContext());
        tv2.setText(String.valueOf(time));
        tv2.setGravity(Gravity.CENTER_HORIZONTAL);

        row.addView(tv1, (new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,1)));
        row.addView(tv2, (new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,1)));
        row.setId(id);

        row.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(selRow != null) {
                    if(selRow.getId() % 2 == 0)
                        selRow.setBackgroundColor(Color.parseColor(COL_1));
                    else selRow.setBackgroundColor(Color.parseColor(COL_2));
                }
                selRow = row;
                row.setBackgroundColor(Color.parseColor(COL_SEL));
                fragmentTableListener.sendRecordToMap(names.get(row.getId()), times.get(row.getId()), addresses.get(row.getId()), locations.get(row.getId()));
                Toast.makeText(getContext(), row.getId() + "", Toast.LENGTH_SHORT).show();
            }
        });

        tblRecords.addView(row);
    }

    private void init(View view) {
        tblRecords = view.findViewById(R.id.tblRecords);
        locations = new ArrayList<>();
        names = new ArrayList<>();
        times = new ArrayList<>();
        addresses = new ArrayList<>();
    }
}
