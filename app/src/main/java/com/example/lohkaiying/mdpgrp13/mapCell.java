package com.example.lohkaiying.mdpgrp13;

/**
 * Created by loh kai Ying on 05-Sep-18.
 */

public class mapCell {

    private boolean color;
    private boolean waypoint;
    private boolean obstacles;

    public boolean isColor() {
        return color;
    }

    public void setColor(boolean color) {
        this.color = color;
    }

    public boolean isWaypoint() {
        return waypoint;
    }

    public void setWaypoint(boolean waypoint) {
        this.waypoint = waypoint;
    }

    public boolean isObstacles() {
        return obstacles;
    }

    public void setObstacles(boolean obstacles) {
        this.obstacles = obstacles;
    }
}
