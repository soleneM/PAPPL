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

import controllers.admin.Admin;
import models.Groupe;
import models.StadeSexeHierarchieDansGroupe;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.expert.gererStadeSexe;

public class GererStadeSexe extends Controller{
	/**
	 * Affiche la page de gestion des stades et des sexes
	 * si l'utilisateur est expert dans le groupe donné.
	 * @param groupe_id
	 * @return
	 */
	public static Result main(Integer groupe_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe))
			return ok(gererStadeSexe.render(groupe));
		else
			return Admin.nonAutorise();
	}

	/**
	 * Ajoute un stade/sexe père au groupe donné.
	 * Si le stade est déjà ajouté dans le groupe, ne fait rien.
	 * @param groupe_id
	 * @return
	 */
	public static Result ajouterStadePere(Integer groupe_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			int stade_sexe_id = Integer.parseInt(df.get("stade_sexe"));
			int position = Integer.parseInt(df.get("position"));
			if(StadeSexeHierarchieDansGroupe.nAPas(groupe,stade_sexe_id)){
				StadeSexeHierarchieDansGroupe sshdg = new StadeSexeHierarchieDansGroupe(groupe,stade_sexe_id,position);
				sshdg.save();
			}
			return redirect("/gererstadesexe/"+groupe_id);
		}else
			return Admin.nonAutorise();
	}

	/**
	 * Ajoute un stade/sexe fils au groupe donné.
	 * Si le stade est déjà ajouté dans le groupe, ne fait rien.
	 * @param groupe_id
	 * @return
	 */
	public static Result ajouterStadeFils(Integer groupe_id, Integer stade_sexe_pere_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			int stade_sexe_id = Integer.parseInt(df.get("stade_sexe"));
			int position = Integer.parseInt(df.get("position"));
			if(StadeSexeHierarchieDansGroupe.nAPas(groupe,stade_sexe_id)){
				StadeSexeHierarchieDansGroupe sshdg = new StadeSexeHierarchieDansGroupe(groupe,stade_sexe_id,stade_sexe_pere_id,position);
				sshdg.save();
			}
			return redirect("/gererstadesexe/"+groupe_id);
		}else
			return Admin.nonAutorise();
	}

	/**
	 * Supprime un stade père et tous ces stades fils
	 * @param groupe_id
	 * @param stade_sexe_id
	 * @return
	 */
	public static Result supprimerStadePere(Integer groupe_id, Integer stade_sexe_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
			StadeSexeHierarchieDansGroupe sshdg_pere = 
					StadeSexeHierarchieDansGroupe.find.where()
					.eq("groupe",groupe)
					.eq("stade_sexe.stade_sexe_id",stade_sexe_id).findUnique();
			for(StadeSexeHierarchieDansGroupe sshdg_fils :
				StadeSexeHierarchieDansGroupe.find.where()
				.eq("groupe", groupe)
				.eq("stade_sexe_pere",sshdg_pere.stade_sexe).findList()){
				sshdg_fils.delete();
			}
			sshdg_pere.delete();
			return redirect("/gererstadesexe/"+groupe_id);
		}else
			return Admin.nonAutorise();
	}
	
	/**
	 * Supprime un stade fils
	 * @param groupe_id
	 * @param stade_sexe_id
	 * @return
	 */
	public static Result supprimerStadeFils(Integer groupe_id, Integer stade_sexe_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
			StadeSexeHierarchieDansGroupe sshdg = 
					StadeSexeHierarchieDansGroupe.find.where()
					.eq("groupe",groupe)
					.eq("stade_sexe.stade_sexe_id",stade_sexe_id).findUnique();
			sshdg.delete();
			return redirect("/gererstadesexe/"+groupe_id);
		}else
			return Admin.nonAutorise();
	}
}
