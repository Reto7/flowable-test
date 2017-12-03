package org.flowable;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class HolidayRequest {

    public static void main(String[] args) {

        /**
         * The first thing we need to do is to instantiate a ProcessEngine instance. This is a thread-safe object that
         * you typically have to instantiate only once in an application. A ProcessEngine is created from a
         * ProcessEngineConfiguration instance, which allows you to configure and tweak the settings for the process engine.
         * Often, the ProcessEngineConfiguration is created using a configuration XML file, but (as we do here) you can
         * also create it programmatically. The minimum configuration a ProcessEngineConfiguration needs is a JDBC connection
         * to a database.
         */
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngine processEngine = cfg.buildProcessEngine();
        System.out.println("1 **************************************************************************");

        /**
         * Now we have the process BPMN 2.0 XML file
         * H:\DATEN\IntelliJ-Projekte\flowabletest\src\main\resources\holiday-request.bpmn20.xml
         *
         * we next need to deploy it to the engine. Deploying a process definition means that:
         * the process engine will store the XML file in the database, so it can be retrieved whenever needed
         * the process definition is parsed to an internal, executable object model, so that process instances can
         * be started from it.
         *
         * To deploy a process definition to the Flowable engine, the RepositoryService is used, which can be retrieved
         * from the ProcessEngine object. Using the RepositoryService, a new Deployment is created by passing the location
         * of the XML file and calling the deploy() method to actually execute it:
         */
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("holiday-request.bpmn20.xml")
                .deploy();
        System.out.println("2 **************************************************************************");

        /**
         * We can now verify that the process definition is known to the engine (and learn a bit about the API) by
         * querying it through the API. This is done by creating a new ProcessDefinitionQuery object through the RepositoryService.
         */
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        System.out.println("Found process definition : " + processDefinition.getName());
        System.out.println("3 **************************************************************************");

        /**
         * 2.3.3. Starting a process instance
         * We now have the process definition deployed to the process engine, so process instances can be started using
         * this process definition as a blueprint.
          */
        //Scanner scanner= new Scanner(System.in);  //java.util.Scanner
        System.out.println("Who are you?");
        String employee = "Reto"; //scanner.nextLine();
        System.out.println("How many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf("3"); //scanner.nextLine());
        System.out.println("Why do you need them?");
        String description = "Ferien"; //scanner.nextLine();

        /**
         * Next, we can start a process instance through the RuntimeService. The collected data is passed as a java.util.Map
         * instance, where the key is the identifier that will be used to retrieve the variables later on. The process instance
         * is started using a key. This key matches the id attribute that is set in the BPMN 2.0 XML file, in this case holidayRequest.
         * <process id="holidayRequest" name="Holiday Request" isExecutable="true">
         */
        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("holidayRequest", variables);
        System.out.println("4 **************************************************************************");
        /**
         * When the process instance is started, an execution is created and put in the start event. From there, this execution
         * follows the sequence flow to the user task for the manager approval and executes the user task behavior. This behavior
         * will create a task in the database that can be found using queries later on. A user task is a wait state and the engine
         * will stop executing anything further, returning the API call.
         */

    }

}