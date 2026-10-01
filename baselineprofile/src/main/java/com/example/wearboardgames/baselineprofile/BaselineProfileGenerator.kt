package com.example.wearboardgames.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startupAndCoreJourney() = rule.collect(
        packageName = PACKAGE_NAME,
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
        device.waitForIdle()

        // Compile the real home scroll/category/game path, not only Activity startup.
        clickTextWithScroll("街机经典")
        device.wait(Until.hasObject(By.text("俄罗斯方块")), 3_000)
        clickTextWithScroll("俄罗斯方块")
        device.wait(Until.hasObject(By.textContains("俄罗斯方块")), 3_000)
        device.pressBack()
        device.waitForIdle()
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.clickTextWithScroll(text: String) {
        repeat(8) {
            device.findObject(By.text(text))?.let {
                it.click()
                device.waitForIdle()
                return
            }
            val w = device.displayWidth
            val h = device.displayHeight
            device.swipe(w / 2, (h * .78f).toInt(), w / 2, (h * .30f).toInt(), 8)
            device.waitForIdle()
        }
        error("Could not find UI text: $text")
    }

    private companion object {
        const val PACKAGE_NAME = "com.example.wearboardgames"
    }
}
