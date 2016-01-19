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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import functions.UTMtoXY;
import models.UTMS;

public class Carte {
	private BufferedImage carte;

	private static final int[] X_AXIS = { 0, 18, 36, 55, 75, 94, 113, 131, 151, 169, 188, 207, 225, 245, 264, 283, 301,
			320, 339, 340 };
	private static final int[] Y_AXIS = { 0, 13, 32, 51, 70, 89, 108, 127, 146, 164, 183, 202, 221, 240, 259, 278, 297,
			316, 335, 354, 355 };
	private static final int RED = -61681;
	private static final int WHITE = -1;
	private String file_name;

	public Carte() throws IOException {
		carte = ImageIO.read(new File("public/images/carte.png"));
		SimpleDateFormat date_format = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
		file_name = date_format.format(Calendar.getInstance().getTime()) + ".png";
	}

	public Carte(HashMap<UTMS, Integer> observations, int tailleUTM) throws IOException {
		this();
		switch (tailleUTM) {
		case 20: // un nombre a afficher par maille UTM 20x20 km
			for (UTMS utms : observations.keySet()) {
				int nbr = observations.get(utms);
				List<UTMS> maille20toUTM = UTMS.parseMaille(utms.maille20x20);
				if (maille20toUTM != null) {
					// reinitialisation de l'indice i
					// qui servira a compter le nombre d'iterations dans la
					// boucle for suivante
					int i = 1;
					for (UTMS utmsBIS : maille20toUTM) {
						int[] xy = UTMtoXY.convert10x10(utmsBIS.utm);
						if (nbr != 0) {
							allumeRouge(xy);
						}
						if (i == 1) {
							// c'est la premiere fois qu'on tombe sur cette
							// maille UTM 20x20 km
							// on ecrit en blanc
							ecrit(xy, nbr, 1);
						} else {
							// on a deja ecrit la valeur correspondant a cette
							// maille UTM 20x20 km
							// on ecrit en noir (a titre indicatif)
							ecrit(xy, nbr, 0);
						}
						i++;
					}
				}
			}
			break;
		default: // un nombre a afficher par maille UTM 10x10 km
			for (UTMS utms : observations.keySet()) {
				int nbr = observations.get(utms);
				int[] xy = UTMtoXY.convert10x10(utms.utm);
				if (nbr != 0) {
					allumeRouge(xy);
				}
				ecrit(xy, nbr, 1);
			}
			break;
		}
	}

	private void allumeRouge(int[] xy) {
		allumeRouge(xy[0], xy[1]);
	}

	public void allumeRouge(int x, int y) {
		for (int i = X_AXIS[x - 1]; i < X_AXIS[x]; i++) {
			for (int j = Y_AXIS[y + 1]; j < Y_AXIS[y + 2]; j++) {
				if (carte.getRGB(i, j) == WHITE)
					carte.setRGB(i, j, RED);
			}
		}
	}

	private void ecrit(int[] xy, int n, int couleur) {
		ecrit(xy[0], xy[1], Integer.toString(n), couleur);
	}

	public void ecrit(int x, int y, String texte, int couleur) {
		Graphics g = carte.getGraphics();
		if (couleur == 1) {
			g.setColor(Color.WHITE);
		} else {
			g.setColor(Color.BLACK);
		}
		switch (texte.length()) {
		case 1:
			g.setFont(g.getFont().deriveFont(1, 16f));
			g.drawString(texte, X_AXIS[x - 1] + 2, Y_AXIS[y + 1] + 17);
			break;
		case 2:
			g.setFont(g.getFont().deriveFont(1, 12f));
			g.drawString(texte, X_AXIS[x - 1] + 2, Y_AXIS[y + 1] + 17);
			break;
		case 3:
			g.setFont(g.getFont().deriveFont(1, 8f));
			g.drawString(texte, X_AXIS[x - 1] + 2, Y_AXIS[y + 1] + 17);
			break;
		default:
			g.setFont(g.getFont().deriveFont(1, 6f));
			g.drawString(texte, X_AXIS[x - 1] + 2, Y_AXIS[y + 1] + 17);
			break;
		}

		g.dispose();
	}

	public void writeToDisk() throws IOException {
		File outputfile = new File("downloads/" + file_name);
		ImageIO.write(carte, "png", outputfile);
	}

	public BufferedImage getImage() {
		return carte;
	}
}
