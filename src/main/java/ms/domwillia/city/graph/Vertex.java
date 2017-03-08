package ms.domwillia.city.graph;

import ms.domwillia.city.RoadType;

import java.awt.geom.Point2D;

public class Vertex
{
	private Point2D.Double pos;
	private RoadType type;

	public Vertex(Point2D.Double pos, RoadType type)
	{
		this.pos = pos;
		this.type = type;
	}

	public Vertex(double x, double y, RoadType type)
	{
		this(new Point2D.Double(x, y), type);
	}

	public RoadType getType()
	{
		return type;
	}

	public boolean isMain() { return type == RoadType.MAIN; }

	public Point2D.Double getPoint()
	{
		return pos;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Vertex vertex = (Vertex) o;
		return pos.equals(vertex.pos);
	}

	@Override
	public int hashCode()
	{
		return pos.hashCode();
	}

	@Override
	public String toString()
	{
		return "Vertex{" +
			"pos=" + pos +
			", type=" + type +
			'}';
	}
}
