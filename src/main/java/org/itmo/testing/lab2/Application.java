package org.itmo.testing.lab2;

import io.javalin.Javalin;
import org.itmo.testing.lab2.controller.UserAnalyticsController;

public class Application {

  private static final int DEFAULT_PORT = 8080;

  public static void main(String[] args) {
    Javalin app = UserAnalyticsController.createApp();
    app.start(DEFAULT_PORT);
  }
}
