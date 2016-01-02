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

import models.Confidentialite;
import models.Membre;
import controllers.admin.Admin;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.expert.gererTemoinsPassifs;
import views.html.expert.ajax.editerTemoinAjax;

public class GererTemoinsPassifs extends Controller {

	public static Result main(){
		if(MenuExpert.isExpertConnected()){
			return ok(gererTemoinsPassifs.render(""));
		}else
			return Admin.nonAutorise();
	}

	public static Result ajouter(){
		if(MenuExpert.isExpertConnected()){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			String civilite = df.get("civilite");
			String nom = df.get("nom");
			if(Membre.find.where().eq("membre_nom",nom).findUnique()!=null)
				return ok(gererTemoinsPassifs.render("Quelqu'un portant le nom '"+nom+"' est déjà référencé. Si par hasard ces deux personnes différentes ont le même nom, rajoutez (2) par exemple pour les différencier."));
			String email = df.get("email");
			if(!email.equals("")){
				if(Membre.find.where().eq("membre_email",email).findUnique()!=null)
					return ok(gererTemoinsPassifs.render("Cette adresse mail est déjà utilisée par un autre membre."));
			}
			String adresse = df.get("adresse");
			String complement = df.get("complement");
			String cp = df.get("cp");
			String ville = df.get("ville");
			String pays = df.get("pays");
			String journais = df.get("journais");
			String moisnais = df.get("moisnais");
			String annenais = df.get("annenais");
			String jourdece = df.get("jourdece");
			String moisdece = df.get("moisdece");
			String annedece = df.get("annedece");
			String telephone = df.get("tel");
			String confidentialite = df.get("confidentialite");
			String biographie = df.get("biographie");
			new Membre(civilite,nom,email,adresse,complement,cp,ville,pays,journais,moisnais,annenais,jourdece,moisdece,annedece,telephone,biographie,confidentialite).save();
			return redirect("/gererTemoinsPassifs");
		}else
			return Admin.nonAutorise();
	}

	/**
	 * Requête ajax pour éditer les informations du témoin.
	 * @param membre_id
	 * @return
	 */
	public static Result editer(Integer membre_id){
		if(MenuExpert.isExpertConnected()){
			Membre membre = Membre.find.byId(membre_id);
			return ok(editerTemoinAjax.render(membre));
		}else
			return Admin.nonAutorise();
	}

	/**
	 * Edite les informations du témoin et enregistre le tout dans la base.
	 * @param membre_id
	 * @return
	 */
	public static Result editPost(Integer membre_id){
		if(MenuExpert.isExpertConnected()){
			Membre temoin = Membre.find.byId(membre_id);
			DynamicForm df = DynamicForm.form().bindFromRequest();
			String civilite = df.get("civilite");
			String nom = df.get("nom");
			if(!nom.equals(temoin.membre_nom) && Membre.find.where().eq("membre_nom",nom).findUnique()!=null)
				return ok(gererTemoinsPassifs.render("Quelqu'un portant le nom '"+nom+"' est déjà référencé. Si par hasard ces deux personnes différentes ont le même nom, rajoutez (2) par exemple pour les différencier."));
			String adresse = df.get("adresse");
			String complement = df.get("complement");
			String cp = df.get("cp");
			String ville = df.get("ville");
			String pays = df.get("pays");
			String journais = df.get("journais");
			String moisnais = df.get("moisnais");
			String annenais = df.get("annenais");
			String jourdece = df.get("jourdece");
			String moisdece = df.get("moisdece");
			String annedece = df.get("annedece");
			String telephone = df.get("tel");
			String confidentialite = df.get("confidentialite");
			String biographie = df.get("biographie");
			temoin.membre_civilite=civilite;
			temoin.membre_nom=nom;
			temoin.membre_adresse=adresse;
			temoin.membre_adresse_complement=complement;
			temoin.membre_code_postal=cp;
			temoin.membre_ville=ville;
			temoin.membre_pays=pays;
			temoin.membre_tel=telephone;
			temoin.membre_biographie=biographie;
			if(confidentialite.equals("casparcas"))
				temoin.membre_confidentialite=Confidentialite.CASPARCAS;
			else
				temoin.membre_confidentialite=Confidentialite.OUVERTE;
			if(!annenais.equals("")){
				temoin.membre_annenais=Integer.parseInt(annenais);
				if(!moisnais.equals("")){
					temoin.membre_moisnais=Integer.parseInt(moisnais);
					if(!journais.equals("")){
						temoin.membre_journais=Integer.parseInt(journais);
					}
				}
			}
			if(!annedece.equals("")){
				temoin.membre_annedece=Integer.parseInt(annedece);
				if(!moisdece.equals("")){
					temoin.membre_moisdece=Integer.parseInt(moisdece);
					if(!jourdece.equals("")){
						temoin.membre_jourdece=Integer.parseInt(jourdece);
					}
				}
			}
			temoin.update();
			return redirect("/gererTemoinsPassifs");
		}else
			return Admin.nonAutorise();
	}
}
