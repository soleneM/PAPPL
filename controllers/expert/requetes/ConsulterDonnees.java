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
package controllers.expert.requetes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import models.Membre;
import controllers.admin.Admin;
import controllers.expert.MenuExpert;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.expert.requetes.consulterDonnees;

public class ConsulterDonnees extends Controller {

	public static Result main() {
        Membre membre = Membre.find.where().eq("membre_email", session("username")).findUnique();
		if(MenuExpert.isExpertConnected()){
			return ok(consulterDonnees.render(membre));
		}else
			return Admin.nonAutorise();
	}

	/**
	 * Télécharge un fichier.
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static Result telechargerFichier(String filename){
		if(MenuExpert.isExpertConnected()){
			FileInputStream fis;
			try {
				fis = new FileInputStream(new File(Play.application().configuration().getString("xls_generes.path")+filename));
				response().setHeader("Content-Disposition", "attachment; filename="+filename);
				return ok(fis);
			} catch (FileNotFoundException e) {
				return notFound("404: File not found");
			}
		}else
			return Admin.nonAutorise();
	}
}
