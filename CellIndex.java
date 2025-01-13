public class CellIndex implements Index2D {
    private int x, y;

    public CellIndex(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return (char) ('A' + x) + Integer.toString(y + 1); // Converts index to "A1", "B2", etc.
    }

    @Override
    public boolean isValid() {
        return x >= 0 && y >= 0;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }
}
