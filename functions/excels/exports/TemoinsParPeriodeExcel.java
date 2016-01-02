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

import models.Espece;
import models.Groupe;
import models.StadeSexe;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import controllers.ajax.expert.requetes.calculs.TemoinsParPeriode;
import functions.excels.Excel;

public class TemoinsParPeriodeExcel extends Excel{

	public TemoinsParPeriodeExcel(Map<String,String> info, List<TemoinsParPeriode> temoins) throws IOException{
		super();
		Sheet sheet = wb.createSheet("Témoins par période");
		Espece espece = Espece.find.byId(Integer.parseInt(info.get("espece")));
		Groupe sous_groupe = Groupe.find.byId(Integer.parseInt(info.get("sous_groupe")));
		Groupe groupe = Groupe.find.byId(Integer.parseInt(info.get("groupe")));
		StadeSexe stade_sexe = StadeSexe.find.byId(Integer.parseInt(info.get("stade")));
		String maille = info.get("maille");
		String date1 = info.get("jour1")+"/"+info.get("mois1")+"/"+info.get("annee1");
		String date2 = info.get("jour2")+"/"+info.get("mois2")+"/"+info.get("annee2");
		String titre = "Liste des témoins ayant fait une observation"+crLf;
		if(espece!=null)
			titre+="de "+espece.espece_nom;
		else if(sous_groupe!=null)
			titre+="de "+sous_groupe;
		else if(groupe!=null)
			titre+="de "+groupe;
		if(stade_sexe!=null)
			titre+=" au stade "+stade_sexe;
		if(!maille.equals(""))
			titre+=" dans la maille "+maille;
		titre+=" du "+date1+" au "+date2;
		titre+=crLf+" ("+temoins.size()+" témoin(s) pour "+TemoinsParPeriode.getSomme(temoins)+" témoignage(s))";

		int page = 0;
		int ligne = 7;
		this.collerLogoEtTitre(page,titre);
		Row rowHead = sheet.createRow(ligne);
		rowHead.createCell(0).setCellValue("Témoin");
		rowHead.createCell(1).setCellValue("Nbre tém.");
		ligne++;
		boolean ecritAGauche = true;
		for(TemoinsParPeriode temoin : temoins){
			if(ecritAGauche){
				Row row = sheet.createRow(ligne);
				row.createCell(0).setCellValue(temoin.temoin.toString());
				row.createCell(1).setCellValue(temoin.nombreDeTemoignages);
				ligne++;
			}else{
				Row row = sheet.getRow(ligne);
				row.createCell(3).setCellValue(temoin.temoin.toString());
				row.createCell(4).setCellValue(temoin.nombreDeTemoignages);
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
					this.piedDePage(page);
					//On fait une nouvelle page
					ligne+=9;
					page++;
					this.collerLogoEtTitre(page, titre);
					Row row = sheet.createRow(ligne);
					row.createCell(0).setCellValue("Témoin");
					row.createCell(1).setCellValue("Nbre tém.");
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
