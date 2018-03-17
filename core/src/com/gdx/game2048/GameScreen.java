package com.gdx.game2048;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gdx.game2048.Shape.CustomShapeRender;

public class GameScreen extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	CustomShapeRender shapeRenderer;
	Stage stage;
	BitmapFont myFont;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("circle1.png");
		shapeRenderer = new CustomShapeRender();

		int row_height = Gdx.graphics.getWidth() / 12;
		int col_width = Gdx.graphics.getWidth() / 12;

		stage = new Stage(new ScreenViewport());

		Label.LabelStyle label1Style = new Label.LabelStyle();
		myFont = new BitmapFont(Gdx.files.internal("bitmapfont/ClearBold32.fnt"));
		label1Style.font = myFont;
		label1Style.fontColor = Color.RED;

		Label label1 = new Label("Title (BitmapFont)",label1Style);
		label1.setSize(Gdx.graphics.getWidth(),row_height);
		label1.setPosition(0,Gdx.graphics.getHeight()-row_height*2);
		label1.setAlignment(Align.center);
		stage.addActor(label1);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act();
		stage.draw();
		batch.begin();
		drawImage();
		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}

	private void drawImage(){
		batch.draw(img, 20, 20, 150, 150);
	}
}
