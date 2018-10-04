package parser.mouse.level1;
import parser.mouse.MouseActivity;
import parser.mouse.atomic.MousePosition;

import java.util.ArrayList;
import java.lang.Math;
import java.text.DecimalFormat;

public class MouseMoveSequence implements MouseActivity {

	public static final double MEMBER_THRESHOLD = 10.0;
	public static final double MEMBER_ANGLE_THRESHOLD = 20.0;

	private ArrayList<MousePosition> positions;
	private int tickOffset = 0;
	private DecimalFormat df = new DecimalFormat("#0.0000");

	public MouseMoveSequence(MousePosition firstPosition) {
		this.positions = new ArrayList<>();
		this.tickOffset = firstPosition.tick;

		firstPosition.tick = 0;
		positions.add(firstPosition);
	}
	
	public boolean add(MousePosition m) {
		m.tick = m.tick - tickOffset;
		// not small enough to have a direction yet, just add it and leave
		if (positions.size() < 2) {
			return positions.add(m);
		}

		// we have enough positions we need to check if the direction works
		MousePosition lastLastPosition = positions.get(positions.size() - 2);
		MousePosition lastPosition = positions.get(positions.size() - 1);

		double lastDirection = direction(lastLastPosition.x, lastLastPosition.y, lastPosition.x, lastPosition.y);
		double newDirection = direction(lastPosition.x, lastPosition.y, m.x, m.y);

		if (directionDifference(lastDirection, newDirection) > MEMBER_ANGLE_THRESHOLD) {
			return positions.add(m);
		}
		else {
			return false;
		}
	}

	public int size() {
		return positions.size();
	}
	
	private double euclidDist(int x1, int y1, int x2, int y2) {
		return Math.sqrt(Math.pow((y2 - y1), 2.0) + Math.pow((x2 - x1), 2.0));
	}
	
	private double direction(int x1, int y1, int x2, int y2) {
		return Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
	}
    
	private double direction(MousePosition a, MousePosition b) {
		return direction(a.x, a.y, b.x, b.y);
	}

	private double directionDifference(double a1, double a2) {
		return Math.min(Math.abs(a1 - a2), Math.abs(a2 - a1));
	}
	
	private double angleOfCurvature(int x1, int y1, int x2, int y2, int x3, int y3) {
		//Law of Cosines
		//Angle at B - x2,y2
		double a = euclidDist(x2, y2, x3, y3);
		double b = euclidDist(x1, y1, x3, y3);
		double c = euclidDist(x1, y1, x2, y2);
		double result = Math.toDegrees(Math.acos((c*c + a*a - b*b)/(2 * c * a)));
		if (Double.isNaN(result) || Double.isInfinite(result)) {
			result = 0.0;
		}
		return result;
	}
    
	private double angleOfCurvature(MousePosition a, MousePosition b, MousePosition c) {
		return angleOfCurvature(a.x, a.y, b.x, b.y, c.x, c.y);
	}
	
	private double curvatureRatio(int x1, int y1, int x2, int y2, int x3, int y3) {
		
        double a = euclidDist(x2, y2, x3, y3);
		double b = euclidDist(x1, y1, x3, y3);
		double c = euclidDist(x1, y1, x2, y2);
		//Law of Cosines
		//Angle at A - x1,y1
        double A = Math.acos((b*b + c*c - a*a)/(2 * b * c));
		double h = c * Math.sin(A);
		double result = b / h;
		if (Double.isNaN(result) || Double.isInfinite(result)) {
			result = 0.0;
		}
		return result;
	}

	private double curvatureRatio(MousePosition a, MousePosition b, MousePosition c) {
		return curvatureRatio(a.x, a.y, b.x, b.y, c.x, c.y);
	}
    
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (MousePosition m: positions) {
			sb.append(m);
		}
		sb.setLength(sb.length() - 1); // removes trailing comma on last position
		sb.append("]");
		return sb.toString();
	}
    
    public String getAngles() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		if (!positions.isEmpty()) {
            MousePosition start = positions.get(0);
            MousePosition end = positions.get(positions.size() - 1);
            MousePosition middle = positions.get(positions.size() / 2);
            sb.append(df.format(direction(start,end)) + ", ");
            sb.append(df.format(angleOfCurvature(start, middle, end)) + ", ");
            sb.append(df.format(curvatureRatio(start, middle, end)));
        }
            
		sb.setLength(sb.length() - 1); // removes trailing comma on last position
		sb.append("]");
		return sb.toString();
	}
    
}
