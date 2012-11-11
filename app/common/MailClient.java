package common;

import javax.mail.Message.RecipientType;

import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.Mailer;

public class MailClient {
	
	private final String FROM_NAME = "forgotpassword";
	private final String FROM_ADDRESS = "forgotpassword@localhost";
	
	private static MailClient instance;
	private static Object lock = new Object();
	
	public static MailClient getInstance(){
		if(instance == null){
			synchronized (lock) {
				if(instance == null)
					instance = new MailClient();
			}
		}
		
		return instance;
	}
	
	public void sendResetPasswordMessage(String recpt, String newPassword){
		Email email = new Email();
		email.setFromAddress(FROM_NAME, FROM_ADDRESS);
		email.setSubject("New Password");
		email.addRecipient("",recpt, RecipientType.TO);
		email.setText("Your new password is:"+newPassword);
		new Mailer("localhost", 25, "forgotpassword", "forgotpassword").sendMail(email);
	}
	
}
