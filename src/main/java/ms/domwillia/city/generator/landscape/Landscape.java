package ms.domwillia.city.generator.landscape;

import java.awt.*;

public class Landscape
{
	private final int width;
	private final int height;

	private River river;
	private RegionMap regionMap;

	public Landscape(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public River getRiver()
	{
		return river;
	}

	public void generateRiver(double minPoints, double scanAngle, double scanRangeScale, int sampleCount)
	{
		river = new River(this, minPoints, scanAngle, scanRangeScale, sampleCount);
	}

	public void createPolygons(int count, int relaxCount)
	{
		regionMap = new RegionMap(this, count, relaxCount);
	}

	public void render(Graphics2D g)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		regionMap.render(g);
		river.render(g);
	}
}
