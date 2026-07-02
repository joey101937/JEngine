package GameDemo.RTSDemo.MapEditor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MapSerializer {

    public static void save(MapData data, File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"background\": \"").append(escape(data.background)).append("\",\n");
        sb.append("  \"objects\": [\n");
        for (int i = 0; i < data.objects.size(); i++) {
            PlacedObject obj = data.objects.get(i);
            sb.append("    {");
            sb.append("\"type\": \"").append(escape(obj.type)).append("\", ");
            sb.append("\"x\": ").append(obj.x).append(", ");
            sb.append("\"y\": ").append(obj.y).append(", ");
            sb.append("\"rotation\": ").append(obj.rotation).append(", ");
            sb.append("\"team\": ").append(obj.team).append(", ");
            sb.append("\"hpPercent\": ").append(obj.hpPercent);
            if (obj.zLayer != Integer.MIN_VALUE)
                sb.append(", \"zLayer\": ").append(obj.zLayer);
            EditorObjectType type = EditorObjectType.fromClassName(obj.type);
            if (type != null && type.hasSpawnPoint()) {
                sb.append(", \"spawnOffsetX\": ").append(obj.spawnOffsetX);
                sb.append(", \"spawnOffsetY\": ").append(obj.spawnOffsetY);
                sb.append(", \"spawnRotation\": ").append(obj.spawnRotation);
            }
            sb.append("}");
            if (i < data.objects.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(sb.toString());
        }
    }

    public static MapData load(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
        }
        return parse(sb.toString());
    }

    private static MapData parse(String json) {
        MapData data = new MapData();
        String bg = extractString(json, "background");
        if (bg != null) data.background = bg;

        int arrStart = json.indexOf("\"objects\"");
        if (arrStart < 0) return data;
        int bracketOpen = json.indexOf('[', arrStart);
        int bracketClose = matchingBracket(json, bracketOpen, '[', ']');
        if (bracketOpen < 0 || bracketClose < 0) return data;

        String arr = json.substring(bracketOpen + 1, bracketClose);
        for (String chunk : splitObjects(arr)) {
            String type = extractString(chunk, "type");
            if (type == null || type.isBlank()) continue;
            PlacedObject obj = new PlacedObject();
            obj.type = type;
            obj.x = (int) extractNumber(chunk, "x");
            obj.y = (int) extractNumber(chunk, "y");
            obj.rotation = extractNumber(chunk, "rotation");
            obj.team = (int) extractNumber(chunk, "team");
            // support both legacy "hp" (treat as percent) and new "hpPercent"
            double hp = extractNumber(chunk, "hpPercent");
            if (hp == 0) hp = extractNumber(chunk, "hp");
            obj.hpPercent = (int) Math.max(1, Math.min(100, hp == 0 ? 100 : hp));
            if (hasKey(chunk, "zLayer")) obj.zLayer = (int) extractNumber(chunk, "zLayer");
            // Spawn point fields default to the Key Building baseline when absent (older maps).
            if (hasKey(chunk, "spawnOffsetX"))  obj.spawnOffsetX  = (int) extractNumber(chunk, "spawnOffsetX");
            if (hasKey(chunk, "spawnOffsetY"))  obj.spawnOffsetY  = (int) extractNumber(chunk, "spawnOffsetY");
            if (hasKey(chunk, "spawnRotation")) obj.spawnRotation = extractNumber(chunk, "spawnRotation");
            data.objects.add(obj);
        }
        return data;
    }

    private static String extractString(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + key.length() + 2);
        if (colon < 0) return null;
        int q1 = json.indexOf('"', colon + 1);
        if (q1 < 0) return null;
        int q2 = q1 + 1;
        while (q2 < json.length() && json.charAt(q2) != '"') {
            if (json.charAt(q2) == '\\') q2++;
            q2++;
        }
        return json.substring(q1 + 1, q2).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static boolean hasKey(String json, String key) {
        return json.indexOf("\"" + key + "\"") >= 0;
    }

    private static double extractNumber(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return 0;
        int colon = json.indexOf(':', idx + key.length() + 2);
        if (colon < 0) return 0;
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-' || json.charAt(end) == 'E' || json.charAt(end) == 'e' || json.charAt(end) == '+')) end++;
        if (start >= end) return 0;
        try { return Double.parseDouble(json.substring(start, end)); } catch (NumberFormatException e) { return 0; }
    }

    private static int matchingBracket(String s, int start, char open, char close) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == open) depth++;
            else if (s.charAt(i) == close) { if (--depth == 0) return i; }
        }
        return -1;
    }

    private static List<String> splitObjects(String json) {
        List<String> out = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}') { if (--depth == 0 && start >= 0) { out.add(json.substring(start, i + 1)); start = -1; } }
        }
        return out;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
