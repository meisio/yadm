package controllers;

import models.FormAccount;
import models.FormLogin;
import models.User;

import org.apache.commons.lang3.RandomStringUtils;

import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.common.base.Optional;
import common.MailClient;
import common.MailServerUserManagment;

/**
 * This class provides method to control the user page.
 * 
 * @author Eugen Meissner
 *
 */
public class UserController extends Controller{

	/**
	 * Show the user control page.
	 * 
	 * @return {@link Result}
	 */
	@Transactional
	public static Result show(){
		String sUserId = session().get("id");
		if(sUserId == null) return redirect(routes.Application.index());
		
		FormAccount form = new FormAccount();
		Optional<User> user =  User.findById(Long.valueOf(sUserId));
		if(user.isPresent()){
			form.setUser(user.get());
			return ok(views.html.account.render(form(FormAccount.class).fill(form)));
		}
		
		flash("error", "Can't show user account");
		return redirect(routes.Application.index());
	}
	
	/**
	 * Register a new user.
	 * 
	 * @return {@link Result}
	 */
	@Transactional
	public static Result register(){
		String sUserId = session().get("id");
		if(sUserId != null) return show();
		
		Form<User> registerForm = form(User.class).bindFromRequest();
		
		if(!registerForm.field("email").valueOr("").isEmpty()) {
            if(!registerForm.field("email").valueOr("").contains("@")) {
            	flash("error", "E-Mail Address is not valid!");
            	return badRequest(views.html.register.render(registerForm));
            }
        }
		
		Optional<User> user = User.findByEmail(registerForm.get().email);
		
		if(user.isPresent()){
			flash("info","E-Mail address already in use!");
			return badRequest(views.html.register.render(form(User.class).fill(registerForm.get())));
		} 
		
		if(!registerForm.field("password").valueOr("").isEmpty()) {
            if(!registerForm.field("password").valueOr("").equals(registerForm.field("passwordConfirm").value())) {
            	flash("error", "Passwords aren't equal!");
            	return badRequest(views.html.register.render(registerForm));
            }
        }
				
		if (registerForm.hasErrors()) {
			return badRequest(views.html.register.render(registerForm));
		}
		
	
		registerForm.get().save();
		flash("success", "Registration sucessful! Please login.");
		return redirect(routes.Application.login());
	}
	
	/**
	 * This method controls the user authentication.
	 * 
	 * @return {@link Result}
	 */
	@Transactional
    public static Result login() {
		String sUserId = session().get("id");
		if(sUserId != null) return redirect(routes.Application.index());
		
        Form<FormLogin> formLogin = form(FormLogin.class).bindFromRequest();

        if(formLogin.hasErrors()) {
            return badRequest(views.html.login.render(formLogin));
        } else {
        	Optional<User> user = User.getAuthenticatedUser(formLogin.get().username, formLogin.get().password);
            if(user.isPresent()){
	        	session("id", String.valueOf(user.get().id));
	            return redirect(
	                routes.Application.index()
	            );
            } else {
            	flash("error", "Username or Password is wrong!");
            	return badRequest(views.html.login.render(formLogin.fill(new FormLogin(formLogin.get().username))));
            }
        }
    }
	
	/**
	 * This method controls the user logout.
	 * 
	 * @return {@link Result}
	 */
	@Transactional
	public static Result logout(){
		session().clear();
        flash("success", "You've been logged out");
        return redirect(
        		routes.Application.login()
        );
	}
	
	/**
	 * This method unregisters a user.
	 * 
	 * @return {@link Result}
	 */
	@Transactional
	public static Result unregister(){
		String sUserId = session().get("id");
		if(sUserId == null) return redirect(routes.Application.index());
		
		Optional<User> user =  User.findById(Long.valueOf(sUserId));
		if(user.isPresent()){
			user.get().delete();
			flash("info", "You are now unregisterd!");
			return logout();
		}
		
		flash("error", "Can't unregister current user");
		return redirect(routes.Application.index());
	}
	
	/**
	 * This method updates the user data.
	 * 
	 * @return {@link Result}
	 */
	@Transactional
	public static Result update(){
		String sUserId = session().get("id");
		if(sUserId == null) return redirect(routes.Application.index());
		Long userId = Long.valueOf(sUserId);
		Optional<User> oUser = User.findById(userId);
		if(!oUser.isPresent()){
			flash("error", "Internal server error.");
			return redirect(routes.Application.index());
		}
		
		User user = oUser.get();
		Form<FormAccount> formAccount = form(FormAccount.class).bindFromRequest();
		boolean changed = false;
		
		FormAccount retFormAcc = new FormAccount();
		retFormAcc.email = formAccount.get().email;
		retFormAcc.firstname = user.firstname;
		retFormAcc.lastname = user.lastname; // TODO: strange behaivor, form resets
		retFormAcc.oldpassword = "";
		
		if(!formAccount.field("oldpassword").valueOr("").isEmpty()){
			if(!formAccount.field("oldpassword").valueOr("").equals(user.password)) {
				flash("error", "Current Password is wrong!");
            	return badRequest(views.html.account.render(formAccount.fill(retFormAcc)));
            }
		}
		
		if(!formAccount.field("newpassword").valueOr("").isEmpty()) {
            if(!formAccount.field("newpassword").valueOr("").equals(formAccount.field("passwordConfirm").value())) {
            	flash("error", "Passwords aren't equal!");
            	return badRequest(views.html.account.render(formAccount.fill(retFormAcc)));
            }
        }
		
		if (formAccount.hasErrors()) {
			return badRequest(views.html.account.render(formAccount));
		}
		
		if(!user.email.equals(formAccount.get().email)){
			MailServerUserManagment.getInstance().updateUserForwarding(user.email, formAccount.get().email);
			user.email = formAccount.get().email;
			changed = true;
		}
		
		if(user.password.equals(formAccount.get().oldpassword)){
			user.password = formAccount.get().newpassword;
			changed = true;
		}
		
		if(changed){
			user.save();
			flash("info", "Your data has been changed! Please login with your new E-Mail/Password!");
			return logout();
		}
				
		return show();
	}
	
	@Transactional
	public static Result resetPassword(){
		Form<String> form = form(String.class).bindFromRequest();
		String email = form.data().get("email");
		if(email == null){
			flash("error", "E-Mail address is empty");
			return badRequest(views.html.forgotpassword.render());
		} else {
			Optional<User> oUser = User.findByEmail(email);
			if(oUser.isPresent()){
				User user = oUser.get();
				user.password = RandomStringUtils.randomAlphabetic(10);
				user.save();
				MailClient.getInstance().sendResetPasswordMessage(user.email, user.password);
				flash("info", "Login with your new password.");
				return Application.login();
			} else {
				flash("error", "Wrong E-Mail address");
				return badRequest(views.html.forgotpassword.render());
			}
			
		}
		
	}
}

