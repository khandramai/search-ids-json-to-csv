package org.folio.handler;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.folio.exception.RequestExecutionException;
import org.folio.utils.Constants;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.folio.utils.Constants.CSV_FILE_EXTENSION;
import static org.folio.utils.Constants.IDS_PREFIX;
import static org.folio.utils.Constants.INSTANCES_PREFIX;
import static org.folio.utils.Constants.JSON_FILE_EXTENSION;

@Log4j2
public class SaveIdsHandler implements ResponseHandler<String> {

    private final List<Integer> sizes;
    private long start;

    public SaveIdsHandler(List<Integer> sizes) {
        this.sizes = sizes;
        start = System.currentTimeMillis();
    }

    @Override
    public String handleResponse(HttpResponse httpResponse) throws IOException {

        log.info(format("Ids are retrieved in: %s ms", System.currentTimeMillis() - start));

        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new RequestExecutionException(new String(httpResponse.getEntity().getContent().readAllBytes()));
        }

        start = System.currentTimeMillis();
        var array = new JSONObject(new String(httpResponse.getEntity().getContent().readAllBytes())).getJSONArray(Constants.IDS).toList();
        Map<Integer, BufferedWriter> writers = new HashMap<>();

        for (var size : sizes) {
            Files.deleteIfExists(Path.of(INSTANCES_PREFIX + size + JSON_FILE_EXTENSION));
            writers.put(size, new BufferedWriter(new FileWriter(IDS_PREFIX + size + CSV_FILE_EXTENSION, true)));
        }

        for (int i = 0; i < array.size(); i++) {
            for (var writer : writers.entrySet()) {
                if (i < writer.getKey()) {
                    writer.getValue().append("\"").append(String.valueOf(((HashMap<?, ?>) array.get(i)).get(Constants.ID))).append("\"\n");
                }
            }
        }

        for (var writer : writers.entrySet()) {
            writer.getValue().close();
        }

        log.info(format("Ids are saved in: %s ms", System.currentTimeMillis() - start));
        return null;
    }

}
