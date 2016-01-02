package controllers.expert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import controllers.admin.Admin;
import functions.excels.Excel;
import functions.excels.ListeExportExcel;
import models.Espece;
import models.Fiche;
import models.FicheHasMembre;
import models.Groupe;
import models.Observation;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.expert.temoignagesValides;

public class TemoignagesValidees extends Controller {

	/**
	 * Renvoie la liste des observations validées, il n'en revoit que 50, la page permet de savoir à quel endroit de la liste on se trouve).
	 * filtre est un entier qui vaut 0 quand il n'y en a pas,  1 quand une espèce est filtrée, 2 quand un membre est filtré afin de pouvoir changer de page d'observation en gardant ses settings
	 * @param groupe_id
	 * @param page
	 * @return
	 */
	public static Result observationsValidees (Integer groupe_id, Integer page, String orderBy, String dir){
		Integer filtre=0;
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
			Integer valide=Observation.VALIDEE;
			List<Observation> observation= Observation.validees(groupe);
			List<Espece> especes= groupe.getAllEspecesInThis();

			Integer premierObservation=Math.min(((page-1)*50),observation.size() );
			Integer dernierObservation=Math.min((page*50-1), observation.size());
			Integer nbpages = observation.size()/50+1;
			List<Observation> observationsvues = observation.subList(premierObservation, dernierObservation);
			return ok(temoignagesValides.render(observationsvues, page,nbpages, groupe, especes, filtre,0,"", orderBy, dir));
		}else
			return Admin.nonAutorise();
	}
	/**
	 * Ne renvoit que les observations de l'espece voulue
	 * filtre est un entier qui vaut 0 quand il n'y en a pas,  1 quand une espèce est filtrée, 2 quand un membre est filtré afin de pouvoir changer de page d'observation en gardant ses settings
	 * @param groupe_id
	 * @param page
	 * @param espece_id
	 * @return
	 */
	public static Result observationsValideesEspece (Integer groupe_id, Integer page, Integer espece_id, String orderBy, String dir){
		Integer filtre=1;
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
			Integer valide=Observation.VALIDEE;
			List<Observation> observation= Observation.find.where()
											.eq("observation_validee",valide)
											.eq("observation_espece.espece_sous_groupe.sous_groupe_groupe",groupe)
											.eq("observation_espece.espece_id", espece_id)
											.orderBy(orderBy+" "+dir)
											.findList();
			List<Espece> especes= Espece.find.where().eq("espece_sous_groupe.sous_groupe_groupe", groupe).findList();
			Integer premierObservation=Math.min(((page-1)*50),observation.size() );
			Integer dernierObservation=Math.min((page*50-1), observation.size());
			Integer nbpages = observation.size()/50+1;
			List<Observation> observationsvues = observation.subList(premierObservation, dernierObservation);
			return ok(temoignagesValides.render(observationsvues, page,nbpages, groupe, especes, filtre,espece_id,"", orderBy, dir));
		}else
			return Admin.nonAutorise();
	}
	
	/**
	 * Permet de trier les observations, ne prenant que celles concernant un membre précis.
	 * filtre est un entier qui vaut 0 quand il n'y en a pas,  1 quand une espèce est filtrée, 2 quand un membre est filtré afin de pouvoir changer de page d'observation en gardant ses settings
	 * @param groupe_id
	 * @param page
	 * @param membre_nom
	 * @return
	 */
	public static Result observationsValideesMembre (Integer groupe_id, Integer page, String membre_nom, String orderBy, String dir){
		Integer filtre=2;
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
			Integer valide=Observation.VALIDEE;
			List<FicheHasMembre> fhms= FicheHasMembre.find.where().eq("membre.membre_nom", membre_nom).findList();
			List<Fiche> fiches= new ArrayList<Fiche>();
			for (FicheHasMembre fhm: fhms){
				fiches.add(fhm.fiche);
			}
			List<Observation> observations= Observation.find.where()
							.eq("observation_validee",valide)
							.eq("observation_espece.espece_sous_groupe.sous_groupe_groupe",groupe)
							.in("observation_fiche",fiches)
							.orderBy(orderBy+" "+dir)
							.findList();
			List<Espece> especes= Espece.find.where().eq("espece_sous_groupe.sous_groupe_groupe", groupe).findList();
			Integer premierObservation=Math.min(((page-1)*50),observations.size() );
			Integer dernierObservation=Math.min((page*50-1), observations.size());
			Integer nbpages = observations.size()/50+1;
			List<Observation> observationsvues = observations.subList(premierObservation, dernierObservation);
			return ok(temoignagesValides.render(observationsvues, page,nbpages, groupe, especes, filtre,0,membre_nom, orderBy, dir));

		}else
			return Admin.nonAutorise();
	}
	/**
	 * Remet une observation en suspens afin de pouvoir la reediter
	 * @param groupe_id
	 * @param observation_id
	 * @param page
	 * @return
	 */
	public static Result remettreEnSuspens(Integer groupe_id, Long observation_id, Integer page){
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
			Observation observation = Observation.find.byId(observation_id);
			if (observation!=null){
				observation.enSuspens();
			}
			observation.save();
			return redirect("/temoignagesValides/"+groupe_id+"/1/observation_date_validation/desc");
		}else
			return Admin.nonAutorise();
	}
	/**
	 * permet de créer le fichier excel de la liste voulue avec les différents paramètres de filtres/tri voulus.
	 *  Si on prend toutes les espèces espèce_id=0, si on prend tout les membres membre_nom="". 
	 * @param espece_id
	 * @param membre_nom
	 * @param orderBy
	 * @param dir
	 * @param groupe_id
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static Result exportExcelTemoignagesValidees(Integer espece_id, String membre_nom, String orderBy, String dir, Integer groupe_id) throws ParseException, IOException{
		// Crée le fichier
		Groupe groupe = Groupe.find.byId(groupe_id);
		if(MenuExpert.isExpertOn(groupe)){
		Excel ove = ListeExportExcel.observationsValidesExcel(espece_id,membre_nom, orderBy, dir, groupe_id);
		ove.writeToDisk();
		
		// Envoie le fichier
		String filename = ove.getFileName();
		FileInputStream fis = new FileInputStream(new File(Play.application().configuration().getString("xls_generes.path")+filename));
		response().setHeader("Content-Disposition", "attachment; filename="+filename);
		return ok(fis);
	}else
		return Admin.nonAutorise();
	}

}
