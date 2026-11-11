package checks;

import graphics.clipping.PolygonClipper;
import graphics.clipping.SutherlandHodgmanClipper;
import model.Point;
import model.Polygon;
import utilities.TestUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FifthCheck {

    public static void main(String[] args) {
        run();
        TestUtility.finish();
    }

    public static void run() {
        PolygonClipper clipper = new SutherlandHodgmanClipper();
        int color = 0xffffffff;
        List<Point> rectangleVertices = TestUtility.points(0, 0, 4, 0, 4, 3, 0, 3);
        Polygon rectangle = TestUtility.polygon(color, rectangleVertices);
        Polygon subject = TestUtility.polygon(color, TestUtility.points(-1, -1, 5, -1, 5, 5, -1, 5));

        TestUtility.check("clockwise and counter-clockwise boundaries agree", () -> {
            List<Point> reversedVertices = new ArrayList<>(rectangleVertices);
            Collections.reverse(reversedVertices);
            Polygon reversed = TestUtility.polygon(color, reversedVertices);
            TestUtility.sameVertices(TestUtility.vertices(clipper.clip(subject, rectangle)), clipper.clip(subject, reversed));
        });
        TestUtility.check("empty subject stays empty", () -> {
            TestUtility.yes(clipper.clip(new Polygon(), rectangle).getLines().isEmpty(), "empty subject produced edges");
        });
        TestUtility.check("subject lines are not modified", () -> {
            Polygon input = TestUtility.polygon(color, TestUtility.points(-1, 1, 2, 1, 2, 2, -1, 2));
            List<Point> before = TestUtility.vertices(input);
            clipper.clip(input, TestUtility.polygon(color, TestUtility.points(0, 0, 4, 0, 4, 3, 0, 3)));
            TestUtility.exactVertices(before, input);
        });
        TestUtility.check("clipping boundary with fewer than three vertices is rejected", () -> {
            Polygon shortBoundary = TestUtility.polygon(color, TestUtility.points(0, 0, 4, 0));
            try {
                clipper.clip(subject, shortBoundary);
            } catch (IllegalArgumentException expected) {
                return;
            }
            throw new AssertionError("boundary with fewer than three vertices was accepted");
        });
        TestUtility.check("degenerate clipping polygon is rejected", () -> {
            Polygon degenerate = TestUtility.polygon(color, TestUtility.points(0, 0, 2, 0, 4, 0));
            try {
                clipper.clip(subject, degenerate);
            } catch (IllegalArgumentException expected) {
                return;
            }
            throw new AssertionError("degenerate clipping polygon was accepted");
        });
    }

}
