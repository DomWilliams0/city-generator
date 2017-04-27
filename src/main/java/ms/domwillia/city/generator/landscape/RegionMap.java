package ms.domwillia.city.generator.landscape;

import com.jwetherell.algorithms.data_structures.KdTree;
import de.alsclo.voronoi.Voronoi;
import de.alsclo.voronoi.graph.Graph;
import de.alsclo.voronoi.graph.Point;
import ms.domwillia.city.generator.RegionType;
import ms.domwillia.city.generator.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class RegionMap
{
	private final Landscape landscape;
	private List<Region> regions;

	public RegionMap(Landscape landscape, Point2D.Double centralPoint, int pointCount, int relaxCount)
	{
		this.landscape = landscape;
		this.regions = new ArrayList<>();

		Graph regionGraph = createVoronoi(pointCount, relaxCount);
		Map<Point, Region> regionMap = new HashMap<>();

		final double maxDistance =
			(landscape.getWidth() * landscape.getWidth()) +
				(landscape.getHeight() * landscape.getHeight());

		regionGraph.getSitePoints().forEach(p ->
		{
			Region r = new Region();
			r.centre = new Point2D.Double(p.x, p.y);
			r.area = new Polygon();
			r.type = RegionType.NONE;

			double distance = r.centre.distanceSq(centralPoint);
			if (distance > maxDistance)
				return;

			r.distanceFromCentre = Utils.scale(distance,
				0, maxDistance,
				0, 1);

			regionMap.put(p, r);
		});

		traceRegionShapes(regionGraph, regionMap);
		regions.addAll(regionMap.values());

		// sort by distance to centre
		regions.sort(Comparator.comparingDouble(r -> r.distanceFromCentre));

		// oof thats an awful lot of magic numbers
		Distribution[] distributions = new Distribution[]{
			new Distribution(RegionType.METROPOLITAN, 10, 20),
			new Distribution(RegionType.COMMERCIAL_LARGE, 15, 25),
			new Distribution(RegionType.INDUSTRIAL, 8, 15),
			new Distribution(RegionType.RURAL, 2, 10),
			new Distribution(RegionType.HOUSING_DENSE, 10, 18),
			new Distribution(RegionType.COMMERCIAL_SMALL, 14, 23),
			new Distribution(RegionType.HOUSING_DENSE, 18, 27),
			new Distribution(RegionType.SERVICES, 10, 15),
			new Distribution(RegionType.HOUSING_LUXURY, 20, 30),
			new Distribution(RegionType.RURAL, 2, 8),
			new Distribution(RegionType.HOUSING_DENSE, 10, 20),
			new Distribution(RegionType.HOUSING_LUXURY, 20, 30),
		};
		int curr = -1;
		int currLimit = 0;
		for (Region region : regions)
		{
			if (currLimit-- <= 0)
			{
				if (++curr >= distributions.length)
					break;

				Distribution d = distributions[curr];
				currLimit = Utils.RANDOM.nextInt(d.max - d.min + 1) + d.min;

			}


			region.type = distributions[curr].type;
		}
	}

	private class Distribution
	{
		RegionType type;
		int min, max;

		public Distribution(RegionType type, int min, int max)
		{
			this.type = type;
			this.min = min;
			this.max = max;
		}
	}

	private void traceRegionShapes(Graph regionGraph, Map<Point, Region> regionMap)
	{
		Map<Region, List<Point2D.Double>> unsortedPoints = new HashMap<>();
		regionGraph.edgeStream().forEach(e ->
		{
			if (e.getB() == null)
				return;

			Region regionA = regionMap.get(e.getSite1());
			Region regionB = regionMap.get(e.getSite2());
			if (regionA == null || regionB == null)
				return;

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
	}

	private Graph createVoronoi(int count, int relaxCount)
	{
		List<Point> startingPoints = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
			startingPoints.add(new Point(
				Utils.RANDOM.nextDouble() * landscape.getWidth(),
				Utils.RANDOM.nextDouble() * landscape.getHeight()
			));

		Voronoi voronoi = new Voronoi(startingPoints);
		for (int i = 0; i < relaxCount; i++)
			voronoi = voronoi.relax();

		return voronoi.getGraph();
	}

	public void render(Graphics2D g)
	{
		int rad = 4;

		// regions
		regions.forEach(r ->
		{
			Color color;
			switch (r.type)
			{
				case METROPOLITAN:
					color = new Color(229, 112, 25);
					break;
				case COMMERCIAL_LARGE:
					color = new Color(255, 92, 80);
					break;
				case INDUSTRIAL:
					color = new Color(255, 33, 81);
					break;
				case SERVICES:
					color = new Color(139, 250, 255);
					break;
				case COMMERCIAL_SMALL:
					color = new Color(255, 160, 147);
					break;
				case HOUSING_DENSE:
					color = new Color(134, 132, 255);
					break;
				case HOUSING_LUXURY:
					color = new Color(162, 176, 255);
					break;
				case RURAL:
					color = new Color(56, 255, 46);
					break;
				default:
					color = Color.DARK_GRAY;
					break;
			}
			g.setColor(color);
			g.fill(r.area);

			g.setColor(Color.ORANGE);
			g.fillOval((int) (r.centre.x - rad / 2), (int) (r.centre.y - rad / 2), rad, rad);
		});
		// labels
		g.setFont(new Font(null, Font.PLAIN, 5));
		g.setColor(Color.BLACK);
		regions.forEach(r ->
		{
			final String label = r.type.toString();
			int w = g.getFontMetrics().stringWidth(label);

			g.drawString(label, (float) r.centre.x - w / 2, (float) r.centre.y);
		});


	}

	private class RegionPoint extends KdTree.XYZPoint
	{
		Region region;

		RegionPoint(double x, double y)
		{
			super(x, y);
			this.region = null;
		}

		RegionPoint(Region region)
		{
			this(region.centre.x, region.centre.y);
			this.region = region;
		}
	}
}
