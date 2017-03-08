package prototype.graph;

import prototype.Config;
import prototype.RoadType;
import prototype.generator.Density;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Graph
{
	private Map<Point2D, Vertex> vertices;
	private Map<Vertex, Set<Vertex>> edges;

	private int width, height;

	public Graph(int width, int height)
	{
		if (width < 10 || height < 10)
			throw new IllegalArgumentException("Invalid graph size");

		this.width = width;
		this.height = height;
		this.vertices = new HashMap<>();
		this.edges = new HashMap<>();
	}

	public Vertex addVertex(double x, double y, RoadType type)
	{
		// bad coords
		if (!isInRange(x, y))
			throw new IllegalArgumentException("Vertex out of range (" + x + ", " + y + ")");

		// fetch existing
		Point2D.Double point = new Point2D.Double(x, y);
		Vertex v = vertices.get(point);

		if (v == null)
		{
			v = new Vertex(x, y, type);
			vertices.put(point, v);
		} else if (v.getType() != type)
		{
			System.err.println("Mismatching type of vertex: " + v);
		}

		return v;
	}


	public boolean hasVertex(double x, double y)
	{
		return vertices.containsKey(new Point2D.Double(x, y));
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

	public BufferedImage render()
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		// noise
		if (Config.getBoolean(Config.Key.RENDER_NOISE))
		{
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					double noise = Density.getValue(x, y);
					int pixel = (int) (noise * 255);
					image.setRGB(x, y, new Color(pixel, pixel, pixel).getRGB());
				}
			}
		}

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

				// green start, pink end
				g.setColor(Color.GREEN);
				drawOval(g, vp, 2, false);
				g.setColor(Color.PINK);
				drawOval(g, np, 2, false);

			}
		}

		return image;
	}

	public void export(String dir, String nameFormat, int index)
	{
		File out = Paths.get(dir, String.format(nameFormat, index)).toFile();

		File parent = out.getParentFile();
		if (!parent.exists())
			parent.mkdir();

		out.delete();

		BufferedImage image = render();
		synchronized (Graph.class)
		{
			try
			{
				ImageIO.write(image, "png", out);
				System.out.printf("%d: exported to '%s'\n", Thread.currentThread().getId(), out.getAbsolutePath());
			} catch (IOException e)
			{
				e.printStackTrace();
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
}
