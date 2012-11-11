package models;

/**
 * This class represents the form login.
 * 
 * @author Eugen Meissner
 *
 */
public class FormLogin {

	public String username;
	public String password;

	/**
	 * 
	 */
	public FormLogin(){
		
	}
	
	/**
	 * 
	 * @param username
	 */
	public FormLogin(String username) {
		this.username = username;
	}
	
}
