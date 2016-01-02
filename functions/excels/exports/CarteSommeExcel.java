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

import java.util.Map;

import models.Espece;
import models.Groupe;
import models.StadeSexe;
import models.UTMS;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import controllers.ajax.expert.requetes.calculs.CarteSomme;
import functions.UTMtoXY;
import functions.excels.Excel;

public class CarteSommeExcel extends Excel {

	public CarteSommeExcel(Map<String,String> info, CarteSomme cs) {
		super();
		Sheet sheet = wb.createSheet("Carte somme");
		Espece espece = Espece.find.byId(Integer.parseInt(info.get("espece")));
		Groupe sous_groupe = Groupe.find.byId(Integer.parseInt(info.get("sous_groupe")));
		Groupe groupe = Groupe.find.byId(Integer.parseInt(info.get("groupe")));
		StadeSexe stade_sexe = StadeSexe.find.byId(Integer.parseInt(info.get("stade")));
		String date1 = info.get("jour1")+"/"+info.get("mois1")+"/"+info.get("annee1");
		String date2 = info.get("jour2")+"/"+info.get("mois2")+"/"+info.get("annee2");
		String titre = "Carte indiquant le nombre d'observations ";
		if(espece!=null)
			titre+="de "+espece.espece_nom;
		else if(sous_groupe!=null)
			titre+="de "+sous_groupe;
		else if(groupe!=null)
			titre+="de "+groupe;
		if(stade_sexe!=null)
			titre+=" au stade "+stade_sexe;
		titre+=" du "+date1+" au "+date2;
		titre+=" ("+cs.getSomme()+" témoignages)";
		sheet.createRow(0).createCell(0).setCellValue(titre);
		sheet.addMergedRegion(new CellRangeAddress(
	            0, //first row (0-based)
	            0, //last row  (0-based)
	            0, //first column (0-based)
	            12  //last column  (0-based)
	    ));
		for(int i = 1 ; i<=20 ; i++){
			sheet.createRow(i);
		}
		CellStyle redBackGround = wb.createCellStyle();
		redBackGround.setFillBackgroundColor(IndexedColors.RED.getIndex());
		redBackGround.setFillPattern(CellStyle.BIG_SPOTS);
		for(UTMS utm : cs.carte.keySet()){
			int xy[] = UTMtoXY.convert10x10(utm.utm);
			Row row = sheet.getRow(xy[1]+1);
			Cell cell = row.createCell(xy[0]);
			int nombre = cs.carte.get(utm);
			cell.setCellValue(nombre);
			if(nombre!=0)
				cell.setCellStyle(redBackGround);
		}
		for(int k = 1 ; k<=20 ; k++){
			sheet.autoSizeColumn(k);
		}
	}

}
