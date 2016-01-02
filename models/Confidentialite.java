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

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import play.db.ebean.Model;

/**
 * 3 niveaux de confidentialite :
 * confidentialite_id=1 -> on peut divulguer tout ses témoignages
 * confidentialite_id=2 -> on peut divulguer au cas par cas
 * confidentialite_id=3 -> on ne peut divulguer à personne
 * @author malik
 *
 */
@SuppressWarnings("serial")
@Entity
public class Confidentialite extends Model {
	//Déclaration des variables globles
	public static Confidentialite OUVERTE;
	public static Confidentialite CASPARCAS;
	public static Confidentialite FERMEE;
	
	@Id
	public Integer confidentialite_id;
	@NotNull
	public String confidentialite_intitule;
	@NotNull
	@Column(columnDefinition="TEXT")
	public String confidentialite_explication;
	
	public static Finder<Integer,Confidentialite> find = new Finder<Integer,Confidentialite>(Integer.class, Confidentialite.class);

	public static List<Confidentialite> findAll(){
		return find.all();
	}

	@Override
	public String toString(){
		return confidentialite_intitule;
	}
}
