package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;

import common.MailServerUserManagment;

@Entity
public class Mail {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Long id;
	
	@Required
	public String address;
	
	@Required
	public Timestamp expires;
	
	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name="USERID")
	public User user;
	
	@OneToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name="DOMAINID")
	public Domain domain;
	
	public void save(){
		if(domain == null || user == null || address == null || expires == null) return;
		JPA.em().persist(this);
	}
	
	public Mail(){
		
	}
	
	public Mail(String tmpMailUser, String host, Timestamp expires, User user){
		this.address = tmpMailUser;
		this.domain = Domain.findByName(host);
		this.expires = expires;
		this.user = user;
	}
	
	public void delete(){
		JPA.em().remove(this);
		MailServerUserManagment.getInstance().removeUser(address, domain.name);
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean isMail(String name, String host){
		List res = JPA.em()
			.createQuery( "from Mail m where m.address='"+name+"' and m.domain.name='"+host+"'" )
	   		.getResultList();
		return (res.size()>0);
	}
	
	@SuppressWarnings("unchecked")
	public static List<Mail> getMails(Long userId){
		return JPA.em()
				.createQuery( "from Mail m where m.user.id="+userId )
		   		.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public static Mail getMail(Long mailId){
		List<Mail> mails = JPA.em()
				.createQuery( "from Mail m where m.id="+mailId )
		   		.getResultList();
		
		if(mails.size() == 1){
			return mails.get(0);
		} else {
			return null;
		}
	}

	/**
	 * TODO: Get unvalied mails from sql query.
	 * @return 
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Mail> getMails() {
		return JPA.em("default")
				.createQuery( "from Mail m " )
		   		.getResultList();
	}
}
