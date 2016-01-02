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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import play.db.ebean.Model;

/**
 * 3 niveaux de droit :
 * droits_id=1 -> simple témoin
 * droits_id=2 -> expert
 * droits_id=3 -> admin
 * @author malik
 *
 */
@SuppressWarnings("serial")
@Entity
public class Droits extends Model {
	//Déclaration des variables globles
	public static Droits TEMOIN;
	public static Droits EXPERT;
	public static Droits ADMIN;
	
	@Id
	public Integer droits_id;
	@NotNull
	public String droits_intitule;
	
	public static Finder<Integer,Droits> find = new Finder<Integer,Droits>(Integer.class, Droits.class);
	
	public static List<Droits> findAll(){
		return find.all();
	}

	public Integer getId() {
		return droits_id;
	}
	
	public boolean isMembre() {
		return (droits_id == 1);
	}
	
	public boolean isExpert() {
		return (droits_id == 2);
	}
	
	public boolean isAdmin() {
		return (droits_id == 3);
	}
	
	@Override
	public String toString(){
		return droits_intitule;
	}
}
