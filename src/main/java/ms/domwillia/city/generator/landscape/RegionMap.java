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
	private KdTree<RegionPoint> kdTree;

	public RegionMap(Landscape landscape, Point2D.Double centralPoint, int pointCount, int relaxCount)
	{
		this.landscape = landscape;
		this.regions = new ArrayList<>();
		this.kdTree = new KdTree<>();

		placeRegions(centralPoint, pointCount, relaxCount);
		allocateRegionTypes();
	}

	private void setRegions(Point2D.Double from, int[] seeds, RegionType type)
	{
		OptionalInt max = Arrays.stream(seeds).max();
		assert max.isPresent();
		Collection<RegionPoint> points = kdTree.nearestNeighbourSearch(max.getAsInt() + 1, new RegionPoint(from.x, from.y));

		ArrayList<RegionPoint> arr = new ArrayList<>(points);
		for (int seed : seeds)
		{
			arr.get(seed).region.type = type;
		}
	}

	private Region getRegion(Point2D.Double from, int n)
	{
		Collection<RegionPoint> points = kdTree.nearestNeighbourSearch(n, new RegionPoint(from.x, from.y));
		RegionPoint last = null;
		for (RegionPoint point : points)
			last = point;

		return last == null ? null : last.region;
	}

	private class Distribution
	{
		int count;
		RegionType type;
		double min, max;

		Distribution(int count, RegionType type, double min, double max)
		{
			this.count = count;
			this.type = type;
			this.min = min;
			this.max = max;
		}
	}

	private class Growth
	{
		RegionType type;
		double growthFactor;

		Growth(RegionType type, double growthFactor)
		{
			this.type = type;
			this.growthFactor = growthFactor;
		}
	}

	private class Morph
	{
		RegionType from, to;
		double factor;

		public Morph(RegionType from, RegionType to, double factor)
		{
			this.from = from;
			this.to = to;
			this.factor = factor;
		}
	}

	private void allocateRegionTypes()
	{
		Distribution[] dist = new Distribution[]{
			new Distribution(1, RegionType.METROPOLITAN, 0, 0.05),
			new Distribution(14, RegionType.RURAL, 0, 1),
			new Distribution(6, RegionType.HOUSING_DENSE, 0.1, 0.2),
			new Distribution(6, RegionType.HOUSING_DENSE, 0.6, 1),
			new Distribution(6, RegionType.HOUSING_LUXURY, 0.25, 0.4),
			new Distribution(6, RegionType.HOUSING_LUXURY, 0.6, 1),
			new Distribution(7, RegionType.COMMERCIAL_SMALL, 0.2, 0.8),
			new Distribution(8, RegionType.COMMERCIAL_LARGE, 0.02, 0.2),
		};

		Growth[] growth = new Growth[]{
			new Growth(RegionType.METROPOLITAN, 0.15),
			new Growth(RegionType.HOUSING_LUXURY, 0.9),
			new Growth(RegionType.HOUSING_DENSE, 0.7),
			new Growth(RegionType.COMMERCIAL_LARGE, 0.05),
			new Growth(RegionType.RURAL, 0.1),
			new Growth(RegionType.INDUSTRIAL, 0.1),
		};

		// randomly distribute
		for (Distribution d : dist)
		{
			for (int i = 0; i < d.count; i++)
			{
				double rand = d.min + (d.max - d.min) * Utils.RANDOM.nextDouble();
				int index = (int) (rand * regions.size());
				if (index == regions.size())
					index = regions.size() - 1;

				regions.get(index).type = d.type;
			}
		}

		// industrials next to the river
		List<Point2D.Double> riverPoints = landscape.getRiver().getPoints();
		getRegion(riverPoints.get(0), 1).type = RegionType.INDUSTRIAL;
		getRegion(riverPoints.get(riverPoints.size() - 1), 1).type = RegionType.INDUSTRIAL;

		// initial iterative growth
		final int nearFactor = 5;
		// allocate most regions
		while (regions.stream().anyMatch(r -> r.type == RegionType.NONE))
		{
			final int[] changes = {0};
			for (Growth g : growth)
			{
				regions.stream().filter(r -> r.type == g.type).forEach(r ->
				{
					Collection<RegionPoint> near =
						kdTree.nearestNeighbourSearch(nearFactor, new RegionPoint(r));

					for (RegionPoint regionPoint : near)
					{
						if (regionPoint.region.type == RegionType.NONE)
						{
							if (Utils.RANDOM.nextDouble() > g.growthFactor)
								continue;

							changes[0]++;
							regionPoint.region.type = g.type;
							break;
						}
					}

				});
			}

			// threshold reached, no more can be allocated
			if (changes[0] == 0)
				break;
		}

		// replace all empties with their neighbours
		for (int i = 0; i < regions.size() - 1; i++)
		{
			Region a = regions.get(i);
			Region b = regions.get(i + 1);

			if (a.type == RegionType.NONE)
				a.type = b.type;
			else if (b.type == RegionType.NONE)
				b.type = a.type;
		}

		// add supplementary regions
		Morph[] morphs = new Morph[]{
			new Morph(RegionType.HOUSING_DENSE, RegionType.COMMERCIAL_SMALL, 0.1),
			new Morph(RegionType.HOUSING_LUXURY, RegionType.COMMERCIAL_SMALL, 0.08),
		};

		for (Morph morph : morphs)
		{
			regions.stream().filter(r -> r.type == morph.from).forEach(from ->
			{
				if (Utils.RANDOM.nextDouble() < morph.factor)
					from.type = morph.to;

			});
		}


	}

	private void placeRegions(Point2D.Double centralPoint, int pointCount, int relaxCount)
	{
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
			kdTree.add(new RegionPoint(r));
		});

		traceRegionShapes(regionGraph, regionMap);
		regions.addAll(regionMap.values());

		// sort by distance to centre
		regions.sort(Comparator.comparingDouble(r -> r.distanceFromCentre));
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
