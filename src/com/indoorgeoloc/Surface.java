package com.indoorgeoloc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.RenderingHints;

class Surface extends JPanel implements ActionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int DELAY = 1000;
    private Timer timer;
    private Image image;
    /**
     * Image Width
     */
    private int iw;
    /**
     * Image Height
     */
    private int ih;

    public Surface() {

        initTimer();
        loadImage();
    }
    
    private void loadImage()
    {
    	 image = new ImageIcon("pict/PlanSalle.png").getImage();
    	 iw = image.getWidth(null);
         ih = image.getHeight(null);
    }

    private void initTimer() {

        timer = new Timer(DELAY, this);
        timer.start();
    }
    
    public Timer getTimer() {
        
        return timer;
    }
    
    private double[] chooseImageDimension()
	{
		double [] res = {iw,ih};
		
		if (iw<getWidth() && ih<getHeight())
		{
			return res;
		}
		
		
		
		return res;
	}

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        
        BufferedImage bi = new BufferedImage(getWidth(),getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bigr = bi.createGraphics();
        bigr.drawImage(image, 0, 0, null);
        bigr.dispose();

        int coef = getWidth()/iw;
        System.out.println(getWidth()+"  "+ih*coef);
        g2d.drawImage(bi, 0, 0, getWidth(), ih*coef, this);
        

        //g2d.dispose();

        g2d.setPaint(Color.blue);
        
        RenderingHints rh = new RenderingHints(
        		RenderingHints.KEY_ANTIALIASING,
        		RenderingHints.VALUE_ANTIALIAS_ON);
        
        rh.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        
        g2d.setRenderingHints(rh);
        

        int w = getWidth();
        int h = getHeight();
        
        /*
        // Ajout de la balise 1 sur la map
        g2d.fill(new Rectangle2D.Double(position.getBaliseX("1"),position.getBaliseY("1"), 10, 10));

     // Ajout de la balise 2 sur la map
        g2d.fill(new Rectangle2D.Double(position.getBaliseX("2"),position.getBaliseY("2"), 10, 10));
        
     // Ajout de la balise 3 sur la map
        g2d.fill(new Rectangle2D.Double(position.getBaliseX("3"),position.getBaliseY("3"), 10, 10));
        
     // Ajout de la balise 4 sur la map
        g2d.fill(new Rectangle2D.Double(position.getBaliseX("4"),position.getBaliseY("4"), 10, 10));
        
     // Ajout de la balise 5 sur la map
        g2d.fill(new Rectangle2D.Double(position.getBaliseX("5"),position.getBaliseY("5"), 10, 10));
        */

        	Random r = new Random();
            int x = Math.abs(r.nextInt()) % w;
            int y = Math.abs(r.nextInt()) % h;
            x=781+150;
            y=56+182;
            
            g2d.fill(new Ellipse2D.Double(x, y, 10, 10));
        
            
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}