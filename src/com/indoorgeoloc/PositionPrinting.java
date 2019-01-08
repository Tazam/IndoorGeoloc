/**
 * 
 */
package com.indoorgeoloc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.RenderingHints;


/**
 * @author Schmidt Gaëtan
 * 
 *         Gère l'affichage et la mise à jour des positions
 */
public class PositionPrinting extends JPanel implements ActionListener {

	 /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int DELAY = 1000;
	    private Timer timer;
	    private Image image;
	    private Position position;
	    private double xCurr;
	    private double yCurr;
	    /**
	     * Image Width
	     */
	    private int iw;
	    /**
	     * Image Height
	     */
	    private int ih;

	    public PositionPrinting(Position p) {

	        initTimer();
	        loadImage();
	        this.position = p;
	        this.xCurr = 50.0;
	        this.yCurr = 50.0;
	        this.setBounds(0, 0, 2000, 2000);
	    }
	    
	    private void loadImage()
	    {
	    	 image = new ImageIcon("pict/PlanSalle.png").getImage();
	    	 iw = image.getWidth(null);
	         ih = image.getHeight(null);
	         this.setPreferredSize(new Dimension(iw,ih));
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
	    
	    private double distToPx(double dist)
	    {
	    	return dist * 100 * 1.24;
	    }
	    

	    private void doDrawing(Graphics g) {

	        Graphics2D g2d = (Graphics2D) g;
	        
	        BufferedImage bi = new BufferedImage(getWidth(),getHeight(), BufferedImage.TYPE_INT_ARGB);
	        Graphics2D bigr = bi.createGraphics();
	        bigr.drawImage(image, 0, 0, null);
	        bigr.dispose();

	        double coef = getWidth()/iw;
	        g2d.drawImage(bi, 0, 0, getWidth(), ih, this);
	        

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
	        
	        
	        // Ajout de la balise 1 sur la map
	        double xBal = distToPx(position.getBaliseX("1"))+150;
	        double yBal = distToPx(position.getBaliseY("1"))+182;
	        g2d.fill(new Rectangle2D.Double(xBal,yBal, 10, 10));

	     // Ajout de la balise 2 sur la map
	        xBal = distToPx(position.getBaliseX("2"))+150;
	        yBal = distToPx(position.getBaliseY("2"))+182;
	        g2d.fill(new Rectangle2D.Double(xBal,yBal, 10, 10));
	        
	     // Ajout de la balise 3 sur la map
	       // g2d.fill(new Rectangle2D.Double(position.getBaliseX("3"),position.getBaliseY("3"), 10, 10));
	        xBal = distToPx(position.getBaliseX("3"))+150;
	        yBal = distToPx(position.getBaliseY("3"))+182;
	        g2d.fill(new Rectangle2D.Double(xBal,yBal, 10, 10));
	        
	     // Ajout de la balise 4 sur la map
	       // g2d.fill(new Rectangle2D.Double(position.getBaliseX("4"),position.getBaliseY("4"), 10, 10));
	        xBal = distToPx(position.getBaliseX("4"))+150;
	        yBal = distToPx(position.getBaliseY("4"))+182;
	        g2d.fill(new Rectangle2D.Double(xBal,yBal, 10, 10));
	        
	     // Ajout de la balise 5 sur la map
	        //g2d.fill(new Rectangle2D.Double(position.getBaliseX("5"),position.getBaliseY("5"), 10, 10));
	        xBal = distToPx(position.getBaliseX("5"))+150;
	        yBal = distToPx(position.getBaliseY("5"))+182;
	        g2d.fill(new Rectangle2D.Double(xBal,yBal, 10, 10));
	        
	        	/*
	        	Random r = new Random();
	            int x = Math.abs(r.nextInt()) % w;
	            int y = Math.abs(r.nextInt()) % h;
	           */
	        
	        	Boolean mooved = this.position.updatePosition();
	        	if (mooved)
	        	{
	        		this.xCurr = distToPx(this.position.getCentroid()[0])+150;
	        		this.yCurr = distToPx(this.position.getCentroid()[1])+182;
	        		System.out.println("xCurr: "+this.xCurr+" yCurr: "+this.yCurr);
	        		int precision = this.position.getNbBaliseActive();
	        		if (precision==0||precision==3) {
	        			g2d.setPaint(Color.red);
	        		}
	        		if (precision==4) {
	        			g2d.setPaint(Color.orange);
	        		}
	        		if (precision==5) {
	        			g2d.setPaint(Color.green);
	        		}
	        		
	        	}
	            
	            g2d.fill(new Ellipse2D.Double(xCurr, yCurr, 10, 10));
	            
	            Random r = new Random();
	            int x = Math.abs(r.nextInt()) % w;
	            int y = Math.abs(r.nextInt()) % h;
	            //g2d.fill(new Ellipse2D.Double(x, y, 10, 10));
	            
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
