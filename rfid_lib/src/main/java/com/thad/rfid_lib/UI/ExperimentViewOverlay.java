package com.thad.rfid_lib.UI;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thad.rfid_lib.Data.ShelvingUnit;
import com.thad.rfid_lib.Experiment;
import com.thad.rfid_lib.R;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_lib.UIRunnables.AddViewRunnable;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by theo on 3/6/18.
 */

public class ExperimentViewOverlay extends RelativeLayout{
    private static final String TAG = "|ExperimentViewOverlay|";

    //private static final int[][] hardcoded_positions = new int[][]{{445,212}, {305,212}, {585,212},
    //                                                                {445,312}, {305,312}, {585,312}};
    //private static final int[][] hardcoded_positions = new int[][]{{222,106}, {152,106}, {292,106},
    //                                                                {222,156}, {152,156}, {292,156}};
    private static final float[][] hardcoded_positions = new float[][]{{0.45f,0.5f}, {0.65f,0.5f}, {0.83f,0.5f},
                                                                   {0.45f,0.75f}, {0.65f,0.75f}, {0.83f,0.75f}};
    //LAYOUT PARAMS
    private static final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    private Activity activity;
    private Experiment experiment;

    public ExperimentViewOverlay(Experiment experiment) {
        super(experiment.getContext());
        this.activity = experiment.getActivity();
        this.experiment = experiment;
        init();
    }

    private void init(){
        int height_px = (int)(Prefs.SCREEN_RATIO*Prefs.SCREEN_WIDTH);
        LayoutParams layoutParams = new LayoutParams(MP, height_px);
        this.setLayoutParams(layoutParams);
        this.setBackground(activity.getResources().getDrawable(R.drawable.black_vertical_fade));

        TextView tapHint = new TextView(activity);
        tapHint.setLayoutParams(new LayoutParams(WC, WC));
        tapHint.setTextSize(20);
        tapHint.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        tapHint.setPadding(Utils.dp_to_pixels(activity, 25),0,0,0);
        tapHint.setTextColor(Color.WHITE);
        tapHint.setShadowLayer(Utils.dp_to_pixels(activity, 10), 0, 0, Color.BLACK);
        tapHint.setText("<TAP>\n when done.");
        this.addView(tapHint);
    }

    public void fill(HashMap<String, Integer> itemsOnHand, int[] correctPos, int[] wrongPos){
        ShelvingUnit cart = experiment.getWarehouseData().getCart();
        int cols = cart.getDimensions()[1];

        float middle_col = 0.5f+Math.min(correctPos[1], wrongPos[1]) + Math.abs(correctPos[1] - wrongPos[1])/2f;
        int[] startingPos = new int[2];
        startingPos[0] = (int) (middle_col * (1 - Prefs.RACK_SCREEN_PERCENTAGE) * Prefs.SCREEN_WIDTH / ((float) cols));
        startingPos[0] -= (int)(Prefs.ITEM_IMG_SIZE / 2);
        startingPos[0] += (int)(Prefs.RACK_SCREEN_PERCENTAGE*Prefs.SCREEN_WIDTH);
        startingPos[1] = Utils.dp_to_pixels(activity, 50);

        int screen_width = Prefs.SCREEN_WIDTH, screen_height = (int)(Prefs.SCREEN_WIDTH*Prefs.SCREEN_RATIO);
        ArrayList<Integer> usedPositionIndices = new ArrayList<Integer>();
        for(String tag : itemsOnHand.keySet()){

            int min_d_ind = -1;
            double min_d = Prefs.SCREEN_WIDTH;
            for(int j = 0 ; j < hardcoded_positions.length ; j++){
                if(usedPositionIndices.contains(j))
                    continue;
                int[] pos_j = new int[]{(int)(screen_width*hardcoded_positions[j][0]+Prefs.ITEM_IMG_SIZE/2),
                                       (int) (screen_height*hardcoded_positions[j][1])};
                double d = Utils.distance(pos_j, startingPos);
                if(d<min_d){
                    min_d = d;
                    min_d_ind = j;
                }
            }
            if(min_d_ind == -1)
                break;
            usedPositionIndices.add(min_d_ind);
            Log.d(TAG, "Final Pixels = "+startingPos[1]);
            //int leftMargin = startingPos[0];//Utils.dp_to_pixels(activity, hardcoded_positions[min_d_ind][0]);
            //int bottomMargin = startingPos[1];//Utils.dp_to_pixels(activity, hardcoded_positions[min_d_ind][1]);
            int leftMargin = (int) (screen_width * hardcoded_positions[min_d_ind][0]);
            int bottomMargin = (int) (screen_height * hardcoded_positions[min_d_ind][1]);


            LinearLayout itemLayout = new LinearLayout(activity);
            LayoutParams layoutParams = new LayoutParams(WC, WC);
            layoutParams.addRule(ALIGN_PARENT_BOTTOM);
            layoutParams.leftMargin = leftMargin;
            layoutParams.bottomMargin = bottomMargin;
            itemLayout.setLayoutParams(layoutParams);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);

            ImageView itemImg = new ImageView(activity);
            itemImg.setLayoutParams(new LayoutParams(Prefs.ITEM_IMG_SIZE, Prefs.ITEM_IMG_SIZE));
            Drawable imgDrawable = Utils.loadDrawableFromAssets(activity, "item_pictures/processed/"+tag.toLowerCase()+".png");
            itemImg.setImageDrawable(imgDrawable);

            TextView itemTxt = new TextView(activity);
            itemTxt.setLayoutParams(new LayoutParams(WC, MP));
            itemTxt.setTextColor(Color.WHITE);
            itemTxt.setGravity(Gravity.CENTER_VERTICAL);
            itemTxt.setPadding(Utils.dp_to_pixels(activity, 6), 0, 0, 0);
            itemTxt.setText("x"+itemsOnHand.get(tag));

            itemLayout.addView(itemImg);
            itemLayout.addView(itemTxt);

            activity.runOnUiThread(new AddViewRunnable(this, itemLayout));
        }
    }

}
