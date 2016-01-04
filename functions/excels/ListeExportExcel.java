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
package functions.excels;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import play.db.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;

import java.text.SimpleDateFormat;

import models.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;

import controllers.ajax.expert.requetes.nvCalculs.ListeDesTemoins;
import functions.excels.Excel;

public class ListeExportExcel extends Excel{
	private ListeExportExcel() {
		super();
	}
	
	private static String formateDate(Date uneDate) {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
		return date_format.format(uneDate);
	}
	
	public static ListeExportExcel listeDesTemoins(Map<String,String> info, ResultSet listeDesTemoins) throws IOException, SQLException{
		ListeExportExcel theFile = new ListeExportExcel();
		Sheet sheet = theFile.wb.createSheet("Témoins par période");

		String titre = "Liste des témoins ayant fait une observation"+crLf;
		if (! info.get("periode").equals("all")) {
			String date1 = info.get("jour1")+"/"+info.get("mois1")+"/"+info.get("annee1");
			String date2 = info.get("jour2")+"/"+info.get("mois2")+"/"+info.get("annee2");
			titre+=" du "+date1+" au "+date2;
		}
		
		int page = 0;
		int ligne = 7;
		theFile.collerLogoEtTitre(page,titre);
		Row rowHead = sheet.createRow(ligne);
		rowHead.createCell(0).setCellValue("Témoin");
		rowHead.createCell(1).setCellValue("Nbre tém.");
		ligne++;
		boolean ecritAGauche = true;
		while (listeDesTemoins.next()){
			String nom = listeDesTemoins.getString("membre_nom");
			String nombre = listeDesTemoins.getString("cpt");
			if(ecritAGauche){
				Row row = sheet.createRow(ligne);
				row.createCell(0).setCellValue(nom);
				row.createCell(1).setCellValue(nombre);
				ligne++;
			}else{
				Row row = sheet.getRow(ligne);
				row.createCell(3).setCellValue(nom);
				row.createCell(4).setCellValue(nombre);
				ligne++;
			}
			if(ligne%LIGNES==(LIGNES-2)){
				if(ecritAGauche){
					ecritAGauche=!ecritAGauche;
					ligne-=(LIGNES-9);
					Row row = sheet.getRow(ligne);
					row.createCell(3).setCellValue("Témoin");
					row.createCell(4).setCellValue("Nbre tém.");
					ligne++;
				}else{
					ecritAGauche=!ecritAGauche;
					//On écrit le pied de page
					theFile.piedDePage(page);
					//On fait une nouvelle page
					ligne+=9;
					page++;
					theFile.collerLogoEtTitre(page, titre);
					Row row = sheet.createRow(ligne);
					row.createCell(0).setCellValue("Témoin");
					row.createCell(1).setCellValue("Nbre tém.");
					ligne++;
				}
			}
		}
		theFile.piedDePage(page);
		sheet.setColumnWidth(0, 7937);
		sheet.autoSizeColumn(1);
		sheet.setColumnWidth(2, 256);
		sheet.setColumnWidth(3,7937);
		sheet.autoSizeColumn(4);
		
		return theFile;
	}
	
	public static ListeExportExcel listeDesEspeces(Map<String,String> info, ResultSet listeDesEspeces) throws IOException, SQLException{
		ListeExportExcel theFile = new ListeExportExcel();
		Sheet sheet = theFile.wb.createSheet("Espèces par période");

		String titre = "Liste des espèces observées"+crLf;
		if (! info.get("periode").equals("all")) {
			String date1 = info.get("jour1")+"/"+info.get("mois1")+"/"+info.get("annee1");
			String date2 = info.get("jour2")+"/"+info.get("mois2")+"/"+info.get("annee2");
			titre+=" du "+date1+" au "+date2;
		}
		
		int page = 0;
		int ligne = 7;
		theFile.collerLogoEtTitre(page,titre);
		Row rowHead = sheet.createRow(ligne);
		rowHead.createCell(0).setCellValue("Espèce");
		rowHead.createCell(1).setCellValue("Nbre mailles");
		ligne++;
		boolean ecritAGauche = true;
		while (listeDesEspeces.next()){
			String nom = listeDesEspeces.getString("espece_nom");
			String nombre = listeDesEspeces.getString("cpt");
			if(ecritAGauche){
				Row row = sheet.createRow(ligne);
				row.createCell(0).setCellValue(nom);
				row.createCell(1).setCellValue(nombre);
				ligne++;
			}else{
				Row row = sheet.getRow(ligne);
				row.createCell(3).setCellValue(nom);
				row.createCell(4).setCellValue(nombre);
				ligne++;
			}
			if(ligne%LIGNES==(LIGNES-2)){
				if(ecritAGauche){
					ecritAGauche=!ecritAGauche;
					ligne-=(LIGNES-9);
					Row row = sheet.getRow(ligne);
					row.createCell(3).setCellValue("Espèce");
					row.createCell(4).setCellValue("Nbre mailles");
					ligne++;
				}else{
					ecritAGauche=!ecritAGauche;
					//On écrit le pied de page
					theFile.piedDePage(page);
					//On fait une nouvelle page
					ligne+=9;
					page++;
					theFile.collerLogoEtTitre(page, titre);
					Row row = sheet.createRow(ligne);
					row.createCell(0).setCellValue("Espèce");
					row.createCell(1).setCellValue("Nbre mailles");
					ligne++;
				}
			}
		}
		theFile.piedDePage(page);
		sheet.setColumnWidth(0, 7937);
		sheet.autoSizeColumn(1);
		sheet.setColumnWidth(2, 256);
		sheet.setColumnWidth(3,7937);
		sheet.autoSizeColumn(4);
		
		return theFile;
	}
	
	public static ListeExportExcel listeDesTemoignages(Map<String,String> info, ResultSet listeDesTemoignages, int tailleUTM) throws IOException, SQLException{
		ListeExportExcel theFile = new ListeExportExcel();
		Sheet sheet = theFile.wb.createSheet("Liste des témoignages");

		int maxTemoignages = Integer.parseInt(info.get("maxtemoignages"));
		SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
		
		String titre = "Liste des témoignages"+crLf;
		if (! info.get("periode").equals("all")) {
			String date1 = info.get("jour1")+"/"+info.get("mois1")+"/"+info.get("annee1");
			String date2 = info.get("jour2")+"/"+info.get("mois2")+"/"+info.get("annee2");
			titre+=" du "+date1+" au "+date2;
		}
		if (! info.get("espece").equals("")) {
			titre+=" pour l'espèce "+info.get("espece");
		} else if (! info.get("sous_groupe").equals("")) {
			titre+=" pour le sous-groupe "+info.get("sous_groupe");
		} else if (! info.get("groupe").equals("")) {
			titre+=" pour le groupe "+info.get("groupe");
		}
		
		int page = 0;
		int ligne = 7;
		theFile.collerLogoEtTitre(page,titre);
		Row rowHead = sheet.createRow(ligne);
		rowHead.createCell(0).setCellValue("Espèce");
		rowHead.createCell(1).setCellValue("Maille");
		// décalage selon le nombre de colonnes pour les mailles
		// il faut 4 colonnes si on affiche les mailles UTM, UTM 20x20, UTM 50x50 et UTM 100x100
		// il faut une seule colonne si on affiche un seul type de maille
		int nbColUTM = 4; // valeur par défaut
		if (tailleUTM == 20) {
			nbColUTM = 1;
		}
		rowHead.createCell(1+nbColUTM).setCellValue("Témoin");
		rowHead.createCell(2+nbColUTM).setCellValue("Date");
		ligne++;
		String lastEspece = null;
		String lastMaille = null;
		String realLastMaille = null;
		int nbTemoignages = 0;
		while (listeDesTemoignages.next()){
			String espece = listeDesTemoignages.getString("Espece_nom");
			String maille = "";
			switch (tailleUTM) {
				case 20:
					maille = listeDesTemoignages.getString("Maille20x20");
					break;
				default:
					maille = listeDesTemoignages.getString("UTM");
					break;	
			}

			if ((realLastMaille == null) || (! realLastMaille.equals(maille))) {
				nbTemoignages = 0;
			}
			nbTemoignages++;
			realLastMaille = maille;

			if (nbTemoignages <= maxTemoignages) {
				Row row = sheet.createRow(ligne);
				if ((lastEspece == null) || (! lastEspece.equals(espece))) {
					row.createCell(0).setCellValue(espece);
					lastMaille = null;
				} else {
					row.createCell(0).setCellValue("");
				}
				if ((lastMaille == null) || (! lastMaille.equals(maille))) {
					row.createCell(1).setCellValue(maille);
					if (tailleUTM != 20) {
						row.createCell(2).setCellValue(listeDesTemoignages.getString("Maille20x20"));
						row.createCell(3).setCellValue(listeDesTemoignages.getString("Maille50x50"));
						row.createCell(4).setCellValue(listeDesTemoignages.getString("Maille100x100"));
					}
				} else {
					for (int i = 1; i<1+nbColUTM; i++) {
						row.createCell(i).setCellValue("");
					}
				}
				row.createCell(1+nbColUTM).setCellValue(listeDesTemoignages.getString("Membre_nom"));
				row.createCell(2+nbColUTM).setCellValue(date_format.format(listeDesTemoignages.getDate("Fiche_Date")));
				ligne++;
			
				if (ligne%LIGNES==(LIGNES-2)){
					//On écrit le pied de page
					theFile.piedDePage(page);
				
					//On fait une nouvelle page
					ligne+=9;
					page++;
					theFile.collerLogoEtTitre(page, titre);
					row = sheet.createRow(ligne);
					row.createCell(0).setCellValue("Espèce");
					rowHead.createCell(1).setCellValue("Maille");
					row.createCell(1+nbColUTM).setCellValue("Témoin");
					rowHead.createCell(2+nbColUTM).setCellValue("Date");
					ligne++;
				
					lastEspece = null;
					lastMaille = null;
				} else {
					lastEspece = espece;
					lastMaille = maille;
				}
			} else {
				lastEspece = espece;
				lastMaille = maille;
			}
		}
		theFile.piedDePage(page);
		sheet.setColumnWidth(0, 7937);
		for (int i = 1; i<3+nbColUTM; i++) {
			sheet.autoSizeColumn(i);
		}
		return theFile;
	}
	
	public static ListeExportExcel sommeEspeces(Map<String,String> info, ResultSet sommeEspeces, int tailleUTM) throws IOException, SQLException{
		ListeExportExcel theFile = new ListeExportExcel();
		Sheet sheet = theFile.wb.createSheet("Somme des espèces");

		String titre = "Somme des espèces"+crLf;
		if (! info.get("periode").equals("all")) {
			String date1 = info.get("jour1")+"/"+info.get("mois1")+"/"+info.get("annee1");
			String date2 = info.get("jour2")+"/"+info.get("mois2")+"/"+info.get("annee2");
			titre+=" du "+date1+" au "+date2;
		}
		if (! info.get("sous_groupe").equals("")) {
			titre+=" pour le sous-groupe "+info.get("sous_groupe");
		} else if (! info.get("groupe").equals("")) {
			titre+=" pour le groupe "+info.get("groupe");
		}
		
		int page = 0;
		int ligne = 7;
		theFile.collerLogoEtTitre(page,titre);
		Row rowHead = sheet.createRow(ligne);
		rowHead.createCell(0).setCellValue("Groupe");
		rowHead.createCell(1).setCellValue("Maille");
		rowHead.createCell(2).setCellValue("Nb d'espèces");
		ligne++;
		String lastGroupe = null;
		String lastMaille = null;
		while (sommeEspeces.next()){
			String groupe = sommeEspeces.getString("groupe.groupe_nom");
			String maille = "";
			switch (tailleUTM) {
				case 20:
					maille = sommeEspeces.getString("utms.maille20x20");
					break;
				default:
					maille = sommeEspeces.getString("utms.utm");
					break;	
			}

				Row row = sheet.createRow(ligne);
				if ((lastGroupe == null) || (! lastGroupe.equals(groupe))) {
					row.createCell(0).setCellValue(groupe);
					lastMaille = null;
				} else {
					row.createCell(0).setCellValue("");
				}
				if ((lastMaille == null) || (! lastMaille.equals(maille))) {
					row.createCell(1).setCellValue(maille);
				}
				row.createCell(2).setCellValue(sommeEspeces.getString("nbespeces"));
				ligne++;
			
				if (ligne%LIGNES==(LIGNES-2)){
					//On écrit le pied de page
					theFile.piedDePage(page);
				
					//On fait une nouvelle page
					ligne+=9;
					page++;
					theFile.collerLogoEtTitre(page, titre);
					row = sheet.createRow(ligne);
					row.createCell(0).setCellValue("Groupe");
					rowHead.createCell(1).setCellValue("Maille");
					row.createCell(2).setCellValue("Nb d'espèces");
					ligne++;
				
					lastGroupe = null;
					lastMaille = null;
				} else {
					lastGroupe = groupe;
					lastMaille = maille;
				}
		}
		theFile.piedDePage(page);
		sheet.setColumnWidth(0, 7937);
		for (int i = 1; i<4; i++) {
			sheet.autoSizeColumn(i);
		}
		return theFile;
	}
	
	public static ListeExportExcel listeEspecesParMaille(Map<String,String> info, ResultSet especesParMaille) throws SQLException{
		ListeExportExcel theFile = new ListeExportExcel();
		Sheet sheet = theFile.wb.createSheet("Especes par maille");

		String titre = "Espèces trouvées par maille ";
		
		sheet.createRow(0).createCell(0).setCellValue(titre);
		sheet.addMergedRegion(new CellRangeAddress(
				0, //first row (0-based)
				0, //last row  (0-based)
				0, //first column (0-based)
				330  //last column  (0-based)
				));
		Row rowHead = sheet.createRow(1);
		rowHead.createCell(0).setCellValue("Maille");
		rowHead.createCell(1).setCellValue("Espèces observées");

		CellStyle cellStyleDate = theFile.wb.createCellStyle();
		CreationHelper creationHelper = theFile.wb.getCreationHelper();
		cellStyleDate.setDataFormat(creationHelper.createDataFormat().getFormat("dd/mm/yyyy"));
		
		especesParMaille.next();
		String maille = especesParMaille.getString("f.fiche_utm_utm");
		String espece = especesParMaille.getString("e.espece_nom");
		String nombre = especesParMaille.getString("count(e.espece_nom)");
		Row row = sheet.createRow(2);
		row.createCell(0).setCellValue(maille);
		row.createCell(1).setCellValue(espece + " : " + nombre);
		
		int i = 3;
		int j = 1;
		while (especesParMaille.next()) {
			String utm = especesParMaille.getString("f.fiche_utm_utm");
			espece = especesParMaille.getString("e.espece_nom");
			nombre = especesParMaille.getString("count(e.espece_nom)");
			
			if (!utm.equals(maille)){
				row = sheet.createRow(i);
				row.createCell(0).setCellValue(utm);
				row.createCell(1).setCellValue(espece + " : " + nombre);
				i++;
				j=2;
				maille = utm;		
			} else {
				row.createCell(j).setCellValue(espece + " : " + nombre);
				j++;
			}

		}

		for(int k = 0; k<329 ; k++)
			sheet.autoSizeColumn(k);
		return(theFile);
	}
	
	public static ListeExportExcel observationsValidesExcel(Integer espece_id, String membre_nom, String orderBy, String dir, Integer groupe_id) throws ParseException, IOException {
		ListeExportExcel theFile = new ListeExportExcel();
		Sheet sheet = theFile.wb.createSheet("Liste d'observations");

		try {
			DataSource ds = DB.getDataSource();
			Connection connection = ds.getConnection();

			Groupe groupe = Groupe.find.byId(groupe_id);
			String titre= "Liste des Observations du groupe des "+groupe.groupe_nom;
			
			// on recupere la liste des groupes possibles
			LinkedList<Integer> pile = new LinkedList<Integer>();
			if (groupe_id > 0) {
				pile.add(groupe_id);
			}
			String gStatement = "SELECT * FROM groupe WHERE groupe_pere_groupe_id=?";
			PreparedStatement listeGroupes = connection.prepareStatement(gStatement);

			StringBuilder groupList = new StringBuilder();
			int nbGroupes = 0;
			while (! pile.isEmpty()) {
				int unGroupe = pile.getFirst();
				pile.removeFirst();
				
				if (nbGroupes > 0)
					groupList.append(",");
				nbGroupes++;	
				groupList.append(unGroupe);
				
				listeGroupes.setInt(1, unGroupe);
				ResultSet rsGroupe = listeGroupes.executeQuery();
				while (rsGroupe.next()) {
					pile.add(rsGroupe.getInt("groupe_id"));
				}
			}
			
			String statement = "SELECT obs.*, espece.*, fiche.*, commune.*"
				+ " FROM observation obs"
				+ " INNER JOIN fiche ON obs.observation_fiche_fiche_id = fiche.fiche_id"
				+ " INNER JOIN espece ON obs.observation_espece_espece_id = espece.espece_id"
				+ " INNER JOIN espece_is_in_groupement_local ON espece.espece_id = espece_is_in_groupement_local.espece_espece_id"
				+ " INNER JOIN groupe ON espece_is_in_groupement_local.groupe_groupe_id = groupe.groupe_id"
				+ " INNER JOIN utms ON utms.utm = fiche.fiche_utm_utm"
				+ " LEFT OUTER JOIN commune ON fiche_commune_ville_id=ville_id"
				+ " WHERE obs.observation_validee = 1";
			if (espece_id > 0) {
				statement += " AND espece.espece_id="+espece_id;

				Espece espece = Espece.find.byId(espece_id);
				titre+=" concernant l'espèce "+espece.espece_nom;
			}
			
			if (groupe_id > 0) {
				statement += " AND groupe.groupe_id IN ("+groupList+")";
			}
			
			if (! membre_nom.equals("")){	
				List<FicheHasMembre> fhms= FicheHasMembre.find.where().eq("membre.membre_nom", membre_nom).findList();
				StringBuilder fiches= new StringBuilder();
				fiches.append("-1");
				for (FicheHasMembre fhm: fhms){
					fiches.append(",");
					fiches.append(fhm.fiche);
				}
				statement += " AND obs.observation_fiche IN ("+fiches+")";

				titre+=" faites par "+membre_nom;
			}
			
			statement += " ORDER BY "+orderBy+" "+dir;

			String statement1 = "SELECT * FROM fiche_has_membre"
				+ " INNER JOIN membre ON membre_membre_id=membre_id"
				+ " WHERE fiche_fiche_id=?";
			PreparedStatement listeMembres = connection.prepareStatement(statement1);

			String statement2 = "SELECT * FROM informations_complementaires"
				+ " LEFT OUTER JOIN stade_sexe ON informations_complementaires_stade_sexe_stade_sexe_id=stade_sexe_id"
				+ " WHERE informations_complementaires_observation_observation_id=?";
			PreparedStatement listeInfos = connection.prepareStatement(statement2);

			PreparedStatement listeObservations = connection.prepareStatement(statement); 
			ResultSet rsListeObservations = listeObservations.executeQuery();
			
			titre+=".";
			sheet.createRow(0).createCell(0).setCellValue(titre);
			Row rowtitre = sheet.createRow(1);
			rowtitre.createCell(0).setCellValue("id");
			rowtitre.createCell(1).setCellValue("nom du(des) témoins");
			rowtitre.createCell(2).setCellValue("espèce");
			rowtitre.createCell(3).setCellValue("déterminateur");
			rowtitre.createCell(4).setCellValue("commentaires");
			rowtitre.createCell(5).setCellValue("Date d'observation");
			rowtitre.createCell(6).setCellValue("Lieu-dit");
			rowtitre.createCell(7).setCellValue("Commune");
			rowtitre.createCell(8).setCellValue("UTM");
			rowtitre.createCell(9).setCellValue("Memo");
			rowtitre.createCell(10).setCellValue("Date de soumission");
			rowtitre.createCell(11).setCellValue("Informations supplémentaires");
			rowtitre.createCell(12).setCellValue("Date de validation");
			CellStyle cellStyleDate = theFile.wb.createCellStyle();
			CreationHelper creationHelper = theFile.wb.getCreationHelper();
			cellStyleDate.setDataFormat(creationHelper.createDataFormat().getFormat("dd/mm/yyyy"));
			
			int i = 2;
			while (rsListeObservations.next()){

				Row row = sheet.createRow(i);
				for (int j=0; j<=12; j++)
					row.createCell(j);
				
				sheet.getRow(i).getCell(0).setCellValue(rsListeObservations.getInt("observation_id"));
				
				StringBuilder membres = new StringBuilder();
				listeMembres.setInt(1, rsListeObservations.getInt("observation_fiche_fiche_id"));
				ResultSet rs1 = listeMembres.executeQuery();
				int noMembre = 0;
				while (rs1.next()) {
					if (noMembre > 0)
						membres.append(", ");
					membres.append(rs1.getString("membre_nom"));
					noMembre++;
				}
				rs1.close();
				sheet.getRow(i).getCell(1).setCellValue(membres.toString());
				
				sheet.getRow(i).getCell(2).setCellValue(rsListeObservations.getString("espece_nom"));
				sheet.getRow(i).getCell(3).setCellValue(rsListeObservations.getString("observation_determinateur"));
				sheet.getRow(i).getCell(4).setCellValue(rsListeObservations.getString("observation_commentaires"));
				sheet.getRow(i).getCell(5).setCellValue(formateDate(rsListeObservations.getDate("fiche_date")));
				sheet.getRow(i).getCell(5).setCellStyle(cellStyleDate);
				sheet.getRow(i).getCell(6).setCellValue(rsListeObservations.getString("fiche_lieudit"));
				
				String commune = rsListeObservations.getString("ville_nom_aer");
				if (! rsListeObservations.wasNull()){
					sheet.getRow(i).getCell(7).setCellValue(commune);
				}
				sheet.getRow(i).getCell(8).setCellValue(rsListeObservations.getString("fiche_utm_utm"));
				sheet.getRow(i).getCell(9).setCellValue(rsListeObservations.getString("fiche_memo"));
				sheet.getRow(i).getCell(10).setCellValue(formateDate(rsListeObservations.getDate("fiche_date_soumission")));
				sheet.getRow(i).getCell(12).setCellStyle(cellStyleDate);
				
				listeInfos.setInt(1, rsListeObservations.getInt("observation_id"));
				ResultSet rs2 = listeInfos.executeQuery();
				
				StringBuilder infos = new StringBuilder();
				while (rs2.next()) {
					int nbSpecimens=rs2.getInt("informations_complementaires_nombre_de_specimens");
					if (! rs2.wasNull()){
						infos.append(nbSpecimens);
						infos.append(" ");
					} else infos.append("? ");
					
					String stade_sexe_intitule = rs2.getString("stade_sexe_intitule");
					if (! rs2.wasNull()){
						infos.append(stade_sexe_intitule);
						infos.append(", ");
					}
				}
				sheet.getRow(i).getCell(11).setCellValue(infos.toString());
				
				rs2.close();
				
				i++;
			}
			listeInfos.close();
			listeMembres.close();

			sheet.autoSizeColumn(1);
			sheet.autoSizeColumn(2);
			sheet.autoSizeColumn(3);
			sheet.autoSizeColumn(4);
			sheet.autoSizeColumn(5);
			sheet.autoSizeColumn(6);
			sheet.autoSizeColumn(7);
			sheet.autoSizeColumn(8);
			sheet.autoSizeColumn(9);
			sheet.autoSizeColumn(10);
			sheet.autoSizeColumn(11);
			sheet.autoSizeColumn(12);
			
			rsListeObservations.close();
			listeObservations.close();
			connection.close();
		} catch (SQLException ex) {
			System.out.println("an SQL exception occured" + ex);
		}
	
		return theFile;
	}
}
