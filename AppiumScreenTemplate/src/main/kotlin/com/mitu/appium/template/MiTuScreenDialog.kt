package com.mitu.appium.template

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.psi.PsiDirectory
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class MiTuScreenDialog(
    project: Project,
    private val screenTargetDir: PsiDirectory,
    /** READ-ONLY finder: must NOT create folders */
    private val testRootFinder: () -> PsiDirectory?,
    private val testPackagePath: String = "com/dt/tests",
) : DialogWrapper(project, true) {

    private val nameField = JTextField()
    private val createTestCheck = JCheckBox("Also create test class", true)

    init {
        title = "Create New Screen"
        init()
    }

    override fun getPreferredSize(): Dimension = Dimension(460, 140)

    override fun getPreferredFocusedComponent(): JComponent = nameField

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(0, 10))

        val form = JPanel()
        form.layout = BoxLayout(form, BoxLayout.Y_AXIS)

        val row = JPanel(BorderLayout(8, 0))
        row.add(JLabel("Screen name:"), BorderLayout.WEST)

        nameField.columns = 30
        nameField.toolTipText = "Example: Onboarding / Home / Order Detail"
        row.add(nameField, BorderLayout.CENTER)

        form.add(row)
        form.add(Box.createVerticalStrut(8))

        val checkRow = JPanel(BorderLayout(8, 0))
        checkRow.add(createTestCheck, BorderLayout.WEST)
        form.add(checkRow)

        panel.add(form, BorderLayout.CENTER)
        return panel
    }

    /**
     * READ-ONLY validation. DO NOT create directories here.
     */
    override fun doValidate(): ValidationInfo? {
        val raw = getRawName()
        if (raw.isBlank()) return ValidationInfo("Please enter a screen name.", nameField)
        if (raw.length > 80) return ValidationInfo("Screen name is too long (max 80 characters).", nameField)

        val baseName = normalizeToPascalCase(raw)
        if (baseName.isBlank()) {
            return ValidationInfo("Screen name is invalid. Please use letters/numbers/spaces/underscore.", nameField)
        }

        val idError = validateJavaLikeIdentifier(baseName)
        if (idError != null) return ValidationInfo(idError, nameField)

        // Screen file check (read-only)
        val screenFileName = "${baseName}Screen.java"
        if (screenTargetDir.findFile(screenFileName) != null) {
            return ValidationInfo("Tên trùng rồi, hãy tạo tên khác. ($screenFileName)", nameField)
        }

        // Test check (read-only) - DO NOT create package dirs
        if (createTestCheck.isSelected) {
            val testRoot = testRootFinder.invoke()
                ?: return ValidationInfo("Cannot find test source root (src/test/java). It will be created when generating.", createTestCheck)

            val pkgDir = findPackageDir(testRoot, testPackagePath) // read-only
            if (pkgDir != null) {
                val testFileName = "${baseName}Test.java"
                if (pkgDir.findFile(testFileName) != null) {
                    return ValidationInfo("Tên trùng rồi, hãy tạo tên khác. ($testPackagePath/$testFileName)", createTestCheck)
                }
            }
            // Nếu package chưa tồn tại: không coi là lỗi, vì action sẽ tạo khi generate.
        }

        return null
    }

    /**
     * Show error INSIDE dialog only when user clicks OK.
     */
    override fun doOKAction() {
        val info = doValidate()
        if (info != null) {
            setErrorText(info.message, info.component)
            return
        }
        setErrorText(null, null)
        super.doOKAction()
    }

    fun getRawName(): String = nameField.text?.trim().orEmpty()
    fun shouldCreateTest(): Boolean = createTestCheck.isSelected

    fun normalizeToPascalCase(raw: String): String {
        val cleaned = raw.trim()
            .replace(Regex("[^\\p{L}\\p{N}_\\-\\s]"), " ")
            .replace('-', ' ')
            .replace('_', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleaned.isBlank()) return ""

        val parts = cleaned.split(" ").filter { it.isNotBlank() }
        val joined = parts.joinToString("") { token ->
            token.lowercase().replaceFirstChar { c -> c.titlecase() }
        }

        return if (joined.isNotEmpty() && joined.first().isDigit()) "S$joined" else joined
    }

    private fun validateJavaLikeIdentifier(name: String): String? {
        if (!Regex("^[A-Za-z_][A-Za-z0-9_]*$").matches(name)) {
            return "Invalid class name: '$name'. Use only letters, digits, and underscore."
        }

        val keywords = setOf(
            "abstract","assert","boolean","break","byte","case","catch","char","class","const","continue",
            "default","do","double","else","enum","extends","final","finally","float","for","goto","if",
            "implements","import","instanceof","int","interface","long","native","new","package","private",
            "protected","public","return","short","static","strictfp","super","switch","synchronized","this",
            "throw","throws","transient","try","void","volatile","while",
            "true","false","null","var","record","yield","sealed","permits","non-sealed"
        )
        return if (keywords.contains(name)) "Class name cannot be a Java keyword: '$name'." else null
    }

    /** READ-ONLY: return null if package path doesn't exist */
    private fun findPackageDir(base: PsiDirectory, pkg: String): PsiDirectory? {
        var current: PsiDirectory? = base
        for (part in pkg.split("/").filter { it.isNotBlank() }) {
            current = current?.findSubdirectory(part) ?: return null
        }
        return current
    }
}
