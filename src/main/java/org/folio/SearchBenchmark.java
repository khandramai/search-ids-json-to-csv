package org.folio;

import lombok.extern.log4j.Log4j2;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.awaitility.Awaitility;
import org.folio.exception.RequestExecutionException;
import org.folio.handler.LoginResponseHandler;
import org.folio.handler.RetrieveIdsHandler;
import org.folio.handler.RetrieveJobIdsHandler;
import org.folio.handler.SearchJobHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.folio.utils.Constants.CONTENT_TYPE;
import static org.folio.utils.Constants.X_OKAPI_TENANT;
import static org.folio.utils.Constants.X_OKAPI_TOKEN;

@Log4j2
public class SearchBenchmark {

    public static void main(String[] args) throws UnsupportedEncodingException {

        String token;
        String jobId;

        Map<String, String> properties = Arrays.stream(args).map(arg -> arg.split("=")).collect(toMap(k -> k[0], v -> v[1]));

        var host = properties.get("-host");
        var tenant = properties.get("-tenant");
        var username = properties.get("-username");
        var password = properties.get("-password");

        if (Objects.isNull(host) || Objects.isNull(tenant) || Objects.isNull(username) || Objects.isNull(password) ) {
            throw new RequestExecutionException("Invalid arguments line: " + String.join(" ", args));
        }

        final HttpPost loginRequest = new HttpPost(format("%s/bl-users/login?expandPermissions=true&fullPermissions=true", host));
        loginRequest.setHeader(X_OKAPI_TENANT, tenant);
        loginRequest.setHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType());
        loginRequest.setEntity(new StringEntity("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            token = client.execute(loginRequest, new LoginResponseHandler());
        } catch (IOException e) {
            throw new RequestExecutionException("Login error", e);
        }

        // // Retrieve ids by Search Job
        final HttpPost createSearchJob = new HttpPost(format("%s/search/resources/jobs", host));
        createSearchJob.setHeader(X_OKAPI_TENANT, tenant);
        createSearchJob.setHeader(X_OKAPI_TOKEN, token);
        createSearchJob.setHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType());
        createSearchJob.setEntity(new StringEntity("{\"entityType\":\"INSTANCE\",\"query\":\"id=*\"}"));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            jobId = client.execute(createSearchJob, new SearchJobHandler());
        } catch (IOException e) {
            throw new RequestExecutionException("Job creating error", e);
        }

        final HttpGet getSearchJob = new HttpGet(format("%s/search/resources/jobs/%s", host, jobId));
        getSearchJob.setHeader(X_OKAPI_TENANT, tenant);
        getSearchJob.setHeader(X_OKAPI_TOKEN, token);
        getSearchJob.setHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType());

        long start = System.currentTimeMillis();

        var reference = new AtomicReference<String>();
        Awaitility.await()
                .atMost(2, TimeUnit.HOURS)
                .until(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                return client.execute(getSearchJob, response -> {
                    var status = new JSONObject(new String(response.getEntity().getContent().readAllBytes())).getString("status");
                    reference.set(status);
                    return !reference.get().equals("IN_PROGRESS");
                });
            } catch (IOException e) {
                throw new RequestExecutionException("Retrieving instances ids job error", e);
            }
        });
        long stop = System.currentTimeMillis();
        log.info(format("Search job processed in: %s ms with status: %s", stop - start, reference.get()));

        final HttpGet idsByJobRequest = new HttpGet(format("%s/search/resources/jobs/%s/ids", host, jobId));
        idsByJobRequest.setHeader(X_OKAPI_TENANT, tenant);
        idsByJobRequest.setHeader(X_OKAPI_TOKEN, token);
        idsByJobRequest.setHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType());

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            client.execute(idsByJobRequest, new RetrieveJobIdsHandler());
        } catch (IOException e) {
            throw new RequestExecutionException("Retrieving instances ids job error", e);
        }

        // Retrieve ids by CQL query
        final HttpGet idsRequest = new HttpGet(format("%s/search/instances/ids?query=(id==*)", host));
        idsRequest.setHeader(X_OKAPI_TENANT, tenant);
        idsRequest.setHeader(X_OKAPI_TOKEN, token);
        idsRequest.setHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType());

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            client.execute(idsRequest, new RetrieveIdsHandler());
        } catch (IOException e) {
            throw new RequestExecutionException("Retrieving instances ids error", e);
        }
    }
}
