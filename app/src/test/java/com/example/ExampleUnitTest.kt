package com.example

import com.example.data.model.FileCategory
import com.example.data.model.formatFileSize
import com.example.util.QrCodeGenerator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testQrCodeGenerator() {
    val matrix = QrCodeGenerator.encode("http://192.168.1.5:8080")
    assertTrue(matrix.isNotEmpty())
    assertTrue(matrix[0].isNotEmpty())
    assertEquals(matrix.size, matrix[0].size)
  }

  @Test
  fun testFormatFileSize() {
    assertEquals("0 B", formatFileSize(0))
    assertEquals("1.0 KB", formatFileSize(1024))
    assertEquals("5.0 MB", formatFileSize(5 * 1024 * 1024))
  }

  @Test
  fun testCategoryDetection() {
    assertEquals(FileCategory.PHOTO, FileCategory.fromMime("image/png"))
    assertEquals(FileCategory.VIDEO, FileCategory.fromMime("video/mp4"))
    assertEquals(FileCategory.AUDIO, FileCategory.fromMime("audio/mpeg"))
    assertEquals(FileCategory.DOCUMENT, FileCategory.fromMime("application/pdf"))
  }
}
