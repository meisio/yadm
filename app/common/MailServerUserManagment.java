package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * 
 * @author Eugen Meissner
 *
 */
public class MailServerUserManagment {

	private Properties users;
	private File usersFile;
	
	private static MailServerUserManagment instance;
	private static Object lock = new Object();
	
	public static MailServerUserManagment getInstance(){
		if(instance == null){
			synchronized (lock) {
				if(instance == null)
					instance = new MailServerUserManagment();
			}
		}
		
		return instance;
	}
	
	/**
	 * 
	 */
	private MailServerUserManagment(){
		usersFile = new File("./ext/jes/user.conf");
		users = new Properties();
		read();
	}
	
	/**
	 * 
	 * @param user
	 * @param host
	 * @param password
	 */
	public synchronized void addUser(String user, String host, String password){
		String userKey = getUserKey(user, host);
		users.put(userKey, password);
		write();
	}
	
	/**
	 * 
	 * @param user
	 * @param host
	 * @param password
	 * @param forwardAddress
	 */
	public synchronized void addUserWithForwarding(String user, String host, String password, String forwardAddress){
		String userKey = getUserKey(user, host);
		String forwardKey = getUserForwadKey(user, host);
		
		users.put(userKey, password);
		users.put(forwardKey, forwardAddress);
		write();
	}
	
	public synchronized void removeUser(String user, String host){
		String userKey = getUserKey(user, host);
		String forwardKey = getUserForwadKey(user, host);
		
		if(users.containsKey(userKey)){
			users.remove(userKey);
		}
		
		if(users.containsKey(forwardKey)){
			users.remove(forwardKey);
		}
		
		write();
	}
	
	/**
	 * 
	 * @param oldForwardAddress
	 * @param newForwardAddress
	 */
	public synchronized void updateUserForwarding(String oldForwardAddress, String newForwardAddress){
		for( Entry<Object, Object> e : users.entrySet()){
			if(e.getValue().equals(oldForwardAddress)){
				users.put(e.getKey(), newForwardAddress);
			}
		}
	}
	
	private String getUserKey(String user, String host){
		return "user."+user+"@"+host;
	}
	
	private String getUserForwadKey(String user, String host){
		return "userprop."+user+"@"+host+".forwardAddresses";
	}
		
	private void write(){
		try {
			OutputStream out = new FileOutputStream(usersFile);
			users.store(out, "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void read(){
		Properties tmp = (Properties) users.clone();
		FileInputStream is;
		try {
			is = new FileInputStream(usersFile);
			users = new Properties();
			users.load(is);
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			users = (Properties)tmp.clone();
			e.printStackTrace();
		}
	}
	
}
