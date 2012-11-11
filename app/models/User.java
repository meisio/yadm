package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;

import com.google.common.base.Optional;

/**
 * User Entity managed by JPA
 * 
 * @author Eugen Meissner
 *
 */
@Entity
public class User {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Long id;
	
	@Required
    public String firstname;
    
	@Required
    public String lastname;
	
    @Required
    public String email;
    
    @Required
    public String password;

    /**
     * 
     */
    public User(){
    	
    }
        
    /**
     * Create a new user.
     * 
     * @param fistname
     * @param lastname
     * @param email
     * @param password
     */
	public User(String fistname, String lastname, String email, String password) {
		super();
		this.firstname = fistname;
		this.lastname = lastname;
		this.email = email;
		this.password = password;
	}

	/**
	 * Save this user
	 */
	public void save(){
		JPA.em().persist(this);
	}
	
	/**
	 * Remove a User
	 */
	public void delete() {
		List<Mail> mails = Mail.getMails(id);
		for(Mail mail : mails)
			mail.delete();
		
		JPA.em().remove(this);
	}

	/**
	 * Find a @link User} by ID
	 * 
	 * @param id - the user id
	 * @return {@link User}
	 */
	public static Optional<User> findById(Long id) {
		User user = JPA.em().find(User.class, id);
		if(user != null)
			return Optional.of(user);
		else
			return Optional.absent();
	}
	
	/**
	 * 
	 * @param email
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Optional<User> findByEmail(String email){
		List<User> users = JPA.em()
			.createQuery("from User u where u.email='"+email+"'")
			.getResultList();
		
		if(users.size() == 1){
			return Optional.of(users.get(0));
		}
		
		return Optional.absent();
	}
	
	 /**
     * Authenticate a User.
     */
	@SuppressWarnings("unchecked")
    public static Optional<User> getAuthenticatedUser(String email, String password) {
		List<User> users = JPA.em()
    			.createQuery("from User u where u.email = '"+email+"' AND u.password='"+password+"'")
    			.getResultList();
		
		if(users.size() == 1){
			return Optional.of(users.get(0));
		}
		
		return Optional.absent();
    }
	
	@SuppressWarnings("unchecked")
	public static List<User> getAll(){
		return JPA.em()
    			.createQuery("from User u ")
    			.getResultList();
	}
	
}
