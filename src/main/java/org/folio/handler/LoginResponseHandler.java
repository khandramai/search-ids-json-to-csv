package org.folio.handler;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.folio.exception.RequestExecutionException;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.String.format;

@Log4j2
public class LoginResponseHandler implements ResponseHandler<String> {

    private final long start;

    public LoginResponseHandler() {
        this.start = System.currentTimeMillis();
    }

    @Override
    public String handleResponse(HttpResponse httpResponse) throws IOException {
        log.info(format("Login response received in: %s ms", System.currentTimeMillis() - start));
        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
            throw new RequestExecutionException(new String(httpResponse.getEntity().getContent().readAllBytes()));
        }
        return Arrays.stream(httpResponse.getHeaders("x-okapi-token")).toList().get(0).getValue();
    }
}
