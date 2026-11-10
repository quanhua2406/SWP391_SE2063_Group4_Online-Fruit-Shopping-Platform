import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Usage (from project root): java tools/ErdExporter.java
// Reads the SQL Server schema scripts and writes image/erd.png
public class ErdExporter {

    static class Column {
        String name, type;
        boolean pk, fk;
    }

    static class Table {
        String name;
        List<Column> cols = new ArrayList<>();
        Set<String> pkCols = new HashSet<>();
        int x, y, w, h;
    }

    record Fk(String from, String fromCol, String to) {}

    static final Pattern CREATE = Pattern.compile(
            "CREATE TABLE\\s+dbo\\.\\[?(\\w+)\\]?\\s*\\(([\\s\\S]*?)\\n\\s*\\);", Pattern.CASE_INSENSITIVE);
    static final Pattern ALTER_FK = Pattern.compile(
            "ALTER TABLE\\s+dbo\\.\\[?(\\w+)\\]?\\s+ADD\\s+(?:CONSTRAINT\\s+\\S+\\s+)?FOREIGN KEY\\s*\\(([^)]*)\\)\\s*REFERENCES\\s+dbo\\.\\[?(\\w+)\\]?",
            Pattern.CASE_INSENSITIVE);
    static final Pattern FK_IN = Pattern.compile(
            "FOREIGN KEY\\s*\\(([^)]*)\\)\\s*REFERENCES\\s+dbo\\.\\[?(\\w+)\\]?", Pattern.CASE_INSENSITIVE);
    static final Pattern PK = Pattern.compile("PRIMARY KEY\\s*\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE);
    static final Pattern COL = Pattern.compile("^\\[?(\\w+)\\]?\\s+(\\w+(?:\\([^)]*\\))?)");
    static final Set<String> KEYWORDS = Set.of("CONSTRAINT", "PRIMARY", "FOREIGN", "UNIQUE", "CHECK", "INDEX", "REFERENCES");

    static final int COLS = 6, BOX_W = 250, GAP_X = 60, GAP_Y = 50, HEAD = 26, ROW = 17, MARGIN = 30, TITLE = 60;

    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args.length > 0 ? args[0] : ".").toAbsolutePath();
        Path out = root.resolve("image").resolve("erd.png");
        List<Path> sqls = List.of(
                root.resolve("database/reference/OnlineFruitShopping_DB_FINAL_MERGED.sql"),
                root.resolve("database/module_delivery_extensions.sql"));

        Map<String, Table> tables = new LinkedHashMap<>();
        List<Fk> fks = new ArrayList<>();

        for (Path p : sqls) {
            String text = Files.readString(p, StandardCharsets.UTF_8).replaceAll("(?m)--.*$", "");
            Matcher m = CREATE.matcher(text);
            while (m.find()) {
                if (tables.containsKey(m.group(1))) continue;
                Table t = new Table();
                t.name = m.group(1);
                parseBody(m.group(2), t, fks);
                tables.put(t.name, t);
            }
            Matcher a = ALTER_FK.matcher(text);
            while (a.find()) {
                fks.add(new Fk(a.group(1), clean(a.group(2).split(",")[0]), a.group(3)));
            }
        }

        for (Fk fk : fks) {
            Table t = tables.get(fk.from());
            if (t == null) continue;
            for (Column c : t.cols) if (c.name.equals(fk.fromCol())) c.fk = true;
        }
        for (Table t : tables.values()) {
            for (Column c : t.cols) c.pk = t.pkCols.contains(c.name);
        }

        List<Table> list = new ArrayList<>(tables.values());
        int y = MARGIN + TITLE;
        for (int r = 0; r * COLS < list.size(); r++) {
            int rowH = 0;
            for (int c = 0; c < COLS && r * COLS + c < list.size(); c++) {
                Table t = list.get(r * COLS + c);
                t.w = BOX_W;
                t.h = HEAD + t.cols.size() * ROW + 10;
                t.x = MARGIN + c * (BOX_W + GAP_X);
                t.y = y;
                rowH = Math.max(rowH, t.h);
            }
            y += rowH + GAP_Y;
        }
        int width = MARGIN * 2 + COLS * BOX_W + (COLS - 1) * GAP_X;
        int height = y - GAP_Y + MARGIN;

        int scale = 2;
        BufferedImage img = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.scale(scale, scale);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(245, 247, 250));
        g.fillRect(0, 0, width, height);

        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(new Color(30, 40, 60));
        g.drawString("OnlineFruitShopping - Database ERD (" + list.size() + " tables, " + fks.size() + " relations)",
                MARGIN, MARGIN + 20);

        g.setStroke(new BasicStroke(1.4f));
        g.setColor(new Color(130, 140, 160));
        for (Fk fk : fks) {
            Table a = tables.get(fk.from()), b = tables.get(fk.to());
            if (a == null || b == null || a == b) continue;
            g.draw(new Line2D.Double(a.x + a.w / 2.0, a.y + a.h / 2.0, b.x + b.w / 2.0, b.y + b.h / 2.0));
        }

        Font nameFont = new Font("SansSerif", Font.PLAIN, 12);
        Font badgeFont = new Font("SansSerif", Font.BOLD, 10);
        Font headFont = new Font("SansSerif", Font.BOLD, 13);
        for (Table t : list) {
            g.setColor(Color.WHITE);
            g.fillRoundRect(t.x, t.y, t.w, t.h, 10, 10);
            g.setColor(new Color(47, 93, 138));
            g.fillRect(t.x + 1, t.y + 1, t.w - 2, HEAD - 1);
            g.setColor(new Color(47, 93, 138));
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(t.x, t.y, t.w, t.h, 10, 10);
            g.setFont(headFont);
            g.setColor(Color.WHITE);
            g.drawString(t.name, t.x + 8, t.y + 18);

            FontMetrics typeFm = g.getFontMetrics(nameFont);
            for (int i = 0; i < t.cols.size(); i++) {
                Column c = t.cols.get(i);
                int ty = t.y + HEAD + i * ROW + 13;
                if (c.pk || c.fk) {
                    g.setFont(badgeFont);
                    g.setColor(c.pk ? new Color(200, 140, 0) : new Color(0, 130, 120));
                    g.drawString(c.pk ? "PK" : "FK", t.x + 6, ty);
                }
                g.setFont(nameFont);
                g.setColor(new Color(30, 40, 60));
                g.drawString(c.name, t.x + 32, ty);
                g.setColor(new Color(110, 120, 135));
                g.drawString(c.type, t.x + t.w - 8 - typeFm.stringWidth(c.type), ty);
            }
        }

        Files.createDirectories(out.getParent());
        ImageIO.write(img, "png", out.toFile());
        System.out.println("Wrote " + out + " (" + list.size() + " tables, " + fks.size() + " relations)");
    }

    static void parseBody(String body, Table t, List<Fk> fks) {
        for (String raw : body.split("\n")) {
            String s = raw.trim();
            if (s.endsWith(",")) s = s.substring(0, s.length() - 1).trim();

            Matcher pk = PK.matcher(s);
            if (pk.find()) {
                for (String c : pk.group(1).split(",")) t.pkCols.add(clean(c));
            }

            Matcher fk = FK_IN.matcher(s);
            if (fk.find()) {
                fks.add(new Fk(t.name, clean(fk.group(1).split(",")[0]), fk.group(2)));
            }

            Matcher c = COL.matcher(s);
            if (c.find() && !KEYWORDS.contains(c.group(1).toUpperCase())) {
                Column col = new Column();
                col.name = c.group(1);
                col.type = c.group(2);
                t.cols.add(col);
            }
        }
    }

    static String clean(String s) {
        return s.trim().replace("[", "").replace("]", "");
    }
}
