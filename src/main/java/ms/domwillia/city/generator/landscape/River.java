package ms.domwillia.city.generator.landscape;

import ms.domwillia.city.Config;
import ms.domwillia.city.generator.Density;
import ms.domwillia.city.generator.util.NoiseRandom;
import ms.domwillia.city.generator.util.Utils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class River
{
	private final Landscape landscape;
	private List<Point2D.Double> riverPoints;
	private List<Point2D.Double> riverPointsInterpolated;
	private Path2D.Double riverPath;

	/**
	 * @param minPoints      Retry until the river has this many points
	 * @param scanAngle      The angle to use when scanning for the next river point (on each side, and in radians)
	 * @param scanRangeScale Distance between each river point - factor of the NOISE_SCALE
	 * @param sampleCount    The number of samples to take to choose the next river point
	 */
	public River(Landscape landscape, double minPoints, double scanAngle, double scanRangeScale, int sampleCount)
	{
		this.landscape = landscape;
		this.riverPoints = new ArrayList<>();
		this.riverPointsInterpolated = new ArrayList<>();
		generateRiver(minPoints, scanAngle, scanRangeScale, sampleCount);
	}

	private void generateRiver(double minPoints, double scanAngle, double scanRangeScale, int sampleCount)
	{
		double noise = Config.getDouble(Config.Key.NOISE_SCALE);

		int maxAttempts = 500;
		do
		{
			placeRivers(scanAngle, noise * scanRangeScale, sampleCount);

			// success
			if (riverPoints.size() >= minPoints &&
				isValid())
				break;

			// too many failures
			if (maxAttempts-- < 0)
				break;

			// restart
			riverPoints.clear();
		} while (true);

		if (maxAttempts < 0)
			System.err.println("Aborted river generation after too many tries, settling with " + riverPoints.size());

		final int scale = 3;

		double[] indices = new double[riverPoints.size()];
		double[] xs = new double[riverPoints.size()];
		double[] ys = new double[riverPoints.size()];
		for (int i = 0, riverSize = riverPoints.size(); i < riverSize; i++)
		{
			Point2D.Double p = riverPoints.get(i);
			xs[i] = p.x;
			ys[i] = p.y;
			indices[i] = i * scale;
		}

		UnivariateInterpolator interpolator = new AkimaSplineInterpolator();
		UnivariateFunction splineX = interpolator.interpolate(indices, xs);
		UnivariateFunction splineY = interpolator.interpolate(indices, ys);

		for (int i = 0; i < xs.length - 1; i++)
		{
			double index = indices[i];

			for (int j = 0; j < scale; j++)
			{
				Point2D.Double newPoint = new Point2D.Double(
					splineX.value(index + j), splineY.value(index + j)
				);
				riverPointsInterpolated.add(newPoint);
			}

		}

		riverPath = new Path2D.Double();
		for (int i = 0, pointCount = riverPointsInterpolated.size() - 1; i < pointCount; i++)
		{
			Point2D.Double curr = riverPointsInterpolated.get(i);
			Point2D.Double next = riverPointsInterpolated.get(i + 1);
			riverPath.moveTo(curr.x, curr.y);
			riverPath.lineTo(next.x, next.y);
		}
	}

	private boolean isValid()
	{
		Point2D.Double first = riverPoints.get(0);
		Point2D.Double last = riverPoints.get(riverPoints.size() - 1);

		double dst = first.distanceSq(last);

		double minDim = Math.min(landscape.getWidth(), landscape.getHeight());
		return dst >= (minDim * minDim);
	}

	private void placeRivers(double scanAngle, double scanRange, int sampleCount)
	{
		Density density = new Density(10);
		NoiseRandom rand = new NoiseRandom();

		// random seed point
		Point2D.Double pos = new Point2D.Double();
		Vector2D direction = generateRiverSeed(pos);

		Point2D.Double bestPoint = new Point2D.Double();
		double minDensity;

		while (true)
		{
			minDensity = Double.MAX_VALUE;

			// add this point
			riverPoints.add(new Point.Double(pos.x, pos.y));

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
				pos.x >= landscape.getWidth() || pos.y >= landscape.getHeight())
				break;
		}

		riverPoints.add(new Point.Double(bestPoint.x, bestPoint.y));
	}

	private Vector2D generateRiverSeed(Point2D.Double pos)
	{
		pos.setLocation(
			Utils.RANDOM.nextDouble() * landscape.getWidth(),
			Utils.RANDOM.nextDouble() * landscape.getHeight()
		);

		Vector2D direction = null;

		switch (Utils.RANDOM.nextInt(4))
		{
			// top
			case 0:
				pos.y = 0;
				direction = new Vector2D(0, 1);
				break;

			// left
			case 1:
				pos.x = 0;
				direction = new Vector2D(1, 0);
				break;

			// bottom
			case 2:
				pos.y = landscape.getHeight();
				direction = new Vector2D(0, -1);
				break;

			// right
			case 3:
				pos.x = landscape.getWidth();
				direction = new Vector2D(-1, 0);
				break;
		}

		return direction;
	}

	public void render(Graphics2D g)
	{
		int rad = 4;

		g.setColor(new Color(36, 103, 247));
		g.setStroke(new BasicStroke(30, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(riverPath);

		g.setStroke(new BasicStroke(1));

		g.setColor(Color.RED);
		for (Point2D.Double p : riverPointsInterpolated)
			g.fillOval((int) (p.x - rad / 2), (int) (p.y - rad / 2), rad, rad);

		g.setColor(Color.BLACK);
		for (Point2D.Double p : riverPoints)
			g.fillOval((int) (p.x - rad / 2), (int) (p.y - rad / 2), rad, rad);

	}

	public Point2D.Double getCentralPoint()
	{
		int index = Utils.RANDOM.nextInt(riverPoints.size() / 2) + (riverPoints.size() / 4);
		return riverPoints.get(index);
	}

	public List<Point2D.Double> getPoints()
	{
		return riverPoints;
	}
}
