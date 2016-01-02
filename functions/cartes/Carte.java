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
package functions.cartes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import functions.UTMtoXY;
import models.UTMS;

public class Carte {
	private BufferedImage carte;

	private static final int[] X_AXIS = {0,18,36,55,75,94,113,131,151,169,188,207,225,245,264,283,301,320,339,340};
	private static final int[] Y_AXIS = {0,13,32,51,70,89,108,127,146,164,183,202,221,240,259,278,297,316,335,354,355};
	private static final int RED = -61681;
	private static final int WHITE = -1;

	public Carte() throws IOException{
		carte = ImageIO.read(new File("public/images/carte.png"));
	}
	public Carte(HashMap<UTMS,Integer> observations) throws IOException{
		this();
		for(UTMS utm : observations.keySet()){
			int nbr = observations.get(utm);
			int[] xy = UTMtoXY.convert10x10(utm.utm);
			if(nbr!=0){
				allumeRouge(xy);
			}
			ecrit(xy,nbr);
		}
	}


	private void allumeRouge(int[] xy){
		allumeRouge(xy[0],xy[1]);
	}
	public void allumeRouge(int x, int y){
		for(int i = X_AXIS[x-1] ; i<X_AXIS[x] ; i++){
			for(int j = Y_AXIS[y+1] ; j<Y_AXIS[y+2] ; j++){
				if(carte.getRGB(i, j)==WHITE)
					carte.setRGB(i, j, RED);
			}
		}
	}
	private void ecrit(int[] xy,int n){
		ecrit(xy[0],xy[1],Integer.toString(n));
	}
	public void ecrit(int x, int y, String texte){
		Graphics g = carte.getGraphics();
		g.setColor(Color.BLACK);
		g.setFont(g.getFont().deriveFont(12f));
		g.drawString(texte, X_AXIS[x-1]+4, Y_AXIS[y+1]+15);
		g.dispose();
	}

	public void writeToDisk() throws IOException{
		File outputfile = new File("/tmp/saved.png");
		ImageIO.write(carte, "png", outputfile);
	}
	public BufferedImage getImage() {
		return carte;
	}
}
