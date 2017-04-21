package ms.domwillia.city.generator;

import java.awt.geom.Point2D;

public class PopulationHotspot
{
	Point2D.Double centre;
	double radius;
	double density;

	PopulationHotspot(Point2D.Double centre, double radius, double density)
	{
		this.centre = centre;
		this.radius = radius;
		this.density = density;
	}

	@Override
	public String toString()
	{
		return "PopulationHotspot{" +
			"centre=" + centre +
			", radius=" + radius +
			", density=" + density +
			'}';
	}

	boolean intersects(PopulationHotspot other)
	{
		return other.intersects(centre.x, centre.y, radius);
	}

	boolean intersects(double x, double y, double radius)
	{
		double dx = x - centre.x;
		double dy = y - centre.y;
		double radSum = radius + radius;
		return dx * dx + dy * dy <= radSum * radSum;
	}

	boolean contains(double x, double y)
	{
		double rad = radius / 2;
		return centre.distanceSq(x, y) < rad * rad;
	}
}
