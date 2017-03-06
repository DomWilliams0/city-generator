package prototype.generator.rules;

import prototype.generator.ProposedVertex;
import prototype.generator.Utils;
import prototype.graph.Vertex;

import java.awt.geom.Point2D;
import java.util.List;

public class GridRule
{
	public void suggestVertices(ProposedVertex src, Vertex srcNewlyAdded, List<ProposedVertex> proposed)
	{
		// TODO only grid generation, but the angle depends on the noise ?
		double minAngle = -90;
		double maxAngle = 90;
		double angleIncrement = 90;

		double suggestRoadLength = 20;
		double tooCloseThreshold = 100;

		// maximum 3 times
		for (int i = 0; i < 3; i++)
		{
			// generate angle
			double proposedAngle = proposeAngle(src.getDirectionAngle(), minAngle, maxAngle, angleIncrement);
			double proposedX = src.getX() + (Math.cos(proposedAngle) * suggestRoadLength);
			double proposedY = src.getY() + (Math.sin(proposedAngle) * suggestRoadLength);

			// ensure not too close to other proposed
			if (notTooCloseToOthers(proposedX, proposedY, proposed, tooCloseThreshold))
			{
				proposed.add(new ProposedVertex(proposedX, proposedY, srcNewlyAdded, src.getType()));
			}

		}
	}

	private boolean notTooCloseToOthers(double proposedX, double proposedY,
	                                    List<ProposedVertex> proposed, double tooCloseThreshold)
	{
		for (ProposedVertex other : proposed)
		{
			double dsqr = Point2D.distanceSq(
				proposedX, proposedY,
				other.getX(), other.getY()
			);

			if (dsqr <= tooCloseThreshold * tooCloseThreshold)
				return false;
		}

		return true;
	}

	/**
	 * All in degrees
	 * @return Radians
	 */
	private double proposeAngle(double offset, double minAngle, double maxAngle, double angleIncrement)
	{
		double r = Utils.RANDOM.nextInt((int) (maxAngle - minAngle)) + minAngle;
		double rounded = ((int) Math.round(Math.abs(r) / angleIncrement)) * angleIncrement;
		rounded *= Math.signum(r);

		return Math.toRadians(offset + rounded);
	}
}
