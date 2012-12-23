/*
This file is part of Kurve.

    Kurve is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Kurve is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Kurve.  If not, see <http://www.gnu.org/licenses/>.
*/a

import java.util.Random;
import java.util.Vector;
import java.lang.Math;
import javax.microedition.lcdui.Image;

public class Curve {
	public final static int ROTATION_STEP = 15;
	public final static int SIZE = 4;
	private final static int SPEED = 4;
	private int x, y, dx, dy, rotation, color;
	public boolean dead = false;
	public boolean draw = true;
	private Image screen;
	private static Random rng = new Random(System.currentTimeMillis());
	private int hole_count;
	
	public Curve(int width, int height, Image scr, boolean isServer) {
		x = rng.nextInt(width/2)+width/4;
		y = rng.nextInt(height/2)+height/4;
		hole_count = rng.nextInt(50-2);
		rotation = rng.nextInt(24)*15;
		if (isServer) color = 0x00FFFF00;
		else color = 0xFFFF0000;
		screen = scr;
	}
	
	public Curve(String info, Image scr, boolean isServer) {
		setInfo(info);
		if (isServer) color = 0x00FFFF00;
		else color = 0xFFFF0000;
		screen = scr;
	}
	
	public Curve(int x, int y, int rotation, Image scr, boolean isServer) {
		this.x = x;
		this.y = y;
		this.rotation = rotation;
		if (isServer) color = 0x00FFFF00;
		else color = 0xFFFF0000;
		this.screen = scr;
	}
	
	public void updatePosition() {
		if (!dead) {
			dx = (int)(Math.sin((double)rotation*(Math.PI/180.0))*SPEED);
			dy = (int)(Math.cos((double)rotation*(Math.PI/180.0))*SPEED*-1);
			x += dx;
			y += dy;
			checkHit();
			if (hole_count == 50-2) {
				draw = false;
				hole_count++;
			} else if (hole_count == 50) {
				hole_count = 0;
				draw = true;
			} else {
				hole_count++;
			}
		}
	}
	
	private void checkHit() {
		int[] pixels = new int[16];
		screen.getRGB(pixels, 0, 4, x, y, 4, 4);	
		for (int i=0; i<4; i++) {
			for (int j=0; j<4; j++) {
				if (pixels[i+j*4] != 0xff000000
				&& (i>-dx+3||i<-dx ||(dx==0&&(dy==4||dy==(-4))))
				&& (j>-dy+3||j<-dy ||(dy==0&&(dx==4||dx==(-4))))
				)
				{
					dead = true;
				}
			}
		}
	}
	
	public void updateRotation(int rot) {
		switch (rot) {
		case 0: break;
		case 1: rotation+=Curve.ROTATION_STEP*-1; break;
		case 2: rotation+=Curve.ROTATION_STEP; break;
		}
		updatePosition();
	}
	
	public void setInfo(String info) {
		if (!dead) {
			int[] parse;
			parse = split(info);
			x = parse[0];
			y = parse[1];
			rotation = parse[2];
			hole_count = parse[3];
		}
	}
	
	public String getInfo() { return x+":"+y+":"+rotation+":"+hole_count; }
	public int getX() { return x; }
	public int getY() { return y; }
	public int getRotation() { return rotation; }
	public int getColor() { return color; }
	public int isDrawn() { return draw?1:0; }
	public void setDrawn(int d) { draw = (d>0?true:false); }
	public int isDead() { return (dead?1:0); }
	public void setDead(int d) { dead = (d>0?true:false); }
	
	private int[] split(String original) {
		Vector nodes = new Vector();
		String separator = ":";

		int index = original.indexOf(separator);
		while(index>=0) {
			nodes.addElement( original.substring(0, index) );
			original = original.substring(index+separator.length());
			index = original.indexOf(separator);
		}
		nodes.addElement( original );

		int[] result = new int[ nodes.size() ];
		if( nodes.size()>0 ) {
		for(int loop=0; loop<nodes.size(); loop++)
			{
				result[loop] = Integer.parseInt((String)nodes.elementAt(loop));
			}
		}

		return result;
	}
}

