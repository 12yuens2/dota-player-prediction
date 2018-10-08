package parser.mouse.level3;

import parser.mouse.MouseActivity;
import parser.mouse.atomic.MousePosition;
import parser.mouse.level1.MouseMoveSequence;

/**
 * Mouse move followed by cast order
 */
public class MouseMoveCast extends Level3MouseAction{

    public MouseMoveCast(MouseMoveSequence sequence, MousePosition castPosition) {
        super(sequence, castPosition);
    }

    @Override
    public String toString() {
        return "[CAST] " + super.toString();
    }

    @Override
    public String outputStats() {
        return "CAST," + super.outputStats();
    }
}
