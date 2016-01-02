package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
@Entity
public class EspeceIsInGroupementLocal extends Model{
    @Id
    public Integer espece_is_in_groupement_local_id;
    @NotNull
    @ManyToOne
    public Espece espece;
    @NotNull
    @ManyToOne
    public Groupe groupe;

    public static Model.Finder<Integer,EspeceIsInGroupementLocal> find = new Model.Finder<Integer,EspeceIsInGroupementLocal>(Integer.class, EspeceIsInGroupementLocal.class);

    public EspeceIsInGroupementLocal(Espece espece, Groupe groupe) {
        this.espece=espece;
        this.groupe=groupe;
    }
    
    public static Groupe especeIsInThisGroupeLocal(Integer espece_id){
    	return find.where().eq("espece", espece_id).findUnique().groupe;
    }

}
