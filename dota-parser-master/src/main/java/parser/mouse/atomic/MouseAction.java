package parser.mouse.atomic;

public class MouseAction {

    public int actionType;
    public MousePosition position;

    public MouseAction(int actionType, MousePosition position) {
        this.actionType = actionType;
        this.position = position;
    }
}
