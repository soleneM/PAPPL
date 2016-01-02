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
package functions.excels.exports;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.SQLException;

import models.Espece;
import models.Groupe;
import models.StadeSexe;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import controllers.ajax.expert.requetes.nvCalculs.ListeDesEspeces;
import functions.excels.Excel;

public class ListeDesEspecesExcel extends Excel{

	public ListeDesEspecesExcel(Map<String,String> info, ResultSet listeDesEspeces) throws IOException, SQLException{
		super();
		Sheet sheet = wb.createSheet("Témoins par période");

		String date1 = info.get("jour1")+"/"+info.get("mois1")+"/"+info.get("annee1");
		String date2 = info.get("jour2")+"/"+info.get("mois2")+"/"+info.get("annee2");
		String titre = "Liste des espèces référencées"+crLf;
		titre+=" du "+date1+" au "+date2;


		int page = 0;
		int ligne = 7;
		this.collerLogoEtTitre(page,titre);
		Row rowHead = sheet.createRow(ligne);
		rowHead.createCell(0).setCellValue("Espèce");
		rowHead.createCell(1).setCellValue("Nbre de mailles");
		ligne++;
		boolean ecritAGauche = true;
		while (listeDesEspeces.next()){
			String espece = listeDesEspeces.getString("e.espece_nom");
			String nombre = listeDesEspeces.getString("count(f.fiche_utm_utm)");
			if(ecritAGauche){
				Row row = sheet.createRow(ligne);
				row.createCell(0).setCellValue(espece);
				row.createCell(1).setCellValue(nombre);
				ligne++;
			}else{
				Row row = sheet.getRow(ligne);
				row.createCell(3).setCellValue(espece);
				row.createCell(4).setCellValue(nombre);
				ligne++;
			}
			if(ligne%LIGNES==(LIGNES-2)){
				if(ecritAGauche){
					ecritAGauche=!ecritAGauche;
					ligne-=(LIGNES-9);
					Row row = sheet.getRow(ligne);
					row.createCell(3).setCellValue("Espèce");
					row.createCell(4).setCellValue("Nbre de mailles");
					ligne++;
				}else{
					ecritAGauche=!ecritAGauche;
					//On écrit le pied de page
					this.piedDePage(page);
					//On fait une nouvelle page
					ligne+=9;
					page++;
					this.collerLogoEtTitre(page, titre);
					Row row = sheet.createRow(ligne);
					row.createCell(0).setCellValue("Espèce");
					row.createCell(1).setCellValue("Nbre de mailles");
					ligne++;
				}
			}
		}
		this.piedDePage(page);
		sheet.setColumnWidth(0, 7937);
		sheet.autoSizeColumn(1);
		sheet.setColumnWidth(2, 256);
		sheet.setColumnWidth(3,7937);
		sheet.autoSizeColumn(4);
	}
}
