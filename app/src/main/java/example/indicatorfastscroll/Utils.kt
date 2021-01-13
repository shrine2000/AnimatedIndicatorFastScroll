package example.indicatorfastscroll


import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.recyclical.ViewHolder
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.datasource.dataSourceTypedOf


const val OPEN_DOCUMENT_REQUEST_CODE = 345

const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

data class AppModel(
        var appName: String,
        var appPackage: String,
        var appIcon: Drawable?
)


class ModelViewHolder(itemView: View) : ViewHolder(itemView) {
    val appName: TextView = itemView.findViewById(R.id.tv_title)
    val appPackageName: TextView = itemView.findViewById(R.id.tv_subtitle)
    val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
}



fun MutableList<AppModel>.toDataSource(): DataSource<AppModel> {
    val dataSource = dataSourceTypedOf<AppModel>()
    this.forEach {
        dataSource.add(AppModel(it.appName, it.appPackage, it.appIcon))
    }
    return dataSource
}

fun String.isNotEmpty() = this != ""


fun getAllChildren(v: View): ArrayList<View> {
    if (v !is ViewGroup) {
        val viewArrayList = ArrayList<View>()
        viewArrayList.add(v)
        return viewArrayList
    }
    val result = ArrayList<View>()
    for (i in 0 until v.childCount) {
        val child = v.getChildAt(i)
        val viewArrayList = ArrayList<View>()
        viewArrayList.add(v)
        viewArrayList.addAll(getAllChildren(child))
        result.addAll(viewArrayList)
    }
    return result
}


