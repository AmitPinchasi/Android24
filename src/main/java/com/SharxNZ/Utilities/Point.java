package com.SharxNZ.Utilities;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Point {
    public int x;
    public int y;

    static ArrayList<Point> points = new ArrayList<>();

    public Point() {
    }

    public static void print() {
        for (Point point : points) {
            System.out.println("X: " + point.x + "  ||  Y: " + point.y);
        }
    }

    public Point(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
        points.add(this);
    }

    public void set(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public static void setOffset(double offset, Point @NotNull ... points) {
        for (Point point : points) {
            point.x = (int) (point.x + offset);
            point.y = (int) (point.y + offset);
        }
    }
}
