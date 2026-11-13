package model;

import java.util.ArrayList;
import java.util.List;

public final class Polygon {

    private final List<Line> lines;

    public Polygon() {
        lines = new ArrayList<>();
    }

    public void clear() {
        lines.clear();
    }

    public Point getLastPoint() {
        return lines.isEmpty() ? null : lines.getLast().getPoint2();
    }

    public Point getPointWithLockedOrientation(Point point) {
        Point start = getLastPoint();
        if (start == null) {
            return point;
        }
        return getPointWithLockedOrientation(start, point);
    }

    public Point getPointWithLockedOrientation(Point start, Point point) {
        int deltaX = point.getX() - start.getX();
        int deltaY = point.getY() - start.getY();
        if (Math.abs(deltaX) >= Math.abs(deltaY)) {
            return new Point(point.getX(), start.getY());
        }
        return new Point(start.getX(), point.getY());
    }

    public Point getPointWithDiagonalOrientation(Point point) {
        Point start = getLastPoint();
        if (start == null) {
            return point;
        }
        return getPointWithDiagonalOrientation(start, point);
    }

    public Point getPointWithDiagonalOrientation(Point start, Point point) {
        int deltaX = point.getX() - start.getX();
        int deltaY = point.getY() - start.getY();
        int distance = Math.max(Math.abs(deltaX), Math.abs(deltaY));
        return new Point(
            start.getX() + Integer.signum(deltaX) * distance,
            start.getY() + Integer.signum(deltaY) * distance
        );
    }

    public List<Line> getLines() {
        return List.copyOf(lines);
    }

    public void addLine(Line line) {
        lines.add(line);
    }

}
