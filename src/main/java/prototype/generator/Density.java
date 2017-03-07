package prototype.generator;

import prototype.Config;
import prototype.OpenSimplexNoise;

public class Density
{
	private static OpenSimplexNoise NOISE;

	static
	{
		reseed();
	}

	private Density()
	{
	}

	public static void reseed()
	{
		NOISE = new OpenSimplexNoise(System.nanoTime());
	}

	public static double getValue(int x, int y)
	{
		return getValue((double) x, (double) y);
	}

	public static double getValue(double x, double y)
	{
		double old_min = -1.0;
		double old_max = 1.0;
		double new_min = 0.0;
		double new_max = 1.0;
		double scale = Config.getDouble(Config.Key.NOISE_SCALE);
		double old_value = NOISE.eval(x / scale, y / scale);

		double new_value = ( (old_value - old_min) / (old_max - old_min) ) * (new_max - new_min) + new_min;
		return Math.min(new_max, Math.max(new_min, new_value));
	}
}
