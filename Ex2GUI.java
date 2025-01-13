import java.awt.*;
import java.io.IOException;

/**
 * ArielU. Intro2CS, Ex2: https://docs.google.com/document/d/1-18T-dj00apE4k1qmpXGOaqttxLn-Kwi/edit?usp=sharing&ouid=113711744349547563645&rtpof=true&sd=true
 * This is the main GUI class that interacts with the Ex2Sheet (spreadsheet) and handles the UI.
 */
public class Ex2GUI {

    private static Sheet table; // This is the main data (an implementation of the Sheet interface).
    private static Index2D cord = null; // A table entry used by the GUI of setting up a cell value / form.

    public Ex2GUI() {;} // An empty (redundant) constructor.

    /** The main function for running Ex2 */
    public static void main(String[] a) {
        table = new Main(Ex2Utils.WIDTH, Ex2Utils.HEIGHT); // Create the spreadsheet
        testSimpleGUI(table);
    }

    /**
     * This function runs the main (endless) loop of the GUI.
     * @param table The spreadsheet to be displayed in the GUI.
     */
    public static void testSimpleGUI(Sheet table) {
        // Initialize parameters
        StdDrawEx2.setCanvasSize(Ex2Utils.WINDOW_WIDTH, Ex2Utils.WINDOW_HEIGHT);
        StdDrawEx2.setScale(0, Ex2Utils.MAX_X);
        StdDrawEx2.setPenRadius(Ex2Utils.PEN_RADIUS);
        StdDrawEx2.enableDoubleBuffering();
        table.eval();

        // Endless loop (GUI)
        while (true) {
            StdDrawEx2.clear(); // Clear the GUI (Ex2 window).
            drawFrame(); // Draw the lines.
            drawCells(); // Draw the cells.
            StdDrawEx2.show(); // Present the window.
            int xx = StdDrawEx2.getXX(); // Get the x coordinate of the mouse click (-1 if none)
            int yy = StdDrawEx2.getYY(); // Get the y coordinate of the mouse click (-1 if none)
            showCellLabel(xx, yy); // Display the cell label (e.g., A1, B5) on click.
            inputCell(xx, yy); // If isIn(xx, yy), an input window will be opened to allow the user to edit the cell (xx, yy).
            StdDrawEx2.pause(Ex2Utils.WAIT_TIME_MS); // Waits a few milliseconds (say 30 fps is sufficient).
        }
    }

    /**
     * Saves the current spreadsheet to a file.
     * @param fileName The name of the file to save the spreadsheet to.
     */
    public static void save(String fileName) {
        try {
            table.save(fileName); // Call save method from the Ex2Sheet class
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a spreadsheet from a file.
     * @param fileName The name of the file to load the spreadsheet from.
     */
    public static void load(String fileName) {
        try {
            table.load(fileName); // Call load method from the Ex2Sheet class
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Draws the lines of the spreadsheet.
     */
    private static void drawFrame() {
        StdDrawEx2.setPenColor(StdDrawEx2.BLACK);
        int max_y = table.height();
        double x_space = Ex2Utils.GUI_X_SPACE, x_start = Ex2Utils.GUI_X_START;
        double y_height = Ex2Utils.GUI_Y_TEXT_START;
        for (int y = 0; y < max_y; y = y + 1) {
            double xs = y * x_space;
            double xc = x_start + y * x_space;
            StdDrawEx2.line(0, y + 1, Ex2Utils.MAX_X, y + 1);
            StdDrawEx2.line(xs, 0, xs, max_y);
            int yy = max_y - (y + 1);
            StdDrawEx2.text(1, y + y_height, "" + (yy));
            StdDrawEx2.text(xc, max_y + y_height, "" + Ex2Utils.ABC[y]);
        }
    }

    /**
     * Draws the content of each cell (non-empty).
     */
    private static void drawCells() {
        StdDrawEx2.setPenColor(StdDrawEx2.BLACK);
        int max_y = table.height();
        int maxx = table.width();
        double x_space = Ex2Utils.GUI_X_SPACE, x_start = Ex2Utils.GUI_X_START;
        double y_height = Ex2Utils.GUI_Y_TEXT_START;
        for (int x = 0; x < maxx; x = x + 1) {
            double xc = x_start + x * x_space;
            for (int y = 0; y < max_y; y = y + 1) {
                String w = table.value(x, y); // Get the value of the cell
                Cell cc = table.get(x, y); // Get the cell
                int t = cc.getType(); // Get the cell type
                StdDrawEx2.setPenColor(getColorFromType(t)); // Set color based on cell type
                int max = Math.min(Ex2Utils.MAX_CHARS, w.length());
                w = w.substring(0, max); // Limit the text length
                double yc = max_y - (y + 1 - y_height);
                StdDrawEx2.text(xc, yc, w); // Draw the text of the cell
            }
        }
    }

    /**
     * Displays the label of the clicked cell, such as "A1" or "B5".
     * @param xx The x-coordinate of the click.
     * @param yy The y-coordinate of the click.
     */
    private static void showCellLabel(int xx, int yy) {
        if (table.isIn(xx, yy)) {
            int row = yy + 1; // Adjusting for 1-based indexing.
            String column = Ex2Utils.ABC[xx]; // Retrieve the column letter.
            String cellLabel = column + String.valueOf(row);
            StdDrawEx2.setPenColor(Color.BLACK);
            StdDrawEx2.text(Ex2Utils.GUI_X_START, Ex2Utils.MAX_X - 1, "Clicked Cell: " + cellLabel);
            StdDrawEx2.show();
        }
    }

    /**
     * Returns the color associated with a given cell type.
     * @param t The cell type.
     * @return The color associated with the cell type.
     */
    private static Color getColorFromType(int t) {
        Color ans = Color.GRAY; // Default color for unknown types.
        if (t == Ex2Utils.NUMBER) {
            ans = Color.BLACK; // Numbers are black.
        } else if (t == Ex2Utils.FORM) {
            ans = Color.BLUE; // Formulas are blue.
        } else if (t == Ex2Utils.ERR_FORM_FORMAT) {
            ans = Color.RED; // Errors in formula format are red.
        } else if (t == Ex2Utils.ERR_CYCLE_FORM) {
            ans = Color.RED; // Cyclic formula errors are red.
        }
        return ans;
    }

    /** Input a content into a cell(xx, yy) if it is within this spreadsheet. */
    private static void inputCell(int xx, int yy) {
        if (table.isIn(xx, yy)) {
            Cell cc = table.get(xx, yy);
            String ww = cord + ": " + cc.toString() + " : ";
            StdDrawEx2.text(Ex2Utils.GUI_X_START, Ex2Utils.MAX_X - 1, ww);
            StdDrawEx2.show();
            String c = StdDrawEx2.getCell(cord, cc.getData());
            String s1 = table.get(xx, yy).getData();
            if (c == null) {
                table.set(xx, yy, s1);
            } else {
                table.set(xx, yy, c);
                int[][] calc_d = table.depth();
                if (calc_d[xx][yy] == Ex2Utils.ERR) {
                    table.get(xx, yy).setType(Ex2Utils.ERR_CYCLE_FORM);
                } else if (c.contains("/")) {
                    String[] parts = c.split("/");
                    try {
                        double numerator = Double.parseDouble(parts[0].trim());
                        double denominator = Double.parseDouble(parts[1].trim());
                        if (denominator == 0) {
                            table.get(xx, yy).setData("Infinity");
                        }
                    } catch (NumberFormatException e) {
                        table.get(xx, yy).setType(Ex2Utils.ERR_FORM_FORMAT);
                    }
                }
            }
            table.eval();
            StdDrawEx2.resetXY();
        }
    }
}
