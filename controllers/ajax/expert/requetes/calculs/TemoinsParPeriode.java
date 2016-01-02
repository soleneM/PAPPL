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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.Expr;

import controllers.ajax.expert.requetes.Calculs;
import models.Espece;
import models.FicheHasMembre;
import models.Groupe;
import models.InformationsComplementaires;
import models.Membre;
import models.Observation;
import models.StadeSexe;
import models.UTMS;

public class TemoinsParPeriode implements Comparator<TemoinsParPeriode>{
	public Membre temoin;
	public int nombreDeTemoignages;

	public TemoinsParPeriode(Membre temoin, Integer nombreDeTemoignage){
		this.temoin=temoin;
		this.nombreDeTemoignages=nombreDeTemoignage;
	}

	@Override
	public String toString(){
		return temoin.membre_nom+" : "+nombreDeTemoignages;
	}

	/**
	 * Trie par ordre décroissant de nombre de témoignages
	 */
	@Override
	public int compare(TemoinsParPeriode t1, TemoinsParPeriode t2) {
		return t1.temoin.membre_nom.compareToIgnoreCase(t2.temoin.membre_nom);
	}
	private TemoinsParPeriode(){}


	public static List<TemoinsParPeriode> calculeTemoinsParPeriode(Map<String,String> info) throws ParseException {
		List<TemoinsParPeriode> temoins = new ArrayList<TemoinsParPeriode>();
		List<InformationsComplementaires> complements = TemoinsParPeriode.getObservations(info);
		//On commence la génération des témoins par période.
		for(InformationsComplementaires complement : complements){
			List<FicheHasMembre> fhms = FicheHasMembre.find.where().eq("fiche", complement.informations_complementaires_observation.getFiche()).findList();
			for(FicheHasMembre fhm : fhms){
				int position;
				if((position=position(fhm.membre,temoins))==-1){
					temoins.add(new TemoinsParPeriode(fhm.membre,1));
				}else{
					temoins.get(position).nombreDeTemoignages++;
				}
			}
		}
		Collections.sort(temoins,new TemoinsParPeriode());
		return temoins;
	}

	/**
	 * Renvoie la position du membre dans la liste temoins
	 * @param membre
	 * @param temoins
	 * @return
	 */
	private static int position(Membre membre, List<TemoinsParPeriode> temoins) {
		int i = 0;
		while(i<temoins.size()){
			if(temoins.get(i).temoin.equals(membre))
				return i;
			i++;
		}
		return -1;
	}
	
	/**
	 * Renvoie les observations pour afficher temoins par période
	 * @param info
	 * @return
	 * @throws ParseException
	 */
	public static List<InformationsComplementaires> getObservations(Map<String,String> info) throws ParseException{
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
		List<UTMS> mailles = UTMS.parseMaille(info.get("maille"));
		Calendar date1 = Calculs.getDataDate1(info);
		Calendar date2 = Calculs.getDataDate2(info);
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
								.in("informations_complementaires_observation.observation_fiche.fiche_utm", mailles)
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
								.in("informations_complementaires_observation.observation_fiche.fiche_utm", mailles)
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
								.in("informations_complementaires_observation.observation_fiche.fiche_utm", mailles)
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
					.in("informations_complementaires_observation.observation_fiche.fiche_utm", mailles)
					.in("informations_complementaires_stade_sexe", stades_sexes)
					.findList();
		}
		return complements;
	}
	
	/**
	 * Renvoie le nombre total de témoignage
	 * @param tpps
	 * @return
	 */
	public static int getSomme(List<TemoinsParPeriode> tpps){
		int somme = 0;
		for(TemoinsParPeriode tpp : tpps){
			somme+=tpp.nombreDeTemoignages;
		}
		return somme;
	}
}
