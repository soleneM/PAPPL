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
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import play.db.ebean.Model;

/**
 * Source des données :
 * http://sql.sh/736-base-donnees-villes-francaises
 * @author malik
 *
 */
@SuppressWarnings("serial")
@Entity
public class Commune extends Model {
	@Id
	@Column(columnDefinition="MEDIUMINT(8) UNSIGNED")
	public Integer ville_id;

	@ManyToOne
	public Departement ville_departement;

	public String ville_slug;
	@Column(columnDefinition="VARCHAR(45)")
	@NotNull
	public String ville_nom;
	@NotNull
	@Column(columnDefinition="VARCHAR(45)")
	public String ville_nom_reel;
	@Column(columnDefinition="VARCHAR(20)")
	public String ville_nom_soundex;
	@Column(columnDefinition="VARCHAR(22)")
	public String ville_nom_metaphone;
	public String ville_code_postal;
	@Column(columnDefinition="VARCHAR(3)")
	public String ville_commune;
	@Column(columnDefinition="VARCHAR(5)")
	public String ville_code_commune;
	@Column(columnDefinition="SMALLINT(3) UNSIGNED")
	public Integer ville_arrondissement;
	@Column(columnDefinition="VARCHAR(4)")
	public String ville_canton;
	@Column(columnDefinition="SMALLINT(5) UNSIGNED")
	public Integer ville_amdi;
	@Column(columnDefinition="MEDIUMINT(11) UNSIGNED")
	public Integer ville_population_2010;
	@Column(columnDefinition="MEDIUMINT(11) UNSIGNED")
	public Integer ville_population_1999;
	@Column(columnDefinition="MEDIUMINT(10) UNSIGNED")
	public Integer ville_population_2012;
	@Column(columnDefinition="INT(11) UNSIGNED")
	public Integer ville_densite_2010;
	@Column(columnDefinition="MEDIUMINT(7) UNSIGNED")
	public Integer ville_surface;
	@Column(columnDefinition="FLOAT")
	public Float ville_longitude_deg;
	@Column(columnDefinition="FLOAT")
	public Float ville_latitude_deg;
	@Column(columnDefinition="VARCHAR(9)")
	public String ville_longitude_grd;
	@Column(columnDefinition="VARCHAR(8)")
	public String ville_latitude_grd;
	@Column(columnDefinition="VARCHAR(9)")
	public String ville_longitude_dms;
	@Column(columnDefinition="VARCHAR(8)")
	public String ville_latitude_dms;
	@Column(columnDefinition="MEDIUMINT(4)")
	public String ville_zmin;
	@Column(columnDefinition="MEDIUMINT(4)")
	public String ville_zmax;
	@Column(columnDefinition="INT(10) UNSIGNED")
	public Integer ville_population_2010_order_france;
	@Column(columnDefinition="INT(10) UNSIGNED")
	public Integer ville_densite_2010_order_france;
	@Column(columnDefinition="INT(10) UNSIGNED")
	public Integer ville_surface_order_france;
	public String ville_nom_aer;

	public static Finder<Integer,Commune> find = new Finder<Integer,Commune>(Integer.class, Commune.class);

	@Override
	public String toString(){
		return ville_nom_aer;
	}

	public static List<Commune> findAll(){
		return find.orderBy("ville_nom_reel").findList();
	}
	
	/**
	 * Remplace les abréviations pour Saint(e) et enlève les articles définis.
	 * Trouve ensuite la commune ayant ce nom.
	 * @param commune_nom
	 * @return
	 */
	public static Commune findFromNomApproximatif(String commune_nom) {
		if(commune_nom.equals("Bonnoeuvre"))
			commune_nom="Bonnœuvre";
		else if(commune_nom.equals("Paimboeuf"))
			commune_nom="Paimbœuf";
		else{
			commune_nom=commune_nom.replaceAll("St-", "Saint-");
			commune_nom=commune_nom.replaceAll("Ste-", "Sainte-");
			if(commune_nom.startsWith("Le ") || commune_nom.startsWith("La "))
				commune_nom=commune_nom.substring(3);
			else if(commune_nom.startsWith("Les "))
				commune_nom=commune_nom.substring(4);
			else if(commune_nom.startsWith("L'"))
				commune_nom=commune_nom.substring(2);
		}
		return find.where().eq("ville_nom_reel", commune_nom).findUnique();
	}
}
