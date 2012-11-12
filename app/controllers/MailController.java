package controllers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.joda.time.DateTime;

import models.Domain;
import models.FormMail;
import models.Mail;
import models.User;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.common.base.Optional;
import common.MailServerUserManagment;
import common.TimeUtils;

/**
 * This class provides method to control the mail page.
 * 
 * @author Eugen Meissner
 *
 */
public class MailController extends Controller{

	private static final int maxTry = 10;
	private static List<String> validTime = createValidationTime();
	
	public static Thread mailChecker = new Thread(new Runnable() {
		
		@Override
		@Transactional
		public void run() {
			checkValidMail();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	});
		
	/**
	 * Show the mail control page.
	 * 
	 * @return
	 */
	@Transactional
	public static Result show(){
		return sendOk(form(FormMail.class));
	}
	
	/**
	 * This method adds a new mail.
	 * 
	 * @return {@link Result}
	 */
	@Transactional
	public static Result add(){
		String userId = session().get("id");
		if(userId == null) return redirect(routes.Application.index());
		Optional<User> oUser = User.findById(Long.valueOf(session().get("id")));
		if(!oUser.isPresent()){
			flash("error", "Internal server error!");
			return UserController.logout();
		}
		
		Form<FormMail> mailForm = form(FormMail.class).bindFromRequest();
		
		if(mailForm.data().containsKey("generate")){
			return generateUserAddress(mailForm);
		} else if(mailForm.data().containsKey("create")){
			return createUser(mailForm);
		} else {
			return badRequest();
		}
	}
		
	/**
	 * This method removes a mail.
	 * 
	 * @param mailId - the mail id
	 * @return {@link Result}
	 */
	@Transactional
	public static Result remove(Long mailId){
		String userId = session().get("id");
		if(userId == null) return redirect(routes.Application.index());
		
		Mail mail = Mail.getMail(mailId);
		
		if(mail != null){
			mail.delete();
		}
		
		return sendOk(form(FormMail.class));
	}
	
	/**
	 * This method updates the user part of a mail.
	 * 
	 * @param mailId - the mail id
	 * @return {@link Result}
	 */
	@Transactional
	public static Result updateName(Long mailId){
		String sUserId = session().get("id");
		if(sUserId == null) return redirect(routes.Application.index());
		
		Long userId = Long.valueOf(sUserId);	
		Form<String> form = form(String.class).bindFromRequest();
		
		Object user = form.data().get("user");
		Mail mail = Mail.getMail(mailId);
		
		if(user != null && mail.user.id==userId){
			if(Mail.isMail(user.toString(), mail.domain.name)) {
				flash("info", "Address already exists!");
				return badRequest(form);
	        }
			
			if(form.hasErrors()){
				return badRequest(form);
			}
			
			mail.address = user.toString();
			mail.save();
			return redirect(
					routes.MailController.show()
	            );
		} else {
			List<Mail> mails = Mail.getMails(Long.valueOf(userId));
			List<Domain> domains = Domain.getDomains();
			return badRequest(views.html.mail.render(form(FormMail.class),mails,domains,validTime));
		}
	}
	
	/**
	 * This method updates the expiration time.
	 * 
	 * @param mailId - the mail id
	 * @return {@link Result}
	 */
	@Transactional
	public static Result updateExpTime(Long mailId){
		String sUserId = session().get("id");
		if(sUserId == null) return redirect(routes.Application.index());
		Long userId = Long.valueOf(sUserId);	
		
		Form<String> form = form(String.class).bindFromRequest();		
		Object expires = form.data().get("expires");
		Mail mail = Mail.getMail(mailId);
		Optional<Timestamp> ots = TimeUtils.getTimeStamp(expires.toString());
				
		if(ots.isPresent() && mail.user.id==userId){
			mail.expires = ots.get();
			mail.save();
			return redirect(
					routes.MailController.show()
	            );
		} else {
			List<Mail> mails = Mail.getMails(userId);
			List<Domain> domains = Domain.getDomains();
			return badRequest(views.html.mail.render(form(FormMail.class),mails,domains,validTime));
		}
	}
	
	/**
	 * This method generates a valid user address.
	 * 
	 * @param mailForm
	 * @param userId
	 * @return
	 */
	private static Result generateUserAddress(Form<FormMail> mailForm){
		String host = mailForm.data().get("domain.name");		
		String userAddress = "";
		
		int i = 0;
		
		while(true){
			userAddress = "m"+(int)(Math.random()*10000);
			if(!Mail.isMail(userAddress, host) || i>=maxTry){
				break;
			} else {
				i++;
			}
		}
		
		FormMail mail = new FormMail();
		mail.user = userAddress;
		mail.host = host;
		
		return sendOk(mailForm.fill(mail));
	}
	
	/**
	 * This method performs the user creation. Please check in caller method that the user exists.
	 * 
	 * @param formMail
	 * @return
	 */
	private static Result createUser(Form<FormMail> formMail){
		if(formMail.get() == null || formMail.get().user == null || formMail.get().host == null){
			flash("info", "Address is empty!");
			return badRequest(formMail);
		} else if(formMail.get().host.trim().isEmpty() || formMail.get().user.trim().isEmpty()){
			flash("info", "Address is empty!");
			return badRequest(formMail);
		} else if(formMail.get().user.contains(" ")){
			flash("info", "Unvailed Address Format!");
			return badRequest(formMail);
		} else {
			if(Mail.isMail(formMail.get().user, formMail.get().host)){
				flash("info", "Address already exists!");
				return badRequest(formMail);
			}
		}
		
		FormMail mf = formMail.get();
		
		Optional<User> oUser = User.findById(Long.valueOf(session().get("id")));
		Optional<Timestamp> ts = Optional.absent();
		
		if (!formMail.hasErrors() && (ts = TimeUtils.getTimeStamp(mf.expires)).isPresent()) {
			Mail mail = new Mail(mf.user, mf.host, ts.get(), oUser.get());
			mail.save();
			
			MailServerUserManagment
				.getInstance()
				.addUserWithForwarding(mail.address, mail.domain.name, oUser.get().password, oUser.get().email);
			return sendOk(formMail);
		} else {
			List<Mail> mails = Mail.getMails(oUser.get().id);
			List<Domain> domains = Domain.getDomains();
			return badRequest(views.html.mail.render(formMail,mails,domains,validTime));
		}
	}
	
	/**
	 * This method generates the validation time list.
	 * 
	 * @return {@link List} with valid times.
	 */
	private static List<String> createValidationTime(){
		List<String> validTime = new ArrayList<String>();
		validTime.add("unlimited");
		
		for(int i=1; i<=24; i++)
			validTime.add(i+"h");
		for(int i=1; i<=30; i++)
			validTime.add(i+"d");
		
		return validTime;
	}
	
	/**
	 * This method loads the available domains, users mails and produces a ok request.
	 * 
	 * @param form - the form to show
	 * @return {@link Result}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Result sendOk(Form form){
		List<Mail> mails = Mail.getMails(Long.valueOf(session().get("id")));
		List<Domain> domains = Domain.getDomains();
		
		return ok(views.html.mail.render(form,mails,domains,validTime));
	}
	
	/**
	 * This method loads the available domains, users mails and produces a bad request.
	 * 
	 * @param form - the form to show
	 * @return {@link Result}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Result badRequest(Form form){
		List<Mail> mails = Mail.getMails(Long.valueOf(session().get("id")));
		List<Domain> domains = Domain.getDomains();
		
		return badRequest(views.html.mail.render(form,mails,domains,validTime));
	}
	
	/**
	 * 
	 */
	@Transactional
	public static void checkValidMail(){
		EntityManager em = Persistence.createEntityManagerFactory("defaultPersistenceUnit").createEntityManager();
		JPA.bindForCurrentThread(em);
		em.getTransaction().begin();
		
		List<Mail> mails = Mail.getMails();	
		Timestamp now = new Timestamp(DateTime.now().getMillis());
		for(Mail mail : mails){
			if(mail.expires.before(now) && !mail.expires.equals(new Timestamp(0)))
				mail.delete();
		}
		
		em.getTransaction().commit();
	}
	
}
