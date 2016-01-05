package functions.excels;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import models.UTMS;

public class HistogrammeExportExcel extends Excel{
	
	private HistogrammeExportExcel() {
		super();
	}
	
	public static HistogrammeExportExcel historique(Map<String,Integer> histogrammeHistorique) throws IOException, SQLException{
		HistogrammeExportExcel theFile = new HistogrammeExportExcel();
		
		Sheet sheet = theFile.wb.createSheet("Historique");
		String titre = "Historique des témoignages par période de 20 ans";

		sheet.createRow(0).createCell(0).setCellValue(titre);
		sheet.addMergedRegion(new CellRangeAddress(
	            0, //first row (0-based)
	            0, //last row  (0-based)
	            0, //first column (0-based)
	            12  //last column  (0-based)
	    ));
		Row row = sheet.createRow(1);
		row.createCell(0).setCellValue("Période");
		row.createCell(1).setCellValue("Nbr. tem.");
		int i = 0;
		ArrayList<String> periodeAsc = new ArrayList<>();
		for(String periode : histogrammeHistorique.keySet()){
			periodeAsc.add(periode);
		}
		Collections.sort(periodeAsc);
		for (String periode : periodeAsc) {	
			row = sheet.createRow(i+2);
			row.createCell(0);
			row.createCell(1);
			sheet.getRow(i+2).getCell(0).setCellValue(periode);
			sheet.getRow(i+2).getCell(1).setCellValue(histogrammeHistorique.get(periode));
			i++;
		}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		
		return(theFile);
	}
}