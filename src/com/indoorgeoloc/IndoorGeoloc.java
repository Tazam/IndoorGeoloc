package com.indoorgeoloc;
import java.util.LinkedHashMap;

/**
 * 
 */

/**
 * @author Gaetan
 *
 */
public class IndoorGeoloc {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double[][] positions = new double[][] { { 5.0, -6.0 }, { 13.0, -15.0 }, { 21.0, -3.0 }, { 12.4, -21.2 } };
		
		LinkedHashMap<String,double[]> balisePosition = new LinkedHashMap<>();
		
		balisePosition.put("1", new double[]{4.0,6.0});
		balisePosition.put("2", new double[]{8.0,3.0});
		balisePosition.put("3", new double[]{4.0,0});
		Position p = new Position(false,balisePosition);
		p.updatePosition();
		System.out.println(p.getCentroid()[0]+"  "+p.getCentroid()[1]);

	}

}
