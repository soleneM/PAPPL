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
package controllers.ajax.expert.requetes.nvCalculs;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;

import play.db.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.avaje.ebean.Expr;

import controllers.ajax.expert.requetes.Calculs;
import models.Espece;
import models.FicheHasMembre;
import models.Groupe;
import models.InformationsComplementaires;
import models.Membre;
import models.Observation;
import models.StadeSexe;
import models.UTMS;

public class ListeDesTemoins {

	public static ResultSet calculeListeDesTemoins(Map<String,String> info) throws ParseException, SQLException {
		
		Calendar date1 = Calculs.getDataDate1(info);
		Calendar date2 = Calculs.getDataDate2(info);
		  
		DataSource ds = DB.getDataSource();

		Connection connection = ds.getConnection();
		String statement = ""
				+ "SELECT m.membre_nom, count(obs.observation_id) FROM Observation obs "
				+ "INNER JOIN Fiche f ON obs.observation_fiche_fiche_id = f.fiche_id "
				+ "INNER JOIN Fiche_Has_Membre fhm ON fhm.fiche_fiche_id = f.fiche_id "
				+ "INNER JOIN Membre m ON fhm.membre_membre_id = m.membre_id "
				+ "WHERE obs.observation_validee = 1 and f.fiche_date BETWEEN ? AND ? "
				+ "GROUP BY m.membre_nom ";
		PreparedStatement listeDesTemoins = connection.prepareStatement(statement); 
		listeDesTemoins.setDate(1,new java.sql.Date(date1.getTimeInMillis()));
		listeDesTemoins.setDate(2,new java.sql.Date(date2.getTimeInMillis()));	
				
		ResultSet rs = listeDesTemoins.executeQuery();

		
		return rs;
	}


}
