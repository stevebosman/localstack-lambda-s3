package uk.co.stevebosman.bucketlambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.TreeMap;
import java.util.Map;

// Handler value: example.Handler
public class Handler implements RequestHandler<Map<String, String>, Map<String, Object>> {
  private static final Logger logger = LoggerFactory.getLogger(Handler.class);

  @Override
  public Map<String, Object> handleRequest(final Map<String, String> input, final Context context) {
    logger.info("Processing: {}", input);
    final Map<String, Object> result = new TreeMap<>();
    result.put("time", LocalDateTime.now().toString());
    result.put("input", input);
    return result;
  }

}