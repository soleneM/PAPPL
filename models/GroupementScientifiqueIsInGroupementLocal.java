package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
@Entity
public class GroupementScientifiqueIsInGroupementLocal extends Model {
    @Id
    public Integer groupement_scientifique_is_in_groupement_local_id;
    @NotNull
    @ManyToOne
    public GroupementScientifique groupementScientifique;
    @NotNull
    @ManyToOne
    public Groupe groupe;

    public static Model.Finder<Integer,GroupementScientifiqueIsInGroupementLocal> find = new Model.Finder<Integer,GroupementScientifiqueIsInGroupementLocal>(Integer.class, GroupementScientifiqueIsInGroupementLocal.class);

    public GroupementScientifiqueIsInGroupementLocal(GroupementScientifique groupementScientifique, Groupe groupe) {
        this.groupementScientifique=groupementScientifique;
        this.groupe=groupe;
    }

}
