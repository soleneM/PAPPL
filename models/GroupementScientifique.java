package models;

import java.util.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class GroupementScientifique extends Model{
    @Id
    public Integer groupement_scientifique_id;
    public String groupement_scientifique_nom;
    @NotNull
    @ManyToOne
    public TypeGroupementScientifique groupement_scientifique_type;
    @ManyToOne
    public GroupementScientifique groupement_scientifique_pere;

    public static Model.Finder<Integer,GroupementScientifique> find = new Finder<Integer,GroupementScientifique>(Integer.class, GroupementScientifique.class);


    public GroupementScientifique (String nom, String type){
        groupement_scientifique_nom=nom;
        groupement_scientifique_type=TypeGroupementScientifique.find.byId(type);
    }

    public GroupementScientifique (String nom, String type, GroupementScientifique pere){
        groupement_scientifique_nom=nom;
        groupement_scientifique_type=TypeGroupementScientifique.find.byId(type);
    }

    public static List<GroupementScientifique> findAllOfType(String type){
        TypeGroupementScientifique t = TypeGroupementScientifique.find.byId(type);
        return find.where().eq("groupement_scientifique_type", t).orderBy("groupement_scientifique_nom").findList();
    }

    @Override
    public String toString(){
        return groupement_scientifique_nom;
    }

    /**
     * permet de récupérer une liste de toutes les groupements scientifiques dans l'ordre hiérarchique.
     * C'est à dire que après un ordre, il y a la première super-famille, puis les familles dans cette première super-famille etc...
     * Cela permet d'afficher la classification dans un menu déroulant
     * @return
     */
	public static List<GroupementScientifique> findAllByHierarchie(){
		List<GroupementScientifique> liste = new LinkedList<GroupementScientifique>();
		for (GroupementScientifique gs : findAllOfType("ordre")){
			liste.add(gs);
            recFils(gs,liste);
		}
		for (GroupementScientifique gsc : GroupementScientifique.findGroupementSansPere()){
			liste.add(gsc);
		}
		return liste;
	}
    private static List<GroupementScientifique> recFils (GroupementScientifique gs, List<GroupementScientifique> liste){
        for (GroupementScientifique sg : gs.getFils()){
            liste.add(sg);
            recFils(sg,liste);
        }
        return liste;
    }

    /****************** getters des espèces contenus ****************/

    /**
     * Renvoie la liste des espèces directement dans ce groupement
     * @return
     */
    public List<Espece> getEspecesInThis(){
        return Espece.find.where()
                .eq("espece_groupement_scientifique_pere", this)
                .orderBy("espece_systematique").findList();
    }

    /**
     * Renvoie la liste de toutes les espèces dans la hiérarchie fille de ce groupement
     * @return
     */
    public List<Espece> getAllEspecesInThis(){
        List<Espece> esp = getEspecesInThis();
        if (!getFils().isEmpty()) {
            for (GroupementScientifique fils : this.getFils()) {
                esp.addAll(fils.getAllEspecesInThis());
            }
        }
        return esp;
    }

    /****************** hiérarchie scientifique  ****************/

    /**
     * retourne la liste des groupement scientifiques directement fils
     * @return
     */
    public List<GroupementScientifique> getFils(){
        return find.where().eq("groupement_scientifique_pere", this).findList();
    }


    /**
     * retourne la liste de tous les groupes parents (du plus proche au plus éloigné)
     * @return
     */
    public List<GroupementScientifique> getHierarchieScientifique(){
        List<GroupementScientifique> h = new ArrayList<GroupementScientifique>();
        if (groupement_scientifique_pere!=null) {
            h.add(groupement_scientifique_pere);
            if (groupement_scientifique_pere.groupement_scientifique_pere != null) {
                groupement_scientifique_pere.groupement_scientifique_pere.getHierarchieScientifique();
            }
        }
        return h;
    }

    public static List<GroupementScientifique> findGroupementSansPere(){
        List<GroupementScientifique> listeGroupements = find.where().eq("groupement_scientifique_pere", null).findList();

        // on supprime de la liste les ordres
        for (ListIterator<GroupementScientifique> it = listeGroupements.listIterator(); it.hasNext();) {
            GroupementScientifique groupement = it.next();
            if (groupement.groupement_scientifique_type.equals(TypeGroupementScientifique.find.byId("ordre"))){
                it.remove();
            }
        }
        return listeGroupements;
    }

    /****************** hiérarchie locale  ****************/



    // supprimer

    /**
     * on
     * @param groupementId
     * @return true si la suppression a eu lieu et false s'il y a eu une erreur dans la suppression des groupements scientifiques fils
     */
    public static Boolean supprimer(Integer groupementId){
        GroupementScientifique g = find.byId(groupementId);
        Boolean status = true;

        //on supprime uniqueement si aucune espèce appartient au groupement scientifique
        if (g.getAllEspecesInThis().isEmpty()){
            for (GroupementScientifique gFils : g.getFils()){
                if ( status && !GroupementScientifique.supprimer(gFils.groupement_scientifique_id) ){
                    status = false;
                }
            }
            if (status) {
                g.delete();
            }
        }
        return status;
    }

}
