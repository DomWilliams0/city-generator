package prototype.generator.rules;

import prototype.generator.ProposedVertex;
import prototype.generator.Utils;
import prototype.graph.Vertex;

import java.util.List;

public class GridRule
{
	public void suggestVertices(ProposedVertex src, Vertex srcNewlyAdded, List<ProposedVertex> proposed)
	{
		// TODO only grid generation, but the angle offset depends on the noise ?
		double angleOffset = 0.1 + Utils.RANDOM.nextFloat() / 4;

		double suggestRoadLength = 20; // TODO constants

		double currentAngle = src.getDirectionAngle();
		double[] gridAngles = {-Math.PI / 2, 0, Math.PI};

		// left, forward, right
		for (int i = 0; i < 3; i++)
		{
			// generate angle
			double proposedAngle = gridAngles[i] + currentAngle + angleOffset;
			double proposedX = src.getX() + (Math.cos(proposedAngle) * suggestRoadLength);
			double proposedY = src.getY() + (Math.sin(proposedAngle) * suggestRoadLength);

			double chance = 0.7; // TODO depend on noise

			if (Utils.RANDOM.nextFloat() < chance)
			{
				proposed.add(new ProposedVertex(proposedX, proposedY, srcNewlyAdded, src.getType()));
			}

		}
	}
}
