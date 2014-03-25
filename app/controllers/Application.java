package controllers;

import play.*;
import play.mvc.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

import models.*;
import models.NotificationHost.Notification;
import models.NotificationHost.Chat;

public class Application extends Controller {

    public static void index() {
        render();
    }
    
    public static void getN() throws InterruptedException, ExecutionException {
    	Object[] notifications = await(NotificationHost.get().stream.getNotifications());
    	renderJSON(notifications);
    }
    
    public static void addN() {
    	NotificationHost.get().stream.addEvent(new Notification("Notification at " + new Date().toString()));
    }
    
    public static void addC() {
    	NotificationHost.get().stream.addEvent(new Chat("Chat at " + new Date().toString()));
    }
}