package utilities;

import model.Line;
import model.Point;
import model.Polygon;

import java.util.ArrayList;
import java.util.List;

public final class TestUtility {

    private static int total;
    private static int passed;

    private TestUtility() {}

    public static void check(String name, Runnable body) {
        total++;
        try {
            body.run();
            passed++;
            System.out.println("  OK   " + name);
        } catch (Throwable error) {
            System.out.println("  FAIL " + name + " -> " + error);
        }
    }

    public static void finish() {
        System.out.printf("%nRESULT: %d / %d checks passed%n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }

    public static void yes(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void same(Point expected, Point actual) {
        yes(actual != null, "missing point");
        yes(expected.getX() == actual.getX() && expected.getY() == actual.getY(),
            "expected (" + expected.getX() + ", " + expected.getY() + "), actual ("
                + actual.getX() + ", " + actual.getY() + ")");
    }

    public static void exactVertices(List<Point> expected, Polygon actual) {
        List<Point> actualVertices = vertices(actual);
        yes(expected.size() == actualVertices.size(),
            "expected " + expected.size() + " vertices, got " + actualVertices.size() + ": " + actualVertices);
        for (int i = 0; i < expected.size(); i++) {
            same(expected.get(i), actualVertices.get(i));
        }
    }

    public static void sameVertices(List<Point> expected, Polygon actual) {
        List<Point> actualVertices = vertices(actual);
        yes(expected.size() == actualVertices.size(),
            "expected " + expected.size() + " vertices, got " + actualVertices.size() + ": " + formatPoints(actualVertices));
        for (Point point : expected) {
            boolean found = actualVertices.stream().anyMatch(candidate -> sameCoordinates(point, candidate));
            yes(found, "missing point (" + point.getX() + ", " + point.getY() + ") from " + formatPoints(actualVertices));
        }
    }

    public static void openEdgeChain(Polygon polygon, int expectedVertices) {
        List<Line> lines = polygon.getLines();
        int expectedEdges = expectedVertices - 1;
        yes(lines.size() == expectedEdges,
            "expected " + expectedEdges + " edges in the open chain, got " + lines.size());
        for (int i = 0; i + 1 < lines.size(); i++) {
            same(lines.get(i).getPoint2(), lines.get(i + 1).getPoint1());
        }
        yes(!sameCoordinates(lines.getFirst().getPoint1(), lines.getLast().getPoint2()),
            "edge chain contains an explicit closing edge");
    }

    public static List<Point> vertices(Polygon polygon) {
        List<Line> lines = polygon.getLines();
        if (lines.isEmpty()) {
            return List.of();
        }

        List<Point> result = new ArrayList<>();
        result.add(lines.get(0).getPoint1());
        for (Line line : lines) {
            result.add(line.getPoint2());
        }
        if (result.size() > 1 && sameCoordinates(result.get(0), result.get(result.size() - 1))) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    public static List<Point> points(int... coordinates) {
        if (coordinates.length % 2 != 0) {
            throw new IllegalArgumentException("Coordinates must be x/y pairs");
        }
        List<Point> result = new ArrayList<>();
        for (int i = 0; i < coordinates.length; i += 2) {
            result.add(new Point(coordinates[i], coordinates[i + 1]));
        }
        return result;
    }

    static Polygon polygon(int color, Point... vertices) {
        Polygon result = new Polygon();
        for (int i = 0; i + 1 < vertices.length; i++) {
            result.addLine(new Line(vertices[i], vertices[i + 1], color));
        }
        return result;
    }

    public static Polygon polygon(int color, List<Point> vertices) {
        return polygon(color, vertices.toArray(Point[]::new));
    }

    public static Line line(int x1, int y1, int x2, int y2, int color) {
        return new Line(new Point(x1, y1), new Point(x2, y2), color);
    }

    private static boolean sameCoordinates(Point first, Point second) {
        return first.getX() == second.getX() && first.getY() == second.getY();
    }

    private static List<String> formatPoints(List<Point> points) {
        return points.stream().map(point -> "(" + point.getX() + ", " + point.getY() + ")").toList();
    }

}
