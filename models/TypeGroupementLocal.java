package models;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import play.db.ebean.Model;

/**
 * Created by Guillaume on 26/02/15.
 */

@SuppressWarnings("serial")
@Entity
public class TypeGroupementLocal extends Model{
    @Id
    public String type_groupement_local_intitule;

    public static Model.Finder<String,TypeGroupementLocal> find = new Model.Finder<String,TypeGroupementLocal>(String.class, TypeGroupementLocal.class);

    public TypeGroupementLocal (String intitule){
        type_groupement_local_intitule = intitule;
    }

    public static TypeGroupementLocal getTypeGroupe(){
        return find.where().eq("type_groupement_local_intitule", "groupe").findUnique();
    }
    public static TypeGroupementLocal getTypeSousGroupe(){
        return find.where().eq("type_groupement_local_intitule", "sous-groupe").findUnique();
    }

    @Override
    public String toString() {
        return type_groupement_local_intitule;
    }
}