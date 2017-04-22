package ms.domwillia.city;


import ms.domwillia.city.generator.Generator;

public class Main
{
	public static void main(String[] args)
	{
		int THREAD_COUNT = 4;
		int TASKS_PER_THREAD = 3;

		Thread[] threads = new Thread[THREAD_COUNT];
		int width = Config.getInt(Config.Key.WORLD_WIDTH);
		int height = Config.getInt(Config.Key.WORLD_HEIGHT);
		for (int i = 0; i < threads.length; i++)
		{
			threads[i] = new Thread(new GeneratorRunnable(width, height, TASKS_PER_THREAD, TASKS_PER_THREAD*i));
			threads[i].start();
		}
		for (Thread thread : threads)
			try
			{
				thread.join();
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
	}

	static class GeneratorRunnable implements Runnable
	{
		private static final String DIR = "/tmp/road-renders";
		private static final String FORMAT = "render-%d.png";

		private final int width;
		private final int height;
		private final int count;
		private int startIndex;

		GeneratorRunnable(int width, int height, int count, int startIndex)
		{
			this.width = width;
			this.height = height;
			this.count = count;
			this.startIndex = startIndex;
		}

		@Override
		public void run()
		{
			for (int i = 0; i < count; i++)
			{
				Generator g = new Generator(width, height);
				g.generate();
				g.export(DIR, FORMAT, startIndex++);
			}
		}
	}
}
