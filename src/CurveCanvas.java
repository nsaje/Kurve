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
*/

import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.Font;

public class CurveCanvas extends GameCanvas implements Runnable {

	private final int BG_COLOR = 0xff000000;
	private Curve s_curve = null;
	private Curve c_curve = null;
	private int s_score = 0;
	private int c_score= 0;
	private Image screen;
	private Graphics g;
	private int width, height, localWidth, localHeight;
	private boolean isServer;
	private boolean run = false;
	private Kurve midlet;
	private BluetoothConnection[] conn;
	private String msg;
	private int[] parse;
	
	public CurveCanvas(BluetoothConnection[] btConns, boolean serv, Kurve mid) {
		super(true);
		setFullScreenMode(true);
		isServer = serv;
		conn = btConns;
		
		localWidth = getWidth();
		localHeight = getHeight();
		int remoteWidth, remoteHeight;
		mid.getLogger().write("pred_merami", 0);
		try {
			conn[0].writeString(localWidth+":"+localHeight);
			msg = conn[0].readString();
			parse = split(msg);
			remoteWidth = parse[0];
			remoteHeight = parse[1];
		} catch (Exception ex) {
			midlet.startUI();
			return;
		}
		mid.getLogger().write("po_merah", 0);
		width = localWidth>remoteWidth ? remoteWidth : localWidth;
		height = localHeight>remoteHeight ? remoteHeight : localHeight;
		
		screen = Image.createImage(width, height);
		drawGameScreen();
		this.start();
		midlet = mid;
	}
	
	public void start() {		
		Thread runner = new Thread(this);
		runner.start();
	}
	
	public void run() {
		try {
			startGame();
            
            long tm = System.currentTimeMillis();
			while (true) {
				if (run) {
					int input = checkUserInput();
					s_curve.updateRotation(input);
					//conn[0].getOutputStream().write(input);
					conn[0].writeString(input+"");
					
					//c_curve.updateRotation(conn[0].getInputStream().read());
					c_curve.updateRotation(Integer.parseInt(conn[0].readString()));
					
					updateGameScreen();
				}

				if (s_curve.dead || c_curve.dead) {
					run = false;
					if (c_curve.dead) s_score++;
					if (s_curve.dead) c_score++;
					Thread.sleep(3000);
					//drawText("("+conn[0].getLocalName()+")\n"+s_score, "("+conn[0].getRemoteName()+")\n"+c_score);
					drawScore();
					while (checkUserInput()==0){continue;}
					//conn[0].getOutputStream().write(1);
					//conn[0].getInputStream().read();
					conn[0].writeString("1");
					conn[0].readString();
					startGame();
					tm = System.currentTimeMillis();
				} else {
					tm += 100;
					Thread.currentThread();
					Thread.sleep(Math.max(0, tm-System.currentTimeMillis()));
				}
			}
		} catch (Exception e) { System.out.print(e.getMessage()); } 
	}
	
	private void startGame() throws InterruptedException {
		initGameState();
		drawGameScreen();
		updateGameScreen();
		Thread.sleep(5000);
		run = true;
	}
	
	private void initGameState() {
		try {
			s_curve = new Curve(width, height, screen, isServer);
			conn[0].writeString(s_curve.getInfo());
			
			c_curve = new Curve(conn[0].readString(), screen, !isServer);
		} catch (Exception e) {}
	}
	
	private int checkUserInput() {
		int keyState = getKeyStates();
		if ((keyState & RIGHT_PRESSED) != 0) {
			return 2;
		} else if ((keyState & LEFT_PRESSED) != 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	private void updateGameScreen() {
		g = screen.getGraphics();
		if (s_curve.isDrawn()==1) {
			g.setColor(s_curve.getColor());
			g.fillRect(s_curve.getX(), s_curve.getY(), 4, 4);
		}
		if (c_curve.isDrawn()==1) {
			g.setColor(c_curve.getColor());
			g.fillRect(c_curve.getX(), c_curve.getY(), 4, 4);
		}
		g = this.getGraphics();
		g.drawImage(screen, (localWidth-width)/2, (localHeight-height)/2, 0);
		this.flushGraphics();
	}
	
	private void drawGameScreen() {
		g = screen.getGraphics();
		g.setColor(BG_COLOR);
		g.fillRect(0,0,width,height);
		g.setColor(0xffffffff);
		g.drawRect(2,2,width-5,height-5);
		g = this.getGraphics();
		g.setColor(BG_COLOR);
		g.fillRect(0,0,localWidth, localHeight);
		g.drawImage(screen, (localWidth-width)/2, (localHeight-height)/2, 0);
		this.flushGraphics();
	}
	
	/*private void drawText(String txt1, String txt2) {
		g = this.getGraphics();
		g.setColor(0x33000000);
		g.fillRect(0,0,localWidth,localHeight);
		g.setColor(0xffffffff);
		g.drawString(txt1, localWidth/2, localHeight/4, Graphics.TOP|Graphics.HCENTER);
		g.drawString(":", localWidth/2, localHeight/4*2, Graphics.TOP|Graphics.HCENTER);
		g.drawString(txt2, localWidth/2, localHeight/4*3,Graphics.BOTTOM|Graphics.HCENTER);
		this.flushGraphics();
	}*/
	private void drawScore() {
		Font name_f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
		Font score_f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
		g = this.getGraphics();
		g.setColor(0xff000000);
		g.fillRect(0,0,localWidth,localHeight);
		g.setColor(0xffffffff);
		g.setFont(name_f);
		g.setColor(isServer?0xffffff00:0xffff0000);
		g.drawString(conn[0].getLocalName(), localWidth/2, localHeight/2-score_f.getHeight(), Graphics.BOTTOM|Graphics.HCENTER);
		g.setFont(score_f);
		g.drawString(s_score+"", localWidth/2, localHeight/2, Graphics.BOTTOM|Graphics.HCENTER);
		g.setColor(isServer?0xffff0000:0xffffff00);
		g.drawString(c_score+"", localWidth/2, localHeight/2, Graphics.TOP|Graphics.HCENTER);
		g.setFont(name_f);
		g.drawString(conn[0].getRemoteName(), localWidth/2, localHeight/2+score_f.getHeight(),Graphics.TOP|Graphics.HCENTER);
		this.flushGraphics();
	}
	
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
