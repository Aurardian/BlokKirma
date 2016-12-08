package android.oyun1;

import java.io.IOException;

import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import android.hardware.SensorManager;
import android.view.KeyEvent;

public class BlokKirmaActivity extends BaseGameActivity 
{
	private static final int CAMERA_WIDTH = 800;
    private static final int CAMERA_HEIGHT = 480;
    private PhysicsWorld physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_DEATH_STAR_I), false);
    private FixtureDef fixDef = PhysicsFactory.createFixtureDef(1.0f, 1.0f, 0.0f);
    private Camera camera;
    private Engine engine;
	Scene sahneOyun, sahneAnaMenu, sahnePauseMenu;
	
	int blokSayisiX = 11, blokSayisiY = 5;
	int blokBuyukluguX = 64, blokBuyukluguY = 32, 
			topBuyuklugu = 32, 
			oyuncuBuyukluguX = 256, oyuncuBuyukluguY = 32;
	int kenardanUzaklikX = 40, kenardanUzaklikY = 10;
	float oyuncuHizi = 7, topHizi = -7;
	
	private ClsBodyNesne [][]bloklar = new ClsBodyNesne[blokSayisiX][blokSayisiY];
	private boolean  [][]blokTemasKonrol = new boolean[blokSayisiX][blokSayisiY];
	private int [][]blokDegerleri = new int[blokSayisiX][blokSayisiY];
	
	private ClsBodyNesne top, oyuncu;
	private ClsBodyNesne ustDuvar, sagDuvar, solDuvar;
	
	
	
	private ClsNesne oyunArka, anaMenuArka, anaMenuOyna,
	anaMenuOynaHover, anaMenuCikis, anaMenuCikisHover,
	pauseMenuArka, pauseMenuMenu, pauseMenuRestart, pauseMenuResume;
	
	// Sound nesneleri tan�mlan�yor
	private Sound soundCarpma;
	
	private boolean anaMenuSahnesiMi = true, oyunSahnesiMi = false, pauseMenuSahnesiMi = false;
	
	ClsNesne oyuncuKontrolInvis;
	
    
	@Override
	public Engine onLoadEngine() 
	{
		// TODO Auto-generated method stub
		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new FillResolutionPolicy(), camera);
        engineOptions.getTouchOptions().setRunOnUpdateThread(true);
        engineOptions.setNeedsSound(true);
        engine = new Engine(engineOptions);
		
		return engine;
	}

	@Override
	public void onLoadResources() {
		// TODO Auto-generated method stub
		Texture [] texturesBloklar = new Texture[blokSayisiX*blokSayisiY];
		
		//Ana men� nesneleri olu�turuluyor
		anaMenuArka = new ClsNesne(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/arkaplan.png", 0, 0);
		anaMenuOyna = new ClsNesne(256, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/bt_play.png", 0, 0);
		anaMenuOynaHover = new ClsNesne(256, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/bt_play_hover.png", 0, 0);
		anaMenuCikis = new ClsNesne(128, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/bt_quit.png", 0, 0);
		anaMenuCikisHover = new ClsNesne(128, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/bt_quit_hover.png", 0, 0);
		
		// Pause men� nesneleri olu�turuluyor
		pauseMenuArka = new ClsNesne(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/pauseArkaplan.png", 0, 0);
		pauseMenuRestart = new ClsNesne(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/restart.png", 0, 0);
		pauseMenuResume = new ClsNesne(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/resume.png", 0, 0);
		pauseMenuMenu = new ClsNesne(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/menu.png", 0, 0);
		
		// Oyun alan�n� �evreleyen duvarlar i�in nesneler olu�turuluyor
		ustDuvar = new ClsBodyNesne(1024, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/ustduvar.png", 0, 0);
		sagDuvar = new ClsBodyNesne(32, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/yanduvar.png", 0, 0);
		solDuvar = new ClsBodyNesne(32, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/yanduvar.png", 0, 0);
		
		// Oyun nesneleri olu�turuluyor
		oyunArka = new ClsNesne(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/arkaplan.png", 0, 0);
		top = new ClsBodyNesne(topBuyuklugu, topBuyuklugu, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/top.png", 0, 0);
		oyuncu = new ClsBodyNesne(oyuncuBuyukluguX, oyuncuBuyukluguY, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/oyuncu.png", 0, 0);
		oyuncuKontrolInvis = new ClsNesne(1024, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/invis.png", 0, 0);
		
		// Blok nesnelerinin altyap�s� haz�rlan�yor
		for(int i = 0; i < blokSayisiX; i++)
		{
			for(int j = 0; j < blokSayisiY; j++)
			{
				if(i == j || blokSayisiX - 1 - i == j)
				{
					bloklar[i][j] = new ClsBodyNesne(blokBuyukluguX, blokBuyukluguY, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/blok3.png", 0, 0);
					blokDegerleri[i][j] = 2;
				}
				else if(i == 5)
				{
					bloklar[i][j] = new ClsBodyNesne(blokBuyukluguX, blokBuyukluguY, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/blok1.png", 0, 0);
					blokDegerleri[i][j] = 3;
				}
				else //if(i == j && i != blokSayisiX/2)
				{
					bloklar[i][j] = new ClsBodyNesne(blokBuyukluguX, blokBuyukluguY, TextureOptions.BILINEAR_PREMULTIPLYALPHA, this, "gfx/blok2.png", 0, 0);
					blokDegerleri[i][j] = 1;
				}
				texturesBloklar[i*(blokSayisiY) + j] = bloklar[i][j].oTexture;
				blokTemasKonrol[i][j] = false;
			}
		}
		
		// Ses nesneleri olu�turuluyor
		try 
		{
			if(soundCarpma == null)
			{
				soundCarpma = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "gfx/DuvaraDokunmaSesi.mp3");
			}
			else
			{
				soundCarpma = null;
				soundCarpma = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "gfx/DuvaraDokunmaSesi.mp3");
			}
		} 
		catch (IllegalStateException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		// Texture Nesneleri donan�ma y�klenmek �zere Texture t�r�nden diziye atan�yor.
		Texture [] textures = {oyunArka.oTexture, top.oTexture, oyuncu.oTexture, oyuncuKontrolInvis.oTexture, 
				ustDuvar.oTexture, sagDuvar.oTexture, solDuvar.oTexture,
				anaMenuArka.oTexture, anaMenuOyna.oTexture, anaMenuOynaHover.oTexture, anaMenuCikis.oTexture, anaMenuCikisHover.oTexture,
				pauseMenuArka.oTexture, pauseMenuMenu.oTexture, pauseMenuRestart.oTexture, pauseMenuResume.oTexture};
		
		// Texture Nesnesleri donan�ma y�kleniyor
		engine.getTextureManager().loadTextures(textures);
		engine.getTextureManager().loadTextures(texturesBloklar);
	}
	

	int kirilanBlokSayisi;
	@Override
	public Scene onLoadScene() 
	{
		this.engine.registerUpdateHandler(new FPSLogger());
		
		// Sahneler olu�turuluyor
		this.sahneOyun = new Scene();
		this.sahneAnaMenu = new Scene();
		
		// Pause ve finish ekran� i�in karartmal� �effaf arkaplan i�in CameraScene nesnesi olu�turuluyor.
		this.sahnePauseMenu = new CameraScene(this.camera);
		
		this.kirilanBlokSayisi = 0;
		
		// Metod isimlerinden hangi metotta hangi i�lem yap�ld���n� ��karabilirsiniz
		anaMenuNesneleriniOlustur();
		pauseMenuNesneleriniOlustur();
		oyunNesneleriniOlustur();
		kontroller();
		
		// Topun y eksenindeki h�z�n� ba�tan veriyoruz ��nk� oyun s�resince bu y�ndeki h�z de�i�meyecek
		this.top.oBody.setLinearVelocity(0, topHizi);
		
		// physicsWorld nesnesi sahne nesnesi i�in tan�mlan�yor.
		this.sahneOyun.registerUpdateHandler(physicsWorld);
		
		// Oyun ba�lad���nda ilk g�sterilecek olan sahne burada geri d�nd�r�len sahnedir(sahneAnaMen�))
		return this.sahneAnaMenu;
	}

	@Override
	public void onLoadComplete() {
		// TODO Auto-generated method stub
	}  
	
	private void anaMenuNesneleriniOlustur()
	{
		anaMenuArka.oSprite = new Sprite(0, 0, anaMenuArka.oTextureRegion);
		

		anaMenuOynaHover.oSprite = new Sprite(200, 107, anaMenuOynaHover.oTextureRegion);
		anaMenuOyna.oSprite = new Sprite(200, 107, anaMenuOyna.oTextureRegion)
		{
			@Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
            		float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
    			if(pSceneTouchEvent.isActionDown())
    			{
    				anaMenuOyna.oSprite.setVisible(false);
    				anaMenuOynaHover.oSprite.setVisible(true);
    			}
    			if(pSceneTouchEvent.isActionUp())
    			{   
    				anaMenuOyna.oSprite.setVisible(true);
    				anaMenuOynaHover.oSprite.setVisible(false);
    				// Fiziksel tu� kullan�m� i�in hangi sahnede bulundu�umuz �nemli
    				// Bu y�zden konumu bu de�i�kenler ile tutuyoruz.
    				anaMenuSahnesiMi = false;
    				oyunSahnesiMi = true;
    				// Oyna tu�una bas�ld���nda oyun sahnesine ge�ilmesini sa�layan kod blo�u
    				// enigne nesnesi arac�l��� ile setScene metodunu kullanarak sahne oyunu g�sterilecek sahne olarak ayarl�yoruz
    				engine.setScene(sahneOyun);
    			}
                return true;
            }
		};
		
		//Hover ve as�l nesne ayn� koordinatlarda olu�turuluyor
		anaMenuCikisHover.oSprite = new Sprite(600, 400, anaMenuCikisHover.oTextureRegion);
		anaMenuCikis.oSprite = new Sprite(600, 400, anaMenuCikis.oTextureRegion)
		{
			@Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
                            float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
    			if(pSceneTouchEvent.isActionDown())
    			{
    				anaMenuCikis.oSprite.setVisible(false);
    				anaMenuCikisHover.oSprite.setVisible(true);
    			}
    			if(pSceneTouchEvent.isActionUp())
    			{   
    	            System.exit(0);
    			}
                return true;
            }
		};
		
		// Hover nesnelerinin g�r�n�rl�k �zelli�i false yap�l�yor
		anaMenuOynaHover.oSprite.setVisible(false);
		anaMenuCikisHover.oSprite.setVisible(false);
		
		// sahneMenu nesneleri sahneye �izdiriliyor
		this.sahneAnaMenu.attachChild(anaMenuArka.oSprite);
		this.sahneAnaMenu.attachChild(anaMenuOyna.oSprite);
		this.sahneAnaMenu.attachChild(anaMenuOynaHover.oSprite);
		this.sahneAnaMenu.attachChild(anaMenuCikis.oSprite);
		this.sahneAnaMenu.attachChild(anaMenuCikisHover.oSprite);
		
		// sahneMenu �zerindeki butonlar�n RegisterArea
		// �zellikleri tan�mlan�yor 
		// (Hover nesnelerin dokunma  �zellikleri hari�)
		this.sahneAnaMenu.registerTouchArea(anaMenuOyna.oSprite);
		this.sahneAnaMenu.registerTouchArea(anaMenuCikis.oSprite);
		
		
	}

	private void pauseMenuNesneleriniOlustur()
	{
		pauseMenuArka.oSprite = new Sprite(0, 0, pauseMenuArka.oTextureRegion);
		pauseMenuRestart.oSprite = new Sprite(211, 209, pauseMenuRestart.oTextureRegion)
		{
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
				if (pSceneTouchEvent.isActionDown()) 
				{
					
				}
				if (pSceneTouchEvent.isActionUp()) 
				{
					restart();
				}
				return true;
			}
		};
		pauseMenuResume.oSprite = new Sprite(467, 209, pauseMenuResume.oTextureRegion)
		{
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
				if (pSceneTouchEvent.isActionDown()) 
				{
					
				}
				if (pSceneTouchEvent.isActionUp()) 
				{
					pauseMenuSahnesiMi = false;
    				oyunSahnesiMi = true;
    				// clearChildScene Metodu, CameraScene nesnelerini temizleyen metottur
    				// Bu metotla pauseMenu ya da finishMenu gibi men�leri temizleyebiliriz.
					sahneOyun.clearChildScene();
					//Fiziksel �zellikler etkinle�tirilir.
					sahneOyun.registerUpdateHandler(physicsWorld);
				}
				return true;
			}
		};
		pauseMenuMenu.oSprite = new Sprite(339, 209, pauseMenuMenu.oTextureRegion)
		{
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
				if (pSceneTouchEvent.isActionDown()) 
				{
					
           				
				}
				if (pSceneTouchEvent.isActionUp()) 
				{
    				restart();
    				anaMenuSahnesiMi = true;
    				oyunSahnesiMi = false;
					engine.setScene(sahneAnaMenu);
				}
				return true;
			}
		};
		
		sahnePauseMenu.attachChild(pauseMenuArka.oSprite);
		sahnePauseMenu.attachChild(pauseMenuMenu.oSprite);
		sahnePauseMenu.attachChild(pauseMenuRestart.oSprite);
		sahnePauseMenu.attachChild(pauseMenuResume.oSprite);
		
		sahnePauseMenu.registerTouchArea(pauseMenuMenu.oSprite);
		sahnePauseMenu.registerTouchArea(pauseMenuRestart.oSprite);
		sahnePauseMenu.registerTouchArea(pauseMenuResume.oSprite);
		
		// Pause menu gibi �effaf sahneler i�in gerekli iki kod sat�r�.
		this.sahneAnaMenu.setTouchAreaBindingEnabled(true);
		this.sahnePauseMenu.setBackgroundEnabled(false);
	}
	
	/*
	 *  Sprite ve body nesneleri olu�turulur
	 *  sahneOyun sahnesine nesneler �izdirilir
	 *  oyuncuKontrolInvis sprite nesnesinin 
	 *  touch area �zelli�i tan�mlan�r
	 */
	float dokunulanNoktaX = CAMERA_WIDTH/2 - oyuncuBuyukluguX/2;
	float ortaNokta;
	private void oyunNesneleriniOlustur()
	{
		// duvar sprite nesnelerini olu�tur
		
		ustDuvar.oSprite = new Sprite(0, -32, ustDuvar.oTextureRegion);
		sagDuvar.oSprite = new Sprite(0, 0, sagDuvar.oTextureRegion);
		solDuvar.oSprite = new Sprite(CAMERA_WIDTH - 32, 0, solDuvar.oTextureRegion);
		
		this.sahneOyun.attachChild(ustDuvar.oSprite);
		this.sahneOyun.attachChild(sagDuvar.oSprite);
		this.sahneOyun.attachChild(solDuvar.oSprite);
		
		oyunArka.oSprite = new Sprite(0, 0, oyunArka.oTextureRegion);
		
		this.sahneOyun.attachChild(oyunArka.oSprite);
		
		ustDuvar.oBody = PhysicsFactory.createBoxBody(physicsWorld, ustDuvar.oSprite, BodyType.StaticBody, fixDef);
		sagDuvar.oBody = PhysicsFactory.createBoxBody(physicsWorld, sagDuvar.oSprite, BodyType.StaticBody, fixDef);
		solDuvar.oBody = PhysicsFactory.createBoxBody(physicsWorld, solDuvar.oSprite, BodyType.StaticBody, fixDef);
		
		this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(ustDuvar.oSprite, ustDuvar.oBody, true, true));
		this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(sagDuvar.oSprite, sagDuvar.oBody, true, true));
		this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(solDuvar.oSprite, solDuvar.oBody, true, true));
		
		
		oyuncu.oSprite = new Sprite(CAMERA_WIDTH/2 - oyuncuBuyukluguX/2, CAMERA_HEIGHT - 50, oyuncu.oTextureRegion);
		top.oSprite = new Sprite(CAMERA_WIDTH/2 - topBuyuklugu/2, CAMERA_HEIGHT - 50 - 32, top.oTextureRegion);
		
		/*
		 * Oyun a��klamalar�nda bahsetti�im g�r�nmez resim olu�turuluyor.
		 * Oyuncunun hareketini sa�layan b�t�n kod bloklar� burada yer almaktad�r		 * 
		 */
		oyuncuKontrolInvis.oSprite = new Sprite(0, CAMERA_HEIGHT-128, oyuncuKontrolInvis.oTextureRegion)
		{			
			boolean dokunmaDurumu = false;
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
			{
				if(pSceneTouchEvent.isActionDown())
				{
					dokunmaDurumu = true;
					dokunulanNoktaX = pSceneTouchEvent.getX();
					
				}
				if(pSceneTouchEvent.isActionMove())
				{
					dokunmaDurumu = true;
					dokunulanNoktaX = pSceneTouchEvent.getX();
				}
				if(pSceneTouchEvent.isActionUp())
				{
					dokunmaDurumu = false;
				}
				return true;
			}
			// Update metodunda gerekli t�m kontrolleri yap�yoruz
			protected void onManagedUpdate(float pSecondsElapsed) 
			{
				for(int i = 0; i < blokSayisiX; i++)
				{
					for(int j = 0; j < blokSayisiY; j++)
					{		
						//blokTemasKontrol boolean de�i�keni, topla temas halinde olmu� bloklar�n de�erini tutuyor
						// E�er temas olmu�sa true, olmam��sa false de�eri atan�yor
						if(blokTemasKonrol[i][j])
							bloklar[i][j].oBody.setTransform(-500, -500, 0);
					}
				}
				
				// Oyuncunun koordinatlar�na g�re kontroller yap�l�yor. 
				// Oyuncu, sadece belirli aral�klarda hareket edebilir. Ekrandan d��ar�ya ��kamaz
				ortaNokta = oyuncu.oSprite.getX() + oyuncuBuyukluguX/2;
				if(dokunulanNoktaX < ortaNokta - oyuncuHizi)
				{
					if(!dokunmaDurumu)
					{
						oyuncu.oBody.setLinearVelocity(0, 0);
					}
					else
					{
						if(dokunulanNoktaX != oyuncu.oSprite.getX() + oyuncuBuyukluguX/2)
						{
						
							if((oyuncu.oSprite.getX() > kenardanUzaklikX))
							{
								oyuncu.oBody.setLinearVelocity(-oyuncuHizi, 0);
							}
							else
							{
								oyuncu.oBody.setLinearVelocity(0, 0);
							}
						}
						else
						{
							oyuncu.oBody.setLinearVelocity(0, 0);
						}
					}
				}
				else if(dokunulanNoktaX > ortaNokta + oyuncuHizi)
				{
					if(!dokunmaDurumu)
					{
						oyuncu.oBody.setLinearVelocity(0, 0);
					}
					else
					{
						if(dokunulanNoktaX != oyuncu.oSprite.getX() + oyuncuBuyukluguX/2)
						{
						
							if((oyuncu.oSprite.getX() < CAMERA_WIDTH - (kenardanUzaklikX + oyuncuBuyukluguX)))
							{
								oyuncu.oBody.setLinearVelocity(oyuncuHizi, 0);
							}
							else
							{
								oyuncu.oBody.setLinearVelocity(0, 0);
							}
						}
						else
						{
							oyuncu.oBody.setLinearVelocity(0, 0);
						}
					}
				}
				else
				{
					oyuncu.oBody.setLinearVelocity(0, 0);
				}				
			}
		};
		
		/* Top nesnesine ait body �zelli�inin Dynamic body oldu�una
		 * Oyuncu ve Bloklar�n body t�r�n�n kinematic body oldu�una dikkat edelim.
		 * Konu anlat�mlar�ndan yola ��karak neden b�yle bir kullan�ma ba�vurdu�umuzu ��karmaya �al��al�m
		*/
		top.oBody = PhysicsFactory.createCircleBody(physicsWorld, top.oSprite, BodyType.DynamicBody, fixDef);
		oyuncu.oBody = PhysicsFactory.createBoxBody(physicsWorld, oyuncu.oSprite, BodyType.KinematicBody, fixDef);
		
		this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(top.oSprite, top.oBody, true, true));
		this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(oyuncu.oSprite, oyuncu.oBody, true, true));
		
		for(int i = 0; i < blokSayisiX; i++)
		{
			for(int j = 0; j < blokSayisiY; j++)
			{
				bloklar[i][j].oSprite = new Sprite((i*blokBuyukluguX + i) + kenardanUzaklikX, (j*blokBuyukluguY + j) + kenardanUzaklikY, bloklar[i][j].oTextureRegion); 
				this.sahneOyun.attachChild(bloklar[i][j].oSprite);
				
				bloklar[i][j].oBody = PhysicsFactory.createBoxBody(physicsWorld, bloklar[i][j].oSprite, BodyType.KinematicBody, fixDef);
				this.physicsWorld.registerPhysicsConnector(new PhysicsConnector(bloklar[i][j].oSprite, bloklar[i][j].oBody, true, true));
			}
		}
		
		oyuncuKontrolInvis.oSprite.setColor(50, 10, 120);
		
		// Nesneler ekrana �izdiriliyor
		this.sahneOyun.attachChild(oyuncu.oSprite);
		this.sahneOyun.attachChild(top.oSprite);
		this.sahneOyun.attachChild(oyuncuKontrolInvis.oSprite);
		
		// oyuncuKontrolInvis nesnesinin dokunma alan� tan�mlan�yor
		this.sahneOyun.registerTouchArea(oyuncuKontrolInvis.oSprite);
		
	}// nesneleriOlustur metodu bitti
	
	// Blok temas, 
	private void kontroller()
	{
		this.sahneOyun.registerUpdateHandler(new IUpdateHandler() {
			
			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onUpdate(float pSecondsElapsed) 
			{
				// top ekrandan ��kt���nda oyunu durdur
				if(top.oSprite.getY() > 490)
				{
					pauseMenuResume.oSprite.setPosition(-200, -200);
					sahneOyun.setChildScene(sahnePauseMenu);
					//Oyunun durdurulmas�n� sa�layan kod blo�u. Fiziksel �zelliklerin tamam� durdurulur.
					sahneOyun.unregisterUpdateHandler(physicsWorld);
				}
				if(kirilanBlokSayisi == (blokSayisiX*blokSayisiY))
				{
					pauseMenuResume.oSprite.setPosition(-200, -200);
					sahneOyun.setChildScene(sahnePauseMenu);
					//Oyunun durdurulmas�n� sa�layan kod blo�u. Fiziksel �zelliklerin tamam� durdurulur.
					sahneOyun.unregisterUpdateHandler(physicsWorld);
				}
				
			}
		});
		
		// top-blok ve top-oyuncu dokunma durumunu kontrol eden ContactListener yap�s�
		this.physicsWorld.setContactListener(new ContactListener() 
		{
			@Override
			public void beginContact(Contact contact) 
			{
				
				for(int i = 0; i < blokSayisiX; i++)
				{
					for(int j = 0; j < blokSayisiY; j++)
					{
						final int x = i;
						final int y = j;
						if(contact.getFixtureA().getBody() == physicsWorld.getPhysicsConnectorManager().findBodyByShape(bloklar[x][y].oSprite))
						{
							if(contact.getFixtureB().getBody() == physicsWorld.getPhysicsConnectorManager().findBodyByShape(top.oSprite))
							{
								if(soundCarpma != null)
								{
									soundCarpma.play();
								}
								
								blokDegerleri[x][y] --;
								
								if(!blokTemasKonrol[i][j])
								{
									
									if(top.oSprite.getX() < bloklar[x][y].oSprite.getX() + blokBuyukluguX && top.oSprite.getX() + topBuyuklugu > bloklar[x][y].oSprite.getX())
									{
										if(top.oBody.getLinearVelocity().y < 0)
										{
											top.oBody.setLinearVelocity(top.oBody.getLinearVelocity().x, - topHizi);
										}
										else if(top.oBody.getLinearVelocity().y > 0)
										{
											top.oBody.setLinearVelocity(top.oBody.getLinearVelocity().x, topHizi);
										}
									}
									else
									{
										top.oBody.setLinearVelocity(-top.oBody.getLinearVelocity().x, top.oBody.getLinearVelocity().y);
									}
									
									if(blokDegerleri[x][y] == 2)
									{
										
									}
									else if(blokDegerleri[x][y] == 1)
									{
										
									}
									else if(blokDegerleri[x][y] == 0)
									{
										kirilanBlokSayisi++;
										blokTemasKonrol[i][j] = true;
									}
								}
							}
						}
						
					}
				}
				
				if(contact.getFixtureA().getBody() == physicsWorld.getPhysicsConnectorManager().findBodyByShape(oyuncu.oSprite))
				{
					if(contact.getFixtureB().getBody() == physicsWorld.getPhysicsConnectorManager().findBodyByShape(top.oSprite))
					{
						//bloklar[blokSayisiX -1][blokSayisiY - 1].oBody.setType(BodyType.DynamicBody);
						
						bloklar[1][1].oBody.setTransform(-200, -200, 0);
						if(top.oSprite.getX() < ortaNokta - 5)
						{
							top.oBody.setLinearVelocity((top.oSprite.getX() - ortaNokta)/20, topHizi );
						}
						else if(top.oSprite.getX() > ortaNokta + 5)
						{
							if(top.oBody.getLinearVelocity().y < oyuncuHizi || top.oBody.getLinearVelocity().y < -oyuncuHizi )
							{
								top.oBody.getLinearVelocity().y += 4;
								top.oBody.setLinearVelocity((top.oSprite.getX() - ortaNokta)/20, topHizi );
							}
							if(top.oBody.getLinearVelocity().y > oyuncuHizi || top.oBody.getLinearVelocity().y > -oyuncuHizi)
							{
								top.oBody.getLinearVelocity().y -= 4;
								top.oBody.setLinearVelocity((top.oSprite.getX() - ortaNokta)/20, topHizi );
							}
						}
					}
				}
			}
			
			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void endContact(Contact contact) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	// Oyunu yeniden ba�latan, de�erleri s�f�rlayan metot
	private void restart()
	{
		oyunSahnesiMi = true;
		pauseMenuSahnesiMi = false;
		sahneOyun.clearChildScene();
		
		//Fiziksel �zellikler etkinle�tirilir.
		sahneOyun.registerUpdateHandler(physicsWorld);
		kirilanBlokSayisi = 0;
		
		for(int i = 0; i < blokSayisiX; i++)
		{
			for(int j = 0; j < blokSayisiY; j++)
			{
				blokTemasKonrol[i][j] = false;
			}
		}

		pauseMenuResume.oSprite.setPosition(467, 209);
		
		for(int i = 0; i < blokSayisiX; i++)
		{
			for(int j = 0; j < blokSayisiY; j++)
			{
				bloklar[i][j].oBody.setTransform(((i*blokBuyukluguX + i) + 2*kenardanUzaklikX)/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, ((j*blokBuyukluguY + j) + 2*kenardanUzaklikY)/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0); 
				
			}
		}
		
		oyuncu.oBody.setTransform((CAMERA_WIDTH/2)/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, (CAMERA_HEIGHT - 50)/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		top.oBody.setTransform((CAMERA_WIDTH/2)/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, (CAMERA_HEIGHT - 50 - 32)/PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		top.oBody.setLinearVelocity(0, topHizi);
	}
	// Fiziksel tu�lar�n kullan�m�na olanak veren onKeyDown Metodu
		@Override
		public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent)
		{
			// Geri Tu�una bas�ld���nda yap�lacaklar
			if(pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) 
			{
				if(oyunSahnesiMi)
				{
					pauseMenuSahnesiMi = true;
    				oyunSahnesiMi = false;
					//Oyunun durdurulmas�n� sa�layan kod blo�u. Fiziksel �zelliklerin tamam� durdurulur.
					sahneOyun.unregisterUpdateHandler(physicsWorld);
					sahneOyun.setChildScene(sahnePauseMenu);
				}
				else if(pauseMenuSahnesiMi)
				{
					pauseMenuSahnesiMi = false;
    				oyunSahnesiMi = true;
    				//Fiziksel �zellikler etkinle�tirilir.
					sahneOyun.registerUpdateHandler(physicsWorld);
					sahneOyun.clearChildScene();
				}
				else if(anaMenuSahnesiMi)
				{
					System.exit(0);
				}
				else
				{
					
				}
	            
				return true;
			}
			// Menu tu�una bas�ld���nda yap�lacaklar
			else if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) 
			{		
				// Bu k�s�mda bir �ey yapmaya gerek duymad�k
				// Ama istenilen bir g�rev buraya yaz�labilir.
				return true;
			}
			else 
			{
				return super.onKeyDown(pKeyCode, pEvent);
			}
		}
}