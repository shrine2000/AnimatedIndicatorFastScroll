package example.indicatorfastscroll


import android.content.Context
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.UserManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.datasource.dataSourceTypedOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import com.reddit.indicatorfastscroll.FastScrollerThumbView
import com.reddit.indicatorfastscroll.FastScrollerView
import java.util.*


val appList: MutableList<AppModel> = mutableListOf()


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val fastScroller = findViewById<FastScrollerView>(R.id.fastscroller)
        val fastScrollerThumb = findViewById<FastScrollerThumbView>(R.id.fastscroller_thumb)

        populateList()

        fastScroller.setupWithRecyclerView(
            recyclerView,
            { position ->
                val item = appList[position].appName

                FastScrollItemIndicator.Text(
                    item.substring(0, 1).toUpperCase()
                )
            },

            )



        fastScrollerThumb.setupWithFastScroller(fastScroller)


        recyclerView.setup {
            withDataSource(appList.toDataSource())
            withItem<AppModel, ModelViewHolder>(R.layout.recyclerview_layout) {
                onBind(::ModelViewHolder) { index, item ->
                    appName.text = item.appName
                    appPackageName.text = item.appPackage
                    appIcon.setImageDrawable(item.appIcon)
                }
                onClick { index ->
                    Toast.makeText(
                        this@MainActivity,
                        appList[index].appPackage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun populateList() {

        try {
            val userManager = getSystemService(Context.USER_SERVICE) as UserManager
            val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

            for (profile in userManager.userProfiles) {
                for (app in launcherApps.getActivityList(null, profile)) {
                    val appIcon = packageManager.getApplicationIcon(app.applicationInfo.packageName)
                    appList.add(
                        AppModel(
                            app.label.toString(),
                            app.applicationInfo.packageName,
                            appIcon
                        )
                    )
                }
            }
            appList.sortBy { it.appName.toLowerCase(Locale.ROOT) }

        } catch (e: java.lang.Exception) {

        }
    }
}


private fun MutableList<AppModel>.toDataSource(): DataSource<AppModel> {
    val dataSource = dataSourceTypedOf<AppModel>()
    this.forEach {
        dataSource.add(AppModel(it.appName, it.appPackage, it.appIcon))
    }
    return dataSource
}

