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

package models;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import models.Espece;
import models.Groupe;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import play.db.ebean.Model;

/**
 * Une observation contient 3 niveaux de validation :
 * observation_validee=0 -> Non validée (l'observation soit n'a pas encore été vue et dans ce cas,
 * 							on attend qu'un expert la voie, soit elle a déjà été vue et
 * 							dans ce cas l'expert l'a rejetée)
 * observation_validee=1 -> Validée
 * observation_validee=2 -> En suspend (il y a un truc qui cloche ou à vérifier avant de valider)
 * @author malik
 *
 */
@SuppressWarnings("serial")
@Entity
public class Observation extends Model {

	public static int NON_VALIDEE = 0;
	public static int VALIDEE = 1;
	public static int EN_SUSPEND = 2;

	@Id
	public Long observation_id;
	@NotNull
	@ManyToOne
	public Espece observation_espece;
	@NotNull
	@ManyToOne
	public Fiche observation_fiche;
	public String observation_determinateur;
	@Column(columnDefinition="TEXT")
	public String observation_commentaires;
	@NotNull
	public boolean observation_vue_par_expert;
	@NotNull
	@Column(columnDefinition="TINYINT")
	public Integer observation_validee;
	@NotNull
	public Calendar observation_date_derniere_modification;
	public Calendar observation_date_validation;

	public static Finder<Long,Observation> find = new Finder<Long,Observation>(Long.class, Observation.class);

	public Observation(Fiche fiche, Espece espece, String determinateur, String commentaires) {
		observation_id=Observation.idSuivante();
		observation_espece=espece;
		observation_determinateur=determinateur;
		observation_fiche=fiche;
		observation_commentaires=commentaires;
		observation_vue_par_expert=true;
		observation_validee=Observation.EN_SUSPEND;
		observation_date_derniere_modification=Calendar.getInstance();
		observation_date_validation=null;
	}

	public static List<Observation> findAll(){
		return find.all();
	}

	/**
	 * Sélectionne la liste des observations de l'état désiré (non validé, en suspend, validée)
	 * 
	 * @param validation (=0 non validé, =1en suspend)
	 * @return
	 */
	public static List<Observation> observationsEtat(Integer validation){
		return find.where().eq("observation_validee",validation).findList();
	}

	/**
	 * liste des observations non vues
	 * @return
	 */
	public static List<Observation> nonVus(Groupe groupe){
		List<Observation> listeObs = new LinkedList<Observation>();
		for(Espece espece : groupe.getAllEspecesInThis()){
			listeObs.addAll(Observation.find.where().eq("observation_espece", espece).eq("observation_vue_par_expert", false).findList());
		}
		return listeObs;
	}

	/**
	 * liste des observations en suspend
	 * @return
	 */
	public static List<Observation> enSuspend(Groupe groupe){
		List<Observation> listeObs = new LinkedList<Observation>();
		for(Espece espece : groupe.getAllEspecesInThis()){
			listeObs.addAll(Observation.find.where().eq("observation_espece", espece).eq("observation_validee", EN_SUSPEND).findList());
		}
		return listeObs;
	}

	/**
	 * liste des observations validées
	 * @return
	 */
	public static List<Observation> validees(Groupe groupe){
		List<Observation> listeObs = new LinkedList<Observation>();
		for(Espece espece : groupe.getAllEspecesInThis()){
			listeObs.addAll(Observation.find.where().eq("observation_espece", espece).eq("observation_validee", VALIDEE).findList());
		}
		return listeObs;
	}
	


	/**
	 * Renvoie la fiche associée à l'observation sélectionnée
	 * @return
	 */

	public Fiche getFiche(){
		Fiche fiche=this.observation_fiche;
		return fiche;
	}
	
	/**
	 * Renvoie l'espèce associée à l'observation sélectionnée
	 * @return
	 */

	public Espece getEspece(){
		Espece espece=this.observation_espece;
		return espece;
	}

	/**
	 * renvoie les infos complémentaires associées à l'observation (le nombre, stade, sexe des espèces trouvées+ des commentaires éventuels).
	 * @return
	 */
	public List<InformationsComplementaires> getInfos(){
		List<InformationsComplementaires> infos= InformationsComplementaires.find.where().eq("informations_complementaires_observation", this).findList();
		return infos;
	}

	/**
	 * Marque l'observation comme vue par l'expert
	 */
	public void vu(){
		this.observation_vue_par_expert=true;
		this.observation_validee=Observation.EN_SUSPEND;
	}
	
	/**
	 * Indique si l'observation a été validée par un expert
	 * @return
	 */
	 
	public boolean estvalidee() {
		return (this.observation_validee==Observation.VALIDEE);
	}

	/**
	 * L'observation est totalement validée.
	 */
	public void valider(){
		this.observation_validee=Observation.VALIDEE;
	}

	public void supprimer(){
		this.observation_validee=Observation.NON_VALIDEE;
	}
	
	/**
	 * Supprime définitivement une observation et toutes liens tables ayant une clef
	 * étrangère de celle-ci.

	 */
	public void supprimerDefinitivement(){
		for(InformationsComplementaires infos : this.getInfos()){
			infos.delete();
		}
		Fiche fiche = this.observation_fiche;
		this.delete();
		// Supprime la fiche dans laquelle est cette observation si c'est la seule observation de la fiche.
//		if(fiche.getObservations().isEmpty())
//			fiche.supprimer();
	}
	/**
	 * Renvoie true si l'observation est validée, false sinon.
	 * @return
	 */
	public boolean estValidee(){
		return observation_validee==Observation.VALIDEE;
	}
	
	/**
	 * Renvoie true si l'observation est en suspend, false sinon.
	 * @return
	 */
	public boolean estEnSuspend(){
		return observation_validee==Observation.EN_SUSPEND;
	}
	/**
	 * Remet l'observation en suspens pour recorriger des erreurs
	 */
	public void enSuspens(){
		this.observation_validee=Observation.EN_SUSPEND;
	}
	@Override
	public String toString(){
		return observation_espece+" "+observation_fiche;
	}
	
	/**
	 * Renvoie l'id suivante qui sera allouée.
	 * @return
	 */
	public static Long idSuivante(){
		Observation o = find.where().setMaxRows(1).orderBy("observation_id desc").findUnique();
		if(o==null)
			return 1L;
		else
			return o.observation_id+1L;
	}
}
