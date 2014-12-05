package es.mpitch.life.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import es.mpitch.life.Lifegdx;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.width = Math.round(LwjglApplicationConfiguration.getDesktopDisplayMode().width*.9f);
		config.height = Math.round(LwjglApplicationConfiguration.getDesktopDisplayMode().height*.7f);
		config.fullscreen = false;
		
		new LwjglApplication(new Lifegdx(), config);
	}
}
