package parser.mouse.level1;
import parser.mouse.MouseActivity;
import parser.mouse.atomic.MousePosition;
import util.Geometry;
import util.VectorFeatures;

import java.util.ArrayList;
import java.lang.Math;
import java.text.DecimalFormat;
import java.util.HashMap;

public class MouseMoveSequence implements MouseActivity {

	public static final double MEMBER_THRESHOLD = 10.0;
	public static final double MEMBER_ANGLE_THRESHOLD = 20.0;

	public ArrayList<MousePosition> positions;

	public ArrayList<Double> angles, curvatures, deltaCurvatures, horizontalVelocity, verticalVelocity, velocity, acceleration, jerk, angularVelocity;

	private int tickOffset = 0;
	private DecimalFormat df = new DecimalFormat("#0.0000");

	public MouseMoveSequence(MousePosition firstPosition) {
		this.positions = new ArrayList<>();
		this.angles = new ArrayList<>();
		this.curvatures = new ArrayList<>();
		this.deltaCurvatures = new ArrayList<>();
		this.horizontalVelocity = new ArrayList<>();
		this.verticalVelocity = new ArrayList<>();
		this.velocity = new ArrayList<>();
		this.acceleration = new ArrayList<>();
		this.jerk = new ArrayList<>();
		this.angularVelocity = new ArrayList<>();

		this.tickOffset = firstPosition.tick;

		firstPosition.tick = 0;
		positions.add(firstPosition);
	}
	
	public void add(MousePosition newPosition) {
//		m.tick = m.tick - tickOffset;
	    MousePosition previousPosition = positions.get(positions.size() - 1);

        double deltaDistance = Geometry.euclideanDistance(newPosition, previousPosition);
	    int deltaTime = newPosition.tick - previousPosition.tick;

        if (canCalculateDelta(positions)) {
             double Vx = (newPosition.x - previousPosition.x) / deltaTime;
             double Vy = (newPosition.y - previousPosition.y) / deltaTime;

             double v = Math.sqrt(Math.pow(Vx, 2.0) + Math.pow(Vy, 2.0));

             horizontalVelocity.add(Vx);
             verticalVelocity.add(Vy);
             velocity.add(v);
        }

        positions.add(newPosition);
	    angles.add(calculateAngle(newPosition, previousPosition));

	    calculateDeltas(angles, curvatures, deltaDistance);
	    calculateDeltas(curvatures, deltaCurvatures, deltaDistance);
        calculateDeltas(velocity, acceleration, deltaTime);
        calculateDeltas(acceleration, jerk, deltaTime);
        calculateDeltas(angles, angularVelocity, deltaTime);


		// not small enough to have a direction yet, just add it and leave
//		if (positions.size() < 2) {
//			return positions.add(m);
//		}
//
//		// we have enough positions we need to check if the direction works
//		MousePosition lastLastPosition = positions.get(positions.size() - 2);
//		MousePosition lastPosition = positions.get(positions.size() - 1);
//
//		double lastDirection = direction(lastLastPosition.x, lastLastPosition.y, lastPosition.x, lastPosition.y);
//		double newDirection = direction(lastPosition.x, lastPosition.y, m.x, m.y);
//
//		if (directionDifference(lastDirection, newDirection) > MEMBER_ANGLE_THRESHOLD) {
//			return positions.add(m);
//		}
//		else {
//			return false;
//		}
	}

	public void printStats() {
        System.out.println("angles: " + angles);
        System.out.println("curvatures: " + curvatures);
        System.out.println("delta curves: " + deltaCurvatures);
        System.out.println("hvel: " + horizontalVelocity);
        System.out.println("vvel: " + verticalVelocity);
        System.out.println("velocity: " + velocity);
        System.out.println("acceleration: " + acceleration);
        System.out.println("jerk: " + jerk);
        System.out.println("angular velocity: " + angularVelocity);
    }

    public ArrayList<VectorFeatures> getStats() {
	    ArrayList<VectorFeatures> stats = new ArrayList<>();
	    stats.add(new VectorFeatures("Angles", angles));
	    stats.add(new VectorFeatures("Curves", curvatures));
	    stats.add(new VectorFeatures("deltaCurves", deltaCurvatures));
	    stats.add(new VectorFeatures("hVelocity", horizontalVelocity));
	    stats.add(new VectorFeatures("vVeclocity", verticalVelocity));
	    stats.add(new VectorFeatures("Velocity", velocity));
	    stats.add(new VectorFeatures("Acceleration", acceleration));
	    stats.add(new VectorFeatures("Jerk", jerk));
	    stats.add(new VectorFeatures("Angular velocity", angularVelocity));

	    return stats;
    }

    @Override
    public String outputStats() {
	    ArrayList<VectorFeatures> stats = getStats();

        StringBuilder sb = new StringBuilder();
        for (VectorFeatures features : stats) {
            sb.append(features.getStats() + ",");
        }

        return sb.toString();
    }

    @Override
    public String headers() {
	    ArrayList<VectorFeatures> stats = getStats();

	    StringBuilder sb = new StringBuilder();
	    for (VectorFeatures features : stats) {
	        sb.append(features.getHeaders() + ",");
        }

        return sb.toString();
    }

	private static boolean canCalculateDelta(ArrayList<?> list) {
	    return list.size() >= 2;
    }

	private double calculateAngle(MousePosition newPosition, MousePosition previousPosition) {
	    return Math.atan2(newPosition.y - previousPosition.y, newPosition.x - previousPosition.x);
    }

    private double calculateCurvature(MousePosition newPosition, MousePosition previousPosition, double deltaDistance) {
        double angleBefore = angles.get(angles.size() - 2);
        double angleNow = angles.get(angles.size() - 1);

        double deltaAngle = angleNow - angleBefore;

        return deltaAngle / deltaDistance;
    }


    private void calculateDeltas(ArrayList<Double> xs, ArrayList<Double> deltas, double quotient) {
	    if (canCalculateDelta(xs)) {
	        double previous = xs.get(xs.size() - 2);
	        double current = xs.get(xs.size() - 1);

	        deltas.add((current - previous) / quotient);
        }
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

	public int getIndexAtTick(int tick) {
        return positions.indexOf(getAtTick(tick));
    }

    public MousePosition get(int index) {
	    return positions.get(index);
    }

	public MousePosition getAtTick(int tick) {
	    for (MousePosition mp : positions) {
	        if (mp.tick == tick) {
	            return mp;
            }
        }
        return null;
    }

    public MousePosition getSecondLast() {
	    int index = positions.size() - 2;
	    if (index < 0) {
	        return null;
        }
	    return positions.get(index);
    }

    public MousePosition getLast() {
	    return positions.get(positions.size() - 1);
    }
}
