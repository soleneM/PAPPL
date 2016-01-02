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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;

import models.Fiche;
import models.Groupe;
import models.InformationsComplementaires;
import models.Observation;
import models.StadeSexe;
import functions.DateUtil;
import functions.excels.Excel;

public class ExportExcelEdit extends Excel{
	private List<InformationsComplementaires> donnees;

	public static int STADE_INDETERMINE = 1;
	public static int COMMUNE_INCONNUE = 2;

	public ExportExcelEdit(Groupe groupe, int contenu) {
		StadeSexe stade_sexe = StadeSexe.find.where().eq("stade_sexe_intitule", "Indéterminé").findUnique();
		ExpressionList<InformationsComplementaires> query = InformationsComplementaires.find.where()
				.eq("informations_complementaires_observation.observation_espece.espece_sous_groupe.sous_groupe_groupe", groupe);
		if(contenu==1)
			query = query.eq("informations_complementaires_stade_sexe", stade_sexe);
		else if(contenu==2)
			query = query.eq("informations_complementaires_observation.observation_fiche.fiche_commune", null);
		else if(contenu==3)
			query = query.or(
					Expr.eq("informations_complementaires_observation.observation_fiche.fiche_commune", null),
					Expr.eq("informations_complementaires_stade_sexe", stade_sexe)
					);
		donnees = query
				.orderBy("informations_complementaires_observation.observation_fiche.fiche_date desc,"+
						"informations_complementaires_observation.observation_fiche.fiche_id,"+
						"informations_complementaires_observation.observation_id,"+
						"informations_complementaires_id")
						.findList();
		Sheet sheet = wb.createSheet("Export des "+groupe+", "+donnees.size()+" lignes");
		Row row = sheet.createRow(0);
		row.createCell(0).setCellValue("InfoCompl.ID");
		row.createCell(1).setCellValue("Obs.ID");
		row.createCell(2).setCellValue("Fiche.ID");
		row.createCell(3).setCellValue("UTM");
		row.createCell(4).setCellValue("Lieu-dit");
		row.createCell(5).setCellValue("Commune");
		row.createCell(6).setCellValue("Date min");
		row.createCell(7).setCellValue("Date");
		row.createCell(8).setCellValue("Espèce");
		row.createCell(9).setCellValue("Nbr");
		row.createCell(10).setCellValue("Stade");
		row.createCell(11).setCellValue("Témoin(s)");
		row.createCell(12).setCellValue("Déterminateur");
		row.createCell(13).setCellValue("Mémo");
		int i = 1;
		for(InformationsComplementaires info : donnees){
			Observation o = info.informations_complementaires_observation;
			Fiche f = o.observation_fiche;
			row = sheet.createRow(i);
			row.createCell(0).setCellValue(info.informations_complementaires_id);
			row.createCell(1).setCellValue(o.observation_id);
			row.createCell(2).setCellValue(f.fiche_id);
			row.createCell(3).setCellValue(f.fiche_utm.utm);
			row.createCell(4).setCellValue(f.fiche_lieudit);
			if(f.fiche_commune!=null)
				row.createCell(5).setCellValue(f.fiche_commune.ville_nom_reel);
			if(f.fiche_date_min!=null){
				Cell cell = row.createCell(6);
				cell.setCellValue(DateUtil.toStringExcel(f.fiche_date_min));
			}
			Cell cell = row.createCell(7);
			cell.setCellValue(DateUtil.toStringExcel(f.fiche_date));
			row.createCell(8).setCellValue(o.observation_espece.espece_nom);
			if(info.informations_complementaires_nombre_de_specimens==null)
				row.createCell(9).setCellValue("?");
			else
				row.createCell(9).setCellValue(info.informations_complementaires_nombre_de_specimens);
			row.createCell(10).setCellValue(info.informations_complementaires_stade_sexe.stade_sexe_intitule);
			row.createCell(11).setCellValue(f.getTemoinsToString());
			row.createCell(12).setCellValue(o.observation_determinateur);
			row.createCell(13).setCellValue(f.fiche_memo);
			i++;
		}
		for(i=0;i<14;i++)
			sheet.autoSizeColumn(i);
	}

}
