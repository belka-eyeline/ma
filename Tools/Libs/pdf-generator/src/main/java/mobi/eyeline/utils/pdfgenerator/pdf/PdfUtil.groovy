package mobi.eyeline.utils.pdfgenerator.pdf

import groovy.transform.CompileStatic
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.util.Matrix

import java.awt.geom.Point2D.Float as Float2D

@CompileStatic
class PdfUtil {

  static final Float2D ZERO_OFFSET = new Float2D(0, 0)

  static Matrix getCenterMatrix(PDPage page,
                                String text,
                                PDFont font,
                                int fontSize,
                                Float2D offset = ZERO_OFFSET) {

    final pageIsLandscape = isLandscape page
    final pageCenter = centerOf page

    float stringWidth = getStringWidth text, font, fontSize
    if (pageIsLandscape) {
      final textX = pageCenter.x - stringWidth / 2F + offset.x
      final textY = pageCenter.y - offset.y
      return Matrix.getRotateInstance(Math.PI / 2, textY as float, textX as float)

    } else {
      final textX = pageCenter.x - stringWidth / 2F + offset.x
      final textY = pageCenter.y + offset.y
      return Matrix.getTranslateInstance(textX as float, textY as float)
    }
  }

  static void addCenteredText(PDPageContentStream content,
                              PDPage page,
                              String text,
                              PDFont font,
                              int fontSize,
                              Float2D offset = ZERO_OFFSET) {

    content.setFont font, fontSize
    content.beginText()

    content.textMatrix = getCenterMatrix page, text, font, fontSize, offset

    content.showText text
    content.endText()
  }

  static boolean isLandscape(PDPage page) {
    page.rotation == 90 || page.rotation == 270
  }

  static Float2D centerOf(PDPage page) {
    final pageSize = page.mediaBox
    new Float2D(pageSize.width / 2.0 as float, pageSize.height / 2.0 as float)
  }

  static float getStringWidth(String text, PDFont font, int fontSize) {
    font.getStringWidth(text) * fontSize / 1000F
  }

}
