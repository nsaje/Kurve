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

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;

public class Kurve extends MIDlet implements CommandListener {

	private CurveCanvas gCanvas;
	private List mainList;
	private BluetoothDiscovery disc;
    public MIDPLogger log;
    public Kurve root = this;
    
    public MIDPLogger getLogger() { return log; }

	public Kurve() {
		try {
			log = new MIDPLogger(MIDPLogger.DEBUG,true,false);
		}
		catch (Exception e) { 
			System.out.println("Exception creating MIDPLogger");
			e.printStackTrace();
		}
		this.getLogger().write("Kurve konstruktor", 0);
		ErrorScreen.init(null, getDisplay());
		disc = new BluetoothDiscovery(getDisplay());
	}

	public void startApp() {
		String name;
		disc.setServiceUUID("20000000000010008000006057028C19");
		try {
			name = LocalDevice.getLocalDevice().getFriendlyName();
		} catch (BluetoothStateException ex) {
			showAlertAndExit("", "Prižgi Bluetooth!", AlertType.ERROR);
			return;
		}
		clearName(name);
		disc.setName(name);
		startUI();
	}
	
	private String clearName(String name) {
		String allowed = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxZz";
		for (int i=0; i<name.length(); i++) {
			if (allowed.indexOf(name.charAt(i)) == -1) {
				name.replace(name.charAt(i), 'X');
			}
		}
		return name;
	}
	
	public void startUI() {
		getDisplay().setCurrent(getList());
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
		log.close();
	}
	
	public void Exit() {
		destroyApp(false);
		notifyDestroyed();
	}
	
	private void showAlertAndExit(String t, String s, AlertType type) {
		Alert a = new Alert(t,s,null,type);
		a.addCommand(new Command("Izhod", Command.EXIT, 1));
		a.setCommandListener(this);
		getDisplay().setCurrent(a);
	}
	
    public void commandAction( Command c, Displayable d )
    {
        if( c.equals(List.SELECT_COMMAND) || (c.getCommandType() == Command.OK) )
        {
            int i = mainList.getSelectedIndex();
            if( i == 0 )
            {
                // Start Server
                ServerThread st = new ServerThread();
                st.start();
            }
            if( i == 1 )
            {   // Start Client with SEARCH_ALL_DEVICES_SELECT_ONE
                ClientThread ct = new ClientThread( BluetoothDiscovery.SEARCH_ALL_DEVICES_SELECT_ONE );
                ct.start();
            }
        }
        else if( c.getCommandType() == Command.EXIT )
        {
            Exit();
        }
    }
	
	public List getList() {
		if (mainList == null) {
			mainList = new List("Kurve", List.IMPLICIT);
			mainList.append("Createj (rumena kurva)", null);
			mainList.append("Joinej (rdeča kurva)", null);
			mainList.addCommand(new Command("Izberi", Command.OK, 1));
			mainList.addCommand(new Command("Izhod", Command.EXIT, 1));
			mainList.setCommandListener(this);
		}
		return mainList;
	}
	
	public Display getDisplay() {
		return Display.getDisplay(this);
	}
	
// By Nokia, modified by me :)
    // Inner class
    /** The ServerThread is used to wait until someone connects. <br
     * A thread is needed otherwise it would not be possible to display
     * anything to the user.
     */
    private class ServerThread
    extends Thread
    {
        /**
         * This method runs the server.
         */
        public void run()
        {
            try
            {
                // Wait on client
                BluetoothConnection[] con = disc.waitOnConnection();
                if( con[0] == null )
                {   // Connection canceled
                    startUI();
                    return;
                }

                // Create Canvas to display keystrokes
                gCanvas = new CurveCanvas( con, true, root );
                gCanvas.addCommand(new Command("Izhod", Command.EXIT, 1));
                gCanvas.setCommandListener(root);
                // Set as new display
                getDisplay().setCurrent( gCanvas );
            }
            catch( Exception e )
            {    // display error message
                showAlertAndExit( "Error:", e.getMessage(), AlertType.ERROR );
                return;
            }
        }
    }

    // Inner class
    /** The ClientThread is used to search for devices/Services and connect to them. <br>
     * A thread is needed otherwise it would not be possible to display
     * anything to the user.
     */
    private class ClientThread
    extends Thread
    {
        // Search type
        private int searchType;

        /** Constructor
         * @param st The search type. Possible values:
         * {@link BluetoothDiscovery.SEARCH_CONNECT_FIRST_FOUND SEARCH_CONNECT_FIRST_FOUND},
         * {@link BluetoothDiscovery.SEARCH_CONNECT_ALL_FOUND SEARCH_CONNECT_ALL_FOUND},
         * {@link BluetoothDiscovery.SEARCH_ALL_DEVICES_SELECT_ONE SEARCH_ALL_DEVICES_SELECT_ONE},
         * {@link BluetoothDiscovery.SEARCH_ALL_DEVICES_SELECT_SEVERAL SEARCH_ALL_DEVICES_SELECT_SEVERAL}.
         */
        protected ClientThread( int st )
        {
            // store search type
            searchType = st;
        }


        /**
         * This method runs the client.
         */
        public void run()
        {
            try
            {
            	root.getLogger().write("prej", 0);
                BluetoothConnection conn[] = disc.searchService( searchType );
                root.getLogger().write("pol", 0);
                if( conn.length != 0 )
                {   // Create Canvas object which deals with receive and send
                	root.getLogger().write("not-prej", 0);
                    gCanvas = new CurveCanvas( conn, false, root );
                    gCanvas.addCommand(new Command("Izhod", Command.EXIT, 1));
                    gCanvas.setCommandListener(root);
                    root.getLogger().write("not-pol", 0);
                    // Set as new display
                    getDisplay().setCurrent( gCanvas );
                }
                else
                {   // nothing found
                    startUI();
                }
            }
            catch( Exception e )
            {    // display error message
                showAlertAndExit( "Error:", e.getMessage(), AlertType.ERROR );
                return;
            }
        }
    }
  
}
