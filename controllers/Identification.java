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
package controllers;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import models.Droits;
import models.Membre;
import functions.credentials.Credentials;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.identification;

public class Identification extends Controller {

	/**
	 * Renvoie la page d'identification.
	 * Si un membre est connecté, renvoie vers la page correspondant à ses droits.
	 * @return
	 */
    public static Result main() {
    	if(session("memory")!=null){
    		Membre membre = Membre.find.where().eq("membre_email", session("username")).findUnique();
    		return allerVers(membre);
    	}else
    		return ok(identification.render(""));
    }
    
    /**
     * Si les identifiants sont faux, retourne à la page d'identification
     * avec un petit message.
     * @return
     */
    public static Result connexionEchouée() {
    	return ok(identification.render("Votre combinaison nom d'utilisateur et mot de passe est incorrecte, ou votre compte n'existe pas ou n'est pas activé."));
    }
    
    /**
     * Se connecte si le membre rentre le bon identifiant et le bon mot de passe.
     * Redirige ensuite vers la page correspondant aux droits du membre.
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static Result connexion() throws NoSuchAlgorithmException, InvalidKeySpecException{
    	session().clear();
    	DynamicForm df = DynamicForm.form().bindFromRequest();
    	String username = df.get("login");
    	Credentials credentials = new Credentials(username);
    	if(credentials.connect(df.get("passw"))){
    		session("username",username);
    		if(df.get("memory")!=null)
    			session("memory","");
    		Membre membre = Membre.find.where().eq("membre_email", username).findUnique();
    		if(membre.inscriptionValidee())
    			return allerVers(membre);
    		else
    			return connexionEchouée();
    	}else
    		return connexionEchouée();
    }
    
    /**
     * Redirige vers la page correspondant aux droits du membre en argument.
     * Si l'utilisateur n'est ni témoin, ni expert, ni admin (ce qui ne peut arriver
     * que s'il est arrivé des choses atroces à la session), alors on remet à 0
     * la session et on renvoie vers la page d'identification.
     * De plus, on remet à zéro le lien de validation par mesure de sécurité. (Si
     * quelqu'un de mal intentionné a forcer l'envoie d'un mail pour créer un nouveau
     * mot de passe.
     * @param membre
     * @return
     */
    public static Result allerVers(Membre membre){
		membre.membre_lien_de_validation_de_mail=null;;
		membre.save();
		if(membre.membre_droits.equals(Droits.TEMOIN)) {
			return redirect("/menuUtilisateur");
		} else if(membre.membre_droits.equals(Droits.EXPERT)){
			session("expert","true");
			return redirect("/menuExpert");
		}
		else if(membre.membre_droits.equals(Droits.ADMIN)){
			session("admin","true");
			return redirect("/menuAdmin");
		}else{
			session().clear();
			return main();
		}
    }
    
    /**
     * Déconnecte l'utilisateur
     * @return
     */
    public static Result deconnexion(){
    	session().clear();
    	return redirect("/");
    }
}
