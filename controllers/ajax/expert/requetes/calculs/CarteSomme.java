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

import com.avaje.ebean.Expr;

import controllers.ajax.expert.requetes.Calculs;
import models.Espece;
import models.Groupe;
import models.InformationsComplementaires;
import models.Observation;
import models.StadeSexe;
import models.UTMS;

/**
 * Donne le nombre d'observations dans chaque maille
 * @author malik
 *
 */
public class CarteSomme {

	public HashMap<UTMS,Integer> carte = new HashMap<UTMS,Integer>();

	public CarteSomme(Map<String,String> info) throws ParseException {
		calcul(info);
	}

	/**
	 * 
	 * @param info
	 * @return
	 * @throws ParseException
	 */
	public void calcul(Map<String,String> info) throws ParseException{
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
		for(UTMS utm : UTMS.findAll()){
			List<InformationsComplementaires> complements;
			if(espece!=null){
				complements = InformationsComplementaires.find.where()
						.eq("informations_complementaires_observation.observation_espece",espece)
						.eq("informations_complementaires_observation.observation_validee", Observation.VALIDEE)
						.or(Expr.and(
								Expr.isNull("informations_complementaires_observation.observation_fiche.fiche_date_min"),
								Expr.between("informations_complementaires_observation.observation_fiche.fiche_date", date1.getTime(), date2.getTime())
								),
								Expr.and(
										Expr.ge("informations_complementaires_observation.observation_fiche.fiche_date_min", date1.getTime()),
										Expr.le("informations_complementaires_observation.observation_fiche.fiche_date", date2.getTime())
										)
								)
								.eq("informations_complementaires_observation.observation_fiche.fiche_utm", utm)
								.in("informations_complementaires_stade_sexe", stades_sexes)
								.findList();
			}else if(sous_groupe!=null){
				complements = InformationsComplementaires.find.where()
						.eq("informations_complementaires_observation.observation_espece.espece_sous_groupe",sous_groupe)
						.eq("informations_complementaires_observation.observation_validee", Observation.VALIDEE)
						.or(Expr.and(
								Expr.isNull("informations_complementaires_observation.observation_fiche.fiche_date_min"),
								Expr.between("informations_complementaires_observation.observation_fiche.fiche_date", date1.getTime(), date2.getTime())
								),
								Expr.and(
										Expr.ge("informations_complementaires_observation.observation_fiche.fiche_date_min", date1.getTime()),
										Expr.le("informations_complementaires_observation.observation_fiche.fiche_date", date2.getTime())
										)
								)
								.in("informations_complementaires_observation.observation_fiche.fiche_utm", utm)
								.in("informations_complementaires_stade_sexe", stades_sexes)
								.findList();
			}else if(groupe!=null){
				complements = InformationsComplementaires.find.where()
						.eq("informations_complementaires_observation.observation_espece.espece_sous_groupe.sous_groupe_groupe",groupe)
						.eq("informations_complementaires_observation.observation_validee", Observation.VALIDEE)
						.or(Expr.and(
								Expr.isNull("informations_complementaires_observation.observation_fiche.fiche_date_min"),
								Expr.between("informations_complementaires_observation.observation_fiche.fiche_date", date1.getTime(), date2.getTime())
								),
								Expr.and(
										Expr.ge("informations_complementaires_observation.observation_fiche.fiche_date_min", date1.getTime()),
										Expr.le("informations_complementaires_observation.observation_fiche.fiche_date", date2.getTime())
										)
								)
								.in("informations_complementaires_observation.observation_fiche.fiche_utm", utm)
								.in("informations_complementaires_stade_sexe", stades_sexes)
								.findList();
			}else{
				complements = InformationsComplementaires.find.where()
						.eq("informations_complementaires_observation.observation_validee", Observation.VALIDEE)
						.or(Expr.and(
								Expr.isNull("informations_complementaires_observation.observation_fiche.fiche_date_min"),
								Expr.between("informations_complementaires_observation.observation_fiche.fiche_date", date1.getTime(), date2.getTime())
								),
								Expr.and(
										Expr.ge("informations_complementaires_observation.observation_fiche.fiche_date_min", date1.getTime()),
										Expr.le("informations_complementaires_observation.observation_fiche.fiche_date", date2.getTime())
										)
								)
								.in("informations_complementaires_observation.observation_fiche.fiche_utm", utm)
								.in("informations_complementaires_stade_sexe", stades_sexes)
								.findList();
			}
			carte.put(utm, complements.size());
		}
	}

	public int getSomme() {
		int somme = 0;
		for(UTMS utm : this.carte.keySet()){
			somme+=carte.get(utm);
		}
		return somme;
	}

}
