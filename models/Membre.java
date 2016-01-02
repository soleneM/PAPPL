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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import functions.credentials.Credentials;
import functions.credentials.PasswordHash;
import functions.mail.VerifierMail;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import play.Play;
import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class Membre extends Model {
	@Id
	public Integer membre_id;
	public String membre_civilite;
	@NotNull
	public String membre_nom;
	public String membre_adresse;
	public String membre_adresse_complement;
	public String membre_code_postal;
	public String membre_ville;
	public String membre_pays;
	@NotNull
	@ManyToOne
	public Confidentialite membre_confidentialite;
	@NotNull
	public boolean membre_abonne;
	@NotNull
	public boolean membre_temoin_actif;
	public Integer membre_journais;
	public Integer membre_moisnais;
	public Integer membre_annenais;
	public Integer membre_jourdece;
	public Integer membre_moisdece;
	public Integer membre_annedece;
	@Column(columnDefinition="TEXT")
	public String membre_biographie;
	public String membre_email;
	public String membre_mdp_hash;
	public String membre_tel;
	@NotNull
	@ManyToOne
	public Droits membre_droits;
	@NotNull
	public boolean membre_inscription_acceptee;
	public String membre_lien_de_validation_de_mail;

	public static Finder<Integer,Membre> find = new Finder<Integer,Membre>(Integer.class, Membre.class);

	/**
	 * Créé un membre standard sans droit particulier sans que celui-ci soit accepté par l'admin.
	 * @param nom
	 * @param civilite
	 * @param email
	 * @param passw
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public Membre(String nom, String civilite, String email, String passw) throws NoSuchAlgorithmException, InvalidKeySpecException {
		membre_nom=nom;
		membre_civilite=civilite;
		membre_email=email;
		membre_mdp_hash=PasswordHash.createHash(passw);
		membre_confidentialite=Confidentialite.OUVERTE;
		membre_abonne=false;
		membre_temoin_actif=true;
		membre_droits=Droits.TEMOIN;
		membre_inscription_acceptee=false;
		this.genereLienDeValidation();
	}

	/**
	 * Ajoute un témoin passif.
	 * @param telephone 
	 */
	public Membre(String civilite, String nom, String email, String adresse,
			String complement, String cp, String ville, String pays,
			String journais, String moisnais, String annenais, String jourdece,
			String moisdece, String annedece, String telephone, String biographie, String confidentialite) {
		this.membre_civilite=civilite;
		this.membre_nom=nom;
		this.membre_email=email;
		this.membre_adresse=adresse;
		this.membre_adresse_complement=complement;
		this.membre_code_postal=cp;
		this.membre_ville=ville;
		this.membre_pays=pays;
		this.membre_tel=telephone;
		this.membre_biographie=biographie;
		this.membre_abonne=false;
		if(confidentialite.equals("casparcas"))
			this.membre_confidentialite=Confidentialite.CASPARCAS;
		else
			this.membre_confidentialite=Confidentialite.OUVERTE;
		this.membre_droits=Droits.TEMOIN;
		this.membre_temoin_actif=false;
		this.membre_inscription_acceptee=true;
		if(!annenais.equals("")){
			this.membre_annenais=Integer.parseInt(annenais);
			if(!moisnais.equals("")){
				this.membre_moisnais=Integer.parseInt(moisnais);
				if(!journais.equals("")){
					this.membre_journais=Integer.parseInt(journais);
				}
			}
		}
		if(!annedece.equals("")){
			this.membre_annedece=Integer.parseInt(annedece);
			if(!moisdece.equals("")){
				this.membre_moisdece=Integer.parseInt(moisdece);
				if(!jourdece.equals("")){
					this.membre_jourdece=Integer.parseInt(jourdece);
				}
			}
		}
	}

	public static List<Membre> findAll(String orderBy, String sortDirection){
		return find.orderBy(orderBy+" "+sortDirection).findList();
	}

	//Fonctions de tri de la liste des membres
	/********************************************/
	public static List<Membre> selectMembresTemoinActif(Boolean isTemoinActif){
		return find.where().eq("membre_temoin_actif",isTemoinActif).orderBy("membre_nom").findList();
	}

	public static List<Membre> selectMembresAbonne(Boolean isAbonne){
		return find.where().eq("membre_abonne",isAbonne).orderBy("membre_nom").findList();
	}

	public static List<Membre> selectMembresConfidentialite(Integer confidentialite){
		return find.where().eq("membre_confidentialite.confidentialite_id",confidentialite).orderBy("membre_nom").findList();
	}

	public static List<Membre> selectMembresDroits(Integer droits){
		return find.where().eq("membre_droits.droits_id",droits).orderBy("membre_nom").findList();
	}

	public static List<Membre> selectMembresInscrit(Boolean isInscrit){
		return find.where().eq("membre_inscription_acceptee",isInscrit).orderBy("membre_nom").findList();
	}

	public static List<Membre> selectMembres(String nom){
		return find.where().eq("membre_nom",nom).orderBy("membre_nom").findList();
	}
	/************************************/

	/**
	 * Valide l'inscription d'un utilisateur et envoie un mail de validation
	 * @param membre_id
	 */

	public static void valideMembre(Integer membre_id){
		Membre membre = Membre.find.byId(membre_id);
		membre.membre_inscription_acceptee=true;
		membre.save();
		VerifierMail.envoyerMailAcceptation(membre);
	}

	/**
	 * invalide une demande d'inscription en supprimant le membre concerné de la base de donnée.
	 * @param id
	 */
	public static void supprimeMembre(Integer id){
		Membre membre=Membre.find.byId(id);
		membre.delete();
	}
	
	@Override
	public String toString(){
		return membre_nom;
	}

	/**
	 * Renvoie vrai si l'adresse mail n'est pas activée, faux sinon.
	 * @return
	 */
	public boolean adresseMailNonActivee() {
		return membre_lien_de_validation_de_mail!=null;
	}

	/**
	 * Renvoie vrai si l'inscription a été acceptée par l'admin, faux sinon.
	 * @return
	 */
	public boolean inscriptionValidee() {
		return membre_inscription_acceptee;
	}

	/**
	 * Renvoie la liste des groupes (sous forme de MembreIsExpertOnGroupe)
	 * dont le membre est expert.
	 * @return
	 */
	public List<MembreIsExpertOnGroupe> getGroupesDExpertise(){
		return MembreIsExpertOnGroupe.find.where().eq("membre",this).findList(); 
	}
	
	/**
	 * Indique si le membre a un statut de Membre
	 * @return
	 */
	public boolean isMembre() {
		if (membre_droits == null)
			return false;
		else
			return membre_droits.isMembre();
	}

	/**
	 * Indique si le membre a un statut d'expert
	 * @return
	 */
	public boolean isExpert() {
		if (membre_droits == null)
			return false;
		else
			return membre_droits.isExpert();
	}

	/**
	 * Indique si le membre a un statut d'admin
	 * @return
	 */
	public boolean isAdmin() {
		if (membre_droits == null)
			return false;
		else
			return membre_droits.isAdmin();
	}

	/**
	 * Génère un lien de validation pour le membre.
	 * _NE_ l'écrit _PAS_ dans la base de données.
	 * Si nécessaire, faire manuellement un .save() après.
	 */
	public void genereLienDeValidation(){
		String lien = Credentials.genereLienAleatoire(10);
		while(!Membre.find.where().eq("membre_lien_de_validation_de_mail", lien).findList().isEmpty()){
			lien = Credentials.genereLienAleatoire(10);
		}
		membre_lien_de_validation_de_mail=lien;
		this.save();
	}
	
	/**
	 * Renvoie l'adresse mail d'un admin.
	 * @return
	 */
	public static String getAdminMail(){
		return Play.application().configuration().getString("contact.mail");
	}

	/**
	 * renvoie la liste de toutes les fiches auxquelles est lié le membre
	 * @return
	 */
	public List<Fiche> getFiches(){
		List<FicheHasMembre> lsfhm = FicheHasMembre.find.where().eq("membre", this).findList();
		List<Fiche> lsFiches = new ArrayList<Fiche>();
		for (FicheHasMembre fhm : lsfhm){
			lsFiches.add(fhm.fiche);
		}
		return lsFiches;
	}
	
}
