package user.ai_camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.net.URL;

public class RetrieveImageTask extends AsyncTask<String, String, Bitmap> {
    private boolean IsFirstPosition,IsCollection;
    private Exception exception;
    private ImageButton ib;
    private ImageButton ib_photo;
    private Point size;
    public Bitmap img;
    private ImageView iv_pass_unlike_or_suggestion;
    public boolean update_ib_photo = false;

    public RetrieveImageTask(ImageButton ib, Point size, boolean IsFirstPosition) {
        this.ib = ib;
        this.IsFirstPosition = IsFirstPosition;
        this.size = size;
    }
    public RetrieveImageTask(Point size, boolean IsFirstPosition){
        this.ib = null;
        this.IsFirstPosition = IsFirstPosition;
        this.size = size;
    }
    public RetrieveImageTask(ImageButton ib, Point size, boolean IsFirstPosition,boolean IsCollection){
        this.ib = ib;
        this.IsFirstPosition = IsFirstPosition;
        this.size = size;
        this.IsCollection = IsCollection;
    }
    public RetrieveImageTask(ImageButton ib, Point size, boolean IsFirstPosition,boolean IsCollection,ImageView iv_pass_unlike_or_suggestion){
        this.ib = ib;
        this.IsFirstPosition = IsFirstPosition;
        this.size = size;
        this.IsCollection = IsCollection;
        this.iv_pass_unlike_or_suggestion = iv_pass_unlike_or_suggestion;
    }

    /* Access modifiers changed, original: protected|varargs */
    /* JADX WARNING: Missing block: B:3:0x0018, code skipped:
            if (r0 != null) goto L_0x001a;
     */
    /* JADX WARNING: Missing block: B:4:0x001a, code skipped:
            android.util.Log.d("response Image", "Image load success");
     */
    /* JADX WARNING: Missing block: B:5:0x0022, code skipped:
            android.util.Log.d("response Image", "Image load failed");
     */
    /* JADX WARNING: Missing block: B:10:0x002f, code skipped:
            if (r0 == null) goto L_0x0022;
     */
    /* JADX WARNING: Missing block: B:11:0x0032, code skipped:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Bitmap doInBackground(String... urls) {
        Bitmap bmp = null;
        try {
            Log.d("RTask image url",urls[0]);
            bmp = BitmapFactory.decodeStream(new URL(urls[0]).openConnection().getInputStream());
        } catch (Exception e) {
            this.exception = e;
        }
        if (bmp != null) {
            //Log.d("RTask image response", "Image load success");
        } else {
            Log.d("RTask image response", "Image load failed");
        }
        return  bmp;
    }

    /* Access modifiers changed, original: protected */

    public void onPostExecute(Bitmap img) {
        if (this.IsFirstPosition && !this.IsCollection) {
            if(ib != null){
                this.ib.setImageBitmap(Bitmap.createScaledBitmap(img, (int) (size.x * 0.85d), (int) (size.y * 0.7d), true));
            }
        }else if (!IsFirstPosition && this.IsCollection){
            if(ib != null){
                this.ib.setImageBitmap(Bitmap.createScaledBitmap(img, (int) (size.x*0.25d), (int) (size.x * 0.25d ), true));
            }
        }
        else if(!this.IsFirstPosition &! this.IsCollection){
            if(ib != null){
                this.ib.setImageBitmap(Bitmap.createScaledBitmap(img, (int) (size.x), (int) (size.x * 0.6d ), true));
            }
        }else { // this.IsFirstPosition & this.IsCollection
            if(ib != null){
                this.ib.setImageBitmap(Bitmap.createScaledBitmap(img, (int) (size.x*0.25d), (int) (size.x * 0.25d ), true));
                iv_pass_unlike_or_suggestion.setVisibility(View.GONE);
            }
        }
        Log.d("RTask test","end");
        this.img = img;
        if(update_ib_photo){
        	Log.d("Update ib photo","in");
        	ib_photo.setImageBitmap(img);
		}
    }

    public Bitmap resultBack(){
        return img;
    }
    public void InformClickedWithoutImage(ImageButton ib_photo){
    	this.ib_photo = ib_photo;
    	update_ib_photo = true;
	}
}
