package models;

public class FormAccount {

	public String firstname;
	public String lastname;
	public String email;
	public String oldpassword;
	public String newpassword;
	
	public void setUser(User user) {
		firstname = user.firstname;
		lastname = user.lastname;
		email = user.email;		
	}

}
