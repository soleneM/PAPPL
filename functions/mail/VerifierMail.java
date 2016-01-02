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
package functions.mail;

import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import models.Droits;
import models.Membre;
import views.html.verificationMail;
import views.html.mails.*;

public class VerifierMail extends Controller{

	public static void envoyerMailDeVerification(Membre membre){
		Mail mail = new Mail("Validation de la création de votre compte AER",
				mailDeVerification.render(membre.membre_lien_de_validation_de_mail).toString(),
				membre.membre_email,
				membre.membre_nom);
		mail.sendMail();
	}

	public static Result verifier(String lien){
		Membre m = Membre.find.where().eq("membre_lien_de_validation_de_mail", lien).findUnique();
		if(m!=null){
			m.membre_lien_de_validation_de_mail=null;
			m.save();
			envoyerMailAcceptationAAdmin(m);
			return ok(verificationMail.render());
		}else{
			return notFound("Ressource not found on server");
		}
	}
	
	/**
	 * Envoie le mail de confirmation lorsque l'on change son adresse mail.
	 * @param membre
	 */
	public static void envoyerMailDeVerificationNouveauMail(Membre membre){
		Mail mail = new Mail("Validation du changement de votre adresse email",
				mailDeVerificationNouveauMail.render(membre.membre_lien_de_validation_de_mail).toString(),
				membre.membre_email,
				membre.membre_nom);
		mail.sendMail();
	}

	/**
	 * Envoie le mail à l'admin pour dire que quelqu'un s'est inscrit
	 * @param membre
	 */
	public static void envoyerMailAcceptationAAdmin(Membre membre){
		List<Membre> admins = Membre.find.where().eq("membre_droits", Droits.ADMIN).findList();
		for(Membre admin : admins){
			Mail mail = new Mail(membre+" vient de s'inscrire sur le site de l'AER",
					mailNouvelleInscriptionPourAdmin.render(membre).toString(),
					admin.membre_email,
					admin.membre_nom);
			mail.sendMail();
		}
	}

	public static void envoyerMailAcceptation(Membre membre) {
		Mail mail = new Mail("Votre inscription sur le site de l'AER a été acceptée",
				mailNotificationAcceptation.render(membre).toString(),
				membre.membre_email,
				membre.membre_nom);
		mail.sendMail();
	}
}
