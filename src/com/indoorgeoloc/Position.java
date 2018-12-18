package com.indoorgeoloc;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
	private String urlBdd;
	private String userNameBdd;
	private String userPswBdd;
	Connection connexion;
	
	/**
	 * 
	 * @param tridimention boolean : true pour un travail en 3D, false pour du 2D
	 * @param balisePosition LinkedHashMap<String,double[][]> : map des positions des balises.
	 */
	public Position(boolean tridimention, LinkedHashMap<String,double[]> balisePosition)
	{
		/* Chargement du driver JDBC pour MySQL */
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.out.println("Erreur du chargement du driver MySQL");
			e1.printStackTrace();
		}
		
		this.tridimention = tridimention;
		this.balisePosition = balisePosition;
		distanceInfo = new LinkedHashMap<>();
		this.centroid = null;
		this.lastUpdate = new Date();
		this.urlBdd = "jdbc:mysql://10.26.3.102:3306/geoloc";
		this.userNameBdd = "defi";
		this.userPswBdd = "defi";
		this.connexion = null;
	}
	
	public double[] getCentroid()
	{
		return this.centroid;
	}
	
	private double rssiToDistance(double rssi)
	{
		//TODO mesurer la valeur du rssi à 1 metre
		// rssi à 1m
		double a = 0;
		double lambda = 20.5;
		double res = Math.pow(10.0, (rssi-a)/lambda);
		return res;
	}
	
	/**
	 * Récupère et stocke dans baliseInfo les dernière informations de la bdd
	 * @return boolean : true si la position à changé, false sinon
	 * 
	 */
	private boolean updateData()
	{
		try {

		    connexion = DriverManager.getConnection( this.urlBdd, this.userNameBdd, this.userPswBdd );


		    Statement statement = connexion.createStatement();
		    ResultSet resultat = statement.executeQuery( "SELECT *  FROM hist_rssi ORDER BY date_pos DESC LIMIT 1;" );
		    LinkedHashMap<String,Double> distanceInfoTemp = new LinkedHashMap<String, Double>();
		    while ( resultat.next() ) {
		    	this.lastUpdate = resultat.getDate("date_pos");
		    	distanceInfoTemp.put("1",rssiToDistance(resultat.getDouble("rssi_bal_1")));
		    	distanceInfoTemp.put("2",rssiToDistance(resultat.getDouble("rssi_bal_2")));
		    	distanceInfoTemp.put("3",rssiToDistance(resultat.getDouble("rssi_bal_3")));
		    	distanceInfoTemp.put("4",rssiToDistance(resultat.getDouble("rssi_bal_4")));
		    	distanceInfoTemp.put("5",rssiToDistance(resultat.getDouble("rssi_bal_5")));
		    }
		    
		    if (this.distanceInfo.equals(distanceInfoTemp))
		    	return false;
		    
		    this.distanceInfo.clear();
		    this.distanceInfo = distanceInfoTemp;


		} catch ( SQLException e ) {

			System.out.println("Erreur de connection à la bdd");
		    return false;

		} finally {

		    if ( connexion != null )

		        try {

		            /* Fermeture de la connexion */

		            connexion.close();

		        } catch ( SQLException ignore ) {

		            /* Si une erreur survient lors de la fermeture, il suffit de l'ignorer. */

		        }

		}
		return false;
	}
	/**
	 * Met à jour la position en fonction de baliseInfo.
	 * @return true si la position à changer, false sinon.
	 */
	public boolean updatePosition()
	{
		
		boolean mooved = updateData();
		
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
