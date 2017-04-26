package ms.domwillia.city.generator;

import de.alsclo.voronoi.Voronoi;
import de.alsclo.voronoi.graph.Graph;
import ms.domwillia.city.Config;
import ms.domwillia.city.generator.util.NoiseRandom;
import ms.domwillia.city.generator.util.Utils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Landscape
{
	private final int width;
	private final int height;

	private List<Point2D.Double> riverPoints;
	private List<Point2D.Double> riverPointsInterpolated;
	private Path2D.Double riverPath;

	private List<Region> regions;

	public Landscape(int width, int height)
	{
		this.width = width;
		this.height = height;
		this.riverPoints = new ArrayList<>();
		this.riverPointsInterpolated = new ArrayList<>();
	}

	/**
	 * @param minPoints      Retry until the river has this many points
	 * @param scanAngle      The angle to use when scanning for the next river point (on each side, and in radians)
	 * @param scanRangeScale Distance between each river point - factor of the NOISE_SCALE
	 * @param sampleCount    The number of samples to take to choose the next river point
	 */
	public void generateRiver(double minPoints, double scanAngle, double scanRangeScale, int sampleCount)
	{
		double noise = Config.getDouble(Config.Key.NOISE_SCALE);

		int maxAttempts = 500;
		while (riverPoints.size() < minPoints && maxAttempts-- > 0)
		{
			riverPoints.clear();
			placeRivers(scanAngle, noise * scanRangeScale, sampleCount);
		}

		if (maxAttempts < 0)
			System.err.println("Aborted river generation after too many tries, settling with " + riverPoints.size());

		final int scale = 3;

		double[] indices = new double[riverPoints.size()];
		double[] xs = new double[riverPoints.size()];
		double[] ys = new double[riverPoints.size()];
		for (int i = 0, riverSize = riverPoints.size(); i < riverSize; i++)
		{
			Point2D.Double p = riverPoints.get(i);
			xs[i] = p.x;
			ys[i] = p.y;
			indices[i] = i * scale;
		}

		UnivariateInterpolator interpolator = new AkimaSplineInterpolator();
		UnivariateFunction splineX = interpolator.interpolate(indices, xs);
		UnivariateFunction splineY = interpolator.interpolate(indices, ys);

		for (int i = 0; i < xs.length - 1; i++)
		{
			double index = indices[i];

			for (int j = 0; j < scale; j++)
			{
				Point2D.Double newPoint = new Point2D.Double(
					splineX.value(index + j), splineY.value(index + j)
				);
				riverPointsInterpolated.add(newPoint);
			}

		}

		riverPath = new Path2D.Double();
		for (int i = 0, pointCount = riverPointsInterpolated.size() - 1; i < pointCount; i++)
		{
			Point2D.Double curr = riverPointsInterpolated.get(i);
			Point2D.Double next = riverPointsInterpolated.get(i + 1);
			riverPath.moveTo(curr.x, curr.y);
			riverPath.lineTo(next.x, next.y);
		}
	}

	private void placeRivers(double scanAngle, double scanRange, int sampleCount)
	{
		Density density = new Density(10);
		NoiseRandom rand = new NoiseRandom();

		// random seed point
		Point2D.Double pos = new Point2D.Double();
		Vector2D direction = generateRiverSeed(pos);

		Point2D.Double bestPoint = new Point2D.Double();
		double minDensity;

		while (true)
		{
			minDensity = Double.MAX_VALUE;

			// add this point
			riverPoints.add(new Point.Double(pos.x, pos.y));

			// scan ahead in for best next point
			for (int i = 0; i < sampleCount; i++)
			{
				double currentAngle = Math.atan2(direction.getY(), direction.getX());

				double searchAngle = currentAngle + (rand.getRandom() * scanAngle * 2) - scanAngle;

				Point2D.Double check = new Point2D.Double(
					pos.x + scanRange * Math.cos(searchAngle),
					pos.y + scanRange * Math.sin(searchAngle)
				);

				double value = density.getValue(check.x, check.y);
				if (value < minDensity)
				{
					minDensity = value;
					bestPoint.setLocation(check);
				}
			}

			// move on
			direction = new Vector2D(bestPoint.x - pos.x, bestPoint.y - pos.y);
			pos.setLocation(bestPoint);

			// out of range
			if (pos.x < 0 || pos.y < 0 ||
				pos.x >= width || pos.y >= height)
				break;
		}

		riverPoints.add(new Point.Double(bestPoint.x, bestPoint.y));
	}

	private Vector2D generateRiverSeed(Point2D.Double pos)
	{
		pos.setLocation(
			Utils.RANDOM.nextDouble() * width,
			Utils.RANDOM.nextDouble() * height
		);

		Vector2D direction = null;

		switch (Utils.RANDOM.nextInt(4))
		{
			// top
			case 0:
				pos.y = 0;
				direction = new Vector2D(0, 1);
				break;

			// left
			case 1:
				pos.x = 0;
				direction = new Vector2D(1, 0);
				break;

			// bottom
			case 2:
				pos.y = height;
				direction = new Vector2D(0, -1);
				break;

			// right
			case 3:
				pos.x = width;
				direction = new Vector2D(-1, 0);
				break;
		}

		return direction;
	}

	public void createPolygons(int count, int relaxCount)
	{
		List<de.alsclo.voronoi.graph.Point> startingPoints = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
			startingPoints.add(new de.alsclo.voronoi.graph.Point(
				Utils.RANDOM.nextDouble() * width,
				Utils.RANDOM.nextDouble() * height
			));

		Voronoi voronoi = new Voronoi(startingPoints);
		for (int i = 0; i < relaxCount; i++)
			voronoi = voronoi.relax();

		Graph regionGraph = voronoi.getGraph();

		Map<de.alsclo.voronoi.graph.Point, Region> regionMap = new HashMap<>();
		regions = new ArrayList<>();

		Density density = new Density();

		regionGraph.getSitePoints().forEach(p ->
		{
			Region r = new Region();
			r.centre = new Point2D.Double(p.x, p.y);
			r.density = density.getValue(p.x, p.y);
			r.area = new Polygon();
			regionMap.put(p, r);
		});

		// create polygons around
		// more edges = more central!

		Map<Region, List<Point2D.Double>> unsortedPoints = new HashMap<>();
		regionGraph.edgeStream().forEach(e ->
		{
			if (e.getB() == null)
				return;

			Region regionA = regionMap.get(e.getSite1());
			Region regionB = regionMap.get(e.getSite2());

			int ax = (int) e.getA().getLocation().x;
			int ay = (int) e.getA().getLocation().y;

			int bx = (int) e.getB().getLocation().x;
			int by = (int) e.getB().getLocation().y;

			Region[] rs = new Region[]{regionA, regionB};
			for (Region r : rs)
			{
				unsortedPoints.putIfAbsent(r, new ArrayList<>());
				List<Point2D.Double> points = unsortedPoints.get(r);
				points.add(new Point2D.Double(ax, ay));
				points.add(new Point2D.Double(bx, by));
			}
		});

		for (Region region : unsortedPoints.keySet())
		{
			List<Point2D.Double> points = unsortedPoints.get(region);
			points.sort((a, b) ->
			{
				double aAngle = Math.atan2(a.y - region.centre.y, a.x - region.centre.x);
				double bAngle = Math.atan2(b.y - region.centre.y, b.x - region.centre.x);
				return Double.compare(aAngle, bAngle);
			});
			for (Point2D.Double point : points)
			{
				region.area.addPoint((int) point.x, (int) point.y);
			}
		}

		regions.addAll(regionMap.values());

	}

	void render(Graphics2D g)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int rad = 4;

		// regions
		regions.forEach(r ->
		{
			int pixel = (int) (r.density * 255);
			g.setColor(new Color(pixel, pixel, pixel));
			g.fill(r.area);
			g.setColor(Color.ORANGE);
			g.fillOval((int) (r.centre.x - rad / 2), (int) (r.centre.y - rad / 2), rad, rad);
		});


		// river
		g.setColor(new Color(39, 141, 247));
		g.setStroke(new BasicStroke(30, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(riverPath);

		g.setStroke(new BasicStroke(1));

		g.setColor(Color.RED);
		for (Point2D.Double p : riverPointsInterpolated)
			g.fillOval((int) (p.x - rad / 2), (int) (p.y - rad / 2), rad, rad);

		g.setColor(Color.BLACK);
		for (Point2D.Double p : riverPoints)
			g.fillOval((int) (p.x - rad / 2), (int) (p.y - rad / 2), rad, rad);

	}

	private class Region
	{
		Point2D.Double centre;
		Polygon area;
		double density;
	}
}
