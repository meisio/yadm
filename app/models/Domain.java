package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import common.MailServerUserManagment;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;

/**
 * This class represents the domain data.
 * 
 * @author Eugen Meissner
 *
 */
@Entity
public class Domain {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Long id;
	
	@Required
	public String name;
	
	/**
	 * Basic Constrcutor
	 */
	public Domain(){}
	
	/**
	 * Basic Constructor
	 * 
	 * @param name - the name of a domain/host
	 */
	public Domain(String name){
		this.name = name;
	}
	
	/**
	 * This method saves this domain in a persistence db.
	 */
	public void save(){
		JPA.em().persist(this);
		Logger.info("Domain "+name+" added.");
	}
	
	/**
	 * This method return the domains/hosts on which the mail server listen.
	 * 
	 * @return list of domains/hosts
	 */
	@SuppressWarnings("unchecked")
	public static List<Domain> getDomains(){
		List<Domain> domains = JPA.em()
				.createQuery( "from Domain d" )
		   		.getResultList();
		
		if(domains.isEmpty()){
			// TODO: Hmm, in {@link Global} it doesn't work... 
			List<String> dms = MailServerUserManagment.getInstance().getDomains();
			for(String domain : dms){
				Domain d = new Domain(domain);
				d.save();
			}
			
			domains = JPA.em()
					.createQuery( "from Domain d" )
			   		.getResultList();
		}
		
		return domains;
	}

	/**
	 * This method looks up for a domain, to a name.
	 * 
	 * @param name - name of the domain/host
	 * @return {@link Domain}
	 */
	public static Domain findByName(String name) {
		return (Domain)JPA.em()
				.createQuery( "from Domain d where d.name='"+name+"'" )
		   		.getSingleResult();
	}
}
