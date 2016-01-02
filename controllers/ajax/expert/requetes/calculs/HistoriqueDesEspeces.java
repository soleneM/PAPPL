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
package controllers.ajax.expert.requetes.calculs;

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

public class HistoriqueDesEspeces {

	public Map<Integer,String> legende = new HashMap<Integer,String>();
	public int[] histogramme = new int[13];
	public String titre;
	public int nbTemoignagesRejetes=0;
	
	public HistoriqueDesEspeces(Map<String,String> info) throws ParseException {
		legende.put(0, "Avant 1901"); 
		legende.put(1, "1901-1910"); 
		legende.put(2, "1911-1920"); 
		legende.put(3, "1921-1930"); 
		legende.put(4, "1931-1940"); 
		legende.put(5, "1941-1950"); 
		legende.put(6, "1951-1960"); 
		legende.put(7, "1961-1970"); 
		legende.put(8, "1971-1980"); 
		legende.put(9, "1981-1990"); 
		legende.put(10, "1991-2000"); 
		legende.put(11, "2001-2010"); 
		legende.put(12, "Depuis 2011"); 
		for(int i = 0; i<13 ; i++)
			histogramme[i]=0;
		
		//Génération de histogramme
		List<InformationsComplementaires> complements = getInformationsComplementaires(info);
		for(InformationsComplementaires complement : complements){
			Fiche fiche = complement.informations_complementaires_observation.observation_fiche;
			int periode = getPeriode(fiche.fiche_date_min,fiche.fiche_date);
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
		titre = "Historique des espèces";
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
	 * Renvoie la période où l'observation a eu lieu. Si c'est à cheval sur deux périodes,
	 * renvoie -1.
	 * @param date_min
	 * @param date
	 * @return
	 */
	public int getPeriode(Calendar date_min, Calendar date){
		if(date_min==null){
			int year = date.get(Calendar.YEAR);
			return getPeriodeUneAnnee(year);
		}else{
			int year_min = date_min.get(Calendar.YEAR);
			int year = date.get(Calendar.YEAR);
			int periode;
			if(year_min==year)
				return getPeriodeUneAnnee(year);
			else if( (periode=getPeriodeUneAnnee(year)) != getPeriodeUneAnnee(year_min) )
				return -1;
			else
				return periode;
		}
	}
	
	private int getPeriodeUneAnnee(int year) {
		if(year>2010)
			return 12;
		else if(year>2000)
			return 11;
		else if(year>1990)
			return 10;
		else if(year>1980)
			return 9;
		else if(year>1970)
			return 8;
		else if(year>1960)
			return 7;
		else if(year>1950)
			return 6;
		else if(year>1940)
			return 5;
		else if(year>1930)
			return 4;
		else if(year>1920)
			return 3;
		else if(year>1910)
			return 2;
		else if(year>1900)
			return 1;
		else
			return 0;
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
	 * Calcule le nombre d'observations en tout
	 * @return
	 */
	public int getSomme(){
		int somme = 0;
		for(int i = 0 ; i<13 ; i++){
			somme+=this.histogramme[i];
		}
		return somme;
	}
}
