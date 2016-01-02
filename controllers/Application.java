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

package controllers;


import java.io.File;

import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

    public static Result index() {
        return redirect("/identification");
    }
    
    /**
	 * Pour accéder aux images uploadées
	 * @param filename
	 * @return l'image
	 */
	public static Result view(String filename) {
	    File file  = new File(play.Play.application().configuration().getString("image.path") + filename);
	    if(file.canRead())
	    	return ok(file);
	    else
	    	return notFound("404: Image not found");
	}    
}
