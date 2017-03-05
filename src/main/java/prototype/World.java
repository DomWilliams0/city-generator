package prototype;

import org.apache.commons.math3.geometry.spherical.twod.Edge;
import org.apache.commons.math3.geometry.spherical.twod.S2Point;
import prototype.graph.Graph;
import prototype.graph.Vertex;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class World
{
	private static int NEXT_RENDER = 1;

	private int width, height;
	private Graph graph;

	public World(int width, int height)
	{
		this.width = width;
		this.height = height;
		this.graph = new Graph();
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	private boolean isInRange(double x, double y)
	{
		return x >= 0 && y >= 0 && x < width && y < height;
	}

	public Vertex addVertex(double x, double y)
	{
		return addVertex(x, y, RoadType.MAIN);
	}

	public Vertex addVertex(double x, double y, RoadType type)
	{
		if (isInRange(x, y))
			return graph.addVertex(x, y, type);

		return null;
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
		Map<Vertex, Set<Vertex>> edges = graph.getEdges();
		for (Map.Entry<Vertex, Set<Vertex>> e : edges.entrySet())
		{
			Vertex v = e.getKey();
			for (Vertex neighbour : e.getValue())
			{
				if (v.isMain() && neighbour.isMain())
				{
					g.setColor(Color.BLACK);
					g.setStroke(MAIN_STROKE);
				}
				else
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
		for (Vertex v : graph.getVertices())
		{
			g.fillOval(v.getIntX() - rad/2, v.getIntY() - rad/2, rad, rad);
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

	public void addEdge(Vertex a, Vertex b)
	{
		graph.addEdge(a, b);
	}
}
