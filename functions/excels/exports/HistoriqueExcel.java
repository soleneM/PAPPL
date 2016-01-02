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
import java.util.Map;
import java.util.Calendar;


import models.Espece;
import models.Groupe;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import controllers.ajax.expert.requetes.nvCalculs.Historique;
import functions.excels.Excel;

public class HistoriqueExcel extends Excel {
	
	public HistoriqueExcel(Map<String,String> info, Historique hde) throws IOException{
		super();
		Sheet sheet = wb.createSheet("Historique des temoignages");
		Espece espece = Espece.find.byId(Integer.parseInt(info.get("espece")));
		Groupe sous_groupe = Groupe.find.byId(Integer.parseInt(info.get("sous_groupe")));
		Groupe groupe = Groupe.find.byId(Integer.parseInt(info.get("groupe")));
		String maille = info.get("maille");
		String titre = "Historique des temoignages ";
		if(espece!=null)
			titre+="de "+espece.espece_nom;
		else if(sous_groupe!=null)
			titre+="de "+sous_groupe;
		else if(groupe!=null)
			titre+="de "+groupe;
		if(!maille.equals(""))
			titre+=" dans la maille "+maille;
		titre+=" ("+hde.getSomme()+" témoignages, "+hde.nbTemoignagesRejetes+" témoignages rejetés)";
		sheet.createRow(0).createCell(0).setCellValue(titre);
		sheet.addMergedRegion(new CellRangeAddress(
	            0, //first row (0-based)
	            0, //last row  (0-based)
	            0, //first column (0-based)
	            12  //last column  (0-based)
	    ));
		Row row = sheet.createRow(1);
		row.createCell(0).setCellValue("Période");
		row.createCell(1).setCellValue("Nbr. tem.");
		int i = 0;
		while(i<hde.histogramme.length){
			row = sheet.createRow(i+2);
			row.createCell(0);
			row.createCell(1);
			sheet.getRow(i+2).getCell(0).setCellValue(hde.legende.get(i));
			sheet.getRow(i+2).getCell(1).setCellValue(hde.histogramme[i]);
			i++;
		}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
	}
}
