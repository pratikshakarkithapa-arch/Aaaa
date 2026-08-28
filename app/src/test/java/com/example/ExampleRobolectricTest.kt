package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AspectRatioOption
import com.example.model.ImageStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AI Image Generator", appName)
  }

  @Test
  fun `verify image styles count and presets`() {
    assertEquals(4, ImageStyle.values().size)
    assertNotNull(ImageStyle.REALISTIC)
    assertNotNull(ImageStyle.ANIME)
    assertNotNull(ImageStyle.THREE_D)
    assertNotNull(ImageStyle.DREAMCORE)
  }

  @Test
  fun `verify aspect ratios`() {
    assertEquals(4, AspectRatioOption.values().size)
    assertNotNull(AspectRatioOption.SQUARE)
    assertNotNull(AspectRatioOption.PORTRAIT_9_16)
    assertNotNull(AspectRatioOption.LANDSCAPE_16_9)
    assertNotNull(AspectRatioOption.PORTRAIT_3_4)
  }
}
