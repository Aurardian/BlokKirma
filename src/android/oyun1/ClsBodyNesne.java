package android.oyun1;

import org.anddev.andengine.opengl.texture.TextureOptions;
import android.content.Context;
import com.badlogic.gdx.physics.box2d.Body;

public class ClsBodyNesne extends ClsNesne {

	public Body oBody;
	
	public ClsBodyNesne()
	{
		super();
	}
	
	public ClsBodyNesne(int oTextureWidth, int oTextureHeight,TextureOptions oTextureOptions, Context oContext,String oAssetPath, int oTexturePositionX, int oTexturePositionY) 
	{
		super(oTextureWidth, oTextureHeight, oTextureOptions, oContext, oAssetPath,oTexturePositionX, oTexturePositionY);
	}
}
