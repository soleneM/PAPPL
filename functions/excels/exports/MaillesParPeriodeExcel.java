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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import models.Espece;
import models.Groupe;
import models.StadeSexe;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import controllers.ajax.expert.requetes.calculs.MaillesParPeriode;
import functions.excels.Excel;

public class MaillesParPeriodeExcel extends Excel {

	public MaillesParPeriodeExcel(Map<String,String> info, MaillesParPeriode mpp) throws IOException {
		ArrayList<Espece> especes = new ArrayList<Espece>(mpp.nb_mailles_par_espece.keySet());
		Collections.sort(especes, new Comparator<Espece>(){
			@Override
			public int compare(Espece arg0, Espece arg1) {
				return arg0.espece_nom.compareTo(arg1.espece_nom);
			}
		});
		Sheet sheet = wb.createSheet("Mailles par espèce");
		Espece espece = Espece.find.byId(Integer.parseInt(info.get("espece")));
		Groupe sous_groupe = Groupe.find.byId(Integer.parseInt(info.get("sous_groupe")));
		Groupe groupe = Groupe.find.byId(Integer.parseInt(info.get("groupe")));
		StadeSexe stade_sexe = StadeSexe.find.byId(Integer.parseInt(info.get("stade")));
		//Titre
		String date1 = info.get("jour1")+"/"+info.get("mois1")+"/"+info.get("annee1");
		String date2 = info.get("jour2")+"/"+info.get("mois2")+"/"+info.get("annee2");
		String titre = "Mailles par période pour ";
		if(espece!=null)
			titre+=espece.espece_nom;
		else if(sous_groupe!=null)
			titre+=" les espèces "+sous_groupe;
		else if(groupe!=null)
			titre+=" les espèces "+groupe;
		if(stade_sexe!=null)
			titre+=" au stade "+stade_sexe;
		titre+=crLf+"du "+date1+" au "+date2;

		int page = 0;
		int ligne = 7;
		boolean ecritAGauche = true;
		//Coller le logo en haut à gauche
		this.collerLogo(page);
		Row row = sheet.createRow(ligne);
		sheet.createRow(ligne-4).createCell(4).setCellValue(titre);
		sheet.addMergedRegion(new CellRangeAddress(
				ligne-4, //first row (0-based)
				ligne-3, //last row  (0-based)
				4, //first column (0-based)
				8  //last column  (0-based)
				));
		ligne++;
		for(Espece especeATraiter : especes){
			if(ecritAGauche){
				row = sheet.createRow(ligne);
				row.createCell(0).setCellValue(especeATraiter.espece_nom);
				sheet.addMergedRegion(new CellRangeAddress(
						ligne, //first row (0-based)
						ligne, //last row  (0-based)
						0, //first column (0-based)
						1  //last column  (0-based)
						));
				row.createCell(2).setCellValue(mpp.nb_mailles_par_espece.get(especeATraiter)+" maille(s)");
				ligne++;
			}else{
				row = sheet.getRow(ligne);
				row.createCell(4).setCellValue(especeATraiter.espece_nom);
				sheet.addMergedRegion(new CellRangeAddress(
						ligne, //first row (0-based)
						ligne, //last row  (0-based)
						4, //first column (0-based)
						5  //last column  (0-based)
						));
				row.createCell(6).setCellValue(mpp.nb_mailles_par_espece.get(especeATraiter)+"  maille(s)");
				ligne++;
			}
			if(ligne%LIGNES==(LIGNES-2)){
				if(ecritAGauche){
					ecritAGauche=!ecritAGauche;
					ligne-=(LIGNES-10);
				}else{
					ecritAGauche=!ecritAGauche;
					//On écrit le pied de page
					row = sheet.createRow(ligne+1);
					row.createCell(8).setCellValue("Page "+(page+1));
					//On fait une nouvelle page
					ligne+=10;
					page++;
					this.collerLogo(page);
					sheet.createRow(ligne-4).createCell(4).setCellValue(titre);
					sheet.addMergedRegion(new CellRangeAddress(
							ligne-4, //first row (0-based)
							ligne-3, //last row  (0-based)
							4, //first column (0-based)
							8  //last column  (0-based)
							));
				}
			}
		}
	}

}
