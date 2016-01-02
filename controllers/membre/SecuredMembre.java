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
package controllers.membre;

import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http.Context;

/**
 * Permet d'assurer la sécurité.
 * En cas de non authentification, on renvoit une erreur 401.
 * @author malik
 *
 */
public class SecuredMembre extends Security.Authenticator {
	@Override
    public String getUsername(Context ctx) {
        return ctx.session().get("username");
    }
	
	@Override
    public Result onUnauthorized(Context ctx) {
        return unauthorized("Vous devez être membre et connecté pour pouvoir faire cela !");
    }
}
