package org.folio.handler;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.folio.exception.RequestExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;

@Log4j2
public class RetrieveIdsHandler implements ResponseHandler<String> {

    public RetrieveIdsHandler() throws IOException {
        Files.deleteIfExists(Path.of("ids.json"));
    }

    @Override
    public String handleResponse(HttpResponse httpResponse) throws IOException {

        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new RequestExecutionException(new String(httpResponse.getEntity().getContent().readAllBytes()));
        }

        try (var is = httpResponse.getEntity().getContent()) {
            var start = System.currentTimeMillis();
            FileUtils.copyInputStreamToFile(is, new File("ids.json"));
            log.info(format("Ids (query) are saved in: %s ms", System.currentTimeMillis() - start));
        }
        return null;
    }

}
