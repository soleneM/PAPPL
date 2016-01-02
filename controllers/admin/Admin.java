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
package controllers.admin;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.admin.menuAdmin;

/**
 * Fonctions générales de gestions des admins
 * @author malik
 *
 */
public class Admin extends Controller{
	/**
	 * Si c'est un admin qui est connecté, renvoie true, sinon, renvoie false.
	 * @return
	 */
	public static boolean isAdminConnected(){
		return session("admin")!=null;
	}

	/**
	 * Affiche la page d'erreur 401
	 * @return
	 */
	public static Result nonAutorise() {
		return unauthorized("You do not have the right to access this page.");
	}

	/**
	 * Affiche la page de menu admin.
	 * @return
	 */
    public static Result menuAdmin() {
    	if(isAdminConnected())
    		return ok(menuAdmin.render());
    	else
    		return nonAutorise();
    }
}
