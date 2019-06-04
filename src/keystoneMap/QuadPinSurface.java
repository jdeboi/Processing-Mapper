
package keystoneMap;

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.media.jai.PerspectiveTransform;
import javax.media.jai.WarpPerspective;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class QuadPinSurface extends CornerPinSurface {
	
	public QuadPinSurface(PApplet parent, int w, int h, int res) {
		super(parent, w, h, res);
		type = "QUAD";
	}
	
	/**
	 * Returns true if the mouse is over this surface, false otherwise.
	 */
	public boolean isMouseOver() {
		if (isPointInTriangle(parent.mouseX - x, parent.mouseY - y, mesh[TL],
				mesh[TR], mesh[BL])
				|| isPointInTriangle(parent.mouseX - x, parent.mouseY - y,
						mesh[BL], mesh[TR], mesh[BR]))
			return true;
		return false;
	}
	
	public void calculateMesh() {

		// The float constructor is deprecated, so casting everything to double
		PerspectiveTransform transform = PerspectiveTransform.getQuadToQuad(0,
				0, w, 0, w, h, 0,
				h, // source to
				mesh[TL].x, mesh[TL].y, mesh[TR].x, mesh[TR].y, mesh[BR].x,
				mesh[BR].y, mesh[BL].x, mesh[BL].y); // dest

		warpPerspective = new WarpPerspective(transform);

		float xStep = (float) w / (res - 1);
		float yStep = (float) h / (res - 1);

		for (int i = 0; i < mesh.length; i++) {

			if (TL == i || BR == i || TR == i || BL == i)
				continue;

			float x = i % res;
			float y = i / res;

			x *= xStep;
			y *= yStep;

			Point2D point = warpPerspective.mapDestPoint(new Point((int) x,
					(int) y));
			mesh[i].x = (float) point.getX();
			mesh[i].y = (float) point.getY();
		}
	}

	
	
	
	public void render(PGraphics g, PImage texture, int tX, int tY, int tW,
			int tH) {
		g.pushMatrix();
		g.translate(x, y);
		g.noStroke();
		g.fill(255);
		g.beginShape(PApplet.QUADS);
		g.texture(texture);
		float u, v = 0;
		for (int x = 0; x < res - 1; x++) {
			for (int y = 0; y < res - 1; y++) {
				MeshPoint mp;
				mp = mesh[(x) + (y) * res];
				u = PApplet.map(mp.u, 0, w, tX, tX + tW);
				v = PApplet.map(mp.v, 0, h, tY, tY + tH);
				g.vertex(mp.x, mp.y, u, v);
				mp = mesh[(x + 1) + (y) * res];
				u = PApplet.map(mp.u, 0, w, tX, tX + tW);
				v = PApplet.map(mp.v, 0, h, tY, tY + tH);
				g.vertex(mp.x, mp.y, u, v);
				mp = mesh[(x + 1) + (y + 1) * res];
				u = PApplet.map(mp.u, 0, w, tX, tX + tW);
				v = PApplet.map(mp.v, 0, h, tY, tY + tH);
				g.vertex(mp.x, mp.y, u, v);
				mp = mesh[(x) + (y + 1) * res];
				u = PApplet.map(mp.u, 0, w, tX, tX + tW);
				v = PApplet.map(mp.v, 0, h, tY, tY + tH);
				g.vertex(mp.x, mp.y, u, v);
			}
		}
		g.endShape(PApplet.CLOSE);

		if (Keystone.calibrate)
			renderControlPoints(g);

		g.popMatrix();
	}
}