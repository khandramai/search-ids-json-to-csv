package org.folio.handler;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.folio.exception.RequestExecutionException;
import org.json.JSONObject;

import java.io.IOException;

import static java.lang.String.format;
import static org.folio.utils.Constants.ID;

@Log4j2
public class SearchJobHandler implements ResponseHandler<String> {

    private final long start;

    public SearchJobHandler() {
        this.start = System.currentTimeMillis();
    }

    @Override
    public String handleResponse(HttpResponse httpResponse) throws IOException {
        var jobId = new JSONObject(new String(httpResponse.getEntity().getContent().readAllBytes())).getString(ID);
        log.info(format("Search job id=%s created in: %s ms", jobId, System.currentTimeMillis() - start));
        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new RequestExecutionException(new String(httpResponse.getEntity().getContent().readAllBytes()));
        }
        return jobId;
    }
}
