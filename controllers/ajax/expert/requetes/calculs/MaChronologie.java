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
package controllers.ajax.expert.requetes.calculs;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.Fiche;
import models.FicheHasMembre;
import models.Membre;
import models.Observation;

public class MaChronologie {

	public static int TOUT = -1;
	public static int ELEMENTS_PAR_PAGE = 700;

	public List<Observation> chronologieTout = new ArrayList<Observation>();
	public List<Observation> chronologie = new ArrayList<Observation>();
	public int nombreTemoignages;

//	public List<Fiche> chronologieFichesTout = new ArrayList<Fiche>();
//	public List<Fiche> chronologieFiches = new ArrayList<Fiche>();

	public MaChronologie(Map<String,String> info, int page) throws ParseException {

		Membre temoin = Membre.find.where().eq("membre_nom", info.get("temoin")).findUnique();
		List<FicheHasMembre> fhms = FicheHasMembre.find.where().eq("membre", temoin).findList();
		List<Fiche> fiches = new ArrayList<Fiche>();
		for (FicheHasMembre fhm: fhms){
			fiches.add(0,fhm.fiche);
		}

		chronologieTout = Observation.find.where()
			.in("observation_fiche", fiches)
			.orderBy("observation_fiche.fiche_date desc")
			.findList();
		nombreTemoignages = chronologieTout.size();
		if(page!=TOUT && page<getNombreDePages())
			chronologie = chronologieTout.subList(page*ELEMENTS_PAR_PAGE, Math.min(nombreTemoignages, (page+1)*ELEMENTS_PAR_PAGE));
	}
	
	public int getNombreDePages(){
		return nombreTemoignages/ELEMENTS_PAR_PAGE+1;
	}
	
	public Date getDateAtPage(int page){
		if(page*ELEMENTS_PAR_PAGE<this.nombreTemoignages)
			return chronologieTout.get(page*ELEMENTS_PAR_PAGE).observation_fiche.fiche_date.getTime();
		else
			return Calendar.getInstance().getTime();
	}

}
