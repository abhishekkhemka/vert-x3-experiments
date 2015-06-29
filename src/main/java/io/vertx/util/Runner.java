package io.vertx.util;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.function.Consumer;

public class Runner {

  public static void run(String prefix, Class clazz) {
    run(prefix, clazz, new VertxOptions().setClustered(false), null);
  }

  public static void run(String prefix, Class clazz, VertxOptions options, DeploymentOptions deploymentOptions) {
    String cwd = prefix + clazz.getPackage().getName().replace(".", "/");
    String verticleID = clazz.getName();

    System.setProperty("vertx.cwd", cwd);
    Consumer<Vertx> runner = vertx -> {
      try {
        if (deploymentOptions != null) {
          vertx.deployVerticle(verticleID, deploymentOptions);
        } else {
          vertx.deployVerticle(verticleID);
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }
    };
    if (options.isClustered()) {
      Vertx.clusteredVertx(options, res -> {
        if (res.succeeded()) {
          Vertx vertx = res.result();
          runner.accept(vertx);
        } else {
          res.cause().printStackTrace();
        }
      });
    } else {
      Vertx vertx = Vertx.vertx(options);
      runner.accept(vertx);
    }
  }
}
