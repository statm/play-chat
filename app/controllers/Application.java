package controllers;

import java.util.List;

import models.MessageHost;
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
		List messages = await(MessageHost.get().getMessages(last));
		renderJSON(messages);
	}

	public static void addMessage() {
		MessageHost.get().addMessage();
	}
}