package util;

import parser.mouse.atomic.MousePosition;

public class Geometry {


    public static double euclideanDistance(MousePosition m1, MousePosition m2) {
        return Math.sqrt(Math.pow(m2.y - m1.y, 2.0) + Math.pow(m2.x - m1.x, 2.0));
    }
}
