/*
 * @File: LOL2ImportApplication.java
 *
 * Copyright (c) 2013 test microtools.
 * Bahnhof.
 * All rights reserved.
 *
 * @Author: KostikX
 *
 * @Version $Revision: $Date: $
 *
 *
 */
package de.microtools.cs.lol.loader.delivery.boot;

import org.springframework.boot.Banner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import de.microtools.n5.infrastructure.batching.starter.spring.boot.BatchSpringBootApplication;

//  java -jar target\lol-loader-delivery-boot-1.0.0-SNAPSHOT.jar --spring.config.location=etc\lol-loader-delivery-boot.conf --jobNames=lolImport --jobParams=download=true
public class LolImportApplication extends BatchSpringBootApplication {

    public static void main(String[] args) {
        // run the application
        ConfigurableApplicationContext context
                = new SpringApplicationBuilder()
                .bannerMode(Banner.Mode.OFF)
                .sources(LolImportApplication.class)
                .run(args);

        registerStandaloneMode(context);
    }

    @Override
    public Health health() {
        return Health.up().withDetail("info", "LolImportApplication is up!").build();
    }

}
