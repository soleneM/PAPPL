package controllers.admin;

import java.io.IOException;

import controllers.membre.SecuredMembre;
import models.*;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;

import javax.naming.NamingException;
import javax.persistence.PersistenceException;

import java.io.File;
import java.io.IOException;

import functions.UploadImage;
import play.mvc.Security;
import views.html.admin.organisationLocale.gererOrganisationLocale;
import views.html.admin.organisationLocale.editerGroupe;
import views.html.admin.organisationLocale.ajouterGroupe;
import views.html.admin.organisationLocale.organisationLocale;

@Security.Authenticated(SecuredMembre.class)
public class GererOrganisationLocale extends Controller{

    public static Result main(){
        if (Admin.isAdminConnected()){
            return ok(gererOrganisationLocale.render());
        } else {
            return Admin.nonAutorise();
        }
    }

	public static Result getOrganisationLocale(){
		return ok(organisationLocale.render());
	}
    
    public static Result getEditGroupementLocal(Integer groupe_id) {
    	Groupe groupe=Groupe.find.where().eq("groupe_id", groupe_id).findUnique();
    	return ok(editerGroupe.render("",groupe));
    }
    
    public static Result postEditGroupementLocal(Integer groupe_id) {
    	Groupe groupe=Groupe.find.where().eq("groupe_id", groupe_id).findUnique();
		if(groupe!=null){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			String groupe_nom = df.get("groupe_nom");
			if(Groupe.find.where().eq("groupe_nom", groupe_nom).findUnique()==null){
				groupe.groupe_nom=groupe_nom;
			}
			String type_intitule = df.get("type");
			TypeGroupementLocal type=TypeGroupementLocal.find.where().eq("type_groupement_local_intitule", type_intitule).findUnique();
			groupe.groupe_type = type;
			String pere_string = df.get("pere");
			if (pere_string.equals("NULL")) {
				groupe.groupe_pere=null;
			} else {
				Integer pere_id = Integer.parseInt(pere_string);
				Groupe pere=Groupe.find.where().eq("groupe_id", pere_id).findUnique();
				groupe.groupe_pere = pere;
			}
			groupe.update();
		}
		return ok(editerGroupe.render("Informations mises à jour avec succès",groupe));
	}
    
    public static Result getNewGroupementLocal() {
    	return ok(ajouterGroupe.render(""));
    }
    
    public static Result postNewGroupementLocal() {
    	DynamicForm df = DynamicForm.form().bindFromRequest();
    	String groupe_nom = df.get("groupe_nom");
    	String type_intitule = df.get("type");
    	String pere_string = df.get("pere");
    	if (pere_string.equals("NULL")){
    	Groupe groupe= new Groupe(groupe_nom,type_intitule);
    	groupe.save();
    	} else {
    	Integer pere_id = Integer.parseInt(pere_string);
    	Groupe pere=Groupe.find.where().eq("groupe_id", pere_id).findUnique();
    	Groupe groupe= new Groupe(groupe_nom,type_intitule,pere);
    	groupe.save();
    	}
    	return ok(ajouterGroupe.render("Informations mises à jour avec succès"));
    }


	public static Result deleteGroupementLocal(Integer groupe_id){
		Groupe groupe = Groupe.find.byId(groupe_id);
		groupe.supprimer();
		return ok("Le groupement a bien été supprimé.");
	}
}
