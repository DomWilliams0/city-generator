package ms.domwillia.city.generator;

import ms.domwillia.city.Config;
import ms.domwillia.city.RoadType;
import ms.domwillia.city.generator.rules.GridRule;
import ms.domwillia.city.graph.Graph;
import ms.domwillia.city.graph.Vertex;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class Generator
{
	private Landscape landscape;
	private Graph graph;
	private Density density;

	private boolean generated;
	private Queue<ProposedVertex> frontier;

	private GridRule rule;

	public Generator(int width, int height)
	{
		this.graph = new Graph(width, height);
		this.frontier = new ArrayDeque<>();
		this.rule = new GridRule();
		this.generated = false;
	}

	private void generate(Collection<ProposedVertex> initialFrontier)
	{
		frontier.addAll(initialFrontier);

		List<ProposedVertex> proposed = new ArrayList<>();

		while (!frontier.isEmpty())
		{
			ProposedVertex vertex = frontier.poll();

			if (acceptLocalConstraints(vertex))
			{
				// may have been tweaked out of range
				if (!graph.isInRange(vertex.getX(), vertex.getY()))
					continue;

				Vertex newlyAdded = graph.addVertex(vertex.getX(), vertex.getY(), vertex.getType());
				graph.addEdge(newlyAdded, vertex.getSourceVertex());

				if (vertex.shouldProposeMore())
				{
					proposed.clear();
					produceWithGlobalGoals(vertex, newlyAdded, proposed);
					frontier.addAll(proposed);
				}
			}
		}
	}

	private void produceWithGlobalGoals(ProposedVertex src, Vertex srcNewlyAdded, List<ProposedVertex> proposed)
	{
		rule.suggestVertices(density, src, srcNewlyAdded, proposed);
	}

	/**
	 * @return True if to be accepted
	 */
	private boolean acceptLocalConstraints(ProposedVertex vertex)
	{
		// out of range
		if (!graph.isInRange(vertex.getX(), vertex.getY()))
			return false;

		// already exists
		if (graph.hasVertex(vertex.getX(), vertex.getY()))
			return false;

		// merge with nearby
		double mergeThreshold = Config.getDouble(vertex.getType() == RoadType.MAIN ?
			Config.Key.MAIN_MERGE_THRESHOLD : Config.Key.MINOR_MERGE_THRESHOLD);
		Point2D toMerge = findClosestVertex(vertex.getPosition(), mergeThreshold,
			vertex.getPosition(), vertex.getSourceVertex().getPoint());
		if (toMerge != null)
		{
			vertex.setPosition(toMerge.getX(), toMerge.getY());
			vertex.setShouldProposeMore(false);
		}

		return true;
	}

	private Point2D findClosestVertex(Point2D pos, double threshold, Point2D.Double moi, Point2D.Double moiSrc)
	{
		Point2D[] nearest = graph.getNearestNeighbours(3, pos);
		double thresholdSq = threshold * threshold;

		for (Point2D p : nearest)
		{
			if (p.equals(moi) || p.equals(moiSrc))
				continue;

			double distance = p.distanceSq(pos);
			if (distance <= thresholdSq)
				return p;
		}

		return null;
	}

	public void generate()
	{
		generated = true;

		// reseed density function
		density = new Density();

		// create landscape
		landscape = new Landscape(graph.getWidth(), graph.getHeight());

		// create a river
		landscape.generateRiver(
			25,
			Math.PI / 4,
			0.5,
			5
		);


//
//		int maxTries = 60;
//		do
//		{
//			// main roads first
//			Collection<ProposedVertex> initialFrontier = new ArrayList<>();
//			initMainFrontier(initialFrontier);
//
//			generate(initialFrontier);
//			graph.scaleAndSubdivide(
//				Config.getInt(Config.Key.MAIN_ROAD_SCALE_FACTOR),
//				Config.getInt(Config.Key.MAIN_ROAD_SUBDIVIDE_COUNT)
//			);
//
//			// minor roads
//			initialFrontier.clear();
//			initMinorFrontier(initialFrontier);
//			generate(initialFrontier);
//		}
//		while (--maxTries > 0 && graph.getVertices().size() < Config.getInt(Config.Key.MINIMUM_VERTICES));
//
//		if (maxTries < 0)
//			System.err.println("Total failure");


	}

	private void initMinorFrontier(Collection<ProposedVertex> initialFrontier)
	{
		// add normal of all straight roads
		graph.getEdges().entrySet().stream().filter((e) -> e.getKey().isMain()).forEach(e ->
		{
			Vertex v = e.getKey();
			Set<Vertex> neighbours = e.getValue();

			if (neighbours.size() != 1)
				return;

			Vertex n = neighbours.iterator().next();

			Vector2D vPos = new Vector2D(v.getPoint().x, v.getPoint().y);
			Vector2D nPos = new Vector2D(n.getPoint().x, n.getPoint().y);
			double angle = Math.atan2(nPos.getY() - vPos.getY(), nPos.getX() - vPos.getX());
			double length = vPos.distance(nPos);

			// perpendicular
			angle += Math.PI / 2;

			double ax = vPos.getX() + (Math.cos(angle) * length);
			double ay = vPos.getY() + (Math.sin(angle) * length);

			double bx = vPos.getX() - (Math.cos(angle) * length);
			double by = vPos.getY() - (Math.sin(angle) * length);

			initialFrontier.add(new ProposedVertex(ax, ay, v, RoadType.MINOR));
			initialFrontier.add(new ProposedVertex(bx, by, v, RoadType.MINOR));
		});
	}

	private void initMainFrontier(Collection<ProposedVertex> initialFrontier)
	{
		// add single reference vertex
		Vertex ref = graph.addVertex(graph.getWidth() / 2, graph.getHeight() / 2, RoadType.MAIN);

		double length = Config.getDouble(Config.Key.MAIN_ROAD_LENGTH);
		ProposedVertex a = new ProposedVertex(ref.getPoint().getX(), ref.getPoint().getY() + length, ref, RoadType.MAIN);

		initialFrontier.add(a);
	}

	public BufferedImage render()
	{
		int width = graph.getWidth();
		int height = graph.getHeight();

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		// background
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		if (!generated)
			return image;

		// noise
		if (Config.getBoolean(Config.Key.RENDER_NOISE))
		{
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					double noise = density.getValue(x, y);
					int pixel = (int) (noise * 255);
					pixel = 255 - pixel; // invert
					image.setRGB(x, y, new Color(pixel, pixel, pixel).getRGB());
				}
			}
		}

		// landscape
		landscape.render(g);

		// graph
		graph.render(g);

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

}
