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


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

import play.db.ebean.Model;

import java.util.*;

@SuppressWarnings("serial")
@Entity
public class Groupe extends Model {
	@Id
	public Integer groupe_id;
	@NotNull
	public String groupe_nom;
	@ManyToOne
	public Groupe groupe_pere;
	@NotNull
	@ManyToOne
	public TypeGroupementLocal groupe_type;

	public static Model.Finder<Integer,Groupe> find = new Model.Finder<Integer,Groupe>(Integer.class, Groupe.class);


	public Groupe(String groupe_nom, String groupe_type) {
		this.groupe_nom=groupe_nom;
		this.groupe_type= TypeGroupementLocal.find.byId(groupe_type);
	}

	public Groupe(String groupe_nom, String groupe_type, Groupe pere) {
		this.groupe_nom=groupe_nom;
		this.groupe_type= TypeGroupementLocal.find.byId(groupe_type);
		this.groupe_pere=pere;
	}

	public static List<Groupe> findAll(){
		return find.findList();
	}

	public static List<Groupe> findAllOfType(String type){
		TypeGroupementLocal t = TypeGroupementLocal.find.byId(type);
		return find.where().eq("groupe_type", t).orderBy("groupe_nom").findList();
	}

	public static List<Groupe> findAllByHierarchie(){
		List<Groupe> liste = new LinkedList<Groupe>();
		for (Groupe g : findAllOfType("groupe")){
			liste.add(g);
			for (Groupe sg :g.getFils()){
				liste.add(sg);
			}

		}
		return liste;
	}

	@Override
	public String toString(){
		return groupe_nom;
	}

	/**
	 * Renvoie la liste d'experts assigné à ce groupe.
	 * @return
	 */
	public List<MembreIsExpertOnGroupe> getExperts(){
		return MembreIsExpertOnGroupe.find.where().eq("groupe", this).findList();
	}


	/************************** stades et sexes ************************/


	/**
	 * Renvoie tous les stades sexes pères (c'est-à-dire les premiers choix qui apparaissent)
	 * pour cd groupe.
	 * @return
	 */
	public List<StadeSexe> getStadeSexePeres(){
		List<StadeSexeHierarchieDansGroupe> sshdgs =
				StadeSexeHierarchieDansGroupe.find.where()
						.eq("stade_sexe_pere", null)
						.eq("groupe",this).orderBy("position").findList();
		List<StadeSexe> stadesexes = new ArrayList<StadeSexe>();
		for(StadeSexeHierarchieDansGroupe sshdg : sshdgs){
			stadesexes.add(sshdg.stade_sexe);
		}
		return stadesexes;
	}

	/**
	 * Renvoie tous les stades sexes fils (c'est-à-dire le deuxième choix qui apparait)
	 * pour le stadeSexe père donné et ce groupe.
	 * @return
	 */
	public List<StadeSexe> getStadeSexeFils(StadeSexe stadeSexePere){
		List<StadeSexeHierarchieDansGroupe> sshdgs =
				StadeSexeHierarchieDansGroupe.find.where()
						.eq("stade_sexe_pere", stadeSexePere)
						.eq("groupe",this).orderBy("position").findList();
		List<StadeSexe> stadesexes = new ArrayList<StadeSexe>();
		for(StadeSexeHierarchieDansGroupe sshdg : sshdgs){
			stadesexes.add(sshdg.stade_sexe);
		}
		return stadesexes;
	}


	/**
	 * Renvoie tous les stades sexes pour le groupe donné.
	 * @return
	 */
	public List<StadeSexe> getStadesSexes(){
		List<StadeSexeHierarchieDansGroupe> sshdgs =
				StadeSexeHierarchieDansGroupe.find.where()
						.eq("groupe",this).orderBy("stade_sexe.stade_sexe_id").findList();
		List<StadeSexe> stadesexes = new ArrayList<StadeSexe>();
		for(StadeSexeHierarchieDansGroupe sshdg : sshdgs){
			stadesexes.add(sshdg.stade_sexe);
		}
		return stadesexes;
	}


	/************************** gestion des dates charnières ************************/

	/**
	 * Renvoie la liste des dates charnières du groupe triées par ordre
	 * chronologiquement croissant.
	 * @return
	 */
	public List<DateCharniere> getDatesCharnieres(){
		return DateCharniere.find.where().eq("date_charniere_groupe", this)
				.orderBy("date_charniere_date").findList();
	}



	/************************** getters des espèces contenues  ************************/


	/**
	 * Renvoie la liste de toutes les espèces dans la hiérarchie fille de ce groupement
	 * @return
	 */
	public List<Espece> getAllEspecesInThis(){
		List<Espece> esp = getEspecesInThis();
		if (!getFils().isEmpty()) {
			for (Groupe fils : this.getFils()) {
				List<Espece> especesFilles = fils.getEspecesInThis();
				if (especesFilles != null) {
					esp.addAll(especesFilles);
				}
			}
		}
		return esp;
	}

	public List<Espece> getEspecesInThis(){
		List<Espece> especes = new ArrayList<Espece>();
		List<EspeceIsInGroupementLocal> listeRelations = EspeceIsInGroupementLocal.find.where().eq("groupe",this).findList();
		for (EspeceIsInGroupementLocal relation : listeRelations){
			especes.add(relation.espece);
		}
		return especes;
	}

	/**
	 * Renvoie la liste des espèces dans ce groupe triées par systématique.
	 * @return
	 */
	public List<Espece> getEspecesInThisBySystematique(){
		List<Espece> especes = getAllEspecesInThis();
		if (especes != null) {
			Collections.sort(especes, new Espece());
		}
		return especes;
	}

	/**
	 * Renvoie la liste des espèces dans ce groupe par ordre alphabétique.
	 * @return
	 */
	public List<Espece> getEspecesInThisByAlpha(){
		List<Espece> especes = getAllEspecesInThis();
		if (especes != null) {
			Collections.sort(especes, new Comparator<Espece>() {
				@Override
				public int compare(Espece arg0, Espece arg1) {
					if ((arg0 == null) || (arg1 == null)) return 0;
					else if ((arg0.espece_nom == null) || (arg1.espece_nom == null)) return 0;
					else return arg0.espece_nom.compareToIgnoreCase(arg1.espece_nom);
				}
			});
		}
		return especes;
	}

	/************************** hiérarchie locale ************************/

	/**
	 * @returnla liste des groupes inclus dans ce groupe
	 */
	public List<Groupe> getFils(){
		return Groupe.find.where().eq("groupe_pere", this).findList();
	}


	/**
	 * TODO organiser la liste du plus haut au plus bas?
	 * @return la liste des groupes parents du plus bas au plus haut : groupe ne peut appartenir qu'à un groupe
	 */
	public List<Groupe> getHierarchieLocale(){
		List<Groupe> h = new ArrayList<Groupe>();
		if (groupe_pere!=null) {
			h.add(groupe_pere);
			if (groupe_pere.groupe_pere != null) {
				groupe_pere.groupe_pere.getHierarchieLocale();
			}
		}
		return h;
	}

	public static List<Groupe> findGroupesSansPere(){
		List<Groupe> listeGroupes = find.where().eq("groupe_pere", null).findList();

		// on supprime de la liste les groupes
		for (ListIterator<Groupe> it = listeGroupes.listIterator(); it.hasNext();) {
			Groupe groupe = it.next();
			if((groupe != null) && (groupe.groupe_type != null)) {
				if (groupe.groupe_type.equals(TypeGroupementLocal.getTypeGroupe())) {
					it.remove();
				}
			}
		}
		return listeGroupes;
	}

	/************************** hiérarchie scientifique ************************/

//	public List<GroupementScientifique> getGroupementsScientifiques(String type){
//		if
//		return OrdreHasSousGroupe.find.where().eq("sous_groupe", this).findList();
//	}


	// supprimer
	
	/**
	 * Supprimer le groupe de la base de données et toutes ses références
	 * dans les autres tables
	 */
	public void supprimer() {
		List<Groupe> listeGroupesFils = getFils();
		for(Groupe groupeFils : listeGroupesFils){
			groupeFils.supprimer();
		}
		List<MembreIsExpertOnGroupe> mieogs = MembreIsExpertOnGroupe.find.where().eq("groupe", this).findList();
		for(MembreIsExpertOnGroupe mieog : mieogs){
			mieog.delete();
		}
		List<StadeSexeHierarchieDansGroupe> sshdsgs = StadeSexeHierarchieDansGroupe.find.where().eq("groupe", this).findList();
		for(StadeSexeHierarchieDansGroupe sshdsg : sshdsgs){
			sshdsg.delete();
		}
		List<DateCharniere> dates_charnieres = DateCharniere.find.where().eq("date_charniere_groupe", this).findList();
		for(DateCharniere date_charniere : dates_charnieres){
			date_charniere.delete();
		}
		this.delete();
	}
	
}
