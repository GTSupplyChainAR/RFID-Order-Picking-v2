package com.thad.rfid_orderpick;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by theo on 2/1/18.
 */

public class ExperimentView extends LinearLayout {
    private int white, black, red, yellow, green, blue, dark_gray, light_gray;

    private boolean dataReady = false;

    private ExperimentData experimentData;
    private PickingOrder activeOrder;

    private Context mContext;

    private LinearLayout mContainer;
    private TextView title;


    public ExperimentView(Context context) {
        super(context);
        init(context);
    }

    public ExperimentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context);
    }

    public ExperimentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(getContext()).inflate(R.layout.experiment_view, this, true);
        //this.setBackgroundColor(Color.rgb(255,255,255));
        mContainer = this.findViewById(R.id.experiment_container);

        white = mContext.getResources().getColor(R.color.white);
        black = mContext.getResources().getColor(R.color.black);
        red = mContext.getResources().getColor(R.color.red);
        yellow = mContext.getResources().getColor(R.color.yellow);
        green = mContext.getResources().getColor(R.color.green);
        blue = mContext.getResources().getColor(R.color.blue);
        dark_gray = mContext.getResources().getColor(R.color.dark_gray);
        light_gray = mContext.getResources().getColor(R.color.gray);
    }


    public void setData(ExperimentData experimentData){
        this.experimentData = experimentData;
        dataReady = true;
        createUI();
    }

    public void start(){
        setVisibility(VISIBLE);
    }

    public void stop(){
        setVisibility(GONE);
    }

    private void createUI(){
        mContainer.removeAllViews();

        int text_size = 17;
        int margins = dp_to_pixels(4);
        int padding = dp_to_pixels(9);
        int box_width = dp_to_pixels(50);
        int box_height = dp_to_pixels(40);

        LinearLayout.LayoutParams match_lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of TextView
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams box_lp = new LinearLayout.LayoutParams(
                box_width, box_height);
        box_lp.setMargins(margins, margins, margins, margins);
        lp.gravity = Gravity.CENTER;
        box_lp.gravity = Gravity.CENTER;

        LinearLayout title_bar_layout = new LinearLayout(mContext);
        title_bar_layout.setLayoutParams(match_lp);
        title_bar_layout.setOrientation(HORIZONTAL);

        title = new TextView(mContext);
        title.setPadding(padding/2, padding/2, padding/2, padding/2);
        title.setLayoutParams(match_lp); // Height of TextView);
        title.setText("Pick Order #");
        title.setTextColor(white);
        title.setTextSize(text_size);
        title.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        title_bar_layout.addView(title);

        LinearLayout cart_boxes = new LinearLayout(mContext);
        cart_boxes.setLayoutParams(match_lp);
        cart_boxes.setOrientation(HORIZONTAL);
        cart_boxes.setGravity(TEXT_ALIGNMENT_CENTER);

        String[] cart_tags = experimentData.getCart_tags();
        for(int i = 0 ; i < cart_tags.length ; i++){
            TextView textView = new TextView(mContext);
            textView.setPadding(padding,padding,padding,padding);
            textView.setText(cart_tags[i]);
            textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(text_size);
            textView.setLayoutParams(box_lp);
            textView.setTextColor(dark_gray);
            textView.setTag(cart_tags[i]);
            textView.setBackground(mContext.getResources().getDrawable(R.drawable.gray_border));
            cart_boxes.addView(textView);
        }

        title_bar_layout.addView(cart_boxes);

        mContainer.addView(title_bar_layout);

        String[][] tags = experimentData.getRack_tags();

        int[] dims = experimentData.getDimensions();

        for(int i = 0; i < dims[0] ; i++){
            LinearLayout lli = new LinearLayout(mContext);
            lli.setLayoutParams(lp);
            lli.setGravity(TEXT_ALIGNMENT_CENTER);
            lli.setOrientation(HORIZONTAL);
            for(int j = 0 ; j < dims[1] ; j++){
                TextView textView = new TextView(mContext);
                textView.setPadding(padding,padding,padding,padding);
                textView.setText(tags[i][j]);
                textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                textView.setTextSize(text_size);
                textView.setLayoutParams(box_lp);
                textView.setTextColor(dark_gray);
                textView.setTag(tags[i][j]);
                switch (i){
                    case 0:
                        textView.setBackground(mContext.getResources().getDrawable(R.drawable.red_border));
                        break;
                    case 1:
                        textView.setBackground(mContext.getResources().getDrawable(R.drawable.yellow_border));
                        break;
                    case 2:
                        textView.setBackground(mContext.getResources().getDrawable(R.drawable.green_border));
                        break;
                    default:
                        textView.setBackground(mContext.getResources().getDrawable(R.drawable.blue_border));
                }
                lli.addView(textView);
                //bins[i][j] = textView;
            }
            mContainer.addView(lli);
        }
    }

    public void newOrder(PickingOrder pickingOrder){

        removeOrder(activeOrder);

        title.setText("Pick Task "+pickingOrder.getTaskID());
        HashMap<String,Integer> item_map = pickingOrder.getItems();

        Iterator it = item_map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            TextView textView = this.findViewWithTag(pair.getKey());
            int row = Integer.parseInt(String.valueOf(((String)pair.getKey()).charAt(1)));
            switch (row){
                case 1:
                    textView.setTextColor(white);
                    textView.setBackgroundColor(red);
                    break;
                case 2:
                    textView.setTextColor(black);
                    textView.setBackgroundColor(yellow);
                    break;
                case 3:
                    textView.setTextColor(black);
                    textView.setBackgroundColor(green);
                    break;
                default:
                    textView.setTextColor(white);
                    textView.setBackgroundColor(blue);
                    break;
            }
            textView.setText(""+pair.getValue());
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        String receiveTag = pickingOrder.getReceiveBinTag();
        TextView receiveView = this.findViewWithTag(receiveTag);
        receiveView.setText(""+pickingOrder.getItemCount());
        receiveView.setTextColor(white);
        receiveView.setBackgroundColor(light_gray);
        activeOrder = pickingOrder;
    }

    private void removeOrder(PickingOrder pickingOrder){
        if(pickingOrder == null)
            return;

        HashMap<String,Integer> item_map = pickingOrder.getItems();

        Iterator it = item_map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            TextView textView = this.findViewWithTag(pair.getKey());
            int row = Integer.parseInt(String.valueOf(((String)pair.getKey()).charAt(1)));
            textView.setTextColor(dark_gray);
            switch (row){
                case 1:
                    textView.setBackground(mContext.getResources().getDrawable(R.drawable.red_border));
                    break;
                case 2:
                    textView.setBackground(mContext.getResources().getDrawable(R.drawable.yellow_border));
                    break;
                case 3:
                    textView.setBackground(mContext.getResources().getDrawable(R.drawable.green_border));
                    break;
                default:
                    textView.setBackground(mContext.getResources().getDrawable(R.drawable.blue_border));
                    break;
            }
            textView.setText(String.valueOf(pair.getKey()));
            textView.setTypeface(Typeface.DEFAULT);
        }

        String receiveTag = pickingOrder.getReceiveBinTag();
        TextView receiveView = this.findViewWithTag(receiveTag);
        receiveView.setText(receiveTag);
        receiveView.setTextColor(dark_gray);
        receiveView.setBackground(mContext.getResources().getDrawable(R.drawable.gray_border));

    }

    //HELPER FUNCTIONS
    private int dp_to_pixels(float dp){
        return (int) ((dp)*mContext.getResources().getDisplayMetrics().density +0.5f);
    }

}