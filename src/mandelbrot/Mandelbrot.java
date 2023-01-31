package mandelbrot;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
/**
 *
 * @author james
 */
public class Mandelbrot implements MouseMotionListener{
    private final int FRAMEWIDTH = 500;
    private final int FRAMEHEIGHT = 500;
    private double zoom = 1;
    private double mouseX = 0, mouseY = 0;
    private final double[][] RENDERVALUES = new double[FRAMEWIDTH][FRAMEHEIGHT];
    private Canvas canvas;
    private Frame frame;
    
    public static void main(String[] args) {
        Mandelbrot mb = new Mandelbrot();
        mb.frame = new Frame();
        mb.frame.setSize(mb.FRAMEWIDTH, mb.FRAMEHEIGHT);
        mb.frame.setResizable(true);
        mb.frame.addMouseWheelListener((MouseWheelEvent e) -> {
            if (e.getWheelRotation() < 0) {
                mb.zoom -= (mb.zoom*0.02);
            } else {
                mb.zoom += (mb.zoom*0.02);
            }
        });
        mb.canvas = new Canvas();
        mb.canvas.setSize(mb.FRAMEWIDTH, mb.FRAMEHEIGHT);
        mb.canvas.addMouseMotionListener(mb);
        mb.frame.add(mb.canvas);
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        mb.frame.setCursor(blankCursor);
        mb.frame.pack();
        mb.frame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing (WindowEvent e){
                System.exit(0);
            }
        });
        mb.frame.setVisible(true);
        mb.render();
    }
    
    private void calculateValues(){
        double x,y;
        double adjX = (mouseX-(FRAMEWIDTH/2))/(FRAMEWIDTH/2);
        double adjY = ((FRAMEHEIGHT/2)-mouseY)/(FRAMEHEIGHT/2);

        for (int i = 0; i < FRAMEWIDTH; i++) {
            for (int j = 0; j < FRAMEHEIGHT; j++) {
                x = zoom*(i-(FRAMEWIDTH/2))/(FRAMEWIDTH/2);
                x = x+adjX;
                y = zoom*((FRAMEHEIGHT/2)-j)/(FRAMEHEIGHT/2);
                y = y+adjY;
                RENDERVALUES[i][j] = process(x, y);
                //System.out.println(RENDERVALUES[i][j]);
            }
        }
    }
    
    private double process(double x, double y){
        int count = 0;
        double zI = 0;
        double zR = 0;
        while(count<100){
              double tempX = zR*zR - zI*zI;
              zI =  (2*zR*zI) + y;
              zR = tempX + x;
              double modz = (zR*zR)+(zI*zI);
              if(modz>4){
                  return count - log(log(Math.sqrt(modz),2),2);
              }
              count++;
        }
        return 105;
    }
    
    private double log(double x, int base){
        return (Math.log(x) / Math.log(base));
    }
    
    
    private void render(){
        Thread thread;
        thread = new Thread(){
            public void run(){
                GraphicsConfiguration gc = canvas.getGraphicsConfiguration();
                VolatileImage vImage = gc.createCompatibleVolatileImage(FRAMEWIDTH, FRAMEHEIGHT);
                Font f = new Font("TimesRoman", Font.PLAIN, 10);
                
                while(true){
                    double startTime = System.nanoTime();
                    if(vImage.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE){
                        vImage = gc.createCompatibleVolatileImage(FRAMEWIDTH, FRAMEHEIGHT);
                    }

                    Graphics g = vImage.getGraphics();
                    calculateValues();
                    double[][] values = RENDERVALUES;
                    for (int i = 0; i < FRAMEWIDTH; i++) {
                        for (int j = 0; j < FRAMEHEIGHT; j++) {
                            if(values[i][j] == 105){
                                g.setColor(Color.BLACK);
                            }else{
                                g.setColor(Color.getHSBColor((float)values[i][j]/80, 1, 1));
                                //g.setColor(Color.WHITE);
                            }
                            g.fillRect(i, j, 1, 1);
                        }
                    }
                    //FPS COUNTER
                    double endTime = System.nanoTime();
                    double timeDiff = endTime - startTime;
                    double timeDiffSecs = timeDiff/1000000000;
                    int fps = (int)(1/timeDiffSecs);
                    g.setColor(Color.WHITE);
                    g.drawString("FPS: "+fps, 3, 12);

                    
                    //CURSOR
                    g.drawLine(FRAMEWIDTH/2, FRAMEHEIGHT/2 + 10, FRAMEWIDTH/2, FRAMEHEIGHT/2 - 10);
                    g.drawLine(FRAMEWIDTH/2 + 10, FRAMEHEIGHT/2, FRAMEWIDTH/2 - 10, FRAMEHEIGHT/2);
                    
                    
                    g.dispose();
                    g = canvas.getGraphics();
                    g.drawImage(vImage, 0, 0, FRAMEWIDTH, FRAMEHEIGHT, null);
                    g.dispose();
                }
            }  
        };
        thread.start();
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        mouseX = me.getX();
        mouseY = me.getY();
    }

   
    @Override
    public void mouseMoved(MouseEvent me) {
        mouseX = me.getX();
        mouseY = me.getY();
    }

}