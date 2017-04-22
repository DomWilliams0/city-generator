package ms.domwillia.city.generator;

import ms.domwillia.city.Config;
import ms.domwillia.city.generator.util.OpenSimplexNoise;
import ms.domwillia.city.generator.util.Utils;

public class Density
{
	private final OpenSimplexNoise noise;
	private final double scale;

	private final OpenSimplexNoise random;
	private int randomIndex;

	public Density()
	{
		this(Config.getDouble(Config.Key.NOISE_SCALE));
	}

	public Density(double scale)
	{
		long seed = System.nanoTime();
		this.scale = scale;
		this.noise = new OpenSimplexNoise(seed);
		this.random = new OpenSimplexNoise(seed + 1);
		this.randomIndex = 0;
	}

	/**
	 * @return Raw untouched value between 0 and the given max
	 */
	private double getRawValue(double x, double y, double max)
	{
		double old_min = -1.0;
		double old_max = 1.0;
		double new_min = 0.0;
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

	public double getRandom()
	{
		return Utils.scale(random.eval((double) (randomIndex++), 0, 0),
			-1.0, 1.0,
			0.0, 1.0);
	}

}
