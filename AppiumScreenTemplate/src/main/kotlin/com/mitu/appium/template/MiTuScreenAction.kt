package com.mitu.appium.template

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.util.IncorrectOperationException
import java.util.Properties

class MiTuScreenAction : AnAction() {

    private val testPackagePath = "com/dt/tests"

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val dir = resolveTargetDirectory(e, project) ?: run {
            Messages.showErrorDialog(project, "Please select a target directory in Project view.", "MiTu Appium Templates")
            return
        }

        // READ-ONLY finder for dialog (must not create directories)
        val testRootFinder = { findTestJavaRoot(project) }

        val dialog = MiTuScreenDialog(
            project = project,
            screenTargetDir = dir,
            testRootFinder = testRootFinder,
            testPackagePath = testPackagePath
        )

        if (!dialog.showAndGet()) return

        val author = System.getProperty("user.name") ?: "user"
        val createdAt = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM, yyyy"))

        val raw = dialog.getRawName()
        val baseName = dialog.normalizeToPascalCase(raw)
        val createTest = dialog.shouldCreateTest()

        val screenClassName = "${baseName}Screen"
        val screenFileName = "$screenClassName.java"

        val testClassName = "${baseName}Test"
        val testFileName = "$testClassName.java"

        // Defensive check before write
        if (dir.findFile(screenFileName) != null) {
            Messages.showErrorDialog(project, "Tên trùng rồi, hãy tạo tên khác. ($screenFileName)", "MiTu Appium Templates")
            return
        }

        try {
            WriteCommandAction.runWriteCommandAction(project, "Create Appium Screen", null, Runnable {
                // 1) Create Screen file
                val screenTemplate = FileTemplateManager.getInstance(project).getInternalTemplate("MiTuScreen")
                val screenProps = Properties().apply {
                    put("CLASS_NAME", screenClassName)
                    put("AUTHOR", author)
                    put("CREATED", createdAt)
                }
                FileTemplateUtil.createFromTemplate(screenTemplate, screenFileName, screenProps, dir)

                // 2) Create Test file + create dirs (WRITE) if requested
                if (createTest) {
                    val testTemplate = FileTemplateManager.getInstance(project).getInternalTemplate("MiTuTest")

                    val testProps = Properties().apply {
                        put("CLASS_NAME", testClassName)
                        put("SCREEN_CLASS", screenClassName)
                        put("VARIABLE_NAME", lowerFirstChar(screenClassName))
                        put("BASE_NAME", baseName)
                        put("BASE_NAME_LOWER", lowerFirstChar(baseName))
                        put("AUTHOR", author)
                        put("CREATED", createdAt)
                    }

                    val testRoot = getOrCreateTestJavaRoot(project)
                        ?: throw IncorrectOperationException("Cannot find/create test source root (src/test/java).")

                    val testPackageDir = ensurePackageDir(testRoot, testPackagePath) // WRITE OK (inside write action)

                    if (testPackageDir.findFile(testFileName) != null) {
                        throw IncorrectOperationException("Test file already exists: $testPackagePath/$testFileName")
                    }

                    FileTemplateUtil.createFromTemplate(testTemplate, testFileName, testProps, testPackageDir)
                }
            })
        } catch (ex: IncorrectOperationException) {
            Messages.showErrorDialog(project, ex.message ?: "Failed to create file(s).", "MiTu Appium Templates")
        } catch (t: Throwable) {
            Messages.showErrorDialog(project, t.message ?: t.toString(), "MiTu Appium Templates")
        }
    }

    private fun resolveTargetDirectory(e: AnActionEvent, project: Project): PsiDirectory? {
        val selectedDir = e.getData(CommonDataKeys.PSI_ELEMENT) as? PsiDirectory
        if (selectedDir != null) return selectedDir

        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        if (psiFile != null) return psiFile.containingDirectory

        val vFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (vFile != null && vFile.isDirectory) {
            return PsiManager.getInstance(project).findDirectory(vFile)
        }

        return null
    }

    /** READ-ONLY: find existing src/test/java (do not create) */
    private fun findTestJavaRoot(project: Project): PsiDirectory? {
        val rootManager = ProjectRootManager.getInstance(project)
        val fileIndex = rootManager.fileIndex
        val psiManager = PsiManager.getInstance(project)

        // If IDE already knows test roots
        rootManager.contentSourceRoots.firstOrNull { fileIndex.isInTestSourceContent(it) }?.let { root ->
            psiManager.findDirectory(root)?.let { return it }
        }

        // Try locate <contentRoot>/src/test/java without creating
        val contentRoot = rootManager.contentRoots.firstOrNull() ?: return null
        val contentDir = psiManager.findDirectory(contentRoot) ?: return null

        val src = contentDir.findSubdirectory("src") ?: return null
        val test = src.findSubdirectory("test") ?: return null
        val java = test.findSubdirectory("java") ?: return null
        return java
    }

    /** WRITE: create <contentRoot>/src/test/java if missing */
    private fun getOrCreateTestJavaRoot(project: Project): PsiDirectory? {
        val rootManager = ProjectRootManager.getInstance(project)
        val fileIndex = rootManager.fileIndex
        val psiManager = PsiManager.getInstance(project)

        rootManager.contentSourceRoots.firstOrNull { fileIndex.isInTestSourceContent(it) }?.let { root ->
            psiManager.findDirectory(root)?.let { return it }
        }

        val contentRoot: VirtualFile = rootManager.contentRoots.firstOrNull() ?: return null
        val contentDir = psiManager.findDirectory(contentRoot) ?: return null

        val srcDir = contentDir.findSubdirectory("src") ?: contentDir.createSubdirectory("src")
        val testDir = srcDir.findSubdirectory("test") ?: srcDir.createSubdirectory("test")
        val javaDir = testDir.findSubdirectory("java") ?: testDir.createSubdirectory("java")
        return javaDir
    }

    /** WRITE: create package path */
    private fun ensurePackageDir(base: PsiDirectory, pkg: String): PsiDirectory {
        var current = base
        pkg.split("/").filter { it.isNotBlank() }.forEach { part ->
            current = current.findSubdirectory(part) ?: current.createSubdirectory(part)
        }
        return current
    }

    private fun lowerFirstChar(name: String): String {
        if (name.isBlank()) return name
        return name.replaceFirstChar { it.lowercase() }
    }
}
