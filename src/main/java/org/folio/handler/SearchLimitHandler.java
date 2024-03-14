package org.folio.handler;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.folio.exception.RequestExecutionException;
import org.folio.utils.Constants;
import org.json.JSONObject;

import java.io.IOException;

import static java.lang.String.format;

@Log4j2
public class SearchLimitHandler implements ResponseHandler<Integer> {

    private final long start;

    public SearchLimitHandler() {
        this.start = System.currentTimeMillis();
    }
    @Override
    public Integer handleResponse(HttpResponse httpResponse) throws IOException {
        log.info(format("Instances number is retrieved in: %s ms", System.currentTimeMillis() - start));
        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new RequestExecutionException(new String(httpResponse.getEntity().getContent().readAllBytes()));
        }
        return new JSONObject(new String(httpResponse.getEntity().getContent().readAllBytes())).getInt(Constants.TOTAL_RECORDS);
    }
}
