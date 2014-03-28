package controllers;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import models.EventHost;
import models.EventHost.Event;
import play.mvc.Controller;

public class Application extends Controller {

	public static void index() {
		render();
	}
	
    public static void prod() {
        render();
    }
    
    public static void cons() {
    	render();
    }
    
    public static void getMessage(Long last) throws InterruptedException, ExecutionException {
    	List messages = await(EventHost.get().nextMessages(last));
    	renderJSON(messages);
    }
    
    public static void addMessage() {
    	EventHost.get().say();
    }
}