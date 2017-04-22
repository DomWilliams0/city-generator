package ms.domwillia.city.generator.util;

public class NoiseRandom
{
	private final OpenSimplexNoise random;
	private final double scale;
	private int randomIndex;

	public NoiseRandom()
	{
		this(20);
	}

	public NoiseRandom(double scale)
	{
		this.scale = scale;
		this.random = new OpenSimplexNoise(System.nanoTime());
		this.randomIndex = 0;
	}

	public double getRandom()
	{
		return Utils.scale(random.eval((double) (randomIndex += scale), 0, 0),
			-1.0, 1.0,
			0.0, 1.0);
	}

}

