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

  @Test
  fun testActiveTransferProgress() {
    val progress50 = com.example.data.model.ActiveTransferProgress(
      id = "1",
      fileName = "video.mp4",
      bytesTransferred = 50 * 1024 * 1024,
      totalBytes = 100 * 1024 * 1024,
      speedMBps = 10f,
      isActive = true
    )
    assertEquals(0.5f, progress50.progress, 0.001f)
    assertEquals(50, progress50.percentage)
    assertEquals(5, progress50.etaSeconds)

    val progress100 = progress50.copy(bytesTransferred = 100 * 1024 * 1024)
    assertEquals(1.0f, progress100.progress, 0.001f)
    assertEquals(100, progress100.percentage)
    assertEquals(0, progress100.etaSeconds)
  }
}
