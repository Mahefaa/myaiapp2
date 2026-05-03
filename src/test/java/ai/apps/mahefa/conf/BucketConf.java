package ai.apps.mahefa.conf;

import ai.apps.mahefa.PojaGenerated;
import org.springframework.test.context.DynamicPropertyRegistry;

@PojaGenerated
public class BucketConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("aws.s3.bucket", () -> "dummy-bucket");

    System.setProperty("aws.s3.bucket", "dummy-bucket");
  }
}
