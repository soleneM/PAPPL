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

import java.util.List;
import java.util.Map;

import models.Espece;
import models.FicheHasMembre;
import models.Groupe;
import models.InformationsComplementaires;
import models.StadeSexe;
import models.UTMS;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import controllers.ajax.expert.requetes.calculs.CarteSommeBiodiversite;
import functions.UTMtoXY;
import functions.excels.Excel;

public class CarteSommeBiodiversiteExcel extends Excel {

	public CarteSommeBiodiversiteExcel(Map<String, String> info,CarteSommeBiodiversite csb) {
		super();
		Sheet sheet = wb.createSheet("Carte somme de la biodiversité");
		Espece espece = Espece.find.byId(Integer.parseInt(info.get("espece")));
		Groupe sous_groupe = Groupe.find.byId(Integer.parseInt(info.get("sous_groupe")));
		Groupe groupe = Groupe.find.byId(Integer.parseInt(info.get("groupe")));
		StadeSexe stade_sexe = StadeSexe.find.byId(Integer.parseInt(info.get("stade")));
		String date1 = info.get("jour1")+"/"+info.get("mois1")+"/"+info.get("annee1");
		String date2 = info.get("jour2")+"/"+info.get("mois2")+"/"+info.get("annee2");
		String titre = "Carte indiquant les premières observations ";
		if(espece!=null)
			titre+="de "+espece.espece_nom;
		else if(sous_groupe!=null)
			titre+="de "+sous_groupe;
		else if(groupe!=null)
			titre+="de "+groupe;
		if(stade_sexe!=null)
			titre+=" au stade "+stade_sexe;
		titre+=" du "+date1+" au "+date2;
		sheet.createRow(0).createCell(0).setCellValue(titre);
		sheet.addMergedRegion(new CellRangeAddress(
				0, //first row (0-based)
				0, //last row  (0-based)
				0, //first column (0-based)
				12  //last column  (0-based)
				));
		Row rowHead = sheet.createRow(1);
		rowHead.createCell(0).setCellValue("UTM");
		rowHead.createCell(1).setCellValue("Fiche ID");
		rowHead.createCell(2).setCellValue("Espèce");
		rowHead.createCell(3).setCellValue("Date");
		rowHead.createCell(4).setCellValue("Témoin(s)");
		CellStyle cellStyleDate = wb.createCellStyle();
		CreationHelper creationHelper = wb.getCreationHelper();
		cellStyleDate.setDataFormat(creationHelper.createDataFormat().getFormat("dd/mm/yyyy"));
		int i = 2;
		for(UTMS utm : UTMS.findAll()){
			List<List<InformationsComplementaires>> observationsDansCetteMaille = csb.carte.get(utm);
			for(List<InformationsComplementaires> observationsPourCetteEspece : observationsDansCetteMaille){
				for(InformationsComplementaires complements : observationsPourCetteEspece){
					Row row = sheet.createRow(i);
					row.createCell(0).setCellValue(utm.utm);
					row.createCell(1).setCellValue(complements.informations_complementaires_observation.observation_fiche.fiche_id);
					row.createCell(2).setCellValue(complements.informations_complementaires_observation.observation_espece.espece_nom);
					Cell cellDate = row.createCell(3);
					cellDate.setCellValue(complements.informations_complementaires_observation.observation_fiche.fiche_date);
					cellDate.setCellStyle(cellStyleDate);
					StringBuilder membres = new StringBuilder();
					List<FicheHasMembre> fhms = complements.informations_complementaires_observation.observation_fiche.getFicheHasMembre();
					for(int j = 0 ; j<fhms.size()-1 ; j++){
						membres.append(fhms.get(j).membre);
						membres.append(", ");
					}
					if(!fhms.isEmpty())
						membres.append(fhms.get(fhms.size()-1).membre);
					else
						membres.append("et al.");
					row.createCell(4).setCellValue(membres.toString());
					i++;
				}
			}
		}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		for(int k = 1 ; k<=20 ; k++){
			if(sheet.getRow(k)==null)
				sheet.createRow(k);
		}
		CellStyle redBackGround = wb.createCellStyle();
		redBackGround.setFillBackgroundColor(IndexedColors.RED.getIndex());
		redBackGround.setFillPattern(CellStyle.BIG_SPOTS);
		for(UTMS utm : csb.carte.keySet()){
			int xy[] = UTMtoXY.convert10x10(utm.utm);
			Row row = sheet.getRow(xy[1]+1);
			Cell cell = row.createCell(xy[0]+5);
			int nombreDEspeces = csb.getNombreDEspecesDansMaille(utm);
			cell.setCellValue(nombreDEspeces);
			if(nombreDEspeces!=0)
				cell.setCellStyle(redBackGround);
		}
		for(int k = 5 ; k<25 ; k++){
			sheet.autoSizeColumn(k);
		}
		Row rowUniteMailleEspece;
		if((rowUniteMailleEspece=sheet.getRow(23))==null)
			rowUniteMailleEspece = sheet.createRow(23);
		rowUniteMailleEspece.createCell(6).setCellValue("Unités maille-espèce : "+csb.getUnitesMailleEspece());
	}

}
