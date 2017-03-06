package prototype.graph;

import prototype.RoadType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Graph
{
	private static int NEXT_RENDER = 1;

	private Map<Point2D, Vertex> vertices;
	private Map<Vertex, Set<Vertex>> edges;

	private int width, height;

	public Graph(int width, int height)
	{
		this.width = width;
		this.height = height;
		this.vertices = new HashMap<>();
		this.edges = new HashMap<>();
	}

	public Vertex addVertex(double x, double y, RoadType type)
	{
		// bad coords
		if (!isInRange(x, y))
			return null;

		// fetch existing
		Point2D.Double point = new Point2D.Double(x, y);
		Vertex v = vertices.get(point);

		if (v == null)
		{
			v = new Vertex(x, y, type);
			vertices.put(point, v);
		}
		else if (v.getType() != type)
		{
			System.err.println("Mismatching type of vertex: " + v);
		}

		return v;
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
	private boolean isInRange(double x, double y)
	{
		return x >= 0 && y >= 0 && x < width && y < height;
	}

	private void render(File outFile)
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		// background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		Stroke MAIN_STROKE = new BasicStroke(1);
		Stroke MINOR_STROKE = new BasicStroke(1);

		// edges
		for (Map.Entry<Vertex, Set<Vertex>> e : edges.entrySet())
		{
			Vertex v = e.getKey();
			for (Vertex neighbour : e.getValue())
			{
				if (v.isMain() && neighbour.isMain())
				{
					g.setColor(Color.BLACK);
					g.setStroke(MAIN_STROKE);
				} else
				{
					g.setColor(Color.BLUE);
					g.setStroke(MINOR_STROKE);
				}

				g.drawLine(v.getIntX(), v.getIntY(), neighbour.getIntX(), neighbour.getIntY());
			}
		}

		// vertices
		g.setColor(Color.RED);
		g.setStroke(MINOR_STROKE);
		int rad = 5;
		for (Vertex v : vertices.values())
		{
			g.fillOval(v.getIntX() - rad / 2, v.getIntY() - rad / 2, rad, rad);
		}

		try
		{
			ImageIO.write(image, "png", outFile);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void export()
	{
		File out = new File("/tmp/road-renders/render-" + NEXT_RENDER++ + ".png");

		File parent = out.getParentFile();
		if (!parent.exists())
			parent.mkdir();

		out.delete();

		render(out);
	}
}
