package com.thad.rfid_lib.UI;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thad.rfid_lib.Experiment.Experiment;
import com.thad.rfid_lib.R;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_lib.UIRunnables.SetTextRunnable;

/**
 * Created by theo on 3/4/18.
 */

public class CellUI {
    private static final String TAG = "|CellUI|";

    private Activity activity;
    private Experiment experiment;

    private boolean isCart;
    private int color_id;
    private int width, height;
    private String tag;
    private int rack_text_size, cart_text_size;

    private CellDrawable cellDrawable;
    private TextView cellView;


    public CellUI(Experiment experiment){
        this(experiment, false);
    }
    public CellUI(Experiment experiment, boolean isCart){
        this.activity = (Activity)experiment.getContext();
        this.experiment = experiment;
        this.isCart = isCart;
    }

    public void generate(){
        cellView = new TextView(activity);

        LinearLayout.LayoutParams lp_cell = new LinearLayout.LayoutParams(width, height);
        int margins = Utils.dp_to_pixels(activity, 1.5f);
        lp_cell.setMargins(margins, margins, margins, margins);

        cellView.setLayoutParams(lp_cell);
        cellView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        cellView.setTextColor(Color.WHITE);
        cellView.setTypeface(Typeface.DEFAULT_BOLD);

        rack_text_size = (int) (height * Prefs.TEXT_SIZE_CELL);
        cart_text_size = (int) (height * Prefs.TEXT_SIZE_CELL);
        if(experiment.isGlass()){
            rack_text_size = (int) (height * Prefs.TEXT_SIZE_CELL_GLASS);
            cart_text_size = (int) (height * Prefs.TEXT_SIZE_CELL_GLASS);
        }

        updateTextStyle();


        cellView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                experiment.onFakeScan(tag);
            }
        });

        cellDrawable = new CellDrawable(activity, this);

        empty();
    }

    private void updateTextStyle(){
        if(!isCart) {
            cellView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, rack_text_size);
            cellView.setGravity(Gravity.CENTER);
        }else{
            cellView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, cart_text_size);
            cellView.setGravity(Gravity.CENTER | Gravity.BOTTOM);
            cellView.setPadding(0 ,0, 0, (int)(height * Prefs.CART_TEXT_BOTTOM_OFFSET));
        }

        if((tag.charAt(1) == '2' || tag.charAt(1) == '3') && !isCart) {
            cellView.setShadowLayer(0,0,0,Color.BLACK);
            cellView.setTextColor(Color.BLACK);
        }else {
            cellView.setShadowLayer(8, 0, 0, Color.BLACK);
            cellView.setTextColor(Color.WHITE);
        }
    }

    public void fill(){
        if(!isCart) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cellDrawable.fill();
                    cellView.setBackground(cellDrawable.getDrawable());
                }
            });
        }else{
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cellView.setBackground(activity.getResources().getDrawable(R.drawable.bin_full));
                }
            });
        }
    }

    public void empty(){
        if(!isCart) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cellDrawable.empty();
                    updateTextStyle();
                    cellView.setBackground(cellDrawable.getDrawable());
                }
            });
        }else{
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cellView.setBackground(activity.getResources().getDrawable(R.drawable.bin_empty));
                }
            });
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cellView.setText("");
            }
        });
    }

    public void toggleCross() {
        if (!isCart){
            activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    cellDrawable.toggleCross();
                    cellView.setBackground(cellDrawable.getDrawable());
                }
            });
        }else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cellView.setBackground(activity.getResources().getDrawable(R.drawable.bin_red_arrow));
                }
            });
        }
    }

    public void addCheck(){
        cellDrawable.addCheck();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cellView.setBackground(cellDrawable.getDrawable());
                cellView.setShadowLayer(8,0,0,Color.BLACK);
                String prev_text = (String) cellView.getText();
                cellView.setTextColor(activity.getResources().getColor(R.color.checked_cell_text));
                cellView.setText(prev_text);
            }
        });
    }


    public View getView(){return cellView;}

    public void setColor(int color_id){this.color_id = color_id;}
    public void setText(String text){
        activity.runOnUiThread(new SetTextRunnable(cellView, text));
    }
    public void setSize(int width, int height){this.width = width; this.height = height;}
    public void setTag(String tag){this.tag = tag;}

    public int getColorId(){return color_id;}
    public int[] getDims(){return new int[]{width, height};}
    public String getTag(){return tag;}
    public Experiment getExperiment(){return experiment;}

}
