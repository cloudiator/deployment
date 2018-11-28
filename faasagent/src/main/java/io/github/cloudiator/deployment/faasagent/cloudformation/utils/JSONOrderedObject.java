package io.github.cloudiator.deployment.faasagent.cloudformation.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class JSONOrderedObject extends JSONObject {

  private Set<String> keys = new LinkedHashSet<>();

  @Override
  public JSONObject put(String key, Object value) throws JSONException {
    this.keys.add(key);
    return super.put(key, value);
  }

  @Override
  protected Set<Map.Entry<String, Object>> entrySet() {
    Map<String, Map.Entry<String, Object>> entries = new HashMap<>();
    for (Map.Entry<String, Object> entry : super.entrySet()) {
      entries.put(entry.getKey(), entry);
    }
    Set<Map.Entry<String, Object>> orderedEntries = new LinkedHashSet<>();
    for (String key : keys) {
      orderedEntries.add(entries.get(key));
    }
    return orderedEntries;
  }

  @Override
  public Object remove(String key) {
    keys.remove(key);
    return super.remove(key);
  }

}
