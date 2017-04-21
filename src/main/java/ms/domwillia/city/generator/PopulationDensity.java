package ms.domwillia.city.generator;

import ms.domwillia.city.Config;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PopulationDensity
{
	private final int width;
	private final int height;
	private final OpenSimplexNoise noise;

	private final List<HotSpot> hotspots;

	public PopulationDensity(int width, int height)
	{
		this.width = width;
		this.height = height;
		this.noise = new OpenSimplexNoise(System.nanoTime());
		this.hotspots = new ArrayList<>();

		placeHotspots();
	}

	private HotSpot getHotSpot(List<HotSpot> hotspots, double x, double y, double radius)
	{
		// intersects other hotspot
		for (HotSpot hotspot : hotspots)
			if (doesIntersect(x, y, radius, hotspot))
				return hotspot;

		return null;
	}

	private boolean doesIntersect(double x, double y, double radius, HotSpot hotspot)
	{
		double dx = x - hotspot.centre.x;
		double dy = y - hotspot.centre.y;
		double radSum = hotspot.radius + radius;
		return dx * dx + dy * dy <= radSum * radSum;
	}

	private void placeHotspots()
	{
		double threshold = 0.75;

		List<HotSpot> tempHotspots = findInitialHotspots(threshold);

		// remove intersecting hotspots
		// TODO surely these should be filtered out above?
		List<HotSpot> bestHotspots = new ArrayList<>(tempHotspots.size());
		for (HotSpot a : tempHotspots)
		{
			boolean ignore = false;
			for (HotSpot b : tempHotspots)
			{
				if (a.hashCode() > b.hashCode() && a.intersects(b))
				{
					ignore = true;
					break;
				}
			}

			if (!ignore)
				bestHotspots.add(a);
		}

		if (bestHotspots.isEmpty())
			return;

		// find top best
		List<HotSpot> sorted = bestHotspots.stream()
//			.filter(h -> !hotspotIsOutsideWorld(h))
			.sorted((a, b) -> Double.compare(b.radius, a.radius))
			.collect(Collectors.toList());

		Iterator<HotSpot> it = sorted.iterator();
		HotSpot last = it.next();
		hotspots.add(last);

		while (it.hasNext())
		{
			HotSpot curr = it.next();

			double diff = (last.radius - curr.radius) / curr.radius;
			if (diff > 0.2 && hotspots.size() > 3)
				break;

			hotspots.add(curr);
			last = curr;
		}

	}

	private List<HotSpot> findInitialHotspots(double threshold)
	{
		List<HotSpot> out = new ArrayList<>();

		double max = 1.0;
		double radiusMax = Config.getDouble(Config.Key.NOISE_SCALE) * 2;
		double radiusMin = radiusMax / 2;

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				double raw = getRawValue(x, y, max);
				if (raw < threshold)
					continue;

				double radius = Utils.scale(raw,
					threshold, max,
					radiusMin, radiusMax);


				HotSpot existing = getHotSpot(out, x, y, radius);
				if (existing == null)
				{
					// create new hotspot here
					out.add(new HotSpot(new Point2D.Double(x, y), radius, raw));
				} else
				{
					// positioned between the 2
					if (radius > existing.radius)
					{
						existing.centre.setLocation(
							(existing.centre.x + x) / 2.,
							(existing.centre.y + y) / 2.
						);
						existing.radius = radius;
					}
				}
			}
		}

		return out;
	}

	public void debugRender(Graphics2D g)
	{
		g.setColor(Color.RED);

		for (HotSpot hotspot : hotspots)
		{
			double x = hotspot.centre.x;
			double y = hotspot.centre.y;
			int rad = (int) hotspot.radius;
			g.drawOval((int) (x - rad / 2), (int) (y - rad / 2), rad, rad);
			g.drawRect((int) x, (int) y, 1, 1);
		}

	}


	/**
	 * @return Raw untouched value between 0 and 0.75
	 */
	private double getRawValue(double x, double y, double max)
	{
		double old_min = -1.0;
		double old_max = 1.0;
		double new_min = 0.0;
		double scale = Config.getDouble(Config.Key.NOISE_SCALE);
		double old_value = noise.eval(x / scale, y / scale);

		double new_value = ((old_value - old_min) / (old_max - old_min)) * (max - new_min) + new_min;
		return Math.min(max, Math.max(new_min, new_value));
	}

	class HotSpot
	{
		Point2D.Double centre;
		double radius;
		double density;

		HotSpot(Point2D.Double centre, double radius, double density)
		{
			this.centre = centre;
			this.radius = radius;
			this.density = density;
		}

		@Override
		public String toString()
		{
			return "HotSpot{" +
				"centre=" + centre +
				", radius=" + radius +
				", density=" + density +
				'}';
		}

		boolean intersects(HotSpot hotSpot)
		{
			return doesIntersect(centre.x, centre.y, radius, hotSpot);
		}

		boolean contains(double x, double y)
		{
			double rad = radius / 2;
			return centre.distanceSq(x, y) < rad * rad;
		}
	}

	/**
	 * @return Hotspots included
	 */
	public double getValue(double x, double y)
	{
		double max = 0.5;

		for (HotSpot hotspot : hotspots)
		{
			if (hotspot.contains(x, y))
			{
				max = hotspot.density;
				break;
			}
		}


		return getRawValue(x, y, max);
	}

	public double getValue(int x, int y)
	{
		return getValue((double) x, (double) y);
	}

}
