package example.indicatorfastscroll


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Typeface
import android.os.Bundle
import android.os.UserManager
import android.util.Log
import android.view.DragEvent
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


var X = 0f
var Y = 0f



open class MainActivity : AppCompatActivity() {

    private val TAG ="mmxx"


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
                onBind(::ModelViewHolder) { index, item ->

                    if (appPreferences.customFontPath.isNotEmpty()) {
                        val typeface = Typeface.createFromFile(File(appPreferences.customFontPath))
                        appName.typeface = typeface
                        appPackageName.typeface = typeface
                    }

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


        findViewById<Button>(R.id.button_select_font).setOnClickListener {
            selectFontPath()
        }


        var widgetXOrigin : Float = 0F
        var widgetYOrigin : Float = 0F
        var widgetDX: Float = 0F

        var widgetDY: Float = 0F

        fastScroller.setOnTouchListener { v, event ->

            val viewParent:View = (v.parent as View)
            val PARENT_HEIGHT = viewParent.height
            val PARENT_WIDTH = viewParent.width

            when(event.actionMasked){
                MotionEvent.ACTION_DOWN -> {
                    widgetDX = v.x - event.rawX
                    widgetDY = v.y - event.rawY
                    // save widget origin coordinate
                    widgetXOrigin = v.x
                    widgetYOrigin = v.y
                }
                MotionEvent.ACTION_MOVE -> {
                    // Screen border Collision
                    var newX = event.rawX + widgetDX
                    newX = 0F.coerceAtLeast(newX)
                    newX = (PARENT_WIDTH - v.width).toFloat().coerceAtMost(newX)
                    v.x = newX

                    var newY = event.rawY + widgetDY
                    newY = 0F.coerceAtLeast(newY)
                    newY = (PARENT_HEIGHT - v.height).toFloat().coerceAtMost(newY)
                    v.y = newY


                }
                MotionEvent.ACTION_UP -> {
                    // Back to original position
                    v.x = widgetXOrigin
                    v.y = widgetYOrigin

                    v.animate().x(widgetXOrigin).setDuration(250).start()
                    v.animate().y(widgetYOrigin).setDuration(250).start()

                }
                else -> {
                    return@setOnTouchListener false
                }
            }
            true
        }

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


    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

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

                if (::recyclerView.isInitialized){
                    recyclerView.adapter?.notifyDataSetChanged()
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

    private fun selectFontPath() {

        Toast.makeText(applicationContext, "Download ttf fonts & select them", Toast.LENGTH_LONG).show()
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE)
    }


}



