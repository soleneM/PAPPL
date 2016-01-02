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
package functions.credentials;

import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import models.Membre;

public class Credentials {
	private String email;
	private Membre membre = null;

	public Credentials(String email){
		this.email=email.toLowerCase();
	}

	/**
	 * Vérifie que le membre en question existe et que son mot de passe est correct.
	 * @param password
	 * @return VRAI ou FAUX
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public boolean connect(String password) throws NoSuchAlgorithmException, InvalidKeySpecException{
		membre = Membre.find.where().eq("membre_email", email).findUnique();
		if(membre==null)
			return false;
		else if (membre.membre_mdp_hash == null)
			return false;
		else {
			return PasswordHash.validatePassword(password, membre.membre_mdp_hash);
		}
	}

	/**
	 * Change le mot de passe du membre. Attention, pour ce faire, il doit être connecté.
	 * @param newMotDePasse
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public boolean changeMotDePasse(String newMotDePasse) throws NoSuchAlgorithmException, InvalidKeySpecException{
		if(membre!=null){
			membre.membre_mdp_hash=PasswordHash.createHash(newMotDePasse);
			membre.save();
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Crée un mot de passe pour tous les utilisateurs ayant une adresse e-mail.
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 */
	public static void creeHashEtMotDePassePourToutLeMonde() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException{
		List<Membre> membres = Membre.find.all();
		FileWriter fw = new FileWriter("motsDePasse.txt");
		for(Membre membre : membres)
			if(!membre.membre_email.isEmpty()){
				String mdp = genereMotDePasse();
				membre.membre_mdp_hash=PasswordHash.createHash(mdp);
				membre.save();
				fw.append(membre.membre_nom+" :	"+mdp);
			}
		fw.close();
	}
	
	//Variables pour générer les mots de passe aléatoirement
	private static final String elements = "23456789azertyuiopqsdfghjkmwxcvbn";
	private static final SecureRandom rnd = new SecureRandom();
	private static final int password_size = 8;
	
	/**
	 * Génère un mot de passe de manière aléatoire sécurisée.
	 * @return
	 */
	private static String genereMotDePasse(){
		StringBuilder sb = new StringBuilder(password_size);
		for( int i = 0; i < password_size; i++ ) 
		      sb.append(elements.charAt(rnd.nextInt(elements.length())));
		return sb.toString();
	}
	/**
	 * Génère un mot de passe de manière aléatoire sécurisée.
	 * @return
	 */
	public static String genereLienAleatoire(int size){
		StringBuilder sb = new StringBuilder(size);
		for( int i = 0; i < size; i++ ) 
		      sb.append(elements.charAt(rnd.nextInt(elements.length())));
		return sb.toString();
	}
}
