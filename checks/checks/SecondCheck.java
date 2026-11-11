package checks;

import graphics.clipping.SutherlandHodgmanClipper;
import model.Line;
import model.Point;
import utilities.TestUtility;

public final class SecondCheck {

    public static void main(String[] args) {
        run();
        TestUtility.finish();
    }

    public static void run() {
        SutherlandHodgmanClipper clipper = new SutherlandHodgmanClipper();
        int color = 0xffffffff;
        Line left = TestUtility.line(0, 3, 0, 0, color);
        Line right = TestUtility.line(4, 0, 4, 3, color);
        Line top = TestUtility.line(0, 0, 4, 0, color);
        Line bottom = TestUtility.line(4, 3, 0, 3, color);

        TestUtility.check("intersection with left edge", () ->
            TestUtility.same(new Point(0, 2), intersection(clipper, TestUtility.line(-2, 1, 2, 3, color), left))
        );
        TestUtility.check("intersection with right edge", () ->
            TestUtility.same(new Point(4, 2), intersection(clipper, TestUtility.line(2, 1, 6, 3, color), right))
        );
        TestUtility.check("intersection with top edge", () ->
            TestUtility.same(new Point(2, 0), intersection(clipper, TestUtility.line(1, -2, 3, 2, color), top))
        );
        TestUtility.check("intersection with bottom edge", () ->
            TestUtility.same(new Point(2, 3), intersection(clipper, TestUtility.line(1, 1, 3, 5, color), bottom))
        );
        TestUtility.check("fractional crossing rounds only at the model boundary", () ->
            TestUtility.same(new Point(0, 1), intersection(clipper, TestUtility.line(-1, 1, 2, 2, color), left))
        );
    }

    private static Point intersection(SutherlandHodgmanClipper clipper, Line segment, Line boundary) {
        return clipper.intersection(segment.getPoint1(), segment.getPoint2(), boundary.getPoint1(), boundary.getPoint2());
    }

}
