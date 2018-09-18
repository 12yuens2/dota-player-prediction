package parser;

public class MousePosition {
	
	public int x,y,tick;
		
	public MousePosition(int x, int y, int tick) {
		this.x = x;
		this.y = y;
		this.tick = tick;
	}

    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.tick + "],";
    }
	

}
