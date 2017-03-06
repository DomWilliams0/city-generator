package prototype.generator;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import prototype.RoadType;
import prototype.graph.Graph;
import prototype.graph.Vertex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Generator
{
	private Graph graph;
	private Queue<ProposedVertex> frontier;

	public Generator(Graph graph)
	{
		this.graph = graph;
		this.frontier = new ArrayDeque<>();
	}

	public void generate()
	{
		initFrontier();

		List<ProposedVertex> proposed = new ArrayList<>();

		while (!frontier.isEmpty())
		{
			ProposedVertex vertex = frontier.poll();

			if (acceptLocalConstraints(vertex))
			{
				Vertex newlyAdded = graph.addVertex(vertex.getX(), vertex.getY(), vertex.getType());
				graph.addEdge(newlyAdded, vertex.getSourceVertex());

				proposed.clear();
				produceWithGlobalGoals(vertex, newlyAdded, proposed);

				frontier.addAll(proposed);
			}


		}

	}

	private void produceWithGlobalGoals(ProposedVertex src, Vertex srcNewlyAdded, List<ProposedVertex> proposed)
	{
		Vector2D direction = src.getDirection();

		// add neighbour following direction
		direction = direction.scalarMultiply(20);

		ProposedVertex suggestion = new ProposedVertex(
			src.getX() + direction.getX(),
			src.getY() + direction.getY(),
			srcNewlyAdded,
			src.getType());

		proposed.add(suggestion);
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

		// can get direction from vertex.getSourceVertex()

		return true;
	}

	private void initFrontier()
	{
		// add single reference vertex
		Vertex ref = graph.addVertex(50, 80, RoadType.MAIN);

		ProposedVertex a = new ProposedVertex(100, 100, ref, RoadType.MAIN);

		frontier.add(a);
	}

}
