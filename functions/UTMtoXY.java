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
package functions;

public class UTMtoXY {

	/**
	 * Convertie une maille utm de format WTxy, XTxy, WSxy, XSxy
	 * en couple de coordonnées entières dont l'origine est dans le coin
	 * au au nord-ouest de la carte, l'axe des x est dirigé vers l'est, l'axe
	 * des y dirigé vers le sud (pour une représentation sur une feuille excel).
	 * @param utm
	 * @return
	 * @throws NumberFormatException
	 */
	public static int[] convert10x10(String utm) throws NumberFormatException{
		int[] xy = new int[2];
		String maille100x100 = utm.substring(0,2);
		if (maille100x100.equals("WS")){
			xy[0]=0;
			xy[1]=0;
		} else if (maille100x100.equals("XS")){
			xy[0]=10;
			xy[1]=0;
		} else if (maille100x100.equals("WT")){
			xy[0]=0;
			xy[1]=10;
		} else if (maille100x100.equals("XT")){
			xy[0]=10;
			xy[1]=10;
		} else {
			throw new NumberFormatException("Format de maille invalide");
		}
		xy[0]+=Integer.parseInt(utm.substring(2,3));
		xy[1]+=Integer.parseInt(utm.substring(3,4));
		xy[1]=19-xy[1];
		return xy;
	}
}
