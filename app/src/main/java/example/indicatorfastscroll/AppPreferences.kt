package example.indicatorfastscroll

import android.content.Context
import android.content.SharedPreferences
import android.view.Gravity

class AppPreferences(context: Context) {

    private val CUSTOM_FONT_PATH = "CUSTOM_FONT_PATH"

    private val prefs: SharedPreferences = context.getSharedPreferences("prefs", 0);

    var customFontPath: String
        get() = prefs.getString(CUSTOM_FONT_PATH, "").toString()
        set(value) = prefs.edit().putString(CUSTOM_FONT_PATH, value).apply()


}