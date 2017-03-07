package prototype;

import java.awt.*;

public class Config
{
	public static double MERGE_THRESHOLD = 15;
	public static double ROAD_LENGTH = 20;

	public static double NOISE_SCALE = 100;
	public static double ANGLE_VARIATION = 7;
	public static int MINIMUM_VERTICES = 100;

	public static boolean RENDER_NOISE = false;
	public static int VERTEX_RENDER_RADIUS = 5;
	public static Color VERTEX_RENDER_COLOUR = Color.RED;
	public static Color ROAD_MAIN_RENDER_COLOUR = Color.BLACK;
	public static Color ROAD_MINOR_RENDER_COLOUR = Color.BLUE;
	public static int ROAD_MAIN_RENDER_THICKNESS = 3;
	public static int ROAD_MINOR_RENDER_THICKNESS = 3;

	private Config()
	{
	}
}
