import java.awt.*;

public class PowerUp {
    private double x, y;
    private int r, type;

    private Color color1;

    // 1 -- +1 life
    // 2 -- +1 power
    // 3 -- +2 power

    // Constructor
    public PowerUp(int type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;

        if (type == 1) {
            color1 = Color.PINK;
            r = 3;
        } else if (type == 2) {
            color1 = Color.YELLOW;
            r = 3;
        } else if (type == 3) {
            color1 = Color.YELLOW;
            r = 5;
        }
    }

    // Funcions
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getR() {
        return r;
    }

    public double getType() {
        return type;
    }

    public boolean update() {
        y += 2;

        if (y > GamePanel.HEIGHT + r) {
            return true;
        }

        return false;
    }

    public void draw(Graphics2D g) {
        g.setColor(color1);
        g.fillRect((int) (x - r), (int) (y - r), 2 * r, 2 * r);

        g.setStroke(new BasicStroke(3));
        g.setColor(color1.darker());
        g.drawRect((int) (x - r), (int) (y - r), 2 * r, 2 * r);
        g.setStroke(new BasicStroke(1));
    }
}
