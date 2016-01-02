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

import models.Fiche;
import models.FicheHasMembre;
import models.InformationsComplementaires;
import models.Observation;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import controllers.ajax.expert.requetes.calculs.MaChronologie;
import functions.excels.Excel;

public class MaChronologieExcel extends Excel {

	public MaChronologieExcel(Map<String,String> info, MaChronologie maChronologie){
		super();
		Sheet sheet = wb.createSheet("Ma chronologie");
		String temoin = info.get("temoin");
		String titre = "Chronologie des témoignages ";
		titre+=" déposés par "+temoin;
		sheet.createRow(0).createCell(0).setCellValue(titre);
		sheet.addMergedRegion(new CellRangeAddress(
				0, //first row (0-based)
				0, //last row  (0-based)
				0, //first column (0-based)
				12  //last column  (0-based)
				));
		Row rowHead = sheet.createRow(1);
		rowHead.createCell(0).setCellValue("Fiche ID");
		rowHead.createCell(1).setCellValue("UTM");
		rowHead.createCell(2).setCellValue("Lieu-dit");
		rowHead.createCell(3).setCellValue("Commune");
		rowHead.createCell(4).setCellValue("Dép.");
		rowHead.createCell(5).setCellValue("Date min");
		rowHead.createCell(6).setCellValue("Date");
		rowHead.createCell(7).setCellValue("Espèce");
		rowHead.createCell(8).setCellValue("Nombre");
		rowHead.createCell(9).setCellValue("Stade/Sexe");
		rowHead.createCell(10).setCellValue("Témoins");
		rowHead.createCell(11).setCellValue("Mémo");
		rowHead.createCell(12).setCellValue("Groupe");
		CellStyle cellStyleDate = wb.createCellStyle();
		CreationHelper creationHelper = wb.getCreationHelper();
		cellStyleDate.setDataFormat(creationHelper.createDataFormat().getFormat("dd/mm/yyyy"));
		int i = 2;
		for(Observation observation : maChronologie.chronologie){
			for(InformationsComplementaires complement : observation.getInfos()){
				Row row = sheet.createRow(i);
				Fiche fiche = observation.observation_fiche;
				row.createCell(0).setCellValue(fiche.fiche_id);
				row.createCell(1).setCellValue(fiche.fiche_utm.utm);
				row.createCell(2).setCellValue(fiche.fiche_lieudit);
				if(fiche.fiche_commune!=null){
					row.createCell(3).setCellValue(fiche.fiche_commune.ville_nom_aer);
					row.createCell(4).setCellValue(fiche.fiche_commune.ville_departement.departement_code);
				}
				if(fiche.fiche_date_min!=null){
					Cell cell = row.createCell(5);
					cell.setCellValue(fiche.fiche_date_min.getTime());
					cell.setCellStyle(cellStyleDate);
				}
				Cell cell = row.createCell(6);
				cell.setCellValue(fiche.fiche_date.getTime());
				cell.setCellStyle(cellStyleDate);
				row.createCell(7).setCellValue(observation.observation_espece.espece_nom);
				Integer nombre = complement.informations_complementaires_nombre_de_specimens;
				if(nombre==null)
					row.createCell(8).setCellValue("?");
				else
					row.createCell(8).setCellValue(nombre);
				row.createCell(9).setCellValue(complement.informations_complementaires_stade_sexe.stade_sexe_intitule);
				StringBuilder membres = new StringBuilder();
				List<FicheHasMembre> fhms = fiche.getFicheHasMembre();
				for(int j = 0 ; j<fhms.size()-1 ; j++){
					membres.append(fhms.get(j).membre);
					membres.append(", ");
				}
				if(!fhms.isEmpty())
					membres.append(fhms.get(fhms.size()-1).membre);
				else
					membres.append("et al.");
				row.createCell(10).setCellValue(membres.toString());
				row.createCell(11).setCellValue(fiche.fiche_memo);
				row.createCell(12).setCellValue(observation.observation_espece.getGroupe().groupe_nom);
				i++;
			}
		}
		for(int j = 0; j<11 ; j++)
			sheet.autoSizeColumn(j);

	}

}
