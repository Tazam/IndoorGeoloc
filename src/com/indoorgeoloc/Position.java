package com.indoorgeoloc;


import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;

import com.trilateration.NonLinearLeastSquaresSolver;
import com.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;


/*
 * TODO class exception
 */

/**
 * @author Gaetan
 *
 */
public class Position {
	private LinkedHashMap<String,double[]> balisePosition;
	private LinkedHashMap<String,Double> distanceInfo;
	private boolean tridimention;
	private Date lastUpdate;
	private double[] centroid;
	
	/**
	 * 
	 * @param tridimention boolean : true pour un travail en 3D, false pour du 2D
	 * @param balisePosition LinkedHashMap<String,double[][]> : map des positions des balises.
	 */
	public Position(boolean tridimention, LinkedHashMap<String,double[]> balisePosition)
	{
		this.tridimention = tridimention;
		this.balisePosition = balisePosition;
		distanceInfo = new LinkedHashMap<>();
		this.centroid = null;
		this.lastUpdate = new Date();
		
	}
	
	public double[] getCentroid()
	{
		return this.centroid;
	}
	
	/**
	 * Récupère et stoke dans baliseInfo les dernière informations de la bdd
	 * @return boolean : true si la position à changé, false sinon
	 * 
	 */
	private boolean updateData()
	{
		this.distanceInfo.clear();
		this.distanceInfo.put("1", (Double)3.0);
		this.distanceInfo.put("2", (Double)4.0);
		this.distanceInfo.put("3", (Double)3.0);
		return true;
	}
	/**
	 * Met à jour la position en fonction de baliseInfo.
	 * @return true si la position à changer, false sinon.
	 */
	public boolean updatePosition()
	{
		
		boolean mooved = updateData();
		
		// TODO throw exception à la place du bool
		// Si 
		if (distanceInfo.size()<3 || !mooved)
			return false;
		
		double[][] positions = new double[balisePosition.size()][2];
		double[] distances = new double[distanceInfo.size()];
		int i = 0;
		for (String baliseName : distanceInfo.keySet())
		{
			positions[i] = this.balisePosition.get(baliseName);
			distances[i] = this.distanceInfo.get(baliseName);
			i++;
		}
		
		NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
		Optimum optimum = solver.solve();
		centroid = optimum.getPoint().toArray();
		
		return true;
	}
	
}
