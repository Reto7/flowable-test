package org.flowable;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;

public class HolidayRequest {

    public static void main(String[] args) {

        /**
         * The first thing we need to do is to instantiate a ProcessEngine instance. This is a thread-safe object that
         * you typically have to instantiate only once in an application. A ProcessEngine is created from a
         * ProcessEngineConfiguration instance, which allows you to configure and tweak the settings for the process engine.
         * Often, the ProcessEngineConfiguration is created using a configuration XML file, but (as we do here) you can
         * also create it programmatically. The minimum configuration a ProcessEngineConfiguration needs is a JDBC connection
         * to a database
         */
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngine processEngine = cfg.buildProcessEngine();
    }

}