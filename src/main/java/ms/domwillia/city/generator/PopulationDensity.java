package ms.domwillia.city.generator;

import ms.domwillia.city.Config;

import java.awt.geom.Point2D;

public class PopulationDensity
{
	private final int width;
	private final int height;
	private final OpenSimplexNoise noise;

	private final HotSpot[] hotspots;

	public PopulationDensity(int width, int height, int hotspotCount)
	{
		this.width = width;
		this.height = height;
		this.noise = new OpenSimplexNoise(System.nanoTime());
		this.hotspots = new HotSpot[hotspotCount];
	}

	/**
	 * @return Raw untouched value between 0 and 0.75
	 */
	private double getRawValue(double x, double y)
	{
		double old_min = -1.0;
		double old_max = 1.0;
		double new_min = 0.0;
		double new_max = 0.75;
		double scale = Config.getDouble(Config.Key.NOISE_SCALE);
		double old_value = noise.eval(x / scale, y / scale);

		double new_value = ((old_value - old_min) / (old_max - old_min)) * (new_max - new_min) + new_min;
		return Math.min(new_max, Math.max(new_min, new_value));
	}

	class HotSpot
	{
		final Point2D.Double centre;
		final double radius;

		public HotSpot(Point2D.Double centre, double radius)
		{
			this.centre = centre;
			this.radius = radius;
		}
	}

	/**
	 * @return Hotspots included
	 */
	public double getValue(double x, double y)
	{
		return getRawValue(x, y);
	}

	public double getValue(int x, int y)
	{
		return getValue((double) x, (double) y);
	}

}
