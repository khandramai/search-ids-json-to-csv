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
import static org.folio.utils.Constants.CSV_FILE_EXTENSION;
import static org.folio.utils.Constants.IDS_PREFIX;

@Log4j2
public class RetrieveJobIdsHandler implements ResponseHandler<String> {

    private long start;

    public RetrieveJobIdsHandler() throws IOException {
        Files.deleteIfExists(Path.of("ids_job.json"));
    }

    @Override
    public String handleResponse(HttpResponse httpResponse) throws IOException {

        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new RequestExecutionException(new String(httpResponse.getEntity().getContent().readAllBytes()));
        }

        try (var is = httpResponse.getEntity().getContent()) {
            start = System.currentTimeMillis();
            FileUtils.copyInputStreamToFile(is, new File("ids_job.json"));
            log.info(format("Ids (job) are saved in: %s ms", System.currentTimeMillis() - start));
        }

        return null;
    }

}
