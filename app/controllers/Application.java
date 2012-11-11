package controllers;


import models.FormLogin;
import models.User;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * This class provides common methods to control the view.
 * 
 * @author Eugen Meissner
 *
 */
public class Application extends Controller{

	/**
	 * This method renders the index page.
	 * 
	 * @return {@link Result}
	 */
	public static Result index() {
		return ok(views.html.index.render());
	}
	
	/**
	 * This method controls the user login.
	 * 
	 * @return {@link Result}
	 */
	public static Result login(){
		return ok(views.html.login.render(form(FormLogin.class)));
	}
		
	/**
	 * This method controls the user authentication.
	 * 
	 * @return {@link Result}
	 */
	public static Result forgotpassword(){
		return ok(views.html.forgotpassword.render());
	}
	
	/**
	 * This method renders the blank register view.
	 * @return {@link Result}
	 */
	@Transactional
	public static Result register(){
		String sUserId = session().get("id");
		if(sUserId != null) return redirect(routes.Application.index());
		
		return ok(views.html.register.render(form(User.class)));
	}
}
