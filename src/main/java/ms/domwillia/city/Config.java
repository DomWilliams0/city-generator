package ms.domwillia.city;

import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.util.EnumMap;

public class Config
{
	private static final Config INSTANCE = new Config();

	public enum KeyType
	{
		INTEGER,
		DOUBLE,
		BOOLEAN,
		COLOUR
	}

	public enum Key
	{
		WORLD_WIDTH(KeyType.INTEGER, "Width"),
		WORLD_HEIGHT(KeyType.INTEGER, "Height"),

		MAIN_MERGE_THRESHOLD(KeyType.DOUBLE),
		MAIN_ROAD_LENGTH(KeyType.DOUBLE),
		MAIN_ANGLE_VARIATION(KeyType.DOUBLE),
		MAIN_ROAD_CHANCE(KeyType.DOUBLE),

		ROAD_MAIN_SCALE_FACTOR(KeyType.INTEGER, "Main Road Scale Factor"),
		ROAD_MAIN_SUBDIVIDE_COUNT(KeyType.INTEGER, "Main Road Subdivision"),

		MINOR_MERGE_THRESHOLD(KeyType.DOUBLE),
		MINOR_ROAD_LENGTH(KeyType.DOUBLE),
		MINOR_ANGLE_VARIATION(KeyType.DOUBLE),
		MINOR_ROAD_CHANCE(KeyType.DOUBLE),

		NOISE_SCALE(KeyType.DOUBLE),
		MINIMUM_VERTICES(KeyType.INTEGER),

		RENDER_NOISE(KeyType.BOOLEAN),
		VERTEX_RENDER_RADIUS(KeyType.INTEGER, "Vertex Radius"),
		VERTEX_RENDER_COLOUR(KeyType.COLOUR, "Vertex Colour"),
		ROAD_MAIN_RENDER_COLOUR(KeyType.COLOUR, "Main Road Colour"),
		ROAD_MAIN_RENDER_THICKNESS(KeyType.INTEGER, "Main Road Thickness"),
		ROAD_MINOR_RENDER_COLOUR(KeyType.COLOUR, "Minor Road Colour"),
		ROAD_MINOR_RENDER_THICKNESS(KeyType.INTEGER, "Minor Road Thickness");

		private final KeyType type;
		private final String name;

		Key(KeyType type)
		{
			this(type, null);
		}

		Key(KeyType type, String name)
		{
			this.type = type;

			if (name == null)
				name = WordUtils.capitalizeFully(name().replace('_', ' '));

			this.name = name;
		}

		public KeyType getType()
		{
			return type;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	private EnumMap<Key, Object> configMap;

	private Config()
	{
		configMap = new EnumMap<>(Key.class);

		// defaults
		configMap.put(Key.WORLD_WIDTH,                 400);
		configMap.put(Key.WORLD_HEIGHT,                400);

		configMap.put(Key.MAIN_MERGE_THRESHOLD,        18.0);
		configMap.put(Key.MAIN_ROAD_LENGTH,            20.0);
		configMap.put(Key.MAIN_ANGLE_VARIATION,        7.0);
		configMap.put(Key.MAIN_ROAD_CHANCE,            0.8);

		configMap.put(Key.MINOR_MERGE_THRESHOLD,       6.0);
		configMap.put(Key.MINOR_ROAD_LENGTH,           10.0);
		configMap.put(Key.MINOR_ANGLE_VARIATION,       15.0);
		configMap.put(Key.MINOR_ROAD_CHANCE,           0.7);


		configMap.put(Key.MINIMUM_VERTICES,            100);
		configMap.put(Key.NOISE_SCALE,                 100.0);
		configMap.put(Key.RENDER_NOISE,                false);
		configMap.put(Key.VERTEX_RENDER_RADIUS,        1);
		configMap.put(Key.VERTEX_RENDER_COLOUR,        Color.CYAN);
		configMap.put(Key.ROAD_MINOR_RENDER_THICKNESS, 1);
		configMap.put(Key.ROAD_MINOR_RENDER_COLOUR,    Color.BLUE);
		configMap.put(Key.ROAD_MAIN_RENDER_COLOUR,     Color.DARK_GRAY);
		configMap.put(Key.ROAD_MAIN_RENDER_THICKNESS,  2);
		configMap.put(Key.ROAD_MAIN_SCALE_FACTOR,      3);
		configMap.put(Key.ROAD_MAIN_SUBDIVIDE_COUNT,   6);
	}

	public static int getInt(Key key)
	{
		return (int) get(key, KeyType.INTEGER);
	}

	public static double getDouble(Key key)
	{
		return (double) get(key, KeyType.DOUBLE);
	}

	public static boolean getBoolean(Key key)
	{
		return (boolean) get(key, KeyType.BOOLEAN);
	}

	public static Color getColour(Key key)
	{
		return (Color) get(key, KeyType.COLOUR);
	}

	private static Object get(Key key, KeyType type)
	{
		if (key.type != type)
			throw new IllegalArgumentException(String.format("Wrong type '%s' for config key '%s'", type, key));

		Object val = INSTANCE.configMap.get(key);
		if (val == null)
			throw new IllegalArgumentException(String.format("Invalid key '%s'", key));

		return val;
	}

	public static void set(Key key, Object value)
	{
		INSTANCE.configMap.put(key, value);
	}

}
