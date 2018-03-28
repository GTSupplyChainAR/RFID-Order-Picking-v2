package com.thad.rfid_lib.Experiment;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.thad.rfid_lib.Data.PickingData;
import com.thad.rfid_lib.Data.PickingOrder;
import com.thad.rfid_lib.Data.PickingTask;
import com.thad.rfid_lib.Data.ShelvingUnit;
import com.thad.rfid_lib.Data.WarehouseData;
import com.thad.rfid_lib.Static.Utils;
import com.thad.rfid_lib.UI.ExperimentView;

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
    private PickingData pickingDataTraining, pickingDataTesting, pickingData;

    private ExperimentView experimentView;
    private ExperimentLog experimentLog;
    private boolean isStudyRunning;

    private Long startTime;
    private int active_shelving_unit = 0;
    private PickingOrder activeOrder;
    private HashMap<String, Integer> itemsOnHand;
    private HashMap<String, Boolean> wrongScans;

    public Experiment(ExperimentListener client){
        warehouseData = new WarehouseData();
        pickingDataTraining = new PickingData();
        pickingDataTesting = new PickingData();
        pickingData = new PickingData();
        wrongScans = new HashMap<String, Boolean>();
        itemsOnHand = new HashMap<String, Integer>();

        state = STATES.BLOCKED;
        isStudyRunning = false;
        mClient = client;
    }


    //COMMANDS
    public boolean startTraining(){
        pickingData = pickingDataTraining;
        return start();
    }
    public boolean startTesting(){
        pickingData = pickingDataTesting;
        return start();
    }
    private boolean start(){
        if(isActive())
            return false;

        print("Experiment Starting...");
        startTime = System.currentTimeMillis();
        state = STATES.ACTIVE;

        experimentView = new ExperimentView(this);
        experimentView.setData(warehouseData);
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContainer().addView(experimentView);
            }
        });

        //Log
        if(!mClient.isGlass() && mClient.isStudyRunning()){
            experimentLog = mClient.getExperimentLog();
            experimentLog.setDataVersion(pickingData.getVersion());
            experimentLog.startLog(this);
            mClient.autosave();
            isStudyRunning = true;
        }

        nextOrder();

        return true;
    }
    public boolean stop(){
        if(!isActive())
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
        if(isStudyRunning) {
            isStudyRunning = false;
            experimentLog.closeLog();
        }
        startTime = null;
        pickingData.reset();
        itemsOnHand.clear();
        wrongScans.clear();
        activeOrder = null;
    }

    public void onNewScan(String tag){
        if(state != STATES.ACTIVE)
            return;

        print("New Scan - "+tag);
        log("SCAN "+tag);

        boolean isScanValid = checkErrors(tag);

        if(!isScanValid)
            return;

        String letterTag = Utils.tagToLetter(tag);
        int[] pos = Utils.tagToPos(tag);
        ShelvingUnit scannedUnit = warehouseData.getUnitByTag(letterTag);


        if(!scannedUnit.isCart()){
            itemsOnHand.put(tag, activeOrder.getBinItemCount(tag));
            activeOrder.scan(tag);
            //experimentView.getRackUI().emptyCell(pos);
            experimentView.checkCell(pos);
            mClient.playSound(Utils.SOUNDS.CLICK);
            log("VALID_RACK");
        }else{
            log("VALID_CART "+itemsOnHand.size()+" "+Utils.countInHashmap(itemsOnHand));

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
            log("INVALID");
            return false;
        }

        //Check if scanned incorrect Rack
        if(!(scannedUnit.getTag()).equals(warehouseData.get(active_shelving_unit).getTag())
                && !(scannedUnit.getTag()).equals(warehouseData.getCart().getTag())){
            print("Scanned wrong rack.");
            log("INVALID_RACK");
            mClient.playSound(Utils.SOUNDS.ERROR);
            experimentView.getRackUI().glow();
            return false;
        }

        //Check if items were dropped in correct receive bin
        if(scannedUnit.isCart() && !pickingOrder.getReceiveBinTag().equals(tag)){
            if(itemsOnHand.size() > 0) {
                print("Items in the wrong receive bin.");
                log("INVALID_RECEIVE_BIN "+tag+" "+pickingOrder.getReceiveBinTag());
                state = STATES.ERROR;
                mClient.playSound(Utils.SOUNDS.ERROR);
                experimentView.displayOverlay(itemsOnHand, pickingOrder.getReceiveBinTag(), tag);
            }
            return false;
        }

        if(pickingOrder.hadTag(tag)){
            log("INVALID_ALREADY_PICKED");
            print("Item was already picked.");
            return false;
        }

        //Check if correct items were picked up from rack
        if(!scannedUnit.isCart() && !pickingOrder.hasTag(tag)){
            if(!wrongScans.containsKey(tag)){
                print("Wrong item picked up.");
                log("INVALID_ITEM");
                mClient.playSound(Utils.SOUNDS.ERROR);
                wrongScans.put(tag, true);
            }else{
                print("Error corrected.");
                log("INVALID_ITEM_CORRECTED");
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

            log("EXPERIMENT_STARTED - "+((pickingData.isTraining())?"TRAINING":"TESTING"));
            log("TASK_STARTED " + activeOrder.getTaskID());
            log("SUB_ORDER_STARTED " + activeOrder.getID() + "_"
                        + warehouseData.get(active_shelving_unit).getTag());
            return;
        }


        wrongScans.clear();

        PickingTask activeTask = activeOrder.getTask();
        int order_id = activeOrder.getID(), task_id = activeTask.getID();
        String unitTag = warehouseData.get(active_shelving_unit).getTag();

        if(activeOrder.getID() == activeTask.getLastOrder().getID()){
            if(active_shelving_unit >= warehouseData.getShelvingUnitCount()-1){
                log("SUB_ORDER_COMPLETED " + order_id + "_" + unitTag);
                log("TASK_COMPLETED " + task_id);
                if(activeTask.getID() == pickingData.getLastTask().getID()){
                    state = STATES.PAUSED;
                    renderActiveOrder();
                    log("EXPERIMENT_ENDED");
                    return;
                }
                active_shelving_unit = 0;
                activeOrder = pickingData.getNextTask(activeTask).getOrder(0);
                log("TASK_STARTED " + activeOrder.getTaskID());
                log("SUB_ORDER_STARTED " + activeOrder.getID() + "_" +
                            warehouseData.get(0).getTag());
            }else{
                log("SUB_ORDER_COMPLETED "+order_id+"_"+unitTag);
                active_shelving_unit ++;
                activeOrder = activeTask.getOrder(0);
                log("SUB_ORDER_STARTED "+activeOrder.getID()+"_"+
                                warehouseData.get(active_shelving_unit).getTag());
            }
            experimentView.changeShelvingUnit(active_shelving_unit);
        }else{
            log("SUB_ORDER_COMPLETED "+order_id+"_"+unitTag);
            activeOrder = activeTask.getNextOrder(activeOrder);
            log("SUB_ORDER_STARTED "+activeOrder.getID()+"_"+unitTag);
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
    public void setData(WarehouseData warehouseData, PickingData pickingDataTraining, PickingData pickingDataTesting){
        setData(warehouseData);
        setData(pickingDataTraining, pickingDataTesting);
    }
    public void setData(WarehouseData warehouseData){
        this.warehouseData = warehouseData;
        print(warehouseData.toString());
        if(pickingData != null)
           state = STATES.READY;
    }
    public void setData(PickingData pickingDataTraining, PickingData pickingDataTesting){
        this.pickingDataTraining = pickingDataTraining;
        this.pickingDataTesting = pickingDataTesting;
        this.pickingData = pickingDataTraining;
        print("Tasks: "+pickingDataTraining.getTaskCount()+" training and "
                        +pickingDataTesting.getTaskCount()+" picking.");
        if(warehouseData != null)
            state = STATES.READY;
    }


    public void errorFixed(){
        if(state != STATES.ERROR)
            return;
        log("ERROR_CORRECTED");
        state = STATES.ACTIVE;
        experimentView.hideOverlay();
        experimentView.getCartUI().emptyAll();
        experimentView.getCartUI().fillCell(Utils.tagToPos(activeOrder.getReceiveBinTag()));
        onNewScan(activeOrder.getReceiveBinTag());
    }

    //UTILS
    public boolean isActive(){return state == STATES.ACTIVE || state == STATES.ERROR || state == STATES.PAUSED;}
    public Long getElapsedTime(){
        if(startTime == null)
            return null;
        return System.currentTimeMillis() - startTime;
    }
    public void log(String str){
        if(isStudyRunning)
            experimentLog.addLine(str);
    }


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
    public void onWrongScan(){
        String fakeScan = warehouseData.get(
                (active_shelving_unit+1)%warehouseData.getShelvingUnitCount()).getTag()+"11";
        onFakeScan(fakeScan);
    }

}
