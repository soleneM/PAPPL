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

import java.io.IOException;

import javax.naming.NamingException;
import javax.persistence.PersistenceException;

import models.Espece;
import models.EspeceSynonyme;
//import models.SousFamille;
import models.Groupe;
import models.Image;
import models.*;
import controllers.admin.Admin;
import functions.UploadImage;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import views.html.expert.editerInsectes;
import views.html.expert.ajax.editerInsectesAjax;

public class EditerInsectes extends Controller {

	public static Result main(Integer groupe_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe))
			return ok(editerInsectes.render(groupe));
		else
			return Admin.nonAutorise();
	}

	public static Result edit(Integer groupe_id, Integer espece_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
			Espece espece = Espece.find.byId(espece_id);
			return ok(editerInsectesAjax.render(espece));
		}else
			return Admin.nonAutorise();
	}

	public static Result changerNom(Integer espece_id){
		if(MenuExpert.isExpertConnected()){
			Espece espece = Espece.find.byId(espece_id);
			DynamicForm df = DynamicForm.form().bindFromRequest();
			String nom = df.get("nom");
			if(Espece.find.where().eq("espece_nom",nom).findUnique()==null){
				espece.espece_nom=nom;
				espece.update();
				EspeceIsInGroupementLocal eiigl = EspeceIsInGroupementLocal.find.where().eq("espece",espece).findList().get(0);
				return redirect("/editerInsectes/"+eiigl.groupe.groupe_id);
			}else{
				return badRequest("Un insecte portant le nom '"+nom+"' est déjà présent dans la base !");
			}

		}else
			return Admin.nonAutorise();
	}
	public static Result changerAuteur(Integer espece_id){
		if(MenuExpert.isExpertConnected()){
			Espece espece = Espece.find.byId(espece_id);
			DynamicForm df = DynamicForm.form().bindFromRequest();
			String auteur = df.get("auteur");
			espece.espece_auteur=auteur;
			espece.update();
			EspeceIsInGroupementLocal eiigl = EspeceIsInGroupementLocal.find.where().eq("espece",espece).findList().get(0);
			return redirect("/editerInsectes/"+eiigl.groupe.groupe_id);
		}else
			return Admin.nonAutorise();
	}
	public static Result supprimerSynonyme(Integer synonyme_id){
		if(MenuExpert.isExpertConnected()){
			EspeceSynonyme synonyme = EspeceSynonyme.find.byId(synonyme_id);
			int e_id = synonyme.synonyme_espece.espece_id;
			Espece e = Espece.find.byId(e_id);
			EspeceIsInGroupementLocal eiigl = EspeceIsInGroupementLocal.find.where().eq("espece",e).findList().get(0);
			int groupe_id=eiigl.groupe.groupe_id;
			synonyme.delete();
			return redirect("/editerInsectes/"+groupe_id);
		}else
			return Admin.nonAutorise();
	}
	public static Result ajouterSynonyme(Integer espece_id) throws NamingException{
		if(MenuExpert.isExpertConnected()){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			String syn = df.get("synonyme");
			Espece espece = Espece.find.byId(espece_id);
			EspeceIsInGroupementLocal eiigl = EspeceIsInGroupementLocal.find.where().eq("espece",espece).findList().get(0);
			new EspeceSynonyme(syn,false,espece.espece_id).save();
			return redirect("/editerInsectes/"+eiigl.groupe.groupe_id);
		}else
			return Admin.nonAutorise();
	}
	/*
	 * 
	 
		if(MenuExpert.isExpertConnected()){
			MultipartFormData body = request().body().asMultipartFormData();
			Espece espece = Espece.find.byId(espece_id);
			FilePart fp = body.getFile("photo");
			Image photo = UploadImage.upload(fp);
			if(photo!=null){
				espece.espece_photo=photo;
				espece.update();
				EspeceIsInGroupementLocal eiigl = EspeceIsInGroupementLocal.find.where().eq("espece",e).findUnique();
				return redirect("/editerInsectes/"+eiigl.groupe.groupe_id);
			}else{
				return badRequest("Le fichier uploadé n'est pas un format d'image valide.");
			}
		}else
			return Admin.nonAutorise();
	}
	*/
	/**
	* Change la sous-famille d'une espèce, ou l'enlève si jamais elle n'en a plus
	* @return
	* @throws NamingException
	 *@throws PersistenceException
	 */
	/*
	 public static Result changerSousFamille(Integer espece_id) throws NamingException, PersistenceException {
	 	 if(MenuExpert.isExpertConnected()){
	 	 	 Espece espece = Espece.find.byId(espece_id);
	 	 	 DynamicForm df = DynamicForm.form().bindFromRequest();
	 	 	 String sousfam = df.get("sous-famille");
	 	 	 if (sousfam!=null){
	 	 	 	 if(sousfam.equals("0")){
	 	 	 	 	 if(espece.espece_sousfamille.sous_famille_existe){
	 	 	 	 	 	 Integer fam_id = espece.getFamille().famille_id;
	 	 	 	 	 	 espece.espece_sousfamille = new SousFamille(espece.espece_nom,false,fam_id);
	 	 	 	 	 	 espece.espece_sousfamille.save();
	 	 	 	 	 	 espece.update();
	 	 	 	 	 }
	 	 	 	 } else {
	 	 	 	 	 if(espece.espece_sousfamille.sous_famille_existe){
	 	 	 	 	 	 espece.espece_sousfamille = SousFamille.find.byId(Integer.parseInt(sousfam));
	 	 	 	 	 	 espece.update();
	 	 	 	 	 } else {
	 	 	 	 	 	 SousFamille ancienne = espece.espece_sousfamille;
	 	 	 	 	 	 espece.espece_sousfamille = SousFamille.find.byId(Integer.parseInt(sousfam));
	 	 	 	 	 	 espece.update();
	 	 	 	 	 	 ancienne.delete();
	 	 	 	 	 }
	 	 	 	 }
	 	 	 } else {
	 	 	 	return badRequest("Erreur lors du changement par "+sousfam);
	 	 	 }
	 	 	EspeceIsInGroupementLocal eiigl = EspeceIsInGroupementLocal.find.where().eq("espece",e).findUnique();
	 	 	 return redirect("/editerInsectes/"+eiigl.groupe.groupe_id);
	 	 } else
	 	 	return Admin.nonAutorise();
	 }
*/
	 /**
	 * Change la famille d'une espèce
	 * @return
	 * @throws NamingException
	 * @throws PersistenceException
	 */
	/*
	 public static Result changerFamille(Integer espece_id) throws NamingException, PersistenceException{
	 	 if(MenuExpert.isExpertConnected()){
	 	 	 Espece espece = Espece.find.byId(espece_id);
	 	 	 DynamicForm df = DynamicForm.form().bindFromRequest();
	 	 	 String fam = df.get("famille");
	 	 	 if(fam!=null){
	 	 	 	 Integer fam_id = Integer.parseInt(fam);
	 	 	 	 SousFamille ancienne = espece.espece_sousfamille;
	 	 	 	 espece.espece_sousfamille = new SousFamille(espece.espece_nom,false,fam_id);
	 	 	 	 espece.espece_sousfamille.save();
	 	 	 	 espece.update();
	 	 	 	 ancienne.delete();
	 	 	 } else {
	 	 	 	 return badRequest("Erreur lors du changement par "+fam);
	 	 	 }
	 	 	EspeceIsInGroupementLocal eiigl = EspeceIsInGroupementLocal.find.where().eq("espece",e).findUnique();
	 	 	 return redirect("/editerInsectes/"+eiigl.groupe.groupe_id);
	 	 } else
	 	 	 return Admin.nonAutorise();
	 }
	 */
}
