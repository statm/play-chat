package controllers;

import java.util.List;

import models.MessageHost;
import models.MessageHost.Notification;
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

	public static void getMessage(Long last) {
		List messages = await(MessageHost.getMessageStream("test").getMessages(last));
		renderJSON(messages);
	}

	public static void addMessage() {
		Notification n = new Notification();
		MessageHost.getMessageStream("test").addMessage(n);
	}
}