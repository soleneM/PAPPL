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
package controllers.ajax.membre;

import models.*;

import controllers.membre.SecuredMembre;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.Routes;

import views.html.membre.ajax.observation.getObservation;
import views.html.membre.ajax.observation.newObservation;
import views.html.membre.ajax.observation.editObservation;
import views.html.membre.ajax.listeEspeces;
import views.html.membre.ajax.informationsComplementaires.newInformationsComplementaires;
import views.html.membre.ajax.informationsComplementaires.listeStadeSexePrecis;
import views.html.membre.ajax.photo;

@Security.Authenticated(SecuredMembre.class)
public class ajaxObservation extends Controller {

//	/** cette fonction permet d'accèder aux méthodes de ce contrôle dans javascript sans utiliser les routes "hardcodées"
//	 *
//	 * @return
//	 */
//	public static Result jsObservation() {
//		response().setContentType("text/javascript");
//		return ok(Routes.javascriptRouter("jsObservation",
//						controllers.ajax.membre.routes.javascript.ajaxObservation.getObservation(),
//						controllers.ajax.membre.routes.javascript.ajaxObservation.newObservation(),
//						controllers.ajax.membre.routes.javascript.ajaxObservation.editObservation(),
//						controllers.ajax.membre.routes.javascript.ajaxObservation.getListeEspeces(),
//						controllers.ajax.membre.routes.javascript.ajaxObservation.getComplement(),
//						controllers.ajax.membre.routes.javascript.ajaxObservation.getStadeSexePrecis(),
//						controllers.ajax.membre.routes.javascript.ajaxObservation.getPhoto()
//				));
//	}


	public static Result getObservation(Long observation_id){
		Observation obs = Observation.find.byId(observation_id);
		return ok(getObservation.render(obs));
	}

	public static Result newObservation(Integer observation_tag, Integer groupe_id, Integer espece_id){
		return ok(newObservation.render(observation_tag,groupe_id, espece_id));
	}

	public static Result editObservation(Long observation_id){
		Observation obs = Observation.find.byId(observation_id);
		return ok(editObservation.render(obs));
	}
	
	/**
	 * Met à jour la liste d'espèce pour un groupe donné
	 * @param groupe_id
	 * @return
	 */

	public static Result getListeEspeces(Integer groupe_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		return ok(listeEspeces.render(groupe));
	}

	/**
	 * Affiche une information complémentaire pour une observation donnée.
	 * @param observation_position
	 * @param groupe_id
	 * @param complement_position
	 * @return
	 */

	public static Result newComplement(String observation_tag, Integer complement_tag, Integer groupe_id,Integer stadeSexe_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		StadeSexe stade = StadeSexe.find.byId(stadeSexe_id);
		return ok(newInformationsComplementaires.render(observation_tag, complement_tag, groupe, stade));
	}

	
	/**
	 * Donne la liste des stades_sexes ayant comme père le stade
	 * d'id stade_sexe_pere_id.
	 * @param groupe_id
	 * @param stade_sexe_pere_id
	 * @param observation_position
	 * @param complement_position
	 * @return
	 */

	public static Result getStadeSexePrecis(Integer groupe_id, Integer stade_sexe_pere_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		StadeSexe stadesexe = StadeSexe.find.byId(stade_sexe_pere_id);
		return ok(listeStadeSexePrecis.render(groupe, stadesexe));
	}
	
	/**
	 * Affiche la photo de l'insecte en question
	 * @param espece_id
	 * @return
	 */

	public static Result getPhoto(Integer espece_id){
		return ok(photo.render(Espece.find.byId(espece_id).espece_photo));
	}
}
