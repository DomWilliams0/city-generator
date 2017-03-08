package ms.domwillia.city.generator;

import ms.domwillia.city.Config;
import ms.domwillia.city.generator.rules.GridRule;
import ms.domwillia.city.graph.Graph;
import ms.domwillia.city.RoadType;
import ms.domwillia.city.graph.Vertex;

import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

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

	public void generate()
	{
		int maxTries = 10;
		while (maxTries-- > 0 && graph.getVertices().size() < Config.getInt(Config.Key.MINIMUM_VERTICES))
		{
			graph.getVertices().clear();
			graph.getEdges().clear();

			initFrontier();

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

		if (maxTries < 0)
			System.err.println("Total failure");

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
		Point2D toMerge = findClosestVertex(vertex.getPosition(), Config.getDouble(Config.Key.MERGE_THRESHOLD),
			vertex.getPosition(), vertex.getSourceVertex().getPoint());
		if (toMerge != null)
		{
			vertex.setPosition(toMerge.getX(), toMerge.getY());
			vertex.setShouldProposeMore(false);
		}

		return true;
	}

	private void initFrontier()
	{
		// add single reference vertex
		Vertex ref = graph.addVertex(graph.getWidth() / 2, graph.getHeight() / 2, RoadType.MAIN);

		double length = Config.getDouble(Config.Key.ROAD_LENGTH);
		ProposedVertex a = new ProposedVertex(ref.getPoint().getX(), ref.getPoint().getY() + length, ref, RoadType.MAIN);

		frontier.add(a);
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
}
