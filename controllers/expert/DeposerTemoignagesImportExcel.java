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
package controllers.expert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import models.Groupe;
import models.Membre;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import controllers.admin.Admin;
import functions.excels.ImportExcel;
import functions.excels.ImportExcelEdit;
import functions.excels.exports.ExportExcelEdit;
import play.Play;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import views.html.expert.deposerTemoignagesImportExcel;

public class DeposerTemoignagesImportExcel extends Controller {
	public static Result main(){
		if(MenuExpert.isExpertConnected()){
			Membre expert = Membre.find.where().eq("membre_email", session("username")).findUnique();
			return ok(deposerTemoignagesImportExcel.render("",expert));
		}else
			return Admin.nonAutorise();
	}

	public static Result post() throws IOException{
		Membre expert = Membre.find.where().eq("membre_email", session("username")).findUnique();
		if(MenuExpert.isExpertConnected()){
			MultipartFormData body = request().body().asMultipartFormData();
			FilePart fp = body.getFile("xls");
			try {
				if(fp!=null){
					FileInputStream fis = new FileInputStream(fp.getFile());
					ImportExcel ie = new ImportExcel(fis);
					ie.checkRows();
					if(ie.noError()){
						ie.saveToDatabase();
						return ok(deposerTemoignagesImportExcel.render("L'import s'est déroulé avec succès.",expert));
					}else{
						return ok(deposerTemoignagesImportExcel.render(ie.getErrorReport(),expert));
					}
				}else
					return badRequest(deposerTemoignagesImportExcel.render("Le fichier que vous avez envoyé n'est pas valide.",expert));
			} catch (InvalidFormatException e) {
				return badRequest(deposerTemoignagesImportExcel.render("Le fichier que vous avez envoyé n'est pas un fichier Excel .xls conforme.",expert));
			}
		}else
			return Admin.nonAutorise();
	}
	
	public static Result telechargerMajDeMasse(Integer groupe_id) throws IOException{
		if(MenuExpert.isExpertConnected()){
			DynamicForm df = DynamicForm.form().bindFromRequest();
			int contenu = 0;
			String stadesexe = df.get("stadesexe");
			String commune = df.get("commune");
			if(stadesexe!=null)
				contenu++;
			if(commune!=null)
				contenu+=2;
			Groupe groupe = Groupe.find.byId(groupe_id);
			ExportExcelEdit eee = new ExportExcelEdit(groupe,contenu);
			eee.writeToDisk();
			FileInputStream fis = new FileInputStream(new File(Play.application().configuration().getString("xls_generes.path")+eee.getFileName()));
			response().setHeader("Content-Disposition", "attachment; filename="+eee.getFileName());
			return ok(fis);
		}else
			return Admin.nonAutorise();
	}
	
	/**
	 * Permet un import de masse avec écrasement des données précédentes.
	 * @return
	 * @throws IOException
	 */
	public static Result televerserMajDeMasse() throws IOException{
		Membre expert = Membre.find.where().eq("membre_email", session("username")).findUnique();
		if(MenuExpert.isExpertConnected()){
			MultipartFormData body = request().body().asMultipartFormData();
			FilePart fp = body.getFile("xls");
			try {
				if(fp!=null){
					FileInputStream fis = new FileInputStream(fp.getFile());
					ImportExcelEdit ie = new ImportExcelEdit(fis);
					ie.checkRows();
					if(ie.noError()){
						ie.saveToDatabase();
						return ok(deposerTemoignagesImportExcel.render("L'import s'est déroulé avec succès.",expert));
					}else{
						return ok(deposerTemoignagesImportExcel.render(ie.getErrorReport(),expert));
					}
				}else
					return badRequest(deposerTemoignagesImportExcel.render("Le fichier que vous avez envoyé n'est pas valide.",expert));
			} catch (InvalidFormatException e) {
				return badRequest(deposerTemoignagesImportExcel.render("Le fichier que vous avez envoyé n'est pas un fichier Excel .xls conforme.",expert));
			}
		}else
			return Admin.nonAutorise();
	}
}
