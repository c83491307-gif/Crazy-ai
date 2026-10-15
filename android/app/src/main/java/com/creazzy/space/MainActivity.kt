package com.creazzy.space

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.zip.ZipFile

class MainActivity : Activity() {
    private lateinit var list: LinearLayout
    private lateinit var search: EditText
    private var apps: List<AppRow> = emptyList()
    private var favoritesOnly = false
    private val favoritePackages: MutableSet<String> by lazy { getSharedPreferences("creazzy-space", MODE_PRIVATE).getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf() }
    private data class AppRow(val label: String, val packageName: String, val system: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(13, 17, 25)
        window.navigationBarColor = android.graphics.Color.rgb(13, 17, 25)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(12))
            setBackgroundColor(android.graphics.Color.rgb(13, 17, 25))
        }
        root.addView(TextView(this).apply {
            text = "CREAZZY SPACE"; textSize = 24f; setTextColor(0xFFC4F36B.toInt()); gravity = Gravity.START
        }, ViewGroup.LayoutParams(-1, -2))
        root.addView(TextView(this).apply {
            text = "مدير التطبيقات الأصلي · Android"; textSize = 14f
            setTextColor(0xFFF3F6FB.toInt()); setPadding(0, dp(4), 0, dp(12))
        })
        root.addView(TextView(this).apply {
            text = "إدارة وفتح التطبيقات واستيراد APK. لا ينشئ هذا الإصدار نسخاً معزولة."
            textSize = 12f; setTextColor(0xFFB8C5D5.toInt()); setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(0xFF17212E.toInt())
        }, ViewGroup.LayoutParams(-1, -2))
        search = EditText(this).apply {
            hint = "ابحث باسم التطبيق أو الحزمة"; textSize = 14f; setSingleLine(true)
            setTextColor(-1); setHintTextColor(0xFF94A3B8.toInt()); setBackgroundColor(0xFF17212E.toInt())
            setPadding(dp(12), 0, dp(12), 0)
            addTextChangedListener(SimpleTextWatcher { renderApps(it) })
        }
        root.addView(search, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(12) })
        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        buttons.addView(actionButton("تحديث القائمة") { loadApps() }, LinearLayout.LayoutParams(0, dp(46), 1f))
        buttons.addView(actionButton("استيراد APK") { pickApk() }, LinearLayout.LayoutParams(0, dp(46), 1f).apply { marginStart = dp(8) })
        root.addView(buttons, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        root.addView(actionButton("☆ عرض المفضلة فقط") { favoritesOnly = !favoritesOnly; renderApps(search.text?.toString().orEmpty()) }, LinearLayout.LayoutParams(-1, dp(44)).apply { topMargin = dp(6) })
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(list) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(12) })
        setContentView(root)
        loadApps()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun actionButton(label: String, click: () -> Unit) = Button(this).apply {
        text = label; isAllCaps = false; setOnClickListener { click() }
    }
    private fun loadApps() {
        apps = try {
            packageManager.getInstalledApplications(0).mapNotNull { info ->
                if (packageManager.getLaunchIntentForPackage(info.packageName) == null) null
                else AppRow(packageManager.getApplicationLabel(info).toString(), info.packageName,
                    (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0)
            }.sortedBy { it.label.lowercase(Locale.getDefault()) }
        } catch (e: Exception) {
            Toast.makeText(this, "تعذر قراءة التطبيقات: ${e.message}", Toast.LENGTH_LONG).show()
            emptyList()
        }
        renderApps(search.text?.toString().orEmpty())
    }
    private fun renderApps(query: String = "") {
        if (!::list.isInitialized) return
        list.removeAllViews()
        val q = query.trim().lowercase(Locale.getDefault())
        val filtered = apps.filter { (!favoritesOnly || favoritePackages.contains(it.packageName)) && (it.label.lowercase(Locale.getDefault()).contains(q) || it.packageName.lowercase(Locale.getDefault()).contains(q)) }
        list.addView(TextView(this).apply {
            text = "التطبيقات القابلة للفتح: ${filtered.size}"; textSize = 12f
            setTextColor(0xFF6DE3D2.toInt()); setPadding(0, dp(4), 0, dp(8))
        })
        filtered.forEach { app ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(10), dp(12), dp(10))
                setBackgroundColor(0xFF17212E.toInt())
            }
            card.addView(TextView(this).apply { text = app.label; textSize = 16f; setTextColor(-1) })
            card.addView(TextView(this).apply {
                text = app.packageName + if (app.system) " · نظام" else " · تطبيق"
                textSize = 11f; setTextColor(0xFF94A3B8.toInt()); setPadding(0, dp(3), 0, dp(7))
            })
            val rowButtons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            rowButtons.addView(Button(this).apply {
                text = "فتح التطبيق"; isAllCaps = false
                setOnClickListener {
                    val intent = packageManager.getLaunchIntentForPackage(app.packageName)
                    if (intent != null) try {
                        getSharedPreferences("creazzy-space", MODE_PRIVATE).edit().putString("last_opened", app.packageName).apply()
                        startActivity(intent)
                    } catch (_: Exception) { Toast.makeText(this@MainActivity, "تعذر فتح التطبيق", Toast.LENGTH_SHORT).show() }
                }
            }, LinearLayout.LayoutParams(0, -2, 1f))
            rowButtons.addView(Button(this).apply {
                text = if (favoritePackages.contains(app.packageName)) "★ مفضلة" else "☆ مفضلة"
                isAllCaps = false
                setOnClickListener {
                    if (!favoritePackages.add(app.packageName)) favoritePackages.remove(app.packageName)
                    getSharedPreferences("creazzy-space", MODE_PRIVATE).edit().putStringSet("favorites", favoritePackages.toSet()).apply()
                    renderApps(search.text?.toString().orEmpty())
                }
            }, LinearLayout.LayoutParams(0, -2, 1f))
            card.addView(rowButtons)
            list.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
        }
        if (filtered.isEmpty()) list.addView(TextView(this).apply {
            text = "لا توجد تطبيقات مطابقة."; setTextColor(0xFF94A3B8.toInt()); setPadding(0, dp(18), 0, dp(18))
        })
    }
    private fun pickApk() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type = "application/vnd.android.package-archive"
        }
        try { startActivityForResult(intent, REQUEST_APK) } catch (_: Exception) {
            Toast.makeText(this, "تعذر فتح منتقي الملفات", Toast.LENGTH_LONG).show()
        }
    }
    @Deprecated("Legacy activity result API retained for compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_APK || resultCode != RESULT_OK) return
        data?.data?.let { inspectAndInstall(it) }
    }
    private data class ApkPreflight(
        val label: String,
        val packageName: String,
        val version: String,
        val targetSdk: Int,
        val permissions: List<String>,
        val abis: List<String>,
        val warnings: List<String>
    )

    private fun inspectApk(apkFile: File): ApkPreflight? {
        val info = packageManager.getPackageArchiveInfo(
            apkFile.absolutePath,
            android.content.pm.PackageManager.GET_PERMISSIONS or
                android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
        ) ?: return null
        val label = info.applicationInfo?.loadLabel(packageManager)?.toString()
            ?.takeIf { it.isNotBlank() } ?: info.packageName
        val permissions = info.requestedPermissions?.toList().orEmpty()
        val abis = mutableSetOf<String>()
        try {
            ZipFile(apkFile).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val name = entries.nextElement().name
                    if (name.startsWith("lib/")) {
                        val parts = name.split("/")
                        if (parts.size >= 3 && parts[1].isNotBlank()) abis.add(parts[1])
                    }
                }
            }
        } catch (_: Exception) {
            // The package parser already accepted the file; ZIP diagnostics are best-effort.
        }
        val warnings = mutableListOf<String>()
        if (permissions.any { it.contains("com.google.android.c2dm", true) } ||
            info.packageName.startsWith("com.google.android.", true) ||
            info.packageName.startsWith("com.google.", true)) {
            warnings.add("قد يعتمد التطبيق على Google Play Services أو هوية Google؛ إعادة التوقيع أو تغيير الحزمة قد يعطّل تسجيل الدخول أو الإشعارات.")
        }
        if (permissions.any {
                it.contains("BILLING", true) ||
                    it.contains("INTEGRITY", true) ||
                    it.contains("CHECK_LICENSE", true)
            }) {
            warnings.add("تم رصد أذونات مرتبطة بالفوترة أو سلامة التطبيق؛ قد يرفض التطبيق نسخة معدّلة أو خارج المتجر.")
        }
        if (abis.isNotEmpty() && abis.none { it == "arm64-v8a" || it == "armeabi-v7a" }) {
            warnings.add("ملفات المكتبات الأصلية لا تتضمن ABI شائعاً لأجهزة ARM؛ تحقق من توافق الهاتف.")
        }
        if (permissions.isEmpty()) {
            warnings.add("لم تُستخرج قائمة أذونات من الحزمة؛ الفحص تمهيدي وليس تدقيقاً أمنياً كاملاً.")
        }
        warnings.add("هذا الفحص لا يثبت إمكانية الاستنساخ ولا يفحص سلوك الخادم أو التحقق من التوقيع داخل التطبيق.")
        return ApkPreflight(
            label = label,
            packageName = info.packageName,
            version = info.versionName ?: "غير معروف",
            targetSdk = info.applicationInfo?.targetSdkVersion ?: 0,
            permissions = permissions,
            abis = abis.sorted(),
            warnings = warnings
        )
    }

    private fun inspectAndInstall(uri: Uri) {
        var temporaryApk: File? = null
        try {
            val dir = File(cacheDir, "apks").apply { mkdirs() }
            val apkFile = File(dir, "selected-${System.currentTimeMillis()}.apk")
            temporaryApk = apkFile
            contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "لا يمكن قراءة الملف" }
                FileOutputStream(apkFile).use { output -> input.copyTo(output) }
            }
            val preflight = inspectApk(apkFile)
            if (preflight == null) {
                apkFile.delete()
                Toast.makeText(this, "الملف ليس APK صالحاً أو غير مدعوم", Toast.LENGTH_LONG).show()
                return
            }
            val warnings = preflight.warnings.joinToString("\n\n• ", prefix = "• ")
            val abiText = preflight.abis.takeIf { it.isNotEmpty() }?.joinToString() ?: "لم يتم العثور على مكتبات native"
            val permissionCount = preflight.permissions.size
            AlertDialog.Builder(this)
                .setTitle("فحص APK قبل المتابعة")
                .setMessage(
                    "الاسم: ${preflight.label}\n" +
                        "الحزمة: ${preflight.packageName}\n" +
                        "الإصدار: ${preflight.version}\n" +
                        "Target SDK: ${preflight.targetSdk}\n" +
                        "عدد الأذونات المعلنة: $permissionCount\n" +
                        "معماريات native: $abiText\n\n" +
                        "ملاحظات التوافق:\n$warnings\n\n" +
                        "الخطوة التالية تثبيت الملف عبر Android فقط؛ هذا الإصدار لا ينشئ نسخة مستنسخة."
                )
                .setNegativeButton("إلغاء") { _, _ -> apkFile.delete() }
                .setPositiveButton("متابعة إلى مثبت Android") { _, _ -> launchInstaller(apkFile) }
                .setOnCancelListener { apkFile.delete() }
                .show()
        } catch (e: Exception) {
            temporaryApk?.delete()
            Toast.makeText(this, "تعذر فحص APK: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    private fun launchInstaller(apk: File) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 26 && !packageManager.canRequestPackageInstalls()) {
                AlertDialog.Builder(this).setTitle("السماح بتثبيت ملفات APK")
                    .setMessage("Android يتطلب تفعيل إذن التثبيت لهذا التطبيق من الإعدادات.")
                    .setNegativeButton("إلغاء", null)
                    .setPositiveButton("فتح الإعدادات") { _, _ ->
                        startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")))
                    }.show()
                return
            }
            val uri = FileProvider.getUriForFile(this, "$packageName.files", apk)
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
        } catch (e: Exception) {
            Toast.makeText(this, "تعذر فتح المثبت: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    companion object { private const val REQUEST_APK = 4102 }
}
