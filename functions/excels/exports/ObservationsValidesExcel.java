package functions.excels.exports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.Espece;
import models.Fiche;
import models.FicheHasMembre;
import models.Groupe;
import models.InformationsComplementaires;
import models.Observation;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import functions.excels.Excel;


public class ObservationsValidesExcel extends Excel{
	
	public ObservationsValidesExcel(Integer espece_id, String membre_nom, String orderBy, String dir, Integer groupe_id) throws IOException{
		super();
		Sheet sheet = wb.createSheet("Liste d'observations");
		Groupe groupe = Groupe.find.byId(groupe_id);
		String titre= "Liste des Observations du groupe des "+groupe.groupe_nom;
		Integer valide=Observation.VALIDEE;
		List<Observation> observations= new ArrayList<Observation>();
		if (membre_nom==""){
			if (espece_id!=0){
				Espece espece = Espece.find.byId(espece_id);
		 observations= Observation.find.where()
				.eq("observation_validee",valide)
				.eq("observation_espece.espece_sous_groupe.sous_groupe_groupe",groupe)
				.eq("observation_espece", espece)
				.orderBy(orderBy+" "+dir)
				.findList();
		titre+=" concernant l'espèce "+espece.espece_nom;
		}
		else { observations= Observation.find.where()
				.eq("observation_validee",valide)
				.eq("observation_espece.espece_sous_groupe.sous_groupe_groupe",groupe)
				.orderBy(orderBy+" "+dir)
				.findList();}}
		else {		
			List<FicheHasMembre> fhms= FicheHasMembre.find.where().eq("membre.membre_nom", membre_nom).findList();
			List<Fiche> fiches= new ArrayList<Fiche>();
			for (FicheHasMembre fhm: fhms){
				fiches.add(fhm.fiche);
			}
			if (espece_id!=0){
				Espece espece = Espece.find.byId(espece_id);
			  observations= Observation.find.where()
					.eq("observation_validee",valide)
					.eq("observation_espece.espece_sous_groupe.sous_groupe_groupe",groupe)
					.in("observation_fiche",fiches)
					.eq("observation_espece", espece)
					.orderBy(orderBy+" "+dir)
					.findList();
			titre+=" concernant l'espèce "+espece.espece_nom;
			}else {
				  observations= Observation.find.where()
					.eq("observation_validee",valide)
					.eq("observation_espece.espece_sous_groupe.sous_groupe_groupe",groupe)
					.in("observation_fiche",fiches)
					.orderBy(orderBy+" "+dir)
					.findList();
			}
			titre+=" faites par "+membre_nom;
	}titre+=".";
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
	CellStyle cellStyleDate = wb.createCellStyle();
	CreationHelper creationHelper = wb.getCreationHelper();
	cellStyleDate.setDataFormat(creationHelper.createDataFormat().getFormat("dd/mm/yyyy"));
	int i = 2;
	for(Observation observation : observations){
		Row row = sheet.createRow(i);
		row.createCell(0);
		row.createCell(1);
		row.createCell(2);
		row.createCell(3);
		row.createCell(4);
		row.createCell(5);
		row.createCell(6);
		row.createCell(7);
		row.createCell(8);
		row.createCell(9);
		row.createCell(10);
		row.createCell(11);
		row.createCell(12);
		sheet.getRow(i).getCell(0).setCellValue(observation.observation_id);
		StringBuilder membres = new StringBuilder();
		List<FicheHasMembre> mp = observation.observation_fiche.getFicheHasMembre();
		for(int j = 0 ; j<mp.size()-1 ; j++){
			membres.append(mp.get(j).membre);
			membres.append(", ");
		}
		if(!mp.isEmpty())
			membres.append(mp.get(mp.size()-1).membre);
		else
			membres.append("et al.");
		sheet.getRow(i).getCell(1).setCellValue(membres.toString());
		sheet.getRow(i).getCell(2).setCellValue(observation.observation_espece.espece_nom);
		sheet.getRow(i).getCell(3).setCellValue(observation.observation_determinateur);
		sheet.getRow(i).getCell(4).setCellValue(observation.observation_commentaires);
		sheet.getRow(i).getCell(5).setCellValue(observation.observation_fiche.fiche_date.getTime());
		sheet.getRow(i).getCell(5).setCellStyle(cellStyleDate);
		sheet.getRow(i).getCell(6).setCellValue(observation.observation_fiche.fiche_lieudit);
		if(observation.observation_fiche.fiche_commune!=null)
		{
		sheet.getRow(i).getCell(7).setCellValue(observation.observation_fiche.fiche_commune.ville_nom_aer);
		}
		sheet.getRow(i).getCell(8).setCellValue(observation.observation_fiche.fiche_utm.utm);
		sheet.getRow(i).getCell(9).setCellValue(observation.observation_fiche.fiche_memo);
		sheet.getRow(i).getCell(10).setCellValue(observation.observation_fiche.fiche_memo);
		sheet.getRow(i).getCell(12).setCellValue(observation.observation_fiche.fiche_date_soumission);
		sheet.getRow(i).getCell(12).setCellStyle(cellStyleDate);
		
		StringBuilder infos = new StringBuilder();
		List<InformationsComplementaires> ic= observation.getInfos();
		for(InformationsComplementaires complements : ic){
			if (complements.informations_complementaires_nombre_de_specimens!=null){
				infos.append(complements.informations_complementaires_nombre_de_specimens);
				infos.append(" ");
			}else infos.append("? ");
			if (complements.informations_complementaires_stade_sexe!=null)
			{infos.append(complements.informations_complementaires_stade_sexe.stade_sexe_intitule);
			infos.append(", ");
			}
		}
		sheet.getRow(i).getCell(11).setCellValue(infos.toString());
		/*
		Integer nombre = complements.informations_complementaires_nombre_de_specimens;
		if(nombre==null)
			row.createCell(8).setCellValue("?");
		else
			row.createCell(8).setCellValue(nombre);
		row.createCell(9).setCellValue(complements.informations_complementaires_stade_sexe.stade_sexe_intitule);
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
		row.createCell(10).setCellValue(membres.toString());*/
		i++;
	}

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
	}

}
