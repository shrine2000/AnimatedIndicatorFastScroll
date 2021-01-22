package example.indicatorfastscroll


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Typeface
import android.os.Bundle
import android.os.UserManager
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.reddit.indicatorfastscroll.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*


val appList: MutableList<AppModel> = mutableListOf()
lateinit var recyclerView: RecyclerView
lateinit var fastScroller: FastScrollerView


class MainActivity : AppCompatActivity() {


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        fastScroller = findViewById<FastScrollerView>(R.id.fastscroller)
        val fastScrollerThumb = findViewById<FastScrollerThumbView>(R.id.fastscroller_thumb)
        val appPreferences = AppPreferences(this)
        populateList()


        recyclerView.setup {
            withDataSource(appList.toDataSource())
            withItem<AppModel, ModelViewHolder>(R.layout.recyclerview_layout) {
                onBind(::ModelViewHolder) { _, item ->

                    if (item.appIcon != null) {
                        if (appPreferences.customFontPath.isNotEmpty()) {
                            val typeface = Typeface.createFromFile(File(appPreferences.customFontPath))
                            appName.typeface = typeface
                            appPackageName.typeface = typeface
                        }

                        appName.text = item.appName
                        appPackageName.text = item.appPackage
                        appIcon.setImageDrawable(item.appIcon)
                    }
                }
                onClick { index ->
                   item.appIcon?.let {
                       Toast.makeText(
                               this@MainActivity,
                               appList[index].appPackage,
                               Toast.LENGTH_SHORT
                       ).show()
                   }
                }
            }
        }


        findViewById<Button>(R.id.button_select_font).setOnClickListener {
            selectFontPath()
        }


        var widgetXOrigin = 0F
        var widgetDX = 0F
        var fastScrollerThumbViewXOrigin = 0F
        var widgetFastScrollerThumbViewXOrigin = 0F

        fastScroller.setOnTouchListener { v, event ->

            val viewParent: View = (v.parent as View)

            val parentWidth = viewParent.width

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    widgetDX = v.x - event.rawX
                    widgetFastScrollerThumbViewXOrigin = fastScrollerThumb.x - event.rawX

                    // save widget origin coordinate
                    widgetXOrigin = v.x
                    fastScrollerThumbViewXOrigin = fastScrollerThumb.x
                }
                MotionEvent.ACTION_MOVE -> {
                    // Screen border Collision
                    var newX = event.rawX + widgetDX
                    newX = 0F.coerceAtLeast(newX)
                    newX = (parentWidth - v.width).toFloat().coerceAtMost(newX)
                    v.animate().x(newX).setDuration(0).start()


                    var newX2 = event.rawX + widgetFastScrollerThumbViewXOrigin
                    newX2 = 0F.coerceAtLeast(newX2)
                    newX2 = (parentWidth - fastScrollerThumb.width).toFloat().coerceAtMost(newX2)
                    fastScrollerThumb.animate().x(newX2).setDuration(0).start()


                }
                MotionEvent.ACTION_UP -> {
                    // Back to original position
                    v.animate().x(widgetXOrigin).setDuration(250).start()
                    fastScrollerThumb.animate().x(fastScrollerThumbViewXOrigin).setDuration(250).start()
                }
                else -> {
                    return@setOnTouchListener false
                }
            }
            false
        }

        fastScroller.setupWithRecyclerView(
                recyclerView,
                { position ->

                    val item = appList[position].appName


                    FastScrollItemIndicator.Text(
                            item.toCharArray()[0].toString().toUpperCase()
                    )
                }
        )

        fastScrollerThumb.setupWithFastScroller(fastScroller)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_FONT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            data?.data?.let { documentUri ->

                contentResolver.takePersistableUriPermission(
                        documentUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val fontName = System.currentTimeMillis().toString() + ".ttf"
                AppPreferences(this).customFontPath = "" // clear previous path to prevent path concatenation

                val inputStream: InputStream? = contentResolver.openInputStream(documentUri)
                val out: OutputStream = FileOutputStream(File("$filesDir/$fontName"))
                val buf = ByteArray(1024)
                var len: Int
                while (inputStream?.read(buf).also { len = it!! }!! > 0) {
                    out.write(buf, 0, len)
                }
                out.close()
                inputStream?.close()

                AppPreferences(this).customFontPath = "$filesDir/$fontName"

                if (::recyclerView.isInitialized) {
                    recyclerView.adapter?.notifyDataSetChanged()
                }

            }
        }
    }


    private fun populateList() {

        try {
            val userManager = getSystemService(Context.USER_SERVICE) as UserManager
            val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

            appList.clear()

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

            if (::recyclerView.isInitialized) {
                recyclerView.adapter?.notifyDataSetChanged()
            }


        } catch (e: java.lang.Exception) {

        }
    }

    private fun selectFontPath() {

        Toast.makeText(applicationContext, "Download .ttf fonts & select them", Toast.LENGTH_LONG).show()

        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivityForResult(this, OPEN_FONT_REQUEST_CODE)
        }

    }


}



