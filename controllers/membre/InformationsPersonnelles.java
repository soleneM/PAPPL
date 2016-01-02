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

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import models.Confidentialite;
import models.Membre;
import controllers.admin.Admin;
import controllers.membre.SecuredMembre;
import functions.credentials.Credentials;
import functions.mail.VerifierMail;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.membre.informationsPersonnelles;
import views.html.membre.changementDeMailTermine;

public class InformationsPersonnelles extends Controller {

	@Security.Authenticated(SecuredMembre.class)
	public static Result main() {
		Membre membre = Membre.find.where().eq("membre_email", session("username")).findUnique();
		return ok(informationsPersonnelles.render("",membre));
	}
	
	/**
	 * Redirige vers la page d'informations personnelles principale.
	 * @return
	 */
	public static Result redirectMain(){
		return redirect("/informationsPersonnelles");
	}

	/**
	 * Change les infos non sensible du membre
	 * @return
	 */
	@Security.Authenticated(SecuredMembre.class)
	public static Result changerInfos() {
		Membre membre = Membre.find.where().eq("membre_email", session("username")).findUnique();
		if(membre!=null){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			String nom = df.get("nom");
			if(Membre.find.where().eq("membre_nom", nom).findUnique()==null){
				membre.membre_nom=nom;
			}
			membre.membre_civilite = df.get("civilite");
			membre.membre_adresse = df.get("adresse");
			membre.membre_adresse_complement = df.get("adresse_complement");
			membre.membre_code_postal = df.get("cp");
			membre.membre_ville = df.get("commune");
			membre.membre_pays = df.get("pays");
			int journais = Integer.parseInt(df.get("jour"));
			if(journais!=0)
				membre.membre_journais = journais;
			int moisnais = Integer.parseInt(df.get("mois"));
			if(journais!=0)
				membre.membre_moisnais = moisnais;
			int annenais = Integer.parseInt(df.get("annee"));
			if(annenais!=0)
				membre.membre_annenais = annenais;
			membre.membre_abonne = df.get("newletter").equals("oui");
			membre.membre_confidentialite = df.get("confidentialite").equals("libre") ? Confidentialite.OUVERTE : Confidentialite.CASPARCAS;
			membre.update();
		}
		return ok(informationsPersonnelles.render("Informations mises à jour avec succès",membre));
	}
	/**
	 * Change le mail du membre, envoie un mail pour valider la nouvelle adresse
	 * @return
	 */
	@Security.Authenticated(SecuredMembre.class)
	public static Result changerMail() throws NoSuchAlgorithmException, InvalidKeySpecException{
		Membre membre = Membre.find.where().eq("membre_email", session("username")).findUnique();
		if(membre!=null){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			String passw = df.get("passw");
			Credentials credentials = new Credentials(session("username"));
			if(credentials.connect(passw)){
				String email = df.get("email");
				if(Membre.find.where().eq("membre_email", email).findUnique()==null){
					membre.membre_email=email;
					membre.update();
					VerifierMail.envoyerMailDeVerificationNouveauMail(membre);
					session().clear();
					return ok(changementDeMailTermine.render());
				}else
					return badRequest(informationsPersonnelles.render("L'adresse mail entrée est déjà utilisée !",membre));
			}else
				return badRequest(informationsPersonnelles.render("Mot de passe entré incorrect !",membre));
		}else
			return Admin.nonAutorise();
	}
	
	/**
	 * Change le mot de passe du membre
	 * @return
	 */
	@Security.Authenticated(SecuredMembre.class)
	public static Result changerMdp() throws NoSuchAlgorithmException, InvalidKeySpecException {
		Membre membre = Membre.find.where().eq("membre_email", session("username")).findUnique();
		if(membre!=null){
			DynamicForm df = DynamicForm.form().bindFromRequest();String passw = df.get("passw");
			Credentials credentials = new Credentials(session("username"));
			if(credentials.connect(passw)){
				String newpassw = df.get("newpassw");
				credentials.changeMotDePasse(newpassw);
				return ok(informationsPersonnelles.render("Mot de passe mis à jour avec succès",membre));
			}else
				return badRequest(informationsPersonnelles.render("Mot de passe entré incorrect !",membre));
		}else
			return Admin.nonAutorise();
	}
	
}
