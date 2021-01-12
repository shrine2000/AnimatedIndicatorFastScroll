package example.indicatorfastscroll


import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.recyclical.ViewHolder
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.datasource.dataSourceTypedOf


const val OPEN_DOCUMENT_REQUEST_CODE = 345

data class AppModel(
    var appName: String,
    var appPackage: String,
    var appIcon: Drawable
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