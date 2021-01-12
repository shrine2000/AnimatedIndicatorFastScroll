package example.indicatorfastscroll


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Typeface
import android.os.Bundle
import android.os.UserManager
import android.widget.Button
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
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*


val appList: MutableList<AppModel> = mutableListOf()
lateinit var recyclerView: RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val fastScroller = findViewById<FastScrollerView>(R.id.fastscroller)
        val fastScrollerThumb = findViewById<FastScrollerThumbView>(R.id.fastscroller_thumb)
        val appPreferences = AppPreferences(this)
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

            Toast.makeText(applicationContext,"Download ttf fonts & select them", Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE)
        }

    }



