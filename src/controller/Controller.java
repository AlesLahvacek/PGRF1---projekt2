package controller;

import graphics.filler.ScanlineFiller;
import graphics.filler.RecursiveSeedFiller;
import graphics.filler.SeedFiller;
import graphics.clipping.PolygonClipper;
import graphics.clipping.SutherlandHodgmanClipper;
import graphics.rasterizer.PolygonOutlineRasterizer;
import graphics.rasterizer.PolygonRasterizer;
import graphics.rasterizer.XiaolinWuLineRasterizer;
import model.Line;
import model.Point;
import model.Polygon;
import view.Canvas;
import view.gui.Toolbar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_R;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.KeyStroke.getKeyStroke;

public class Controller {

    private final Canvas canvas;
    private final Toolbar toolbar;

    private Line previewLine;
    private final Polygon polygon;
    private final PolygonClipper clipper;
    private final Polygon clippingPolygon;
    private final PolygonRasterizer polygonRasterizer;

    private Point seedFillPoint;
    private boolean seedFillMode;
    private boolean clippingPolygonMode;
    private final SeedFiller seedFiller;
    private final ScanlineFiller scanlineFiller;

    private static final int LINE_COLOR = Color.WHITE.getRGB();
    private static final int PREVIEW_COLOR = Color.RED.getRGB();
    private static final int ORIGINAL_COLOR = new Color(0x9099A3).getRGB();
    private static final int CLIPPED_COLOR = new Color(0x08B024).getRGB();
    private static final int CLIPPING_TRIANGLE_COLOR = new Color(0xE6B85C).getRGB();
    private static final int FILL_COLOR = new Color(0x58BFA9).getRGB();

    private static final List<Point> CLIPPING_TRIANGLE_VERTICES = List.of(
        new Point(250, 100),
        new Point(480, 470),
        new Point(40, 470)
    );

    public Controller(Canvas canvas, Toolbar toolbar) {
        this.canvas = canvas;
        this.toolbar = toolbar;
        this.polygon = new Polygon();
        this.clipper = new SutherlandHodgmanClipper();
        this.scanlineFiller = new ScanlineFiller(canvas.getRaster());
        this.seedFiller = new RecursiveSeedFiller(canvas.getRaster());
        this.polygonRasterizer = new PolygonOutlineRasterizer(new XiaolinWuLineRasterizer(canvas.getRaster()));
        this.clippingPolygon = createPolygon(CLIPPING_TRIANGLE_VERTICES, CLIPPING_TRIANGLE_COLOR);
    }

    public void init() {
        toolbar.addClearListener(_ -> clearDrawing());
        toolbar.addSeedFillListener(_ -> toggleSeedFillMode());
        canvas.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke(VK_C, 0), "clearCanvas");
        canvas.getActionMap().put("clearCanvas", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearDrawing();
            }
        });
        canvas.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke(VK_R, 0), "toggleClippingPolygon");
        canvas.getActionMap().put("toggleClippingPolygon", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                toggleClippingPolygonMode();
            }
        });

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON2) {
                    moveNearestVertex(getPoint(event));
                } else if (event.getButton() == MouseEvent.BUTTON1) {
                    if (seedFillMode) {
                        fillAt(event);
                    } else {
                        startLine(event);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (event.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                if (previewLine == null) {
                    return;
                }
                getActivePolygon().addLine(new Line(
                    previewLine.getPoint1(),
                    previewLine.getPoint2(),
                    clippingPolygonMode ? CLIPPING_TRIANGLE_COLOR : LINE_COLOR
                ));
                previewLine = null;
                render();
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                if (previewLine != null) {
                    previewLine = new Line(
                        previewLine.getPoint1(),
                        getPoint(event, event.isShiftDown(), event.isControlDown()),
                        PREVIEW_COLOR
                    );
                    render();
                }
            }
        });

        render();
    }

    private void render() {
        canvas.clear();
        Polygon subject = withPreviewLine(polygon, clippingPolygonMode ? null : previewLine);
        Polygon boundary = clippingPolygonMode
            ? withPreviewLine(clippingPolygon, previewLine)
            : clippingPolygon;
        Polygon clipped;
        if (boundary.getLines().size() < 2) {
            clipped = null;
        } else {
            try {
                clipped = clipper.clip(subject, boundary);
            } catch (IllegalArgumentException | UnsupportedOperationException unfinishedClipper) {
                clipped = null;
            }
        }
        if (clipped != null) {
            scanlineFiller.fill(clipped, FILL_COLOR);
        }
        polygonRasterizer.rasterize(
            withColor(polygon, ORIGINAL_COLOR),
            clippingPolygonMode ? null : previewLine
        );
        if (clipped != null) {
            polygonRasterizer.rasterize(withColor(clipped, CLIPPED_COLOR), null);
        }
        polygonRasterizer.rasterize(boundary, null);
        if (seedFillPoint != null) {
            seedFiller.fill(seedFillPoint.getX(), seedFillPoint.getY(), canvas.getBackgroundColor(), FILL_COLOR);
        }
        canvas.repaint();
    }

    private void clearDrawing() {
        polygon.clear();
        previewLine = null;
        seedFillPoint = null;
        seedFillMode = false;
        toolbar.setSeedFillMode(false);
        render();
    }

    private void toggleClippingPolygonMode() {
        clippingPolygonMode = !clippingPolygonMode;
        previewLine = null;
        seedFillPoint = null;
        seedFillMode = false;
        toolbar.setSeedFillMode(false);
        if (clippingPolygonMode) {
            clippingPolygon.clear();
        }
        render();
    }

    private void toggleSeedFillMode() {
        seedFillMode = !seedFillMode;
        toolbar.setSeedFillMode(seedFillMode);
        render();
    }

    private void fillAt(MouseEvent event) {
        Point seed = getPoint(event);
        Integer seedColor = canvas.getRaster().getPixel(seed.getX(), seed.getY());
        if (seedFillPoint != null && seedColor != null && seedColor == FILL_COLOR) {
            seedFillPoint = null;
            render();
            return;
        }

        boolean filled = seedFiller.fill(seed.getX(), seed.getY(), canvas.getBackgroundColor(), FILL_COLOR);
        if (filled) {
            seedFillPoint = seed;
            render();
        }
    }

    private Point getPoint(MouseEvent event) {
        return new Point(event.getX(), event.getY());
    }

    private Point getPoint(MouseEvent event, boolean lockOrientation, boolean lockDiagonalOrientation) {
        Point point = getPoint(event);
        if (previewLine == null) {
            return point;
        }
        if (lockDiagonalOrientation) {
            return getActivePolygon().getPointWithDiagonalOrientation(previewLine.getPoint1(), point);
        }
        if (!lockOrientation) {
            return point;
        }
        return getActivePolygon().getPointWithLockedOrientation(previewLine.getPoint1(), point);
    }

    private void startLine(MouseEvent event) {
        seedFillPoint = null;
        Polygon activePolygon = getActivePolygon();
        Point startPoint = activePolygon.getLastPoint();
        if (startPoint == null) {
            startPoint = getPoint(event);
        }
        previewLine = new Line(startPoint, startPoint, PREVIEW_COLOR);
        render();
    }

    private Polygon getActivePolygon() {
        return clippingPolygonMode ? clippingPolygon : polygon;
    }

    private void moveNearestVertex(Point target) {
        Polygon activePolygon = getActivePolygon();
        List<Line> lines = activePolygon.getLines();
        if (lines.size() < 2) {
            return;
        }

        List<Point> vertices = new ArrayList<>();
        for (Line line : lines) {
            vertices.add(line.getPoint1());
        }
        vertices.add(lines.getLast().getPoint2());

        int nearestVertex = 0;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < vertices.size(); i++) {
            double distance = distanceSquared(target, vertices.get(i));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestVertex = i;
            }
        }

        vertices.set(nearestVertex, target);

        activePolygon.clear();
        for (int i = 0; i < lines.size(); i++) {
            activePolygon.addLine(new Line(
                vertices.get(i),
                vertices.get(i + 1),
                lines.get(i).getColor()
            ));
        }
        previewLine = null;
        seedFillPoint = null;
        render();
    }

    private double distanceSquared(Point first, Point second) {
        double differenceX = first.getX() - second.getX();
        double differenceY = first.getY() - second.getY();
        return differenceX * differenceX + differenceY * differenceY;
    }

    private Polygon withColor(Polygon source, int color) {
        Polygon result = new Polygon();
        for (Line line : source.getLines()) {
            result.addLine(new Line(line.getPoint1(), line.getPoint2(), color));
        }
        return result;
    }

    private Polygon withPreviewLine(Polygon source, Line preview) {
        Polygon result = new Polygon();
        for (Line line : source.getLines()) {
            result.addLine(line);
        }
        if (preview != null) {
            result.addLine(preview);
        }
        return result;
    }

    private Polygon createPolygon(List<? extends Point> vertices, int color) {
        Polygon result = new Polygon();
        for (int i = 0; i + 1 < vertices.size(); i++) {
            result.addLine(new Line(vertices.get(i), vertices.get(i + 1), color));
        }
        return result;
    }

}
