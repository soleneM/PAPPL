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

import java.text.ParseException;
import java.util.Calendar;

import javax.persistence.PersistenceException;

import models.Commune;
import models.Espece;
import models.EspeceSynonyme;
import models.Fiche;
import models.FicheHasMembre;
import models.InformationsComplementaires;
import models.Membre;
import models.Observation;
import models.StadeSexe;
import models.UTMS;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import functions.DateUtil;

public class RowCheckEdit {
	private Row row;
	private int rowNumber;
	private StringBuilder errorReport;
	private boolean noError = true;
	private Calendar creationTime;

	private InformationsComplementaires complement = null;
	private Observation observation = null;
	private UTMS utm = null;
	private String lieu_dit = "";
	private Commune commune = null;
	private Calendar date_min = null;
	private Calendar date = null;
	private Espece espece = null;
	private Integer nombre = null;
	private StadeSexe stade_sexe = null;
	private Membre[] temoins;
	private String determinateur = "";
	private String memo = "";

	/**
	 * Instancie la classe pour charger les informations dans une Row
	 * @param row
	 * @param rowNumber
	 * @param errorReport
	 */
	public RowCheckEdit(Row row, int rowNumber, StringBuilder errorReport, Calendar creationTime){
		this.row = row;
		this.rowNumber=rowNumber;
		this.errorReport=errorReport;
		this.creationTime=creationTime;
	}

	/**
	 * Vérifie que la ligne donnée est juste.
	 */
	public void checkRow(){
		//ID
		Cell cell = row.getCell(0);
		double info_id = -1;
		if(cell!=null && (info_id=cell.getNumericCellValue())>0)
			complement = InformationsComplementaires.find.byId((long) info_id);
		if(info_id!=0 && complement==null)
			addError("ID information complémentaire inexistante "+info_id);
		//ID observation
		cell = row.getCell(1);
		if(cell!=null && (info_id=cell.getNumericCellValue())>0)
			observation = Observation.find.byId((long) info_id);
		if(observation==null && complement==null)
			addError("ID observation inexistante "+info_id);
		//UTM
		cell = row.getCell(3);
		String utm_str = null;
		if(cell!=null){
			utm_str = cell.getStringCellValue();
			utm = UTMS.find.byId(utm_str);
		}
		if(utm==null)
			addError("Maille UTM inexistante : "+utm_str);
		//Lieu-dit
		cell = row.getCell(4);
		if(cell!=null)
			this.lieu_dit=cell.getStringCellValue();
		//Commune
		cell = row.getCell(5);
		if(cell!=null){
			String commune_nom = cell.getStringCellValue();
			if(!commune_nom.isEmpty()){
				commune = Commune.findFromNomApproximatif(commune_nom);
				if(commune==null)
					addError("La commune '"+commune_nom+"' n'est pas référencée.");
			}
		}
		//Date_min
		cell = row.getCell(6);
		if(cell!=null){
			try{
				String date_min_str = cell.getStringCellValue();
				if(date_min_str!=null && !date_min_str.isEmpty()){
					date_min = DateUtil.toCalendarExcel(date_min_str);
				}
			}catch(ParseException e){
				addError("Date min invalide");
			}
		}
		//Date
		cell = row.getCell(7);
		if(cell!=null){
			try{
				String date_str = cell.getStringCellValue();
				if(date_str!=null){
					date = DateUtil.toCalendarExcel(date_str);
				}
			}catch(ParseException e){
				addError("Date invalide");
			}
		}
		if(date==null)
			addError("La date est vide !");
		//Espèce
		cell = row.getCell(8);
		if(cell==null)
			addError("Pas d'espèce.");
		else{
			String espece_nom = cell.getStringCellValue();
			if((espece=Espece.find.where().eq("espece_nom", espece_nom).findUnique())==null){
				try{
					EspeceSynonyme syn = EspeceSynonyme.find.where().eq("synonyme_nom", espece_nom).findUnique();
					if(syn!=null)
						espece=syn.synonyme_espece;
					else
						addError("L'espèce "+espece_nom+" n'existe pas.");
				}catch(PersistenceException e){
					addError("Deux espèces synonymes ont le même nom : "+espece_nom);
				}
			}
		}
		//Nombre
		cell = row.getCell(9);
		if(cell!=null){
			try{
				this.nombre = (int) cell.getNumericCellValue();
			}catch(IllegalStateException  e){
				this.nombre = null;
			}catch(NumberFormatException e){
				this.nombre = null;
			}
			this.nombre = (this.nombre!=null && this.nombre==0) ? null : nombre;
		}
		//Stade
		cell = row.getCell(10);
		if(cell!=null){
			String sexe = cell.getStringCellValue();
			if(sexe!=null && !sexe.equals("")){
				if(sexe.equals("oeuf") || sexe.equals("OEuf") || sexe.equals("Oeuf"))
					stade_sexe=StadeSexe.find.byId(6);
				else{
					if((stade_sexe=StadeSexe.find.where().eq("stade_sexe_intitule",sexe).findUnique())==null)
						addError("Le stade/sexe "+sexe+" n'existe pas.");
					if(stade_sexe!=null && espece!=null && !espece.getGroupe().getStadesSexes().contains(stade_sexe)){
						addError("Le stade/sexe "+stade_sexe+" n'est pas valable pour le groupe "+espece.getGroupe());
					}
				}
			}
		}
		//Témoin(s)
		cell = row.getCell(11);
		if(cell==null)
			addError("Témoin non spécifié.");
		else{
			String temoins_str=cell.getStringCellValue();
			if(temoins_str==null)
				addError("Témoin non spécifié.");
			else{
				String[] temoins_str_tab = temoins_str.split(",");
				temoins = new Membre[temoins_str_tab.length];
				for(int i = 0; i<temoins_str_tab.length; i++){
					temoins_str_tab[i]=temoins_str_tab[i].trim();
					temoins[i]=Membre.find.where().eq("membre_nom", temoins_str_tab[i]).findUnique();
					if(temoins[i]==null)
						addError("Le membre '"+temoins_str_tab[i]+"' n'est pas référencé.");
				}
			}
		}
		//Déterminateur
		cell = row.getCell(12);
		if(cell!=null)
			determinateur = cell.getStringCellValue();
		//Mémo
		cell = row.getCell(13);
		try{
			if(cell!=null)
				memo = cell.getStringCellValue();
		}catch(IllegalStateException e){
			addError("Le champ mémo n'est pas une chaîne de caractères");
		}
	}

	/**
	 * Ajoute une erreur dans la liste des erreurs
	 * @param s
	 */
	public void addError(String s){
		noError=false;
		errorReport.append("Ligne "+(rowNumber+1)+": ");
		errorReport.append(s+"<br>");
	}

	public boolean noError(){
		return noError;
	}
	public String getErrors(){
		return errorReport.toString();
	}

	/**
	 * Sauvegarde la Row dans la base de données.
	 */
	public void saveToDatabase() {
		Observation o;
		if(this.complement!=null)
			o = this.complement.informations_complementaires_observation;
		else
			o = this.observation;
		if(complement!=null){
			Fiche f = o.observation_fiche;
			f.fiche_utm=this.utm;
			f.fiche_lieudit=this.lieu_dit;
			f.fiche_commune=this.commune;
			f.fiche_date_min=this.date_min;
			f.fiche_date=this.date;
			f.fiche_memo=this.memo;
			f.update();
			o.observation_espece=this.espece;
			o.observation_determinateur=this.determinateur;
			o.observation_date_derniere_modification=this.creationTime;
			o.observation_date_validation=this.creationTime;
			o.update();
			for(FicheHasMembre fhm : FicheHasMembre.find.where().eq("fiche", f).findList())
				fhm.delete();
			for(Membre temoin : temoins){
				new FicheHasMembre(temoin,f).save();
			}
		}
		if(complement!=null){
			complement.informations_complementaires_nombre_de_specimens=this.nombre;
			complement.informations_complementaires_stade_sexe=this.stade_sexe;
			complement.update();
		}else{
			new InformationsComplementaires(o, nombre, stade_sexe).save();
		}
	}
}
