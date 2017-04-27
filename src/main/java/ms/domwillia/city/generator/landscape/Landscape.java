package ms.domwillia.city.generator.landscape;

import de.alsclo.voronoi.Voronoi;
import de.alsclo.voronoi.graph.Graph;
import ms.domwillia.city.generator.Density;
import ms.domwillia.city.generator.RegionType;
import ms.domwillia.city.generator.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Landscape
{
	private final int width;
	private final int height;

	private River river;

	private List<Region> regions;

	public Landscape(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public void generateRiver(double minPoints, double scanAngle, double scanRangeScale, int sampleCount)
	{
		river = new River(this, minPoints, scanAngle, scanRangeScale, sampleCount);
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

		final int commScale = 30;
		final int densScale = 150;
		Density commercialisationScale = new Density(commScale);
		Density densityScale = new Density(densScale);

		regionGraph.getSitePoints().forEach(p ->
		{
			Region r = new Region();
			r.centre = new Point2D.Double(p.x, p.y);
			r.area = new Polygon();
			regionMap.put(p, r);

			double comm = commercialisationScale.getValue(p.x, p.y);
			double dens = densityScale.getValue(p.x, p.y);

			final double LOW = 0.5;
			final double MED = 0.75;
			if (comm < LOW)
			{
				if (dens < LOW)
					r.type = RegionType.RURAL;
				else if (dens < MED)
					r.type = RegionType.HOUSING_LUXURY;
				else
					r.type = RegionType.HOUSING_DENSE;
			} else if (comm < MED)
			{
				if (dens < LOW)
					r.type = RegionType.COMMERCIAL_SMALL;
				else if (dens < MED)
					r.type = RegionType.COMMERCIAL_LARGE;
				else
					r.type = RegionType.SERVICES;
			} else
			{
				if (dens < LOW)
					r.type = RegionType.INDUSTRIAL;
				else if (dens < MED)
					r.type = RegionType.COMMERCIAL_LARGE;
				else
					r.type = RegionType.METROPOLITAN;
			}

		});

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

	public void render(Graphics2D g)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int rad = 4;

		// regions
		regions.forEach(r ->
		{
			Color color = null;
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


		// river
		river.render(g);
	}

	private class Region
	{
		Point2D.Double centre;
		Polygon area;
		RegionType type;
	}
}
