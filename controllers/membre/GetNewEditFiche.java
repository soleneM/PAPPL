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
package controllers.membre;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.ListIterator;

import models.Commune;
import models.Espece;
import models.Fiche;
import models.FicheHasMembre;
import models.InformationsComplementaires;
import models.Membre;
import models.Observation;
import models.StadeSexe;
import models.UTMS;

import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import views.html.membre.fiche.getFiche;
import views.html.membre.fiche.newFiche;
import views.html.membre.fiche.editFiche;


@Security.Authenticated(SecuredMembre.class)
public class GetNewEditFiche extends Controller {

	/*************** affichage d'une fiche de témoignage *******************/

	/**
	 * Affichage (print) d'une fiche
	 * @param fiche_id : id de la fiche à afficher
	 * @return
	 */


	public static Result getFiche(Long fiche_id){
		Fiche fiche = Fiche.find.where().eq("fiche_id",fiche_id).findUnique();
		return ok(getFiche.render(fiche));
	}

	/*************** nouvelle fiche de témoignage*******************/

	public static Result mainNewFiche() {
		Membre membre = Membre.find.where().eq("membre_email", session("username")).findUnique();
		return ok(newFiche.render(membre, ""));
	}

	/**
	 * Récupère le formulaire de création et enregistre la fiche et son contenu dans la base
	 * @return la page d'édition de la fiche en question
	 * @throws ParseException
	 */
	public static Result postNewFiche() throws ParseException{

		Membre temoin = Membre.find.where().eq("membre_email", session("username")).findUnique();
		DynamicForm df = DynamicForm.form().bindFromRequest();

		// entête -> fiche

		String commune_nom = df.get("ville_nom_reel");
		Commune commune = Commune.find.where().eq("ville_nom_reel", commune_nom).findUnique();
		if(commune==null && !commune_nom.equals(""))
			return badRequest("La commune "+commune_nom+" n'est pas répertoriée !");

		String lieu_dit = df.get("lieu-dit");
		String utm_string = df.get("utm");
		UTMS utm = UTMS.find.byId(utm_string);
		String gps = df.get("gps");

		String jourmin = df.get("jourmin");
		String moismin = df.get("moismin");
		String anneemin = df.get("anneemin");

		String jour = df.get("jour");
		String mois = df.get("mois");
		String annee = df.get("annee");

		Calendar date = Calendar.getInstance();
		SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
		date.setTime(date_format.parse(jour+"/"+mois+"/"+annee));
		Calendar date_min = null;
		if(!jourmin.isEmpty() && !moismin.isEmpty() && !anneemin.isEmpty()){
			date_min = Calendar.getInstance();
			date_min.setTime(date_format.parse(jourmin + "/" + moismin + "/" + anneemin));
			if(date_min.compareTo(date)>=0)
				return ok(newFiche.render(temoin, "La date min est supérieur à la date !"));
		}

		String memo = df.get("memo");

		Fiche fiche = new Fiche(commune,lieu_dit,utm,gps,date_min,date,memo);
		fiche.save();

		// entête -> témoins

		int nombreMembres=Integer.parseInt(df.get("nombreMembres"));
		int membre_position;
		String membre_nom;
		for (membre_position=1; membre_position<=nombreMembres ; membre_position++) {
			if ((membre_nom = df.get("membre_nom" + membre_position)) != null) {
				Membre membre = Membre.find.where().eq("membre_nom", membre_nom).findUnique();
				if (membre != null)
					new FicheHasMembre(membre, fiche).save();
				else { //si un des membres n'est reconnu on arrête tout
					fiche.supprimer(); //supprime la fiche ainsi que toutes les fhm et observations qui lui sont liées
					return badRequest("Le membre " + membre + " n'est pas référencé.");
				}
			}
		}

		//observations

		Boolean b = saveNewObs(df, fiche);
		if(!b){ //à remplacer par un catch exception
			return badRequest("Erreur dans les observations");
		}

		return redirect("/ficheDeTemoignage/editFiche/"+fiche.fiche_id+"?msg=Merci!");
	}


	/*************** édition d'une fiche de témoignage *******************/

	public static Result mainEditFiche(Long fiche_id, String msg){
		Membre membre = Membre.find.where().eq("membre_email", session("username")).findUnique();
		Fiche fiche = Fiche.find.where().eq("fiche_id",fiche_id).findUnique();
		return ok(editFiche.render(membre, msg, fiche));
	}

	/**
	 * Récupère le formulaire  et met à jour la fiche et son contenu dans la base
	 * @return la page d'édition de la fiche en question
	 * @throws ParseException
	 */
	public static Result postEditFiche(Long fiche_id) throws ParseException{

		Membre temoin = Membre.find.where().eq("membre_email", session("username")).findUnique();
		Fiche fiche = Fiche.find.where().eq("fiche_id", fiche_id).findUnique();
		DynamicForm df = DynamicForm.form().bindFromRequest();

		// entête -> fiche

		String commune_nom = df.get("ville_nom_reel");
		Commune commune = Commune.find.where().eq("ville_nom_reel", commune_nom).findUnique();
		if(commune==null && !commune_nom.equals("")){
			return badRequest("La commune "+commune_nom+" n'est pas répertoriée !");}
		fiche.fiche_commune = commune;

		fiche.fiche_lieudit = df.get("lieu-dit");
		String utm_string = df.get("utm");
		fiche.fiche_utm = UTMS.find.byId(utm_string);
		fiche.fiche_gps_coordinates = df.get("gps");

		String jourmin = df.get("jourmin");
		String moismin = df.get("moismin");
		String anneemin = df.get("anneemin");

		String jour = df.get("jour");
		String mois = df.get("mois");
		String annee = df.get("annee");

		Calendar date = Calendar.getInstance();
		SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
		date.setTime(date_format.parse(jour+"/"+mois+"/"+annee));
		Calendar date_min = null;
		if(!jourmin.isEmpty() && !moismin.isEmpty() && !anneemin.isEmpty()){
			date_min = Calendar.getInstance();
			date_min.setTime(date_format.parse(jourmin + "/" + moismin + "/" + anneemin));
			if(date_min.compareTo(date)>=0){
				return ok(editFiche.render(temoin,"La date min est supérieure à la date !", fiche));
			}
		}
		fiche.fiche_date=date;
		fiche.fiche_date_min=date_min;

		fiche.fiche_memo = df.get("memo");

		fiche.update();


		// entête -> témoins (on ne supprime pas les relations existantes si pas de modifications)

		int nombreMembres=Integer.parseInt(df.get("nombreMembres"));
		int membre_position;
		String membre_nom;
		List<Membre> lsMembres = new ArrayList<Membre>();

			// on récupère dans lsMembres les membres précisés dans le formulaire
		for (membre_position=1; membre_position<=nombreMembres ; membre_position++) {
			membre_nom = df.get("membre_nom" + membre_position);
				Membre membre = Membre.find.where().eq("membre_nom", membre_nom).findUnique();
				if (membre != null)
					lsMembres.add(membre);
				else if (membre_position == 1) {
					return badRequest("Le membre " + membre + " n'est pas référencé.");
				}
		}
			// on analyse les changements et on ajoute/supprime une fhm en fonction

				// on vérifie les utilisateurs qui était déjà liés à cette fiche
		for (FicheHasMembre fhm : fiche.getFicheHasMembre()){
			boolean toDelete=true;
				// si il est tjs dans le formulaire
			for (ListIterator<Membre> it= lsMembres.listIterator(); it.hasNext();) {
				Membre membreForm = it.next();
				if (membreForm.membre_id == fhm.membre.membre_id){
					toDelete = false;
					it.remove();
				}
			}
				// si il n'est plus dans le formulaire : on supprime la relation
			if (toDelete){
				fhm.delete();
			}
		}
			// on crée les relations pour les nouveaux témoins
		for (Membre membreForm : lsMembres){
			new FicheHasMembre(membreForm, fiche).save();
		}


		// observations


			// on met à jour les anciennes observations

		Boolean c = editOldObs(df, fiche);
		if(!c){ //à remplacer par un catch exception
			return badRequest("Erreur dans les anciennes observations");
		}

			// on enregistre les nouvelles anciennes (à ne pas faire avant l'édition des anciennes)

		Boolean b = saveNewObs(df,fiche);
		if(!b){ //à remplacer par un catch exception
			return badRequest("Erreur dans les nouvelles observations");
		}


		return ok(editFiche.render(temoin,"Votre témoignage a été mis à jour avec succès !", fiche));
	}


	/**
	 * permet de sauvegarder les nouvelles observations (et uniquement les nouvelles)
	 * @param df fomulaire de création ou d'édition de fiche
	 * @param fiche la fiche en question (nouvelle ou pas)
	 * @return
	 */
	public static Boolean saveNewObs(DynamicForm df, Fiche fiche){
		String stLsObs = df.get("listeObservationsNew");
		List<String> lsObs = Arrays.asList(stLsObs.split("\\s*,\\s*"));
		String especeId;

		for (String obs_tag : lsObs){

			if( (especeId = df.get("espece-"+obs_tag)) != null ) {
				Espece espece = Espece.find.byId(Integer.parseInt(especeId));
				if (espece != null) {
					String determinateur = df.get("determinateur-" + obs_tag);
					String commentaires = df.get("commentaires-" + obs_tag);
					Observation obs = new Observation(fiche, espece, determinateur, commentaires);
					obs.save();

					saveComp(df,obs,obs_tag);

				} else {
					fiche.supprimer();
					// TODO throw une exception
					return false;
//					return badRequest("L'espèce d'une observation n'a pas été reconnue.");
				}
			}
		}
		return true;
	}

	/**
	 * permet de mettre à jour les anciennces observations (et uniquement les anciennes)
	 * @param df fomulaire d'édition de la fiche
	 * @param fiche la fiche en question
	 * @return
	 */
	//TODO vérifier l'intégration quand au process de validation des observations par les experts
	public static Boolean editOldObs(DynamicForm df, Fiche fiche){
		String stLsObs = df.get("listeObservationsEdit");
		List<String> lsObs = Arrays.asList(stLsObs.split("\\s*,\\s*"));
		String especeId;

		// on supprime une obs si besoin

			// on vérifie les obs qui étaient déjà liées à cette fiche
		for (Observation oldObs : fiche.getObservations()){
			boolean toDelete=true;
			// on test si il est tjs dans le formulaire
			for (String obsForm : lsObs) {
				if (obsForm.equals(oldObs.observation_id.toString())){
					toDelete = false;
				}
			}
			// si l'obs n'est plus dans le formulaire on la supprime (mais uniquement si elle n'est pas validée)
			if (toDelete && !oldObs.estValidee()){
				oldObs.supprimerDefinitivement();
			}
		}

		// on récupère les changements des anciennes obs non supprimées


		for (String obs_tag : lsObs){
			if(!obs_tag.isEmpty()) {
				Observation obs = Observation.find.byId((long) Integer.parseInt(obs_tag));

				if ((especeId = df.get("espece-" + obs_tag)) != null) {
					Espece espece = Espece.find.byId(Integer.parseInt(especeId));
					if (espece != null) {
						obs.observation_espece = espece;
						obs.observation_determinateur = df.get("determinateur-" + obs_tag);
						obs.observation_commentaires = df.get("commentaires-" + obs_tag);
						obs.update();

						// TODO y a t il un pb à supprimer les informationsComp à chaque édition? pb d'ID sur la BDD (on le fait augmenter inutilement)?
						// TODO tenir compte du changements des compléments pour la date de dernière modification
						for (InformationsComplementaires info : obs.getInfos()) {
							info.delete();
						}
						saveComp(df, obs, obs_tag);

					} else {
						fiche.supprimer();
						// TODO throw une exception
						return false;
//					return badRequest("L'espèce d'une observation n'a pas été reconnue.");
					}
				}
			}
		}
		return true;
	}


	/**
	 * récupère dans le formaulaire les éléments d'un complément d'information pour l'enregistrer dans la base
	 * @param df fomulaire de création ou d'édition de fiche
	 * @param obs l'observation auquelle est liée l'info complémentaire
	 * @param obs_tag le "tag" de cette info (id ou new-xx)
	 */
	public static void saveComp(DynamicForm df, Observation obs, String obs_tag){
		String stLsComp = df.get("listeComplements-" + obs_tag);
		List<String> lsComp = Arrays.asList(stLsComp.split("\\s*,\\s*"));

		for (String comp_tag : lsComp){
			String nombreSpecimens_string;
			if ((nombreSpecimens_string = df.get("nombreSpecimens-" + obs_tag + "-" + comp_tag)) != null) {

				Integer nombreSpecimens = nombreSpecimens_string.isEmpty() ? null : Integer.parseInt(nombreSpecimens_string);

				String stade_sexe_string = df.get("stadeSexePrecis-" + obs_tag + "-" + comp_tag);
				StadeSexe stade_sexe;
				if (stade_sexe_string != null) {
					stade_sexe = StadeSexe.find.byId(Integer.parseInt(stade_sexe_string));
				} else {
					stade_sexe_string = df.get("stadeSexe-" + obs_tag + "-" + comp_tag);
					stade_sexe = StadeSexe.find.byId(Integer.parseInt(stade_sexe_string));
				}
				if (stade_sexe != null) {
					new InformationsComplementaires(obs, nombreSpecimens, stade_sexe).save();
				}
//				} else {
//					fiche.supprimer();
//					// TODO throw une exception
//					return false;
////								return badRequest("L'information complémentaire" + comp_tag + "de l'observation " + obs_tag + "n'a pas été reconnu.");
//				}
			}
		}
	}

}
