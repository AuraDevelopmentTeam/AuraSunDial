package dev.aura.sundial.message;

import dev.aura.lib.messagestranslator.Message;
import dev.aura.lib.messagestranslator.MessagesTranslator;
import dev.aura.sundial.AuraSunDial;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

@RequiredArgsConstructor
public enum PluginMessages implements Message {
  // Admin Messages
  ADMIN_RELOAD_SUCCESSFUL("reloadSuccessful"),
  ADMIN_RELOAD_NOT_SUCCESSFUL("reloadNotSuccessful");

  @Getter private final String stringPath;

  public Text getMessage() {
    return getMessage(null);
  }

  public Text getMessage(Map<String, String> replacements) {
    final String message = getMessageRaw(replacements);

    return TextSerializers.FORMATTING_CODE.deserialize(message);
  }

  public String getMessageRaw() {
    return getMessageRaw(null);
  }

  public String getMessageRaw(Map<String, String> replacements) {
    final MessagesTranslator translator = AuraSunDial.getTranslator();

    if (translator == null) {
      return getStringPath();
    } else {
      return translator.translateWithFallback(this, replacements);
    }
  }
}
