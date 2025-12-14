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
    private val testTargetDirProvider: () -> PsiDirectory?,
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
        form.add(createTestCheck)

        panel.add(form, BorderLayout.CENTER)
        return panel
    }

    /**
     * IMPORTANT:
     * - doValidate() just returns ValidationInfo (no UI side-effects).
     * - doOKAction() is responsible for showing error text inside the dialog when user clicks OK.
     */
    override fun doValidate(): ValidationInfo? {
        val raw = getRawName()
        if (raw.isBlank()) {
            return ValidationInfo("Please enter a screen name.", nameField)
        }
        if (raw.length > 80) {
            return ValidationInfo("Screen name is too long (max 80 characters).", nameField)
        }

        val baseName = normalizeToPascalCase(raw)
        if (baseName.isBlank()) {
            return ValidationInfo("Screen name is invalid. Please use letters/numbers/spaces/underscore.", nameField)
        }

        // Validate a safe Java class identifier without relying on Java PSI (more robust for build/runIde).
        val identifierError = validateJavaLikeIdentifier(baseName)
        if (identifierError != null) {
            return ValidationInfo(identifierError, nameField)
        }

        // Screen file
        val screenClassName = "${baseName}Screen"
        val screenFileName = "$screenClassName.java"
        if (screenTargetDir.findFile(screenFileName) != null) {
            return ValidationInfo("Tên trùng rồi, hãy tạo tên khác. ($screenFileName)", nameField)
        }

        // Optional test file
        if (createTestCheck.isSelected) {
            val testRoot = testTargetDirProvider.invoke()
                ?: return ValidationInfo("Cannot find/create test source root (src/test/java).", createTestCheck)

            val testPackageDir = ensurePackageDir(testRoot, testPackagePath)

            val testClassName = "${baseName}Test"
            val testFileName = "$testClassName.java"
            if (testPackageDir.findFile(testFileName) != null) {
                return ValidationInfo("Tên trùng rồi, hãy tạo tên khác. ($testPackagePath/$testFileName)", createTestCheck)
            }
        }

        return null
    }

    /**
     * Only show errors when user clicks OK.
     * - If invalid: show error text inside dialog and keep dialog open.
     * - If valid: clear error and close.
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

    /**
     * Convert raw input to PascalCase base name.
     * Examples:
     *  - "home" -> "Home"
     *  - "order detail" -> "OrderDetail"
     *  - "order_detail" -> "OrderDetail"
     *  - "  onboarding  screen " -> "OnboardingScreen"
     *
     * Keeps only letters/digits/space/underscore/hyphen.
     */
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

        // Class cannot start with digit → prefix S
        return if (joined.isNotEmpty() && joined.first().isDigit()) "S$joined" else joined
    }

    /**
     * Minimal-but-strong validation for Java-style class names:
     * - Must start with letter or underscore
     * - Must contain only letters/digits/underscore
     * - Avoid common Java keywords
     */
    private fun validateJavaLikeIdentifier(name: String): String? {
        // Basic identifier check
        val ok = Regex("^[A-Za-z_][A-Za-z0-9_]*$").matches(name)
        if (!ok) return "Invalid class name: '$name'. Use only letters, digits, and underscore."

        // Common Java keywords (enough for real-world guard)
        val keywords = setOf(
            "abstract","assert","boolean","break","byte","case","catch","char","class","const","continue",
            "default","do","double","else","enum","extends","final","finally","float","for","goto","if",
            "implements","import","instanceof","int","interface","long","native","new","package","private",
            "protected","public","return","short","static","strictfp","super","switch","synchronized","this",
            "throw","throws","transient","try","void","volatile","while",
            // literals / reserved in newer Java
            "true","false","null","var","record","yield","sealed","permits","non-sealed"
        )
        if (keywords.contains(name)) return "Class name cannot be a Java keyword: '$name'."

        return null
    }

    private fun ensurePackageDir(base: PsiDirectory, pkg: String): PsiDirectory {
        var current = base
        pkg.split("/").filter { it.isNotBlank() }.forEach { part ->
            current = current.findSubdirectory(part) ?: current.createSubdirectory(part)
        }
        return current
    }
}
