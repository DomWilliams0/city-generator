package ms.domwillia.city.graph;

import com.jwetherell.algorithms.data_structures.KdTree;
import ms.domwillia.city.Config;
import ms.domwillia.city.RoadType;
import org.apache.commons.math3.geometry.euclidean.twod.SubLine;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class Graph
{
	private Map<Point2D, Vertex> vertices;
	private Map<Vertex, Set<Vertex>> edges;

	private int width, height;
	private KdTree<SpatialVertex> kdTree;

	public Graph(int width, int height)
	{
		if (width < 10 || height < 10)
			throw new IllegalArgumentException("Invalid graph size");

		this.width = width;
		this.height = height;
		this.vertices = new HashMap<>();
		this.edges = new HashMap<>();
		this.kdTree = new KdTree<>();
	}

	public Vertex addVertex(double x, double y, RoadType type)
	{
		return addVertex(new Point2D.Double(x, y), type);
	}

	public Vertex addVertex(Point2D.Double point, RoadType type)
	{
		// bad coords
		if (!isInRange(point.x, point.y))
			throw new IllegalArgumentException("Vertex out of range (" + point.x + ", " + point.y + ")");

		// fetch existing
		Vertex v = vertices.get(point);

		if (v == null)
		{
			v = new Vertex(point.x, point.y, type);
			vertices.put(point, v);

			kdTree.add(new SpatialVertex(point.x, point.y));
		}

		return v;
	}

	public boolean hasVertex(double x, double y)
	{
		return hasVertex(new Point2D.Double(x, y));
	}

	public boolean hasVertex(Point2D.Double point)
	{
		return vertices.containsKey(point);
	}

	public Collection<Vertex> getVertices()
	{
		return vertices.values();
	}

	public Map<Vertex, Set<Vertex>> getEdges()
	{
		return edges;
	}

	public void addEdge(Vertex a, Vertex b)
	{
		Set<Vertex> vertices = edges.computeIfAbsent(a, k -> new HashSet<>());
		vertices.add(b);
	}

	public void removeEdge(Vertex a, Vertex b)
	{
		Set<Vertex> vertices = edges.get(a);
		if (vertices != null)
			vertices.remove(b);

	}

	public boolean isInRange(double x, double y)
	{
		return x >= 0 && y >= 0 && x < width && y < height;
	}

	private void drawOval(Graphics2D g, Point2D.Double point, int radius, boolean fill)
	{
		if (fill)
			g.fillOval((int) point.getX() - radius / 2, (int) (point.getY() - radius / 2), radius, radius);
		else
			g.drawOval((int) point.getX() - radius / 2, (int) (point.getY() - radius / 2), radius, radius);
	}

	public void render(Graphics2D g)
	{
		// vertices
		g.setColor(Config.getColour(Config.Key.VERTEX_RENDER_COLOUR));
		for (Vertex v : vertices.values())
		{
			drawOval(g, v.getPoint(), Config.getInt(Config.Key.VERTEX_RENDER_RADIUS), true);
		}

		Stroke mainStroke = new BasicStroke(Config.getInt(Config.Key.ROAD_MAIN_RENDER_THICKNESS));
		Stroke minorStroke = new BasicStroke(Config.getInt(Config.Key.ROAD_MINOR_RENDER_THICKNESS));

		// edges
		for (Map.Entry<Vertex, Set<Vertex>> e : edges.entrySet())
		{
			Vertex v = e.getKey();
			for (Vertex neighbour : e.getValue())
			{
				if (v.isMain() && neighbour.isMain())
				{
					g.setColor(Config.getColour(Config.Key.ROAD_MAIN_RENDER_COLOUR));
					g.setStroke(mainStroke);
				} else
				{
					g.setColor(Config.getColour(Config.Key.ROAD_MINOR_RENDER_COLOUR));
					g.setStroke(minorStroke);
				}

				Point2D.Double vp = v.getPoint();
				Point2D.Double np = neighbour.getPoint();
				g.drawLine((int) vp.x, (int) vp.y, (int) np.x, (int) np.y);

			}
		}
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}


	public void scaleAndSubdivide(int factor, int subdivisions)
	{
		width *= factor;
		height *= factor;

		Map<Vertex, Set<Vertex>> edgesCopy = new LinkedHashMap<>(edges);

		edges.clear();
		vertices.clear();
		for (SpatialVertex vertex : kdTree)
			kdTree.remove(vertex);

		edgesCopy.forEach((srcVertex, value) ->
		{
			Point2D.Double src = new Point2D.Double(srcVertex.getPoint().x * factor, srcVertex.getPoint().y * factor);

			Vector2D self = new Vector2D(src.x, src.y);
			value.forEach(neighbour ->
			{
				Vector2D neighbourPos = new Vector2D(neighbour.getPoint().x * factor, neighbour.getPoint().y * factor);
				Vector2D direction = neighbourPos.subtract(self).normalize();

				double length = self.distance(neighbourPos);
				double each = length / subdivisions;

				Vertex[] newVertices = new Vertex[subdivisions + 1];
				for (int i = 0; i <= subdivisions; i++)
				{
					Vector2D newPos = direction.scalarMultiply(each * i).add(self);
					Vertex next = addVertex(newPos.getX(), newPos.getY(), srcVertex.getType());
					newVertices[i] = next;
				}

				for (int i = 0; i < newVertices.length - 1; i++)
				{
					addEdge(newVertices[i], newVertices[i + 1]);
				}
			});
		});
	}

	public Point2D[] getNearestNeighbours(int k, Point2D point)
	{
		SpatialVertex value = new SpatialVertex(point.getX(), point.getY());
		Collection<SpatialVertex> result = kdTree.nearestNeighbourSearch(k, value);
		return result.stream()
			.map(v -> new Point2D.Double(v.getX(), v.getY()))
			.toArray(Point2D.Double[]::new);
	}


	public void removeIntersectingEdges()
	{
		List<EdgeLine> lines = new ArrayList<>();

		// collect lines
		edges.forEach((src, dsts) ->
			dsts.forEach(dst ->
				lines.add(new EdgeLine(src, dst))));

		lines.forEach(a ->
		{
			if (a.removed)
				return;

			lines.forEach(b ->
			{
				if (a.hashCode() <= b.hashCode() || b.removed)
					return;

				boolean remove = false;

				Vector2D intersection = a.line.intersection(b.line, false);
				if (intersection != null)
				{
					// intersects
					remove = true;
				}
				else
				{
					// too close together
					Point2D.Double aMid = new Point2D.Double(
						(a.src.getPoint().x + a.dst.getPoint().x) / 2,
						(a.src.getPoint().y + a.dst.getPoint().y) / 2
					);
					Point2D.Double bMid = new Point2D.Double(
						(b.src.getPoint().x + b.dst.getPoint().x) / 2,
						(b.src.getPoint().y + b.dst.getPoint().y) / 2
					);

					double distance = aMid.distance(bMid);
					if (distance > 0 && distance < 20)
						remove = true;
				}

				if (remove)
				{
					removeEdge(b.src, b.dst);
					b.removed = true;
				}
			});
		});

	}

	private class EdgeLine
	{
		SubLine line;
		Vertex src, dst;
		boolean removed;

		EdgeLine(Vertex src, Vertex dst)
		{
			this.src = src;
			this.dst = dst;
//			this.line = new Line2D.Double(src.getPoint(), dst.getPoint());
			this.line = new SubLine(
				new Vector2D(src.getPoint().x, src.getPoint().y),
				new Vector2D(dst.getPoint().x, dst.getPoint().y),
				0.0000001
			);
			this.removed = false;
		}
	}


	private class SpatialVertex extends KdTree.XYZPoint
	{
		SpatialVertex(double x, double y)
		{
			super(x, y);
		}
	}

}
