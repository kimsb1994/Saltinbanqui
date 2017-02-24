package quim.cat.saltinbanqui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PlatformView extends SurfaceView implements Runnable {

    private boolean debugging = true;
    private volatile boolean running;
    private Thread gameThread = null;
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder ourHolder;
    Context context;
    long startFrameTime;
    long timeThisFrame;
    long fps;
    private LevelManager lm;
    private ViewPort vp;
    InputController ic;

    PlatformView(Context context, int screenWidth,int screenHeight) {
        super(context);
        this.context = context;
        ourHolder = getHolder();
        paint = new Paint();
        vp = new ViewPort(screenWidth, screenHeight);
        loadLevel("LevelCave", 15, 2);
    }
    public void loadLevel(String level, float px, float py) {
        lm = null;
        // Create a new LevelManager
        // Pass in a Context, screen details, level name// and player location
        lm = new LevelManager(context,vp.getPixelsPerMetreX(),vp.getScreenWidth(),ic, level, px, py);
        ic = new InputController(vp.getScreenWidth(),vp.getScreenHeight());
        // Set the players location as the world centre
        vp.setWorldCentre(lm.gameObjects.get(lm.playerIndex).getWorldLocation().x, lm.gameObjects.get(lm.playerIndex).getWorldLocation().y);
    }


    @Override
    public void run() {
        while (running) {
            startFrameTime = System.currentTimeMillis();
            update();
            draw();
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            } }
    }

    private void update() {
        for (GameObject go : lm.gameObjects) {
            if (go.isActive()) {
                // Clip anything off-screen
                if (!vp.clipObjects(go.getWorldLocation().x, go.getWorldLocation().y, go.getWidth(), go.getHeight())) {
                    // Set visible flag to true
                    go.setVisible(true);
                } else {
                    // Set visible flag to false
                    go.setVisible(false);
                    // Now draw() can ignore them
                } }
        } }

    private void draw() {
        if (ourHolder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = ourHolder.lockCanvas();
            // Rub out the last frame with arbitrary color paint.setColor(Color.argb(255, 0, 0, 255)); canvas.drawColor(Color.argb(255, 0, 0, 255)); // Draw all the GameObjects
            Rect toScreen2d = new Rect();
            // Draw a layer at a time
            for (int layer = -1; layer <= 1; layer++){
                for (GameObject go : lm.gameObjects) {
                    //Only draw if visible and this layer
                    if (go.isVisible() && go.getWorldLocation().z == layer) {

                    }
                    toScreen2d.set(vp.worldToScreen(go.getWorldLocation().x,go.getWorldLocation().y,go.getWidth(),go.getHeight()));
                    // Draw the appropriate bitmap
                canvas.drawBitmap(lm.bitmapsArray[lm.getBitmapIndex(go.getType())],toScreen2d.left,toScreen2d.top, paint);}
            }// Text for debugging

        if (debugging) {
            paint.setTextSize(16);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(Color.argb(255, 255, 255, 255));
            canvas.drawText("fps:" + fps, 10, 60, paint);
            canvas.drawText("num objects:" + lm.gameObjects.size(), 10, 80, paint);
            canvas.drawText("num clipped:" + vp.getNumClipped(), 10, 100, paint);
            canvas.drawText("playerX:" + lm.gameObjects.get(lm.playerIndex).getWorldLocation().x,10, 120, paint);
            canvas.drawText("playerY:" + lm.gameObjects.get(lm.playerIndex).getWorldLocation().y,10, 140, paint);
            //for reset the number of clipped objects each frame
            vp.resetNumClipped();
        }// End if(debugging)
            // Unlock and draw the scene
            ourHolder.unlockCanvasAndPost(canvas);
        }// End (ourHolder.getSurface().isValid())
    }// End draw()


    public void pause() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("error", "failed to pause thread");
        }
    }
    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}// End of constructor
