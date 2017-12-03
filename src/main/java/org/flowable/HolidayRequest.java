package org.flowable;


import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HolidayRequest {

    /**
     * http://www.flowable.org/docs/userguide/index.html#sources
     *
     * 2.3. Building a command-line application
     *
     */

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
        Scanner scanner= new Scanner(System.in);  //java.util.Scanner
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


        /**
         * To get the actual task list, we create a TaskQuery through the TaskService and we configure the query to only
         * return the tasks for the managers group
         */
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        System.out.println("You have " + tasks.size() + " tasks:");
        for (int i=0; i<tasks.size(); i++) {
            System.out.println((i+1) + ") " + tasks.get(i).getName());
        }
        System.out.println("5 **************************************************************************");

        /**
         * Using the task identifier, we can now get the specific process instance variables and show on
         * the screen the actual request
         */
        System.out.println("Which task would you like to complete?");
        int taskIndex = Integer.valueOf("1"); //scanner.nextLine());
        Task task = tasks.get(taskIndex - 1);
        Map<String, Object> processVariables = taskService.getVariables(task.getId());
        System.out.println(processVariables.get("employee") + " wants " +
                processVariables.get("nrOfHolidays") + " of holidays. Do you approve this?");
        System.out.println("6 **************************************************************************");

        /**
         * The manager can now complete the task. In reality, this often means that a form is submitted by the user.
         * The data from the form is then passed as process variables. Here, we’ll mimic this by passing a map with
         * the approved variable (the name is important, as it’s used later on in the conditions of the sequence flow!)
         * when the task is completed
         */
        boolean approved = true; ///scanner.nextLine().toLowerCase().equals("y");
        variables = new HashMap<String, Object>();
        variables.put("approved", approved);
        taskService.complete(task.getId(), variables);

        /**
         * There is a last piece of the puzzle still missing: we haven’t implemented the automatic logic that will get
         * executed when the request is approved. In the BPMN 2.0 XML this is a service task and it looked above like:
         * <serviceTask id="externalSystemCall" name="Enter holidays in external system"
         *   flowable:class="org.flowable.CallExternalSystemDelegate"/>
         * Klasse: CallExternalSystemDelegate
         * @todo: Delegate Design Pattern?
         */

        /**
         * 2.3.7. Working with historical data
         * For example, suppose we want to show the duration of the process instance that we’ve been executing so far.
         * To do this, we get the HistoryService from the ProcessEngine and create a query for historical activities.
         * Running the example again, we now see something like this in the console:
         *   startEvent took 1 milliseconds
         *   approveTask took 2638 milliseconds
         *   decision took 3 milliseconds
         *   externalSystemCall took 1 milliseconds
         */
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> activities =
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .finished()
                        .orderByHistoricActivityInstanceEndTime().asc()
                        .list();

        for (HistoricActivityInstance activity : activities) {
            System.out.println(activity.getActivityId() + " took "
                    + activity.getDurationInMillis() + " milliseconds");
        }
        System.out.println("7 **************************************************************************");





    }

}