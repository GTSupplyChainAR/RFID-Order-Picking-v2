package com.thad.rfid_lib;


import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.thad.rfid_lib.Data.PickingData;
import com.thad.rfid_lib.Data.PickingOrder;
import com.thad.rfid_lib.Data.PickingTask;
import com.thad.rfid_lib.Data.ShelvingUnit;
import com.thad.rfid_lib.Data.WarehouseData;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_lib.UI.ExperimentView;
import com.thad.rfid_lib.UI.ExperimentViewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by theo on 2/22/18.
 */

public class Experiment {
    private static final String TAG = "|Experiment|";
    private enum STATES {
        BLOCKED,
        READY,
        ACTIVE,
        ERROR,
        PAUSED
    }

    private ExperimentListener mClient;

    private STATES state;
    private WarehouseData warehouseData;
    private PickingData pickingData;

    private ExperimentView experimentView;

    private int active_shelving_unit = 0;
    private PickingOrder activeOrder;
    private HashMap<String, Integer> itemsOnHand;
    private HashMap<String, Boolean> wrongScans;

    public Experiment(ExperimentListener client){
        warehouseData = new WarehouseData();
        pickingData = new PickingData();
        wrongScans = new HashMap<String, Boolean>();
        itemsOnHand = new HashMap<String, Integer>();

        state = STATES.BLOCKED;
        mClient = client;
    }


    //COMMANDS
    public boolean start(){
        if(state != STATES.READY)
            return false;

        print("Experiment Starting...");
        state = STATES.ACTIVE;

        experimentView = new ExperimentView(this);
        experimentView.setData(warehouseData);
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContainer().addView(experimentView);
            }
        });

        nextOrder();

        return true;
    }
    public boolean stop(){
        if(state != STATES.ACTIVE && state != STATES.ERROR)
            return false;

        print("Experiment Stopping...");
        state = STATES.READY;

        reset();

        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContainer().removeAllViews();
            }
        });

        return true;
    }

    public void reset(){
        pickingData.reset();
        itemsOnHand.clear();
        wrongScans.clear();
        activeOrder = null;
    }

    public void onNewScan(String tag){
        if(state != STATES.ACTIVE)
            return;

        print("New Scan - "+tag);

        boolean isScanValid = checkErrors(tag);

        if(!isScanValid)
            return;

        String letterTag = String.valueOf(tag.charAt(0));
        int row = Character.getNumericValue(tag.charAt(1));
        int col = Character.getNumericValue(tag.charAt(2));
        int[] pos = new int[]{row-1, col-1};
        ShelvingUnit scannedUnit = warehouseData.getUnitByTag(letterTag);


        if(!scannedUnit.isCart()){
            itemsOnHand.put(tag, activeOrder.getBinItemCount(tag));
            activeOrder.scan(tag);
            experimentView.getRackUI().emptyCell(pos);
            experimentView.checkCell(pos);
            mClient.playSound(Utils.SOUNDS.CLICK);
        }else{
            itemsOnHand.clear();
            int remainingItems = activeOrder.getRemainingCountInShelvingUnit(warehouseData.get(active_shelving_unit));
            if(remainingItems == 0){
                print("Order Done.");
                mClient.playSound(Utils.SOUNDS.SUCCESS);
                nextOrder();
            }else{
               experimentView.getCartUI().setCellText(pos, ""+remainingItems);
                mClient.playSound(Utils.SOUNDS.CLICK);
            }
        }
    }

    private boolean checkErrors(String tag){
        String letterTag = Utils.tagToLetter(tag);
        int[] pos = Utils.tagToPos(tag);

        PickingOrder pickingOrder = activeOrder;
        ShelvingUnit scannedUnit = warehouseData.getUnitByTag(letterTag);

        //Check if scan is valid
        if(scannedUnit == null || pos[0] >= scannedUnit.getDimensions()[0] || pos[0] < 0
                || pos[1] >= scannedUnit.getDimensions()[1] || pos[1] < 0) {
            print("Invalid Scan rejected.");
            return false;
        }

        //Check if scanned incorrect Rack
        if(!(scannedUnit.getTag()).equals(warehouseData.get(active_shelving_unit).getTag())
                && !(scannedUnit.getTag()).equals(warehouseData.getCart().getTag())){
            print("Scanned wrong rack.");
            mClient.playSound(Utils.SOUNDS.ERROR);
            return false;
        }

        //Check if items were dropped in correct receive bin
        if(scannedUnit.isCart() && !pickingOrder.getReceiveBinTag().equals(tag)){
            if(itemsOnHand.size() > 0) {
                print("Items in the wrong receive bin.");
                state = STATES.ERROR;
                mClient.playSound(Utils.SOUNDS.ERROR);
                experimentView.displayOverlay(itemsOnHand, pickingOrder.getReceiveBinTag(), tag);
            }
            return false;
        }

        if(pickingOrder.hadTag(tag)){
            print("Item was already picked.");
            return false;
        }

        //Check if correct items were picked up from rack
        if(!scannedUnit.isCart() && !pickingOrder.hasTag(tag)){
            if(!wrongScans.containsKey(tag)){
                print("Wrong item picked up.");
                mClient.playSound(Utils.SOUNDS.ERROR);
                wrongScans.put(tag, true);
            }else{
                print("Error corrected.");
                mClient.playSound(Utils.SOUNDS.CLICK);
                wrongScans.remove(tag);
            }
            experimentView.toggleError(pos);
            return false;
        }

        return true;
    }


    public void nextOrder(){
        if(state != STATES.ACTIVE)
            return;

        if(activeOrder == null) {
            activeOrder = pickingData.getTask(0).getOrder(0);
            active_shelving_unit = 0;
            renderActiveOrder();
            return;
        }

        wrongScans.clear();

        PickingTask activeTask = activeOrder.getTask();
        if(activeOrder.getID() == activeTask.getLastOrder().getID()){
            if(active_shelving_unit >= warehouseData.getShelvingUnitCount()-1){
                if(activeTask.getID() == pickingData.getLastTask().getID()){
                    state = STATES.PAUSED;
                    return;
                }
                active_shelving_unit = 0;
                activeOrder = pickingData.getNextTask(activeTask).getOrder(0);
            }else{
                active_shelving_unit ++;
                activeOrder = activeTask.getOrder(0);
            }
            experimentView.changeShelvingUnit(active_shelving_unit);
        }else{
            activeOrder = activeTask.getNextOrder(activeOrder);
        }

        int remainingItems = activeOrder.getRemainingCountInShelvingUnit(warehouseData.get(active_shelving_unit));
        if(remainingItems != 0) {
            experimentView.setTitle(activeOrder);
            renderActiveOrder();
        }else
            nextOrder();
    }


    private void renderActiveOrder(){
        experimentView.emptyAllCells();
        Map map = activeOrder.getRemainingItems();
        Set<String> tags = map.keySet();
        for(String tag : tags){
            String shelvingUnitTag = Utils.tagToLetter(tag);
            int[] pos = Utils.tagToPos(tag);
            if(shelvingUnitTag.equals(warehouseData.get(active_shelving_unit).getTag())) {
                experimentView.getRackUI().fillCell(pos);
                experimentView.getRackUI().setCellText(pos, ""+map.get(tag));
            }
        }
        String receiveBinTag = activeOrder.getReceiveBinTag();
        int[] pos = Utils.tagToPos(receiveBinTag);
        experimentView.getCartUI().fillCell(pos);
        int remainingItems = activeOrder.getRemainingCountInShelvingUnit(warehouseData.get(active_shelving_unit));
        experimentView.getCartUI().setCellText(pos, ""+remainingItems);

        experimentView.setTitle(activeOrder);
    }


    //SETTERS
    public void setData(WarehouseData warehouseData, PickingData pickingData){
        setData(warehouseData);
        setData(pickingData);
    }
    public void setData(WarehouseData warehouseData){
        this.warehouseData = warehouseData;
        print(warehouseData.toString());
        if(pickingData != null)
           state = STATES.READY;
    }
    public void setData(PickingData pickingData){
        this.pickingData = pickingData;
        print(pickingData.toString());
        if(warehouseData != null)
            state = STATES.READY;
    }


    //UTILS
    public boolean isActive(){return state == STATES.ACTIVE || state == STATES.ERROR;}


    //CALLBACKS
    public void print(String str){
        mClient.mLog(str);
    }
    public Context getContext(){return mClient.getContext();}
    public Activity getActivity(){return (Activity)mClient.getContext();}
    public ViewGroup getContainer(){return mClient.getExperimentContainer();}
    public WarehouseData getWarehouseData(){return warehouseData;}
    public PickingData getPickingData(){return pickingData;}
    public boolean isGlass(){return mClient.isGlass();}
    public void onFakeScan(String scan){mClient.onFakeScan(scan);}

}
