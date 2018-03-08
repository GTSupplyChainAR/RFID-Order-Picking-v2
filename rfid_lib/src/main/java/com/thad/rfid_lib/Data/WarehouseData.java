package com.thad.rfid_lib.Data;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by theo on 2/1/18.
 */

public class WarehouseData {
    private static final String TAG = "|WarehouseData|";
    private String json_string, version;

    private ArrayList<ShelvingUnit> rackShelvingUnits;
    private ShelvingUnit cart;

    public WarehouseData(){
        rackShelvingUnits = new ArrayList<ShelvingUnit>();
    }

    public void addShelvingUnit(ShelvingUnit shelvingUnit){
        if(shelvingUnit.isCart())
            cart = shelvingUnit;
        else
            rackShelvingUnits.add(shelvingUnit);
    }

    public int getShelvingUnitCount(){return rackShelvingUnits.size();}
    public ShelvingUnit get(int index){return rackShelvingUnits.get(index);}
    public ShelvingUnit getCart(){return cart;}
    public ShelvingUnit getUnitByTag(String tag){
        if(cart.getTag().equals(tag))
            return cart;
        for(ShelvingUnit rackUnit : rackShelvingUnits){
            if(rackUnit.getTag().equals(tag))
                return rackUnit;
        }
        return null;
    }

    public void setJSON(String json_string){this.json_string = json_string;}
    public void setVersion(String version){this.version = version;}
    public String getVersion(){return version;}
    public String getJSON(){return json_string;}
    public String toString(){
        return "The warehouse has "+rackShelvingUnits.size()+" x "
                +Arrays.toString(rackShelvingUnits.get(0).getDimensions())
                +" shelving unit(s), and a " +Arrays.toString(cart.getDimensions())
                +" cart.";
    }
}
