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
import java.util.List;
import java.util.Map;

import models.Espece;
import models.Fiche;
import models.FicheHasMembre;
import models.Groupe;
import models.InformationsComplementaires;
import models.Membre;
import models.Observation;
import models.StadeSexe;
import models.UTMS;
import controllers.ajax.expert.requetes.Calculs;

public class ChronologieDUnTemoin {

	public List<InformationsComplementaires> chronologie = new ArrayList<InformationsComplementaires>();

	public ChronologieDUnTemoin(Map<String,String> info) throws ParseException {
		Espece espece = Espece.find.byId(Integer.parseInt(info.get("espece")));
		Groupe sous_groupe = Groupe.find.byId(Integer.parseInt(info.get("sous_groupe")));
		Groupe groupe = Groupe.find.byId(Integer.parseInt(info.get("groupe")));
		StadeSexe stade_sexe = StadeSexe.find.byId(Integer.parseInt(info.get("stade")));
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
		List<StadeSexe> stades_sexes;
		if(stade_sexe!=null){
			stades_sexes=new ArrayList<StadeSexe>();
			stades_sexes.add(stade_sexe);
			stades_sexes.addAll(stade_sexe.getStadeSexeFilsPourTelGroupe(groupe));
		}else{
			stades_sexes=StadeSexe.findAll();
		}
		List<UTMS> mailles = UTMS.parseMaille(info.get("maille"));
		Membre temoin = Membre.find.where().eq("membre_nom", info.get("temoin")).findUnique();
		List<FicheHasMembre> fhms = FicheHasMembre.find.where().eq("membre", temoin).findList();
		List<Fiche> fiches = new ArrayList<Fiche>();
		for (FicheHasMembre fhm: fhms){
			fiches.add(fhm.fiche);
		}
		Calendar date1 = Calculs.getDataDate1(info);
		Calendar date2 = Calculs.getDataDate2(info);
		chronologie = InformationsComplementaires.find.where()
						.in("informations_complementaires_observation.observation_espece",especesATraiter)
						.eq("informations_complementaires_observation.observation_validee", Observation.VALIDEE)
						.between("informations_complementaires_observation.observation_fiche.fiche_date", date1.getTime(), date2.getTime())
						.in("informations_complementaires_observation.observation_fiche.fiche_utm", mailles)
						.in("informations_complementaires_stade_sexe", stades_sexes)
						.in("informations_complementaires_observation.observation_fiche", fiches)
						.orderBy("informations_complementaires_observation.observation_fiche.fiche_date")
						.findList();
	}

}
