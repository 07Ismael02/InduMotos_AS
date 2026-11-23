package pe.indumotos.mobile

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal actual fun decodeGuideImage(bytes:ByteArray):ImageBitmap? = runCatching {
    val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true}
    BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
    val sample=BitmapFactory.Options().apply{inSampleSize=generateSequence(1){it*2}.first{bounds.outWidth/it<=2048&&bounds.outHeight/it<=2048}}
    BitmapFactory.decodeByteArray(bytes,0,bytes.size,sample)?.asImageBitmap()
}.getOrNull()
