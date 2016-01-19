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

public class HistogrammeExportExcel extends Excel {

	private HistogrammeExportExcel() {
		super();
	}

	/**
	 * Creation du fichier Excel pour la fonction Historique
	 * 
	 * @param histogrammeHistorique
	 *            les donnee l'histogramme
	 * @return un objet HistogrammeExportExcel representant un fichier Excel
	 * @throws IOException
	 * @throws SQLException
	 */
	public static HistogrammeExportExcel historique(Map<String, Integer> histogrammeHistorique)
			throws IOException, SQLException {
		HistogrammeExportExcel theFile = new HistogrammeExportExcel();

		Sheet sheet = theFile.wb.createSheet("Historique");
		String titre = "Historique des temoignages par periode de 20 ans";

		sheet.createRow(0).createCell(0).setCellValue(titre);
		sheet.addMergedRegion(new CellRangeAddress(0, // first row (0-based)
				0, // last row (0-based)
				0, // first column (0-based)
				12 // last column (0-based)
		));
		Row row = sheet.createRow(1);
		row.createCell(0).setCellValue("Periode");
		row.createCell(1).setCellValue("Nbr. tem.");
		int i = 0;
		// liste qui contiendra les periodes dans l'ordre chronologique
		ArrayList<String> periodeAsc = new ArrayList<>();
		for (String periode : histogrammeHistorique.keySet()) {
			periodeAsc.add(periode);
		}
		Collections.sort(periodeAsc);
		// remplissage du fichier Excel
		for (String periode : periodeAsc) {
			row = sheet.createRow(i + 2);
			row.createCell(0);
			row.createCell(1);
			sheet.getRow(i + 2).getCell(0).setCellValue(periode);
			sheet.getRow(i + 2).getCell(1).setCellValue(histogrammeHistorique.get(periode));
			i++;
		}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);

		return (theFile);
	}

	/**
	 * Creation du fichier Excel pour la fonction Phenologie
	 * 
	 * @param histogrammeHistorique
	 *            les donnee l'histogramme
	 * @return un objet HistogrammeExportExcel representant un fichier Excel
	 * @throws IOException
	 * @throws SQLException
	 */
	public static HistogrammeExportExcel phenologie(Map<String, String> info,
			Map<String, Integer> histogrammePhenologie) throws IOException, SQLException {
		HistogrammeExportExcel theFile = new HistogrammeExportExcel();

		Sheet sheet = theFile.wb.createSheet("Phenologie");
		String titre = "Historique par decade du nombre de temoignages";
		// ajout de la periode dans le titre
		if (!info.get("periode").equals("all")) {
			String date1 = info.get("jour1") + "/" + info.get("mois1") + "/" + info.get("annee1");
			String date2 = info.get("jour2") + "/" + info.get("mois2") + "/" + info.get("annee2");
			titre += " du " + date1 + " au " + date2;
		}
		// ajout du groupe/sous-groupe/espece dans le titre
		if (!info.get("espece").equals("")) {
			titre += " pour l'espece " + info.get("espece");
		} else if (!info.get("sous_groupe").equals("")) {
			titre += " pour le sous-groupe " + info.get("sous_groupe");
		} else if (!info.get("groupe").equals("")) {
			titre += " pour le groupe " + info.get("groupe");
		}

		sheet.createRow(0).createCell(0).setCellValue(titre);
		sheet.addMergedRegion(new CellRangeAddress(0, // first row (0-based)
				0, // last row (0-based)
				0, // first column (0-based)
				12 // last column (0-based)
		));
		Row row = sheet.createRow(1);
		row.createCell(0).setCellValue("Periode");
		row.createCell(1).setCellValue("Nbr. tem.");
		int i = 0;
		ArrayList<String> periodeAsc = new ArrayList<>();
		for (String periode : histogrammePhenologie.keySet()) {
			periodeAsc.add(periode);
		}
		Collections.sort(periodeAsc);
		for (String periode : periodeAsc) {
			row = sheet.createRow(i + 2);
			row.createCell(0);
			row.createCell(1);
			sheet.getRow(i + 2).getCell(0).setCellValue(periode);
			sheet.getRow(i + 2).getCell(1).setCellValue(histogrammePhenologie.get(periode));
			i++;
		}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);

		return (theFile);
	}

}
