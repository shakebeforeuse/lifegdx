package es.mpitch.life;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

public class Life implements Runnable
{
	private static boolean[][] grid;
	private static boolean[][] nextGrid;
	private static int size;
	private static int cores = Runtime.getRuntime().availableProcessors();
	private static ExecutorService threadPool = Executors.newFixedThreadPool(cores);
	private static Runnable[] tasks = new Runnable[cores];
	private static CyclicBarrier barrier = new CyclicBarrier(cores + 1);
	private int beginX;
	private int endX;
	
	public void randomGrid(int size)
	{
		Life.size = size;
		grid = new boolean[size][size];
		nextGrid = new boolean[size][size];
		
		for (int i = 0; i < size; ++i)
			for (int j = 0; j < size; ++j)
				grid[j][i] = Math.random() < 0.1;
		
		// //Glider
		// grid[155][155] = true;
		// grid[156][155] = true;
		// grid[157][155] = true;
		// grid[155][156] = true;
		// grid[156][157] = true;
		// //Glider
		// grid[355][355] = true;
		// grid[356][355] = true;
		// grid[357][355] = true;
		// grid[355][356] = true;
		// grid[356][357] = true;
		// //Blinker
		// grid[200][200] = true;
		// grid[200][201] = true;
		// grid[200][202] = true;
	}
	
	public Life(int size)
	{
		if (grid == null)
			randomGrid(size);
			
		this.beginX = 0;
		this.endX   = size;
		
		int parts = size/cores;
		
		for (int i = 0; i < cores; ++i)
		{
			int beginX = i*parts;
			int endX;
			
			if (i+1 == cores)
				endX = size;
			else
				endX = (i+1)*parts;
			
			tasks[i] = new Life(size, beginX, endX);
		}
	}
	
	public Life(int size, int beginX, int endX)
	{
		if (grid == null)
			randomGrid(size);
			
		this.beginX = beginX;
		this.endX   = endX;
	}
	
	private int mod(int a, int b)
	{
		int r = a % b;
		if (r < 0)
		{
			r += b;
		}
		
		return r;
	}
	
	public int aliveNeighbours(int x, int y)
	{
		int alives = 0;
		
		for (int i = -1; i <= 1; ++i)
		{
			for (int j = -1; j <= 1; ++j)
			{
				int col = mod(y+j, size);
				int row = mod(x+i, size);
				
				if (grid[col][row] && (col != y || row != x))
					++alives;
			}
		}

		return alives;
	}
	
	public boolean nextState(int x, int y)
	{
		boolean newState = false;
		int neighbours = aliveNeighbours(x, y);
		
		if (grid[y][x])
		{
			if (neighbours == 2 || neighbours == 3)
				newState = true;
			else
				newState = false;
		}
		else
			if (neighbours == 3)
				newState = true;
			
		return newState;
	}
	
	public void nextSubGeneration(int beginX, int endX, int beginY, int endY)
	{
		for (int i = beginX; i < endX; ++i)
			for (int j = beginY; j < endY; ++j)
				nextGrid[j][i] = nextState(i, j);
	}
	
	public static void nextGeneration()
	{
		nextGrid = new boolean[size][size];
		
		for (int i = 0; i < cores; ++i)
			threadPool.execute(tasks[i]);
		
		try
		{
			barrier.await();
			barrier.reset();
			acceptNextGeneration();
		}
		catch (InterruptedException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
		catch (BrokenBarrierException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public void run()
	{
		nextSubGeneration(beginX, endX, 0, size);
		try
		{
			barrier.await();
		}
		catch (InterruptedException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
		catch (BrokenBarrierException e)
		{
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public boolean[][] getGrid()
	{
		return grid;
	}
	
	public static void acceptNextGeneration()
	{
		grid = nextGrid;
	}
}
