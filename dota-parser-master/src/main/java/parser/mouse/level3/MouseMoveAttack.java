package parser.mouse.level3;

import parser.mouse.MouseActivity;
import parser.mouse.atomic.MousePosition;
import parser.mouse.level1.MouseMoveSequence;
import util.Geometry;

/**
 * Mouse move followed by an attack order
 */
public class MouseMoveAttack extends Level3MouseAction {


    public MouseMoveAttack(MouseMoveSequence sequence, MousePosition attackPosition) {
        super(sequence, attackPosition);
    }


    @Override
    public String toString() {
        return "[ATTACK] " + super.toString();
    }

}
