package ai.apps.mahefa.conf;

import ai.apps.mahefa.PojaGenerated;
import org.springframework.test.context.DynamicPropertyRegistry;

@PojaGenerated
public class EmailConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("aws.ses.source", () -> "dummy-ses-source");

    System.setProperty("aws.ses.source", "dummy-ses-source");
  }
}
