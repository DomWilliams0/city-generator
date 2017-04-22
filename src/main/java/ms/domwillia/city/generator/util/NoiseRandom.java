package ms.domwillia.city.generator.util;

public class NoiseRandom
{
	private final OpenSimplexNoise random;
	private final double scale;
	private double randomIndex;

	public NoiseRandom()
	{
		this(0.5);
	}

	public NoiseRandom(double scale)
	{
		this.scale = scale;
		this.random = new OpenSimplexNoise(System.nanoTime());
		this.randomIndex = 0;
	}

	public double getRandom()
	{
		return Utils.scale(random.eval(randomIndex += scale, 0, 0),
			-1.0, 1.0,
			0.0, 1.0);
	}

}

