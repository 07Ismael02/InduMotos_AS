package pe.indumotos.mobile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import pe.indumotos.mobile.domain.GuidePhoto

internal fun readCameraPhoto(file:File):GuidePhoto {
    require(file.length() in 1..40L*1024*1024){"La cámara no guardó una fotografía válida de hasta 40 MB."}
    val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true}
    BitmapFactory.decodeFile(file.absolutePath,bounds)
    require(bounds.outWidth>=600&&bounds.outHeight>=600){"La fotografía debe medir al menos 600 × 600 px."}
    val options=BitmapFactory.Options().apply{inSampleSize=1}
    while(maxOf(bounds.outWidth,bounds.outHeight)/options.inSampleSize>3200)options.inSampleSize*=2
    val original=BitmapFactory.decodeFile(file.absolutePath,options)?:error("No se pudo leer la fotografía.")
    var rotated:Bitmap?=null
    try {
        val orientation=ExifInterface(file.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)
        val matrix=Matrix().apply{when(orientation){
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL->setScale(-1f,1f)
            ExifInterface.ORIENTATION_ROTATE_180->setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL->setScale(1f,-1f)
            ExifInterface.ORIENTATION_TRANSPOSE->{setRotate(90f);postScale(-1f,1f)}
            ExifInterface.ORIENTATION_ROTATE_90->setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE->{setRotate(270f);postScale(-1f,1f)}
            ExifInterface.ORIENTATION_ROTATE_270->setRotate(270f)
        }}
        rotated=Bitmap.createBitmap(original,0,0,original.width,original.height,matrix,true)
        val out=ByteArrayOutputStream()
        var quality=90
        do{out.reset();check(rotated.compress(Bitmap.CompressFormat.JPEG,quality,out));quality-=10}while(out.size()>5*1024*1024&&quality>=50)
        require(out.size()<=5*1024*1024){"La fotografía excede 5 MB. Intenta nuevamente con menor resolución."}
        require(minOf(rotated.width,rotated.height)>=600){"Usa una fotografía de mayor resolución."}
        return GuidePhoto("Fotografía.jpg","image/jpeg",out.toByteArray())
    }finally{
        if(rotated!==original)rotated?.recycle()
        original.recycle()
    }
}
