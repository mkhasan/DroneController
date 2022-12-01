package com.drone.pi.dronecontroller.GLSurface;

/**
 * Created by usrc on 17. 12. 28.
 */

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.drone.pi.dronecontroller.R;
import com.drone.pi.dronecontroller.Shapes.Spheres;
import com.drone.pi.dronecontroller.Utils.TextureHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.opengles.GL10;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES20 is used instead.
 */
public class ShapeRenderer implements GLSurfaceView.Renderer
{
    /** Used for debug logs. */
    private static final String TAG = "ShapeRenderer";

    private final Activity aShapeActivity;
    private final GLSurfaceView aGlSurfaceView;
    private final int aShapeNumber;

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] aModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] aViewMatrix = new float[16];

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
    private float[] aProjectionMatrix = new float[16];

    /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
    private float[] aMVPMatrix = new float[16];

    /** Store the accumulated rotation. */
    private final float[] aAccumulatedRotation = new float[16];

    private float verticalRotation = 0.0f;

    private float horizontalRotation = 0.0f;

    private final float maxRotation = 45.0f;
    /** Store the current rotation. */
    private final float[] aCurrentRotation = new float[16];

    /** A temporary matrix. */
    private float[] aTemporaryMatrix = new float[16];

    /**
     * Stores a copy of the model matrix specifically for the light position.
     */
    private float[] aLightModelMatrix = new float[16];

    /** This will be used to pass in the light position. */
    private int aLightPosHandle;

    /** Size of the position data in elements. */
    static final int POSITION_DATA_SIZE = 3;

    /** Size of the normal data in elements. */
    static final int NORMAL_DATA_SIZE = 3;

    /** Size of the texture coordinate data in elements. */
    static final int TEXTURE_COORDINATE_DATA_SIZE = 2;

    /** How many bytes per float. */
    static final int BYTES_PER_FLOAT = 4;

    private static final int PLANE_INDICES_SIZE = 6;

    private static final int COLOR_DATA_SIZE = 4;

    /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     *  we multiply this by our transformation matrices. */
    private final float[] aLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

    /** Used to hold the current position of the light in world space (after transformation via model matrix). */
    private final float[] aLightPosInWorldSpace = new float[4];

    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
    private final float[] aLightPosInEyeSpace = new float[4];

    /** Texture to draw */
    private static int aTexture;

    // Shapes objects
    private Spheres aSpheres;


    private static final int PLOT_RANGE = 240;
    private static final int PLOT_MIN_POSITION = -120;

    // These still work without volatile, but refreshes are not guaranteed to happen.
    public volatile float aDeltaX;
    public volatile float aDeltaY;

    /** Thread executor for generating data points in the background. */
    private final ExecutorService aSingleThreadedExecutor = Executors.newSingleThreadExecutor();

    /** The current shape variables. */
    private int _width, _height;
    private float aPrevTime;
    private boolean aRotationStatus = false;

    /**
     * Initialize the model data.
     */
    public ShapeRenderer(final Activity shapeActivity, final GLSurfaceView glSurfaceView, int shapeNumber) {
        aShapeActivity = shapeActivity;
        aGlSurfaceView = glSurfaceView;
        aShapeNumber = shapeNumber;
    }

    private void generatePlots(int aShapeNumber) {
        aSingleThreadedExecutor.submit(new GenDataRunnable(aShapeNumber));
    }

    class GenDataRunnable implements Runnable {

        private final int shapeNumner;
        GenDataRunnable(int aShapeNumber) {
            shapeNumner = aShapeNumber;
        }

        @Override
        public void run() {
            try {

                // Run on the GL thread -- the same thread the other members of the renderer run in.
                aGlSurfaceView.queueEvent(new Runnable() {

                    @Override
                    public void run() {

                        // Not supposed to manually call this, but Dalvik sometimes needs some additional prodding to clean up the heap.
                        System.gc();

                        try {
                            float[] positions = new float[]{0, 0, 0};
                            float[] colors = new float[]{0, 1, 0, 1};
                            float[] radii = new float[]{1.0f};
                            aSpheres = new Spheres(aShapeActivity, 0, 50, positions, colors, radii);


                        } catch (OutOfMemoryError err) {

                            // Not supposed to manually call this, but Dalvik sometimes needs some additional prodding to clean up the heap.
                            System.gc();

                            aShapeActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//									Toast.makeText(mLessonSevenActivity, "Out of memory; Dalvik takes a while to clean up the memory. Please try again.\nExternal bytes allocated=" + dalvik.system.VMRuntime.getRuntime().getExternalBytesAllocated(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            } catch (OutOfMemoryError e) {
                // Not supposed to manually call this, but Dalvik sometimes needs some additional prodding to clean up the heap.
                System.gc();

                aShapeActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//						Toast.makeText(mLessonSevenActivity, "Out of memory; Dalvik takes a while to clean up the memory. Please try again.\nExternal bytes allocated=" + dalvik.system.VMRuntime.getRuntime().getExternalBytesAllocated(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, javax.microedition.khronos.egl.EGLConfig config)
    {
        aTexture = TextureHelper.loadTexture(aShapeActivity, R.drawable.arrow_tiny
                , false);
        generatePlots(aShapeNumber);



        // Set the background clear color to black.
  //      GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Use culling to remove back faces.
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);




        // Position the eye in front of the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -0.0f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(aViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        //aAndroidDataHandle = a[0];
        // Initialize the accumulated rotation matrix
        Matrix.setIdentityM(aAccumulatedRotation, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {

        _width = width;
        _height = height;

        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 5000.0f;

        Matrix.frustumM(aProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // Calculate position of the light. Push into the distance.
        Matrix.setIdentityM(aLightModelMatrix, 0);
        Matrix.translateM(aLightModelMatrix, 0, 1.0f, 0.0f, -1.5f);

        Matrix.multiplyMV(aLightPosInWorldSpace, 0, aLightModelMatrix, 0, aLightPosInModelSpace, 0);
        Matrix.multiplyMV(aLightPosInEyeSpace, 0, aViewMatrix, 0, aLightPosInWorldSpace, 0);

        // Translate the cube into the screen.
        Matrix.setIdentityM(aModelMatrix, 0);
        Matrix.translateM(aModelMatrix, 0, 0, 0, -3.5f);

        if(aDeltaX != 0 || aDeltaY != 0) {
            aRotationStatus = false;
        }
        if(aRotationStatus) {
            Matrix.rotateM(aModelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(aModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
            Matrix.rotateM(aModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        }

        // Set a matrix that contains the current rotation.


        if(Math.abs(aDeltaX) >= Math.abs(aDeltaY))
            verticalRotation += aDeltaX;
        else
            horizontalRotation += aDeltaY;
        if(verticalRotation > maxRotation)
            verticalRotation = maxRotation;
        if (verticalRotation < -maxRotation)
            verticalRotation = -maxRotation;

        if(horizontalRotation > maxRotation)
            horizontalRotation = maxRotation;
        if(horizontalRotation < -maxRotation)
            horizontalRotation = -maxRotation;



        Matrix.setIdentityM(aCurrentRotation, 0);

        Matrix.rotateM(aCurrentRotation, 0, verticalRotation, 0.0f, 1.0f, 0.0f);

        Matrix.rotateM(aCurrentRotation, 0, horizontalRotation, 1.0f, 0.0f, 0.0f);

        //Matrix.setIdentityM(aCurrentRotation, 0);

        /*
        if(Math.abs(aDeltaX) >= Math.abs(aDeltaY))
            Matrix.rotateM(aCurrentRotation, 0, aDeltaX, 0.0f, 1.0f, 0.0f);
        else
            Matrix.rotateM(aCurrentRotation, 0, aDeltaY, 1.0f, 0.0f, 0.0f);

        Log.e(TAG, "aDeltaX " + aDeltaX + " aDeltaY " + aDeltaY);
        */

        //Log.e(TAG, "horizontalRotation " + horizontalRotation + " verticalRotation " + verticalRotation);

        aDeltaX = 0.0f;
        aDeltaY = 0.0f;

        // Multiply the current rotation by the accumulated rotation, and then set the accumulated rotation to the result.
        //Matrix.multiplyMM(aTemporaryMatrix, 0, aCurrentRotation, 0, aAccumulatedRotation, 0);
        System.arraycopy(aCurrentRotation, 0, aAccumulatedRotation, 0, 16);

        // Rotate the cube taking the overall rotation into account.
        Matrix.multiplyMM(aTemporaryMatrix, 0, aModelMatrix, 0, aAccumulatedRotation, 0);
        System.arraycopy(aTemporaryMatrix, 0, aModelMatrix, 0, 16);

        // This multiplies the view matrix by the model matrix, and stores
        // the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(aMVPMatrix, 0, aViewMatrix, 0, aModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix,
        // and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(aTemporaryMatrix, 0, aProjectionMatrix, 0, aMVPMatrix, 0);
        System.arraycopy(aTemporaryMatrix, 0, aMVPMatrix, 0, 16);

        if(aSpheres != null){
            aSpheres.render(aMVPMatrix, aTexture);
        }

    }

}
