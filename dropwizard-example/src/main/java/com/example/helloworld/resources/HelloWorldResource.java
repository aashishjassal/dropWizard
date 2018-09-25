package com.example.helloworld.resources;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.joda.time.LocalDate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.example.helloworld.api.Saying;
import com.example.helloworld.core.Template;

import io.dropwizard.jersey.caching.CacheControl;
import io.dropwizard.jersey.params.DateTimeParam;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldResource.class);

    private final Template template;

    private final AtomicLong counter;

    public HelloWorldResource(Template template) {
        this.template = template;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed(name = "get-requests")
    @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
    public Saying sayHello(@QueryParam("name") Optional<String> name) {
        return new Saying(counter.incrementAndGet(), template.render(name));
    }

    @GET
    @Path("other")
    public String sayHelloUsingOther() {
        Client client = ClientBuilder.newClient();
        String result = client.target("http://localhost:8090/hello-world").request().get(String.class);
        return result;
    }

    @GET
    @Path("checkout")
    public String checkout() {
        Client client = ClientBuilder.newClient();
        System.out.println("aashish");
        String result = "";
        int page = 1;
        boolean process = false;
        List<String> repos = new ArrayList<>();
        List<String> names = new ArrayList<>();
        do {
            result = client.target("http://iondelvm107.iontrading.com:2080/api/v3/projects?page=" + page
                    + "&per_page=100&private_token=hxTkrSbrbqWZyjT_tVYM").request().get(String.class);

            process = false;
            JSONParser parser = new JSONParser();
            try {
                JSONArray jsonArray = (JSONArray) parser.parse(result);
                System.out.println("parsing done");

                for (Object l : jsonArray) {
                    process = true;
                    JSONObject obj = (JSONObject) l;
                    System.out.println(obj.get("http_url_to_repo"));
                    repos.add((String) obj.get("http_url_to_repo"));
                    names.add((String) obj.get("name"));
                }
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println(page + " done");
            page++;

        } while (process);

        System.out.println("Now clone");
        String cloneDirectoryPath = "D:\\Test\\"; // Ex.in windows c:\\gitProjects\SpringBootMongoDbCRUD\
        for (int i = 0; i < repos.size(); i++) {
            String repoUrl = repos.get(i);
            String name = names.get(i);
            try {
                System.out.println("Cloning " + repoUrl + " into " + repoUrl);
                CloneCommand cloneCommand = Git.cloneRepository();
                cloneCommand.setURI(repoUrl);
                cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("ajassal", "git@team1"))
                        .setDirectory(Paths.get(cloneDirectoryPath + name).toFile()).call();
                System.out.println("Completed Cloning");
            } catch (Exception e) {
                System.out.println("Exception occurred while cloning repo");
                e.printStackTrace();
            }

        }

        // for (String repoUrl : repos) {
        // try {
        // System.out.println("Cloning " + repoUrl + " into " + repoUrl);
        // Git.cloneRepository().setURI(repoUrl).setDirectory(Paths.get(cloneDirectoryPath).toFile()).call();
        // System.out.println("Completed Cloning");
        // } catch (GitAPIException e) {
        // System.out.println("Exception occurred while cloning repo");
        // e.printStackTrace();
        // }
        //
        // }
        return result;
    }
    // ajassal:hxTkrSbrbqWZyjT_tVYM

    @POST
    public Saying receiveHello(@Valid Saying saying) {
        LOGGER.info("Received a saying: {}", saying);
        return saying;
    }

    @GET
    @Path("/date")
    @Produces(MediaType.TEXT_PLAIN)
    public String receiveDate(@QueryParam("date") Optional<DateTimeParam> dateTimeParam) {
        if (dateTimeParam.isPresent()) {
            final DateTimeParam actualDateTimeParam = dateTimeParam.get();
            LOGGER.info("Received a date: {}", actualDateTimeParam);
            return actualDateTimeParam.get().toString();
        } else {
            LOGGER.warn("No received date");
            return new LocalDate().toString();
        }
    }
}
