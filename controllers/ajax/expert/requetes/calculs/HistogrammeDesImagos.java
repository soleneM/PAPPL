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
import models.Groupe;
import models.InformationsComplementaires;
import models.Observation;
import models.StadeSexe;
import models.UTMS;
import controllers.ajax.expert.requetes.Calculs;

public class HistogrammeDesImagos {

	public Map<Integer,String> legende = new HashMap<Integer,String>();
	public int[] histogramme = new int[36];
	public String titre;
	
	public HistogrammeDesImagos(Map<String,String> info) throws ParseException {
		legende.put(0, "Début Janvier"); 
		legende.put(1, "Mi-Janvier"); 
		legende.put(2, "Fin Janvier"); 
		legende.put(3, "Début Février"); 
		legende.put(4, "Mi-Février"); 
		legende.put(5, "Fin Février"); 
		legende.put(6, "Début Mars"); 
		legende.put(7, "Mi-Mars"); 
		legende.put(8, "Fin Mars"); 
		legende.put(9, "Début Avril"); 
		legende.put(10, "Mi-Avril"); 
		legende.put(11, "Fin Avril"); 
		legende.put(12, "Début Mai"); 
		legende.put(13, "Mi-Mai"); 
		legende.put(14, "Fin Mai"); 
		legende.put(15, "Début Juin"); 
		legende.put(16, "Mi-Juin"); 
		legende.put(17, "Fin Juin"); 
		legende.put(18, "Début Juillet"); 
		legende.put(19, "Mi-Juillet"); 
		legende.put(20, "Fin Juillet"); 
		legende.put(21, "Début Août"); 
		legende.put(22, "Mi-Août"); 
		legende.put(23, "Fin Août"); 
		legende.put(24, "Début Septembre"); 
		legende.put(25, "Mi-Septembre"); 
		legende.put(26, "Fin Septembre"); 
		legende.put(27, "Début Octobre"); 
		legende.put(28, "Mi-Octobre"); 
		legende.put(29, "Fin Octobre"); 
		legende.put(30, "Début Novembre"); 
		legende.put(31, "Mi-Novembre"); 
		legende.put(32, "Fin Novembre"); 
		legende.put(33, "Début Décembre"); 
		legende.put(34, "Mi-Décembre"); 
		legende.put(35, "Fin Décembre");
		for(int i = 0; i<36 ; i++)
			histogramme[i]=0;
		
		//Génération de histogramme
		List<InformationsComplementaires> complements = getInformationsComplementaires(info);
		for(InformationsComplementaires complement : complements){
			int periode = getPeriodeDeLAnnee(complement.informations_complementaires_observation.observation_fiche.fiche_date);
			Integer nombreSpecimens = complement.informations_complementaires_nombre_de_specimens;
			if(nombreSpecimens==null)
				histogramme[periode]++;
			else
				histogramme[periode]+=nombreSpecimens;
		}
		
		//Génération du titre
		Espece espece = Espece.find.byId(Integer.parseInt(info.get("espece")));
		Groupe sous_groupe = Groupe.find.byId(Integer.parseInt(info.get("sous_groupe")));
		Groupe groupe = Groupe.find.byId(Integer.parseInt(info.get("groupe")));
		String maille = info.get("maille");
		String date1 = info.get("jour1")+"/"+info.get("mois1")+"/"+info.get("annee1");
		String date2 = info.get("jour2")+"/"+info.get("mois2")+"/"+info.get("annee2");
		StadeSexe stade_sexe = StadeSexe.find.byId(Integer.parseInt(info.get("stade")));
		titre = "Histogramme des observations ";
		if(espece!=null)
			titre+=" de "+espece.espece_nom;
		else if(sous_groupe!=null)
			titre+=" de "+sous_groupe;
		else if(groupe!=null)
			titre+=" de "+groupe;
		if(!maille.equals(""))
			titre+=" dans la maille "+maille;
		if(stade_sexe==null)
			titre+=" à tous les stades";
		else
			titre+=" au stade "+stade_sexe.stade_sexe_intitule;
		titre+=" du "+date1+" au "+date2;
	}
	
	public static int getPeriodeDeLAnnee(Calendar calendar){
		int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR); 
		if(dayOfYear<=10)//janvier
			return 0;
		else if(dayOfYear<=20)
			return 1;
		else if(dayOfYear<=31)
			return 2;
		else if(dayOfYear<=40)//février
			return 3;
		else if(dayOfYear<=49)
			return 4;
		else if(dayOfYear<=59)
			return 5;
		else if(dayOfYear<=69)//mars
			return 6;
		else if(dayOfYear<=79)
			return 7;
		else if(dayOfYear<=90)
			return 8;
		else if(dayOfYear<=100)//avril
			return 9;
		else if(dayOfYear<=110)
			return 10;
		else if(dayOfYear<=120)
			return 11;
		else if(dayOfYear<=130)//mai
			return 12;
		else if(dayOfYear<=140)
			return 13;
		else if(dayOfYear<=151)
			return 14;
		else if(dayOfYear<=161)//juin
			return 15;
		else if(dayOfYear<=171)
			return 16;
		else if(dayOfYear<=181)
			return 17;
		else if(dayOfYear<=191)//juillet
			return 18;
		else if(dayOfYear<=201)
			return 19;
		else if(dayOfYear<=212)
			return 20;
		else if(dayOfYear<=222)//août
			return 21;
		else if(dayOfYear<=232)
			return 22;
		else if(dayOfYear<=243)
			return 23;
		else if(dayOfYear<=253)//septembre
			return 24;
		else if(dayOfYear<=263)
			return 25;
		else if(dayOfYear<=273)
			return 26;
		else if(dayOfYear<=283)//octobre
			return 27;
		else if(dayOfYear<=293)
			return 28;
		else if(dayOfYear<=304)
			return 29;
		else if(dayOfYear<=314)//novembre
			return 30;
		else if(dayOfYear<=324)
			return 31;
		else if(dayOfYear<=334)
			return 32;
		else if(dayOfYear<=344)//décembre
			return 33;
		else if(dayOfYear<=354)
			return 34;
		else
			return 35;
	}
	
	/**
	 * Renvoie les informations complémentaires pour afficher histogramme des imagos
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
		Calendar date1 = Calculs.getDataDate1(info);
		Calendar date2 = Calculs.getDataDate2(info);
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
						.in("informations_complementaires_observation.observation_espece",especesATraiter)
						.eq("informations_complementaires_observation.observation_validee", Observation.VALIDEE)
						.isNull("informations_complementaires_observation.observation_fiche.fiche_date_min")
						.between("informations_complementaires_observation.observation_fiche.fiche_date", date1.getTime(), date2.getTime())
						.in("informations_complementaires_observation.observation_fiche.fiche_utm", mailles)
						.in("informations_complementaires_stade_sexe", stades_sexes)
						.findList();
	}
	
	/**
	 * Calcule le nombre d'observation en tout sur l'année
	 * @return
	 */
	public int getSomme(){
		int somme = 0;
		for(int i = 0 ; i<36 ; i++){
			somme+=this.histogramme[i];
		}
		return somme;
	}
}
