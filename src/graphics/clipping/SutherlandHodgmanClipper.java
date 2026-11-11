package graphics.clipping;


import model.Line;
import model.Point;
import model.Polygon;

import java.util.ArrayList;
import java.util.List;

public final class SutherlandHodgmanClipper implements PolygonClipper {

    /**
     * Clips a polygon against a convex clipping polygon
     * using the Sutherland-Hodgman algorithm.
     */
    public Polygon clip(Polygon subject, Polygon boundary) {
        List<Line> lines = subject.getLines();
        List<Point> subjectPts = vertices(subject);
        List<Point> boundaryPts = vertices(boundary);
        if (subjectPts.size() < 3) {
            return new Polygon();
        }
        if (boundaryPts.size() < 3 || area(boundaryPts) == 0) {
            throw new IllegalArgumentException();
        }

        boolean ccw = area(boundaryPts) > 0;

        for (int i = 0; i < boundaryPts.size(); i++) {
            Point a = boundaryPts.get(i);
            Point b = boundaryPts.get((i + 1) % boundaryPts.size());
            subjectPts = clipEdge(subjectPts, a, b, ccw);
            if (subjectPts.size() < 3) {
                return new Polygon();
            }
        }

        return toPolygon(subjectPts, lines.getFirst().getColor());
    }

    /**
     * Checks whether a point lies inside the clipping half-plane.
     * The inside direction depends on the polygon orientation (CW or CCW).
     */
    public boolean isInside(Point p, Point a, Point b, boolean ccw) {
        double side = cross(a, b, p);
        return ccw ? side >= 0 : side <= 0;
    }

    /**
     * Calculates the intersection of segment PQ with the line AB.
     * The resulting coordinates are rounded to integers.
     */
    public Point intersection(Point p, Point q, Point a, Point b) {
        double d1 = cross(a, b, p);
        double d2 = cross(a, b, q);
        if (d1 == d2) {
            throw new IllegalArgumentException();
        }
        double t = d1 / (d1 - d2);
        int x = (int) Math.round(p.getX() + t * (q.getX() - p.getX()));
        int y = (int) Math.round(p.getY() + t * (q.getY() - p.getY()));
        return new Point(x, y);
    }

    /**
     * Clips a polygon against a single edge of the clipping polygon.
     * Returns the vertices that remain inside the clipping half-plane.
     */
    private List<Point> clipEdge(List<Point> polygon, Point a, Point b, boolean ccw) {
        List<Point> edges = new ArrayList<>();
        Point previous = polygon.getLast();

        for (Point current : polygon) {
            boolean previousInside = isInside(previous, a, b, ccw);
            boolean currentInside = isInside(current, a, b, ccw);
            if (previousInside && currentInside) {
                edges.add(current);
            } else if (previousInside) {
                edges.add(intersection(previous, current, a, b));
            } else if (currentInside) {
                edges.add(intersection(previous, current, a, b));
                edges.add(current);
            }

            previous = current;
        }

        return edges;
    }

    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- Utilities -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- */

    /**
     * Calculates the signed area of a polygon using the shoelace formula.
     * The sign determines the orientation of its vertices:
     *
     * > 0 - Counterclockwise (CCW).
     * < 0 - Clockwise (CW).
     * = 0 - Zero area (degenerate polygon).
     *
     * The last vertex is automatically connected to the first.
     */
    private double area(List<Point> polygon) {
        double sum = 0;
        for (int i = 0; i < polygon.size(); i++) {
            Point a = polygon.get(i);
            Point b = polygon.get((i + 1) % polygon.size());
            sum += (double) a.getX() * b.getY() - (double) b.getX() * a.getY();
        }
        return sum / 2;
    }

    /**
     * Calculates the cross product of vectors AB and AP.
     * The sign determines the position of point P relative to the directed line AB:
     *
     * > 0 - Point is to the left of the line.
     * < 0 - Point is to the right of the line.
     * = 0 - Point lies on the line.
     *
     * Used to determine whether a point is inside the clipping region.
     */
    private double cross(Point a, Point b, Point p) {
        return ((double) b.getX() - a.getX()) * (p.getY() - a.getY()) - ((double) b.getY() - a.getY()) * (p.getX() - a.getX());
    }

    /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

    /**
     * Converts a polygon represented by edges into a list of vertices.
     * Adds the final endpoint only if it differs from the first vertex,
     * avoiding a duplicate vertex in closed polygons.
     */
    private List<Point> vertices(Polygon polygon) {
        List<Line> lines = polygon.getLines();
        List<Point> result = new ArrayList<>();
        for (Line line : lines) {
            result.add(line.getPoint1());
        }

        if (!lines.isEmpty()) {
            Point first = lines.getFirst().getPoint1();
            Point last = lines.getLast().getPoint2();
            if (first.getX() != last.getX() || first.getY() != last.getY()) {
                result.add(last);
            }
        }
        return result;
    }

    /**
     * Converts a list of vertices back into a polygon represented by edges.
     * Returns an empty polygon if fewer than three vertices remain
     * or the polygon has zero area.
     *
     * Consecutive vertices form edges; zero-length edges are skipped.
     * The polygon is stored as an open chain, with closure handled during rendering.
     */
    private Polygon toPolygon(List<Point> points, int color) {
        Polygon result = new Polygon();
        if (points.size() < 3 || area(points) == 0) {
            return result;
        }

        for (int i = 1; i < points.size(); i++) {
            Point a = points.get(i - 1);
            Point b = points.get(i);
            if (a.getX() != b.getX() || a.getY() != b.getY()) {
                result.addLine(new Line(a, b, color));
            }
        }
        return result;
    }

}
