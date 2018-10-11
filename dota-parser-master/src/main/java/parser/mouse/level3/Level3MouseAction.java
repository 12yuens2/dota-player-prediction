package parser.mouse.level3;

import parser.mouse.MouseActivity;
import parser.mouse.atomic.MousePosition;
import parser.mouse.level1.MouseMoveSequence;
import util.Geometry;

public abstract class Level3MouseAction implements MouseActivity {

    protected int ticksToAction;
    protected double distanceToAction;
    protected MouseMoveSequence sequence;

    public Level3MouseAction(MouseMoveSequence sequence, MousePosition actionPosition)  {
        this.sequence = sequence;

        MousePosition beforeAction = sequence.getSecondLast();
        if (beforeAction != null) {
            this.ticksToAction = actionPosition.tick - beforeAction.tick;
            this.distanceToAction = Geometry.euclideanDistance(beforeAction, actionPosition);
        }
    }

    @Override
    public String toString() {
        return ticksToAction + " -- " + distanceToAction;
    }

    @Override
    public String outputStats() {
        String sequenceStats = sequence.outputStats();

        return String.format("%s%d,%f", sequenceStats, ticksToAction, distanceToAction);
    }

    @Override
    public String headers() {
        StringBuilder sb = new StringBuilder();
        sb.append("actionType,");
        sb.append(sequence.headers());
        sb.append("ticksToAction,distanceToAction");

        return sb.toString();
    }

}
