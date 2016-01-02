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
package controllers.ajax.expert.requetes;

import java.util.List;

import controllers.admin.Admin;
import controllers.expert.MenuExpert;
import functions.Periode;
import models.Groupe;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.expert.requetes.ajax.listeSousGroupes;
import views.html.expert.requetes.ajax.listeEspeces;
import views.html.expert.requetes.ajax.listeStades;
import views.html.expert.requetes.ajax.infoDates;

public class Requetes extends Controller {

	public static Result listeSousGroupes(Integer groupe_id){
		if(MenuExpert.isExpertConnected()){
			Groupe groupe = Groupe.find.byId(groupe_id);
			return ok(listeSousGroupes.render(groupe));
		}else
			return Admin.nonAutorise();
	}
	
	public static Result listeEspeces(Integer groupe_id, Integer sous_groupe_id){
		if(MenuExpert.isExpertConnected()){
			Groupe groupe = Groupe.find.byId(groupe_id);
			Groupe sous_groupe = Groupe.find.byId(sous_groupe_id);
			return ok(listeEspeces.render(groupe,sous_groupe));
		}else
			return Admin.nonAutorise();
	}
	
	public static Result listeStades(Integer groupe_id){
		if(MenuExpert.isExpertConnected()){
			Groupe groupe = Groupe.find.byId(groupe_id);
			return ok(listeStades.render(groupe));
		}else
			return Admin.nonAutorise();
	}

	/**
	 * Renvoie la page de date avec les périodes correspondant au groupe
	 * donné.
	 * @param groupe_id
	 * @return
	 */
	public static Result infoDates(Integer groupe_id){
		if(MenuExpert.isExpertConnected()){
			Groupe groupe = Groupe.find.byId(groupe_id);
			List<Periode> periodes = Periode.getPeriodes(groupe);
			return ok(infoDates.render(periodes));
		}else
			return Admin.nonAutorise();
	}
}
