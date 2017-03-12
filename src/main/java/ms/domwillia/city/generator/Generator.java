package ms.domwillia.city.generator;

import ms.domwillia.city.Config;
import ms.domwillia.city.RoadType;
import ms.domwillia.city.generator.rules.GridRule;
import ms.domwillia.city.graph.Graph;
import ms.domwillia.city.graph.Vertex;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.geom.Point2D;
import java.util.*;

public class Generator
{
	private Graph graph;
	private Queue<ProposedVertex> frontier;

	private GridRule rule;

	public Generator(Graph graph)
	{
		this.graph = graph;
		this.frontier = new ArrayDeque<>();
		this.rule = new GridRule();
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
		rule.suggestVertices(src, srcNewlyAdded, proposed);
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
		Point2D closest = null;
		double closestDistance = Double.MAX_VALUE;
		double thresholdSq = threshold * threshold;

		for (Vertex vertex : graph.getVertices())
		{
			if (vertex.getPoint().equals(moi) || vertex.getPoint().equals(moiSrc))
				continue;

			double d = vertex.getPoint().distanceSq(pos);
			if (d <= thresholdSq && d < closestDistance)
			{
				closestDistance = d;
				closest = vertex.getPoint();
			}
		}

		return closest;
	}

	public Graph getGraph()
	{
		return graph;
	}

	public void generate()
	{
		int maxTries = 60;
		do
		{
			// main roads first
			Collection<ProposedVertex> initialFrontier = new ArrayList<>();
			initMainFrontier(initialFrontier);

			generate(initialFrontier);
			graph.scaleAndSubdivide(
				Config.getInt(Config.Key.ROAD_MAIN_SCALE_FACTOR),
				Config.getInt(Config.Key.ROAD_MAIN_SUBDIVIDE_COUNT)
			);

			// minor roads
			initialFrontier.clear();
			initMinorFrontier(initialFrontier);
			generate(initialFrontier);
		}
		while (--maxTries > 0 && graph.getVertices().size() < Config.getInt(Config.Key.MINIMUM_VERTICES));

		if (maxTries < 0)
			System.err.println("Total failure");


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
}
