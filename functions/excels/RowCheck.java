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

public class RowCheck {
	public StringBuilder errorReport;
	private boolean noError = true;

	private Row row;
	private int rowNumber;
	private String espece_nom;
	private Espece espece;
	private String sexe;
	private StadeSexe stade_sexe = null;
	private double nombre_dbl;
	private Integer nombre = null;
	@SuppressWarnings("unused")
	private String departement;
	private String commune_nom;
	private Commune commune = null;
	private String lieu_dit;
	private String utm_str;
	private UTMS utm;
	private Calendar date = Calendar.getInstance();
	private Calendar date_min = null;
	private String[] temoins;
	private Membre[] membres;
	private String determinateur;
	private String methodeCapture;
	private String milieu;
	private String essence;
	private String remarque;
	private String collection;
	private String memo = null;

	/**
	 * Instancie la classe pour charger les informations dans une Row
	 * @param row
	 * @param rowNumber
	 * @param errorReport
	 */
	public RowCheck(Row row, int rowNumber, StringBuilder errorReport){
		this.row = row;
		this.rowNumber=rowNumber;
		this.errorReport=errorReport;
	}

	/**
	 * Vérifie que la ligne donnée est juste.
	 */
	public void checkRow(){
		Cell cell = row.getCell(0);
		if(cell==null)
			addError("Pas d'espèce.");
		else{
			espece_nom = cell.getStringCellValue();
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
		cell = row.getCell(1);
		if(cell!=null){
			sexe = cell.getStringCellValue();
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
		cell = row.getCell(2);
		if(cell!=null){
			try{
				nombre_dbl = cell.getNumericCellValue();
				if(nombre_dbl!=0){
					nombre = (int) nombre_dbl;
				}
			}catch(IllegalStateException e){
				addError(nombre_dbl+" n'est pas un entier.");
			}catch( NumberFormatException e){
				addError(nombre_dbl+" n'est pas un entier.");
			}
		}
		cell = row.getCell(3);//Département, on s'en fout.
		cell = row.getCell(4);
		if(cell!=null){
			commune_nom = cell.getStringCellValue();
			if(commune_nom!=null && !commune_nom.equals("")){
				commune = Commune.findFromNomApproximatif(commune_nom);
				if(commune==null)
					addError("La commune "+commune_nom+" n'est pas référencée.");
			}
		}
		cell = row.getCell(5);
		lieu_dit=cell.getStringCellValue();
		cell = row.getCell(6);
		if(cell==null)
			addError("Maille UTM non spécifiée.");
		else{
			utm_str = cell.getStringCellValue();
			utm = UTMS.find.byId(utm_str);
			if(utm==null)
				addError("Maille UTM "+utm_str+" non existante.");
		}
		//Date_min
		cell = row.getCell(7);
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
		cell = row.getCell(8);
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
		//Témoins
		cell = row.getCell(9);
		if(cell==null)
			addError("Témoin non spécifié.");
		else{
			String temoins_str=cell.getStringCellValue();
			if(temoins_str==null)
				addError("Témoin non spécifié.");
			else{
				temoins = temoins_str.split(",");
				membres = new Membre[temoins.length];
				for(int i = 0; i<temoins.length; i++){
					temoins[i]=temoins[i].trim();
					membres[i]=Membre.find.where().eq("membre_nom", temoins[i]).findUnique();
					if(membres[i]==null)
						addError("Le membre '"+temoins[i]+"' n'est pas référencé.");
				}
			}
		}
		cell = row.getCell(10);
		if(cell!=null)
			determinateur=cell.getStringCellValue();
		cell = row.getCell(11);
		if(cell!=null)
			methodeCapture=cell.getStringCellValue();
		else
			methodeCapture=null;
		cell = row.getCell(12);
		if(cell!=null)
			milieu=cell.getStringCellValue();
		else
			milieu=null;
		cell = row.getCell(13);
		if(cell!=null)
			essence=cell.getStringCellValue();
		else
			essence=null;
		cell = row.getCell(14);
		if(cell!=null)
			remarque=cell.getStringCellValue();
		else
			remarque=null;
		cell = row.getCell(15);
		if(cell!=null)
			collection=cell.getStringCellValue();
		else
			collection=null;

		StringBuilder memo_sb = new StringBuilder();
		boolean started = false;
		if(remarque!=null && !remarque.equals("")){
			if(!started){
				started=true;
				memo_sb.append(remarque);
			}else
				memo_sb.append(" ; "+remarque);
		}
		if(methodeCapture!=null && !methodeCapture.equals("")){
			if(!started){
				started=true;
				memo_sb.append("Méthode de capture : "+methodeCapture);
			}else
				memo_sb.append(" ; Méthode de capture : "+methodeCapture);
		}
		if(milieu!=null && !milieu.equals("")){
			if(!started){
				started=true;
				memo_sb.append("Milieu : "+milieu);
			}else
				memo_sb.append(" ; Milieu : "+milieu);
		}
		if(essence!=null && !essence.equals("")){
			if(!started){
				started=true;
				memo_sb.append("Essence : "+essence);
			}else
				memo_sb.append(" ; Essence : "+essence);
		}
		if(collection!=null && !collection.equals("")){
			if(!started){
				started=true;
				memo_sb.append("Collection : "+collection);
			}else
				memo_sb.append(" ; Collection : "+collection);
		}
		memo = memo_sb.toString();
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
		Fiche fiche = new Fiche(commune,lieu_dit,utm,date_min,date,memo);
		fiche.save();
		Observation observation = new Observation(fiche,espece,determinateur,null);
		observation.observation_date_validation=Calendar.getInstance();
		observation.observation_vue_par_expert=true;
		observation.observation_validee=Observation.VALIDEE;
		observation.save();
		new InformationsComplementaires(observation,nombre,stade_sexe).save();
		for(Membre membre : membres){
			new FicheHasMembre(membre,fiche).save();
		}
	}
}
