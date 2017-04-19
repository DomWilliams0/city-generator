package ms.domwillia.city.generator;

import java.util.Random;

public class Utils
{
	public static final Random RANDOM = new Random();

	private Utils()
	{
	}

	// http://stackoverflow.com/a/23157705
	public static double scale(final double valueIn, final double baseMin, final double baseMax,
	                           final double limitMin, final double limitMax)
	{
		return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
	}
}
