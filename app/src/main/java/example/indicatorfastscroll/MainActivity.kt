package example.indicatorfastscroll


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import com.reddit.indicatorfastscroll.FastScrollerThumbView
import com.reddit.indicatorfastscroll.FastScrollerView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val fastscroller = findViewById<FastScrollerView>(R.id.fastscroller)
        val fastscroller_thumb = findViewById<FastScrollerThumbView>(R.id.fastscroller_thumb)

        fastscroller.setupWithRecyclerView(
            recyclerView,
            { position ->
                val item = getData()[position].title

                FastScrollItemIndicator.Text(
                    item.substring(0, 1).toUpperCase()
                )
            },

            )



        fastscroller_thumb.setupWithFastScroller(fastscroller)


        recyclerView.setup {
            withDataSource(getData())
            withItem<Model, ModelViewHolder>(R.layout.recyclerview_layout) {
                onBind(::ModelViewHolder) { index, item ->

                    title.text = item.title
                    subtitle.text = "${item.subtitle}"
                }
                onClick { index ->
                    Toast.makeText(
                        this@MainActivity,
                        getData()[index].toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}