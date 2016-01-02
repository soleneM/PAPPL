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
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class StadeSexeHierarchieDansGroupe extends Model {

	@Id
	public Integer StadeSexeHierarchieDansGroupe_id;
	@NotNull
	@ManyToOne
	public StadeSexe stade_sexe;
	@NotNull
	@ManyToOne
	public Groupe groupe;
	@ManyToOne
	public StadeSexe stade_sexe_pere;
	@NotNull
	public Integer position;
	
	public static Finder<Integer,StadeSexeHierarchieDansGroupe> find = new Finder<Integer,StadeSexeHierarchieDansGroupe>(Integer.class, StadeSexeHierarchieDansGroupe.class);


	public StadeSexeHierarchieDansGroupe(Groupe groupe, Integer stade_sexe_id, int position) {
		this.groupe=groupe;
		this.stade_sexe=StadeSexe.find.byId(stade_sexe_id);
		this.stade_sexe_pere=null;
		this.position=position;
	}

	public StadeSexeHierarchieDansGroupe(Groupe groupe, Integer stade_sexe_id, Integer stade_sexe_pere_id, int position) {
		this.groupe=groupe;
		this.stade_sexe=StadeSexe.find.byId(stade_sexe_id);
		this.stade_sexe_pere=StadeSexe.find.byId(stade_sexe_pere_id);
		this.position=position;
	}

	/**
	 * Verifie que le groupe et ce stade/sexe ne sont pas liés.
	 * @param groupe
	 * @param stade_sexe_id
	 * @return
	 */
	public static boolean nAPas(Groupe groupe, int stade_sexe_id) {
		return find.where().eq("groupe",groupe).eq("stade_sexe.stade_sexe_id", stade_sexe_id).findList().isEmpty();
	}
}
