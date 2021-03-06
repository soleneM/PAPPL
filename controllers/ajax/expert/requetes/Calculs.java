/*********************************************************************************
 * 
 *   Copyright 2014 BOUSSEJRA Malik Olivier, HALDEBIQUE Geoffroy, ROYER Johan
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *   
 ********************************************************************************/
package controllers.ajax.expert.requetes;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;

import functions.cartes.Carte;
import functions.excels.Excel;
import functions.excels.HistogrammeExportExcel;
import functions.excels.ListeExportExcel;

import play.db.*;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.expert.requetes.ajax.resultats.temoinsParPeriode;
import views.html.expert.requetes.ajax.resultats.histogrammeDesImagos;
import views.html.expert.requetes.ajax.resultats.carteSomme;
import views.html.expert.requetes.ajax.resultats.carteSommeBiodiversite;
import views.html.expert.requetes.ajax.resultats.maillesParEspece;
import views.html.expert.requetes.ajax.resultats.chronologieDUnTemoin;
import views.html.expert.requetes.ajax.resultats.historiqueDesEspeces;
import views.html.expert.requetes.ajax.resultats.maillesParPeriode;
import views.html.expert.requetes.ajax.resultats.emptyExcel;
import views.html.expert.requetes.ajax.resultats.exportExcel;

import com.avaje.ebean.Expr;

import models.Espece;
import models.FicheHasMembre;
import models.Groupe;
import models.InformationsComplementaires;
import models.Membre;
import models.Observation;
import models.StadeSexe;
import models.UTMS;

public class Calculs extends Controller {

	/**
	 * Permet d'ajouter des parametres pour les PreparedStatement
	 * 
	 * @param aStatement
	 *            un statement de requete SQL avec des ?
	 * @param listeParams
	 *            une liste de parametres
	 * @throws SQLException
	 */
	private static void setParams(PreparedStatement aStatement, ArrayList<Object> listeParams) throws SQLException {
		int index = 0;
		for (Object o : listeParams) {
			index++;
			if (o instanceof String) {
				aStatement.setString(index, (String) o);
			} else if (o instanceof java.sql.Date) {
				aStatement.setDate(index, (java.sql.Date) o);
			}
		}
	}

	/**
	 * Calcule la liste des temoignages classes par especes choix possible de la
	 * periode, d'une espece, d'un groupe ou d'un sous-groupe
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @param tailleUTM
	 *            la taille de maille UTM voulue pour les resultats
	 * @return un ResultSet
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static ResultSet calculeListeDesTemoignages(Map<String, String> info, int tailleUTM)
			throws ParseException, SQLException {

		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement listeDesTemoignages;

		ArrayList<Object> listeParams = new ArrayList<Object>();
		String statement = "";
		statement = "SELECT espece.espece_nom as espece_nom, membre.membre_nom as membre_nom, ";
		// utilisation d'un switch au cas ou on voudrait utiliser uniquement les
		// mailles 50x50, ou 100x100
		switch (tailleUTM) {
		case 20:
			statement += "utms.maille20x20, f.fiche_Date";
			break;
		default:
			statement += "utms.utm, utms.maille20x20, utms.maille50x50, utms.maille100x100, f.fiche_Date";
			break;
		}
		statement += " FROM observation obs" + " INNER JOIN fiche f ON obs.observation_fiche_fiche_id = f.fiche_id"
				+ " INNER JOIN espece ON obs.observation_espece_espece_id = espece.espece_id"
				+ " INNER JOIN fiche_has_membre fhm ON fhm.fiche_fiche_id = f.fiche_id"
				+ " INNER JOIN membre ON fhm.membre_membre_id = membre.membre_id";

		if ((info.get("espece") != null) && (!info.get("espece").equals(""))) {
			statement += " INNER JOIN utms ON utms.utm = f.fiche_utm_utm" + " WHERE obs.observation_validee = 1"
					+ " AND espece.espece_id=?";
			listeParams.add(info.get("espece"));
		} else if ((info.get("sous_groupe") != null) && (!info.get("sous_groupe").equals(""))) {
			statement += " INNER JOIN espece_is_in_groupement_local ON espece.espece_id = espece_is_in_groupement_local.espece_espece_id"
					+ " INNER JOIN groupe ON espece_is_in_groupement_local.groupe_groupe_id = groupe.groupe_id"
					+ " INNER JOIN utms ON utms.utm = f.fiche_utm_utm" + " WHERE obs.observation_validee = 1"
					+ " AND groupe.groupe_id=?";
			listeParams.add(info.get("sous_groupe"));
		} else if ((info.get("groupe") != null) && (!info.get("groupe").equals(""))) {
			statement += " INNER JOIN espece_is_in_groupement_local ON espece.espece_id = espece_is_in_groupement_local.espece_espece_id"
					+ " INNER JOIN groupe ON espece_is_in_groupement_local.groupe_groupe_id = groupe.groupe_id"
					+ " INNER JOIN utms ON utms.utm = f.fiche_utm_utm" + " WHERE obs.observation_validee = 1"
					+ " AND groupe.groupe_pere_groupe_id=?";
			listeParams.add(info.get("groupe"));
		} else {
			statement += " INNER JOIN utms ON utms.utm = f.fiche_utm_utm" + " WHERE obs.observation_validee = 1";
		}

		if (!info.get("periode").equals("all")) {
			statement += " AND f.fiche_date BETWEEN ? AND ?";

			Calendar date1 = Calculs.getDataDate1(info);
			Calendar date2 = Calculs.getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}

		String ordre = null;
		switch (tailleUTM) {
		case 20:
			ordre = "espece.espece_nom, utms.maille20x20, f.fiche_Date";
			break;
		default:
			ordre = "espece.espece_nom, utms.utm, f.fiche_Date";
			break;
		}
		statement += " ORDER BY " + ordre;
		info.put("maxtemoignages", "20");

		listeDesTemoignages = connection.prepareStatement(statement);
		setParams(listeDesTemoignages, listeParams);
		ResultSet rs = listeDesTemoignages.executeQuery();

		connection.close();

		return rs;
	}

	/**
	 * Calcule les donnees pour la carte des temoignages choix possible de la
	 * periode, d'une espece, d'un groupe ou d'un sous-groupe
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @param tailleUTM
	 *            la taille de maille UTM voulue pour les resultats
	 * @return une HashMap contenant les informations pour remplir la carte
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static HashMap<UTMS, Integer> calculeCarteTemoignages(Map<String, String> info, int tailleUTM)
			throws ParseException, SQLException {
		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement nbDeTemoignages;

		ArrayList<Object> listeParams = new ArrayList<Object>();
		String statement = "";
		// utilisation d'un switch au cas oe on voudrait utiliser uniquement les
		// mailles 50x50, ou 100x100
		switch (tailleUTM) {
		case 20:
			statement += "SELECT utms.maille20x20, COUNT(f.fiche_Date) AS nbtem";
			break;
		default:
			statement += "SELECT utms.utm, COUNT(f.fiche_Date) AS nbtem";
			break;
		}
		statement += " FROM observation obs" + " INNER JOIN fiche f ON obs.observation_fiche_fiche_id = f.fiche_id"
				+ " INNER JOIN espece ON obs.observation_espece_espece_id = espece.espece_id"
				+ " INNER JOIN fiche_has_membre fhm ON fhm.fiche_fiche_id = f.fiche_id"
				+ " INNER JOIN membre ON fhm.membre_membre_id = membre.membre_id";

		if ((info.get("espece") != null) && (!info.get("espece").equals(""))) {
			statement += " INNER JOIN utms ON utms.utm = f.fiche_utm_utm" + " WHERE obs.observation_validee = 1"
					+ " AND espece.espece_id=?";
			listeParams.add(info.get("espece"));
		} else if ((info.get("sous_groupe") != null) && (!info.get("sous_groupe").equals(""))) {
			statement += " INNER JOIN espece_is_in_groupement_local ON espece.espece_id = espece_is_in_groupement_local.espece_espece_id"
					+ " INNER JOIN groupe ON espece_is_in_groupement_local.groupe_groupe_id = groupe.groupe_id"
					+ " INNER JOIN utms ON utms.utm = f.fiche_utm_utm" + " WHERE obs.observation_validee = 1"
					+ " AND groupe.groupe_id=?";
			listeParams.add(info.get("sous_groupe"));
		} else if ((info.get("groupe") != null) && (!info.get("groupe").equals(""))) {
			statement += " INNER JOIN espece_is_in_groupement_local ON espece.espece_id = espece_is_in_groupement_local.espece_espece_id"
					+ " INNER JOIN groupe ON espece_is_in_groupement_local.groupe_groupe_id = groupe.groupe_id"
					+ " INNER JOIN utms ON utms.utm = f.fiche_utm_utm" + " WHERE obs.observation_validee = 1"
					+ " AND groupe.groupe_pere_groupe_id=?";
			listeParams.add(info.get("groupe"));
		} else {
			statement += " INNER JOIN utms ON utms.utm = f.fiche_utm_utm" + " WHERE obs.observation_validee = 1";
		}

		if (!info.get("periode").equals("all")) {
			statement += " AND f.fiche_date BETWEEN ? AND ?";

			Calendar date1 = Calculs.getDataDate1(info);
			Calendar date2 = Calculs.getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}

		String groupement = null;
		switch (tailleUTM) {
		case 20:
			groupement = "utms.maille20x20";
			break;
		default:
			groupement = "utms.utm";
			break;
		}
		statement += " GROUP BY " + groupement;

		nbDeTemoignages = connection.prepareStatement(statement);
		setParams(nbDeTemoignages, listeParams);
		ResultSet rs = nbDeTemoignages.executeQuery();

		HashMap<UTMS, Integer> carteData = new HashMap<>();
		while (rs.next()) {
			UTMS mailleUTM = new UTMS();
			switch (tailleUTM) {
			case 20:
				mailleUTM.maille20x20 = rs.getString("utms.maille20x20");
				break;
			default:
				mailleUTM.utm = rs.getString("utms.utm");
				break;
			}
			Integer nbTemoignages = rs.getInt("nbtem");
			carteData.put(mailleUTM, nbTemoignages);
		}

		connection.close();
		return carteData;

	}

	/**
	 * Calcule la somme du nombre d'especes temoignees choix possible du groupe,
	 * du sous-groupe et de la periode
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @param tailleUTM
	 *            la taille de maille UTM voulue pour les resultats
	 * @return un ResultSet
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static ResultSet calculeSommeEspeces(Map<String, String> info, int tailleUTM)
			throws ParseException, SQLException {
		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement sommeEspeces;

		ArrayList<Object> listeParams = new ArrayList<Object>();
		String statement = "";

		statement += "SELECT groupe.groupe_nom,";
		switch (tailleUTM) {
		case 20:
			statement += " utms.maille20x20";
			break;
		default:
			statement += " utms.utm";
			break;
		}
		statement += ", COUNT(espece.espece_id) as nbespeces" + " FROM groupe "
				+ " INNER JOIN espece_is_in_groupement_local ON (groupe.groupe_id = espece_is_in_groupement_local.groupe_groupe_id)"
				+ " INNER JOIN espece ON (espece_is_in_groupement_local.espece_espece_id = espece.espece_id)"
				+ " INNER JOIN observation ON (espece.espece_id = observation.observation_espece_espece_id)"
				+ " INNER JOIN fiche ON (observation.observation_fiche_fiche_id = fiche.fiche_id)"
				+ " INNER JOIN utms ON (fiche.fiche_utm_utm = utms.utm)" + " WHERE observation.observation_validee = 1";

		if ((info.get("sous_groupe") != null) && (!info.get("sous_groupe").equals(""))) {
			statement += " AND groupe.groupe_id = ?";
			listeParams.add(info.get("sous_groupe"));
		} else if ((info.get("groupe") != null) && (!info.get("groupe").equals(""))) {
			statement += " AND groupe.groupe_pere_groupe_id = ?";
			listeParams.add(info.get("groupe"));
		}

		if (!info.get("periode").equals("all")) {
			statement += " AND fiche.fiche_date BETWEEN ? AND ?";

			Calendar date1 = Calculs.getDataDate1(info);
			Calendar date2 = Calculs.getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}

		String groupement = null;
		switch (tailleUTM) {
		case 20:
			groupement = "groupe.groupe_id, utms.maille20x20";
			break;
		default:
			groupement = "groupe.groupe_id, utms.utm";
			break;
		}
		statement += " GROUP BY " + groupement;

		sommeEspeces = connection.prepareStatement(statement);
		setParams(sommeEspeces, listeParams);
		ResultSet rs = sommeEspeces.executeQuery();

		connection.close();

		return rs;
	}

	/**
	 * Calcule les donnees pour la carte somme du nombre d'especes temoignees
	 * choix possible du groupe, du sous-groupe et de la periode
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @param tailleUTM
	 *            la taille de maille UTM voulue pour les resultats
	 * @return une HashMap contenant les informations pour remplir la carte
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static HashMap<UTMS, Integer> calculeCarteSommeEspeces(Map<String, String> info, int tailleUTM)
			throws ParseException, SQLException {
		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement sommeEspeces;

		ArrayList<Object> listeParams = new ArrayList<Object>();
		String statement = "";

		switch (tailleUTM) {
		case 20:
			statement += "SELECT utms.maille20x20";
			break;
		default:
			statement += "SELECT utms.utm";
			break;
		}
		statement += ", COUNT(DISTINCT espece.espece_id) as nbespeces" + " FROM espece";
		
		if ((info.get("sous_groupe") != null) && (!info.get("sous_groupe").equals(""))) {
			statement += " INNER JOIN espece_is_in_groupement_local ON (espece.espece_id = espece_is_in_groupement_local.espece_espece_id)"
				+ " INNER JOIN groupe ON (espece_is_in_groupement_local.groupe_groupe_id = groupe.groupe_id)";
		} else if ((info.get("groupe") != null) && (!info.get("groupe").equals(""))) {
			statement += " INNER JOIN espece_is_in_groupement_local ON (espece.espece_id = espece_is_in_groupement_local.espece_espece_id)"
				+ " INNER JOIN groupe ON (espece_is_in_groupement_local.groupe_groupe_id = groupe.groupe_id)";
		}
		
		statement += " INNER JOIN observation ON (espece.espece_id = observation.observation_espece_espece_id)"
				+ " INNER JOIN fiche ON (observation.observation_fiche_fiche_id = fiche.fiche_id)"
				+ " INNER JOIN utms ON (fiche.fiche_utm_utm = utms.utm)" + " WHERE observation.observation_validee = 1";

		if ((info.get("sous_groupe") != null) && (!info.get("sous_groupe").equals(""))) {
			statement += " AND groupe.groupe_id = ?";
			listeParams.add(info.get("sous_groupe"));
		} else if ((info.get("groupe") != null) && (!info.get("groupe").equals(""))) {
			statement += " AND groupe.groupe_pere_groupe_id = ?";
			listeParams.add(info.get("groupe"));
		}

		if (!info.get("periode").equals("all")) {
			statement += " AND fiche.fiche_date BETWEEN ? AND ?";

			Calendar date1 = Calculs.getDataDate1(info);
			Calendar date2 = Calculs.getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}

		String groupement = null;
		switch (tailleUTM) {
		case 20:
			groupement = "utms.maille20x20";
			break;
		default:
			groupement = "utms.utm";
			break;
		}
		statement += " GROUP BY " + groupement;

		sommeEspeces = connection.prepareStatement(statement);
		setParams(sommeEspeces, listeParams);
		ResultSet rs = sommeEspeces.executeQuery();

		HashMap<UTMS, Integer> carteData = new HashMap<>();
		while (rs.next()) {
			String maille = "";
			UTMS mailleUTM = new UTMS();
			switch (tailleUTM) {
			case 20:
				maille = rs.getString("utms.maille20x20");
				mailleUTM.maille20x20 = maille;
				break;
			default:
				maille = rs.getString("utms.utm");
				mailleUTM.utm = maille;
				break;
			}
			Integer nbEspeces = rs.getInt("nbespeces");
			carteData.put(mailleUTM, nbEspeces);
		}

		connection.close();

		return carteData;
	}

	/**
	 * Calcule la liste des temoins ayant depose un temoignange choix possible
	 * de la periode
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @return un ResultSet
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static ResultSet calculeListeDesTemoins(Map<String, String> info) throws ParseException, SQLException {
		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement listeDesTemoins;

		ArrayList<Object> listeParams = new ArrayList<Object>();
		String statement = "SELECT m.membre_nom as membre_nom, count(obs.observation_id) as cpt"
				+ " FROM observation obs" + " INNER JOIN fiche f ON obs.observation_fiche_fiche_id = f.fiche_id"
				+ " INNER JOIN fiche_has_membre fhm ON fhm.fiche_fiche_id = f.fiche_id"
				+ " INNER JOIN membre m ON fhm.membre_membre_id = m.membre_id" + " WHERE obs.observation_validee = 1";
		if (!info.get("periode").equals("all")) {
			statement += " AND f.fiche_date BETWEEN ? AND ?";

			Calendar date1 = Calculs.getDataDate1(info);
			Calendar date2 = Calculs.getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}
		statement += " GROUP BY m.membre_nom ";

		listeDesTemoins = connection.prepareStatement(statement);
		setParams(listeDesTemoins, listeParams);
		ResultSet rs = listeDesTemoins.executeQuery();

		connection.close();

		return rs;
	}

	/**
	 * Calcule la liste des especes observees choix possible de la periode
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @return un ResultSet
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static ResultSet calculeListeDesEspeces(Map<String, String> info) throws ParseException, SQLException {

		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement listeDesEspeces;

		ArrayList<Object> listeParams = new ArrayList<Object>();

		String statement = "" + "SELECT e.espece_nom as espece_nom, count(DISTINCT f.fiche_utm_utm) as cpt FROM observation obs "
				+ "INNER JOIN fiche f ON obs.observation_fiche_fiche_id = f.fiche_id "
				+ "INNER JOIN espece e ON obs.observation_espece_espece_id = e.espece_id "
				+ "WHERE obs.observation_validee = 1";
		if (!info.get("periode").equals("all")) {
			statement += " AND f.fiche_date BETWEEN ? AND ?";

			Calendar date1 = Calculs.getDataDate1(info);
			Calendar date2 = Calculs.getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}
		statement += " GROUP BY e.espece_nom";

		listeDesEspeces = connection.prepareStatement(statement);
		setParams(listeDesEspeces, listeParams);

		ResultSet rs = listeDesEspeces.executeQuery();

		connection.close();

		return rs;
	}

	/**
	 * Calcule la liste des especes observees par maille choix possible de la
	 * periode
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @return un ResultSet
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static ResultSet calculeEspecesParMaille(Map<String, String> info) throws ParseException, SQLException {

		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement especesParMaille;

		ArrayList<Object> listeParams = new ArrayList<Object>();
		String statement = "" + "SELECT f.fiche_utm_utm, e.espece_nom, count(e.espece_nom) FROM observation obs "
				+ "INNER JOIN fiche f ON obs.observation_fiche_fiche_id = f.fiche_id "
				+ "INNER JOIN espece e ON obs.observation_espece_espece_id = e.espece_id "
				+ "WHERE obs.observation_validee = 1";
		if (!info.get("periode").equals("all")) {
			statement += " AND f.fiche_date BETWEEN ? AND ?";

			Calendar date1 = Calculs.getDataDate1(info);
			Calendar date2 = Calculs.getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}
		statement += " GROUP BY f.fiche_utm_utm, e.espece_nom";

		especesParMaille = connection.prepareStatement(statement);
		setParams(especesParMaille, listeParams);
		ResultSet rs = especesParMaille.executeQuery();

		connection.close();

		return rs;

	}

	/**
	 * Calcule la liste des observations d'un temoin choix possible de la
	 * periode
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @return un ResultSet
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static ResultSet calculeListeDesObservations(Map<String, String> info) throws ParseException, SQLException {

		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement carteObs;

		ArrayList<Object> listeParams = new ArrayList<Object>();
		String statement = "";

		statement += "SELECT DISTINCT utms.utm, espece.espece_nom" + " FROM espece_is_in_groupement_local"
				+ " INNER JOIN espece ON (espece_is_in_groupement_local.espece_espece_id = espece.espece_id)"
				+ " INNER JOIN observation ON (espece.espece_id = observation.observation_espece_espece_id)"
				+ " INNER JOIN fiche ON (observation.observation_fiche_fiche_id = fiche.fiche_id)"
				+ " INNER JOIN utms ON (fiche.fiche_utm_utm = utms.utm)"
				+ " INNER JOIN fiche_has_membre ON (fiche.fiche_id = fiche_has_membre.fiche_fiche_id)"
				+ " INNER JOIN membre ON (fiche_has_membre.membre_membre_id = membre.membre_id)"
				+ " WHERE observation.observation_validee = 1" + " AND membre.membre_nom = ?";

		listeParams.add(info.get("temoin"));

		if (!info.get("periode").equals("all")) {
			statement += " AND fiche.fiche_date BETWEEN ? AND ?";

			Calendar date1 = getDataDate1(info);
			Calendar date2 = getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}

		statement += " ORDER BY utms.utm";

		carteObs = connection.prepareStatement(statement);
		setParams(carteObs, listeParams);
		ResultSet rs = carteObs.executeQuery();

		connection.close();

		return rs;
	}

	/**
	 * Calcule les donnees pour la carte des observations d'un temoin choix
	 * possible de la periode
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @return une HashMap contenant les informations pour remplir la carte
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static HashMap<UTMS, Integer> calculeCarteDesObservations(Map<String, String> info)
			throws ParseException, SQLException {

		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement carteObs;

		ArrayList<Object> listeParams = new ArrayList<Object>();
		String statement = "";

		statement += "SELECT utms.utm, COUNT(DISTINCT espece.espece_id) as nbespeces" + " FROM espece_is_in_groupement_local"
				+ " INNER JOIN espece ON (espece_is_in_groupement_local.espece_espece_id = espece.espece_id)"
				+ " INNER JOIN observation ON (espece.espece_id = observation.observation_espece_espece_id)"
				+ " INNER JOIN fiche ON (observation.observation_fiche_fiche_id = fiche.fiche_id)"
				+ " INNER JOIN utms ON (fiche.fiche_utm_utm = utms.utm)"
				+ " INNER JOIN fiche_has_membre ON (fiche.fiche_id = fiche_has_membre.fiche_fiche_id)"
				+ " INNER JOIN membre ON (fiche_has_membre.membre_membre_id = membre.membre_id)"
				+ " WHERE observation.observation_validee = 1" + " AND membre.membre_nom = ?";

		listeParams.add(info.get("temoin"));

		if (!info.get("periode").equals("all")) {
			statement += " AND fiche.fiche_date BETWEEN ? AND ?";

			Calendar date1 = getDataDate1(info);
			Calendar date2 = getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}

		statement += " GROUP BY utms.utm";

		carteObs = connection.prepareStatement(statement);
		setParams(carteObs, listeParams);
		ResultSet rs = carteObs.executeQuery();

		HashMap<UTMS, Integer> carteData = new HashMap<>();
		while (rs.next()) {
			String maille = rs.getString("utms.utm");
			UTMS mailleUTM = new UTMS();
			mailleUTM.utm = maille;
			Integer nbEspeces = rs.getInt("nbespeces");
			carteData.put(mailleUTM, nbEspeces);
		}

		connection.close();

		return carteData;

	}

	/**
	 * Calcule les donnees pour l'histogramme de phenologie choix possible de la
	 * periode, de l'espece, du groupe ou du sous-groupe
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @return une Map contenant les informations pour l'histogramme
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static Map<String, Integer> calculePhenologie(Map<String, String> info)
			throws ParseException, SQLException {

		DataSource ds = DB.getDataSource();
		ArrayList<Object> listeParams = new ArrayList<Object>();

		Connection connection = ds.getConnection();
		String statement = "" + "SELECT fiche.fiche_date,observation.observation_id, espece.espece_nom FROM espece"
				+ " INNER JOIN observation ON observation.observation_espece_espece_id = espece.espece_id"
				+ " INNER JOIN fiche ON observation.observation_fiche_fiche_id = fiche.fiche_id";

		if ((info.get("espece") != null) && (!info.get("espece").equals(""))) {
			statement += " WHERE observation.observation_validee = 1" + " AND espece.espece_id=?";
			listeParams.add(info.get("espece"));
		} else if ((info.get("sous_groupe") != null) && (!info.get("sous_groupe").equals(""))) {
			statement += " INNER JOIN espece_is_in_groupement_local ON (espece_is_in_groupement_local.espece_espece_id = espece.espece_id)"
					+ " INNER JOIN groupe ON (groupe.groupe_id = espece_is_in_groupement_local.groupe_groupe_id)"
					+ " WHERE observation.observation_validee = 1" + " AND groupe.groupe_id = ?";
			listeParams.add(info.get("sous_groupe"));
		} else if ((info.get("groupe") != null) && (!info.get("groupe").equals(""))) {
			statement += " INNER JOIN espece_is_in_groupement_local ON (espece_is_in_groupement_local.espece_espece_id = espece.espece_id)"
					+ " INNER JOIN groupe ON (groupe.groupe_id = espece_is_in_groupement_local.groupe_groupe_id)"
					+ " WHERE observation.observation_validee = 1" + " AND groupe.groupe_pere_groupe_id = ?";
			listeParams.add(info.get("groupe"));
		} else {
			statement += " WHERE observation.observation_validee = 1";
		}

		if (!info.get("periode").equals("all")) {
			statement += " AND fiche.fiche_date BETWEEN ? AND ?";

			Calendar date1 = getDataDate1(info);
			Calendar date2 = getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}

		statement += " ORDER BY fiche.fiche_date";

		PreparedStatement phenologie = connection.prepareStatement(statement);
		setParams(phenologie, listeParams);
		ResultSet rs = phenologie.executeQuery();

		Map<String, Integer> histogramme = new HashMap<>();

		SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
		// on initialise deux objets a la date actuelle (modification plus tard)
		Calendar dateDebutPeriode = Calendar.getInstance();
		Calendar dateFinPeriode = Calendar.getInstance();

		boolean periode1 = true; // permet de savoir si on est dans la premiere
									// iteration du while suivant
		int nbTem = 0;
		String legende = "";
		while (rs.next()) {
			Date date = rs.getDate("fiche.fiche_date");
			if (periode1) {
				// on modifie dateDebutPeriode et dateFinPeriode
				if (!info.get("periode").equals("all")) {
					// si une periode est renseignee par l'utilisateur,
					// dateDebutPeriode correspond a la date de debut demandee
					// par l'utilisateur
					dateDebutPeriode.setTime(getDataDate1(info).getTime());
					dateFinPeriode.setTime(getDataDate1(info).getTime());
					dateFinPeriode.add(Calendar.DATE, 10);
				} else {
					// si aucune periode n'est renseignee par l'utilisateur, il
					// faut prendre la date du 1er temoignage
					dateDebutPeriode.setTime(date);
					dateFinPeriode.setTime(date);
					dateFinPeriode.add(Calendar.DATE, 10);
				}
				legende = date_format.format(dateDebutPeriode.getTime()) + " - "
						+ date_format.format(dateFinPeriode.getTime());
				nbTem = 0;
				histogramme.put(legende, nbTem);
				periode1 = false;
			}
			// disjonction de cas selon que la date du temoignage est apres ou
			// avant dateFinPeriode
			if (!date.after(dateFinPeriode.getTime())) {
				nbTem++;
			} else {
				// on met a jour dateDebutPeriode et dateFinPeriode
				dateFinPeriode.add(Calendar.DATE, 1);
				dateDebutPeriode.setTime(dateFinPeriode.getTime());
				dateFinPeriode.setTime(dateDebutPeriode.getTime());
				dateFinPeriode.add(Calendar.DATE, 10);
				// on met a� jour la legende
				legende = date_format.format(dateDebutPeriode.getTime()) + " - "
						+ date_format.format(dateFinPeriode.getTime());
				// on reinitialise le nombre de temoignages
				nbTem = 0;
			}
			histogramme.put(legende, nbTem);
		}

		connection.close();

		return histogramme;

	}

	/**
	 * Calcule la liste des especes observees par commune choix possible de la
	 * periode
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @return un ResultSet
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static ResultSet calculeEspecesParCommune(Map<String, String> info) throws ParseException, SQLException {

		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement especesParCommune;

		ArrayList<Object> listeParams = new ArrayList<Object>();

		String statement = "" + "SELECT c.ville_nom , e.espece_nom, count(e.espece_nom) FROM observation obs "
				+ "INNER JOIN fiche f ON obs.observation_fiche_fiche_id = f.fiche_id "
				+ "INNER JOIN espece e ON obs.observation_espece_espece_id = e.espece_id "
				+ "INNER JOIN commune c ON f.fiche_commune_ville_id = c.ville_id "
				+ "WHERE obs.observation_validee = 1";
		if (!info.get("periode").equals("all")) {
			statement += " AND f.fiche_date BETWEEN ? AND ?";

			Calendar date1 = Calculs.getDataDate1(info);
			Calendar date2 = Calculs.getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}
		statement += " GROUP BY c.ville_nom, e.espece_nom";

		especesParCommune = connection.prepareStatement(statement);
		setParams(especesParCommune, listeParams);

		ResultSet rs = especesParCommune.executeQuery();

		connection.close();

		return rs;

	}

	/**
	 * Calcule la liste des especes observees par departement choix possible de
	 * la periode
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @return un ResultSet
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static ResultSet calculeEspecesParDepartement(Map<String, String> info)
			throws ParseException, SQLException {

		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement especesParDepartement;

		ArrayList<Object> listeParams = new ArrayList<Object>();

		String statement = "" + "SELECT d.departement_nom , e.espece_nom, count(e.espece_nom) FROM observation obs "
				+ "INNER JOIN fiche f ON obs.observation_fiche_fiche_id = f.fiche_id "
				+ "INNER JOIN espece e ON obs.observation_espece_espece_id = e.espece_id "
				+ "INNER JOIN commune c ON f.fiche_commune_ville_id = c.ville_id "
				+ "INNER JOIN departement d ON c.ville_departement_departement_code = d.departement_code "
				+ "WHERE obs.observation_validee = 1";
		if (!info.get("periode").equals("all")) {
			statement += " AND f.fiche_date BETWEEN ? AND ?";

			Calendar date1 = Calculs.getDataDate1(info);
			Calendar date2 = Calculs.getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}
		statement += " GROUP BY d.departement_nom, e.espece_nom";

		especesParDepartement = connection.prepareStatement(statement);
		setParams(especesParDepartement, listeParams);

		ResultSet rs = especesParDepartement.executeQuery();

		connection.close();

		return rs;

	}

	/**
	 * Calcule la chronologie de l'utilisateur
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @return un ResultSet
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static ResultSet calculeCarnetDeChasse(Map<String, String> info) throws SQLException {

		DataSource ds = DB.getDataSource();

		Connection connection = ds.getConnection();
		String statement = ""
				+ "SELECT f.fiche_utm_utm, obs.observation_id, e.espece_nom, i.informations_complementaires_nombre_de_specimens, s.stade_sexe_intitule "
				+ "FROM observation obs " + "INNER JOIN fiche f ON obs.observation_fiche_fiche_id = f.fiche_id "
				+ "INNER JOIN fiche_has_membre fhm ON fhm.fiche_fiche_id = f.fiche_id "
				+ "INNER JOIN membre m ON fhm.membre_membre_id = m.membre_id "
				+ "INNER JOIN espece e ON obs.observation_espece_espece_id = e.espece_id "
				+ "INNER JOIN informations_complementaires i ON obs.observation_id = i.informations_complementaires_observation_observation_id "
				+ "INNER JOIN stade_sexe s ON i.informations_complementaires_stade_sexe_stade_sexe_id = s.stade_sexe_id "
				+ "WHERE  m.membre_email = ? " + "GROUP BY f.fiche_utm_utm, obs.observation_id ";
		PreparedStatement carnetDeChasse = connection.prepareStatement(statement);
		carnetDeChasse.setString(1, session("username"));

		ResultSet rs = carnetDeChasse.executeQuery();

		connection.close();

		return rs;

	}

	/**
	 * Calcule les donnees pour l'histogramme historique
	 * 
	 * @param info
	 *            les informations rentrees par l'utilisateur
	 * @return une Map contenant les informations pour l'histogramme
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static Map<String, Integer> calculeHistorique(Map<String, String> info)
			throws SQLException, ParseException {
		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement historique;

		String statement = "";
		statement = "SELECT fiche.fiche_date, observation.observation_id" + " FROM observation"
				+ " INNER JOIN fiche ON observation.observation_fiche_fiche_id = fiche.fiche_id"
				+ " WHERE observation.observation_validee = 1" + " ORDER BY fiche.fiche_date";

		historique = connection.prepareStatement(statement);
		ResultSet rs = historique.executeQuery();

		// on stocke les annees de chaque temoignage
		ArrayList<String> yearTemoignages = new ArrayList<>();
		while (rs.next()) {
			String date = rs.getString("fiche.fiche_date");
			if (!date.equals(null)) {
				char[] dateCharArray = date.toCharArray();
				String yearString = "";
				// la date est au format yyyy-...
				yearString += dateCharArray[0];
				yearString += dateCharArray[1];
				yearString += dateCharArray[2];
				yearString += dateCharArray[3];
				yearTemoignages.add(yearString);
			}
		}

		int nbTem = yearTemoignages.size();
		int yearMin = Integer.parseInt(yearTemoignages.get(0));
		int yearMax = Integer.parseInt(yearTemoignages.get(nbTem - 1));

		// calcul du nombre de barres de l'histogramme
		// en fonction des annees min et max
		int nbBarresHisto = 0;
		if ((yearMax - yearMin) % 20 == 0) {
			nbBarresHisto = (yearMax - yearMin) / 20;
		} else {
			nbBarresHisto = ((yearMax - yearMin) / 20 + 1);
		}

		// on remplit le tableau correspondant aux valeurs de l'histogramme
		int[] histogrammeData = new int[nbBarresHisto];
		int year;
		for (String str : yearTemoignages) {
			year = Integer.parseInt(str);
			histogrammeData[(year - yearMin) / 20]++;
		}

		// on edite les legendes et on stocke legendes et valeurs dans une Map
		Map<String, Integer> histogramme = new HashMap<>();
		int yearTempMin;
		int yearTempMax;
		for (int i = 0; i < nbBarresHisto; i++) {
			yearTempMin = yearMin + i * 20;
			yearTempMax = yearMin + i * 20 + 19;
			String legende = "" + yearTempMin + "-" + yearTempMax;
			histogramme.put(legende, histogrammeData[i]);
		}

		connection.close();

		return histogramme;
	}

	/**
	 * Etablit le message a afficher
	 * 
	 * @param titre
	 *            le titre du message
	 * @param info
	 *            la liste des informations de construction du fichier Excel
	 * @return le message a afficher
	 */
	private static String buildMessage(String titre, Map<String, String> info) {
		StringBuilder temp = new StringBuilder();
		if (info.get("periode").equals("all")) {
			temp.append(titre);
		} else {
			temp.append(titre);
			temp.append(" du ");
			temp.append(info.get("jour1"));
			temp.append("/");
			temp.append(info.get("mois1"));
			temp.append("/");
			temp.append(info.get("annee1"));
			temp.append(" au ");
			temp.append(info.get("jour2"));
			temp.append("/");
			temp.append(info.get("mois2"));
			temp.append("/");
			temp.append(info.get("annee2"));
		}

		return temp.toString();
	}

	/**
	 * Exporte les donnees dans un fichier Excel, et eventuellement dans une
	 * carte
	 * 
	 * @return
	 */
	public static Result exportDonnees() throws ParseException, IOException, SQLException {
		Map<String, String> info = getData();

		Excel excelData = null;
		HashMap<UTMS, Integer> carteData = null;
		Carte carteImage = null;
		String message = new String();
		if ((info.get("typeDonnees") != null) && (!info.get("typeDonnees").equals("null"))) {

			int typeStat = Integer.parseInt(info.get("typeDonnees"));
			StringBuilder temp = new StringBuilder();

			switch (typeStat) {
			case 10: // Carte par espece
				// Pour une periode donnee, et par espece, liste des premiers
				// temoignages
				// de chaque maille (maille, index, date, temoin(s)) avec carte
				// du nombre
				// de temoignages par mailles
				ResultSet listeTemoignages = calculeListeDesTemoignages(info, 10);
				excelData = ListeExportExcel.listeDesTemoignages(info, listeTemoignages, 10);
				carteData = calculeCarteTemoignages(info, 10);
				carteImage = new Carte(carteData, 10);
				carteImage.writeToDisk();
				message = buildMessage("Carte par especes", info);

				break;

			case 20: // Carte 20x20 par espece
				// Pour une periode donnee, et par espece, liste des premiers
				// temoignages
				// de chaque maille UTM 20km X 20km (maille, index, date,
				// temoin(s)) avec
				// carte du nombre de temoignages par mailles
				ResultSet listeTemoignagesUTM20 = calculeListeDesTemoignages(info, 20);
				excelData = ListeExportExcel.listeDesTemoignages(info, listeTemoignagesUTM20, 20);
				carteData = calculeCarteTemoignages(info, 20);
				carteImage = new Carte(carteData, 20);
				carteImage.writeToDisk();
				message = buildMessage("Carte 20x20 par especes", info);

				break;

			case 30: // Carte somme
				// Pour une periode donnee, et pour toutes les especes du groupe
				// ou du
				// sous-groupe choisi, carte du nombre d'especes temoignees par
				// maille
				ResultSet sommeEspeces = calculeSommeEspeces(info, 10);
				excelData = ListeExportExcel.sommeEspeces(info, sommeEspeces, 10);
				carteData = calculeCarteSommeEspeces(info, 10);
				carteImage = new Carte(carteData, 10);
				carteImage.writeToDisk();
				message = buildMessage("Carte somme", info);

				break;

			case 40: // Carte somme 20x20
				// Pour une periode donnee, et pour toutes les especes du groupe
				// ou du
				// sous-groupe choisi, carte du nombre d'especes temoignees par
				// maille
				// UTM 20km X 20km
				ResultSet sommeEspecesUTM20 = calculeSommeEspeces(info, 20);
				excelData = ListeExportExcel.sommeEspeces(info, sommeEspecesUTM20, 20);
				carteData = calculeCarteSommeEspeces(info, 20);
				carteImage = new Carte(carteData, 20);
				carteImage.writeToDisk();
				message = buildMessage("Carte somme 20x20", info);
				break;

			case 50: // Liste des temoins
				// Liste alphabetique des temoins pour une periode donnee
				ResultSet listeTemoins = calculeListeDesTemoins(info);
				excelData = ListeExportExcel.listeDesTemoins(info, listeTemoins);
				message = buildMessage("Liste des temoins", info);
				break;

			case 60: // Liste des especes
				// Liste des especes par ordre systematique pour une periode
				// donnee avec
				// le nombre de mailles renseignees
				ResultSet listeEspeces = calculeListeDesEspeces(info);
				excelData = ListeExportExcel.listeDesEspeces(info, listeEspeces);
				message = buildMessage("Liste des especes", info);
				break;

			case 70: // Especes par maille(s)
				// Pour une periode donnee liste maille par maille des especes
				// renseignees
				// avec le nombre des temoignages de ces especes
				ResultSet especesParMaille = calculeEspecesParMaille(info);
				excelData = ListeExportExcel.listeEspecesParMaille(info, especesParMaille);
				message = buildMessage("Especes par maille", info);
				break;

			case 80: // Especes par commune
				// Pour une periode donnee liste par commune des especes
				// renseignees avec
				// le nombre des temoignages de ces especes
				ResultSet especesParCommune = calculeEspecesParCommune(info);
				excelData = ListeExportExcel.listeEspecesParCommune(info, especesParCommune);
				message = buildMessage("Especes par commune", info);
				break;

			case 90: // Especes par departement
				// Pour une periode donnee liste par departement des especes
				// renseignees
				// avec le nombre des temoignages de ces especes</td>
				ResultSet especesParDepartement = calculeEspecesParDepartement(info);
				excelData = ListeExportExcel.listeEspecesParDepartement(info, especesParDepartement);
				message = buildMessage("Especes par departement", info);
				break;

			case 100: // Phenologie
				// Pour une periode donnee, et par espece, histogramme par
				// decades
				// (mois divises en trois) du nombre de temoignages (quels que
				// soient
				// le nombre d'individus)
				Map<String, Integer> histogrammePhenologie = calculePhenologie(info);
				excelData = HistogrammeExportExcel.phenologie(info, histogrammePhenologie);
				message = buildMessage("Phenologie", info);
				break;

			case 110: // Carnet de Chasse
				// liste chronologique des differents lieux prospectes et, dans
				// ces lieux,
				// des differentes especes observees avec detail des nombres et
				// stade/sexe
				ResultSet carnetDeChasse = calculeCarnetDeChasse(info);
				excelData = ListeExportExcel.carnetDeChasse(info, carnetDeChasse);
				message = buildMessage("Carnet de chasse de " + info.get("temoin"), info);
				break;

			case 120: // Carte des observations
				// Pour un temoin donne, carte du nombre d'especes differentes
				// par
				// mailles prospectees
				ResultSet listeObservations = calculeListeDesObservations(info);
				excelData = ListeExportExcel.listeDesObservations(info, listeObservations);
				carteData = calculeCarteDesObservations(info);
				carteImage = new Carte(carteData, 0);
				carteImage.writeToDisk();
				message = buildMessage("Carnet des observations de " + info.get("temoin"), info);
				break;

			case 130: // Historique
				// Graphique par periode de 20 ans du nombre de temoignages
				Map<String, Integer> histogrammeHistorique = calculeHistorique(info);
				excelData = HistogrammeExportExcel.historique(histogrammeHistorique);
				message = buildMessage("Historique", info);
				break;
			}
		}
		if (excelData != null) {
			excelData.writeToDisk();
			return ok(exportExcel.render(message, excelData.getFileName()));
		} else {
			return ok(emptyExcel.render());
		}
	}

	/**
	 * Recupere les parametres du formulaire et les charges dans une Map
	 * 
	 * @return
	 */
	public static Map<String, String> getData() {
		DynamicForm df = DynamicForm.form().bindFromRequest();
		Map<String, String> info = new HashMap<String, String>();

		if ((df.get("groupe") != null) && (!df.get("groupe").equals("null")) && (!df.get("groupe").equals("0")))
			info.put("groupe", df.get("groupe"));
		else
			info.put("groupe", "");
		if ((df.get("sous_groupe") != null) && (!df.get("sous_groupe").equals("null"))
				&& (!df.get("sous_groupe").equals("0")))
			info.put("sous_groupe", df.get("sous_groupe"));
		else
			info.put("sous_groupe", "");
		if ((df.get("espece") != null) && (!df.get("espece").equals("null")) && (!df.get("espece").equals("0")))
			info.put("espece", df.get("espece"));
		else
			info.put("espece", "");

		if ((df.get("stade") != null) && (!df.get("stade").equals("null")))
			info.put("stade", df.get("stade"));
		else
			info.put("stade", "");

		info.put("maille", df.get("maille"));
		info.put("mailles", df.get("mailles"));

		if ((df.get("temoin") != null) && (!df.get("temoin").equals("null")))
			info.put("temoin", df.get("temoin"));
		else
			info.put("temoin", "");

		if (df.get("periode").equals("autre")) {
			info.put("periode", df.get("periode"));
			info.put("jour1", df.get("jour1"));
			info.put("mois1", df.get("mois1"));
			info.put("annee1", df.get("annee1"));
			info.put("jour2", df.get("jour2"));
			info.put("mois2", df.get("mois2"));
			info.put("annee2", df.get("annee2"));
		} else {
			info.put("periode", "all");
		}
		info.put("typeDonnees", df.get("typeDonnees"));
		if (info.get("typeDonnees").equals("")) {
			info.put("typeDonnees", "10");
		}
		return info;
	}

	/**
	 * Renvoie la date 1 de le Map sous forme de Calendar
	 * 
	 * @param info
	 * @return
	 * @throws ParseException
	 */
	public static Calendar getDataDate1(Map<String, String> info) throws ParseException {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
		c.setTime(date_format.parse(info.get("jour1") + "/" + info.get("mois1") + "/" + info.get("annee1")));
		return c;
	}

	/**
	 * Renvoie la date 2 de le Map sous forme de Calendar
	 * 
	 * @param info
	 * @return
	 * @throws ParseException
	 */
	public static Calendar getDataDate2(Map<String, String> info) throws ParseException {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
		c.setTime(date_format.parse(info.get("jour2") + "/" + info.get("mois2") + "/" + info.get("annee2")));
		return c;
	}
}
