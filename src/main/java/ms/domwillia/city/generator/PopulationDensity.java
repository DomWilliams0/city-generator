package ms.domwillia.city.generator;

import ms.domwillia.city.Config;
import org.apache.commons.math3.util.MathUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PopulationDensity
{
	private final int width;
	private final int height;
	private final OpenSimplexNoise noise;

	private final List<PopulationHotspot> hotspots;

	public PopulationDensity(int width, int height)
	{
		this.width = width;
		this.height = height;
		this.noise = new OpenSimplexNoise(System.nanoTime());
		this.hotspots = new ArrayList<>();

		placeHotspots();
	}

	private PopulationHotspot getHotSpot(List<PopulationHotspot> hotspots, double x, double y, double radius)
	{
		// intersects other hotspot
		for (PopulationHotspot hotspot : hotspots)
			if (hotspot.intersects(x, y, radius))
				return hotspot;

		return null;
	}

	public List<PopulationHotspot> getHotspots()
	{
		return hotspots;
	}

	private void placeHotspots()
	{
		double threshold = 0.75;

		List<PopulationHotspot> tempHotspots = findInitialHotspots(threshold);

		// remove intersecting hotspots
		// TODO surely these should be filtered out above?
		List<PopulationHotspot> bestHotspots = new ArrayList<>(tempHotspots.size());
		for (PopulationHotspot a : tempHotspots)
		{
			boolean ignore = false;
			for (PopulationHotspot b : tempHotspots)
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
		List<PopulationHotspot> sorted = bestHotspots.stream()
//			.filter(h -> !hotspotIsOutsideWorld(h))
			.sorted((a, b) -> Double.compare(b.radius, a.radius))
			.collect(Collectors.toList());

		Iterator<PopulationHotspot> it = sorted.iterator();
		PopulationHotspot last = it.next();
		hotspots.add(last);

		while (it.hasNext())
		{
			PopulationHotspot curr = it.next();

			double diff = (last.radius - curr.radius) / curr.radius;
			if (diff > 0.2 && hotspots.size() > 3)
				break;

			hotspots.add(curr);
			last = curr;
		}

	}

	private List<PopulationHotspot> findInitialHotspots(double threshold)
	{
		List<PopulationHotspot> out = new ArrayList<>();
		double noiseScale = Config.getDouble(Config.Key.NOISE_SCALE);

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				double raw = getRawValue(x, y, 1.0);
				if (raw < threshold)
					continue;

				double radius = calculateRadius(x, y, noiseScale);

				PopulationHotspot existing = getHotSpot(out, x, y, radius);
				if (existing == null)
				{
					// create new hotspot here
					out.add(new PopulationHotspot(new Point2D.Double(x, y), radius, raw));
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

	/**
	 * @return Average some random points within the given distance to
	 * calculate the radius
	 */
	private double calculateRadius(int x, int y, double distanceToAverageIn)
	{
		// for avg
		double total = 0;
		int count = 5;

		for (int i = 0; i < count; i++)
		{
			double r1 = distanceToAverageIn * Utils.RANDOM.nextDouble();
			double r2 = distanceToAverageIn * Utils.RANDOM.nextDouble();

			double rx = x + r2 * Math.cos(MathUtils.TWO_PI * r1 / r2);
			double ry = y + r2 * Math.sin(MathUtils.TWO_PI * r1 / r2);

			total += getRawValue(rx, ry, 1.0);
		}

		double avg = total / count;
		return avg * distanceToAverageIn;
	}

	void debugRender(Graphics2D g)
	{
		g.setColor(Color.RED);

		for (PopulationHotspot hotspot : hotspots)
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

	/**
	 * @return Hotspots included
	 */
	public double getValue(double x, double y)
	{
		double max = 0.5;

		for (PopulationHotspot hotspot : hotspots)
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
