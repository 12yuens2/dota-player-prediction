package parser.mouse.level3;

import parser.mouse.MouseActivity;
import parser.mouse.atomic.MousePosition;
import parser.mouse.level1.MouseMoveSequence;
import util.Geometry;

/**
 * Mouse move followed by an attack order
 */
public class MouseMoveAttack implements MouseActivity {

    private int ticksToAttack;
    private double distanceToAttack;

    private MouseMoveSequence sequenceToAttack;

    public MouseMoveAttack(MouseMoveSequence sequence, MousePosition attackPosition) {
        this.sequenceToAttack = sequence;

        MousePosition beforeAttack = sequence.getSecondLast();
        if (beforeAttack != null) {
            this.ticksToAttack = attackPosition.tick - beforeAttack.tick;
            this.distanceToAttack = Geometry.euclideanDistance(beforeAttack, attackPosition);
        }
    }

    @Override
    public String toString() {
        return "[ATTACK " + ticksToAttack + " -- " + distanceToAttack + "]";
    }

}
