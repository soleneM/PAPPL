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
package controllers.expert;


import java.util.List;
import java.util.Calendar;

import controllers.admin.Admin;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.expert.temoignagesAValider;
import views.html.expert.editeTemoignagesAValider;
import models.Commune;
import models.Espece;
import models.Groupe;
import models.InformationsComplementaires;
import models.Observation;
import models.StadeSexe;
import models.UTMS;
import models.*;
import java.util.ArrayList;


/**
 * Gère les fonctions liées à la validation d'observations par les experts.
 * 
 * @author johan
 *
 */

public class ValiderObservations extends Controller {
	
	
	/**
	 * renvoit les témoignages non vus
	 * @return
	 */
	public static Result temoignagesNonVus(Integer groupe_id) {
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe))
    	return ok( temoignagesAValider.render(Observation.nonVus(groupe), groupe));
		else
			return Admin.nonAutorise();
    }
	
	/**
	 * renvoit les témoignages en suspend
	 * @return
	 */
	public static Result temoignagesEnSuspends(Integer groupe_id) {
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe))
    	return ok( temoignagesAValider.render(Observation.enSuspend(groupe),groupe));
		else
			return Admin.nonAutorise();
    }
	
	/**
	 * Permet de charger la page voulue lorsque l'on veut éditer une observation (editeTemoignageAValider)
	 * @param groupe_id
	 * @param observation_id
	 * @return
	 */
     public static Result editeTemoignage( Long observation_id, Integer groupe_id){
    			Groupe groupe = Groupe.find.byId(groupe_id);
    			Observation observation= Observation.find.byId(observation_id);
    			List<UTMS> utms= UTMS.find.all();
    			List<EspeceIsInGroupementLocal> eiigls= EspeceIsInGroupementLocal.find.where().eq("groupe", groupe).findList();
    			List<Espece> especes= new ArrayList<Espece>();
    			for (EspeceIsInGroupementLocal eiigl : eiigls){
    				especes.add(eiigl.espece);
    			}
    			List<StadeSexe> stadessexes=groupe.getStadesSexes();
    			if(MenuExpert.isExpertOn(groupe))
    				return ok(editeTemoignagesAValider.render(observation,utms,especes, stadessexes, groupe_id));
    			else
    				return Admin.nonAutorise();
     }
	
	/**
	 * Permet de marquer comme vue l'obsertvation sélectionnée
	 * @param id
	 * @return
	 */
	public static Result marquerVu(Long id, Integer groupe_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
		Observation observation = Observation.find.byId(id);
		if (observation!=null){
			observation.vu();
		}
		observation.save();
		return redirect("/temoignagesAValider/"+groupe_id);}
		else
			return Admin.nonAutorise();
	}
	
	/**
	 * Permet de marquer comme validé l'observation sélectionnée
	 * @param id
	 * @return
	 */
	public static Result valide(Long id, Integer groupe_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
		Observation observation = Observation.find.byId(id);
		if (observation!=null){
			observation.valider();
			observation.observation_date_validation=Calendar.getInstance();
		}
		observation.save();
		return redirect("/temoignagesAValider/enSuspens/"+groupe_id);}
		else
			return Admin.nonAutorise();
	}
	
	public static Result supprimer(Long id, Integer groupe_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
		Observation observation = Observation.find.byId(id);
		if (observation!=null){
			observation.supprimer();
		}
		observation.save();
		return redirect("/temoignagesAValider/enSuspens/"+groupe_id);}
		else
			return Admin.nonAutorise();
	}
	/**
	 * Permet à un expert de changer les valeurs d'une observation 
	 * @param observation_id
	 * @param groupe_id
	 * @return
	 */
	public static Result editer(Long observation_id, Integer groupe_id) {
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
		DynamicForm df = DynamicForm.form().bindFromRequest();
		Integer espece_id= Integer.parseInt(df.get("espece_id"));
		String commentaire= df.get("commentaire");
		String lieudit= df.get("lieudit");
		String communenom= df.get("ville_nom_reel");
		String utm = df.get("utm");
		String memo = df.get("memo");
		String nouvelleinfo=df.get("infonew");
		Observation observation= Observation.find.byId(observation_id);
		List<InformationsComplementaires> infos= observation.getInfos();
		for(InformationsComplementaires info: infos){
			String id=Long.toString(info.informations_complementaires_id);
			String nombres=df.get("nombre"+id);
			try{
			Integer.parseInt(nombres);}
			catch(NumberFormatException e){
				nombres=null;
			} 
			
			
			if (nombres!=null){
				Integer nombre=Integer.parseInt(nombres);
				info.informations_complementaires_nombre_de_specimens=nombre;
				}else{
					info.informations_complementaires_nombre_de_specimens=null;
				}
	//		Integer stadesexeid=Integer.parseInt(df.get("stadesexe"+id));
	//		if(stadesexeid!=null){
	//		StadeSexe stadesexe= StadeSexe.find.byId(stadesexeid);
	//		info.informations_complementaires_stade_sexe=stadesexe;
	//		}
			info.save();
		}
		if (nouvelleinfo!=null){
			StadeSexe stade=StadeSexe.find.byId(1);
			InformationsComplementaires newinfo = new InformationsComplementaires(observation,0,stade);
			newinfo.save();
		}
		if (observation!=null){
			observation.observation_commentaires=commentaire;
			observation.observation_fiche.fiche_lieudit=lieudit;
			Espece espece= Espece.find.byId(espece_id);
			if (espece!=null){
			observation.observation_espece=espece;
			}
			observation.observation_fiche.fiche_memo=memo;
			if(communenom.isEmpty())
				observation.observation_fiche.fiche_commune=null;
			else{
			Commune commune=Commune.find.where().eq("ville_nom_reel", communenom).findUnique();
				if (commune!=null){
					observation.observation_fiche.fiche_commune=commune;
				}
			}
			UTMS utms=UTMS.find.byId(utm);
			if (utms!=null){
			observation.observation_fiche.fiche_utm=utms;
			}
			observation.observation_fiche.save();
			observation.observation_date_derniere_modification=Calendar.getInstance();
			observation.save();
			}
				return redirect("/temoignagesAValider/enSuspens/"+groupe_id);
		}
		else
			return Admin.nonAutorise();
	}
	

}
