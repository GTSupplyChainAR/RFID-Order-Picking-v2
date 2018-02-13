package com.thad.rfid_orderpick.Util;

import android.content.Context;
import android.graphics.Typeface;
import android.media.Image;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thad.rfid_orderpick.DrawableMapper;
import com.thad.rfid_orderpick.ExperimentData;
import com.thad.rfid_orderpick.GlassMainActivity;
import com.thad.rfid_orderpick.PickingOrder;
import com.thad.rfid_orderpick.PickingTask;
import com.thad.rfid_orderpick.R;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This Layout Class represents the main UI of the Experiment View.
 */

public class ExperimentView extends RelativeLayout {
    private static final String TAG = "ExperimentView";

    private int white, black, red, yellow, green, blue, dark_gray, light_gray;

    private GlassMainActivity mMain;

    private ExperimentData experimentData;


    private LinearLayout[] rack;

    public ExperimentView(Context context) {
        super(context);
        mMain = (GlassMainActivity)context;
        init();
    }

    public ExperimentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMain = (GlassMainActivity)context;
        init();
    }

    public ExperimentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMain = (GlassMainActivity)context;
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.experiment_view, this, true);


        white = mMain.getResources().getColor(R.color.white);
        black = mMain.getResources().getColor(R.color.black);
        red = mMain.getResources().getColor(R.color.red);
        yellow = mMain.getResources().getColor(R.color.yellow);
        green = mMain.getResources().getColor(R.color.green);
        blue = mMain.getResources().getColor(R.color.blue);
        dark_gray = mMain.getResources().getColor(R.color.dark_gray);
        light_gray = mMain.getResources().getColor(R.color.gray);
    }


    public void createUI(ExperimentData experimentData){

        this.experimentData = experimentData;

        rack = new LinearLayout[2];
        rack[0] = (LinearLayout)mMain.findViewById(R.id.rackA);//this.getChildAt(R.id.rackA);
        rack[1] = (LinearLayout)mMain.findViewById(R.id.rackB);//this.getChildAt(R.id.rackB);
        rack[0].removeAllViews();
        rack[1].removeAllViews();

        String[][] rack_tags = experimentData.getRack_tags();
        int box_width = dp_to_pixels(52);
        int box_height = dp_to_pixels(35);

        int margin = dp_to_pixels(2);
        int text_size = 17;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                    LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams box_lp = new LinearLayout.LayoutParams(box_width, box_height);
        box_lp.setMargins(margin, margin, margin, margin);

        for(int r = 0 ; r < 2 ; r++){
            for(int i = 0 ; i < rack_tags.length ; i++){
                LinearLayout layout_i = new LinearLayout(mMain);
                layout_i.setLayoutParams(lp);
                layout_i.setOrientation(LinearLayout.HORIZONTAL);

                for(int j = 0 ; j < rack_tags[0].length ; j++){
                    if((r == 0 && j >= 3) || (r == 1 && j < 3 ))
                        continue;

                    TextView shelf = new TextView(mMain);
                    shelf.setLayoutParams(box_lp);
                    shelf.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                    shelf.setGravity(Gravity.CENTER);
                    shelf.setTextColor(dark_gray);
                    shelf.setTextSize(text_size);
                    shelf.setText(rack_tags[i][j]);
                    shelf.setTag(rack_tags[i][j]);
                    switch (i){
                        case 0:
                            shelf.setBackground(mMain.getResources().getDrawable(R.drawable.red_border));
                            break;
                        case 1:
                            shelf.setBackground(mMain.getResources().getDrawable(R.drawable.yellow_border));
                            break;
                        case 2:
                            shelf.setBackground(mMain.getResources().getDrawable(R.drawable.green_border));
                            break;
                        default:
                            shelf.setBackground(mMain.getResources().getDrawable(R.drawable.blue_border));
                    }
                    layout_i.addView(shelf);
                }
                rack[r].addView(layout_i);
            }
        }

        createCartMode();

    }

    private void createCartMode(){
        LinearLayout cart_mode = (LinearLayout) mMain.findViewById(R.id.cart_mode);

        int box_size = dp_to_pixels(120);

        String[] cart_tags = experimentData.getCart_tags();
        for(int i = 0 ; i < cart_tags.length ; i++){
            LinearLayout.LayoutParams rlparams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            int margin = dp_to_pixels(10);
            rlparams.setMargins(margin, 0, margin, 0);
            RelativeLayout rl = new RelativeLayout(mMain);
            rl.setLayoutParams(rlparams);
            rl.setTag(cart_tags[i]);


            RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(
                    box_size, box_size);
            imgParams.addRule(CENTER_IN_PARENT);
            ImageView imageView = new ImageView(mMain);
            imageView.setLayoutParams(imgParams);
            imageView.setImageDrawable(mMain.getResources().getDrawable(R.drawable.bin_empty));
            rl.addView(imageView);

            RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.addRule(CENTER_IN_PARENT);

            TextView textView = new TextView(mMain);
            textView.setLayoutParams(textParams);
            textView.setTextColor(white);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextSize(dp_to_pixels(33));
            int paddingTop = dp_to_pixels(35);
            textView.setPadding(0, paddingTop, 0, 0);
            textView.setShadowLayer(15,1,1,black);
            rl.addView(textView);


            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            lparams.gravity = Gravity.BOTTOM;

            LinearLayout item_list = new LinearLayout(mMain);
            item_list.setLayoutParams(lparams);
            item_list.setOrientation(LinearLayout.VERTICAL);
            item_list.setVisibility(GONE);
            item_list.setGravity(Gravity.BOTTOM);
            int padding = dp_to_pixels(10);
            item_list.setPadding(0,padding,0,padding);
            rl.addView(item_list);


            RelativeLayout.LayoutParams lp_arrow = new RelativeLayout.LayoutParams(dp_to_pixels(50), dp_to_pixels(60));
            lp_arrow.addRule(CENTER_IN_PARENT);
            //lp_arrow.bottomMargin = dp_to_pixels(50);

            ImageView arrow = new ImageView(mMain);
            arrow.setLayoutParams(lp_arrow);
            arrow.setImageDrawable(mMain.getResources().getDrawable(R.drawable.arrow_red));
            arrow.setVisibility(GONE);

            rl.addView(arrow);

            cart_mode.addView(rl);
        }
    }


    //ACTIVE
    public void clearBin(String tag){
        TextView textView = (TextView) this.findViewWithTag(tag);
        textView.setTextColor(dark_gray);

        int row = Integer.parseInt(String.valueOf((tag).charAt(1)));
        switch (row) {
            case 1:
                textView.setBackground(mMain.getResources().getDrawable(R.drawable.red_border));
                break;
            case 2:
                textView.setBackground(mMain.getResources().getDrawable(R.drawable.yellow_border));
                break;
            case 3:
                textView.setBackground(mMain.getResources().getDrawable(R.drawable.green_border));
                break;
            default:
                textView.setBackground(mMain.getResources().getDrawable(R.drawable.blue_border));
                break;
        }

        textView.setText(tag);
        textView.setTypeface(Typeface.DEFAULT);
    }

    public void wrongBin(String tag){
        TextView textView = (TextView) this.findViewWithTag(tag);
        textView.setTextColor(red);

        textView.setText(" X ");
        textView.setTypeface(Typeface.DEFAULT_BOLD);
    }


    public void newOrder(PickingOrder pickingOrder){
        clearRack();

        setRackMode();

        TextView task_no = (TextView) mMain.findViewById(R.id.task_no);
        TextView order_no = (TextView) mMain.findViewById(R.id.order_no);


        task_no.setText(""+(pickingOrder.getTaskID()+1));
        order_no.setText(""+(pickingOrder.getID()+1));

        HashMap<String,Integer> item_map = pickingOrder.getItems();

        Iterator it = item_map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            setBin((String)pair.getKey(), (Integer)pair.getValue());
        }
    }

    private void clearRack(){
        String[][] tags = experimentData.getRack_tags();
        for(int i = 0 ; i < tags.length ; i++){
            for(int j = 0 ; j < tags[i].length; j++){
                clearBin(tags[i][j]);
            }
        }
    }

    public void setCartMode(String cart_tag, int num_items) {
        LinearLayout lp = (LinearLayout) mMain.findViewById(R.id.rack_mode);
        lp.setVisibility(GONE);
        lp = (LinearLayout) mMain.findViewById(R.id.cart_mode);
        lp.setVisibility(VISIBLE);

        String[] cart_tags = experimentData.getCart_tags();
        for(int i = 0 ; i < cart_tags.length ; i++){
            RelativeLayout rl = (RelativeLayout) this.findViewWithTag(cart_tags[i]);
            LinearLayout.LayoutParams lpi = (LinearLayout.LayoutParams) rl.getLayoutParams();
            ImageView imgView = (ImageView) rl.getChildAt(0);
            imgView.setImageDrawable(mMain.getResources().getDrawable(R.drawable.bin_empty));
            RelativeLayout.LayoutParams rlp = (LayoutParams) imgView.getLayoutParams();
            rlp.removeRule(ALIGN_PARENT_BOTTOM);
            rlp.addRule(CENTER_IN_PARENT);
            imgView.setLayoutParams(rlp);
            TextView textView = (TextView) rl.getChildAt(1);
            textView.setText("");
            LinearLayout ll = (LinearLayout) rl.getChildAt(2);
            ll.setVisibility(GONE);
            ImageView arrow = (ImageView) rl.getChildAt(3);
            arrow.setVisibility(GONE);

            if(cart_tag.equals(cart_tags[i])){
                imgView.setImageDrawable(mMain.getResources().getDrawable(R.drawable.bin_full));
                textView.setText(""+num_items);
            }

        }
    }

    public void setErrorMode(PickingOrder pickingOrder, String wrongTag){

        LinearLayout title_layout = (LinearLayout) mMain.findViewById(R.id.title_layout);
        title_layout.setVisibility(GONE);

        LinearLayout lp = (LinearLayout) mMain.findViewById(R.id.rack_mode);
        lp.setVisibility(GONE);
        lp = (LinearLayout) mMain.findViewById(R.id.cart_mode);
        lp.setVisibility(VISIBLE);
        lp.setClipChildren(false);
        lp.setClipToPadding(false);


        String[] cart_tags = experimentData.getCart_tags();
        for(int i = 0 ; i < cart_tags.length ; i++){
            RelativeLayout rl = (RelativeLayout) this.findViewWithTag(cart_tags[i]);
            rl.setClipChildren(false);
            rl.setClipToPadding(false);

            ImageView imgView = (ImageView) rl.getChildAt(0);
            imgView.setImageDrawable(mMain.getResources().getDrawable(R.drawable.bin_empty));
            RelativeLayout.LayoutParams rlp = (LayoutParams) imgView.getLayoutParams();
            rlp.addRule(ALIGN_PARENT_BOTTOM);
            imgView.setLayoutParams(rlp);
            TextView textView = (TextView) rl.getChildAt(1);
            textView.setText("");

            LinearLayout itemList = (LinearLayout) rl.getChildAt(2);
            itemList.removeAllViews();
            itemList.setVisibility(VISIBLE);
            itemList.setClipChildren(false);
            itemList.setClipToPadding(false);


            ImageView arrow = (ImageView) rl.getChildAt(3);
            arrow.setVisibility(GONE);


            if(cart_tags[i].equals(pickingOrder.getReceiveBinTag())) {
                HashMap<String, Integer> items = pickingOrder.getBackupItems();
                Iterator iter = items.entrySet().iterator();
                int count = 0;
                while (iter.hasNext()) {
                    Map.Entry pair = (Map.Entry) iter.next();
                    String tag = (String) pair.getKey();


                    LinearLayout.LayoutParams lp_entry = new LinearLayout.LayoutParams(dp_to_pixels(50), dp_to_pixels(65));
                    lp_entry.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

                    LinearLayout ll_entry = new LinearLayout(mMain);
                    ll_entry.setLayoutParams(lp_entry);
                    ll_entry.setOrientation(LinearLayout.VERTICAL);
                    ll_entry.setGravity(Gravity.CENTER);
                    ll_entry.setClipChildren(false);
                    ll_entry.setClipToPadding(false);

                    int img_size_dp = dp_to_pixels(40);
                    LinearLayout.LayoutParams lp_img = new LinearLayout.LayoutParams(img_size_dp, img_size_dp);
                    lp_img.gravity = Gravity.CENTER;

                    int drawable_index = mMain.getResources().getIdentifier(tag.toLowerCase(), "drawable", mMain.getPackageName());
                    ImageView imageView = new ImageView(mMain);
                    imageView.setLayoutParams(lp_img);
                    imageView.setImageDrawable(mMain.getResources().getDrawable(drawable_index));
                    imageView.setBackgroundColor(red);
                    ll_entry.addView(imageView);

                    int[] mult_size_dp = {dp_to_pixels(30), dp_to_pixels(20)};
                    LinearLayout.LayoutParams lp_mult = new LinearLayout.LayoutParams(mult_size_dp[0], mult_size_dp[1]);
                    lp_mult.gravity = Gravity.CENTER;
                    TextView multiplier = new TextView(mMain);
                    multiplier.setLayoutParams(lp_mult);
                    multiplier.setText("x" + pair.getValue());
                    multiplier.setTextSize(dp_to_pixels(10));
                    multiplier.setTextColor(white);
                    multiplier.setGravity(Gravity.CENTER);
                    ll_entry.addView(multiplier);

                    Log.d(TAG, itemList.getChildCount() + "");
                    if (count == 2 || count == 4 || count == 5) {
                        LinearLayout last_row = (LinearLayout) itemList.getChildAt(0);
                        last_row.addView(ll_entry);
                    } else {
                        LinearLayout.LayoutParams lp_row = new LinearLayout.LayoutParams(dp_to_pixels(150), dp_to_pixels(65));
                        lp_row.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

                        LinearLayout ll_row = new LinearLayout(mMain);
                        ll_row.setLayoutParams(lp_row);
                        ll_row.setOrientation(LinearLayout.HORIZONTAL);
                        ll_row.setGravity(Gravity.CENTER);
                        ll_row.setClipChildren(false);
                        ll_row.setClipToPadding(false);

                        ll_row.addView(ll_entry);

                        itemList.addView(ll_row, 0);
                    }
                    count++;
                }
            }else if (cart_tags[i].equals(wrongTag)){
                arrow.setVisibility(VISIBLE);
            }

        }

        TextView subtitle = (TextView) mMain.findViewById(R.id.subtitle);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int recv_tag = Integer.parseInt(""+pickingOrder.getReceiveBinTag().charAt(2));
        switch (recv_tag){
            case 3:
                layoutParams.gravity = Gravity.LEFT;
                break;
            default:
                layoutParams.gravity = Gravity.RIGHT;
                break;
        }
        subtitle.setLayoutParams(layoutParams);
        subtitle.setVisibility(VISIBLE);

    }


    public void setRackMode(){
        LinearLayout lp = (LinearLayout) mMain.findViewById(R.id.rack_mode);
        lp.setVisibility(VISIBLE);
        lp = (LinearLayout) mMain.findViewById(R.id.cart_mode);
        lp.setVisibility(GONE);

        TextView subtitle = (TextView) mMain.findViewById(R.id.subtitle);
        subtitle.setVisibility(GONE);

        LinearLayout title_layout = (LinearLayout) mMain.findViewById(R.id.title_layout);
        title_layout.setVisibility(VISIBLE);
    }

    private void setBin(String tag, int val){
        TextView textView = (TextView) this.findViewWithTag(tag);

        int row = Integer.parseInt(String.valueOf((tag.charAt(1))));
        switch (row) {
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

        textView.setText(""+val);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
    }

    //HELPER FUNCTIONS
    private int dp_to_pixels(float dp){
        return (int) ((dp)*mMain.getResources().getDisplayMetrics().density +0.5f);
    }

}