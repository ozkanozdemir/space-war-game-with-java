import java.awt.*;


public class Enemy {

    //Fields

    private double x, y;
    private int r, health, type, rank;

    private double dx, dy, rad, speed;

    private Color color1;
    private boolean ready, dead;

    private boolean hit;
    private long hitTimer;

    //Constructor

    public Enemy(int type, int rank) {
        this.type = type;
        this.rank = rank;

        // default enemy
        if (type == 1) {
            color1 = Color.BLUE;
            if (rank == 1) {
                speed = 2;
                r = 5;
                health = 1;
            } else if (rank == 2) {
                speed = 2;
                r = 10;
                health = 2;
            } else if (rank == 3) {
                speed = 1.5;
                r = 20;
                health = 3;
            } else if (rank == 4) {
                speed = 1.5;
                r = 30;
                health = 4;
            }
        } else if (type == 2) { // stronger, faster default
            color1 = Color.RED;

            if (rank == 1) {
                speed = 3;
                r = 5;
                health = 2;
            }
        } else if (type == 3) { // slow, but hard to kill
            color1 = Color.GREEN;

            if (rank == 1) {
                speed = 1.5;
                r = 5;
                health = 5;
            }
        }

        x = Math.random() * GamePanel.WIDTH / 2 + GamePanel.WIDTH / 4;
        y = -r;

        double angle = Math.random() * 140 + 20;
        rad = Math.toRadians(angle);
        dx = Math.cos(rad) * speed;
        dy = Math.cos(rad) * speed;

        ready = false;
        dead = false;

        hit = false;
        hitTimer = 0;
    }

    // Functions
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getR() {
        return r;
    }

    public int getType() {
        return type;
    }

    public int getRank() {
        return rank;
    }

    public boolean isDead() {
        return dead;
    }

    public void hit() {
        health--;
        if (health < 0) {
            dead = true;
        }
        hit = true;
        hitTimer = System.nanoTime();
    }

    public void explode() {
        if (rank > 1) {
            int amount = 0;

            if (type == 1) {
                amount = 3;
            }

            for (int i = 0; i < amount; i++) {
                Enemy e = new Enemy(getType(), getRank() - 1);
                e.x = this.x;
                e.y = this.y;
                double angle = 0;
                if (!ready) {
                    angle = Math.random() * 140 + 20;
                } else {
                    angle = Math.random() * 360;
                }
                e.rad = Math.toRadians(angle);

                GamePanel.enemies.add(e);
            }
        }
    }

    public void update() {
        x += dx;
        y += dy;
        if (!ready) {
            if (x > r && x < GamePanel.WIDTH - r && y > r && y < GamePanel.HEIGHT - r) {
                ready = true;
            }
        }
        if (x < r && dx < 0) {
            dx = -dx;
        }
        if (y < r && dy < 0) {
            dy = -dy;
        }
        if (x > GamePanel.WIDTH - r && dx > 0) {
            dx = -dx;
        }
        if (y > GamePanel.HEIGHT - r && dy > 0) {
            dy = -dy;
        }

        if (hit) {
            long elapsed = (System.nanoTime() - hitTimer) / 1000000;

            if (elapsed > 50) {
                hit = false;
                hitTimer = 0;
            }
        }
    }

    public void draw(Graphics2D g) {
       if (hit) {
           g.setColor(Color.WHITE);
           g.fillOval((int) (x - r), (int) (y - r), 2 * r, 2 * r);

           g.setStroke(new BasicStroke(3));
           g.setColor(Color.WHITE.darker());
           g.drawOval((int) (x - r), (int) (y - r), 2 * r, 2 * r);
           g.setStroke(new BasicStroke(1));
       } else {
           g.setColor(color1);
           g.fillOval((int) (x - r), (int) (y - r), 2 * r, 2 * r);

           g.setStroke(new BasicStroke(3));
           g.setColor(color1.darker());
           g.drawOval((int) (x - r), (int) (y - r), 2 * r, 2 * r);
           g.setStroke(new BasicStroke(1));
       }
    }
}
