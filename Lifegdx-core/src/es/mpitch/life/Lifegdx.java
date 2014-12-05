package es.mpitch.life;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class Lifegdx extends ApplicationAdapter {
	
	ShapeRenderer shapes;
	Life life;
	int size;
	
	@Override
	public void create () {
		shapes = new ShapeRenderer();
		size = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		life = new Life(size);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		boolean[][] grid = life.getGrid();
		
		shapes.begin(ShapeType.Filled);
		
		shapes.rect(0, 0, size, size, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
		
		for (int i=0; i<size; ++i)
			for (int j=0; j<size; ++j)
				if (grid[j][i])
					shapes.rect(i, j, 1, 1, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
		
		shapes.end();
		
		Life.nextGeneration();
	}
	
	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		super.resize(width, height);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		super.pause();
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		super.resume();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		super.dispose();
		shapes.dispose();
	}
}
