package models;


import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Guillaume on 26/02/15.
 */

@SuppressWarnings("serial")
@Entity
public class TypeGroupementScientifique extends Model{
    @Id
    public String intitule;

    public static Model.Finder<String,TypeGroupementScientifique> find = new Model.Finder<String,TypeGroupementScientifique>(String.class, TypeGroupementScientifique.class);

    public TypeGroupementScientifique(String intitule){
        this.intitule = intitule;
    }

    @Override
    public String toString() {
        return intitule;
    }

}