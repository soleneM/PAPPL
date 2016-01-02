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

import java.util.List;

import models.Groupe;
import models.Membre;
import models.MembreIsExpertOnGroupe;
import controllers.admin.Admin;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.expert.menuExpert;

public class MenuExpert extends Controller {

	public static Result main() {
		if(isExpertConnected())
			return ok(menuExpert.render(Membre.find.where().eq("membre_email", session("username")).findUnique()));
		else
			return Admin.nonAutorise();
	}

	/**
	 * Vérifie qu'un expert est connecté
	 * @return Vrai ou Faux
	 */
	public static boolean isExpertConnected(){
		return session("expert")!=null;
	}

	/**
	 * Vérifie si l'expert connecté est expert dans le groupe donné
	 * @param groupe
	 * @return Vrai ou Faux
	 */
	public static boolean isExpertOn(Groupe groupe){
		Membre expert = Membre.find.where().eq("membre_email",session("username")).findUnique();
		if(expert==null)
			return false;
		else{
			List<MembreIsExpertOnGroupe> mieogs = expert.getGroupesDExpertise();
			for(MembreIsExpertOnGroupe mieog : mieogs){
				if(mieog.groupe.equals(groupe))
					return true;
			}
			return false;
		}
	}
}
