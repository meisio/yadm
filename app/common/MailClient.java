package common;

import javax.mail.Message.RecipientType;

import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.Mailer;

/**
 * This is a client to send common mails such reset password.
 * 
 * @author Eugen Meissner
 *
 */
public class MailClient {
	
	private String host = "localhost";
	private String username = "";
	private String password = "";
	
	private static MailClient instance;
	private static Object lock = new Object();
	
	/**
	 * Singleton
	 * @return instance of {@link MailClient}
	 */
	public static MailClient getInstance(){
		if(instance == null){
			synchronized (lock) {
				if(instance == null)
					instance = new MailClient();
			}
		}
		
		return instance;
	}
	
	/**
	 * Set the credentials.
	 * 
	 * @param username
	 * @param password
	 */
	public void setCredentials(String username, String password){
		this.username = username;
		this.password = password;
	}
	
	/**
	 * The the domain/host. It is also the smtp mail server and the host part of an email address.
	 * 
	 * @param host
	 */
	public void setHost(String host){
		this.host = host;
	}
	
	/**
	 * Send a rest password message, with a new generated password to a user.
	 * 
	 * @param recpt - Recipient of this message.
	 * @param newPassword - The new Password
	 */
	public void sendResetPasswordMessage(String recpt, String newPassword){
		Email email = new Email();
		email.setFromAddress(username, username+"@"+host);
		email.setSubject("New Password");
		email.addRecipient(recpt, recpt, RecipientType.TO);
		email.setText("Your new password is: "+newPassword+" ");
		new Mailer(host, 25, username, password).sendMail(email);
	}
	
}
