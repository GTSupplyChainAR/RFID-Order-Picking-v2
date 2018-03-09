package com.thad.rfid_lib.UI;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
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

import java.util.HashMap;

/**
 * Created by theo on 3/1/18.
 */

public class ShelvingUnitUI extends LinearLayout {
    private static final String TAG = "|ShelvingUnitUI|";
    //STATIC
    public enum NEIGHBOR{LEFT, RIGHT}
    private static final int[] rack_colors = {
            R.color.red,
            R.color.yellow,
            R.color.green,
            R.color.blue
    };
    private static final int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    //END OF STATIC\
    private int cell_width, cell_height;

    private Context context;
    private Experiment experiment;

    private ShelvingUnit shelvingUnit;
    private CellUI[][] cellUIs;

    private HashMap<NEIGHBOR, Boolean> neighbors;

    public ShelvingUnitUI(Experiment experiment, ShelvingUnit shelvingUnit){
        super(experiment.getContext());
        this.context = experiment.getContext();
        this.experiment = experiment;
        this.shelvingUnit = shelvingUnit;
        neighbors = new HashMap<NEIGHBOR, Boolean>();
        neighbors.put(NEIGHBOR.LEFT, false);
        neighbors.put(NEIGHBOR.RIGHT, false);
    }

    public void generateUI(){
        ViewGroup container;
        if(!shelvingUnit.isCart())
            container = initRack();
        else
            container = initCart();

        generateCells(container);
    }

    private ViewGroup initRack(){
        this.setOrientation(VERTICAL);
        int width = (int)(Prefs.SCREEN_WIDTH*Prefs.RACK_SCREEN_PERCENTAGE);
        int height = (int)(Prefs.SCREEN_WIDTH*Prefs.SCREEN_RATIO);
        this.setLayoutParams(new LayoutParams(width, height));

        cell_width = (int)((1-Prefs.RACK_TAG_PERCENTAGE)*width) / shelvingUnit.getDimensions()[1];
        cell_height = (int)((1-Prefs.FADED_RACKS_PERCENTAGE)*height) / shelvingUnit.getDimensions()[0];
        if(cell_height >= cell_width)
            cell_height = cell_width;
        cell_height *= 0.92;
        cell_width *= 0.92;

        this.addView(generateTagBar(shelvingUnit.getTag()));

        LinearLayout all_neighs_layout = new LinearLayout(context);
        all_neighs_layout.setLayoutParams(new LayoutParams(MP, MP, Prefs.RACK_TAG_PERCENTAGE));
        all_neighs_layout.setOrientation(HORIZONTAL);

        LinearLayout main_rack = new LinearLayout(context);
        main_rack.setLayoutParams(new LayoutParams(WC, WC));//MP, MP, Prefs.FADED_RACKS_PERCENTAGE));
        main_rack.setOrientation(VERTICAL);



        int cnt = 0;
        for(NEIGHBOR neighbor : NEIGHBOR.values()){
            LinearLayout neigh_layout = new LinearLayout(context);
            neigh_layout.setLayoutParams(new LayoutParams(WC, WC));//MP, MP, 1-Prefs.FADED_RACKS_PERCENTAGE));
            neigh_layout.setOrientation(VERTICAL);
            createViewsFadedNeighbor(neighbor, neigh_layout);

            if(neighbors.get(neighbor))
                all_neighs_layout.addView(neigh_layout);

            if(cnt == 0)
                all_neighs_layout.addView(main_rack);
            cnt++;
        }

        this.addView(all_neighs_layout);

        return main_rack;
    }

    private ViewGroup initCart(){
        this.setOrientation(VERTICAL);
        int width = (int)(Prefs.SCREEN_WIDTH*(1-Prefs.RACK_SCREEN_PERCENTAGE));
        int height = (int)(Prefs.SCREEN_WIDTH*Prefs.SCREEN_RATIO*Prefs.TITLE_CART_PERCENTAGE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        this.setLayoutParams(layoutParams);
        //this.setLayoutParams(new LayoutParams(MP, MP, 1-Prefs.TITLE_CART_PERCENTAGE));

        cell_width = width / shelvingUnit.getDimensions()[1];
        cell_height = width / shelvingUnit.getDimensions()[0];
        if(cell_height >= cell_width)
            cell_height = cell_width;
        cell_height *= 0.9;
        cell_width *= 0.9;

        this.setGravity(Gravity.BOTTOM);
        return this;
    }


    private void generateCells(ViewGroup container){
        int[] dims = shelvingUnit.getDimensions();

        cellUIs = new CellUI[dims[0]][dims[1]];

        for(int r = 0 ; r < dims[0] ; r++){
            LinearLayout row_layout = new LinearLayout(context);
            row_layout.setLayoutParams(new LayoutParams(WC, WC));
            row_layout.setOrientation(HORIZONTAL);
            //row_layout.setGravity(Gravity.TOP);

            for(int c = 0 ; c < dims[1] ; c++){
                CellUI cellUI = new CellUI(experiment, shelvingUnit.isCart());

                cellUI.setSize(cell_width, cell_height);
                cellUI.setColor(rack_colors[r%rack_colors.length]);
                cellUI.setTag(shelvingUnit.getTag()+""+(r+1)+""+(c+1));

                cellUI.generate();

                cellUIs[r][c] = cellUI;
                row_layout.addView(cellUI.getView());
            }
            container.addView(row_layout);
        }

    }

    //GENERATE TAG
    private LinearLayout generateTagBar(String tag){

        LinearLayout tag_layout = new LinearLayout(context);
        tag_layout.setLayoutParams(new LayoutParams(MP, MP, 1-Prefs.RACK_TAG_PERCENTAGE));
        int padding_px = Utils.dp_to_pixels(context, 1);
        tag_layout.setPadding(0, 0, 0, 2*padding_px);
        tag_layout.setGravity(Gravity.CENTER|Gravity.BOTTOM);

        if(neighbors.get(NEIGHBOR.RIGHT) && !neighbors.get(NEIGHBOR.LEFT)) {
            tag_layout.setPadding(5*padding_px, 0, 0, 2*padding_px);
            tag_layout.setGravity(Gravity.LEFT|Gravity.BOTTOM);
        }else if(!neighbors.get(NEIGHBOR.RIGHT) && neighbors.get(NEIGHBOR.LEFT)){
            tag_layout.setPadding(0, 0, 12*padding_px, 2*padding_px);
            tag_layout.setGravity(Gravity.RIGHT|Gravity.BOTTOM);
        }

        TextView tag_view = new TextView(context);
        tag_view.setLayoutParams(new LayoutParams(WC, WC));
        tag_view.setBackground(context.getResources().getDrawable(R.drawable.crem_tag_container));
        tag_view.setPadding(12*padding_px, padding_px, 12*padding_px, padding_px);
        tag_view.setTextColor(Color.BLACK);
        int text_size = R.dimen.tag_text;
        if(experiment.isGlass())
            text_size = R.dimen.glass_tag_text;
        tag_view.setTextSize(Utils.dp_to_pixels(context,
                context.getResources().getDimension(text_size)));
        tag_view.setTypeface(Typeface.DEFAULT_BOLD);
        tag_view.setText(tag);
        tag_view.setGravity(Gravity.CENTER);

        tag_layout.addView(tag_view);

        return tag_layout;
    }

    //FADED NEIGHBORS
    public void addFadedNeighbor(NEIGHBOR neighbor){
        neighbors.put(neighbor, true);
    }

    private void createViewsFadedNeighbor(NEIGHBOR neighbor, ViewGroup container){
        int[] dims = shelvingUnit.getDimensions();

        int faded_width = cell_width;
        for(NEIGHBOR neigh : neighbors.keySet())
            if(neighbors.get(neigh))
                faded_width = faded_width/2;

        for(int r = 0 ; r < dims[0] ; r++){
            ImageView cell = new ImageView(context);
            LayoutParams lp_cell = new LayoutParams(faded_width, cell_height);
            int margins = Utils.dp_to_pixels(context, 1.5f);
            lp_cell.setMargins(margins, margins, margins, margins);
            cell.setLayoutParams(lp_cell);

            if(neighbor == NEIGHBOR.RIGHT)
                cell.setBackground(context.getResources().getDrawable(R.drawable.gray_fade_right));
            else
                cell.setBackground(context.getResources().getDrawable(R.drawable.gray_fade_left));

            container.addView(cell);
        }
    }

    //COMMANDS
    public void fillCell(int[] pos){
        cellUIs[pos[0]][pos[1]].fill();
    }
    public void setCellText(int[] pos, String text){
        cellUIs[pos[0]][pos[1]].setText(text);
    }
    public void toggleError(int[] pos){cellUIs[pos[0]][pos[1]].toggleCross();}
    public void checkCell(int[] pos){cellUIs[pos[0]][pos[1]].addCheck();}
    public void emptyCell(int[] pos){cellUIs[pos[0]][pos[1]].empty();}
    public void emptyAll(){
        for(int i = 0 ; i < cellUIs.length ; i++){
            for(int j = 0 ; j < cellUIs[i].length ; j++){
                cellUIs[i][j].empty();
            }
        }
    }

}
