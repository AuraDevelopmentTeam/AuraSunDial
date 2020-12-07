package dev.aura.sundial.config;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.junit.Test;

public class ConfigMigrationsTest {
  private static BufferedReader getResourceReader(String resourcePath) {
    return new BufferedReader(
        new InputStreamReader(
            ConfigMigrationsTest.class.getResourceAsStream("/migration/" + resourcePath),
            StandardCharsets.UTF_8));
  }

  private static HoconConfigurationLoader getLoaderFromResource(
      String resourcePath, BufferedWriter sink) {
    return HoconConfigurationLoader.builder()
        .setSource(() -> getResourceReader("in/" + resourcePath))
        .setSink(() -> sink)
        .build();
  }

  private static void assertConfigMatch(String resourcePath, StringWriter sink) {
    final String result = sink.toString();
    final String expected =
        getResourceReader("out/" + resourcePath).lines().collect(Collectors.joining("\n")) + '\n';

    assertEquals(expected, result);
  }

  private static void migrationTest(String testName) throws IOException {
    final String testFile = testName + ".conf";
    final StringWriter sink = new StringWriter();
    final HoconConfigurationLoader loader =
        getLoaderFromResource(testFile, new BufferedWriter(sink));

    ConfigurationNode node = loader.load();
    node = ConfigMigrations.migrateConfig(node);
    loader.save(node);

    assertConfigMatch(testFile, sink);
  }

  @Test
  public void initialToV1_all_Test() throws IOException {
    migrationTest("initialToV1_all");
  }

  @Test
  public void initialToV1_missing_Test() throws IOException {
    migrationTest("initialToV1_missing");
  }

  @Test
  public void initialToV1_upToDate_Test() throws IOException {
    migrationTest("initialToV1_upToDate");
  }
}
