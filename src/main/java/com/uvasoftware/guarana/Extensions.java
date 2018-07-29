package com.uvasoftware.guarana;

import java.util.HashMap;
import java.util.Map;

/**
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types
 */
class Extensions {
  private static Map<String, String> extension = new HashMap<>();

  static {
    extension.put("application/javascript", ".js");
    extension.put("application/json", ".json");
    extension.put("application/xml", ".xml");
    extension.put("text/html", ".html");
  }

  static String resolveOrDefault(String mimeType) {
    String[] parts = mimeType.split(";");
    if (extension.containsKey(parts[0])) {
      return extension.get(parts[0]);
    }
    return ".txt";
  }
}
