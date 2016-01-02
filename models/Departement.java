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

import com.avaje.ebean.Expr;

import play.db.ebean.Model;

/**
 * Source des données :
 * http://sql.sh/1879-base-donnees-departements-francais
 * @author malik
 *
 */
@SuppressWarnings("serial")
@Entity
public class Departement extends Model {
	@Id
	@Column(columnDefinition="VARCHAR(3)")
	public String departement_code;
	public String departement_nom;
	public String departement_nom_uppercase;
	public String departement_slug;
	@Column(columnDefinition="VARCHAR(20)")
	public String departement_nom_soundex;
	
	public static Finder<Integer,Departement> find = new Finder<Integer,Departement>(Integer.class, Departement.class);

	public static List<Departement> findDepartementsAER(){
		return find.where().or(
					Expr.eq("departement_code","44"),
					Expr.or(
						Expr.eq("departement_code","85"),
						Expr.or(
							Expr.eq("departement_code","56"),
							Expr.or(
								Expr.eq("departement_code","35"),
								Expr.or(
									Expr.eq("departement_code","79"),
									Expr.eq("departement_code","17")
								)
							)
						)
					)
				).findList();
	}
	
	@Override
	public String toString(){
		return departement_code+" - "+departement_nom;
	}
}
