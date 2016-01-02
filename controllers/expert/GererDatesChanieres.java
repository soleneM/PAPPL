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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import models.DateCharniere;
import models.Groupe;
import controllers.admin.Admin;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.expert.gererDatesCharnieres;

public class GererDatesChanieres extends Controller{
	/**
	 * Affiche la page de gestion des dates charnières
	 * si l'utilisateur est expert dans le groupe donné.
	 * @param groupe_id
	 * @return
	 */
	public static Result main(Integer groupe_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe))
			return ok(gererDatesCharnieres.render(groupe));
		else
			return Admin.nonAutorise();
	}
	
	/**
	 * Ajoute une date charnière et redirige vers la page de gestion
	 * des dates charnières.
	 * @param groupe_id
	 * @return
	 * @throws ParseException
	 */
	public static Result ajouter(Integer groupe_id) throws ParseException{
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			int jour = Integer.parseInt(df.get("jour"));
			int mois = Integer.parseInt(df.get("mois"));
			int annee = Integer.parseInt(df.get("annee"));
			Calendar c = Calendar.getInstance();
			SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
			c.setTime(date_format.parse(jour+"/"+mois+"/"+annee));
			new DateCharniere(groupe, c).save();
			return redirect("/gererdatescharnieres/"+groupe_id);
		}
		else
			return Admin.nonAutorise();
	}
	
	/**
	 * Supprime la date charnière en argument et redirige vers la page de gestion
	 * des dates charnières
	 * @param groupe_id
	 * @param date_charniere_id
	 * @return
	 */
	public static Result supprimer(Integer groupe_id, Integer date_charniere_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
			DateCharniere dc = DateCharniere.find.byId(date_charniere_id);
			if(dc!=null){
				dc.delete();
			}
			return redirect("/gererdatescharnieres/"+groupe_id);
		}
		else
			return Admin.nonAutorise();
	}
}
