package com.bimport.ashrae.common;

import com.google.gson.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JsonUtil {
    private static final Gson gson = new Gson();

    public static <T> T clone(T jo, Class<T> cls) {
        return gson.fromJson(gson.toJson(jo, cls), cls);
    }

    public static JsonArray readJsonArray(JsonElement je, String key) {
        if (je == null || je.isJsonNull()) {
            return null;
        }

        try {
            JsonObject jo = je.getAsJsonObject();
            if (jo.has(key) && !jo.get(key).isJsonNull()) {
                return jo.get(key).getAsJsonArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JsonObject readJsonObject(JsonElement je, String key, boolean isDefaultNull) {
        if (je == null || je.isJsonNull()) {
            return isDefaultNull ? null : new JsonObject();
        }

        try {
            JsonObject jo = je.getAsJsonObject();
            if (jo.has(key) && !jo.get(key).isJsonNull()) {
                return jo.get(key).getAsJsonObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isDefaultNull ? null : new JsonObject();
    }

    public static boolean readBoolean(JsonElement je, String key) {
        if (je == null || je.isJsonNull()) {
            return false;
        }

        try {
            JsonObject jo = je.getAsJsonObject();
            if (jo.has(key) && !jo.get(key).isJsonNull()) {
                return jo.get(key).getAsBoolean();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static long readLongValue(JsonElement je, String key, long defaultVal) {
        if (je == null || je.isJsonNull()) {
            return defaultVal;
        }

        try {
            JsonObject jo = je.getAsJsonObject();
            if (jo.has(key) && !jo.get(key).isJsonNull()) {
                return jo.get(key).getAsLong();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultVal;
    }

    public static String readStringValue(JsonElement je, String key, String defaultVal) {
        if (je == null || je.isJsonNull()) {
            return defaultVal;
        }

        try {
            JsonObject jo = je.getAsJsonObject();
            if (jo.has(key) && !jo.get(key).isJsonNull()) {
                return jo.get(key).getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultVal;
    }

    public static JsonObject mapToJson(Map<String, JsonObject> map) {
        JsonObject jo = new JsonObject();
        map.forEach(jo::add);
        return jo;
    }

    public static Map<String, JsonObject> jsonToMap(JsonObject jo) {
        Map<String, JsonObject> map = new HashMap<>();

        Set<Map.Entry<String, JsonElement>> data = jo.entrySet();
        for (Map.Entry<String, JsonElement> ele : data) {
            map.put(ele.getKey(), ele.getValue().getAsJsonObject());
        }

        return map;
    }

    public static JsonObject readJsonArray(JsonArray ja, int idx, boolean isDefautNull) {
        if (ja != null && ja.size() > idx) {
            JsonElement je = ja.get(idx);
            if (je != null && !je.isJsonNull() && je.isJsonObject()) {
                return je.getAsJsonObject();
            }
        }

        if (isDefautNull) {
            return null;
        }
        return new JsonObject();
    }

    public static void readResultSet(JsonObject res, ResultSet rs) {
        try {
            ResultSetMetaData meta = rs.getMetaData();
            int columns = meta.getColumnCount();
            for (int i = 1; i <= columns; i++) {
                String colLabel = meta.getColumnLabel(i);
                res.addProperty(colLabel, rs.getString(colLabel));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static JsonElement convertToJsonElement(String str) {
        JsonParser jp = new JsonParser();
        return jp.parse(str);
    }

    public static JsonArray getAsJsonArray(String str) {
        JsonElement je = convertToJsonElement(StringUtil.checkNullAndEmpty(str, "[]"));
        return je.getAsJsonArray();
    }

    public static JsonObject parseToJsonObject(String string) {
        JsonParser jp = new JsonParser();
        try {
            JsonElement je = jp.parse(string);
            if (je != null) {
                return je.getAsJsonObject();
            }
        } catch (JsonSyntaxException ignored) {
        }
        return null;
    }

    public static void checkNullOrEmpty(JsonObject jo, String key, String defVal) {
        JsonElement je = jo.get(key);
        if (je.isJsonNull() || StringUtil.isNullOrEmpty(je.getAsString())) {
            jo.addProperty(key, defVal);
        }
    }

    public static double readDoubleValue(JsonElement je, String key, double defaultVal) {
        if (je == null || je.isJsonNull()) {
            return defaultVal;
        }

        try {
            JsonObject jo = je.getAsJsonObject();
            if (jo.has(key) && !jo.get(key).isJsonNull()) {
                return jo.get(key).getAsDouble();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultVal;
    }

    public static JsonArray parseToJsonArray(String string) {
        JsonParser jp = new JsonParser();
        try {
            JsonElement je = jp.parse(string);
            if (je != null) {
                return je.getAsJsonArray();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String toString(JsonElement jsonElement) {
        if (jsonElement == null) {
            return "";
        }
        return jsonElement.toString();
    }
}
