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

import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class InformationsComplementaires extends Model {
	@Id
	public Long informations_complementaires_id;
	@NotNull
	@ManyToOne
	public Observation informations_complementaires_observation;
	@Column(columnDefinition="INT UNSIGNED")
	public Integer informations_complementaires_nombre_de_specimens;
	@ManyToOne
	public StadeSexe informations_complementaires_stade_sexe;
	
	public static Finder<Long,InformationsComplementaires> find = new Finder<Long,InformationsComplementaires>(Long.class, InformationsComplementaires.class);

	public InformationsComplementaires(Observation observation, Integer nombreSpecimens, StadeSexe stade_sexe) {
		informations_complementaires_observation=observation;
		if(nombreSpecimens==null || nombreSpecimens<=0)
			informations_complementaires_nombre_de_specimens = null;
		else
			informations_complementaires_nombre_de_specimens=nombreSpecimens;
		informations_complementaires_stade_sexe=stade_sexe;
	}

	@Override
	public String toString(){
		return informations_complementaires_observation+", "
				+informations_complementaires_nombre_de_specimens+" specimens à l'état : "
				+informations_complementaires_stade_sexe;
	}
}
