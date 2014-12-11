package es.mpitch.life;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Conway's Game of Life.
 * @author Manuel F.
 * @version 0.1 06/12/2014
 */
public class Life implements Runnable
{
	/** Cell's grid, represents states of each cell, being 'true' alive and 'false' dead. */
	private static boolean[][] grid;
	
	/** Auxiliary grid, necessary to save the next generation without collisions */
	private static boolean[][] nextGrid;
	
	/** Size of our grid. It has to be square at the moment. */
	private static int size;
	
	/** Number of available cores */
	private static int cores = Runtime.getRuntime().availableProcessors();
	
	/** Thread's pool */
	private static ExecutorService threadPool = Executors.newFixedThreadPool(cores);
	
	/** Array of Runnable tasks. Necessary to avoid creating tasks for each new generation */
	private static Runnable[] tasks = new Runnable[cores];
	
	/** Barrier to keep threads synchronised. */
	private static CyclicBarrier barrier = new CyclicBarrier(cores + 1);
	
	/** Integer representing where this task has to start computing the next generation */
	private int beginX;
	
	/** Integer representing where this task has to stop computing the next generation */
	private int endX;
	
	/**
	 * Create a new random state for the automata, with a 0.1 probability of a cell being alive
	 */
	public void randomGrid()
	{
		grid = new boolean[size][size];
		
		//0.1 probability of a cell being alive
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
	
	/**
	 * Constructor of the 2D automata, with a given size.
	 * @param size Size of the matrix (square)
	 */
	public Life(int size)
	{
		//Set size
		Life.size = size;
		
		//Generate new random initial state
		randomGrid();
		
		//In case we have only 1 core
		this.beginX = 0;
		this.endX   = size;
		
		//Create new tasks.
		int parts = size/cores;
		
		for (int i = 0; i < cores; ++i)
		{
			int beginX = i*parts;
			int endX;
			
			//Is this our last iteration? Last task have to take the remaining rows,
			//just in case size%cores != 0
			if (i+1 == cores)
				endX = size;
			else
				endX = (i+1)*parts;
			
			tasks[i] = new Life(beginX, endX);
		}
	}
	
	/**
	 * Private constructor to create tasks.
	 * @param beginX First row we have to compute
	 * @param endX Last row we have to compute
	 */
	private Life(int beginX, int endX)
	{
		this.beginX = beginX;
		this.endX   = endX;
	}
	
	/**
	 * Calculate modulo! Java doesn't do it right (mathematically speaking).
	 * @param a Dividend
	 * @param b Divisor
	 * @return Remainder
	 */
	private int mod(int a, int b)
	{
		int r = a % b;
		if (r < 0)
			r += b;
		
		return r;
	}
	
	/**
	 * Alive neighbours of cell (x,y)
	 * @param x x axis coordinate
	 * @param y y axis coordinate
	 * @return Alive neighbours considering circular borders. -1 in case cell is invalid.
	 */
	public int aliveNeighbours(int x, int y)
	{
		int alives = -1;
		
		if (x < size && y < size)
		{
			alives = 0;
			
			for (int i = -1; i <= 1; ++i)
			{
				for (int j = -1; j <= 1; ++j)
				{
					int col = mod(y+j, size);
					int row = mod(x+i, size);
					
					//Avoid considering (x,y) cell
					if (grid[col][row] && (col != y || row != x))
						++alives;
				}
			}
		}

		return alives;
	}
	
	/**
	 * State cell (x,y) has to take in the next generation.
	 * @param x x axis coordinate
	 * @param y y axis coordinate
	 * @return Next state for cell (x, y), being 'true' alive and 'false' dead.
	 */
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
	
	/**
	 * Private method tasks will use to compute the given submatrix. It will save his
	 * work on nextGrid.
	 */
	private void nextSubGeneration()
	{
		for (int i = beginX; i < endX; ++i)
			for (int j = 0; j < size; ++j)
				nextGrid[j][i] = nextState(i, j);
	}
	
	/**
	 * Computes next generation of cells and store it in nextGrid matrix
	 */
	public static void nextGeneration()
	{
		//Discard previous changes in nextGrid matrix
		nextGrid = new boolean[size][size];
		
		//Launch our tasks
		for (int i = 0; i < cores; ++i)
			threadPool.execute(tasks[i]);
		
		//Block our main thread, until all tasks are done, and take the next generation
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
	
	/**
	 * Thread-executable method. Computes corresponding submatrix, and awaits in a barrier,
	 * just for synchronisation purposes.
	 */
	@Override
	public void run()
	{
		nextSubGeneration();
		
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
	
	/**
	 * Grid viewer.
	 * @return Matrix representing the state of each cell, being 'true' alive and 'false' dead.
	 */
	public boolean[][] getGrid()
	{
		return grid;
	}
	
	/**
	 * Take next generation as current.
	 */
	public static void acceptNextGeneration()
	{
		grid = nextGrid;
	}
}
