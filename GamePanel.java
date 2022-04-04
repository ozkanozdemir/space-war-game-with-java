import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.*;


public class GamePanel extends JPanel implements Runnable {

    //Fields
    public static int WIDTH = 400;
    public static int HEIGHT = 400;
    private Thread thread;
    private boolean running;
    private BufferedImage image;
    private Graphics2D g;





    //Constructor
    public GamePanel(){
        super ();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
    }

    //Functions
    public void addNotify(){
        super.addNotify();
        if(thread==null){
            thread = new Thread(this);
            thread.start();
        }
    }

    public void run(){
        running = true;
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();

        //GameLoop
        while (running){
            gameUpdate();
            gameRender();
            gameDraw();

        }

    }

    private void gameUpdate(){

    }

    private void gameRender(){
    g.setColor(Color.white);
    g.fillRect(0,0,WIDTH,HEIGHT);
    g.setColor(Color.BLACK);
    g.drawString("TEST STRING", 100,100);

    }

    private void gameDraw(){
    Graphics g2 = this.getGraphics();
    g2.drawImage(image, 0,0,null);
    g2.dispose();
    }




}
