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
	private Double[] lastRSSI;
	private Double[] lastDistance;
	private String lastDate;
	private Integer lastId;

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
		this.lastRSSI = new Double[5];
		this.lastDistance = new Double[5];
		this.lastId = 0;
	}

	public Double getLastRSSI(int numBalise) {
		return this.lastRSSI[numBalise - 1];
	}
	
	public Double getLastDistance(int numBalise) {
		return this.lastDistance[numBalise-1];
	}

	public String getLastDate() {
		return this.lastDate;
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
			double res2 = calculateDistance(rssi);
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
	
	private boolean rollbackPosition() {
		try { 

			connexion = DriverManager.getConnection(this.urlBdd, this.userNameBdd, this.userPswBdd);

			Statement statement = connexion.createStatement();
			ResultSet resultat = null;
			
			if (this.lastId==0) {
				resultat = statement.executeQuery("SELECT *  FROM hist_rssi ORDER BY date_pos DESC;");
				//resultat = statement.executeQuery("SELECT * FROM hist_rssi ORDER BY date_pos DESC limit 1;");
				if (resultat.next()) {
					this.lastId = resultat.getInt("id_hist_rssi");
				}
			}
			
			resultat = null;
			LinkedHashMap<String, Double> distanceInfoTemp = new LinkedHashMap<String, Double>();
			
			do {
					resultat = statement.executeQuery("SELECT * FROM hist_rssi where id_hist_rssi = '"+this.lastId+"' ORDER BY date_pos DESC limit 1;");
					if (resultat==null) {
						this.lastId--;
						continue;
					}
					
					if (resultat.next()) {
						this.lastUpdate = resultat.getDate("date_pos");
						distanceInfoTemp.put("1", rssiToDistance(resultat.getDouble("rssi_bal_1")));
						distanceInfoTemp.put("2", rssiToDistance(resultat.getDouble("rssi_bal_2")));
						distanceInfoTemp.put("3", rssiToDistance(resultat.getDouble("rssi_bal_3")));
						distanceInfoTemp.put("4", rssiToDistance(resultat.getDouble("rssi_bal_4")));
						distanceInfoTemp.put("5", rssiToDistance(resultat.getDouble("rssi_bal_5")));
						this.lastRSSI[0] = resultat.getDouble("rssi_bal_1");
						this.lastRSSI[1] = resultat.getDouble("rssi_bal_2");
						this.lastRSSI[2] = resultat.getDouble("rssi_bal_3");
						this.lastRSSI[3] = resultat.getDouble("rssi_bal_4");
						this.lastRSSI[4] = resultat.getDouble("rssi_bal_5");
						this.lastDate = resultat.getString("date_pos");
						this.lastDistance[0] = rssiToDistance(this.lastRSSI[0]);
						this.lastDistance[1] = rssiToDistance(this.lastRSSI[1]);
						this.lastDistance[2] = rssiToDistance(this.lastRSSI[2]);
						this.lastDistance[3] = rssiToDistance(this.lastRSSI[3]);
						this.lastDistance[4] = rssiToDistance(this.lastRSSI[4]);
					}
					this.lastId--;
					
					if (isValid(distanceInfoTemp))
						break;
				
			}while (this.lastId>0);

			clearDistanceInfo(distanceInfoTemp);
			return true;

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
				this.lastRSSI[0] = resultat.getDouble("rssi_bal_1");
				this.lastRSSI[1] = resultat.getDouble("rssi_bal_2");
				this.lastRSSI[2] = resultat.getDouble("rssi_bal_3");
				this.lastRSSI[3] = resultat.getDouble("rssi_bal_4");
				this.lastRSSI[4] = resultat.getDouble("rssi_bal_5");
				this.lastDate = resultat.getString("date_pos");
				this.lastDistance[0] = rssiToDistance(this.lastRSSI[0]);
				this.lastDistance[1] = rssiToDistance(this.lastRSSI[1]);
				this.lastDistance[2] = rssiToDistance(this.lastRSSI[2]);
				this.lastDistance[3] = rssiToDistance(this.lastRSSI[3]);
				this.lastDistance[4] = rssiToDistance(this.lastRSSI[4]);

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
	 * @param roolback: si true, met à jour la position avec un retour en arrière, sinon, met à jour à la dernière position.
	 * 
	 * @return true si la position à changer, false sinon.
	 */
	public boolean updatePosition(boolean rollback) {

		boolean mooved;
		
		if (rollback) {
			mooved = rollbackPosition();
		}else
		{
			mooved = updateData();
		}

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

	public int getNbBaliseActive() {
		return this.nbBaliseActive;
	}

}
