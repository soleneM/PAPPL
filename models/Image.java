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
import javax.validation.constraints.NotNull;

import play.db.ebean.Model;

@Entity
@SuppressWarnings("serial")
public class Image extends Model {
	@Id
	public Long image_id;
	@NotNull
	public String image_chemin;
	public String image_nom;
	
	public static Finder<Long,Image> find = new Finder<Long,Image>(Long.class, Image.class);

	public Image(String image_nom){
		this.image_nom=image_nom;
		 this.image_chemin=this.idSuivante()+"_"+image_nom;
	}
	
	/**
	 * Trouve l'id suivante dans la table image.
	 * @return
	 */
	private Long idSuivante() {
		Image image = find.where().setMaxRows(1).orderBy("image_id desc").findUnique();
		if(image!=null)
			return image.image_id+1L;
		else
			return 1L;
	}

	/**
	 * Ajoute la route pour l'utiliser directement dans les templates
	 */
	@Override
	public String toString(){
		return "/image/"+image_chemin;
	}
}
