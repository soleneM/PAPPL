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

import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class UTMS extends Model {
	@Id
	@Column(columnDefinition="VARCHAR(4)")
	public String utm;
	
	@Column(columnDefinition="VARCHAR(4)")
	public String maille20x20;
	
	@Column(columnDefinition="VARCHAR(4)")
	public String maille50x50;
	
	@Column(columnDefinition="VARCHAR(4)")
	public String maille100x100;
	
	public static Finder<String,UTMS> find = new Finder<String,UTMS>(String.class, UTMS.class);

	public static List<UTMS> findAll(){
		return find.orderBy("utm").findList();
	}
	
	@Override
	public String toString(){
		return utm;
	}

	/**
	 * Convertie une string en liste de mailles UTM.
	 * Si la string en argument est vide, renvoit toutes les mailles.
	 * Si la string est une maille, renvoit la liste des maills utms 10x10 dans
	 * cette maille.
	 * Si la string ne correspond à rien, renvoie null.
	 * @param maille
	 * @return
	 */
	public static List<UTMS> parseMaille(String maille) {
		if(maille.equals(""))
			return find.all();
		List<UTMS> utms = new ArrayList<UTMS>();
		UTMS utm = find.byId(maille);
		if(utm!=null){
			utms.add(utm);
			return utms;
		}
		utms = find.where().eq("maille20x20", maille).findList();
		if(!utms.isEmpty())
			return utms;
		utms = find.where().eq("maille50x50", maille).findList();
		if(!utms.isEmpty())
			return utms;
		utms = find.where().eq("maille100x100", maille).findList();
		if(!utms.isEmpty())
			return utms;
		else
			return null;
	}
}
