import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

public class Main implements Sheet {
    private Cell[][] table;

    // Constructor to initialize the spreadsheet with specified dimensions.
    public Main(int x, int y) {
        table = new Cell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL); // Initialize with empty cells
            }
        }
    }

    @Override
    public String value(int x, int y) {
        return table[x][y].getData(); // Return the current value of a cell
    }

    @Override
    public void set(int x, int y, String c) {
        table[x][y] = new SCell(c); // Set the cell value at the specified coordinates
    }

    @Override
    public void eval() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell cell = table[i][j];
                if (cell.getType() == Ex2Utils.FORM) {
                    String formula = cell.getData();
                    String computedValue = evaluateFormula(formula, i, j);
                    cell.setData(computedValue);
                }
            }
        }
    }

    private String evaluateFormula(String formula, int x, int y) {
        if (formula.startsWith("=")) {
            try {
                String expression = formula.substring(1).toUpperCase().replaceAll("\\s+", ""); // Remove all spaces
                for (int i = 0; i < width(); i++) {
                    for (int j = 0; j < height(); j++) {
                        String cellName = (char) ('A' + i) + Integer.toString(j + 1);
                        if (expression.contains(cellName)) {
                            String cellData = table[i][j].getData();
                            // Recursively evaluate the cell if it contains a formula
                            if (cellData.startsWith("=")) {
                                cellData = evaluateFormula(cellData, i, j);
                            }
                            expression = expression.replace(cellName, cellData);
                        }
                    }
                }
                double result = evaluateMath(expression);

                // Handle division by zero
                if (Double.isInfinite(result)) {
                    return "Infinity"; // Explicitly return "Infinity"
                }
                return Double.toString(result);
            } catch (Exception e) {
                return Ex2Utils.ERR_FORM; // Return error if evaluation fails
            }
        }
        return formula; // If it's not a formula, return the formula itself
    }

    private double evaluateMath(String expression) {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();
        int n = expression.length();

        for (int i = 0; i < n; i++) {
            char c = expression.charAt(i);

            // Handle numbers (including decimals)
            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < n && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i));
                    i++;
                }
                i--; // Step back after reading the number
                values.push(Double.parseDouble(sb.toString()));
            }
            // Handle opening parenthesis
            else if (c == '(') {
                operators.push(c);
            }
            // Handle closing parenthesis
            else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop(); // Pop '('
            }
            // Handle operators (+, -, *, /)
            else if (c == '+' || c == '-' || c == '*' || c == '/') {
                // Handle unary minus (e.g., -5)
                if (c == '-' && (i == 0 || expression.charAt(i - 1) == '(')) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(c);
                    i++;
                    while (i < n && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                        sb.append(expression.charAt(i));
                        i++;
                    }
                    i--; // Step back after reading the number
                    values.push(Double.parseDouble(sb.toString()));
                } else {
                    // Standard operator handling (binary operators)
                    while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
                        values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
                    }
                    operators.push(c);
                }
            }
        }

        // Apply remaining operators
        while (!operators.isEmpty()) {
            values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private double applyOperation(char op, double b, double a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    return Double.POSITIVE_INFINITY; // Correctly handle division by zero
                }
                return a / b; // Correct operand order
            default:
                throw new IllegalArgumentException("Invalid operator: " + op);
        }
    }


    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    @Override
    public int[][] depth() {
        int[][] depths = new int[width()][height()];
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell cell = table[i][j];
                if (cell.getType() == Ex2Utils.NUMBER || cell.getType() == Ex2Utils.TEXT) {
                    depths[i][j] = 0;
                } else if (cell.getType() == Ex2Utils.FORM) {
                    depths[i][j] = 1; // Example depth for formulas
                }
            }
        }
        return depths;
    }

    @Override
    public boolean isIn(int x, int y) {
        return x >= 0 && y >= 0 && x < width() && y < height();
    }

    @Override
    public int width() {
        return table.length;
    }

    @Override
    public int height() {
        return table[0].length;
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String entry) {
        int x = xCell(entry);
        int y = yCell(entry);
        if (isIn(x, y)) {
            return table[x][y];
        }
        return null;
    }

    private int xCell(String c) {
        return c.charAt(0) - 'A';
    }

    private int yCell(String c) {
        return Integer.parseInt(c.substring(1)) - 1;
    }

    @Override
    public void load(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                String data = parts[2];
                set(x, y, data);
            }
        }
        reader.close();
    }

    @Override
    public void save(String fileName) throws IOException {
        // Implement save logic if needed
    }

    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell != null && cell.getType() == Ex2Utils.FORM) {
            String formula = cell.getData();
            return evaluateFormula(formula, x, y);
        }
        return cell != null ? cell.getData() : Ex2Utils.EMPTY_CELL;
    }

    public void handleMouseClick(double mx, double my) {
        int x = (int) ((mx - Ex2Utils.GUI_X_START) / Ex2Utils.GUI_X_SPACE);
        int y = height() - (int) (my - Ex2Utils.GUI_Y_TEXT_START) - 1;

        if (isIn(x, y)) {
            String cellName = (char) ('A' + x) + Integer.toString(y + 1);
            System.out.println("Clicked on: " + cellName);
        }
    }
}