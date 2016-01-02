package models;

import javax.naming.NamingException;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceException;
import javax.validation.constraints.NotNull;

import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class EspeceSynonyme extends Model  {
	@Id
	public Integer synonyme_id;
	@NotNull
	public String synonyme_nom;
	@NotNull
	public boolean synonyme_origineAER;
	@NotNull
	@ManyToOne
	public Espece synonyme_espece;
	
	public static Finder<Integer,EspeceSynonyme> find = new Finder<Integer,EspeceSynonyme>(Integer.class, EspeceSynonyme.class);
	
	/**Crée un synonyme pour une espèce
	* @param nom
	 * @param origineAER
	 * @param espece_id
	 * @throws PersistenceException
	 * @throws NamingException
	 */
	
	public EspeceSynonyme(String nom, boolean origineAER, Integer espece_id) throws NamingException, PersistenceException {
		synonyme_nom=nom;
		synonyme_origineAER=origineAER;
		synonyme_espece=Espece.find.byId(espece_id);
		if(synonyme_espece==null){
			throw new NamingException("L'espèce "+espece_id+" n'existe pas !");
		}
	}
	
	/**Ajoute un synonyme à la base de données
	* @param nom
	 * @param origineAER
	 * @param espece_id
	 * @throws PersistenceException
	 * @throws NamingException
	 */
	 public static void ajouterSynonyme(String nomSyn, boolean origineAER, Integer espece_id) throws NamingException, PersistenceException {
		EspeceSynonyme synonyme = new EspeceSynonyme(nomSyn, origineAER, espece_id);
		Espece esp = Espece.find.byId(espece_id);
		if(esp==null){
			throw new NamingException("L'espèce "+espece_id+" n'existe pas !");
		} else {
		synonyme.save();
		}
	}
	
	/** Supprime un synonyme
	*/
	public static void supprimerSynonyme(Integer synonyme_id){
		EspeceSynonyme synonyme = EspeceSynonyme.find.byId(synonyme_id);
		synonyme.delete();
	}
	
	@Override
	public String toString(){
		return synonyme_espece+" alias "+synonyme_nom;
	}
}
