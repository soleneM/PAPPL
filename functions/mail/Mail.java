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

import play.Play;

import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;

public class Mail {
	private static String from = "AER <"+Play.application().configuration().getString("smtp.from")+">";

	private String sujet;
	private String contenu;
	private String to;
	private String recipient;

	/**
	 * Crée un mail
	 * @param sujet		Sujet du mail
	 * @param contenu	Contenu du mail
	 * @param to		Adresse mail de la personne à qui on envoie le mail
	 * @param recipient	Nom de la personne qui doit recevoir un mail
	 */
	public Mail(String sujet, String contenu, String to, String recipient){
		this.sujet=sujet;
		this.contenu=contenu;
		this.to=to;
		this.recipient=recipient;
	}

	/**
	 * Envoie le mail
	 */
	public void sendMail(){
		if(Play.application().configuration().getString("mail.on").equals("yes")){
			MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
			mail.setSubject(sujet);
			mail.addRecipient(recipient+" <"+to+">");
			mail.addFrom(from);
			mail.sendHtml(contenu);
		}
	}
}
