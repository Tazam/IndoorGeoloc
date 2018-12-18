package com.indoorgeoloc;


import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.Timer;

public class PointsEx extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PointsEx() {

        initUI();
    }
	
	private void initUI2() {

        add(new Surface());

        setSize(300, 300);
        setTitle("Spotlight");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initUI() {

        final Surface surface = new Surface();
        add(surface);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Timer timer = surface.getTimer();
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

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                PointsEx ex = new PointsEx();
                ex.setVisible(true);
            }
        });
    }
}