package com.indoorgeoloc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;

import com.trilateration.NonLinearLeastSquaresSolver;
import com.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

/*
 * TODO class exception
 */

/**
 * @author Gaetan Classe de calcule de la position d'un objet par
 *         multilateration
 *
 */
public class Position {
	private LinkedHashMap<String, double[]> balisePosition;
	private LinkedHashMap<String, Double> distanceInfo;
	private Date lastUpdate;
	private double[] centroid;
	private String urlBdd;
	private String userNameBdd;
	private String userPswBdd;
	Connection connexion;
	private int nbBaliseActive;

	/**
	 * 
	 * @param balisePosition
	 *            LinkedHashMap<String,double[][]> : map des positions des balises.
	 */
	public Position(LinkedHashMap<String, double[]> balisePosition) {
		/* Chargement du driver JDBC pour MySQL */

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.out.println("Erreur du chargement du driver MySQL");
			e1.printStackTrace();
		}

		this.balisePosition = balisePosition;
		distanceInfo = new LinkedHashMap<>();
		this.centroid = null;
		this.lastUpdate = new Date();
		// this.urlBdd = "jdbc:mysql://10.120.14.149:3306/geoloc";
		this.urlBdd = "jdbc:mysql://localhost:3306/geoloc?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC";
		this.userNameBdd = "defi";
		this.userPswBdd = "defi";
		this.connexion = null;
		nbBaliseActive = 0;
	}

	public double getBaliseX(String baliseNum) {
		return this.balisePosition.get(baliseNum)[0];
	}

	public double getBaliseY(String baliseNum) {
		return this.balisePosition.get(baliseNum)[1];
	}

	public double[] getCentroid() {
		return this.centroid;
	}

	private double calculateDistance(double rssi) {

		double txPower = -65.0; // hard coded power value. Usually ranges between -59 to -65

		if (rssi == 0.0) {
			return 0.0;
		}

		double ratio = rssi * 1.0 / txPower;
		if (ratio < 1.0) {
			return Math.pow(ratio, 10);
		} else {
			double distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
			return distance;
		}
	}

	private double rssiToDistance(double rssi) {
		if (true) {
			double res2= calculateDistance(rssi);
			System.out.println("rssiToDistance (" + rssi + ") --> " + res2);
			return res2;
		}
		if (rssi == 0.0)
			return 0.0;
		// TODO mesurer la valeur du rssi à 1 metre
		// rssi à 1m
		double a = 42;
		double lambda = 20.5;
		double res = Math.pow(10.0, (rssi - a) / lambda);
		res = res * 100;
		System.out.println("rssiToDistance (" + rssi + ") --> " + res);
		return res;
	}

	private boolean isValid(LinkedHashMap<String, Double> map) {
		int cpt = map.size();
		for (String key : map.keySet()) {
			if (map.get(key) == 0.0)
				cpt--;
		}
		System.out.println("cpt: " + cpt);
		return cpt > 2;
	}

	private void clearDistanceInfo(Map<String, Double> map) {

		this.distanceInfo.clear();
		for (String key : map.keySet()) {

			if (map.get(key) != 0.0) {
				this.distanceInfo.put(key, map.get(key));
				System.out.println("get: " + this.distanceInfo.toString());
			}
		}

	}

	/**
	 * Récupère et stocke dans baliseInfo les dernière informations de la bdd
	 * 
	 * @return boolean : true si la position à changé, false sinon
	 * 
	 */
	private boolean updateData() {
		try {

			connexion = DriverManager.getConnection(this.urlBdd, this.userNameBdd, this.userPswBdd);

			Statement statement = connexion.createStatement();
			ResultSet resultat = statement.executeQuery("SELECT *  FROM hist_rssi ORDER BY date_pos DESC;");
			LinkedHashMap<String, Double> distanceInfoTemp = new LinkedHashMap<String, Double>();
			while (resultat.next()) {
				this.lastUpdate = resultat.getDate("date_pos");
				distanceInfoTemp.put("1", rssiToDistance(resultat.getDouble("rssi_bal_1")));
				distanceInfoTemp.put("2", rssiToDistance(resultat.getDouble("rssi_bal_2")));
				distanceInfoTemp.put("3", rssiToDistance(resultat.getDouble("rssi_bal_3")));
				distanceInfoTemp.put("4", rssiToDistance(resultat.getDouble("rssi_bal_4")));
				distanceInfoTemp.put("5", rssiToDistance(resultat.getDouble("rssi_bal_5")));

				if (isValid(distanceInfoTemp))
					break;
			}
			if (this.distanceInfo.equals(distanceInfoTemp))
				return false;

			clearDistanceInfo(distanceInfoTemp);

		} catch (SQLException e) {

			System.out.println("Erreur de connection à la bdd");
			e.printStackTrace(System.out);
			return false;

		} finally {

			if (connexion != null)

				try {

					/* Fermeture de la connexion */

					connexion.close();

				} catch (SQLException ignore) {

					/* Si une erreur survient lors de la fermeture, il suffit de l'ignorer. */

				}

		}
		return true;
	}

	/**
	 * Met à jour la position en fonction de baliseInfo.
	 * 
	 * @return true si la position à changer, false sinon.
	 */
	public boolean updatePosition() {

		boolean mooved = updateData();

		System.out.println("RSSI -> " + this.distanceInfo.toString());

		// Si on à pas assez de données on laisse la position tel quelle
		if (distanceInfo.size() < 3 || !mooved)
			return false;

		double[][] positions = new double[distanceInfo.size()][2];
		double[] distances = new double[distanceInfo.size()];
		int i = 0;
		for (String baliseName : distanceInfo.keySet()) {
			if (this.distanceInfo.containsKey(baliseName) && this.distanceInfo.get(baliseName) != null) {
				positions[i] = this.balisePosition.get(baliseName);
				distances[i] = this.distanceInfo.get(baliseName);
				i++;
			}
		}

		NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(
				new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
		Optimum optimum = solver.solve();
		centroid = optimum.getPoint().toArray();
		// System.out.println("RSSI: "+distances.toString());
		
		this.nbBaliseActive = this.distanceInfo.size();
		return true;
	}
	
	public int getNbBaliseActive()
	{
		return this.nbBaliseActive;
	}

}
