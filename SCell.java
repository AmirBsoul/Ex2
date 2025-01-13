public class SCell implements Cell {
    private String line;
    private int type;
    private int order; // Variable to store the order of computation for this cell.

    public SCell(String s) {
        setData(s);
    }

    @Override
    public int getOrder() {
        return order; // Return the current order of computation
    }

    @Override
    public String toString() {
        return getData();
    }

    @Override
    public void setData(String s) {
        line = s;
        // You can also update type based on the data if needed.
        // Example: Check if it's a formula or a number and update the type accordingly.
        if (line.startsWith("=")) {
            setType(Ex2Utils.FORM); // Formula
        } else {
            setType(Ex2Utils.TEXT); // Text (or implement other logic for number type)
        }
    }

    @Override
    public String getData() {
        return line;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public void setOrder(int t) {
        this.order = t; // Set the computation order for this cell
    }
}
