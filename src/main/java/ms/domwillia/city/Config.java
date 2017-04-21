package ms.domwillia.city;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.util.Arrays;
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

	public enum Section
	{
		WORLD,
		MAIN,
		MINOR,
		RENDER;

		private final String title;

		Section()
		{
			this.title = WordUtils.capitalizeFully(name());
		}

		public String getTitle()
		{
			return title;
		}

		public Key[] getKeys()
		{
			return Arrays.stream(Key.values())
				.filter(k -> k.getSection() == this)
				.toArray(Key[]::new);
		}
	}

	public enum Key
	{
		WORLD_WIDTH(Section.WORLD, KeyType.INTEGER, "Width"),
		WORLD_HEIGHT(Section.WORLD, KeyType.INTEGER, "Height"),

		MAIN_MERGE_THRESHOLD(Section.MAIN, KeyType.DOUBLE),
		MAIN_ROAD_LENGTH(Section.MAIN, KeyType.DOUBLE),
		MAIN_ANGLE_VARIATION_MIN(Section.MAIN, KeyType.DOUBLE),
		MAIN_ANGLE_VARIATION_MAX(Section.MAIN, KeyType.DOUBLE),
		MAIN_ROAD_CHANCE(Section.MAIN, KeyType.DOUBLE),

		MAIN_ROAD_SCALE_FACTOR(Section.MAIN, KeyType.INTEGER, "Main Road Scale Factor"),
		MAIN_ROAD_SUBDIVIDE_COUNT(Section.MAIN, KeyType.INTEGER, "Main Road Subdivision"),

		MINOR_MERGE_THRESHOLD(Section.MINOR, KeyType.DOUBLE),
		MINOR_ROAD_LENGTH(Section.MINOR, KeyType.DOUBLE),
		MINOR_ANGLE_VARIATION_MIN(Section.MINOR, KeyType.DOUBLE),
		MINOR_ANGLE_VARIATION_MAX(Section.MINOR, KeyType.DOUBLE),
		MINOR_ROAD_CHANCE(Section.MINOR, KeyType.DOUBLE),

		NOISE_SCALE(Section.WORLD, KeyType.DOUBLE),
		MINIMUM_VERTICES(Section.WORLD, KeyType.INTEGER),

		RENDER_NOISE(Section.RENDER, KeyType.BOOLEAN),
		VERTEX_RENDER_RADIUS(Section.RENDER, KeyType.INTEGER, "Vertex Radius"),
		VERTEX_RENDER_COLOUR(Section.RENDER, KeyType.COLOUR, "Vertex Colour"),
		ROAD_MAIN_RENDER_COLOUR(Section.RENDER, KeyType.COLOUR, "Main Road Colour"),
		ROAD_MAIN_RENDER_THICKNESS(Section.RENDER, KeyType.INTEGER, "Main Road Thickness"),
		ROAD_MINOR_RENDER_COLOUR(Section.RENDER, KeyType.COLOUR, "Minor Road Colour"),
		ROAD_MINOR_RENDER_THICKNESS(Section.RENDER, KeyType.INTEGER, "Minor Road Thickness");

		private final Section section;
		private final KeyType type;
		private final String name;

		Key(Section section, KeyType type)
		{
			this(section, type, null);
		}

		Key(Section section, KeyType type, String name)
		{
			this.section = section;
			this.type = type;

			if (name == null)
				name = WordUtils.capitalizeFully(name().replace('_', ' '));

			this.name = name;
		}

		public KeyType getType()
		{
			return type;
		}

		public Section getSection()
		{
			return section;
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
		configMap.put(Key.WORLD_WIDTH, 600);
		configMap.put(Key.WORLD_HEIGHT, 600);

		configMap.put(Key.MAIN_MERGE_THRESHOLD, 18.0);
		configMap.put(Key.MAIN_ROAD_LENGTH, 20.0);
		configMap.put(Key.MAIN_ANGLE_VARIATION_MIN, 7.0);
		configMap.put(Key.MAIN_ANGLE_VARIATION_MAX, 15.0);
		configMap.put(Key.MAIN_ROAD_CHANCE, 0.8);

		configMap.put(Key.MINOR_MERGE_THRESHOLD, 8.0);
		configMap.put(Key.MINOR_ROAD_LENGTH, 10.0);
		configMap.put(Key.MINOR_ANGLE_VARIATION_MIN, 5.0);
		configMap.put(Key.MINOR_ANGLE_VARIATION_MAX, 10.0);
		configMap.put(Key.MINOR_ROAD_CHANCE, 0.7);


		configMap.put(Key.MINIMUM_VERTICES, 100);
		configMap.put(Key.NOISE_SCALE, 50.0);
		configMap.put(Key.RENDER_NOISE, true);
		configMap.put(Key.VERTEX_RENDER_RADIUS, 1);
		configMap.put(Key.VERTEX_RENDER_COLOUR, Color.CYAN);
		configMap.put(Key.ROAD_MINOR_RENDER_THICKNESS, 1);
		configMap.put(Key.ROAD_MINOR_RENDER_COLOUR, Color.BLUE);
		configMap.put(Key.ROAD_MAIN_RENDER_COLOUR, Color.DARK_GRAY);
		configMap.put(Key.ROAD_MAIN_RENDER_THICKNESS, 2);
		configMap.put(Key.MAIN_ROAD_SCALE_FACTOR, 3);
		configMap.put(Key.MAIN_ROAD_SUBDIVIDE_COUNT, 6);
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
