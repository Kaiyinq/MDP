package com.example.lohkaiying.mdpgrp13;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static android.graphics.Color.rgb;
import static com.example.lohkaiying.mdpgrp13.MainActivity.MapCoorXY;
import static com.example.lohkaiying.mdpgrp13.controllerFragment.arrowArraySide;
import static com.example.lohkaiying.mdpgrp13.controllerFragment.arrowArrayX;
import static com.example.lohkaiying.mdpgrp13.controllerFragment.arrowArrayY;
import static com.example.lohkaiying.mdpgrp13.controllerFragment.exploredArray;
import static com.example.lohkaiying.mdpgrp13.controllerFragment.isMapUnexplored;
import static com.example.lohkaiying.mdpgrp13.controllerFragment.obstacleArray;
import static com.example.lohkaiying.mdpgrp13.controllerFragment.robotCenter;
import static com.example.lohkaiying.mdpgrp13.controllerFragment.robotFront;

/**
 * Created by loh kai Ying on 29-Aug-18.
 */

//create maze map
public class mapArena extends View {

    private RelativeLayout mapView;
    public static int cellSize;

    private Paint peach = new Paint();
    private Paint black = new Paint();
    private Paint rosewhite = new Paint();
    private Paint blue = new Paint();
    private Paint green = new Paint();
    private Paint red = new Paint();
    private Paint grey = new Paint();

    public mapArena(Context context) {
        super(context);

        peach.setColor(getResources().getColor(R.color.peach));
        black.setColor(getResources().getColor(R.color.black));
        rosewhite.setColor(getResources().getColor(R.color.rosewhite));
        blue.setColor(getResources().getColor(R.color.blue));
        green.setColor(getResources().getColor(R.color.green));
        red.setColor(getResources().getColor(R.color.red));
        grey.setColor(getResources().getColor(R.color.grey));
    }

    // draw the cells out
    @Override
    public void onDraw(Canvas canvas){
        //get the size for each box
        mapView = (RelativeLayout) getRootView().findViewById(R.id.map);
        int width = mapView.getMeasuredWidth();
        int height = mapView.getMeasuredHeight();
        cellSize = (width-320)/(Constants.COL+1);

        colorCell(canvas);
        drawLines(canvas);
        colorRobotCell(canvas);
    }

    // the lines for the grid
    public void drawLines (Canvas canvas) {
        black.setStrokeWidth(2);

        //draw COLUMN line
        for(int i = 0; i < Constants.COL+1; i++) {
            canvas.drawLine(cellSize*i, 0, cellSize*i, cellSize*Constants.ROW, black);
        }

        //draw ROW line
        for(int i = 0; i < Constants.ROW+1; i++) {
            canvas.drawLine(0, cellSize*i, cellSize*Constants.COL, cellSize*i, black);
        }
    }

    // ROBOT IS DRAWN HERE
    public void colorRobotCell (Canvas canvas) {
        if (robotCenter[0] >= 0) {
            float radius = 40;
            float axisX = (robotCenter[0] * cellSize) + (cellSize / 2);
            float axisY = ((Constants.ROW - robotCenter[1]) * cellSize) - (cellSize / 2);
            canvas.drawCircle(axisX, axisY, radius, peach);
        }

        if (robotFront[0] >= 0) {
            float radius = cellSize / 3;
            float axisX = (robotFront[0] * cellSize) + (cellSize / 2);
            float axisY = ((Constants.ROW - robotFront[1]) * cellSize) - (cellSize / 2);
            canvas.drawCircle(axisX, axisY, radius, rosewhite);
        }
    }

    // color selected cell
    public void colorCell (Canvas canvas) {

//        if(waypointArray != null) {
//            int x = waypointArray[0];
//            int y = waypointArray[1];
//
//            float leftX = x * cellSize;
//            float topY = y * cellSize;
//            float rightX = leftX + cellSize;
//            float btmY = topY + cellSize;
//
//            canvas.drawRoundRect(new RectF(leftX, topY, rightX, btmY), 0, 0, red);
//
//        }

        if (isMapUnexplored) {
            // MAP UNEXPLORED WHOLE GRID LIGHTGREY
            canvas.drawColor(getResources().getColor(R.color.grey));

//            for (int i = 0; i < Constants.COL; i++) {
//                for (int j = 0; j < Constants.ROW; j++) {
//                    black.setTextSize(18);
//                    canvas.drawText("0", (i * cellSize) + 10, (j * cellSize) + 20, black);
//                }
//            }
        }else {
            // START POINT IS DRAWN HERE
            for (int i = 0; i < 3; i++) {
                for (int j = 17; j < 20; j++) {
                    float leftX = i * cellSize;
                    float topY = j * cellSize;
                    float rightX = leftX + cellSize;
                    float btmY = topY + cellSize;
                    canvas.drawRoundRect(new RectF(leftX, topY, rightX, btmY), 5, 5,  blue);
                }
            }

            // END POINT IS DRAWN HERE
            for (int i = 12; i < 15; i++) {
                for (int j = 0; j < 3; j++) {
                    float leftX = (i * cellSize);
                    float topY = (j * cellSize);
                    float rightX = leftX + cellSize;
                    float btmY = topY + cellSize;
                    canvas.drawRoundRect(new RectF(leftX, topY, rightX, btmY), 5, 5,  green);
                }
            }
        }

        // EXPLORED AREA IS DRAWN HERE
        if (exploredArray != null) {
            // it starts from the top right of the maze map
            int y = -1;
            int obstaclePointer = 0;

            for (int i = 0; i < exploredArray.length; i++) {
                int x = i % Constants.COL;
                if (x == 0) {
                    y++;
                }

                float leftX = (x * cellSize);
                float topY = (Constants.ROW - 1 - y) * cellSize;
                float rightX = (x + 1) * cellSize;
                float btmY = (Constants.ROW - y) * cellSize;

                if (exploredArray[i] == 1) {
                    String coordinates = x + "," + y;
                    switch (coordinates) {
                        // START POINT FOR EXPLORATION IS DRAWN HERE
                        case "0,0":
                        case "0,1":
                        case "0,2":
                        case "1,0":
                        case "1,1":
                        case "1,2":
                        case "2,0":
                        case "2,1":
                        case "2,2":
                            canvas.drawRoundRect(new RectF(leftX, topY, rightX, btmY), 5, 5, blue);
                            break;
                        // END POINT FOR EXPLORATION IS DRAWN HERE
                        case "12,17":
                        case "12,18":
                        case "12,19":
                        case "13,17":
                        case "13,18":
                        case "13,19":
                        case "14,17":
                        case "14,18":
                        case "14,19":
                            canvas.drawRoundRect(new RectF(leftX, topY, rightX, btmY), 5, 5, green);
                            break;
                    }

                    if (obstacleArray != null && obstacleArray[obstaclePointer] == 1) {
                        canvas.drawRoundRect(new RectF(leftX, topY, rightX, btmY), 0, 0, black);
                    }
                    obstaclePointer++;

                }else {
                    canvas.drawRoundRect(new RectF(leftX, topY, rightX, btmY), 0, 0, grey);
                }
            }

            // DRAW OBS WITH ARROW
            // https://www.flaticon.com/packs/arrows-kit
            int arrowPointer = 0;
            if (arrowArrayX != null && arrowArrayY != null) {
                while (arrowPointer != arrowArrayX.length) {
                    float leftArrX = arrowArrayX[arrowPointer] * cellSize;
                    float topArrY = (Constants.ROW - arrowArrayY[arrowPointer] - 1) * cellSize;
                    char direction = arrowArraySide[arrowPointer];
                    //System.out.println("ArrX: " + arrowArrayX[arrowPointer] + " ArrY: " + arrowArrayY[arrowPointer]);

                    // put arrow on the block
                    Drawable myDrawable =  getResources().getDrawable(R.drawable.obs_arrow);
                    Bitmap arrowBM = ((BitmapDrawable) myDrawable).getBitmap();
                    canvas.drawBitmap(arrowBM, leftArrX + 5, topArrY + 5, rosewhite);

                    // point where is the block
                    if (direction == 'U') {
                        topArrY = (Constants.ROW - arrowArrayY[arrowPointer] - 2) * cellSize;
                        Drawable myDrawable2 = getResources().getDrawable(R.drawable.obs_arrow_down);
                        Bitmap arrowBMPointer = ((BitmapDrawable) myDrawable2).getBitmap();
                        canvas.drawBitmap(arrowBMPointer, leftArrX + 5, topArrY + 5, red);
                    } else if (direction == 'D') {
                        topArrY = (Constants.ROW - arrowArrayY[arrowPointer]) * cellSize;
                        Drawable myDrawable2 = getResources().getDrawable(R.drawable.obs_arrow_up);
                        Bitmap arrowBMPointer = ((BitmapDrawable) myDrawable2).getBitmap();
                        canvas.drawBitmap(arrowBMPointer, leftArrX + 5, topArrY + 5, red);
                    } else if (direction == 'L') {
                        leftArrX = (arrowArrayX[arrowPointer] - 1) * cellSize;
                        Drawable myDrawable2 = getResources().getDrawable(R.drawable.obs_arrow_right);
                        Bitmap arrowBMPointer = ((BitmapDrawable) myDrawable2).getBitmap();
                        canvas.drawBitmap(arrowBMPointer, leftArrX + 5, topArrY + 5, red);
                    } else if (direction == 'R') {
                        leftArrX = (arrowArrayX[arrowPointer] + 1) * cellSize;
                        Drawable myDrawable2 = getResources().getDrawable(R.drawable.obs_arrow_left);
                        Bitmap arrowBMPointer = ((BitmapDrawable) myDrawable2).getBitmap();
                        canvas.drawBitmap(arrowBMPointer, leftArrX + 5, topArrY + 5, red);
                    }

                    arrowPointer++;
                }
            }

        }



        for (int i = 0; i < Constants.COL; i++) {
            for (int j = 0; j < Constants.ROW; j++) {

                float leftX = (i * cellSize);
                float topY = (j * cellSize);
                float rightX = leftX + cellSize;
                float btmY = topY + cellSize;
                //System.out.println("left: " + leftX + "top: " + topY);

                // color selected waypoint cell
                if (MainActivity.MapCoorXY[i][j].isWaypoint() == true) {
                    //Color.setColor(getResources().getColor(R.color.red));
                    canvas.drawRoundRect(new RectF(leftX, topY, rightX, btmY), 0, 0, red);
                }

                // color selected cell
                if (MainActivity.MapCoorXY[i][j].isColor() == true) {
                    //new rect with the points
                    canvas.drawRoundRect(new RectF(leftX, topY, rightX, btmY), 5, 5,  rosewhite);
                    MainActivity.MapCoorXY[i][j].setColor(false);
                }

            }
        }

    }

}
