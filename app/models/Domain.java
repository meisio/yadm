package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.data.validation.Constraints.Required;
import play.db.jpa.JPA;

@Entity
public class Domain {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Long id;
	
	@Required
	public String name;
	
	public Domain(){
		
	}
	
	public Domain(String name){
		this.name = name;
	}
	
	public void save(){
		JPA.em().persist(this);
	}
	
	public static List<Domain> getDomains(){
		return JPA.em()
				.createQuery( "from Domain d" )
		   		.getResultList();
	}

	public static Domain findByName(String name) {
		return (Domain)JPA.em()
				.createQuery( "from Domain d where d.name='"+name+"'" )
		   		.getSingleResult();
	}
}
