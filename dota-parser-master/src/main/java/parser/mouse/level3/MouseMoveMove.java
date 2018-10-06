package parser.mouse.level3;

import parser.mouse.MouseActivity;
import parser.mouse.atomic.MousePosition;
import parser.mouse.level1.MouseMoveSequence;

/**
 * Mouse move followed by move order
 */
public class MouseMoveMove extends Level3MouseAction {

    public MouseMoveMove(MouseMoveSequence sequence, MousePosition movePosition) {
        super(sequence, movePosition);
    }

    @Override
    public String toString() {
        return "[MOVE] " + super.toString();
    }
}
