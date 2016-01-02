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
package functions.excels;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;


public class ImportExcel extends Excel{
	private Sheet sheet;
	private StringBuilder errorReport;
	private boolean noError = true;
	
	private List<RowCheck> temoignages = new ArrayList<RowCheck>();
	
	/**
	 * Créer une nouvelle instance d'importExcel pour gérer l'import
	 * de témoignages.
	 * @param fis
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public ImportExcel(FileInputStream fis) throws InvalidFormatException, IOException{
		errorReport=new StringBuilder();
		wb = WorkbookFactory.create(fis);
		sheet = wb.getSheetAt(0);
	}
	
	/**
	 * Vérifie que chaque ligne comporte des informations valides
	 */
	public void checkRows(){
		int i = 1;
		Row row;
		while((row=sheet.getRow(i))!=null && row.getCell(0)!=null){
			RowCheck rc = new RowCheck(row,i,errorReport);
			rc.checkRow();
			noError=(noError && rc.noError()) ? true : false;
			temoignages.add(rc);
			i++;
		}
	}
	
	public String getErrorReport(){
		return errorReport.toString();
	}
	public boolean noError(){
		return noError;
	}
	
	/**
	 * Sauvegarde les témoignages dans la base de données.
	 * N'utilisez ceci que quand vous avez déjà lancé checkRows et qu'il
	 * n'y a pas eu d'erreurs !
	 */
	public void saveToDatabase() {
		for(RowCheck temoignage : temoignages){
			temoignage.saveToDatabase();
		}
	}
}
