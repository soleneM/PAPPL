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

import controllers.ajax.expert.requetes.Calculs;
import models.Espece;
import models.Groupe;
import models.InformationsComplementaires;
import models.Observation;
import models.StadeSexe;
import models.UTMS;

public class MaillesParPeriode {
	
	public HashMap<Espece,Integer> nb_mailles_par_espece;
	
	public MaillesParPeriode(Map<String, String> info) throws ParseException {
		Espece espece = Espece.find.byId(Integer.parseInt(info.get("espece")));
		Groupe sous_groupe = Groupe.find.byId(Integer.parseInt(info.get("sous_groupe")));
		Groupe groupe = Groupe.find.byId(Integer.parseInt(info.get("groupe")));
		StadeSexe stade_sexe = StadeSexe.find.byId(Integer.parseInt(info.get("stade")));
		List<StadeSexe> stades_sexes;
		if(stade_sexe!=null){
			stades_sexes=new ArrayList<StadeSexe>();
			stades_sexes.add(stade_sexe);
			stades_sexes.addAll(stade_sexe.getStadeSexeFilsPourTelGroupe(groupe));
		}else{
			stades_sexes=StadeSexe.findAll();
		}
		Calendar date1 = Calculs.getDataDate1(info);
		Calendar date2 = Calculs.getDataDate2(info);
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
		nb_mailles_par_espece = new HashMap<Espece,Integer>();
		for(Espece especeATraiter : especesATraiter){
			int n = 0;
			for(UTMS utm : UTMS.findAll()){
				if(InformationsComplementaires.find.where()
						.eq("informations_complementaires_observation.observation_validee", Observation.VALIDEE)
						.between("informations_complementaires_observation.observation_fiche.fiche_date", date1.getTime(), date2.getTime())
						.eq("informations_complementaires_observation.observation_fiche.fiche_utm", utm)
						.in("informations_complementaires_stade_sexe", stades_sexes)
						.eq("informations_complementaires_observation.observation_espece", especeATraiter)
						.findRowCount() > 0 )
					n++;
			}
			nb_mailles_par_espece.put(especeATraiter, n);
		}
	}

}
