package com.thad.rfid_lib.UI;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thad.rfid_lib.Data.PickingOrder;
import com.thad.rfid_lib.Data.ShelvingUnit;
import com.thad.rfid_lib.Data.WarehouseData;
import com.thad.rfid_lib.Experiment;
import com.thad.rfid_lib.Static.Prefs;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_lib.UIRunnables.AddViewRunnable;
import com.thad.rfid_lib.UIRunnables.RemoveAllRunnable;
import com.thad.rfid_lib.UIRunnables.SetTextRunnable;

import java.util.HashMap;

/**
 * Created by theo on 2/28/18.
 */

public class ExperimentView extends LinearLayout {
    private static final String TAG = "|ExperimentView|";

    //LAYOUT PARAMS
    private static final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    private Context context;
    private Experiment experiment;

    private WarehouseData warehouseData;
    private ShelvingUnitUI rackUI, cartUI;
    private ExperimentViewOverlay overlay;
    private int activeShelvingUnit;

    //VIEWS
    private TextView title;


    public ExperimentView(Experiment experiment) {
        super(experiment.getContext());
        this.context = experiment.getContext();
        this.experiment = experiment;
        init();
    }

    private void init(){
        this.setLayoutParams(new LayoutParams(MP, WC));
        this.setOrientation(VERTICAL);
    }

    public void closeOverlay(){
        overlay.setVisibility(GONE);
        overlay.removeAllViews();
    }

    public void displayOverlay(HashMap<String, Integer> itemsOnHand, String correctBinTag, String wrongBinTag){
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                overlay.setVisibility(VISIBLE);
            }
        });

        String correctLetterTag = Utils.tagToLetter(correctBinTag);
        String wrongLetterTag = Utils.tagToLetter(wrongBinTag);
        if(!wrongLetterTag.equals(correctLetterTag))
            return;

        int[] correctPos = Utils.tagToPos(correctBinTag);
        int[] wrongPos = Utils.tagToPos(wrongBinTag);

        Log.d(TAG, "Error mode: Items on Hand-> "+itemsOnHand.size());
        Log.d(TAG, "Should have been put into "+correctBinTag+" but were put into " + wrongBinTag+" instead.");

        cartUI.toggleError(wrongPos);
        overlay.fill(itemsOnHand, correctPos, wrongPos);
    }

    public void hideOverlay(){
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                overlay.setVisibility(GONE);
                overlay.removeAllViews();
            }
        });
    }

    private void createViews(){
        ((Activity)context).runOnUiThread(new RemoveAllRunnable(this));

        RelativeLayout mainLayout = new RelativeLayout(context);
        mainLayout.setLayoutParams(new LayoutParams(Prefs.SCREEN_WIDTH,
                (int)(Prefs.SCREEN_WIDTH*Prefs.SCREEN_RATIO)));//MP, WC));

        LinearLayout rackLayout = generateRackLayout();
        LinearLayout cartLayout = generateCartLayout();
        title = generateTitle();

        overlay = new ExperimentViewOverlay(experiment);
        overlay.setVisibility(GONE);

        mainLayout.addView(rackLayout);
        mainLayout.addView(overlay);
        mainLayout.addView(cartLayout);
        mainLayout.addView(title);


        ((Activity)context).runOnUiThread(new AddViewRunnable(this, mainLayout));
    }

    private LinearLayout generateRackLayout(){
        ShelvingUnit shelvingUnit = warehouseData.get(activeShelvingUnit);
        rackUI = new ShelvingUnitUI(experiment, shelvingUnit);
        addFadedNeighbors(rackUI);

        rackUI.generateUI();
        return rackUI;
    }

    private LinearLayout generateCartLayout(){
        cartUI = new ShelvingUnitUI(experiment, warehouseData.getCart());
        cartUI.generateUI();

        return cartUI;
    }

    private TextView generateTitle(){
        TextView titleText = new TextView(context);
        RelativeLayout.LayoutParams titleLP = new RelativeLayout.LayoutParams(
                (int)(Prefs.SCREEN_WIDTH*(1-Prefs.RACK_SCREEN_PERCENTAGE)),
                (int)(Prefs.SCREEN_WIDTH*Prefs.SCREEN_RATIO*(1-Prefs.TITLE_CART_PERCENTAGE)));
        titleLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        titleLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        titleText.setLayoutParams(titleLP);
        titleText.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        int padding_px = Utils.dp_to_pixels(context, 5);
        titleText.setPadding(padding_px, padding_px, padding_px, padding_px);
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);

        titleText.setTextColor(Color.WHITE);
        titleText.setText("Task "+"1"+" - "+"Order "+"1");//+" ("+"TRAINING"+")");

        titleText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                experiment.errorFixed();
            }
        });

        return titleText;
    }

    private void addFadedNeighbors(ShelvingUnitUI shelvingUnitUI){
        int unitCount = warehouseData.getShelvingUnitCount();
        Log.d(TAG, "Adding Faded Neighbors, "+activeShelvingUnit+"/ "+unitCount);
        if(activeShelvingUnit == 0 && unitCount >= 2){
            shelvingUnitUI.addFadedNeighbor(ShelvingUnitUI.NEIGHBOR.RIGHT);
        }else if(activeShelvingUnit == unitCount-1 && unitCount >= 2){
            shelvingUnitUI.addFadedNeighbor(ShelvingUnitUI.NEIGHBOR.LEFT);
        }else if(unitCount >= 3){
            shelvingUnitUI.addFadedNeighbor(ShelvingUnitUI.NEIGHBOR.RIGHT);
            shelvingUnitUI.addFadedNeighbor(ShelvingUnitUI.NEIGHBOR.LEFT);
        }
    }


    //COMMANDS
    public ShelvingUnitUI getRackUI(){return rackUI;}
    public ShelvingUnitUI getCartUI(){return cartUI;}
    public void setData(WarehouseData warehouseData){
        this.warehouseData = warehouseData;
        activeShelvingUnit = 0;
        createViews();
    }
    public void emptyAllCells(){
        rackUI.emptyAll();
        cartUI.emptyAll();
    }
    public void toggleError(int[] pos){
        rackUI.toggleError(pos);
    }
    public void checkCell(int[] pos){
        rackUI.checkCell(pos);
    }
    public void setTitle(PickingOrder pickingOrder){
        ((Activity)context).runOnUiThread(new SetTextRunnable(title,
                "Task "+pickingOrder.getTaskID()+" - Order "+pickingOrder.getID()));
    }
    public void changeShelvingUnit(){
        changeShelvingUnit((activeShelvingUnit+1)%warehouseData.getShelvingUnitCount());
    }
    public void changeShelvingUnit(String tag){
        for(int i = 0 ; i < warehouseData.getShelvingUnitCount() ; i++){
            if(warehouseData.get(i).getTag() == tag){
                changeShelvingUnit(i);
                return;
            }
        }
    }
    public void changeShelvingUnit(int new_ind){
        activeShelvingUnit = new_ind;
        createViews();
    }

}
