package ms.domwillia.city.generator;

import ms.domwillia.city.Config;

public class Density
{
	private final int width;
	private final int height;
	private final OpenSimplexNoise noise;

	public Density(int width, int height)
	{
		this.width = width;
		this.height = height;
		this.noise = new OpenSimplexNoise(System.nanoTime());
	}

	/**
	 * @return Raw untouched value between 0 and the given max
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
	 * @return Tweaked value
	 */
	public double getValue(double x, double y)
	{
		return getRawValue(x, y, 1.0);
	}

	public double getValue(int x, int y)
	{
		return getValue((double) x, (double) y);
	}

}
