package checks;

import graphics.clipping.PolygonClipper;
import graphics.clipping.SutherlandHodgmanClipper;
import model.Polygon;
import utilities.TestUtility;

public final class FourthCheck {

    public static void main(String[] args) {
        run();
        TestUtility.finish();
    }

    public static void run() {
        PolygonClipper clipper = new SutherlandHodgmanClipper();
        int color = 0xffffffff;
        Polygon rectangle = TestUtility.polygon(color, TestUtility.points(0, 0, 4, 0, 4, 3, 0, 3));
        Polygon triangle = TestUtility.polygon(color, TestUtility.points(0, 0, 4, 0, 2, 4));

        TestUtility.check("polygon inside the window remains unchanged", () -> {
            Polygon subject = TestUtility.polygon(color, TestUtility.points(1, 1, 3, 1, 3, 2, 1, 2));
            TestUtility.exactVertices(TestUtility.vertices(subject), clipper.clip(subject, rectangle));
        });
        TestUtility.check("polygon crossing every rectangle edge becomes the rectangle", () -> {
            Polygon subject = TestUtility.polygon(color, TestUtility.points(-2, -1, 6, -1, 6, 5, -2, 5));
            TestUtility.sameVertices(TestUtility.vertices(rectangle), clipper.clip(subject, rectangle));
        });
        TestUtility.check("large subject clipped by a triangle", () -> {
            Polygon subject = TestUtility.polygon(color, TestUtility.points(-1, -1, 5, -1, 5, 5, -1, 5));
            TestUtility.sameVertices(TestUtility.vertices(triangle), clipper.clip(subject, triangle));
        });
        TestUtility.check("polygon completely outside the window is empty", () -> {
            Polygon subject = TestUtility.polygon(color, TestUtility.points(6, 5, 7, 5, 7, 6, 6, 6));
            TestUtility.yes(clipper.clip(subject, rectangle).getLines().isEmpty(),
                "outside polygon was retained");
        });
    }

}
