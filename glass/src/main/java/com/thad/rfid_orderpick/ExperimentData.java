package com.thad.rfid_orderpick;

/**
 * Created by theo on 2/1/18.
 */

public class ExperimentData {
    private boolean dataReady = false;
    private String[][] rack_tags;
    private String[] cart_tags;

    public ExperimentData(){}
    public ExperimentData(String[][] racks, String[] carts){
        rack_tags = racks;
        cart_tags = carts;
        dataReady = true;
    }

    public void setTags(String[][] rack_tags, String[] cart_tags){
        this.rack_tags = rack_tags;
        this.cart_tags = cart_tags;
        dataReady = true;
    }

    public String[][] getRack_tags(){
        return rack_tags;
    }
    public String[] getCart_tags(){
        return cart_tags;
    }
    public int[] getDimensions(){
        if(!dataReady) return null;
        return new int[]{rack_tags.length, rack_tags[0].length};
    }
}
