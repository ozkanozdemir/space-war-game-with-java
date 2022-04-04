import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.*;
import java.util.ArrayList;
// import java.util.*;
// import java.awt.event.*;


public class GamePanel extends JPanel implements Runnable, KeyListener {

    //Fields
    public static int WIDTH = 400;
    public static int HEIGHT = 400;
    private Thread thread;
    private boolean running;
    private BufferedImage image;
    private Graphics2D g;
    private int FPS = 30;
    private double averageFPS;
    public static Player player;
    public static ArrayList<Bullet> bullets;
    public static ArrayList<Enemy> enemies;

    private long waveStartTimer;
    private long waveStartTimerDiff;
    private int waveNumber;
    private boolean waveStart;
    private int waveDelay;

    //Constructor
    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
    }

    //Functions
    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
        addKeyListener(this);
    }

    public void run() {
        running = true;
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        player = new Player();
        bullets = new ArrayList<Bullet>();
        enemies = new ArrayList<Enemy>();

        // Spawn enemies variables
        waveStartTimer = 0;
        waveStartTimerDiff = 0;
        waveNumber = 0;
        waveStart = false;
        waveDelay = 2000;

        long startTime;
        long URDTimeMillis;
        long waitTime;
        long totalTime = 0;
        long targetTime = 1000 / FPS;

        int frameCount = 0;
        int maxFrameCount = 30;


        //GameLoop
        while (running) {

            startTime = System.nanoTime();

            gameUpdate();
            gameRender();
            gameDraw();

            URDTimeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - URDTimeMillis;
            try {
                Thread.sleep(waitTime);
            } catch (Exception e) {
            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;
            if (frameCount == maxFrameCount) {
                averageFPS = 1000.0 / ((totalTime / frameCount) / 1000000);
                frameCount = 0;
                totalTime = 0;
            }

        }

    }

    private void gameUpdate() {
        // New wave
        if (waveStartTimer == 0 && enemies.size() == 0) {
            System.out.println(waveNumber);
            waveNumber++;
            waveStart = false;
            waveStartTimer = System.nanoTime();
        } else {
            waveStartTimerDiff = (System.nanoTime() - waveStartTimer) / 1000000;
            if (waveStartTimerDiff > waveDelay) {
                waveStart = true;
                waveStartTimer = 0;
                waveStartTimerDiff = 0;
            }
        }

        // Create enemy
        if (waveStart && enemies.size() == 0) {
            createEnemies();
        }

        // Player Update
        player.update();

        // Bullet Update
        for (int i = 0; i < bullets.size(); i++) {

            boolean remove = bullets.get(i).update();
            if (remove) {
                bullets.remove(i);
                i--;
            }

        }

        // Enemy Update
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).update();
        }

        // bullet-enemy collision
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            double bx = b.getx();
            double by = b.gety();
            double br = b.getr();

            for (int j = 0; j < enemies.size(); j++) {
                Enemy e = enemies.get(j);
                double ex = e.getx();
                double ey = e.gety();
                double er = e.getr();

                double dx = bx - ex;
                double dy = by - ey;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < br + er) {
                    e.hit();
                    bullets.remove(i);
                    i--;
                    break;
                }
            }
        }

        // Check Dead Enemies
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).isDead()) {
                Enemy e = enemies.get(i);
                player.addScore(e.getType() + e.getRank());
                enemies.remove(i);
                i--;
            }
        }

        // player-enemy collision
        if (!player.isRecovering()) {
            int px = player.getx();
            int py = player.gety();
            int pr = player.getr();

            for (int i = 0; i < enemies.size(); i++) {
                Enemy e = enemies.get(i);
                double ex = e.getx();
                double ey = e.gety();
                double er = e.getr();

                double dx = px - ex;
                double dy = py - ey;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < pr + er) {
                    player.loseLife();
                }
            }
        }
    }

    private void gameRender() {
        // draw background
        g.setColor(new Color(0, 100, 255));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw Player
        player.draw(g);

        // Draw Bullet
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).draw(g);
        }

        // Draw Enemies
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).draw(g);
        }

        // draw wave number
        if (waveStartTimer != 0) {
            g.setFont(new Font("Century Gothic", Font.PLAIN, 18));
            String s = "- W A V E    " + waveNumber + "    -";
            int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
            int alpha = (int) (255 * Math.sin(3.14 * waveStartTimerDiff / waveDelay));
            if (alpha > 255) {
                alpha = 255;
            }
            g.setColor(new Color(255, 255, 255, alpha));
            g.drawString(s, WIDTH / 2 - length / 2, HEIGHT / 2);
        }

        // draw player lives
        for (int i = 0; i < player.getLives(); i++) {
            g.setColor(Color.WHITE);
            g.fillOval(20 + (20 * i), 20, player.getr() * 2, player.getr() * 2);
            g.setStroke(new BasicStroke(3));
            g.setColor(Color.WHITE.darker());
            g.drawOval(20 + (20 * i), 20, player.getr() * 2, player.getr() * 2);
            g.setStroke(new BasicStroke(1));
        }

        // draw player score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        g.drawString("Score : " + player.getScore(), WIDTH - 100, 30);
    }

    private void gameDraw() {
        Graphics g2 = this.getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }

    private void createEnemies() {
        enemies.clear();
        Enemy e;

        for (int i = 0; i < 4 * waveNumber; i++) {
            enemies.add(new Enemy(1, 1));
        }
    }

    public void keyTyped(KeyEvent key) {
    }

    public void keyPressed(KeyEvent key) {
        int keyCode = key.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT) {
            player.setLeft(true);
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            player.setRight(true);
        }
        if (keyCode == KeyEvent.VK_UP) {
            player.setUp(true);
        }
        if (keyCode == KeyEvent.VK_DOWN) {
            player.setDown(true);
        }
        if (keyCode == KeyEvent.VK_Z) {
            player.setFiring(true);
        }
    }

    public void keyReleased(KeyEvent key) {
        int keyCode = key.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT) {
            player.setLeft(false);
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            player.setRight(false);
        }
        if (keyCode == KeyEvent.VK_UP) {
            player.setUp(false);
        }
        if (keyCode == KeyEvent.VK_DOWN) {
            player.setDown(false);
        }
        if (keyCode == KeyEvent.VK_Z) {
            player.setFiring(false);
        }
    }


}
