package ms.domwillia.city.generator;

import ms.domwillia.city.Config;
import ms.domwillia.city.generator.util.NoiseRandom;
import ms.domwillia.city.generator.util.Utils;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Landscape
{
	private final int width;
	private final int height;

	private List<Point2D.Double> river = new ArrayList<>();
	private Path2D.Double riverPath;

	public Landscape(int width, int height)
	{
		this.width = width;
		this.height = height;

	}

	/**
	 * @param minPoints      Retry until the river has this many points
	 * @param scanAngle      The angle to use when scanning for the next river point (on each side, and in radians)
	 * @param scanRangeScale Distance between each river point - factor of the NOISE_SCALE
	 * @param sampleCount    The number of samples to take to choose the next river point
	 */
	public void generateRiver(double minPoints, double scanAngle, double scanRangeScale, int sampleCount)
	{
		double noise = Config.getDouble(Config.Key.NOISE_SCALE);

		int maxAttempts = 500;
		while (river.size() < minPoints && maxAttempts-- > 0)
		{
			river.clear();
			placeRivers(scanAngle, noise * scanRangeScale, sampleCount);
		}

		if (maxAttempts < 0)
			System.err.println("Aborted river generation after too many tries");

		riverPath = new Path2D.Double();
		for (int i = 0, riverSize = river.size() - 1; i < riverSize; i++)
		{
			Point2D.Double curr = river.get(i);
			Point2D.Double next = river.get(i + 1);
			riverPath.moveTo(curr.x, curr.y);
			riverPath.lineTo(next.x, next.y);
		}
	}

	private void placeRivers(double scanAngle, double scanRange, int sampleCount)
	{
		Density density = new Density(20);
		NoiseRandom rand = new NoiseRandom(10);

		// random seed point
		// TODO generate on any side
		Point2D.Double pos = new Point2D.Double(
			Utils.RANDOM.nextDouble() * width,
			0
		);
		Vector2D direction = new Vector2D(0, 1); // down

		Point2D.Double bestPoint = new Point2D.Double();
		double minDensity;

		while (true)
		{
			minDensity = Double.MAX_VALUE;

			// add this point
			river.add(new Point.Double(pos.x, pos.y));

			// scan ahead in for best next point
			for (int i = 0; i < sampleCount; i++)
			{
				double currentAngle = Math.atan2(direction.getY(), direction.getX());

				double searchAngle = currentAngle + (rand.getRandom() * scanAngle * 2) - scanAngle;

				Point2D.Double check = new Point2D.Double(
					pos.x + scanRange * Math.cos(searchAngle),
					pos.y + scanRange * Math.sin(searchAngle)
				);

				double value = density.getValue(check.x, check.y);
				if (value < minDensity)
				{
					minDensity = value;
					bestPoint.setLocation(check);
				}
			}

			// move on
			direction = new Vector2D(bestPoint.x - pos.x, bestPoint.y - pos.y);
			pos.setLocation(bestPoint);

			// out of range
			if (pos.x < 0 || pos.y < 0 ||
				pos.x >= width || pos.y >= height)
				break;
		}

		river.add(new Point.Double(bestPoint.x, bestPoint.y));
	}

	void render(Graphics2D g)
	{
		g.setColor(new Color(39, 141, 247));
		g.setStroke(new BasicStroke(8));
		g.draw(riverPath);


		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));

		int rad = 3;
		for (int i = 0; i < river.size(); i++)
		{
			Point2D.Double p = river.get(i);

			g.fillOval((int) (p.x - rad / 2), (int) (p.y - rad / 2), rad, rad);
//			g.drawRect((int) p.x, (int) p.y, 1, 1);
		}

	}
}
