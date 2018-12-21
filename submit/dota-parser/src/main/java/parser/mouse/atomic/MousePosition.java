package parser.mouse.atomic;

public class MousePosition {
	
	public int x,y,tick;
		
	public MousePosition(int x, int y, int tick) {
		this.x = x;
		this.y = y;
		this.tick = tick;
	}

	@Override
	public boolean equals(Object other) {
	    MousePosition mp = (MousePosition) other;
	    return (mp.x == x && mp.y == y);
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.tick + "],";
    }


}
