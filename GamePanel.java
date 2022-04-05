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
    public static int WIDTH = 600;
    public static int HEIGHT = 600;
    private Thread thread;
    private boolean running;
    private BufferedImage image;
    private Graphics2D g;
    private int FPS = 30;
    private double averageFPS;
    public static Player player;
    public static ArrayList<Bullet> bullets;
    public static ArrayList<Enemy> enemies;
    public static ArrayList<PowerUp> powerUps;
    public static ArrayList<Explosion> explosions;
    public static ArrayList<Text> texts;

    private long waveStartTimer;
    private long waveStartTimerDiff;
    private int waveNumber;
    private boolean waveStart;
    private int waveDelay;

    private long slowDownTimer, slowDownTimerDiff;
    private int slowDownLength = 6000;

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
        powerUps = new ArrayList<PowerUp>();
        explosions = new ArrayList<Explosion>();
        texts = new ArrayList<Text>();

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

        g.setColor(new Color(0,100,255));
        g.fillRect(0,0,WIDTH,HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Centruy Gothic", Font.PLAIN,16));
        String s = "G A M E   O V E R";
        int length = (int) g.getFontMetrics().getStringBounds(s,g).getWidth();
        g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2);
        s = "Final Score:  " + player.getScore();
        length = (int) g.getFontMetrics().getStringBounds(s,g).getWidth();
        g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2+30);
        gameDraw();

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

        // Power Up Update
        for (int i = 0; i < powerUps.size(); i++) {
            boolean remove = powerUps.get(i).update();
            if (remove) {
                powerUps.remove(i);
                i--;
            }
        }

        // explosion update
        for (int i = 0; i < explosions.size(); i++) {
            boolean remove = explosions.get(i).update();
            if (remove) {
                explosions.remove(i);
                i--;
            }
        }
        // Text update
        for(int i= 0; i < texts.size(); i++){
            boolean remove = texts.get(i).update();
            if(remove){
                texts.remove(i);
                i--;
            }


        }

        // bullet-enemy collision
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            double bx = b.getX();
            double by = b.getY();
            double br = b.getR();

            for (int j = 0; j < enemies.size(); j++) {
                Enemy e = enemies.get(j);
                double ex = e.getX();
                double ey = e.getY();
                double er = e.getR();

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
            Enemy e = enemies.get(i);

            if (e.isDead()) {
                // chance for powerUp
                double rand = Math.random();
                if (rand < 0.001) {
                    powerUps.add(new PowerUp(1, e.getX(), e.getY()));
                } else if (rand < 0.02) {
                    powerUps.add(new PowerUp(3, e.getX(), e.getY()));
                } else if (rand < 0.12) {
                    powerUps.add(new PowerUp(2, e.getX(), e.getY()));
                }
                  else if (rand < 0.13) {
                    powerUps.add(new PowerUp(4, e.getX(), e.getY()));
                }
                player.addScore(e.getType() + e.getRank());
                enemies.remove(i);
                i--;

                e.explode();
                explosions.add(new Explosion(e.getX(), e.getY(), e.getR(), e.getR() + 20));
            }


        }

        // player position
        int px = player.getX();
        int py = player.getY();
        int pr = player.getR();

        // check player dead

        if(player.isDead()) {
            running = false;
        }

        // player-enemy collision
        if (!player.isRecovering()) {

            for (int i = 0; i < enemies.size(); i++) {
                Enemy e = enemies.get(i);
                double ex = e.getX();
                double ey = e.getY();
                double er = e.getR();

                double dx = px - ex;
                double dy = py - ey;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < pr + er) {
                    player.loseLife();
                }
            }
        }

        // player-powerup collision
        for (int i = 0; i < powerUps.size(); i++) {
            PowerUp powerUp = powerUps.get(i);
            double powerUpX = powerUp.getX();
            double powerUpY = powerUp.getY();
            double powerUpR = powerUp.getR();

            double dx = px - powerUpX;
            double dy = py - powerUpY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < pr + powerUpR) {
                int type = powerUp.getType();

                if (type == 1) {
                    player.gainLife();
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Ekstra Life" ));
                } else if (type == 2) {
                    player.increasePower(1);
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Power" ));
                } else if (type == 3) {
                    player.increasePower(2);
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Double Power" ));
                }
                else if (type == 4) {
                    slowDownTimer = System.nanoTime();
                    for (int j = 0; j < enemies.size(); j++){
                        enemies.get(j).setSlow(true);
                    }
                    texts.add(new Text(player.getX(), player.getY(), 2000, "Slow Down" ));
                }

                powerUps.remove(i);
                i--;
            }
        }

        // Slowdown Update

        if (slowDownTimer != 0){
            slowDownTimerDiff = (System.nanoTime() - waveStartTimer) / 1000000;
            if (slowDownTimerDiff > slowDownLength) {
                slowDownTimer = 0;
                for (int j = 0; j < enemies.size(); j++){
                    enemies.get(j).setSlow(false);
                }
            }
        }
    }

    private void gameRender() {
        // draw background
        g.setColor(new Color(0, 100, 255));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // draw slowdown screen
        if (slowDownTimer != 0) {
            g.setColor((new Color(255, 255, 255, 64)));
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }

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

        // Draw Power Ups
        for (int i = 0; i < powerUps.size(); i++) {
            powerUps.get(i).draw(g);
        }

        // Draw Explosions
        for (int i = 0; i < explosions.size(); i++) {
            explosions.get(i).draw(g);
        }

        // Draw text
        for (int i = 0; i< texts.size(); i++){
            texts.get(i).draw(g);
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
            g.fillOval(20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);
            g.setStroke(new BasicStroke(3));
            g.setColor(Color.WHITE.darker());
            g.drawOval(20 + (20 * i), 20, player.getR() * 2, player.getR() * 2);
            g.setStroke(new BasicStroke(1));
        }

        // draw player power
        g.setColor(Color.YELLOW);
        g.fillRect(20, 40, player.getPower() * 8, 8);
        g.setColor(Color.YELLOW.darker());
        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < player.getRequiredPower(); i++) {
            g.drawRect(20 + 8 * i, 40, 8, 8);
        }
        g.setStroke(new BasicStroke(1));

        // draw player score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Century Gothic", Font.PLAIN, 14));
        g.drawString("Score : " + player.getScore(), WIDTH - 100, 30);

        // Slowdonw Meter
        if(slowDownTimer !=0) {
            g.setColor(Color.WHITE);
            g.drawRect(20, 60, 100, 8);
            g.fillRect(20,60, (int) (100 - 100.0 * slowDownTimerDiff / slowDownLength), 8);
        }
    }

    private void gameDraw() {
        Graphics g2 = this.getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }

    private void createEnemies() {
        enemies.clear();
        Enemy e;

        if (waveNumber == 1) {
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(1, 1));
            }
        } else if (waveNumber == 2) {
            for (int i = 0; i < 8; i++) {
                enemies.add(new Enemy(1, 1));
            }
//            enemies.add(new Enemy(1, 2));
//            enemies.add(new Enemy(1, 2));
        } else if (waveNumber == 3) {
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(1, 1));
            }
            enemies.add(new Enemy(1, 2));
            enemies.add(new Enemy(1, 2));
        }
        else if (waveNumber == 4) {
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(1, 4));
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(2, 1));
            }
        }
        else if (waveNumber == 5) {
            enemies.add(new Enemy(1, 4));
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(2, 3));
        }
        else if (waveNumber == 6) {
            enemies.add(new Enemy(1, 3));
            for (int i = 0; i < 4; i++) {
                enemies.add(new Enemy(2, 1));
                enemies.add(new Enemy(3, 1));
            }
        }
        else if (waveNumber == 7) {
            enemies.add(new Enemy(1, 3));
            enemies.add(new Enemy(2, 3));
            enemies.add(new Enemy(3, 3));
        }
        else if (waveNumber == 8) {
            enemies.add(new Enemy(1, 4));
            enemies.add(new Enemy(2, 4));
            enemies.add(new Enemy(3, 4));
        }
        else if (waveNumber ==9) {
            running = false;
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
