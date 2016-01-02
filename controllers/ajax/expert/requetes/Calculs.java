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
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.Row;

import java.sql.Connection;
import java.sql.PreparedStatement;


import controllers.ajax.expert.requetes.nvCalculs.ListeDesTemoins;
import controllers.ajax.expert.requetes.nvCalculs.SommeEspeces;
import controllers.ajax.expert.requetes.nvCalculs.ListeDesEspeces;
import controllers.ajax.expert.requetes.calculs.CarteSomme;
import controllers.ajax.expert.requetes.nvCalculs.CarnetDeChasse;
import controllers.ajax.expert.requetes.nvCalculs.EspecesParMaille;
import controllers.ajax.expert.requetes.nvCalculs.EspecesParCommune;
import controllers.ajax.expert.requetes.nvCalculs.EspecesParDepartement;
import controllers.ajax.expert.requetes.nvCalculs.Historique;
import functions.cartes.Carte;
import functions.excels.Excel;
import functions.excels.ListeExportExcel;

import functions.excels.exports.HistoriqueDesEspecesExcel;
import functions.excels.exports.MaillesParEspeceExcel;
import functions.excels.exports.CarteSommeBiodiversiteExcel;
import functions.excels.exports.CarteSommeExcel;
import functions.excels.exports.ChronologieDUnTemoinExcel;
import functions.excels.exports.HistogrammeDesImagosExcel;
import functions.excels.exports.MaillesParPeriodeExcel;
import functions.excels.exports.TemoinsParPeriodeExcel;
import functions.excels.exports.ListeDesTemoinsExcel;
import functions.excels.exports.ListeDesTemoignagesExcel;
import functions.excels.exports.ListeDesEspecesExcel;
import functions.excels.exports.CarnetDeChasseExcel;
import functions.excels.exports.EspecesParMailleExcel;
import functions.excels.exports.EspecesParCommuneExcel;
import functions.excels.exports.EspecesParDepartementExcel;
import functions.excels.exports.HistoriqueExcel;


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
	 * Calcule la liste des témoignages par espèces sur une période
	 * @return
	 */
	private static ResultSet calculeListeDesTemoignages(Map<String,String> info, int tailleUTM) throws ParseException, SQLException {
		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement listeDesTemoignages;

		ArrayList<Object> listeParams = new ArrayList<Object>();
		String statement = "";
		statement = "SELECT espece.espece_nom as espece_nom, membre.membre_nom as membre_nom, ";
		// utilisation d'un switch au cas où on voudrait utiliser uniquement les mailles 50x50, ou 100x100
		switch (tailleUTM) {
			case 20:
				statement += "utms.maille20x20, f.fiche_Date";
				break;
			default:
				statement += "utms.utm, utms.maille20x20, utms.maille50x50, utms.maille100x100, f.fiche_Date";
				break;	
		}
		statement += " FROM observation obs"
				+ " INNER JOIN fiche f ON obs.observation_fiche_fiche_id = f.fiche_id"
				+ " INNER JOIN espece ON obs.observation_espece_espece_id = espece.espece_id"
				+ " INNER JOIN fiche_has_membre fhm ON fhm.fiche_fiche_id = f.fiche_id"
				+ " INNER JOIN membre ON fhm.membre_membre_id = membre.membre_id";
		
		if ((info.get("espece") != null) && (! info.get("espece").equals(""))) {
			statement += " INNER JOIN utms ON utms.utm = f.fiche_utm_utm"
				+ " WHERE obs.observation_validee = 1"
				+ " AND espece.espece_id=?";
			listeParams.add(info.get("espece"));			
		} else if ((info.get("sous_groupe") != null) && (! info.get("sous_groupe").equals(""))) {
			statement += " INNER JOIN espece_is_in_groupement_local ON espece.espece_id = espece_is_in_groupement_local.espece_espece_id"
				+ " INNER JOIN groupe ON espece_is_in_groupement_local.groupe_groupe_id = groupe.groupe_id"
				+ " INNER JOIN utms ON utms.utm = f.fiche_utm_utm"
				+ " WHERE obs.observation_validee = 1"
				+ " AND groupe.groupe_id=?";
			listeParams.add(info.get("sous_groupe"));
		} else if ((info.get("groupe") != null) && (! info.get("groupe").equals(""))) {
			statement += " INNER JOIN espece_is_in_groupement_local ON espece.espece_id = espece_is_in_groupement_local.espece_espece_id"
				+ " INNER JOIN groupe ON espece_is_in_groupement_local.groupe_groupe_id = groupe.groupe_id"
				+ " INNER JOIN utms ON utms.utm = f.fiche_utm_utm"
				+ " WHERE obs.observation_validee = 1"
				+ " AND groupe.groupe_id=?";
			listeParams.add(info.get("groupe"));
		} else {
			statement += " INNER JOIN utms ON utms.utm = f.fiche_utm_utm"
				+ " WHERE obs.observation_validee = 1";
		}

		if (! info.get("periode").equals("all")) {
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
		statement += " ORDER BY "+ordre;
		info.put("maxtemoignages", "20");

		listeDesTemoignages = connection.prepareStatement(statement); 
		setParams(listeDesTemoignages, listeParams);
		ResultSet rs = listeDesTemoignages.executeQuery();
		
		return rs;
	}
	
	private static HashMap<UTMS,Integer> calculeCarteTemoignages(Map<String,String> info, int tailleUTM) throws ParseException, SQLException {
		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement nbDeTemoignages;

		ArrayList<Object> listeParams = new ArrayList<Object>();
		String statement = "";
		// utilisation d'un switch au cas où on voudrait utiliser uniquement les mailles 50x50, ou 100x100
		switch (tailleUTM) {
			case 20:
				statement += "SELECT utms.maille20x20, COUNT(f.fiche_Date) AS nbtem";
				break;
			default:
				statement += "SELECT utms.utm, COUNT(f.fiche_Date) AS nbtem";
				break;	
		}
		statement += " FROM observation obs"
				+ " INNER JOIN fiche f ON obs.observation_fiche_fiche_id = f.fiche_id"
				+ " INNER JOIN espece ON obs.observation_espece_espece_id = espece.espece_id"
				+ " INNER JOIN fiche_has_membre fhm ON fhm.fiche_fiche_id = f.fiche_id"
				+ " INNER JOIN membre ON fhm.membre_membre_id = membre.membre_id";
		
		if ((info.get("espece") != null) && (! info.get("espece").equals(""))) {
			statement += " INNER JOIN utms ON utms.utm = f.fiche_utm_utm"
				+ " WHERE obs.observation_validee = 1"
				+ " AND espece.espece_id=?";
			listeParams.add(info.get("espece"));			
		} else if ((info.get("sous_groupe") != null) && (! info.get("sous_groupe").equals(""))) {
			statement += " INNER JOIN espece_is_in_groupement_local ON espece.espece_id = espece_is_in_groupement_local.espece_espece_id"
				+ " INNER JOIN groupe ON espece_is_in_groupement_local.groupe_groupe_id = groupe.groupe_id"
				+ " INNER JOIN utms ON utms.utm = f.fiche_utm_utm"
				+ " WHERE obs.observation_validee = 1"
				+ " AND groupe.groupe_id=?";
			listeParams.add(info.get("sous_groupe"));
		} else if ((info.get("groupe") != null) && (! info.get("groupe").equals(""))) {
			statement += " INNER JOIN espece_is_in_groupement_local ON espece.espece_id = espece_is_in_groupement_local.espece_espece_id"
				+ " INNER JOIN groupe ON espece_is_in_groupement_local.groupe_groupe_id = groupe.groupe_id"
				+ " INNER JOIN utms ON utms.utm = f.fiche_utm_utm"
				+ " WHERE obs.observation_validee = 1"
				+ " AND groupe.groupe_id=?";
			listeParams.add(info.get("groupe"));
		} else {
			statement += " INNER JOIN utms ON utms.utm = f.fiche_utm_utm"
				+ " WHERE obs.observation_validee = 1";
		}

		if (! info.get("periode").equals("all")) {
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
		statement += " GROUP BY "+groupement;

		nbDeTemoignages = connection.prepareStatement(statement); 
		setParams(nbDeTemoignages, listeParams);
		ResultSet rs = nbDeTemoignages.executeQuery();
		
		HashMap<UTMS,Integer> carteData = new HashMap<>();
		while (rs.next()){
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
			Integer nbTemoignages = rs.getInt("nbtem");
			carteData.put(mailleUTM,nbTemoignages);
		}
		return carteData;
		
	}
	
	/**
	 * Calcule la somme des espèces témoignées par maille sur une période pour un groupe ou sous-groupe défini
	 * @param info
	 * @param tailleUTM
	 * @return
	 * @throws ParseException
	 * @throws SQLException
	 */
	private static ResultSet calculeSommeEspeces(Map<String,String> info, int tailleUTM) throws ParseException, SQLException {
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
		statement += ", COUNT(espece.espece_id) as nbespeces"
				+ " FROM groupe " 
				+ " INNER JOIN espece_is_in_groupement_local ON (groupe.groupe_id = espece_is_in_groupement_local.groupe_groupe_id)"
				+ " INNER JOIN espece ON (espece_is_in_groupement_local.espece_espece_id = espece.espece_id)"
				+ " INNER JOIN observation ON (espece.espece_id = observation.observation_espece_espece_id)"
				+ " INNER JOIN fiche ON (observation.observation_fiche_fiche_id = fiche.fiche_id)"
				+ " INNER JOIN utms ON (fiche.fiche_utm_utm = utms.utm)"
				+ " WHERE observation.observation_validee = 1";
		
		if ((info.get("sous_groupe") != null) && (! info.get("sous_groupe").equals(""))) {
			statement += " AND groupe.groupe_id = ?";
			listeParams.add(info.get("sous_groupe"));
		} else if ((info.get("groupe") != null) && (! info.get("groupe").equals(""))) {
			statement += " AND groupe.groupe_id = ?";
			listeParams.add(info.get("groupe"));
		}
		
		if (! info.get("periode").equals("all")) {
			statement += " AND fiche.fiche_date BETWEEN ? AND ?";

			Calendar date1 = Calculs.getDataDate1(info);
			Calendar date2 = Calculs.getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}
		
		String ordreETgroupement = null;
		switch (tailleUTM) {
			case 20:
				ordreETgroupement = "groupe.groupe_id, utms.maille20x20";
				break;
			default:
				ordreETgroupement = "groupe.groupe_id, utms.utm";
				break;	
		}
		statement += " GROUP BY "+ordreETgroupement;
				//" ORDER BY "+ordreETgroupement+
		
		sommeEspeces = connection.prepareStatement(statement); 
		setParams(sommeEspeces, listeParams);
		ResultSet rs = sommeEspeces.executeQuery();
		
		return rs;
	}
	
	private static HashMap<UTMS,Integer> calculeCarteSommeEspeces(Map<String,String> info, int tailleUTM) throws ParseException, SQLException {
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
		statement += ", COUNT(espece.espece_id) as nbespeces"
				+ " FROM groupe " 
				+ " INNER JOIN espece_is_in_groupement_local ON (groupe.groupe_id = espece_is_in_groupement_local.groupe_groupe_id)"
				+ " INNER JOIN espece ON (espece_is_in_groupement_local.espece_espece_id = espece.espece_id)"
				+ " INNER JOIN observation ON (espece.espece_id = observation.observation_espece_espece_id)"
				+ " INNER JOIN fiche ON (observation.observation_fiche_fiche_id = fiche.fiche_id)"
				+ " INNER JOIN utms ON (fiche.fiche_utm_utm = utms.utm)"
				+ " WHERE observation.observation_validee = 1";
		
		if ((info.get("sous_groupe") != null) && (! info.get("sous_groupe").equals(""))) {
			statement += " AND groupe.groupe_id = ?";
			listeParams.add(info.get("sous_groupe"));
		} else if ((info.get("groupe") != null) && (! info.get("groupe").equals(""))) {
			statement += " AND groupe.groupe_id = ?";
			listeParams.add(info.get("groupe"));
		}
		
		if (! info.get("periode").equals("all")) {
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
		statement += " GROUP BY "+groupement;
		
		sommeEspeces = connection.prepareStatement(statement); 
		setParams(sommeEspeces, listeParams);
		ResultSet rs = sommeEspeces.executeQuery();
		
		
		HashMap<UTMS,Integer> carteData = new HashMap<>();
		while (rs.next()){
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
			carteData.put(mailleUTM,nbEspeces);
		}
		return carteData;
	}

	/**
	 * Calcule la liste des témoins et le nombre de témoignages sur une période
	 * @return
	 */
	private static ResultSet calculeListeDesTemoins(Map<String,String> info) throws ParseException, SQLException {
		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement listeDesTemoins;

		ArrayList<Object> listeParams = new ArrayList<Object>();
		String statement = "SELECT m.membre_nom as membre_nom, count(obs.observation_id) as cpt"
			+ " FROM observation obs"
			+ " INNER JOIN fiche f ON obs.observation_fiche_fiche_id = f.fiche_id"
			+ " INNER JOIN fiche_has_membre fhm ON fhm.fiche_fiche_id = f.fiche_id"
			+ " INNER JOIN membre m ON fhm.membre_membre_id = m.membre_id"
			+ " WHERE obs.observation_validee = 1";
		if (! info.get("periode").equals("all")) {
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

		return rs;
	}
	
	/**
	 * Calcule la liste des espèces observées et le nombre de mailles renseignées
	 * @return
	 */
	private static ResultSet calculeListeDesEspeces(Map<String,String> info) throws ParseException, SQLException {
		
		Calendar date1 = Calculs.getDataDate1(info);
		Calendar date2 = Calculs.getDataDate2(info);
		  
		DataSource ds = DB.getDataSource();

		Connection connection = ds.getConnection();
		String statement = ""
				+ "SELECT e.espece_nom as espece_nom, count(f.fiche_utm_utm) as cpt FROM observation obs "
				+ "INNER JOIN fiche f ON obs.observation_fiche_fiche_id = f.fiche_id "
				+ "INNER JOIN espece e ON obs.observation_espece_espece_id = e.espece_id "
				+ "WHERE obs.observation_validee = 1 and f.fiche_date BETWEEN ? AND ? "
				+ "GROUP BY e.espece_nom ";
		PreparedStatement listeDesEspeces = connection.prepareStatement(statement); 
		listeDesEspeces.setDate(1,new java.sql.Date(date1.getTimeInMillis()));
		listeDesEspeces.setDate(2,new java.sql.Date(date2.getTimeInMillis()));	
				
		ResultSet rs = listeDesEspeces.executeQuery();
		
		return rs;
	}
	
	private static void setParams(PreparedStatement aStatement, ArrayList<Object> listeParams) throws SQLException {
		int index = 0;
		for (Object o : listeParams) {
			index++;
			if (o instanceof String) {
				aStatement.setString(index, (String)o);
			} else if (o instanceof java.sql.Date) {
				aStatement.setDate(index, (java.sql.Date)o);
			}
		}
	}
	
	
	/**
	 * Etablit le message à afficher
	 * @param titre : le titre du message
	 * @param info : la liste des informations de construction du fichier Excel
	 * @return
	 */
	private static String buildMessage(String titre, Map<String,String> info) {
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
	 * Exporte les données dans un fichier Excel
	 * @return
	 */
	public static Result exportDonnees() throws ParseException, IOException, SQLException{
		Map<String,String> info = getData();
		
		Excel excelData = null;
		HashMap<UTMS,Integer> carteData = null;
		Carte carteImage = null;
		String message = new String();
		if ((info.get("typeDonnees") != null) && (!info.get("typeDonnees").equals("null"))) {

			int typeStat = Integer.parseInt(info.get("typeDonnees"));
			StringBuilder temp = new StringBuilder();

			switch (typeStat) {
				case 10 : // Carte par espèce
					// Pour une période donnée, et par espèce, liste des premiers témoignages 
					// de chaque maille (maille, index, date, témoin(s)) avec carte du nombre 
					// de témoignages par mailles
					ResultSet listeTemoignages = calculeListeDesTemoignages(info,0);
					excelData = ListeExportExcel.listeDesTemoignages(info,listeTemoignages,0);
					carteData = calculeCarteTemoignages(info,0);
					carteImage = new Carte(carteData);
					carteImage.writeToDisk();
					message = buildMessage("Carte par espèces", info);

				break;

				case 20 : // Carte 20x20 par espèce
					// Pour une période donnée, et par espèce, liste des premiers témoignages
					// de chaque maille UTM 20km X 20km (maille, index, date, témoin(s)) avec
					// carte du nombre de témoignages par mailles
					ResultSet listeTemoignagesUTM20 = calculeListeDesTemoignages(info,20);
					excelData = ListeExportExcel.listeDesTemoignages(info,listeTemoignagesUTM20,20);
					/*
					NE FONCTIONNE PAS POUR L'INSTANT (pas de fichier carte.png représentant les mailles 20x20)
					message = buildMessage("Carte 20x20 par espèces", info);
					carteData = calculeCarteTemoignages(info,20);
					carteImage = new Carte(carteData);
					carteImage.writeToDisk();
					*/
					message = buildMessage("Carte 20x20 par espèces", info);

				break;

				case 30 : // Carte somme
					// Pour une période donnée, et pour toutes les espèces du groupe ou du 
					// sous-groupe choisi, carte du nombre d'espèces témoignées par maille
					ResultSet sommeEspeces = calculeSommeEspeces(info,0);
					excelData = ListeExportExcel.sommeEspeces(info,sommeEspeces,0);
					carteData = calculeCarteSommeEspeces(info,0);
					carteImage = new Carte(carteData);
					carteImage.writeToDisk();
					message = buildMessage("Carte somme", info);
					
				break;

				case 40 : // Carte somme 20x20
					// Pour une période donnée, et pour toutes les espèces du groupe ou du 
					// sous-groupe choisi, carte du nombre d'espèces témoignées par maille 
					// UTM 20km X 20km
					ResultSet sommeEspecesUTM20 = calculeSommeEspeces(info,20);
					excelData = ListeExportExcel.sommeEspeces(info,sommeEspecesUTM20,20);
					/*
					NE FONCTIONNE PAS POUR L'INSTANT (pas de fichier carte.png représentant les mailles 20x20)
					carteData = calculeCarteSommeEspeces(info,20);
					carteImage = new Carte(carteData);
					carteImage.writeToDisk();
					*/
					message = buildMessage("Carte somme 20x20", info);
				break;

				case 50 : // Liste des témoins
					// Liste alphabétique des témoins pour une période donnée
					ResultSet listeTemoins = calculeListeDesTemoins(info);
					excelData = ListeExportExcel.listeDesTemoins(info,listeTemoins);
					message = buildMessage("Liste des témoins", info);
					break;

				case 60 : // Liste des espèces
					// Liste des espèces par ordre systématique pour une période donnée avec 
					// le nombre de mailles renseignées
					ResultSet listeEspeces = calculeListeDesEspeces(info);
					excelData = ListeExportExcel.listeDesEspeces(info,listeEspeces);
					message = buildMessage("Liste des espèces", info);
					break;

				case 70 : // Espèces par maille(s)
					// Pour une période donnée liste maille par maille des espèces renseignées 
					// avec le nombre des témoignages de ces espèces
					ResultSet especesParMaille = EspecesParMaille.calculeEspecesParMaille(info);
					excelData = new EspecesParMailleExcel(info,especesParMaille);
					message = buildMessage("Espèces par maille", info);
				break;

				case 80 : // Espèces par commune
					// Pour une période donnée liste par commune des espèces renseignées avec 
					// le nombre des témoignages de ces espèces
					ResultSet especesParCommune = EspecesParCommune.calculeEspecesParCommune(info);
					excelData = new EspecesParCommuneExcel(info,especesParCommune);
					message = buildMessage("Espèces par commune", info);
					break;

				case 90 : // Espèces par département
					// Pour une période donnée liste par département des espèces renseignées 
					// avec le nombre des témoignages de ces espèces</td>
					ResultSet especesParDepartement = EspecesParDepartement.calculeEspecesParDepartement(info);
					excelData = new EspecesParDepartementExcel(info,especesParDepartement);
					message = buildMessage("Espèces par département", info);
					break;

				case 100 : // Phénologie
					// Pour une période donnée, et par espèce, histogramme par décades 
					// (mois divisé en trois) du nombre de témoignages (quels que soient 
					// le nombre d'individus)
				break;

				case 110 : // Carnet de Chasse
					// liste chronologique des différents lieux prospectés et, dans ces lieux, 
					// des différentes espèces observées avec détail des nombres et stade/sexe
					ResultSet carnetDeChasse = CarnetDeChasse.calculeCarnetDeChasse(info);
					excelData = new CarnetDeChasseExcel(info,carnetDeChasse);
					message = buildMessage("Carnet de chasse de "+info.get("temoin"), info);
					break;

				case 120 : // Carte des observations
					// Pour un témoin donné, carte du nombre d'espèces différentes par 
					// mailles prospectées
				break;

				case 130 : // Historique
					// Graphique par période de 20 ans du nombre de témoignages
					Historique historique = new Historique(info);
					excelData = new HistoriqueExcel(info,historique);
					message = buildMessage("Historique", info);
				break;
				/*
				// -------------------------------------------------------------------------------------------
				// OLD STATS
				case 1001 :	// temoins par periode
					List<TemoinsParPeriode> temoins = TemoinsParPeriode.calculeTemoinsParPeriode(info);
					excelData = new TemoinsParPeriodeExcel(info,temoins);
					message = buildMessage("Témoignages pour "+info.get("temoin"), info);
					break;
				case 1002 :	// Historique des especes selectionnées
					HistoriqueDesEspeces hde = new HistoriqueDesEspeces(info);
					excelData = new HistoriqueDesEspecesExcel(info,hde);
				break;
				case 1003 : // Chronologie d'un témoin
					ChronologieDUnTemoin cdut = new ChronologieDUnTemoin(info);
					excelData = new ChronologieDUnTemoinExcel(info,cdut);
					message = "Chronologie d'un témoin";
				break;
				case 1004 : // Mailles par période
					MaillesParPeriode mpp = new MaillesParPeriode(info);
					excelData = new MaillesParPeriodeExcel(info,mpp);
					message = "Mailles par période";
				break;
				case 1005 : // Histogramme des stades
					HistogrammeDesImagos hdi = new HistogrammeDesImagos(info);
					excelData = new HistogrammeDesImagosExcel(info,hdi);
				break;
				case 1006 : // Mailles par espèces
					MaillesParEspece mpe = new MaillesParEspece(info);
					excelData = new MaillesParEspeceExcel(info,mpe);
					message = "Mailles par espèces";
				break;
				case 1007 : // Carte somme
					CarteSomme cs = new CarteSomme(info);
					excelData = new CarteSommeExcel(info,cs);
					message = "Carte somme";
				break;
				case 1008 : // Carte somme biodiversité
					CarteSommeBiodiversite csb = new CarteSommeBiodiversite(info);
					excelData = new CarteSommeBiodiversiteExcel(info,csb);
					message = "Carte somme biodiversité";
				break; */
			}
		}
		if (excelData != null) {
			excelData.writeToDisk();
			return ok(exportExcel.render(message,excelData.getFileName()));
		} else {
			return ok(emptyExcel.render());
		}
	}
	
	/**
	 * Récupère les paramètres du formulaire
	 * et les charges dans une Map
	 * @return
	 */
	public static Map<String,String> getData(){
		DynamicForm df = DynamicForm.form().bindFromRequest();
		Map<String,String> info = new HashMap<String,String>();
		
		if ((df.get("groupe") != null) && (! df.get("groupe").equals("null")) && (! df.get("groupe").equals("0")))
			info.put("groupe", df.get("groupe"));
		else
			info.put("groupe", "");
		if ((df.get("sous_groupe") != null) && (! df.get("sous_groupe").equals("null")) && (! df.get("sous_groupe").equals("0")))
			info.put("sous_groupe", df.get("sous_groupe"));
		else
			info.put("sous_groupe", "");
		if ((df.get("espece") != null) && (! df.get("espece").equals("null")) && (! df.get("espece").equals("0")))
			info.put("espece", df.get("espece"));
		else
			info.put("espece", "");
		
		if ((df.get("stade") != null) && (! df.get("stade").equals("null")))
			info.put("stade", df.get("stade"));
		else
			info.put("stade", "");
		
		info.put("maille", df.get("maille"));
		info.put("mailles", df.get("mailles"));
		
		if ((df.get("temoin") != null) && (! df.get("temoin").equals("null")))
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
	 * @param info
	 * @return
	 * @throws ParseException
	 */
	public static Calendar getDataDate1(Map<String,String> info) throws ParseException {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
		c.setTime(date_format.parse(info.get("jour1")+"/"+info.get("mois1")+"/"+info.get("annee1")));
		return c;
	}
	/**
	 * Renvoie la date 2 de le Map sous forme de Calendar
	 * @param info
	 * @return
	 * @throws ParseException
	 */
	public static Calendar getDataDate2(Map<String,String> info) throws ParseException {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
		c.setTime(date_format.parse(info.get("jour2")+"/"+info.get("mois2")+"/"+info.get("annee2")));
		return c;
	}
}
