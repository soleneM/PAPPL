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

import java.util.List;

import models.Droits;
import models.Groupe;
import models.Membre;
import models.MembreIsExpertOnGroupe;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.admin.assignerExperts;

/**
 * Gère les fonctions pour assigner les experts
 * @author malik
 *
 */
public class AssignerExpert extends Controller {

	/**
	 * Affiche la page principale
	 * @return
	 */
	public static Result main() {
		if(Admin.isAdminConnected()){
			List<Groupe> groupes = Groupe.find.orderBy("groupe_nom").findList();
			return ok(assignerExperts.render(groupes));
		}else
			return Admin.nonAutorise();
	}

	/**
	 * Ajoute l'expert donné dans la form (s'il existe) dans le groupe dont
	 * l'ID est donné en argument.
	 * Si les données entrées sont fausses, retourne juste sur main().
	 * @param groupe_id
	 * @return
	 */
	public static Result ajouter(Integer groupe_id){
		if(Admin.isAdminConnected()){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			Groupe groupe = Groupe.find.byId(groupe_id);
			Membre expert = Membre.find.where().eq("membre_nom", df.get("membre_nominputGroupe"+groupe_id)).findUnique();
			if(expert!=null && groupe!=null && !expert.membre_droits.equals(Droits.ADMIN)){
				MembreIsExpertOnGroupe mieog = new MembreIsExpertOnGroupe(expert,groupe);
				mieog.save();
				expert.membre_droits=Droits.EXPERT;
				expert.save();
			}
			return redirect("/assignerExperts");
		}else
			return Admin.nonAutorise();
	}

	/**
	 * Change l'expert du groupe. DEPRECIEE !!!
	 * @param groupe_id
	 * @return
	 */
	public static Result changer(Integer groupe_id){
		if(Admin.isAdminConnected()){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			MembreIsExpertOnGroupe mieog = MembreIsExpertOnGroupe.find.where().eq("groupe.groupe_id",groupe_id).findUnique();
			Membre expert = Membre.find.where().eq("membre_nom", df.get("membre_nominputGroupe"+groupe_id)).findUnique();
			if(mieog!=null && expert!=null){
				if(!mieog.membre.membre_droits.equals(Droits.ADMIN)){
					mieog.membre.membre_droits=Droits.TEMOIN;
					mieog.membre.save();
				}
				mieog.membre=expert;
				mieog.save();
				expert.membre_droits=Droits.EXPERT;
				expert.save();
			}
			return redirect("/assignerExperts");
		}else
			return Admin.nonAutorise();
	}
	
	/**
	 * Enlève un expert de son statut d'expert en tel domaine.
	 * @param mieog_id
	 * @return
	 */
	public static Result retrograder(Integer mieog_id){
		if(Admin.isAdminConnected()){
			MembreIsExpertOnGroupe mieog = MembreIsExpertOnGroupe.find.byId(mieog_id);
			if(mieog!=null){
				Membre ex_expert = mieog.membre;
				mieog.delete();
				if(MembreIsExpertOnGroupe.find.where().eq("membre",ex_expert).findList().isEmpty()
						&& !ex_expert.membre_droits.equals(Droits.ADMIN)){
					ex_expert.membre_droits=Droits.TEMOIN;
					ex_expert.update();
				}
			}
			return redirect("/assignerExperts");
		}else
			return Admin.nonAutorise();
	}

}
