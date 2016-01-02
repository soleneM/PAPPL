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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import play.mvc.Controller;
import models.Observation;
import models.InformationsComplementaires;
import models.Fiche;
import models.UTMS;
import models.FicheHasMembre;
import models.Membre;

import play.db.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CarnetDeChasse extends Controller {
	
	
	public static ResultSet calculeCarnetDeChasse(Map<String,String> info) throws SQLException {
	
		  
		DataSource ds = DB.getDataSource();

		Connection connection = ds.getConnection();
		String statement = ""
				+ "SELECT f.fiche_utm_utm, obs.observation_id, e.espece_nom, i.informations_complementaires_nombre_de_specimens, s.stade_sexe_intitule "
				+ "FROM Observation obs "
				+ "INNER JOIN Fiche f ON obs.observation_fiche_fiche_id = f.fiche_id "
				+ "INNER JOIN Fiche_Has_Membre fhm ON fhm.fiche_fiche_id = f.fiche_id "
				+ "INNER JOIN Membre m ON fhm.membre_membre_id = m.membre_id "
				+ "INNER JOIN Espece e ON obs.observation_espece_espece_id = e.espece_id "
				+ "INNER JOIN Informations_Complementaires i ON obs.observation_id = i.informations_complementaires_observation_observation_id "
				+ "INNER JOIN Stade_sexe s ON i.informations_complementaires_stade_sexe_stade_sexe_id = s.stade_sexe_id "
				+ "WHERE  m.membre_email = ? "
				+ "GROUP BY f.fiche_utm_utm, obs.observation_id ";
		PreparedStatement carnetDeChasse = connection.prepareStatement(statement); 
		carnetDeChasse.setString(1,session("username"));
				
		ResultSet rs = carnetDeChasse.executeQuery();
		
		return rs;
			
		}
	
	

}