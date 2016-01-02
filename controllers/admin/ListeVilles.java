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

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;

import models.Commune;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.admin.listeVilles;

public class ListeVilles extends Controller {

	public static Result main(){
		if(Admin.isAdminConnected()){
			return ok(listeVilles.render());
		}else
			return Admin.nonAutorise();
	}

	/**
	 * Change le nom customisé ville_nom_aer de la ville en argument.
	 * On est obligé de faire une mise à jour de la base à la main
	 * à cause d'un bug de play concernant les floats dans les champs.
	 * Pour plus de détails, chercher javax.persistence.OptimisticLockException: Data has changed.
	 * @param ville_id
	 * @return
	 */
	public static Result renommer(Integer ville_id){
		if(Admin.isAdminConnected()){
			Commune commune = Commune.find.byId(ville_id);
			if(commune!=null){
				DynamicForm df = DynamicForm.form().bindFromRequest();
				String nouveauNom = df.get("nouveauNom");
				SqlUpdate update = Ebean.createSqlUpdate("UPDATE commune SET ville_nom_aer=:aer WHERE ville_id=:id")
						.setParameter("aer", nouveauNom)
						.setParameter("id", ville_id);
				update.execute();
			}
			return ok("Nom de commune changé avec succès.");
		}else
			return Admin.nonAutorise();
	}
}
