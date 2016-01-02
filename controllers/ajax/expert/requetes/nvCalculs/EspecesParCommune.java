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

import java.util.Comparator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import controllers.ajax.expert.requetes.Calculs;
import models.Espece;
import models.Groupe;
import models.InformationsComplementaires;
import models.Observation;
import models.StadeSexe;
import models.UTMS;

import play.db.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EspecesParCommune {

	
	public static ResultSet calculeEspecesParCommune(Map<String,String> info) throws ParseException, SQLException {
		
		Calendar date1 = Calculs.getDataDate1(info);
		Calendar date2 = Calculs.getDataDate2(info);
		  
		DataSource ds = DB.getDataSource();

		Connection connection = ds.getConnection();
		String statement = ""
				+ "SELECT c.ville_nom , e.espece_nom, count(e.espece_nom) FROM Observation obs "
				+ "INNER JOIN Fiche f ON obs.observation_fiche_fiche_id = f.fiche_id "
				+ "INNER JOIN Espece e ON obs.observation_espece_espece_id = e.espece_id "
				+ "INNER JOIN Commune c ON f.fiche_commune_ville_id = c.ville_id "			
				+ "WHERE obs.observation_validee = 1 and f.fiche_date BETWEEN ? AND ? "
				+ "GROUP BY c.ville_nom, e.espece_nom ";
		PreparedStatement especesParCommune = connection.prepareStatement(statement); 
		especesParCommune.setDate(1,new java.sql.Date(date1.getTimeInMillis()));
		especesParCommune.setDate(2,new java.sql.Date(date2.getTimeInMillis()));	
				
		ResultSet rs = especesParCommune.executeQuery();
		
		return rs;

		
	}

}
