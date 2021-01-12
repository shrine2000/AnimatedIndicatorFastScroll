package example.indicatorfastscroll

import android.view.View
import android.widget.TextView
import com.afollestad.recyclical.ViewHolder
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.datasource.dataSourceTypedOf
import java.util.*
import kotlin.random.Random

private val ALPHA_NUMERIC = ('0'..'9') + ('A'..'Z') + ('a'..'z')
private const val LENGTH = 7


data class Model(
    var title: String,
    var subtitle: Int
)

class ModelViewHolder(itemView: View) : ViewHolder(itemView) {
    val title: TextView = itemView.findViewById(R.id.tv_title)
    val subtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
}


fun getData(): DataSource<Model> {
    val dataSource = dataSourceTypedOf<Model>()
    repeat((1..40).count()) {
        dataSource.add(Model(generateId(), ((Math.random() * 9000) + 1000000).toInt()))
    }

    val sortedData = dataSource.toList().sortedWith { t1, t2 ->
        t1.title.compareTo(t2.title)
    }
    dataSource.clear()

    sortedData.forEach {
        dataSource.add(it)
    }

    return dataSource
}

fun generateId() = List(LENGTH) { Random.nextInt(0, ALPHA_NUMERIC.size) }
    .map { ALPHA_NUMERIC[it] }
    .joinToString(separator = "")


