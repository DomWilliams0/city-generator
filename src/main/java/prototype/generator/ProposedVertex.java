package prototype.generator;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import prototype.RoadType;
import prototype.graph.Vertex;

import java.awt.geom.Point2D;

public class ProposedVertex
{
	private Point2D.Double pos;
	private RoadType type;

	private Vertex srcVertex;

	public ProposedVertex(double x, double y, Vertex src, RoadType type)
	{
		this.pos = new Point2D.Double(x, y);
		this.type = type;
		this.srcVertex = src;
	}


	public void setPosition(double x, double y)
	{
		pos.setLocation(x, y);
	}

	public double getX()
	{
		return pos.getX();
	}

	public double getY()
	{
		return pos.getY();
	}

	public RoadType getType()
	{
		return type;
	}

	public void setType(RoadType type)
	{
		this.type = type;
	}

	public Point2D.Double getPosition()
	{
		return pos;
	}

	public Vertex getSourceVertex()
	{
		return srcVertex;
	}

	public Vector2D getDirection()
	{
		Vector2D src = new Vector2D(srcVertex.getPoint().x, srcVertex.getPoint().y);
		Vector2D dst = new Vector2D(pos.x, pos.y);

		return dst.subtract(src).normalize();
	}

	/**
	 * @return Degrees
	 */
	public double getDirectionAngle()
	{
		double radians = Math.atan2(pos.y - srcVertex.getPoint().y, pos.x - srcVertex.getPoint().x);
		return Math.toDegrees(radians);
	}

	@Override
	public String toString()
	{
		return "ProposedVertex{" +
			"pos=" + pos +
			", type=" + type +
			", srcVertex=" + srcVertex +
			'}';
	}
}
