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
package controllers.admin;

import functions.mail.Mail;
import models.Membre;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.admin.demandesInscription;
import views.html.mails.mailNouveauMotDePasse;

public class DemandesInscription extends Controller {

	public static Result main() {
		if(Admin.isAdminConnected()){
			return ok(demandesInscription.render(Membre.selectMembresInscrit(false)));
		}else
			return Admin.nonAutorise();
	}

	/**
	 * Valide l'inscription du membre en paramètre
	 * @param membre_id
	 * @return
	 */
	public static Result valideInscription(Integer membre_id){
		if(Admin.isAdminConnected()){
			Membre.valideMembre(membre_id);
			return redirect("/demandesInscription");
		}else
			return Admin.nonAutorise();
	}

	/**
	 * Permet via l'admin de donner l'acces à un témoin.
	 * Le témoin reçoit alors un mail de validation de son adresse mail.
	 * @return
	 */
	public static Result donnerPremierAccess(){
		if(Admin.isAdminConnected()){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			String membre_nom = df.get("membre");
			Membre membre = Membre.find.where().eq("membre_nom", membre_nom).findUnique();
			if(membre==null)
				return badRequest("Le membre "+membre_nom+" n'est pas référencé.");
			String email = df.get("email");
			Membre membreAvecMemeEmail = Membre.find.where().eq("membre_email", email).setMaxRows(1).findUnique();
			if(membreAvecMemeEmail==null){
				membre.membre_email=email;
				membre.genereLienDeValidation();
				membre.membre_inscription_acceptee=true;
				Mail mail = new Mail(
						"AER - Votre compte a été créé. Choisissez un mot de passe",
						"Votre compte AER a été créé ! Vous devez maintenant choisir un mot de passe pour vous connecter. L'identifiant est votre adresse e-mail.<br>"+mailNouveauMotDePasse.render(membre.membre_lien_de_validation_de_mail).toString(),
						email,
						membre.membre_nom
						);
				mail.sendMail();
				membre.save();
				return redirect("/demandesInscription");
			}else{
				return badRequest("Le membre "+membreAvecMemeEmail+" a la même adresse e-mail !");
			}
		}else
			return Admin.nonAutorise();
	}
    
    public static Result refuserInscription(Integer membre_id){
    	if(Admin.isAdminConnected()){
    		Membre.supprimeMembre(membre_id);
    		return redirect("/demandesInscription");
    	}else
			return Admin.nonAutorise();
    }
}
