package ms.domwillia.city.generator.rules;

import ms.domwillia.city.Config;
import ms.domwillia.city.RoadType;
import ms.domwillia.city.generator.Density;
import ms.domwillia.city.generator.ProposedVertex;
import ms.domwillia.city.generator.Utils;
import ms.domwillia.city.graph.Vertex;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

public class GridRule
{
	public void suggestVertices(ProposedVertex src, Vertex srcNewlyAdded, List<ProposedVertex> proposed)
	{
		double angleVariationMin = Config.getDouble(src.getType() == RoadType.MAIN ?
			Config.Key.MAIN_ANGLE_VARIATION_MIN : Config.Key.MINOR_ANGLE_VARIATION_MIN);
		double angleVariationMax = Config.getDouble(src.getType() == RoadType.MAIN ?
			Config.Key.MAIN_ANGLE_VARIATION_MAX : Config.Key.MINOR_ANGLE_VARIATION_MAX);
		double roadChance = Config.getDouble(src.getType() == RoadType.MAIN ?
			Config.Key.MAIN_ROAD_CHANCE : Config.Key.MINOR_ROAD_CHANCE);
		double roadLength = Config.getDouble(src.getType() == RoadType.MAIN ?
			Config.Key.MAIN_ROAD_LENGTH : Config.Key.MINOR_ROAD_LENGTH);


		double density = Density.getValue(src.getX(), src.getY());

		double angleVariation = Utils.scale(density,
			0.0, 1.0,
			angleVariationMin, angleVariationMax);

		double angleOffset = density / angleVariation;

		double currentAngle = src.getDirectionAngle();
		double[] gridAngles = {-Math.PI / 2, 0, Math.PI};

		// left, forward, right
		for (int i = 0; i < 3; i++)
		{
			if (Utils.RANDOM.nextFloat() < roadChance)
			{
				double proposedAngle = gridAngles[i] + currentAngle + angleOffset;
				double proposedX = src.getX() + (Math.cos(proposedAngle) * roadLength);
				double proposedY = src.getY() + (Math.sin(proposedAngle) * roadLength);

				proposed.add(new ProposedVertex(proposedX, proposedY, srcNewlyAdded, src.getType()));
			}


		}
	}
}
