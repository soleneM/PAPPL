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
import views.html.admin.organisationScientifique.gererOrganisationScientifique;
import views.html.admin.organisationScientifique.editerGroupementScientifique;
import views.html.admin.organisationScientifique.ajouterGroupementScientifique;
import views.html.admin.organisationScientifique.organisationScientifique;

@Security.Authenticated(SecuredMembre.class)
public class GererOrganisationScientifique extends Controller{

    public static Result main(){
        if (Admin.isAdminConnected()){
            return ok(gererOrganisationScientifique.render());
        } else {
            return Admin.nonAutorise();
        }
    }
    
    public static Result getOrganisationScientifique(){
		return ok(organisationScientifique.render());
	}
    
    public static Result getEditGroupementScientifique(Integer gsci_id) {
    	GroupementScientifique gsci=GroupementScientifique.find.where().eq("groupement_scientifique_id", gsci_id).findUnique();
    	return ok(editerGroupementScientifique.render("",gsci));
    }
    
    public static Result postEditGroupementScientifique(Integer gsci_id) {
    	GroupementScientifique gsci=GroupementScientifique.find.where().eq("groupement_scientifique_id", gsci_id).findUnique();
		if(gsci!=null){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			String gsci_nom = df.get("groupement_scientifique_nom");
			//if(GroupementScientifique.find.where().eq("groupement_scientifique_nom", gsci_nom).findUnique()==null){
				gsci.groupement_scientifique_nom=gsci_nom;
			//}
			String type_intitule = df.get("type");
			TypeGroupementScientifique type=TypeGroupementScientifique.find.where().eq("intitule", type_intitule).findUnique();
			gsci.groupement_scientifique_type = type;
			String pere_string = df.get("pere");
			if (pere_string.equals("NULL")) {
				gsci.groupement_scientifique_pere=null;
			} else {
				Integer pere_id = Integer.parseInt(pere_string);
				GroupementScientifique pere=GroupementScientifique.find.where().eq("groupement_scientifique_id", pere_id).findUnique();
				gsci.groupement_scientifique_pere = pere;
			}
			gsci.update();
		}
		return ok(editerGroupementScientifique.render("Informations mises à jour avec succès",gsci));
	}
    
    public static Result getNewGroupementScientifique() {
    	return ok(ajouterGroupementScientifique.render(""));
    }
    
    public static Result postNewGroupementScientifique() {
    	DynamicForm df = DynamicForm.form().bindFromRequest();
    	String gsci_nom = df.get("groupement_scientifique_nom");
    	String type_intitule = df.get("type");
    	String pere_string = df.get("pere");
    	if (pere_string.equals("NULL")){
    	GroupementScientifique gsci= new GroupementScientifique(gsci_nom,type_intitule);
    	gsci.save();
    	} else {
    	Integer pere_id = Integer.parseInt(pere_string);
    	GroupementScientifique pere=GroupementScientifique.find.where().eq("groupement_scientifique_id", pere_id).findUnique();
    	GroupementScientifique gsci= new GroupementScientifique(gsci_nom,type_intitule,pere);
    	gsci.save();
    	}
    	return ok(ajouterGroupementScientifique.render("Informations mises à jour avec succès"));
    }


	public static Result deleteGroupementScientifique(Integer gsci_id){
		GroupementScientifique.supprimer(gsci_id);
		return ok("Le groupement a bien été supprimé.");
	}
}



