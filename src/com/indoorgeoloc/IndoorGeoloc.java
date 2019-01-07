package com.indoorgeoloc;
import java.util.LinkedHashMap;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.JScrollPane;

/**
 * 
 */

/**
 * @author Gaetan
 *
 */
public class IndoorGeoloc extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public IndoorGeoloc(Position p) {

        initUI(p);
    }
	
	
	
	private void initUI2() {

        add(new Surface());

        setSize(300, 300);
        setTitle("Spotlight");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initUI(Position p) {

        PositionPrinting positionPrinting = new PositionPrinting(p);
        JScrollPane jScrollPane = new JScrollPane(positionPrinting,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setMinimumSize(new Dimension(2000,2000));
        add(jScrollPane);
        this.setSize(2000, 2000);
        this.setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Timer timer = positionPrinting.getTimer();
                timer.stop();
            }
        });
        Dimension dimension= Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("x:"+dimension.getWidth()+" y:"+dimension.height);
        setTitle("Points");
        setSize(dimension.width, dimension.height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
    	
    	// tableau de test
		
    			// map contenant la position des balises
    			LinkedHashMap<String,double[]> balisePosition = new LinkedHashMap<>();
    			// en Metre
    			balisePosition.put("1", new double[]{1.05, 6.9});
    			balisePosition.put("2", new double[]{5.25, 6.75});
    			balisePosition.put("3", new double[]{1.05, 0.45});
    			balisePosition.put("4", new double[]{6.3,0.45});
    			balisePosition.put("5", new double[]{0.0, 3.6});
    			
    			Position p = new Position(balisePosition);
    			
    			//p.updatePosition();
    			//System.out.println(p.getCentroid()[0]+"  "+p.getCentroid()[1]);

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                IndoorGeoloc ex = new IndoorGeoloc(p);
                ex.setVisible(true);
            }
        });
    }
}
