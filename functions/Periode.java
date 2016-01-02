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
package functions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.DateCharniere;
import models.Fiche;
import models.Groupe;

public class Periode {
	private Calendar date1;
	private Calendar date2;
	
	public Periode(Calendar date1, Calendar date2){
		this.date1=date1;
		this.date2=date2;
	}
	
	public Calendar getDate1(){
		return date1;
	}
	public Calendar getDate2(){
		return date2;
	}
	
	/**
	 * Donne la liste des périodes pour un groupe donné.
	 * @param groupe
	 * @return
	 */
	public static List<Periode> getPeriodes(Groupe groupe){
		List<Periode> periodes = new ArrayList<Periode>();
		List<DateCharniere> dates_charnieres = DateCharniere.find.where().eq("date_charniere_groupe", groupe).orderBy("date_charniere_date").findList();
		Calendar date1 = Fiche.getPlusVieuxTemoignage().fiche_date;
		if (date1 != null) {
			Calendar date2;
			boolean premierePeriode = true;
			for(DateCharniere date_charniere : dates_charnieres){
				date2 = date_charniere.date_charniere_date;
				Calendar date1plusUnJour = (Calendar) date1.clone();
				if(!premierePeriode)
					date1plusUnJour.add(Calendar.DAY_OF_YEAR, 1);
				periodes.add(new Periode(date1plusUnJour,date2));
				date1.setTime(date2.getTime());
				premierePeriode = false;
			}
			date2=Calendar.getInstance();
			if(!premierePeriode)
				date1.add(Calendar.DAY_OF_YEAR, 1);
			periodes.add(new Periode(date1,date2));
		}
		return periodes;
	}
	
	/**
	 * Renvoie la concaténation date1+":"+date2 au fromat dd/MM/yyyy
	 */
	@Override
	public String toString(){
		SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
		if ((date1 == null) && (date2 == null))
			return "dd/MM/yyyy:dd/MM/yyyy";
		else if (date2 == null)
			return date_format.format(date1.getTime())+":dd/MM/yyyy";
		else if (date1 == null)
			return "dd/MM/yyyy:"+date_format.format(date2.getTime());
		else
			return date_format.format(date1.getTime())+":"+date_format.format(date2.getTime());
	}
}
