/**
 * Copyright (C) 2009-15 David Bouchard
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package keystoneMap;

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.media.jai.PerspectiveTransform;
import javax.media.jai.WarpPerspective;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.XML;

/**
 * A simple Corner Pin "keystoned" surface. The surface is a quad mesh that can
 * be skewed to an arbitrary shape by moving its four corners.
 * 
 * September-2011 Added JAI library for keystone calculus (@edumo)
 * 
 * March-2013 Added methods to programmatically move the corner points
 */
public abstract class CornerPinSurface implements Draggable  {

	PApplet parent;

	MeshPoint[] mesh;

	public float x;
	public float y;
	float clickX;
	float clickY;

	int res;

	// Daniel Wiedeman: made them public static
	public static int TL; // top left
	public static int TR; // top right
	public static int BL; // bottom left
	public static int BR; // bottom right

	int w;
	int h;

	int gridColor;
	int controlPointColor;
	
	String type;

	// Jai class for keystone calculus
	WarpPerspective warpPerspective = null;

	/**
	 * @param parent
	 *            The parent applet -- used for default rendering mode
	 * @param w
	 *            The surface's width, in pixels
	 * @param h
	 *            The surface's height, in pixels
	 * @param res	
	 *            The surface's grid resolution
	 */
	CornerPinSurface(PApplet parent, int w, int h, int res) {

		this.parent = parent;

		this.w = w;
		this.h = h;

		res++;
		this.res = res;

		// initialize the point array
		mesh = new MeshPoint[res * res];
		for (int i = 0; i < mesh.length; i++) {
			float x = (i % res) / (float) (res - 1);
			float y = (i / res) / (float) (res - 1);
			mesh[i] = new MeshPoint(this, x * w, y * h, x * w, y * h);
		}

		// indices of the corner points
		TL = 0 + 0; // x + y
		TR = res - 1 + 0;
		BL = 0 + (res - 1) * (res);
		BR = res - 1 + (res - 1) * (res);

		// make the corners control points
		mesh[TL].setControlPoint(true);
		mesh[TR].setControlPoint(true);
		mesh[BL].setControlPoint(true);
		mesh[BR].setControlPoint(true);

		calculateMesh();

		this.gridColor = 128;
		this.controlPointColor = 0xFF00FF00;
	}
	
	public PVector getPointOnTransformedPlane(float x, float y) {
		
		
		 //The float constructor is deprecated, so casting everything to double
				PerspectiveTransform transform = PerspectiveTransform.getQuadToQuad(0,
						0, w, 0, w, h, 0,
						h, // source to
						mesh[TL].x, mesh[TL].y, mesh[TR].x, mesh[TR].y, mesh[BR].x,
						mesh[BR].y, mesh[BL].x, mesh[BL].y); // dest

				warpPerspective = new WarpPerspective(transform);


				Point2D point = warpPerspective.mapDestPoint(new Point((int) x,
							(int) y));
				PVector mapped = new PVector( (float) point.getX(), (float) point.getY());
				return mapped;
	}


	
	// ///////////////
	// MANUAL MESHPOINT MOVE FUNCTIONS
	// added by Daniel Wiedemann
	// to move meshpoints via keyboard for example (in OSX the mouse can not go
	// further then the screen bounds, which is obviously a very unpleasant
	// thing if corner points have to be moved across them)
	// ///////////////
	/**
	 * Manually move one of the corners for this surface by some amount. 
	 * The "corner" parameter should be either: CornerPinSurface.TL, CornerPinSurface.BL, 
	 * CornerPinSurface.TR or CornerPinSurface.BR*
	 */
	public void moveMeshPointBy(int corner, float moveX, float moveY) {
		mesh[corner].moveTo(mesh[corner].x + moveX, mesh[corner].y + moveY);
	}

	/**
	 * @return The surface's mesh resolution, in number of "tiles"
	 */
	public int getRes() {
		// The actual resolution is the number of tiles, not the number of mesh
		// points
		return res - 1;
	}

	/**
	 * Renders and applies keystoning to the image using the parent applet's
	 * renderer.
	 */
	public void render(PImage texture) {
		render(parent.g, texture);
	}

	/**
	 * Renders and applies keystoning to the image using a specific renderer.
	 */
	public void render(PGraphics g, PImage texture) {
		render(g, texture, 0, 0, w, h);
	}

	/**
	 * Renders and applies keystoning to the image using the parent applet's
	 * renderer.The tX, tY, tW and tH parameters specify which section of the
	 * image to render onto this surface.
	 */
	public void render(PImage texture, int tX, int tY, int tW, int tH) {
		render(parent.g, texture, tX, tY, tW, tH);
	}

	/**
	 * Renders and applies keystoning to the image using a specific render. The
	 * tX, tY, tW and tH parameters specify which section of the image to render
	 * onto this surface.
	 */
	public abstract void render(PGraphics g, PImage texture, int tX, int tY, int tW,
			int tH);

	
	public abstract boolean isMouseOver();
	
	
	/**
	 * Draws targets around the control points
	 */
	public void renderControlPoints(PGraphics g) {
		g.stroke(controlPointColor);
		g.noFill();
		for (int i = 0; i < mesh.length; i++) {
			if (mesh[i].isControlPoint()) {
				g.ellipse(mesh[i].x, mesh[i].y, 30, 30);
				g.ellipse(mesh[i].x, mesh[i].y, 10, 10);
			}
		}
	}
	
	/**
	 * This function will give you the position of the mouse in the surface's
	 * coordinate system.
	 * 
	 * @return The transformed mouse position
	 */

	public PVector getTransformedCursor(int cx, int cy) {
		Point2D point = warpPerspective.mapSourcePoint(new Point(cx - (int) x,
				cy - (int) y));
		return new PVector((int) point.getX(), (int) point.getY());
	}

	
	public PVector getTransformedMouse() {
		return getTransformedCursor(parent.mouseX, parent.mouseY);
	}

	// 2d cross product
	public float cross2(float x0, float y0, float x1, float y1) {
		return x0 * y1 - y0 * x1;
	}



	/**
	 * Sets the grid used for calibration's color
	 */
	public void setGridColor(int newColor) {
		gridColor = newColor;
	}

	/**
	 * Sets the control points color
	 */
	public void setControlPointsColor(int newColor) {
		controlPointColor = newColor;
	}

	/**
	 * @invisible
	 */
	Draggable select(float x, float y) {
		// first, see if one of the control points are selected
		x -= this.x;
		y -= this.y;
		for (int i = 0; i < mesh.length; i++) {
			if (PApplet.dist(mesh[i].x, mesh[i].y, x, y) < 30
					&& mesh[i].isControlPoint())
				return mesh[i];
		}

		// then, see if the surface itself is selected
		if (isMouseOver()) {
			clickX = x;
			clickY = y;
			return this;
		}
		return null;
	}
	
	
	/**
	 * Used for mouse selection of surfaces
	 */
	public boolean isPointInTriangle(float x, float y, MeshPoint a,
			MeshPoint b, MeshPoint c) {
		// http://www.blackpawn.com/texts/pointinpoly/default.html
		PVector v0 = new PVector(c.x - a.x, c.y - a.y);
		PVector v1 = new PVector(b.x - a.x, b.y - a.y);
		PVector v2 = new PVector(x - a.x, y - a.y);

		float dot00 = v0.dot(v0);
		float dot01 = v1.dot(v0);
		float dot02 = v2.dot(v0);
		float dot11 = v1.dot(v1);
		float dot12 = v2.dot(v1);

		// Compute barycentric coordinates
		float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
		float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
		float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

		// Check if point is in triangle
		return (u > 0) && (v > 0) && (u + v < 1);
	}

	/**
	 * Interpolates the position of the points in the mesh according to the 4
	 * corners TODO: allow for arbitrary control points, not just the four
	 * corners
	 */
	abstract void calculateMesh();


	/**
	 * @invisible
	 * 
	 *            This moves the surface according to the offset from where the
	 *            mouse was pressed when selecting the surface.
	 */
	public void moveTo(float x, float y) {
		this.x = x - clickX;
		this.y = y - clickY;
	}

	/**
	 * @invisible
	 * 
	 *            Populates values from an XML object
	 */
	void load(XML xml) {

		this.x = xml.getFloat("x");
		this.y = xml.getFloat("y");
		// reload the mesh points
		XML[] pointsXML = xml.getChildren("point");
		for (XML point : pointsXML) {
			MeshPoint mp = mesh[point.getInt("i")];
			mp.x = point.getFloat("x");
			mp.y = point.getFloat("y");
			mp.u = point.getFloat("u");
			mp.v = point.getFloat("v");
			mp.setControlPoint(true);
		}
		calculateMesh();
	}

	XML save() {

		XML parent = new XML("surface");

		parent.setFloat("x", x);
		parent.setFloat("y", y);

		for (int i = 0; i < mesh.length; i++) {
			if (mesh[i].isControlPoint()) {
				// fmt = "point i=\"%d\" x=\"%f\" y=\"%f\" u=\"%f\" v=\"%f\"";
				// fmted = String.format(fmt, i, s.mesh[i].x, s.mesh[i].y,
				// s.mesh[i].u, s.mesh[i].v);
				XML point = new XML("point");
				point.setFloat("x", mesh[i].x);
				point.setFloat("y", mesh[i].y);
				point.setFloat("u", mesh[i].u);
				point.setFloat("v", mesh[i].v);
				point.setFloat("i", i);
				parent.addChild(point);
			}
		}
		return parent;
	}
	
	public int getWidth() {
		return w;
	}
	
	public int getHeight() {
		return h;
	}

}
