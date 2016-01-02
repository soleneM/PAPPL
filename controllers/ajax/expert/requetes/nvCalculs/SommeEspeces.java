package controllers.ajax.expert.requetes.nvCalculs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import javax.sql.DataSource;

import controllers.ajax.expert.requetes.Calculs;
import play.db.DB;

public class SommeEspeces {
	
	/**
	 * Calcule la somme des espèces témoignées par maille sur une période pour un groupe ou sous-groupe défini
	 * @param info
	 * @param tailleUTM
	 * @return
	 * @throws ParseException
	 * @throws SQLException
	 */
	public static ResultSet calculeSommeEspeces(Map<String,String> info, int tailleUTM) throws ParseException, SQLException {
		DataSource ds = DB.getDataSource();
		Connection connection = ds.getConnection();
		PreparedStatement sommeEspeces;

		ArrayList<Object> listeParams = new ArrayList<Object>();
		String statement = "";
		
		statement += "SELECT groupe.groupe_id,";
		switch (tailleUTM) {
			case 20:
				statement += " utms.maille20x20";
			break;
			default:
				statement += " utms.utm";
			break;
		}
		statement += ", COUNT(espece.espece_id) as nbespeces"
				+ " FROM groupe " 
				+ " INNER JOIN espece_is_in_groupement_local ON (groupe.groupe_id = espece_is_in_groupement_local.groupe_groupe_id)"
				+ " INNER JOIN espece ON (espece_is_in_groupement_local.espece_espece_id = espece.espece_id)"
				+ " INNER JOIN observation ON (espece.espece_id = observation.observation_espece_espece_id)"
				+ " INNER JOIN fiche ON (observation.observation_fiche_fiche_id = fiche.fiche_id)"
				+ " INNER JOIN utms ON (fiche.fiche_utm_utm = utms.utm)"
				+ " WHERE observation.observation_validee == 1";
		
		if ((info.get("sous_groupe") != null) && (! info.get("sous_groupe").equals(""))) {
			statement += " AND groupe.groupe_id == ?";
			listeParams.add(info.get("sous_groupe"));
		} else if ((info.get("groupe") != null) && (! info.get("groupe").equals(""))) {
			statement += " AND groupe.groupe_id == ?";
			listeParams.add(info.get("groupe"));
		}
		
		if (! info.get("periode").equals("all")) {
			statement += " AND f.fiche_date BETWEEN ? AND ?";

			Calendar date1 = Calculs.getDataDate1(info);
			Calendar date2 = Calculs.getDataDate2(info);
			listeParams.add(new java.sql.Date(date1.getTimeInMillis()));
			listeParams.add(new java.sql.Date(date2.getTimeInMillis()));
		}
		
		String ordreETgroupement = null;
		switch (tailleUTM) {
			case 20:
				ordreETgroupement = "groupe.groupe_id, utms.maille20x20";
				break;
			default:
				ordreETgroupement = "groupe.groupe_id, utms.utm";
				break;	
		}
		statement += " ORDER BY "+ordreETgroupement+" GROUP BY "+ordreETgroupement;
		
		sommeEspeces = connection.prepareStatement(statement); 
		setParams(sommeEspeces, listeParams);
		ResultSet rs = sommeEspeces.executeQuery();
		
		return rs;
	}
	
	
	private static void setParams(PreparedStatement aStatement, ArrayList<Object> listeParams) throws SQLException {
		int index = 0;
		for (Object o : listeParams) {
			index++;
			if (o instanceof String) {
				aStatement.setString(index, (String)o);
			} else if (o instanceof java.sql.Date) {
				aStatement.setDate(index, (java.sql.Date)o);
			}
		}
	}

}
