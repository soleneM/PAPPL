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

package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.sql.DataSource;

import play.db.DB;
import play.db.ebean.Model;

@SuppressWarnings("serial")
@Entity
public class UTMS extends Model {
	@Id
	@Column(columnDefinition="VARCHAR(4)")
	public String utm;
	
	@Column(columnDefinition="VARCHAR(4)")
	public String maille20x20;
	
	@Column(columnDefinition="VARCHAR(4)")
	public String maille50x50;
	
	@Column(columnDefinition="VARCHAR(4)")
	public String maille100x100;
	
	public static Finder<String,UTMS> find = new Finder<String,UTMS>(String.class, UTMS.class);

	public static List<UTMS> findAll(){
		return find.orderBy("utm").findList();
	}
	
	@Override
	public String toString(){
		return utm;
	}

	/**
	 * Convertie une string en liste de mailles UTM.
	 * Si la string en argument est vide, renvoit toutes les mailles.
	 * Si la string est une maille, renvoit la liste des mailles utms 10x10 dans
	 * cette maille.
	 * Si la string ne correspond à rien, renvoie null.
	 * @param maille
	 * @return
	 * @throws SQLException 
	 */
	public static List<UTMS> parseMaille(String maille) {
		DataSource ds = DB.getDataSource();
		Connection connection;
		try {
			connection = ds.getConnection();
			String statement = "";
			
			if(maille.equals("")) {
				statement += "SELECT utm, maille20x20, maille50x50, maille100x100 FROM utms;";
			}
			else {
				statement += "SELECT utm, maille20x20, maille50x50, maille100x100 FROM utms"
						+ " WHERE utms.maille20x20 = ?"
						+ " OR utms.maille50x50 = ?"
						+ " OR utms.maille100x100 = ?;";
			}
			PreparedStatement listeUTM = connection.prepareStatement(statement);
			listeUTM.setString(1,maille);
			listeUTM.setString(2,maille);
			listeUTM.setString(3,maille);

			ResultSet rs = listeUTM.executeQuery();
			
			List<UTMS> utms = new ArrayList<UTMS>();
			
			while (rs.next()){
				UTMS maillesUTM = new UTMS();
				maillesUTM.utm = rs.getString("utm");
				maillesUTM.maille20x20 = rs.getString("maille20x20");
				maillesUTM.maille50x50 = rs.getString("maille50x50");
				maillesUTM.maille100x100 = rs.getString("maille100x100");
				utms.add(maillesUTM);
			}
			
			listeUTM.close();
			connection.close();
			return(utms);
			
		} catch (SQLException e) {
			return(null);
		}
	}
}
