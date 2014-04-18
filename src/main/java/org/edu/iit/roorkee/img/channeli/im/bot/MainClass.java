package org.edu.iit.roorkee.img.channeli.im.bot;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import javax.net.ssl.SSLSocketFactory;

import org.aitools.programd.Core;
import org.aitools.programd.util.URLTools;
import org.aitools.programd.util.XMLKit;
import org.apache.commons.lang.WordUtils;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

public class MainClass implements MessageListener, ChatManagerListener {
	private static XMPPConnection connection;

	static Connection regol;

	private static Core core;

	public void login(String userName, String password) throws XMPPException,
			SQLException, MalformedURLException {
		regol = DriverManager.getConnection(
				"jdbc:postgresql://ip:port/database", "username", "password");
		ConnectionConfiguration config = new ConnectionConfiguration(
				"talk.google.com", 443, "gmail.com");
		config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
		config.setSocketFactory(SSLSocketFactory.getDefault());
		connection = new XMPPConnection(config);

		connection.connect();
		connection.login(userName, password);
		connection.getChatManager().addChatListener(this);
		connection.getRoster().setSubscriptionMode(
				Roster.SubscriptionMode.accept_all);

		// Send the connect string, for the first connection.
		// this.core.processResponse(this.core.getSettings().getConnectString());
		// this.console.startShell();
		// connection.addPacketListener(new PacketListener()
		// {
		//
		// public void processPacket(Packet arg0)
		// {
		// System.out.println("processPacketCalled");
		// }
		// }, new PacketFilter()
		// {
		//
		// public boolean accept(Packet packet)
		// {
		// if (packet instanceof Presence) {
		// if (((Presence) packet).getType().equals(Presence.Type.subscribe))
		// return true;
		// System.out.println(((Presence) packet).getFrom() + ((Presence)
		// packet).getTo());
		// }
		// return false;
		// }
		// });

		// connection.
	}

	public void sendMessage(String message, String to) throws XMPPException {
		Chat chat = connection.getChatManager().createChat(to, this);
		chat.sendMessage(message);
	}

	public void displayBuddyList() {
		Roster roster = connection.getRoster();
		Collection<RosterEntry> entries = roster.getEntries();

		System.out.println("\n\n" + entries.size() + " buddy(ies):");
		for (RosterEntry r : entries)
			System.out.println(r.getUser());
	}

	public void disconnect() {
		connection.disconnect();
	}

	public void processMessage(Chat chat, Message message) {
		try {
			String msg = null;
			if (message.getType() == Message.Type.chat
					&& message.getBody() != null) {
				String incoming = message.getBody().toLowerCase();
				RosterEntry entry = connection.getRoster().getEntry(
						chat.getParticipant().split("/")[0].trim());
				if (entry.getName() == null) {
					String username = getNameFromAlternateEmail(entry.getUser());
					entry.setName(username);
					core.getResponse("My Name is " + username, chat
							.getParticipant().split("/")[0].trim(), "SampleBot");
				}
				String name = entry.getName().split(" ")[0];
				try {
					while (true) {
						if (incoming.startsWith("people")) {
							if (incoming.contains("search")) {
								String arr[] = incoming.split(" ");
								if (arr.length == 2) {
									msg = "Syntax: 'people search <name/login-id>'\te.g: people search venryuec\t(Search for student/faculty of name or login-id containing 'venryuec')\n";
									break;
								}
								String value = "";
								for (int i = 2; i < arr.length; i++) {
									if ((i + 1) == arr.length)
										value += arr[i];
									else
										value += arr[i] + "%";
								}
								if (value.length() < 4) {
									msg = "'"
											+ value
											+ "' is too short a query for a search.";
									break;
								}
								msg = searchPeople(value);
								break;
							}
							if (incoming.contains("info")) {
								String arr[] = incoming.split(" ");
								String value = arr[arr.length - 1];
								if (!value.equals("info")) {
									if (value.length() != 8) {
										msg = "'"
												+ value
												+ "' should contain exactly 8 characters.";
										break;
									}
									msg = infoPerson(value);
									break;
								}
								msg = "Syntax: 'people info <person_id>'\te.g: person info venryuec\t(Get information regarding the person)\n";
								break;
							}
							msg = "Syntax Error, Try: 'help people'";
							break;
						}
						if (incoming.startsWith("help")) {
							if (incoming.contains("buy")) {
								msg = "Syntax for Buy-Sell:\n"
										+ "'buy search <item>'\te.g: buy search trunk\t(Search for items)\n"
										+ "'buy info <item_id>'\te.g: buy info 12323\t(Get information regarding the item)\n"
										+ "'buy contact <item_id>'\te.g: buy contact 12323\t(Get contact information of the seller)";
								break;
							}
							if (incoming.contains("people")) {
								msg = "Syntax for People-Search:\n"
										+ "'people search <name/login-id>'\te.g: people search venryuec\t(Search for student/faculty of name or login-id containing 'venryuec')\n"
										+ "'people info <person_id>'\te.g: person info venryuec\t(Get information regarding the person)\n";
								break;
							}
							if (msg == null) {
								msg = "Hi, "
										+ name
										+ ". I'm Sentinel Prime, your friendly Roorkee Auto-bot.\n"
										+ "While I'm not trying to save the earth, I try and help fellow thomsonians with the following things:-\n"
										+ "Search for students/faculty and their information -> Type: 'help people'\n"
										+ "Buy stuff from the campus -> Type: 'help buy'\n"
										+ "Information about this bot -> Type: 'img-bot'";
								// +
								// "Search for the latest notices -> Type: 'help notice'\n"
								// +
								// "Get news from various sections/groups -> Type: 'help connect'\n"
								// +
								// "Get placement news -> Type: 'help placement'\n";
								break;
							}
						}
						/*
						 * Easter Eggs.
						 */
						if (incoming.contains("venkatesh")) {
							msg = "Ok. So you blew my cover. Do I know you? If you think I would recognize you.. add me here http://www.facebook.com/venkatesh.nandakumar";
							break;
						}
						if (incoming.contains(":d:d:d")) {
							msg = "For a moment I thought I heard Nambiar. Wait..what the <turn around.. psycho music plays..> Its pulkit.";
							break;
						}
						if (incoming.contains("i <3 ubuntu")
								|| incoming.contains("i love ubuntu")) {
							msg = "So do we!";
							break;
						}
						if (msg == null) {
							try {
								String[] response = XMLKit
										.filterViaHTMLTags(core.getResponse(
												incoming, chat.getParticipant()
														.split("/")[0].trim(),
												"SampleBot"));
								if (response.length > 0) {
									msg = "";
									for (int line = 0; line < response.length; line++) {
										String temp = XMLKit
												.filterWhitespace(response[line]);
										if (temp.trim().equals(""))
											continue;
										if (line == (response.length - 1)) {
											msg += temp;
											continue;
										}
										msg += temp + "\n";
									}
								}
								break;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if (msg == null) { // Errors
							msg = "Syntax Error. Sentinel Prime has gone to war with the decepticons. Type: 'help'.";
						}
					}
					chat.sendMessage(msg);
				} catch (XMPPException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			try {
				chat.sendMessage("Internal Error. Sentinel Prime has gone to war with the decepticons. Please contact IMG, IIT Roorkee");
			} catch (XMPPException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	private static String infoPerson(String value) throws SQLException {
		Statement stmt = regol.createStatement();
		ResultSet rs = stmt
				.executeQuery("SELECT name,person_id,degree,d.corresponds_to as discipline,alternate_email,contact_phone_no,room_no,b.corresponds_to as bhawan,gender from person,codes_used d,codes_used b where person_id='"
						+ value + "' and d.code=discipline and b.code=bhawan");
		if (rs.next()) {
			String msg;
			String gender = rs.getString("gender").equals("t") ? "He" : "She";
			msg = "*"
					+ WordUtils.capitalize(rs.getString("name").toLowerCase())
					+ "* is a *" + rs.getString("degree") + "* student of *"
					+ rs.getString("discipline") + "*\n";
			msg += gender + " stays at *" + rs.getString("room_no") + " "
					+ rs.getString("bhawan")
					+ "* and can be contacted through:\n";
			msg += "E-mail: *" + rs.getString("person_id") + "@iitr.ernet.in "
					+ rs.getString("alternate_email") + "*\n";
			msg += "Phone: *" + rs.getString("contact_phone_no") + "*\n";
			return msg;
		}
		return "No result for search term '" + value + "'";
	}

	private static String searchPeople(String value) throws SQLException {
		Statement stmt = regol.createStatement();
		ResultSet rs = stmt
				.executeQuery("SELECT name,person_id,degree,corresponds_to from person,codes_used where (person_id ilike '%"
						+ value
						+ "%' OR name ilike '%"
						+ value
						+ "%') and code=discipline");
		boolean exists = false;
		String msg = "\tName\t|\tperson_id\t|\tDegree\t|\tDiscipline\n";
		while (rs.next()) {
			if (!exists)
				exists = true;
			msg += WordUtils.capitalize(rs.getString("name").toLowerCase())
					+ " | " + rs.getString("person_id") + " | "
					+ rs.getString("degree") + " | "
					+ rs.getString("corresponds_to") + "\n";
		}
		if (exists)
			return msg;
		else
			return "No Record of Person with person_id '" + value + "'";
	}

	private static String getNameFromAlternateEmail(String user)
			throws SQLException {
		Statement stmt = regol.createStatement();
		ResultSet rs = stmt
				.executeQuery("SELECT name from person where alternate_email ='"
						+ user + "'");
		if (rs.next())
			return WordUtils.capitalize(rs.getString(1).toLowerCase());
		return "Student Unknown";
	}

	public static void main(String args[]) throws XMPPException, IOException,
			SQLException {
		MainClass c = new MainClass();
		// turn on the enhanced debugger
		// XMPPConnection.DEBUG_ENABLED = true;
		try {
			core = new Core(URLTools.createValidURL(System
					.getProperty("user.dir") + "/ProgramD/"),
					URLTools.createValidURL("file://"
							+ System.getProperty("user.dir")
							+ "/ProgramD/conf/core.xml"));
		} catch (Exception e) {
			System.exit(1);
			e.printStackTrace();
		}
		c.login("channeli.bot@gmail.com", "password");
		System.out.println("all up and running.");
		// c.connection.getChatManager().addChatListener(this);

		// c.displayBuddyList();
		// c.acceptBuddies();

		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//
	// private void acceptBuddies()
	// {
	// Roster roster = connection.getRoster();
	// Collection<RosterEntry> entries = roster.getEntries();
	//
	// System.out.println("\n\n" + entries.size() + " buddy(ies):");
	// for (RosterEntry r : entries) {
	// System.out.println(r.getUser() + "- pending : " +
	// r.getStatus().equals(ItemStatus.SUBSCRIPTION_PENDING));
	// System.out.println(r.getUser() + "- UNSUBSCRIPTION PENDING : "
	// + r.getStatus().equals(ItemStatus.UNSUBSCRIPTION_PENDING));
	// }
	// }

	public void chatCreated(Chat chat, boolean arg) {
		chat.addMessageListener(this);
	}

}
