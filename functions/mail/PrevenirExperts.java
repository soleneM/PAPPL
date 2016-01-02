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
package functions.mail;

import java.util.List;
import java.util.concurrent.TimeUnit;

import models.MembreIsExpertOnGroupe;
import models.Observation;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import views.html.mails.mailDeRappelPourExperts;

public class PrevenirExperts {
	/**
	 * Prépare l'envoie de mails de rappel aux experts tous les 7 jours
	 */
	public static void scheduler(){
		PrevenirExperts.prevenirExperts();
		Akka.system().scheduler().scheduleOnce(
				Duration.create(7, TimeUnit.DAYS),
				new Runnable() {
					public void run() {
						scheduler();
					}
				},
				Akka.system().dispatcher()
				); 
	}

	public static void prevenirExperts() {
		for(MembreIsExpertOnGroupe mieog : MembreIsExpertOnGroupe.find.all()){
			List<Observation> observation = Observation.find.where()
					.eq("observation_vue_par_expert", false)
					.eq("observation_espece.espece_sous_groupe.sous_groupe_groupe", mieog.groupe)
					.findList();
			if(observation.size()>0){
				if(mieog.membre.membre_email!=null && !mieog.membre.membre_email.isEmpty()){
					Mail mail = new Mail("AER : Vous avez "+observation.size()+" observation(s) de "+mieog.groupe+" en suspens",
							mailDeRappelPourExperts.render(mieog,observation).toString(),
							mieog.membre.membre_email,
							mieog.membre.membre_nom);
					mail.sendMail();
				}else
					System.out.println(mieog.membre+ " est un expert sans adresse mail.");
			}
		}
	}

}
