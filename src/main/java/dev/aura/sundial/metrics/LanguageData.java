package dev.aura.sundial.metrics;

import com.google.common.annotations.VisibleForTesting;
import dev.aura.sundial.AuraSunDial;
import java.util.Locale;
import java.util.MissingResourceException;
import org.bstats.sponge.Metrics2.SimplePie;

public class LanguageData extends SimplePie {
  public LanguageData() {
    super("languages", LanguageData::getLanguage);
  }

  private static String getLanguage() {
    final String configLanguage = AuraSunDial.getConfig().getGeneral().getLanguage();

    return isValidLanguage(configLanguage) ? configLanguage : "custom";
  }

  @VisibleForTesting
  static boolean isValidLanguage(String lang) {
    String[] parts = lang.split("_", 3);

    if (parts.length != 2) return false;

    try {
      final Locale loc = new Locale(parts[0], parts[1]);

      return (loc.getISO3Language() != null) && (loc.getISO3Country() != null);
    } catch (MissingResourceException e) {
      return false;
    }
  }
}
