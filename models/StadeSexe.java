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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class StadeSexe extends Model{
	@Id
	public Integer stade_sexe_id;
	@NotNull
	public String stade_sexe_intitule;
	@Column(columnDefinition="TEXT")
	public String stade_sexe_explication;

	public static Model.Finder<Integer,StadeSexe> find = new Model.Finder<Integer,StadeSexe>(Integer.class, StadeSexe.class);

	public static List<StadeSexe> findAll(){
		return find.where().orderBy("stade_sexe_id").findList();
	}

	@Override
	public String toString(){
		return stade_sexe_intitule;
	}

	/**
	 * Renvoie tous les fils du stade sexe pour le groupe donné.
	 * Si le groupe est null, alors renvoie tous les fils possibles (en faisant l'union des groupes)
	 * @param groupe
	 * @return
	 */
	public List<StadeSexe> getStadeSexeFilsPourTelGroupe(Groupe groupe){
		if(groupe!=null){
			List<StadeSexeHierarchieDansGroupe> sshdgs =
					StadeSexeHierarchieDansGroupe.find.where()
					.eq("stade_sexe_pere", this)
					.eq("groupe",groupe).orderBy("position").findList();
			List<StadeSexe> stadesexes = new ArrayList<StadeSexe>();
			for(StadeSexeHierarchieDansGroupe sshdg : sshdgs){
				stadesexes.add(sshdg.stade_sexe);
			}
			return stadesexes;
		}else{
			if(this.stade_sexe_intitule.equals("Adulte vivant")){
				List<Integer> stades_ids = new ArrayList<Integer>();
				stades_ids.add(10);
				stades_ids.add(11);
				stades_ids.add(12);
				stades_ids.add(13);
				return find.where().in("stade_sexe_id", stades_ids).findList();
			}else if(this.stade_sexe_intitule.equals("Traces")){
				List<Integer> stades_ids = new ArrayList<Integer>();
				stades_ids.add(20);
				stades_ids.add(21);
				stades_ids.add(22);
				stades_ids.add(23);
				stades_ids.add(24);
				stades_ids.add(25);
				stades_ids.add(26);
				stades_ids.add(27);
				stades_ids.add(28);
				return find.where().in("stade_sexe_id", stades_ids).findList();
			}else
				return new ArrayList<StadeSexe>();
		}
	}

	/**
	 * Renvoie le père du stade sexe précis pour le groupe donné.
	 * S'il n'y a pas de père, renvoie le state sexe précis (celui ci)
	 * @param groupe
	 * @return
	 */
	public StadeSexe getStadeSexePerePourTelGroupe(Groupe groupe){
		 StadeSexe s = StadeSexeHierarchieDansGroupe.find
						.where()
						.eq("stade_sexe", this)
						.eq("groupe", groupe).findUnique().stade_sexe_pere;
		if (s != null){
			return s;
		} else {
			return this;
		}
	}

	/**
	 * Renvoie la liste des stades imagos.
	 * @return
	 */
	public static List<StadeSexe> getStadesImagos() {
		List<Integer> imagos = new ArrayList<Integer>();
		imagos.add(1);
		imagos.add(10);
		imagos.add(11);
		imagos.add(12);
		imagos.add(13);
		return find.where().in("stade_sexe_id", imagos).findList();
	}
}
