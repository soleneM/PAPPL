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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class DateCharniere extends Model {
	@Id
	public Integer date_charniere_id;
	@NotNull
	public Calendar date_charniere_date;
	@NotNull
	@ManyToOne
	public Groupe date_charniere_groupe;
	
	public static Finder<Integer,DateCharniere> find = new Finder<Integer,DateCharniere>(Integer.class, DateCharniere.class);

	public DateCharniere(Groupe groupe, Calendar c) {
		date_charniere_groupe=groupe;
		date_charniere_date=c;
	}

	@Override
	public String toString(){
		SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
		return date_format.format(date_charniere_date.getTime());
	}
}
