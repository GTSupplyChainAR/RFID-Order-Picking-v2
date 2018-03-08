package com.thad.rfid_lib.Data;

/**
 * Created by theo on 2/28/18.
 */

public class ShelvingUnit {
    private String tag;
    private int rows, cols;
    private boolean isCart;

    public ShelvingUnit(String tag, int rows, int cols, boolean isCart){
        this.tag = tag;
        this.rows = rows;
        this.cols = cols;
        this.isCart = isCart;
    }

    public int[] getDimensions(){return new int[]{rows, cols};}
    public String getTag(){return tag;}
    public boolean isCart(){return isCart;}

}
