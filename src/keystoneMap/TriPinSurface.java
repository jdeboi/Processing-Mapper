
package keystoneMap;

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.media.jai.PerspectiveTransform;
import javax.media.jai.WarpPerspective;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;


public class TriPinSurface extends CornerPinSurface {
	
	public static int TP;
	
	public TriPinSurface(PApplet parent, int w, int h, int res) {
		super(parent, w, h, res);
		
		TP = res/2 - 1;
		mesh[TP].setControlPoint(true);
		mesh[TL].setControlPoint(false);
		mesh[TR].setControlPoint(false);
		
		type = "TRI";
	}
	
	
	/**
	 * Returns true if the mouse is over this surface, false otherwise.
	 */
	public boolean isMouseOver() {
		if (isPointInTriangle(parent.mouseX - x, parent.mouseY - y, mesh[TP],
				mesh[BL], mesh[BR]))
			return true;
		return false;
	}
	
	public void calculateMesh() {

//		nada
	}
	
	// Compute barycentric coordinates (u, v, w) for
	// point p with respect to triangle (a, b, c)
	void Barycentric(PVector p, PVector a, PVector b, PVector c, float u, float v, float w)
	{
	    PVector v0 = b.sub(a), v1 = c.sub(a), v2 = p.sub(a);
	    float d00 = v0.dot(v0);
	    float d01 = v0.dot(v1);
	    float d11 = v1.dot(v1);
	    float d20 = v2.dot(v0);
	    float d21 = v2.dot(v1);
	    float denom = d00 * d11 - d01 * d01;
	    v = (d11 * d20 - d01 * d21) / denom;
	    w = (d00 * d21 - d01 * d20) / denom;
	    u = 1.0f - v - w;
	}
	
	public void render(PImage texture, PVector[] uvPoints) {
		PGraphics g = parent.g;
		g.pushMatrix();
		g.translate(x, y);
	
		g.noStroke();
		g.fill(0);
		g.beginShape();
		g.texture(texture);
		int u = 0;
		int v = h;
		g.vertex(mesh[BL].x, mesh[BL].y, u, v);
		
		u = w/2;
		v = 0;
		g.vertex(mesh[TP].x, mesh[TP].y, u, v);
		
		u = w;
		v = h;
		g.vertex(mesh[BR].x, mesh[BR].y, u, v);
		g.endShape(PApplet.CLOSE);

		if (Keystone.calibrate) {
			renderControlPoints(g);
		}
		

		g.popMatrix();
	}
	
	public void render(PGraphics g, PImage texture, int tX, int tY, int tW,
			int tH) {
		PVector [] uvPoints = {new PVector(0, h), new PVector(w/2, 0), new PVector(w, h)};
		render(texture, uvPoints);
		
	}
	
	public PVector getP1() {
		PVector p = new PVector(mesh[TP].x, mesh[TP].y);
		return p;
	}
	
	public PVector getP2() {
		PVector p = new PVector(mesh[BL].x, mesh[BL].y);
		return p;
	}
	
	public PVector getP3() {
		PVector p = new PVector(mesh[BR].x, mesh[BR].y);
		return p;
	}
}