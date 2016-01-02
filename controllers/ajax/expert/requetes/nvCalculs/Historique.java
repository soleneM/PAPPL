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
package controllers.ajax.expert.requetes.nvCalculs;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Espece;
import models.Fiche;
import models.Groupe;
import models.InformationsComplementaires;
import models.Observation;
import models.StadeSexe;
import models.UTMS;

import controllers.ajax.expert.requetes.Calculs;


public class Historique {

	public Map<Integer,String> legende = new HashMap<Integer,String>();
	
	public String titre;
	public int nbTemoignagesRejetes=0;
	public int min=0;
	public int max=0;
	public int[] histogramme;
		
	public Historique(Map<String,String> info) throws ParseException {
		
		//Génération de histogramme
		List<InformationsComplementaires> complements = getInformationsComplementaires(info);
		
		Calendar date1 = Calculs.getDataDate1(info);
		Calendar date2 = Calculs.getDataDate2(info);
		
		min= date1.get(Calendar.YEAR);
		max = date2.get(Calendar.YEAR);
		
		int diff = 0;
		if((max-min) % 20 == 0){
			diff = (max-min)/20;
		}else{
			diff = ((max-min)/20 + 1);
		}
		
		histogramme = new int[diff];
	
		int temp = min;
		int temp1 = min + 20;
		
		for(int i = 0; i<diff ; i++){
			legende.put(i, temp+"-"+temp1);
			histogramme[i]=0;
			temp = temp1;
			temp1 = temp1 + 20;
		}
			histogramme[diff]=0;
			legende.put(diff, "avant "+max);
				
		
		
		
		for(InformationsComplementaires complement : complements){
			Fiche fiche = complement.informations_complementaires_observation.observation_fiche;
			int periode = getPeriode(fiche.fiche_date_min,fiche.fiche_date, min, max);
			if(periode==-1)
				nbTemoignagesRejetes++;
			else
				histogramme[periode]++;
		}
		
		//Génération du titre
		Espece espece = Espece.find.byId(Integer.parseInt(info.get("espece")));
		Groupe sous_groupe = Groupe.find.byId(Integer.parseInt(info.get("sous_groupe")));
		Groupe groupe = Groupe.find.byId(Integer.parseInt(info.get("groupe")));
		String maille = info.get("maille");
		titre = "Historique des temoignages";
		if(espece!=null)
			titre+=" de "+espece.espece_nom;
		else if(sous_groupe!=null)
			titre+=" de "+sous_groupe;
		else if(groupe!=null)
			titre+=" de "+groupe;
		if(!maille.equals(""))
			titre+=" dans la maille "+maille;
	}
	
	/**
	 * Renvoie la période où l'observation a eu lieu. Si c'est à cheval sur deux périodes ou en dehors de la période recherchée,
	 * renvoie -1.
	 * @param date_min
	 * @param date
	 * @return
	 */
	public int getPeriode(Calendar date_min, Calendar date, int min, int max){
		if(date_min==null){
			int year = date.get(Calendar.YEAR);
			return getPeriodeUneAnnee(year, min, max);
		}else{
			int year_min = date_min.get(Calendar.YEAR);
			int year = date.get(Calendar.YEAR);
			int periode;
			if(year_min==year)
				return getPeriodeUneAnnee(year, min, max);
			else if( (periode=getPeriodeUneAnnee(year, min, max)) != getPeriodeUneAnnee(year_min, min, max) )
				return -1;
			else
				return periode;
		}
	}
	
	private int getPeriodeUneAnnee(int year, int min, int max) {
		if(year>max)
			return -1;
		else if(year < min)
			return -1;
		else 
			return ((year-min)/20);
			
	}

	/**
	 * Renvoie les informations complémentaires pour afficher histogramme de l'historique des espèces
	 * @param info
	 * @return
	 * @throws ParseException
	 */
	public static List<InformationsComplementaires> getInformationsComplementaires(Map<String,String> info) throws ParseException{
		Espece espece = Espece.find.byId(Integer.parseInt(info.get("espece")));
		Groupe sous_groupe = Groupe.find.byId(Integer.parseInt(info.get("sous_groupe")));
		Groupe groupe = Groupe.find.byId(Integer.parseInt(info.get("groupe")));
		List<UTMS> mailles = UTMS.parseMaille(info.get("maille"));
		StadeSexe stade_sexe = StadeSexe.find.byId(Integer.parseInt(info.get("stade")));
		List<StadeSexe> stades_sexes;
		if(stade_sexe!=null){
			stades_sexes=new ArrayList<StadeSexe>();
			stades_sexes.add(stade_sexe);
			stades_sexes.addAll(stade_sexe.getStadeSexeFilsPourTelGroupe(groupe));
		}else{
			stades_sexes=StadeSexe.findAll();
		}
		List<Espece> especesATraiter;
		if(espece!=null){
			especesATraiter = new ArrayList<Espece>();
			especesATraiter.add(espece);
		}else if(sous_groupe!=null){
			especesATraiter = sous_groupe.getEspecesInThis();
		}else if(groupe!=null){
			especesATraiter = groupe.getAllEspecesInThis();
		}else{
			especesATraiter = Espece.findAll();
		}
		return InformationsComplementaires.find.where()
				.eq("informations_complementaires_observation.observation_validee", Observation.VALIDEE)
				.in("informations_complementaires_observation.observation_espece",especesATraiter)
				.in("informations_complementaires_observation.observation_fiche.fiche_utm", mailles)
				.in("informations_complementaires_stade_sexe", stades_sexes)
				.findList();
	}
	
	/**
	 * Calcule le nombre de témoignages en tout
	 * @return
	 */
	public int getSomme(){
		int somme = 0;
		for(int i = 0 ; i<this.histogramme.length ; i++){
			somme+=this.histogramme[i];
		}
		return somme;
	}
}
