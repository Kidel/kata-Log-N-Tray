package com.logntray.bonofiglio;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.net.URL;

public class UI {

	private LoginFrame frame;
	private TrayIcon trayIcon;
	private SystemTray tray;

	private String title;

	private Repository r = new Repository();

	public UI() {
		title = "Log 'n Tray";
		/* Use an appropriate Look and Feel */
		if (!tryLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"))
			tryLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

		/* Turn off metal's use of bold fonts */
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		//Schedule a job for the event-dispatching thread:
		//adding TrayIcon.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private boolean tryLookAndFeel(String lf) {
		boolean ret = false;	
		try {
			UIManager.setLookAndFeel(lf);
			ret = true;
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
			ret = false;
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
			ret = false;
		} catch (InstantiationException ex) {
			ex.printStackTrace();
			ret = false;
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			ret = false;
		}

		return ret;
	}

	private void showInfoTray(String message) {
		trayIcon.displayMessage(title,
				message, TrayIcon.MessageType.INFO);
	}

	private void createAndShowGUI() {
		// Login window
		frame = new LoginFrame(title);
		frame.setActionLogin(
				new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						if (r.authenticate(frame.getLoginText(), frame.getPasswordText())) {
							JOptionPane.showMessageDialog(null,
									"Hi " + frame.getLoginText() + "! You have successfully logged in.",
									"Login",
									JOptionPane.INFORMATION_MESSAGE);
							frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							frame.dispose();
							setTrayIcon("online","png");
						} else {
							JOptionPane.showMessageDialog(null,
									"Invalid username or password",
									"Login",
									JOptionPane.ERROR_MESSAGE);
							// reset username and password
							frame.setPasswordText("");

						}
					}
				});		
		
		// Login window behavior when closed (windowClosed will only apply after a successful login because of DISPOSE_ON_CLOSE)
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				//
			}

			@Override
			public void windowClosed(WindowEvent e) {
				showInfoTray(title + " is still open. Right click on the tray icon to change your status.");
			}
		});

		//Check the SystemTray support
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		
		// setting up the System Tray
		trayIcon = new TrayIcon(createImage("../../../images/offline.png", "offline"));
		tray = SystemTray.getSystemTray();

		trayIcon.setToolTip(title);
		trayIcon.setImageAutoSize(true);
		
		// setting up the System Tray menu
		final PopupMenu popup = new PopupMenu();

		// Create a popup menu components
		MenuItem aboutItem = new MenuItem("About");
		Menu displayMenu = new Menu("Status");
		MenuItem activeItem = new MenuItem("Active");
		MenuItem busyItem = new MenuItem("Busy");
		MenuItem exitItem = new MenuItem("Exit");

		//Add components to popup menu
		popup.add(aboutItem);
		popup.addSeparator();
		popup.add(displayMenu);
		displayMenu.add(activeItem);
		displayMenu.add(busyItem);
		popup.addSeparator();
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			JOptionPane.showMessageDialog(null,
					"Critical Error!");
			e.printStackTrace();
			System.out.println("TrayIcon could not be added.");
			System.exit(0);
			return;
		}

		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"This is Log 'n Tray");
			}
		});

		activeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showInfoTray("You are now active");
				setTrayIcon("online","png");
				r.setStatus("active");
			}
		});

		busyItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showInfoTray("You are now busy");
				setTrayIcon("busy","png");
				r.setStatus("busy");
			}
		});


		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				r.logout();
				setTrayIcon("offline","png");
				tray.remove(trayIcon);
				System.exit(0);
			}
		});
	}

	protected URL getFileURL(String path) {
		return LogNTray.class.getResource(path);
	}

	protected Image createImage(String path, String description) {
		URL imageURL = getFileURL(path);
		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}
	
	protected void setTrayIcon(String iconName, String iconExt) {
		trayIcon.setImage(createImage("../../../images/"+iconName+"."+iconExt, iconName));
	}

}
