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

import functions.credentials.PasswordHash;
import functions.mail.Mail;
import models.Membre;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.mails.mailNouveauMotDePasse;
import views.html.membre.motDePasse.entrerNouveauMotDePasse;
import views.html.membre.motDePasse.motDePasseOublie;
import views.html.membre.motDePasse.motDePasseReinitialise;
import views.html.membre.motDePasse.notificationDeMailEnvoye;

public class MotDePasseOublie extends Controller {

	public static Result main() {
		return ok(motDePasseOublie.render(""));
	}

	/**
	 * Envoie un mail contenant un lien permettant de choisir
	 * un nouveau mot de passe.
	 * @return
	 */
	public static Result nouveauMotDePasse() {
		DynamicForm df = DynamicForm.form().bindFromRequest();
		String email = df.get("email");
		Membre membre = Membre.find.where().eq("membre_email",email).setMaxRows(1).findUnique();
		if(membre!=null){
			membre.genereLienDeValidation();
			Mail mail = new Mail(
					"AER - Choisir un nouveau mot de passe",
					mailNouveauMotDePasse.render(membre.membre_lien_de_validation_de_mail).toString(),
					email,
					membre.membre_nom
					);
			mail.sendMail();
			membre.save();
			return ok(notificationDeMailEnvoye.render());
		}else
			return ok(motDePasseOublie.render("Cette adresse mail n'est pas référencée."));
	}

	/**
	 * Renvoie vers la page pour obtenir un nouveau mot de passe
	 * @param lien
	 * @return
	 */
	public static Result entrerNouveauMotDePasse(String lien){
		Membre membre = Membre.find.where().eq("membre_lien_de_validation_de_mail", lien).findUnique();
		if(membre!=null)
			return ok(entrerNouveauMotDePasse.render(membre));
		else
			return notFound("Ressource not found on server");
	}

	/**
	 * Réinitialise le mot de passe et sauvegarde le tout dans la base de données.
	 * @param membre_id
	 * @return
	 */
	public static Result reinitialiseMotDePasse(Integer membre_id, String lien)  throws NoSuchAlgorithmException, InvalidKeySpecException{
		Membre membre = Membre.find.byId(membre_id);
		if(membre!=null && membre.membre_lien_de_validation_de_mail.equals(lien)){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			String passw = df.get("passw");
			membre.membre_mdp_hash=PasswordHash.createHash(passw);
			membre.membre_lien_de_validation_de_mail=null;
			membre.save();
			return ok(motDePasseReinitialise.render());
		}else
			return notFound("Ressource not found on server");
	}
}
