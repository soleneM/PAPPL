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

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class Espece extends Model implements Comparator<Espece>{
	@Id
	public Integer espece_id;
	@NotNull
	public String espece_nom;
	@NotNull
	public String espece_auteur;
	@NotNull
	public Integer espece_systematique;
	@ManyToOne
	public Image espece_photo;
	@ManyToOne
	public GroupementScientifique espece_groupement_scientifique_pere;
	@Column(columnDefinition="TEXT")
	public String espece_commentaires;

	public static Finder<Integer,Espece> find = new Finder<Integer,Espece>(Integer.class, Espece.class);

	// constructeurs

	public Espece() {}

	/**
	 * Créé une nouvelle espèce. Attention, on ne peux pas enregistrer cette espèce dans la base
	 * de données tout de suite avec la méthode save() ! Il faut utiliser ajouterNouvelleEspece().
	 * @param nom
	 * @param auteur
	 * @param systematique
	 * @param commentaires
	 */
	public Espece(String nom, String auteur, Integer systematique, String commentaires){
		espece_nom=nom;
		espece_auteur=auteur;
		espece_systematique=systematique;
		espece_commentaires=commentaires;
		espece_groupement_scientifique_pere=null;
	}
	/**
	 * Idem que le constructeur normal, mais avec un photo en plus
	 * @param nom
	 * @param auteur
	 * @param systematique
	 * @param commentaires
	 * @param photo
	 */
	public Espece(String nom, String auteur, Integer systematique, String commentaires, Image photo){
		espece_nom=nom;
		espece_auteur=auteur;
		espece_systematique=systematique;
		espece_commentaires=commentaires;
		espece_photo=photo;
		espece_groupement_scientifique_pere=null;
	}
	
	public static List<Espece> findAll(){
		return find.orderBy("espece_systematique").findList();
	}
	public static List<Espece> findAllByAlpha(){
		return find.orderBy("espece_nom").findList();
	}
	
	//Fonctions de filtres de la liste des insectes

	/*************************************************/
	/*
	public static List<Espece> selectEspecesSousFamille(SousFamille sousfam){
		return find.where().eq("espece_sousfamille", sousfam).orderBy("espece_systematique").findList();
	}
	public static List<Espece> selectEspecesFamille(Famille fam){
		return find.where().eq("espece_sousfamille.sous_famille_famille", fam).orderBy("espece_systematique").findList();
	}
	public static List<Espece> selectEspecesSuperFamille(SuperFamille superfam){
		return find.where().eq("espece_sousfamille.sous_famille_famille.famille_super_famille", superfam).orderBy("espece_systematique").findList();
	}
	public static List<Espece> selectEspecesOrdre(Ordre ordre){
		return find.where().eq("espece_sousfamille.sous_famille_famille.famille_super_famille.super_famille_ordre", ordre).orderBy("espece_systematique").findList();
	}
	public static List<Espece> selectEspecesSousGroupe(Groupe sousg){
		return find.where().eq("espece_sous_groupe", sousg).orderBy("espece_systematique").findList();
	}
	public static List<Espece> selectEspecesGroupe(Groupe groupe){
		return find.where().eq("espece_sous_groupe.sous_groupe_groupe",groupe).orderBy("espece_systematique").findList();
	}*/

	@Override
	public String toString(){
		return espece_systematique+". "+espece_nom+"-"+espece_auteur;
	}
	/**
	 * Pour trier les listes d'especes selon la systématique
	 * Le constructeur vide est là pour utliser le comparateur.
	 */
	@Override
	public int compare(Espece e1, Espece e2) {
		return (e1.espece_systematique<e2.espece_systematique ? -1 : (e1.espece_systematique==e2.espece_systematique ? 0 : 1));
	}

	/**
	 * Renvoie la liste des synonymes d'une espèce
	 *@return
	 */
	public List<EspeceSynonyme> getSynonymes(){
		return EspeceSynonyme.find.where().eq("synonyme_espece.espece_id", this.espece_id).findList();
	}

	/**
	 * Renvoie la plus vieille fiche contenant un témoignage de l'espèce en question
	 * Renvoie null si l'espèce n'a jamais été observée.
	 * @return
	 */
	public Fiche getPlusVieuxTemoignage(){
		Observation o = Observation.find.where()
				.eq("observation_espece",this)
				.eq("observation_validee",Observation.VALIDEE)
				.setMaxRows(1).orderBy("observation_fiche.fiche_date").findUnique();
		if(o==null)
			return null;
		else
			return o.getFiche();
	}

	/**
	 * Ajoute l'espèce en réordonnant toutes les espèces qui suivent.
	 * Ajoute une espèce au milieu ou début. Instancier la nouvelle espèce avant.
	 * @param avecSousFamille
	 * @param sousFamilleOuFamille
	 * @throws NamingException
	 * @throws PersistenceException
	 */
	public void ajouterNouvelleEspece(boolean avecSousFamille, Integer sousFamilleOuFamilleId){

	}
//	public void ajouterNouvelleEspece(boolean avecSousFamille, Integer sousFamilleOuFamilleId) throws NamingException, PersistenceException{
//		if(avecSousFamille){
//			this.espece_sousfamille=SousFamille.find.byId(sousFamilleOuFamilleId);
//			if(espece_sousfamille!=null){
//				this.save();
//			}else{
//				throw new NamingException("La sous-famille "+sousFamilleOuFamilleId+" n'existe pas !");
//			}
//		}
//		else{
//			this.espece_sousfamille=new SousFamille(this.espece_nom,false,sousFamilleOuFamilleId);
//			this.espece_sousfamille.save();
//			this.save();
//		}
//		List<Espece> especes = Espece.find.where().ge("espece_systematique",this.espece_systematique).findList();
//		for(Espece e : especes){
//			if(!e.espece_nom.equals(this.espece_nom)){
//				e.espece_systematique++;
//				e.save();
//			}
//		}
//		this.metAJourSousGroupes();
//	}

	/***************************  hiérarchie locale *******************************/
	/* il pourrait il y avoir plusieurs hiérarchies locales. c'est à dire qy'une espèce pourrait appartenir à plusieurs groupes
	* mais cela c'est pas implémenter dans l'application en elle même */


	/**
	 * Renvoie le groupe (celui de plus haut niveau) auquel appartient l'espèce.
	 * Si aucun groupe n'est défini, renvoie null.
	 * Attention, si les groupes ont été définis de telle
	 * sorte qu'une espèce est dans plusieurs groupes (ce
	 * qui est une erreur), la fonction de renvoiera qu'un seul
	 * groupe
	 * @return Groupe s'il existe, null sinon
	 */
	//TODO à supprimer : une espèce peut être dans PLUSIEURS groupes
	public Groupe getGroupe(){

		// choix arbitraire d'une des branches hiérarchiques
		List<Groupe> ls =  getHierarchiesLocales().get(0);
		if (!ls.isEmpty()) {
			return ls.get(ls.size() - 1);
		} else {
			return null;
		}
	}

	/**
	 *
	 * @return la liste des groupes dont l'espece est directement fille. (une espèce peut appartenir à plusieurs groupes)
	 */
	public List<Groupe> getGroupesPeres(){
		List<Groupe> gPeres = new ArrayList<Groupe>();
		List<EspeceIsInGroupementLocal> listeRelations = EspeceIsInGroupementLocal.find.where().eq("espece",this).findList();
		
		for (EspeceIsInGroupementLocal relation : listeRelations){
			gPeres.add(relation.groupe);	
		}
		return gPeres;
	}
	

	/**
	 *
	 * @return la liste des hierarchies locales c-à-d les différentes branches de groupement locaux qui mènent à cette espèce. Du niveau le plus bas au plus haut.
	 */
	public List<List<Groupe>> getHierarchiesLocales(){

		List<List<Groupe>> hierarchiesLocales = new ArrayList<List<Groupe>>();
		List<Groupe> listeGroupesPeres = getGroupesPeres();

		for (Groupe groupePere : listeGroupesPeres){

			//on récupère la hierarchie complete du groupe Pere, lui y compris
			List<Groupe> groupePereHierarchie = new ArrayList<Groupe>();
			groupePereHierarchie.add(groupePere);
			groupePereHierarchie.addAll(groupePere.getHierarchieLocale());

			// l'ajoute à la liste des hiérarchie locales
			hierarchiesLocales.add(groupePereHierarchie);
		}
		return hierarchiesLocales;
	}

	/**
	 * Renvoie la liste des espèces sans groupe.
	 * @return
	 */
	public static List<Espece> findEspecesSansGroupe(){
		List<Espece> listeEspeces = Espece.findAll();
		// on supprime de la liste les especes qui une relation avec un groupe
		for (ListIterator<Espece> it = listeEspeces.listIterator(); it.hasNext();) {
			Espece espece = it.next();
			if (!EspeceIsInGroupementLocal.find.where().eq("espece" ,espece).findList().isEmpty()){
				it.remove();
			}
		}
		return listeEspeces;
	}


	/**
	 * Trouve les espèces ajoutables dans un sous-groupe
	 * @return
	 */
	//TODO à supprimer ?
	public static List<Espece> findEspecesAjoutablesDansSousGroupe(){
		return findEspecesSansGroupe();
	}


	/***************************  hiérarchie scientifique *******************************/
	/* la hiérarchie scientifique est unique */

	/**
	 * Renvoie la liste des espèces sans groupement pere.
	 * @return
	 */
	public static List<Espece> findEspecesSansGroupementScientifique(){
		return Espece.find.where().eq("espece_groupement_scientifique_pere", null).findList();
	}

	/**
	 * @return la liste de tous les groupements scientiques pères (du plus bas au plus haut)
	 */
	public List<GroupementScientifique> getHierarchieScientifique(){
		List<GroupementScientifique> h = new ArrayList<GroupementScientifique>();
		if (espece_groupement_scientifique_pere!=null) {
			h.add( espece_groupement_scientifique_pere);
			//h.add(0, espece_groupement_scientifique_pere); // TODO du plus haut au plus bas?
			h.addAll(espece_groupement_scientifique_pere.getHierarchieScientifique());
		}
		return h;
	}

	/**
	 * @param type libellé du niveau hiérarchique (famille, sous-famille etc...)
	 * @return groupement correspondant si existant sinon null
	 */
	public GroupementScientifique getNiveauHeriarchiqueScientifique (String type){
		List<GroupementScientifique> h = getHierarchieScientifique();
		TypeGroupementScientifique t = TypeGroupementScientifique.find.where().eq("intitule",type).findUnique();

		GroupementScientifique retour = null;

		for (GroupementScientifique g : h){
			if (g.groupement_scientifique_type == t){
				retour = g;
			}
		}
		return retour;
	}


	// suppression
	
	/**
	* Supprime l'espèce et tous les témoignages qui lui sont associés
	*/
	public static void supprEspece(Integer espece_id){
		Espece espece = find.byId(espece_id);
		List<Espece> especesApres = find.where().gt("espece_systematique",espece.espece_systematique).findList();
		for (Espece e : especesApres){
			e.espece_systematique--;
			e.save();
		}
		espece.delete();
	}

}
